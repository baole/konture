/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/** Composite condition assertions for Gradle module rules. */
public interface ModulesShouldCompositeAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: ModulesRuleBuilder

    /** Asserts that selected modules satisfy a custom boolean condition. */
    public infix fun satisfy(assertion: (Module) -> Boolean): ModulesRuleBuilder =
        satisfy("custom condition") { module, _ -> assertion(module) }

    /** Asserts that selected modules satisfy a custom condition described by [description]. */
    public infix fun satisfy(description: String): ModulesRuleBuilder = satisfy(description) { _, _ -> false }

    /** Asserts that selected modules satisfy a custom condition with description. */
    public fun satisfy(
        description: String,
        assertion: (Module, ProjectGraph) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            if (!assertion(module, graph)) {
                violations.add(
                    getMessage("module.should.satisfyCustom", module.path, description),
                )
            }
        }
        return builder
    }

    /** Asserts that selected modules satisfy a custom violation-collecting assertion. */
    public fun satisfy(assertion: (Module, MutableList<String>) -> Unit): ModulesRuleBuilder {
        builder.setShould { module, _, violations -> assertion(module, violations) }
        return builder
    }

    /** Asserts that selected modules satisfy at least one of the specified assertion blocks. */
    public fun anyOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        /** Filter or assertion criteria for assertions. */
        val assertions =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { module, g, violations ->
            /** Filter or assertion criteria for temp violations list. */
            val tempViolationsList =
                assertions.map { assertion ->
                    /** Filter or assertion criteria for temp. */
                    val temp = mutableListOf<String>()
                    assertion(module, g, temp)
                    temp
                }
            if (tempViolationsList.all { it.isNotEmpty() }) {
                violations.add(
                    getMessage("module.should.satisfyAtLeastOneNested", module.path),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    public fun allOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        /** Filter or assertion criteria for assertions. */
        val assertions =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { module, g, violations ->
            assertions.forEach { assertion ->
                assertion(module, g, violations)
            }
        }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    public fun noneOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        /** Filter or assertion criteria for assertions. */
        val assertions =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { module, g, violations ->
            assertions.forEach { assertion ->
                /** Filter or assertion criteria for temp. */
                val temp = mutableListOf<String>()
                assertion(module, g, temp)
                if (temp.isEmpty()) {
                    violations.add(
                        getMessage("module.should.notSatisfyNested", module.path),
                    )
                }
            }
        }
        return builder
    }
}
