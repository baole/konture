/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a parsed function declaration within a Kotlin source file.
 *
 * @property name Function name.
 * @property visibility Explicit or implicit visibility modifier.
 * @property modifiers Set of modifiers attached to the function.
 * @property returnType Declared return type string.
 * @property parameters Parameter declarations.
 * @property annotations List of annotations attached to this function.
 * @property kdocText KDoc comment string if present.
 * @property isExtension Whether this function is an extension function.
 * @property sourceStartOffset Source code starting character offset.
 * @property sourceEndOffset Source code ending character offset.
 * @property resolvedReturnType Fully qualified resolved return type if available.
 * @property sourceLine 1-based source code line number.
 * @property receiverType Receiver type string if this is an extension function.
 */
public data class FunctionDeclaration(
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
    val receiverType: String? = null,
)
