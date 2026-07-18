/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

data class ParameterDeclaration(
    val name: String,
    val type: String,
    val hasDefaultValue: Boolean,
    val annotations: List<AnnotationDeclaration>,
)
