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
import io.github.baole.konture.impl.ViolationLocation

/**
 * A builder for compiling and verifying architectural rules on Kotlin classes.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against all project classes by calling [check].
 */
@KontureDsl
class ClassesRuleBuilder(
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
     * Configures this builder to allow empty selections (i.e. if no classes match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    fun allowEmpty(): ClassesRuleBuilder {
        allowEmpty = true
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
    fun that(): ClassesThat = ClassesThat(this)

    /**
     * Starts adding assertion rules that the selected classes must satisfy.
     */
    fun should(): ClassesShould = ClassesShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    fun and(): ClassesThat {
        activeOperator = LogicalOperator.AND
        return ClassesThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    fun or(): ClassesThat {
        activeOperator = LogicalOperator.OR
        return ClassesThat(this)
    }

    /**
     * Logical XOR (Exclusive OR) operator for chaining filter conditions.
     */
    fun xor(): ClassesThat {
        activeOperator = LogicalOperator.XOR
        return ClassesThat(this)
    }

    /**
     * Logical NOT operator for negating the next filter condition.
     */
    fun not(): ClassesThat {
        negateNext = true
        return ClassesThat(this)
    }

    /**
     * Logical AND operator for chaining assertion conditions.
     */
    fun andShould(): ClassesShould {
        activeShouldOperator = LogicalOperator.AND
        return ClassesShould(this)
    }

    /**
     * Logical OR operator for chaining assertion conditions.
     */
    fun orShould(): ClassesShould {
        activeShouldOperator = LogicalOperator.OR
        return ClassesShould(this)
    }

    /**
     * Logical XOR (Exclusive OR) operator for chaining assertion conditions.
     */
    fun xorShould(): ClassesShould {
        activeShouldOperator = LogicalOperator.XOR
        return ClassesShould(this)
    }

    /**
     * Logical NOT operator for negating the next assertion condition.
     */
    fun notShould(): ClassesShould {
        negateNextShould = true
        return ClassesShould(this)
    }

    internal fun setThat(predicate: (ClassDeclaration) -> Boolean) {
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                val p = { c: ClassDeclaration -> !predicate(c) }
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

    internal fun setShould(assertion: (ClassDeclaration, List<ClassDeclaration>, MutableList<String>) -> Unit) {
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                val a = { cls: ClassDeclaration, allCls: List<ClassDeclaration>, violations: MutableList<String> ->
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

        val current = shouldAssertion
        if (current == null) {
            shouldAssertion = actualAssertion
        } else {
            val op = activeShouldOperator
            if (op == LogicalOperator.OR) {
                shouldAssertion = { cls, allCls, violations ->
                    val temp1 = mutableListOf<String>()
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
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(cls, allCls, temp1)
                    actualAssertion(cls, allCls, temp2)
                    val ok1 = temp1.isEmpty()
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
    fun check(g: ProjectGraph = graph) {
        val located =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    file.membershipsFor(module.path).filter(sourceSets::matches).flatMap { sourceSet ->
                        file.classes.map { cls -> ClassLocation(cls, module.path, sourceSet.name) }
                    }
                }
            }.distinctBy { it.cls.fqName to it.cls.filePath }
        val allClasses = located.map { it.cls }
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

        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("classes.rule.noAssertion"),
            )

        val runCheck = { list: MutableList<String> ->
            for ((cls, modulePath, sourceSetName) in classesToCheck) {
                val startIdx = list.size
                assertion(cls, allClasses, list)
                for (i in startIdx until list.size) {
                    list[i] = "${list[i]} (at ${ViolationLocation.of(modulePath, sourceSetName, cls.filePath, cls.sourceLine)})"
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
    val cls: ClassDeclaration,
    val modulePath: String,
    val sourceSetName: String?,
)
