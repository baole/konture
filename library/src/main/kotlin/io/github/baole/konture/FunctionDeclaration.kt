/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a parsed Kotlin function declaration (top-level or member function).
 *
 * @property name Simple function name (e.g., `execute`).
 * @property visibility Declared visibility modifier of the function.
 * @property modifiers Modifiers declared on the function (e.g. `SUSPEND`, `INLINE`, `OPEN`).
 * @property returnType Return type name as written in code.
 * @property parameters List of parameters declared by the function.
 * @property annotations List of annotations declared on the function.
 * @property kdocText Raw text content of KDoc attached to the function, or null if absent.
 * @property isExtension True if this function is an extension function.
 * @property sourceStartOffset Start character offset in source file, or -1 if unknown.
 * @property sourceEndOffset End character offset in source file, or -1 if unknown.
 * @property resolvedReturnType Fully qualified or resolved return type name if available.
 * @property sourceLine 1-indexed source line number where the function is declared.
 */
data class FunctionDeclaration(
    val name: String,
    val visibility: Visibility,
    val modifiers: Set<Modifier>,
    val returnType: String,
    val parameters: List<ParameterDeclaration>,
    val annotations: List<AnnotationDeclaration>,
    val kdocText: String?,
    val isExtension: Boolean,
    val sourceStartOffset: Int = -1,
    val sourceEndOffset: Int = -1,
    val resolvedReturnType: String? = null,
    val sourceLine: Int = -1,
)
