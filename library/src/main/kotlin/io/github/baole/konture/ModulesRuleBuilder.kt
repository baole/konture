/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
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
class ModulesRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
) {
    private var thatPredicate: ((Module) -> Boolean)? = null
    private var shouldAssertion: ((Module, ProjectGraph, MutableList<String>) -> Unit)? = null

    private var activeOperator = LogicalOperator.AND
    private var negateNext = false

    private var activeShouldOperator = LogicalOperator.AND
    private var negateNextShould = false
    private var allowEmpty = false

    /**
     * Configures this builder to allow empty selections (i.e. if no modules match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    fun allowEmpty(): ModulesRuleBuilder {
        allowEmpty = true
        return this
    }

    internal fun getThatPredicate(): ((Module) -> Boolean)? = thatPredicate

    internal fun getShouldAssertion(): ((Module, ProjectGraph, MutableList<String>) -> Unit)? = shouldAssertion

    /**
     * Starts adding filtering conditions to select which modules to verify.
     */
    fun that(): ModulesThat = ModulesThat(this)

    /**
     * Starts adding assertion rules that the selected modules must satisfy.
     */
    fun should(): ModulesShould = ModulesShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    fun and(): ModulesThat {
        activeOperator = LogicalOperator.AND
        return ModulesThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    fun or(): ModulesThat {
        activeOperator = LogicalOperator.OR
        return ModulesThat(this)
    }

    /**
     * Logical XOR (Exclusive OR) operator for chaining filter conditions.
     */
    fun xor(): ModulesThat {
        activeOperator = LogicalOperator.XOR
        return ModulesThat(this)
    }

    /**
     * Logical NOT operator for negating the next filter condition.
     */
    fun not(): ModulesThat {
        negateNext = true
        return ModulesThat(this)
    }

    /**
     * Logical AND operator for chaining assertion conditions.
     */
    fun andShould(): ModulesShould {
        activeShouldOperator = LogicalOperator.AND
        return ModulesShould(this)
    }

    /**
     * Logical OR operator for chaining assertion conditions.
     */
    fun orShould(): ModulesShould {
        activeShouldOperator = LogicalOperator.OR
        return ModulesShould(this)
    }

    /**
     * Logical XOR (Exclusive OR) operator for chaining assertion conditions.
     */
    fun xorShould(): ModulesShould {
        activeShouldOperator = LogicalOperator.XOR
        return ModulesShould(this)
    }

    /**
     * Logical NOT operator for negating the next assertion condition.
     */
    fun notShould(): ModulesShould {
        negateNextShould = true
        return ModulesShould(this)
    }

    internal fun setThat(predicate: (Module) -> Boolean) {
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                val p = { m: Module -> !predicate(m) }
                p
            } else {
                predicate
            }

        val current = thatPredicate
        if (current == null) {
            thatPredicate = actualPredicate
        } else {
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
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                val a = { module: Module, g: ProjectGraph, violations: MutableList<String> ->
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

        val current = shouldAssertion
        if (current == null) {
            shouldAssertion = actualAssertion
        } else {
            val op = activeShouldOperator
            if (op == LogicalOperator.OR) {
                shouldAssertion = { module, g, violations ->
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(module, g, temp1)
                    actualAssertion(module, g, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            getMessage("modules.rule.eitherOr", module.path, temp1.joinToString(), temp2.joinToString()),
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { module, g, violations ->
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(module, g, temp1)
                    actualAssertion(module, g, temp2)
                    val ok1 = temp1.isEmpty()
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
    fun check(g: ProjectGraph = graph) {
        val allModules = g.getAllModules()
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

        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("modules.rule.noAssertion"),
            )

        val runCheck = { list: MutableList<String> ->
            for (module in modulesToCheck) {
                assertion(module, g, list)
            }
        }

        BaselineManager.checkRule(
            getMessage("modules.rule.violationHeader"),
            runCheck,
        )
    }
}
