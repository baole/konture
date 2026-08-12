/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

private val externalDependencyRuleCall =
    Regex("\\b(?:notDependOnExternalLibraries|onlyDependOnExternalLibraries)\\s*(?:<[^>]+>)?\\s*\\(")
private const val TRIPLE_QUOTE_LENGTH = 3

/** Detects direct Konture external-dependency assertions in Kotlin test sources. */
@CacheableTask
public abstract class DetectExternalDependencyRules : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val testSources: ConfigurableFileCollection

    @get:OutputFile
    public abstract val resultFile: RegularFileProperty

    /** Scans configured test source files to detect whether external dependency rules are referenced. */
    @TaskAction
    public fun detect() {
        val requiresGraph =
            testSources.files.any { file ->
                file.extension == "kt" && externalDependencyRuleCall.containsMatchIn(stripCommentsAndStrings(file.readText()))
            }
        resultFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(requiresGraph.toString())
        }
    }

    private fun stripCommentsAndStrings(source: String): String {
        val code = StringBuilder(source.length)
        var index = 0
        while (index < source.length) {
            when {
                source.startsWith("//", index) -> {
                    index = source.indexOf('\n', index).takeIf { it >= 0 } ?: source.length
                }
                source.startsWith("/*", index) -> {
                    index = skipBlockComment(source, index)
                }
                source.startsWith("\"\"\"", index) -> {
                    index =
                        source
                            .indexOf("\"\"\"", index + TRIPLE_QUOTE_LENGTH)
                            .takeIf { it >= 0 }
                            ?.plus(TRIPLE_QUOTE_LENGTH) ?: source.length
                }
                source[index] == '\"' -> {
                    index = skipQuoted(source, index, '\"')
                }
                source[index] == '\'' -> {
                    index = skipQuoted(source, index, '\'')
                }
                else -> {
                    code.append(source[index])
                    index++
                }
            }
        }
        return code.toString()
    }

    private fun skipQuoted(
        source: String,
        start: Int,
        quote: Char,
    ): Int {
        var index = start + 1
        while (index < source.length) {
            if (source[index] == '\\') {
                index += 2
            } else if (source[index++] == quote) {
                break
            }
        }
        return index.coerceAtMost(source.length)
    }

    private fun skipBlockComment(
        source: String,
        start: Int,
    ): Int {
        var depth = 1
        var index = start + 2
        while (index < source.length && depth > 0) {
            when {
                source.startsWith("/*", index) -> {
                    depth++
                    index += 2
                }
                source.startsWith("*/", index) -> {
                    depth--
                    index += 2
                }
                else -> index++
            }
        }
        return index
    }
}
