/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlinx.serialization.Serializable

/**
 * Represents the visibility modifier of a Kotlin declaration (public, internal, protected, or private).
 */
@Serializable
public enum class Visibility {
    /**
     * Public visibility modifier.
     */
    PUBLIC,

    /**
     * Internal visibility modifier.
     */
    INTERNAL,

    /**
     * Protected visibility modifier.
     */
    PROTECTED,

    /**
     * Private visibility modifier.
     */
    PRIVATE,
}
