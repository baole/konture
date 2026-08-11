/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a parameter in a function or constructor declaration.
 *
 * @property name Parameter name.
 * @property type Declared parameter type string.
 * @property hasDefaultValue Whether the parameter has a default argument value.
 * @property annotations List of annotations attached to this parameter.
 * @property resolvedType Fully qualified resolved type if available.
 */
public data class ParameterDeclaration(
    val name: String,
    val type: String,
    val hasDefaultValue: Boolean,
    val annotations: List<AnnotationDeclaration>,
    val resolvedType: String? = null,
)
