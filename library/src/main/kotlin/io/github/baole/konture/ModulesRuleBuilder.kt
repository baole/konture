/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.LogicalOperator

/**
 * A builder for compiling and verifying architectural rules on project modules.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against a project structure by calling [check].
 */
@KontureDsl
public class ModulesRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
    internal val sourceSets: SourceSetSelector? = null,
) {
    private var thatPredicate: ((Module) -> Boolean)? = null
    private var shouldAssertion: ((Module, ProjectGraph, MutableList<String>) -> Unit)? = null

    private var activeOperator = LogicalOperator.AND
    private var negateNext = false

    private var activeShouldOperator = LogicalOperator.AND
    private var negateNextShould = false
    private var allowEmpty = false

    /**
     * Debugging helper that prints information about all modules matched by the `that()` filter.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printMatchedModules(
        logger: (Module) -> Unit = {
            println(getMessage("debug.modules.matched", it.path, it.projectDir, it.appliedPlugins))
        },
    ): ModulesRuleBuilder =
        this.apply {
            setShould { module, _, _ ->
                logger(module)
            }
        }

    /**
     * Debugging helper that prints information about all discovered modules in the project graph.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printAllModules(
        logger: (Module) -> Unit = {
            println(getMessage("debug.modules.discovered", it.path, it.projectDir, it.appliedPlugins))
        },
    ): ModulesRuleBuilder =
        this.apply {
            graph.getAllModules().forEach(logger)
        }

    private val ignoredPredicates = mutableListOf<(Module) -> Boolean>()

    /**
     * Configures this builder to allow empty selections (i.e. if no modules match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    public fun allowEmpty(): ModulesRuleBuilder {
        allowEmpty = true
        return this
    }

    /**
     * Configures this builder to ignore failures for modules satisfying the given predicate.
     */
    public fun ignoreFailuresIn(predicate: (Module) -> Boolean): ModulesRuleBuilder {
        ignoredPredicates.add(predicate)
        return this
    }

    /**
     * Configures this builder to ignore failures for modules matching any of the specified paths or patterns.
     */
    public fun ignoreFailuresIn(vararg modulePaths: String): ModulesRuleBuilder {
        ignoredPredicates.add { module ->
            modulePaths.any { path ->
                module.path == path || io.github.baole.konture.impl.PatternMatchers.matchesModuleGlob(path, module.path)
            }
        }
        return this
    }

    internal fun getThatPredicate(): ((Module) -> Boolean)? = thatPredicate

    internal fun getShouldAssertion(): ((Module, ProjectGraph, MutableList<String>) -> Unit)? = shouldAssertion

    /**
     * Starts adding filtering conditions to select which modules to verify.
     */
    public fun that(): ModulesThat = ModulesThat(this)

    /**
     * Starts adding assertion rules that the selected modules must satisfy.
     */
    public fun should(): ModulesShould = ModulesShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    public fun and(): ModulesThat {
        activeOperator = LogicalOperator.AND
        return ModulesThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    public fun or(): ModulesThat {
        activeOperator = LogicalOperator.OR
        return ModulesThat(this)
    }

    /**
     * Logical XOR (Exclusive OR) operator for chaining filter conditions.
     */
    public fun xor(): ModulesThat {
        activeOperator = LogicalOperator.XOR
        return ModulesThat(this)
    }

    /**
     * Logical NOT operator for negating the next filter condition.
     */
    public fun not(): ModulesThat {
        negateNext = true
        return ModulesThat(this)
    }

    /**
     * Logical AND operator for chaining assertion conditions.
     */
    public fun andShould(): ModulesShould {
        activeShouldOperator = LogicalOperator.AND
        return ModulesShould(this)
    }

    /**
     * Logical OR operator for chaining assertion conditions.
     */
    public fun orShould(): ModulesShould {
        activeShouldOperator = LogicalOperator.OR
        return ModulesShould(this)
    }

    /**
     * Logical XOR (Exclusive OR) operator for chaining assertion conditions.
     */
    public fun xorShould(): ModulesShould {
        activeShouldOperator = LogicalOperator.XOR
        return ModulesShould(this)
    }

    /**
     * Logical NOT operator for negating the next assertion condition.
     */
    public fun notShould(): ModulesShould {
        negateNextShould = true
        return ModulesShould(this)
    }

    internal fun setThat(predicate: (Module) -> Boolean) {
        /** Filter or assertion criteria for actual predicate. */
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                /** Filter or assertion criteria for p. */
                val p = { m: Module -> !predicate(m) }
                p
            } else {
                predicate
            }

        /** Filter or assertion criteria for current. */
        val current = thatPredicate
        if (current == null) {
            thatPredicate = actualPredicate
        } else {
            /** Filter or assertion criteria for op. */
            val op = activeOperator
            thatPredicate =
                when (op) {
                    LogicalOperator.OR -> {
                        { current(it) || actualPredicate(it) }
                    }

                    LogicalOperator.XOR -> {
                        { current(it) xor actualPredicate(it) }
                    }

                    LogicalOperator.AND -> {
                        { current(it) && actualPredicate(it) }
                    }
                }
            activeOperator = LogicalOperator.AND
        }
    }

    internal fun setShould(assertion: (Module, ProjectGraph, MutableList<String>) -> Unit) {
        /** Filter or assertion criteria for actual assertion. */
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                /** Filter or assertion criteria for a. */
                val a = { module: Module, g: ProjectGraph, violations: MutableList<String> ->
                    /** Filter or assertion criteria for temp violations. */
                    val tempViolations = mutableListOf<String>()
                    assertion(module, g, tempViolations)
                    if (tempViolations.isEmpty()) {
                        violations.add(
                            getMessage("modules.rule.negatedSatisfied", module.path),
                        )
                    }
                }
                a
            } else {
                assertion
            }

        /** Filter or assertion criteria for current. */
        val current = shouldAssertion
        if (current == null) {
            shouldAssertion = actualAssertion
        } else {
            /** Filter or assertion criteria for op. */
            val op = activeShouldOperator
            if (op == LogicalOperator.OR) {
                shouldAssertion = { module, g, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(module, g, temp1)
                    actualAssertion(module, g, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            getMessage("modules.rule.eitherOr", module.path, temp1.joinToString("; "), temp2.joinToString("; ")),
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { module, g, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(module, g, temp1)
                    actualAssertion(module, g, temp2)
                    /** Filter or assertion criteria for ok1. */
                    val ok1 = temp1.isEmpty()

                    /** Filter or assertion criteria for ok2. */
                    val ok2 = temp2.isEmpty()
                    if (ok1 == ok2) {
                        violations.add(
                            getMessage("modules.rule.xor", module.path),
                        )
                    }
                }
            } else {
                shouldAssertion = { module, g, violations ->
                    current(module, g, violations)
                    actualAssertion(module, g, violations)
                }
            }
            activeShouldOperator = LogicalOperator.AND
        }
    }

    /**
     * Executes the built module rules against the specified project graph.
     *
     * @param g The [ProjectGraph] to check. Defaults to the lazy-loaded project graph.
     * @throws AssertionError If any of the verified modules violate the assertion rules.
     */
    public fun check(g: ProjectGraph = graph): ViolationReport {
        /** Filter or assertion criteria for all modules. */
        val allModules = g.getAllModules()

        /** Filter or assertion criteria for modules to check. */
        val modulesToCheck = allModules.filter { thatPredicate?.invoke(it) ?: true }
        KontureLogger.log(
            LogLevel.DEBUG,
            "Checking Modules Rules: found ${allModules.size} modules total. Selected ${modulesToCheck.size} modules to verify.",
        )
        if (modulesToCheck.isEmpty()) {
            if (!allowEmpty) {
                throw AssertionError(
                    getMessage("modules.rule.emptySelect"),
                )
            } else {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "No modules matched the filter 'that()'. Rule silently succeeded as allowEmpty is enabled.",
                )
            }
        }

        /** Filter or assertion criteria for assertion. */
        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("modules.rule.noAssertion"),
            )

        /** Filter or assertion criteria for run check. */
        val runCheckReport = { list: MutableList<Violation> ->
            for (module in modulesToCheck) {
                if (ignoredPredicates.any { it(module) }) continue
                val rawMessages = mutableListOf<String>()
                assertion(module, g, rawMessages)
                for (rawMsg in rawMessages) {
                    val subject =
                        Subject.ModuleSubject(
                            path = module.path,
                            location = SourceLocation(filePath = module.projectDir),
                        )
                    list.add(
                        Violation(
                            ruleId = "modules.rule",
                            subject = subject,
                            message = rawMsg,
                            severity = Severity.ERROR,
                        ),
                    )
                }
            }
        }

        return BaselineManager.checkRuleReport(
            ruleId = "modules.rule",
            violationHeader = getMessage("modules.rule.violationHeader"),
            runCheckReport = runCheckReport,
        )
    }
}
