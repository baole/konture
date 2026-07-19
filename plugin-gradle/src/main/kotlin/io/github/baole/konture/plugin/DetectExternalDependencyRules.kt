/*
 * Copyright 2026 Bao Le Duc
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

/** Detects direct Konture external-dependency assertions in Kotlin test sources. */
@CacheableTask
abstract class DetectExternalDependencyRules : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val testSources: ConfigurableFileCollection

    @get:OutputFile
    abstract val resultFile: RegularFileProperty

    @TaskAction
    fun detect() {
        val requiresGraph = testSources.files.any { file ->
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
                    index = source.indexOf("\"\"\"", index + 3).takeIf { it >= 0 }?.plus(3) ?: source.length
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
