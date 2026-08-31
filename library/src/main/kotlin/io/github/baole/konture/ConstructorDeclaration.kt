/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlinx.serialization.Serializable

/** Represents a constructor declaration of a Kotlin class. */
@Serializable
public data class ConstructorDeclaration(
    /** Filter or assertion criteria for visibility. */
    public val visibility: Visibility,
    /** Filter or assertion criteria for parameters. */
    public val parameters: List<ParameterDeclaration>,
    /** Filter or assertion criteria for annotations. */
    public val annotations: List<AnnotationDeclaration>,
)
