/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

interface ModulesShouldCompositeAssertions {
    val builder: ModulesRuleBuilder

    infix fun satisfy(assertion: (Module) -> Boolean): ModulesRuleBuilder =
        satisfy("custom condition") { module, _ -> assertion(module) }

    infix fun satisfy(description: String): ModulesRuleBuilder = satisfy(description) { _, _ -> false }

    fun satisfy(
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

    fun satisfy(assertion: (Module, MutableList<String>) -> Unit): ModulesRuleBuilder {
        builder.setShould { module, _, violations -> assertion(module, violations) }
        return builder
    }

    fun anyOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { module, g, violations ->
            val tempViolationsList =
                assertions.map { assertion ->
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

    fun allOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        val assertions =
            blocks.map { block ->
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

    fun noneOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { module, g, violations ->
            assertions.forEach { assertion ->
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
