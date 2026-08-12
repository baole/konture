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
        satisfy("custom condition") { f, _ -> assertion(f) }

    /** Asserts that selected files satisfy a custom boolean condition with description. */
    public fun satisfy(
        description: String,
        assertion: (FileDeclarationContext, List<FileDeclarationContext>) -> Boolean,
    ): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            if (!assertion(file, allFiles)) {
                violations.add(
                    getMessage("file.should.satisfyCustom", file.declaration.name, description),
                )
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
