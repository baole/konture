/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a parsed function or constructor parameter declaration.
 *
 * @property name Parameter name (e.g., `id`).
 * @property type Parameter type string as written in source code.
 * @property hasDefaultValue True if a default argument value expression is declared for this parameter.
 * @property annotations List of annotations declared on this parameter.
 * @property resolvedType Fully qualified or resolved parameter type name if available.
 */
data class ParameterDeclaration(
    val name: String,
    val type: String,
    val hasDefaultValue: Boolean,
    val annotations: List<AnnotationDeclaration>,
    val resolvedType: String? = null,
)
