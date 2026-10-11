/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.test

import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.FunctionDeclaration
import io.github.baole.konture.PropertyDeclaration
import io.github.baole.konture.impl.PsiParser
import kotlin.io.path.createTempFile

/**
 * Rapid AST prototyping and assertion test harness for Konture agents and developers.
 *
 * Allows parsing raw Kotlin source code snippets directly into fully resolved
 * [FileDeclaration], [ClassDeclaration], [FunctionDeclaration], and [PropertyDeclaration]
 * models using Konture's embeddable PSI engine in a single line of test code.
 */
object SnippetAssertionHarness {
    /**
     * Parses a raw Kotlin source snippet string into a [FileDeclaration].
     *
     * @param snippet Raw Kotlin source code snippet (will be trimmed with [trimIndent]).
     * @param fileName Optional file name for the parsed snippet (default "Snippet.kt").
     * @return The parsed [FileDeclaration] model.
     */
    fun parseSnippet(
        snippet: String,
        fileName: String = "Snippet.kt",
    ): FileDeclaration {
        val temp = createTempFile(prefix = "konture_snippet_", suffix = ".kt").toFile()
        try {
            temp.writeText(snippet.trimIndent())
            return PsiParser.parseFile(temp)
                ?: error("Failed to parse snippet into FileDeclaration: $fileName")
        } finally {
            temp.delete()
        }
    }

    /**
     * Parses a snippet and returns its single/first class declaration.
     */
    fun parseClass(
        snippet: String,
        className: String? = null,
    ): ClassDeclaration {
        val file = parseSnippet(snippet)
        return if (className != null) {
            file.classes.firstOrNull { it.name == className }
                ?: error("Class '$className' not found in snippet. Available classes: ${file.classes.map { it.name }}")
        } else {
            file.classes.firstOrNull()
                ?: error("No classes found in snippet.")
        }
    }

    /**
     * Parses a snippet and returns its single/first top-level function declaration.
     */
    fun parseFunction(
        snippet: String,
        functionName: String? = null,
    ): FunctionDeclaration {
        val file = parseSnippet(snippet)
        return if (functionName != null) {
            file.topLevelFunctions.firstOrNull { it.name == functionName }
                ?: error("Function '$functionName' not found in snippet. Available: ${file.topLevelFunctions.map { it.name }}")
        } else {
            file.topLevelFunctions.firstOrNull()
                ?: error("No top-level functions found in snippet.")
        }
    }

    /**
     * Parses a snippet and returns its single/first top-level property declaration.
     */
    fun parseProperty(
        snippet: String,
        propertyName: String? = null,
    ): PropertyDeclaration {
        val file = parseSnippet(snippet)
        return if (propertyName != null) {
            file.topLevelProperties.firstOrNull { it.name == propertyName }
                ?: error("Property '$propertyName' not found in snippet. Available: ${file.topLevelProperties.map { it.name }}")
        } else {
            file.topLevelProperties.firstOrNull()
                ?: error("No top-level properties found in snippet.")
        }
    }
}
