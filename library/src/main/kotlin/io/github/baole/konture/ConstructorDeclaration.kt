/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/** Represents a constructor declaration of a Kotlin class. */
public data class ConstructorDeclaration(
    /** Filter or assertion criteria for visibility. */
    public val visibility: Visibility,
    /** Filter or assertion criteria for parameters. */
    public val parameters: List<ParameterDeclaration>,
    /** Filter or assertion criteria for annotations. */
    public val annotations: List<AnnotationDeclaration>,
)
