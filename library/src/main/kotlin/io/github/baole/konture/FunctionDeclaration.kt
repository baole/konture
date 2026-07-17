/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

data class FunctionDeclaration(
    val name: String,
    val visibility: Visibility,
    val modifiers: Set<Modifier>,
    val returnType: String,
    val parameters: List<ParameterDeclaration>,
    val annotations: List<AnnotationDeclaration>,
    val kdocText: String?,
    val isExtension: Boolean,
)
