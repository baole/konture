/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/** Composite condition assertions for file rules. */
public interface FilesShouldCompositeAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: FilesRuleBuilder

    /** Asserts that selected files satisfy a custom boolean condition. */
    public infix fun satisfy(assertion: (FileDeclarationContext) -> Boolean): FilesRuleBuilder =
        satisfy(id = "custom condition", description = "custom condition") { file -> assertion(file) }

    /** Asserts that selected files satisfy custom description [description]. */
    public infix fun satisfy(description: String): FilesRuleBuilder =
        satisfy(id = description, description = description) { false }

    /** Asserts that selected files satisfy custom predicate [predicate] with [description]. */
    public fun satisfy(
        description: String,
        predicate: (FileDeclarationContext) -> Boolean,
    ): FilesRuleBuilder = satisfy(id = description, description = description) { file -> predicate(file) }

    /**
     * Asserts that selected files satisfy a custom predicate within a [SatisfyContext] block identified by [id] and optional [description].
     */
    public fun satisfy(
        id: String,
        description: String? = null,
        predicate: SatisfyContext<FileDeclarationContext>.(FileDeclarationContext) -> Boolean,
    ): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
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
                        subject = file,
                        id = id,
                        description = description,
                        graph = builder.graph,
                        rawMessages = violations,
                    )
                val initialCount = violations.size
                val passed = context.predicate(file)
                if (!passed && violations.size == initialCount) {
                    val msg = getMessage("file.should.satisfyCustom", file.declaration.name, description ?: id)
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Asserts that selected files satisfy a custom violation-collecting assertion. */
    public fun satisfy(assertion: (FileDeclarationContext, MutableList<String>) -> Unit): FilesRuleBuilder {
        builder.setShould { file, _, violations -> assertion(file, violations) }
        return builder
    }

    /** Asserts that selected files satisfy at least one of the specified assertion blocks. */
    public fun anyOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            /** Filter or assertion criteria for any passed. */
            val anyPassed =
                blocks.any { block ->
                    /** Filter or assertion criteria for sub builder. */
                    val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                    FilesShould(subBuilder).apply(block)
                    /** Filter or assertion criteria for sub assertion. */
                    val subAssertion = subBuilder.getShouldAssertion()

                    /** Filter or assertion criteria for sub violations. */
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(file, allFiles, subViolations)
                    subViolations.isEmpty()
                }
            if (!anyPassed) {
                violations.add(getMessage("file.should.failedAnyOf", file.declaration.name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    public fun allOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            blocks.forEach { block ->
                /** Filter or assertion criteria for sub builder. */
                val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                FilesShould(subBuilder).apply(block)
                /** Filter or assertion criteria for sub assertion. */
                val subAssertion = subBuilder.getShouldAssertion()
                subAssertion?.invoke(file, allFiles, violations)
            }
        }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    public fun noneOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            /** Filter or assertion criteria for any passed. */
            val anyPassed =
                blocks.any { block ->
                    /** Filter or assertion criteria for sub builder. */
                    val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                    FilesShould(subBuilder).apply(block)
                    /** Filter or assertion criteria for sub assertion. */
                    val subAssertion = subBuilder.getShouldAssertion()

                    /** Filter or assertion criteria for sub violations. */
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(file, allFiles, subViolations)
                    subViolations.isEmpty()
                }
            if (anyPassed) {
                violations.add(getMessage("file.should.failedNoneOf", file.declaration.name))
            }
        }
        return builder
    }
}
