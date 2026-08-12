/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/** Composite condition assertions for property rules. */
public interface PropertiesShouldCompositeAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: PropertiesRuleBuilder

    /** Asserts that selected properties satisfy a custom boolean condition. */
    public infix fun satisfy(assertion: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder =
        satisfy("custom condition") { p, _ -> assertion(p) }

    /** Asserts that selected properties satisfy a custom boolean condition with description. */
    public fun satisfy(
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

    /** Asserts that selected properties satisfy a custom violation-collecting assertion. */
    public fun satisfy(assertion: (PropertyDeclarationContext, MutableList<String>) -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations -> assertion(prop, violations) }
        return builder
    }

    /** Asserts that selected properties satisfy at least one of the specified assertion blocks. */
    public fun anyOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            /** Filter or assertion criteria for any passed. */
            val anyPassed =
                blocks.any { block ->
                    /** Filter or assertion criteria for sub builder. */
                    val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                    PropertiesShould(subBuilder).apply(block)
                    /** Filter or assertion criteria for sub assertion. */
                    val subAssertion = subBuilder.getShouldAssertion()

                    /** Filter or assertion criteria for sub violations. */
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

    /** Filter or assertion criteria for all of. */
    public fun allOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            blocks.forEach { block ->
                /** Filter or assertion criteria for sub builder. */
                val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                PropertiesShould(subBuilder).apply(block)
                /** Filter or assertion criteria for sub assertion. */
                val subAssertion = subBuilder.getShouldAssertion()
                subAssertion?.invoke(prop, allProps, violations)
            }
        }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    public fun noneOf(vararg blocks: PropertiesShould.() -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            /** Filter or assertion criteria for any passed. */
            val anyPassed =
                blocks.any { block ->
                    /** Filter or assertion criteria for sub builder. */
                    val subBuilder = PropertiesRuleBuilder(builder.graph).allowEmpty()
                    PropertiesShould(subBuilder).apply(block)
                    /** Filter or assertion criteria for sub assertion. */
                    val subAssertion = subBuilder.getShouldAssertion()

                    /** Filter or assertion criteria for sub violations. */
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
