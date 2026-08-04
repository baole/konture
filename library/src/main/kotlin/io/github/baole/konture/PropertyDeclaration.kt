/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a parsed Kotlin property declaration (top-level or member property).
 *
 * @property name Simple property name (e.g., `id`).
 * @property visibility Declared visibility modifier of the property.
 * @property modifiers Modifiers declared on the property (e.g. `CONST`, `LATEINIT`).
 * @property type Property type name as written in source code.
 * @property isVal True if declared with `val`, false if declared with `var`.
 * @property annotations List of annotations declared on the property.
 * @property kdocText Raw text content of KDoc attached to the property, or null if absent.
 * @property isExtension True if this property is an extension property.
 * @property resolvedType Fully qualified or resolved type name if available.
 * @property sourceLine 1-indexed source line number where the property is declared.
 */
data class PropertyDeclaration(
    val name: String,
    val visibility: Visibility,
    val modifiers: Set<Modifier>,
    val type: String,
    val isVal: Boolean,
    val annotations: List<AnnotationDeclaration>,
    val kdocText: String?,
    val isExtension: Boolean = false,
    val resolvedType: String? = null,
    val sourceLine: Int = -1,
) {
    val isVar: Boolean get() = !isVal
}
