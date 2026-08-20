/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/** Composite condition assertions for function rules. */
public interface FunctionsShouldCompositeAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: FunctionsRuleBuilder

    /** Asserts that selected functions satisfy a custom boolean condition. */
    public infix fun satisfy(assertion: (FunctionDeclarationContext) -> Boolean): FunctionsRuleBuilder =
        satisfy(
            "custom condition",
        ) { f, _ -> assertion(f) }

    /** Asserts that selected functions satisfy a custom boolean condition with description. */
    public fun satisfy(
        description: String,
        assertion: (FunctionDeclarationContext, List<FunctionDeclarationContext>) -> Boolean,
    ): FunctionsRuleBuilder {
        builder.setShould { func, allFuncs, violations ->
            if (!assertion(func, allFuncs)) {
                violations.add(
                    getMessage("function.should.satisfyCustom", func.qualifiedName, description),
                )
            }
        }
        return builder
    }

    /** Asserts that selected functions satisfy custom description [description]. */
    public fun satisfy(description: String): FunctionsRuleBuilder =
        satisfy(id = description, description = description) { false }

    /**
     * Asserts that selected functions satisfy a custom predicate within a [SatisfyContext] block identified by [id] and optional [description].
     */
    public fun satisfy(
        id: String,
        description: String? = null,
        predicate: SatisfyContext<FunctionDeclarationContext>.(FunctionDeclarationContext) -> Boolean,
    ): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val currentState = io.github.baole.konture.impl.KontureRuntimeStateProvider.currentState
            val activeSeverity = currentState.currentRuleMetadata?.severity ?: io.github.baole.konture.core.model.Severity.ERROR
            val activeTags = currentState.currentRuleMetadata?.tags ?: emptySet()
            val overrideMeta =
                io.github.baole.konture.core.model.RuleMetadata(
                    id = id,
                    description = description,
                    severity = activeSeverity,
                    tags = activeTags,
                )

            io.github.baole.konture.impl.KontureRuntimeStateProvider.runWithState(
                currentState.copy(currentRuleMetadata = overrideMeta),
            ) {
                val context =
                    SatisfyContextImpl(
                        subject = func,
                        id = id,
                        description = description,
                        graph = builder.graph,
                        rawMessages = violations,
                    )
                val initialCount = violations.size
                val passed = context.predicate(func)
                if (!passed && violations.size == initialCount) {
                    val msg = description ?: getMessage("function.should.satisfyCustomCondition", id)
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Asserts that selected functions satisfy a custom violation-collecting assertion. */
    public fun satisfy(assertion: (FunctionDeclarationContext, MutableList<String>) -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, _, violations -> assertion(func, violations) }
        return builder
    }

    /** Asserts that selected functions satisfy at least one of the specified assertion blocks. */
    public fun anyOf(vararg blocks: FunctionsShould.() -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, allFuncs, violations ->
            /** Filter or assertion criteria for any passed. */
            val anyPassed =
                blocks.any { block ->
                    /** Filter or assertion criteria for sub builder. */
                    val subBuilder = FunctionsRuleBuilder(builder.graph).allowEmpty()
                    FunctionsShould(subBuilder).apply(block)
                    /** Filter or assertion criteria for sub assertion. */
                    val subAssertion = subBuilder.getShouldAssertion()

                    /** Filter or assertion criteria for sub violations. */
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(func, allFuncs, subViolations)
                    subViolations.isEmpty()
                }
            if (!anyPassed) {
                violations.add(getMessage("function.should.satisfyAnyOf", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    public fun allOf(vararg blocks: FunctionsShould.() -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, allFuncs, violations ->
            blocks.forEach { block ->
                /** Filter or assertion criteria for sub builder. */
                val subBuilder = FunctionsRuleBuilder(builder.graph).allowEmpty()
                FunctionsShould(subBuilder).apply(block)
                /** Filter or assertion criteria for sub assertion. */
                val subAssertion = subBuilder.getShouldAssertion()
                subAssertion?.invoke(func, allFuncs, violations)
            }
        }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    public fun noneOf(vararg blocks: FunctionsShould.() -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, allFuncs, violations ->
            /** Filter or assertion criteria for any passed. */
            val anyPassed =
                blocks.any { block ->
                    /** Filter or assertion criteria for sub builder. */
                    val subBuilder = FunctionsRuleBuilder(builder.graph).allowEmpty()
                    FunctionsShould(subBuilder).apply(block)
                    /** Filter or assertion criteria for sub assertion. */
                    val subAssertion = subBuilder.getShouldAssertion()

                    /** Filter or assertion criteria for sub violations. */
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(func, allFuncs, subViolations)
                    subViolations.isEmpty()
                }
            if (anyPassed) {
                violations.add(getMessage("function.should.satisfyNoneOf", func.qualifiedName))
            }
        }
        return builder
    }
}
