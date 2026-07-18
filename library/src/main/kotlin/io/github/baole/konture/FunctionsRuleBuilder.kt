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
 * A builder for compiling and verifying architectural rules on Kotlin function declarations.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against all functions in the project (both member/class functions and top-level functions).
 */
@KontureDsl
class FunctionsRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
) {
    private var thatPredicate: ((FunctionDeclarationContext) -> Boolean)? = null
    private var shouldAssertion: (
        (FunctionDeclarationContext, List<FunctionDeclarationContext>, MutableList<String>) -> Unit
    )? = null

    private var activeOperator = LogicalOperator.AND
    private var negateNext = false

    private var activeShouldOperator = LogicalOperator.AND
    private var negateNextShould = false
    private var allowEmpty = false

    /**
     * Configures this builder to allow empty selections (i.e. if no functions match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    fun allowEmpty(): FunctionsRuleBuilder {
        allowEmpty = true
        return this
    }

    internal fun getThatPredicate(): ((FunctionDeclarationContext) -> Boolean)? = thatPredicate

    internal fun getShouldAssertion(): (
        (FunctionDeclarationContext, List<FunctionDeclarationContext>, MutableList<String>) -> Unit
    )? =
        shouldAssertion

    /**
     * Starts adding filtering conditions to select which functions to verify.
     */
    fun that(): FunctionsThat = FunctionsThat(this)

    /**
     * Starts adding assertion rules that the selected functions must satisfy.
     */
    fun should(): FunctionsShould = FunctionsShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    fun and(): FunctionsThat {
        activeOperator = LogicalOperator.AND
        return FunctionsThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    fun or(): FunctionsThat {
        activeOperator = LogicalOperator.OR
        return FunctionsThat(this)
    }

    /**
     * Logical XOR operator for chaining filter conditions.
     */
    fun xor(): FunctionsThat {
        activeOperator = LogicalOperator.XOR
        return FunctionsThat(this)
    }

    /**
     * Negates the next filter condition in the chain.
     */
    fun not(): FunctionsThat {
        negateNext = true
        return FunctionsThat(this)
    }

    /**
     * Logical AND operator for chaining assertion rules.
     */
    fun andShould(): FunctionsShould {
        activeShouldOperator = LogicalOperator.AND
        return FunctionsShould(this)
    }

    /**
     * Logical OR operator for chaining assertion rules.
     */
    fun orShould(): FunctionsShould {
        activeShouldOperator = LogicalOperator.OR
        return FunctionsShould(this)
    }

    /**
     * Logical XOR operator for chaining assertion rules.
     */
    fun xorShould(): FunctionsShould {
        activeShouldOperator = LogicalOperator.XOR
        return FunctionsShould(this)
    }

    /**
     * Negates the next assertion rule in the chain.
     */
    fun notShould(): FunctionsShould {
        negateNextShould = true
        return FunctionsShould(this)
    }

    internal fun setThat(predicate: (FunctionDeclarationContext) -> Boolean) {
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                val p = { f: FunctionDeclarationContext -> !predicate(f) }
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

    internal fun setShould(assertion: (FunctionDeclarationContext, List<FunctionDeclarationContext>, MutableList<String>) -> Unit) {
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                val a = {
                        func: FunctionDeclarationContext,
                        allFuncs: List<FunctionDeclarationContext>,
                        violations: MutableList<String>,
                    ->
                    val tempViolations = mutableListOf<String>()
                    assertion(func, allFuncs, tempViolations)
                    if (tempViolations.isEmpty()) {
                        violations.add(
                            getMessage("functions.rule.negatedSatisfied", func.declaration.name),
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
                shouldAssertion = { func, allFuncs, violations ->
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(func, allFuncs, temp1)
                    actualAssertion(func, allFuncs, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            getMessage("functions.rule.eitherOr", func.declaration.name, temp1.joinToString(), temp2.joinToString()),
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { func, allFuncs, violations ->
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(func, allFuncs, temp1)
                    actualAssertion(func, allFuncs, temp2)
                    val ok1 = temp1.isEmpty()
                    val ok2 = temp2.isEmpty()
                    if (ok1 == ok2) {
                        violations.add(
                            getMessage("functions.rule.xor", func.declaration.name),
                        )
                    }
                }
            } else {
                shouldAssertion = { func, allFuncs, violations ->
                    current(func, allFuncs, violations)
                    actualAssertion(func, allFuncs, violations)
                }
            }
            activeShouldOperator = LogicalOperator.AND
        }
    }

    /**
     * Executes the compiled function rules against the provided project graph.
     * Throws an [AssertionError] if any rule violations are detected.
     */
    fun check(g: ProjectGraph = graph) {
        val allFunctions =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    val topLevel =
                        file.topLevelFunctions.map { func ->
                            FunctionDeclarationContext(func, file.packageName, null, module.path, file.filePath)
                        }
                    val members =
                        file.classes.flatMap { cls ->
                            cls.functions.map { func ->
                                FunctionDeclarationContext(func, file.packageName, cls.name, module.path, file.filePath)
                            }
                        }
                    topLevel + members
                }
            }
        val functionsToCheck = allFunctions.filter { thatPredicate?.invoke(it) ?: true }
        KontureLogger.log(
            LogLevel.DEBUG,
            "Checking Functions Rules: found ${allFunctions.size} functions total. Selected ${functionsToCheck.size} functions to verify.",
        )
        if (functionsToCheck.isEmpty()) {
            if (!allowEmpty) {
                throw AssertionError(
                    getMessage("functions.rule.emptySelect"),
                )
            } else {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "No functions matched the filter 'that()'. Rule silently succeeded as allowEmpty is enabled.",
                )
            }
        }
        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("functions.rule.noAssertion"),
            )

        val runCheck = { list: MutableList<String> ->
            for (func in functionsToCheck) {
                val startIdx = list.size
                assertion(func, allFunctions, list)
                for (i in startIdx until list.size) {
                    list[i] = "${list[i]} (at ${func.filePath})"
                }
            }
        }

        BaselineManager.checkRule(
            getMessage("functions.rule.violationHeader"),
            runCheck,
        )
    }
}
