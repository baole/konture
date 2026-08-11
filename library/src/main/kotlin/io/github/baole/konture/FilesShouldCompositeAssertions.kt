/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

interface FilesShouldCompositeAssertions {
    val builder: FilesRuleBuilder

    infix fun satisfy(assertion: (FileDeclarationContext) -> Boolean): FilesRuleBuilder =
        satisfy("custom condition") { f, _ -> assertion(f) }

    fun satisfy(
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

    fun satisfy(assertion: (FileDeclarationContext, MutableList<String>) -> Unit): FilesRuleBuilder {
        builder.setShould { file, _, violations -> assertion(file, violations) }
        return builder
    }

    fun anyOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                    FilesShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
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

    fun allOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            blocks.forEach { block ->
                val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                FilesShould(subBuilder).apply(block)
                val subAssertion = subBuilder.getShouldAssertion()
                subAssertion?.invoke(file, allFiles, violations)
            }
        }
        return builder
    }

    fun noneOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                    FilesShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
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
