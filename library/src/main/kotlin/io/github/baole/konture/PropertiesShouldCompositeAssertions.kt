/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

interface PropertiesShouldCompositeAssertions {
    val builder: PropertiesRuleBuilder

    infix fun satisfy(assertion: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder =
        satisfy("custom condition") { p, _ -> assertion(p) }

    fun satisfy(
        description: String,
        assertion: (PropertyDeclarationContext, List<PropertyDeclarationContext>) -> Boolean,
    ): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            if (!assertion(prop, allProps)) {
                violations.add(
                    getMessage("property.should.satisfyCustom", prop.qualifiedName, description),
                )
            }
        }
        return builder
    }

    fun satisfy(assertion: (PropertyDeclarationContext, MutableList<String>) -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations -> assertion(prop, violations) }
        return builder
    }

    fun anyOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                    PropertiesShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(prop, allProps, subViolations)
                    subViolations.isEmpty()
                }
            if (!anyPassed) {
                violations.add(getMessage("property.should.satisfyAnyOf", prop.qualifiedName))
            }
        }
        return builder
    }

    fun allOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            blocks.forEach { block ->
                val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                PropertiesShould(subBuilder).apply(block)
                val subAssertion = subBuilder.getShouldAssertion()
                subAssertion?.invoke(prop, allProps, violations)
            }
        }
        return builder
    }

    fun noneOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                    PropertiesShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(prop, allProps, subViolations)
                    subViolations.isEmpty()
                }
            if (anyPassed) {
                violations.add(getMessage("property.should.satisfyNoneOf", prop.qualifiedName))
            }
        }
        return builder
    }
}
