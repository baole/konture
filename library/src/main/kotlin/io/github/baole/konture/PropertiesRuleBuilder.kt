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
 * A builder for compiling and verifying architectural rules on Kotlin property declarations.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against all properties in the project (both member/class properties and top-level properties).
 */
@KontureDsl
class PropertiesRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
) {
    private var thatPredicate: ((PropertyDeclarationContext) -> Boolean)? = null
    private var shouldAssertion: (
        (PropertyDeclarationContext, List<PropertyDeclarationContext>, MutableList<String>) -> Unit
    )? = null

    private var activeOperator = LogicalOperator.AND
    private var negateNext = false

    private var activeShouldOperator = LogicalOperator.AND
    private var negateNextShould = false
    private var allowEmpty = false

    /**
     * Configures this builder to allow empty selections (i.e. if no properties match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    fun allowEmpty(): PropertiesRuleBuilder {
        allowEmpty = true
        return this
    }

    internal fun getThatPredicate(): ((PropertyDeclarationContext) -> Boolean)? = thatPredicate

    internal fun getShouldAssertion(): (
        (PropertyDeclarationContext, List<PropertyDeclarationContext>, MutableList<String>) -> Unit
    )? =
        shouldAssertion

    /**
     * Starts adding filtering conditions to select which properties to verify.
     */
    fun that(): PropertiesThat = PropertiesThat(this)

    /**
     * Starts adding assertion rules that the selected properties must satisfy.
     */
    fun should(): PropertiesShould = PropertiesShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    fun and(): PropertiesThat {
        activeOperator = LogicalOperator.AND
        return PropertiesThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    fun or(): PropertiesThat {
        activeOperator = LogicalOperator.OR
        return PropertiesThat(this)
    }

    /**
     * Logical XOR operator for chaining filter conditions.
     */
    fun xor(): PropertiesThat {
        activeOperator = LogicalOperator.XOR
        return PropertiesThat(this)
    }

    /**
     * Negates the next filter condition in the chain.
     */
    fun not(): PropertiesThat {
        negateNext = true
        return PropertiesThat(this)
    }

    /**
     * Logical AND operator for chaining assertion rules.
     */
    fun andShould(): PropertiesShould {
        activeShouldOperator = LogicalOperator.AND
        return PropertiesShould(this)
    }

    /**
     * Logical OR operator for chaining assertion rules.
     */
    fun orShould(): PropertiesShould {
        activeShouldOperator = LogicalOperator.OR
        return PropertiesShould(this)
    }

    /**
     * Logical XOR operator for chaining assertion rules.
     */
    fun xorShould(): PropertiesShould {
        activeShouldOperator = LogicalOperator.XOR
        return PropertiesShould(this)
    }

    /**
     * Negates the next assertion rule in the chain.
     */
    fun notShould(): PropertiesShould {
        negateNextShould = true
        return PropertiesShould(this)
    }

    internal fun setThat(predicate: (PropertyDeclarationContext) -> Boolean) {
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                val p = { p: PropertyDeclarationContext -> !predicate(p) }
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

    internal fun setShould(assertion: (PropertyDeclarationContext, List<PropertyDeclarationContext>, MutableList<String>) -> Unit) {
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                val a = {
                        prop: PropertyDeclarationContext,
                        allProps: List<PropertyDeclarationContext>,
                        violations: MutableList<String>,
                    ->
                    val tempViolations = mutableListOf<String>()
                    assertion(prop, allProps, tempViolations)
                    if (tempViolations.isEmpty()) {
                        violations.add(
                            getMessage("properties.rule.negatedSatisfied", prop.declaration.name),
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
                shouldAssertion = { prop, allProps, violations ->
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(prop, allProps, temp1)
                    actualAssertion(prop, allProps, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            getMessage("properties.rule.eitherOr", prop.declaration.name, temp1.joinToString(), temp2.joinToString()),
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { prop, allProps, violations ->
                    val temp1 = mutableListOf<String>()
                    val temp2 = mutableListOf<String>()
                    current(prop, allProps, temp1)
                    actualAssertion(prop, allProps, temp2)
                    val ok1 = temp1.isEmpty()
                    val ok2 = temp2.isEmpty()
                    if (ok1 == ok2) {
                        violations.add(
                            getMessage("properties.rule.xor", prop.declaration.name),
                        )
                    }
                }
            } else {
                shouldAssertion = { prop, allProps, violations ->
                    current(prop, allProps, violations)
                    actualAssertion(prop, allProps, violations)
                }
            }
            activeShouldOperator = LogicalOperator.AND
        }
    }

    /**
     * Executes the compiled property rules against the provided project graph.
     * Throws an [AssertionError] if any rule violations are detected.
     */
    fun check(g: ProjectGraph = graph) {
        val allProperties =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    val topLevel =
                        file.topLevelProperties.map { prop ->
                            PropertyDeclarationContext(prop, file.packageName, null, module.path, file.filePath)
                        }
                    val members =
                        file.classes.flatMap { cls ->
                            cls.properties.map { prop ->
                                PropertyDeclarationContext(prop, file.packageName, cls.name, module.path, file.filePath)
                            }
                        }
                    topLevel + members
                }
            }
        val propertiesToCheck = allProperties.filter { thatPredicate?.invoke(it) ?: true }
        KontureLogger.log(
            LogLevel.DEBUG,
            "Checking Properties Rules: found ${allProperties.size} properties total. Selected ${propertiesToCheck.size} properties to verify.",
        )
        if (propertiesToCheck.isEmpty()) {
            if (!allowEmpty) {
                throw AssertionError(
                    getMessage("properties.rule.emptySelect"),
                )
            } else {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "No properties matched the filter 'that()'. Rule silently succeeded as allowEmpty is enabled.",
                )
            }
        }
        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("properties.rule.noAssertion"),
            )

        val runCheck = { list: MutableList<String> ->
            for (prop in propertiesToCheck) {
                val startIdx = list.size
                assertion(prop, allProperties, list)
                for (i in startIdx until list.size) {
                    list[i] = "${list[i]} (at ${prop.filePath})"
                }
            }
        }

        BaselineManager.checkRule(
            getMessage("properties.rule.violationHeader"),
            runCheck,
        )
    }
}
