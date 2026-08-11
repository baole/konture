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
    /** Filter or assertion criteria for name. */
    val name: String,
    /** Filter or assertion criteria for visibility. */
    val visibility: Visibility,
    /** Filter or assertion criteria for modifiers. */
    val modifiers: Set<Modifier>,
    /** Filter or assertion criteria for return type. */
    val returnType: String,
    /** Filter or assertion criteria for parameters. */
    val parameters: List<ParameterDeclaration>,
    /** Filter or assertion criteria for annotations. */
    val annotations: List<AnnotationDeclaration>,
    /** Filter or assertion criteria for kdoc text. */
    val kdocText: String?,
    /** Filter or assertion criteria for is extension. */
    val isExtension: Boolean,
    /** Filter or assertion criteria for source start offset. */
    val sourceStartOffset: Int = -1,
    /** Filter or assertion criteria for source end offset. */
    val sourceEndOffset: Int = -1,
    /** Filter or assertion criteria for resolved return type. */
    val resolvedReturnType: String? = null,
    /** Filter or assertion criteria for source line. */
    val sourceLine: Int = -1,
    /** Filter or assertion criteria for receiver type. */
    val receiverType: String? = null,
)
