/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents the visibility modifier of a Kotlin declaration (public, internal, protected, or private).
 */
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
