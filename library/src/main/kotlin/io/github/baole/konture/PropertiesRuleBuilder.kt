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
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import io.github.baole.konture.impl.LogicalOperator
import io.github.baole.konture.impl.ViolationLocation

/**
 * A builder for compiling and verifying architectural rules on Kotlin property declarations.
 *
 * Accumulates filtering conditions (`that()`) and assertions (`should()`), which are verified
 * against all properties in the project (both member/class properties and top-level properties).
 */
@KontureDsl
public class PropertiesRuleBuilder(
    internal val graph: ProjectGraph = Konture.projectGraph,
    private val sourceSets: SourceSetSelector = SourceSets.production(),
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
     * Debugging helper that prints information about all properties matched by the `that()` filter.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printMatchedProperties(
        logger: (PropertyDeclarationContext) -> Unit = {
            println(getMessage("debug.properties.matched", it.qualifiedName, ViolationLocation.format(it)))
        },
    ): PropertiesRuleBuilder =
        this.apply {
            setShould { prop, _, _ ->
                logger(prop)
            }
        }

    /**
     * Debugging helper that prints information about all discovered properties in the project graph.
     *
     * @param logger Custom log consumer (defaults to printing to standard output).
     */
    public fun printAllProperties(
        logger: (PropertyDeclarationContext) -> Unit = {
            println(getMessage("debug.properties.discovered", it.qualifiedName, ViolationLocation.format(it)))
        },
    ): PropertiesRuleBuilder =
        this.apply {
            graph.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    /** Filter or assertion criteria for top level. */
                    val topLevel =
                        file.topLevelProperties.map { prop ->
                            PropertyDeclarationContext(prop, file.packageName, null, module.path, file.filePath, null)
                        }

                    /** Filter or assertion criteria for members. */
                    val members =
                        file.classes.flatMap { cls ->
                            cls.properties.map { prop ->
                                PropertyDeclarationContext(
                                    prop,
                                    file.packageName,
                                    cls.name,
                                    module.path,
                                    file.filePath,
                                    null,
                                )
                            }
                        }
                    topLevel + members
                }
            }.distinctBy { listOf(it.modulePath, it.className, it.declaration.name) }.forEach(logger)
        }

    private val ignoredPredicates = mutableListOf<(PropertyDeclarationContext) -> Boolean>()

    /**
     * Configures this builder to allow empty selections (i.e. if no properties match the `that()` filter,
     * the rule will pass instead of throwing an AssertionError).
     */
    public fun allowEmpty(): PropertiesRuleBuilder {
        allowEmpty = true
        return this
    }

    /**
     * Configures this builder to ignore failures for properties satisfying the given predicate.
     */
    public fun ignoreFailuresIn(predicate: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder {
        ignoredPredicates.add(predicate)
        return this
    }

    /**
     * Configures this builder to ignore failures for properties matching any of the specified names or patterns.
     */
    public fun ignoreFailuresIn(vararg propertyNames: String): PropertiesRuleBuilder {
        ignoredPredicates.add { ctx ->
            propertyNames.any { name ->
                ctx.declaration.name == name || ctx.qualifiedName == name || io.github.baole.konture.impl.PatternMatchers.matchesSimpleGlob(name, ctx.declaration.name)
            }
        }
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
    public fun that(): PropertiesThat = PropertiesThat(this)

    /**
     * Starts adding assertion rules that the selected properties must satisfy.
     */
    public fun should(): PropertiesShould = PropertiesShould(this)

    /**
     * Logical AND operator for chaining filter conditions.
     */
    public fun and(): PropertiesThat {
        activeOperator = LogicalOperator.AND
        return PropertiesThat(this)
    }

    /**
     * Logical OR operator for chaining filter conditions.
     */
    public fun or(): PropertiesThat {
        activeOperator = LogicalOperator.OR
        return PropertiesThat(this)
    }

    /**
     * Logical XOR operator for chaining filter conditions.
     */
    public fun xor(): PropertiesThat {
        activeOperator = LogicalOperator.XOR
        return PropertiesThat(this)
    }

    /**
     * Negates the next filter condition in the chain.
     */
    public fun not(): PropertiesThat {
        negateNext = true
        return PropertiesThat(this)
    }

    /**
     * Logical AND operator for chaining assertion rules.
     */
    public fun andShould(): PropertiesShould {
        activeShouldOperator = LogicalOperator.AND
        return PropertiesShould(this)
    }

    /**
     * Logical OR operator for chaining assertion rules.
     */
    public fun orShould(): PropertiesShould {
        activeShouldOperator = LogicalOperator.OR
        return PropertiesShould(this)
    }

    /**
     * Logical XOR operator for chaining assertion rules.
     */
    public fun xorShould(): PropertiesShould {
        activeShouldOperator = LogicalOperator.XOR
        return PropertiesShould(this)
    }

    /**
     * Negates the next assertion rule in the chain.
     */
    public fun notShould(): PropertiesShould {
        negateNextShould = true
        return PropertiesShould(this)
    }

    internal fun setThat(predicate: (PropertyDeclarationContext) -> Boolean) {
        /** Filter or assertion criteria for actual predicate. */
        val actualPredicate =
            if (negateNext) {
                negateNext = false
                /** Filter or assertion criteria for p. */
                val p = { p: PropertyDeclarationContext -> !predicate(p) }
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

    internal fun setShould(
        assertion: (PropertyDeclarationContext, List<PropertyDeclarationContext>, MutableList<String>) -> Unit,
    ) {
        /** Filter or assertion criteria for actual assertion. */
        val actualAssertion =
            if (negateNextShould) {
                negateNextShould = false
                /** Filter or assertion criteria for a. */
                val a = {
                        prop: PropertyDeclarationContext,
                        allProps: List<PropertyDeclarationContext>,
                        violations: MutableList<String>,
                    ->
                    /** Filter or assertion criteria for temp violations. */
                    val tempViolations = mutableListOf<String>()
                    assertion(prop, allProps, tempViolations)
                    if (tempViolations.isEmpty()) {
                        violations.add(
                            getMessage("properties.rule.negatedSatisfied", prop.qualifiedName),
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
                shouldAssertion = { prop, allProps, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(prop, allProps, temp1)
                    actualAssertion(prop, allProps, temp2)
                    if (temp1.isNotEmpty() && temp2.isNotEmpty()) {
                        violations.add(
                            getMessage("properties.rule.eitherOr", prop.qualifiedName, temp1.joinToString("; "), temp2.joinToString("; ")),
                        )
                    }
                }
            } else if (op == LogicalOperator.XOR) {
                shouldAssertion = { prop, allProps, violations ->
                    /** Filter or assertion criteria for temp1. */
                    val temp1 = mutableListOf<String>()

                    /** Filter or assertion criteria for temp2. */
                    val temp2 = mutableListOf<String>()
                    current(prop, allProps, temp1)
                    actualAssertion(prop, allProps, temp2)
                    /** Filter or assertion criteria for ok1. */
                    val ok1 = temp1.isEmpty()

                    /** Filter or assertion criteria for ok2. */
                    val ok2 = temp2.isEmpty()
                    if (ok1 == ok2) {
                        violations.add(
                            getMessage("properties.rule.xor", prop.qualifiedName),
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
    public fun check(g: ProjectGraph = graph): ViolationReport {
        /** Filter or assertion criteria for all properties. */
        val allProperties =
            g.getAllModules().flatMap { module ->
                module.files.flatMap { file ->
                    file.membershipsFor(module.path).filter(sourceSets::matches).flatMap { sourceSet ->
                        /** Filter or assertion criteria for top level. */
                        val topLevel =
                            file.topLevelProperties.map { prop ->
                                PropertyDeclarationContext(
                                    prop,
                                    file.packageName,
                                    null,
                                    module.path,
                                    file.filePath,
                                    sourceSet,
                                )
                            }

                        /** Filter or assertion criteria for members. */
                        val members =
                            file.classes.flatMap { cls ->
                                cls.properties.map { prop ->
                                    PropertyDeclarationContext(
                                        prop,
                                        file.packageName,
                                        cls.name,
                                        module.path,
                                        file.filePath,
                                        sourceSet,
                                    )
                                }
                            }
                        topLevel + members
                    }
                }
            }.distinctBy { Triple(it.className, it.declaration.name, it.filePath) }

        /** Filter or assertion criteria for properties to check. */
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
        /** Filter or assertion criteria for assertion. */
        val assertion =
            shouldAssertion ?: throw AssertionError(
                getMessage("properties.rule.noAssertion"),
            )

        /** Filter or assertion criteria for run check. */
        val currentMeta = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        val activeRuleId = currentMeta?.id ?: "properties.rule"
        val activeSeverity = currentMeta?.severity ?: Severity.ERROR

        val runCheckReport = { list: MutableList<Violation> ->
            for (prop in propertiesToCheck) {
                if (ignoredPredicates.any { it(prop) }) continue
                val rawMessages = mutableListOf<String>()
                assertion(prop, allProperties, rawMessages)
                for (rawMsg in rawMessages) {
                    val fullMsg =
                        if (!rawMsg.contains(" (at ")) {
                            "$rawMsg (at ${ViolationLocation.format(prop)})"
                        } else {
                            rawMsg
                        }
                    val propName =
                        prop.className?.let { "${prop.packageName}.$it.${prop.declaration.name}" }
                            ?: "${prop.packageName}.${prop.declaration.name}"
                    val subject =
                        Subject.CustomSubject(
                            name = propName,
                            location = SourceLocation(filePath = prop.filePath, line = prop.declaration.sourceLine),
                        )
                    list.add(
                        Violation(
                            ruleId = activeRuleId,
                            subject = subject,
                            message = fullMsg,
                            severity = activeSeverity,
                            metadata = currentMeta,
                        ),
                    )
                }
            }
        }

        return BaselineManager.checkRuleReport(
            ruleId = activeRuleId,
            violationHeader = currentMeta?.description ?: getMessage("properties.rule.violationHeader"),
            runCheckReport = runCheckReport,
        )
    }
}
