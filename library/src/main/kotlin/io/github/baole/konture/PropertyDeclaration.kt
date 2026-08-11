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
    /** Filter or assertion criteria for name. */
    val name: String,
    /** Filter or assertion criteria for visibility. */
    val visibility: Visibility,
    /** Filter or assertion criteria for modifiers. */
    val modifiers: Set<Modifier>,
    /** Filter or assertion criteria for type. */
    val type: String,
    /** Filter or assertion criteria for is val. */
    val isVal: Boolean,
    /** Filter or assertion criteria for annotations. */
    val annotations: List<AnnotationDeclaration>,
    /** Filter or assertion criteria for kdoc text. */
    val kdocText: String?,
    /** Filter or assertion criteria for is extension. */
    val isExtension: Boolean = false,
    /** Filter or assertion criteria for resolved type. */
    val resolvedType: String? = null,
    /** Filter or assertion criteria for source line. */
    val sourceLine: Int = -1,
) {
    /** Whether the property is mutable (`var`). */
    public val isVar: Boolean get() = !isVal
}
