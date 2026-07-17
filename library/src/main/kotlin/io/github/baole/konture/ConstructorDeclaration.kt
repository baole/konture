/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

data class ConstructorDeclaration(
    val visibility: Visibility,
    val parameters: List<ParameterDeclaration>,
    val annotations: List<AnnotationDeclaration>,
)
