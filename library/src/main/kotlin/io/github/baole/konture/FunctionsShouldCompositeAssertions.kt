/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

interface FunctionsShouldCompositeAssertions {
    val builder: FunctionsRuleBuilder

    infix fun satisfy(assertion: (FunctionDeclarationContext) -> Boolean): FunctionsRuleBuilder =
        satisfy(
            "custom condition",
        ) { f, _ -> assertion(f) }

    fun satisfy(
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

    fun satisfy(assertion: (FunctionDeclarationContext, MutableList<String>) -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, _, violations -> assertion(func, violations) }
        return builder
    }

    fun anyOf(vararg blocks: FunctionsShould.() -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, allFuncs, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = FunctionsRuleBuilder(builder.graph).allowEmpty()
                    FunctionsShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
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

    fun allOf(vararg blocks: FunctionsShould.() -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, allFuncs, violations ->
            blocks.forEach { block ->
                val subBuilder = FunctionsRuleBuilder(builder.graph).allowEmpty()
                FunctionsShould(subBuilder).apply(block)
                val subAssertion = subBuilder.getShouldAssertion()
                subAssertion?.invoke(func, allFuncs, violations)
            }
        }
        return builder
    }

    fun noneOf(vararg blocks: FunctionsShould.() -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, allFuncs, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = FunctionsRuleBuilder(builder.graph).allowEmpty()
                    FunctionsShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
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
