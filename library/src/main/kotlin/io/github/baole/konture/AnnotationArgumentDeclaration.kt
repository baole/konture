/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlinx.serialization.Serializable

/** Represents an argument passed to a Kotlin annotation. */
@Serializable
public data class AnnotationArgumentDeclaration(
    /** Filter or assertion criteria for name. */
    public val name: String?,
    /** Filter or assertion criteria for value. */
    public val value: String,
)
