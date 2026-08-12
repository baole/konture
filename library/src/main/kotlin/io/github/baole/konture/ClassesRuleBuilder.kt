/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.LogicalOperator
import io.github.baole.konture.impl.ViolationLocation

/**
 * A builder for compiling and verifying architectural rules on Kotlin classes.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against all project classes by calling [check].
 */
@KontureDsl
public class ClassesRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
    private val sourceSets: SourceSetSelector = SourceSets.production(),
) {
    private var thatPredicate: ((ClassDeclaration) -> Boolean)? = null
    private var shouldAssertion: (
        (
            ClassDeclaration,
            List<ClassDeclaration>,
            MutableList<String>,
        ) -> Unit
    )? = null

    private var activeOperator = LogicalOperator.AND
    private var negateNext = false

    private var activeShouldOperator = LogicalOperator.AND
    private var negateNextShould = false
    private var allowEmpty = false

    /**
     * Debugging helper that prints information about all classes matched by the `that()` filter.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printMatchedClasses(
        logger: (ClassDeclaration) -> Unit = {
            println(getMessage("debug.classes.matched", it.fqName, ViolationLocation.format(it), it.supertypes))
        },
    ): ClassesRuleBuilder =
        this.apply {
            setShould { cls, _, _ ->
                logger(cls)
            }
        }

    /**
     * Debugging helper that prints information about all discovered classes in the project graph.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printAllClasses(
        logger: (ClassDeclaration) -> Unit = {
            println(getMessage("debug.classes.discovered", it.fqName, ViolationLocation.format(it), it.supertypes))
        },
    ): ClassesRuleBuilder =
        this.apply {
            graph.getAllModules().flatMap { module ->
                module.files.flatMap { file -> file.classes }
            }.distinctBy { it.fqName }.forEach(logger)
        }

    private val ignoredPredicates = mutableListOf<(ClassDeclaration) -> Boolean>()

    /**
     * Configures this builder to allow empty selections (i.e. if no classes match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    public fun allowEmpty(): ClassesRuleBuilder {
        allowEmpty = true
        return this
    }

    /**
     * Configures this builder to ignore failures for classes satisfying the given predicate.
     */
    public fun ignoreFailuresIn(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        ignoredPredicates.add(predicate)
        return this
    }

    /**
     * Configures this builder to ignore failures for classes matching any of the specified names or patterns.
     */
    public fun ignoreFailuresIn(vararg classNames: String): ClassesRuleBuilder {
        ignoredPredicates.add { cls ->
            classNames.any { name ->
                cls.fqName == name || cls.name == name || io.github.baole.konture.impl.PatternMatchers.matchesSimpleGlob(name, cls.fqName)
            }
        }
        return this
    }

    internal fun getThatPredicate(): ((ClassDeclaration) -> Boolean)? = thatPredicate

    internal fun getShouldAssertion(): (
        (
            ClassDeclaration,
            List<ClassDeclaration>,
            MutableList<String>,
        ) -> Unit
    )? =
        shouldAssertion

    /**
     * Starts adding filtering conditions to select which classes to verify.
     */
    public fun that(): ClassesThat = ClassesThat(this)

    /**
     * Starts adding assertion rules that the selected classes must satisfy.
     */
    public fun should(): ClassesShould = ClassesShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    public fun and(): ClassesThat {
        activeOperator = LogicalOperator.AND
        return ClassesThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    public fun or(): ClassesThat {
        activeOperator = LogicalOperator.OR
        return ClassesThat(this)
    }

    /**
     * Logical XOR (Exclusive OR) operator for chaining filter conditions.
     */
    public fun xor(): ClassesThat {
        activeOperator = LogicalOperator.XOR
        return ClassesThat(this)
    }

    /**
     * Logical NOT operator for negating the next filter condition.
     */
    public fun not(): ClassesThat {
        negateNext = true
        return ClassesThat(this)
    }

    /**
     * Logical AND operator for chaining assertion conditions.
     */
    public fun andShould(): ClassesShould {
        activeShouldOperator = LogicalOperator.AND
        return ClassesShould(this)
    }

    /**
     * Logical OR operator for chaining assertion conditions.
     */
    public fun orShould(): ClassesShould {
        activeShouldOperator = LogicalOperator.OR
        return ClassesShould(this)
    }

    /**
     * Logical XOR (Exclusive OR) operator for chaining assertion conditions.
     */
    public fun xorShould(): ClassesShould {
        activeShouldOperator = LogicalOperator.XOR
        return ClassesShould(this)
    }

    /**
     * Logical NOT operator for negating the next assertion condition.
     */
    public fun notShould(): ClassesShould {
        negateNextShould = true
        return ClassesShould(this)
    }

    internal fun setThat(predicate: (ClassDeclaration) -> Boolean) {
        /** Filter or assertion criteria for actual predicate. */
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                /** Filter or assertion criteria for p. */
                val p = { c: ClassDeclaration -> !predicate(c) }
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

    internal fun setShould(assertion: (ClassDeclaration, List<ClassDeclaration>, MutableList<String>) -> Unit) {
        /** Filter or assertion criteria for actual assertion. */
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                /** Filter or assertion criteria for a. */
                val a = { cls: ClassDeclaration, allCls: List<ClassDeclaration>, violations: MutableList<String> ->
                    /** Filter or assertion criteria for temp violations. */
                    val tempViolations = mutableListOf<String>()
                    assertion(cls, allCls, tempViolations)
                    if (tempViolations.isEmpty()) {
                        violations.add(getMessage("classes.rule.negatedSatisfied", cls.fqName))
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
                shouldAssertion = { cls, allCls, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(cls, allCls, temp1)
                    actualAssertion(cls, allCls, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            getMessage("classes.rule.eitherOr", cls.fqName, temp1.joinToString("; "), temp2.joinToString("; ")),
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { cls, allCls, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(cls, allCls, temp1)
                    actualAssertion(cls, allCls, temp2)
                    /** Filter or assertion criteria for ok1. */
                    val ok1 = temp1.isEmpty()

                    /** Filter or assertion criteria for ok2. */
                    val ok2 = temp2.isEmpty()
                    if (ok1 == ok2) {
                        violations.add(getMessage("classes.rule.xor", cls.fqName))
                    }
                }
            } else {
                shouldAssertion = { cls, allCls, violations ->
                    current(cls, allCls, violations)
                    actualAssertion(cls, allCls, violations)
                }
            }
            activeShouldOperator = LogicalOperator.AND
        }
    }

    /**
     * Executes the built class rules against the specified project graph.
     *
     * @param g The [ProjectGraph] to check. Defaults to the lazy-loaded project graph.
     * @throws AssertionError If any of the verified classes violate the assertion rules.
     */
    public fun check(g: ProjectGraph = graph) {
        /** Filter or assertion criteria for located. */
        val located =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    file.membershipsFor(module.path).filter(sourceSets::matches).flatMap { sourceSet ->
                        file.classes.map { cls -> ClassLocation(cls, module.path, sourceSet.name) }
                    }
                }
            }.distinctBy { it.cls.fqName to it.cls.filePath }

        /** Filter or assertion criteria for all classes. */
        val allClasses = located.map { it.cls }

        /** Filter or assertion criteria for classes to check. */
        val classesToCheck = located.filter { thatPredicate?.invoke(it.cls) ?: true }

        KontureLogger.log(
            LogLevel.DEBUG,
            "Checking Classes Rules: found ${allClasses.size} classes total. Selected ${classesToCheck.size} classes to verify.",
        )
        if (classesToCheck.isEmpty()) {
            if (!allowEmpty) {
                throw AssertionError(
                    getMessage("classes.rule.emptySelect"),
                )
            } else {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "No classes matched the filter 'that()'. Rule silently succeeded as allowEmpty is enabled.",
                )
            }
        }

        /** Filter or assertion criteria for assertion. */
        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("classes.rule.noAssertion"),
            )

        /** Filter or assertion criteria for run check. */
        val runCheck = { list: MutableList<String> ->
            for ((cls, modulePath, sourceSetName) in classesToCheck) {
                if (ignoredPredicates.any { it(cls) }) continue
                /** Filter or assertion criteria for start idx. */
                val startIdx = list.size
                assertion(cls, allClasses, list)
                for (i in startIdx until list.size) {
                    if (!list[i].contains(" (at ")) {
                        list[i] = "${list[i]} (at ${ViolationLocation.format(cls, modulePath, sourceSetName)})"
                    }
                }
            }
        }

        BaselineManager.checkRule(
            getMessage("classes.rule.violationHeader"),
            runCheck,
        )
    }
}

/** Pairs a class with the module path and source set it was selected from, for violation locations. */
private data class ClassLocation(
    /** Filter or assertion criteria for cls. */
    public val cls: ClassDeclaration,
    /** Filter or assertion criteria for module path. */
    public val modulePath: String,
    /** Filter or assertion criteria for source set name. */
    public val sourceSetName: String?,
)
