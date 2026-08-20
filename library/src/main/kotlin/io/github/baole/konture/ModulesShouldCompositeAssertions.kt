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

    /** Asserts that selected modules satisfy custom description [description]. */
    public fun satisfy(description: String): ModulesRuleBuilder =
        satisfy(id = description, description = description) { false }

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

    /**
     * Asserts that selected modules satisfy a custom predicate within a [SatisfyContext] block identified by [id] and optional [description].
     */
    public fun satisfy(
        id: String,
        description: String? = null,
        predicate: SatisfyContext<Module>.(Module) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
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
                        subject = module,
                        id = id,
                        description = description,
                        graph = builder.graph,
                        rawMessages = violations,
                    )
                val initialCount = violations.size
                val passed = context.predicate(module)
                if (!passed && violations.size == initialCount) {
                    val msg = description ?: getMessage("module.should.satisfyCustomCondition", id)
                    violations.add(msg)
                }
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
