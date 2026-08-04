/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Visibility modifiers supported in Kotlin declarations.
 */
enum class Visibility {
    /** Visible anywhere. */
    PUBLIC,

    /** Visible within the same module. */
    INTERNAL,

    /** Visible in subclasses and declaring class/file. */
    PROTECTED,

    /** Visible only within the declaring scope or file. */
    PRIVATE,
}
