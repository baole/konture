/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a property declaration within a Kotlin class or source file.
 *
 * @property name Property name.
 * @property visibility Explicit or implicit visibility modifier.
 * @property modifiers Set of modifiers attached to the property.
 * @property type Declared property type string.
 * @property isVal Whether the property is read-only (`val`).
 * @property annotations List of annotations attached to this property.
 * @property kdocText KDoc comment string if present.
 * @property isExtension Whether this property is an extension property.
 * @property resolvedType Fully qualified resolved type string if available.
 * @property sourceLine 1-based source code line number.
 */
public data class PropertyDeclaration(
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
    /** Whether the property is mutable (`var`). */
    public val isVar: Boolean get() = !isVal
}
