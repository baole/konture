/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a argument passed to an annotation declaration.
 *
 * @property name Optional argument parameter name (e.g., `value` or `target`), or null if positional.
 * @property value Raw code representation of the argument value.
 */
data class AnnotationArgumentDeclaration(
    val name: String?,
    val value: String,
)
