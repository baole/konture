/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a parsed Kotlin class constructor (primary or secondary).
 *
 * @property visibility Declared visibility modifier of the constructor.
 * @property parameters List of parameter declarations accepted by this constructor.
 * @property annotations List of annotations declared on this constructor.
 */
data class ConstructorDeclaration(
    val visibility: Visibility,
    val parameters: List<ParameterDeclaration>,
    val annotations: List<AnnotationDeclaration>,
)
