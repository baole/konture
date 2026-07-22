/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

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
