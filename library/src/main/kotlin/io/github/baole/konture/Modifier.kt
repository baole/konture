/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Language modifiers supported in Kotlin declarations.
 */
enum class Modifier {
    /** Sealed class or interface modifier. */
    SEALED,

    /** Open class or member modifier. */
    OPEN,

    /** Abstract class or member modifier. */
    ABSTRACT,

    /** Data class modifier. */
    DATA,

    /** Value class modifier. */
    VALUE,

    /** Inner class modifier. */
    INNER,

    /** Inline function or value class modifier. */
    INLINE,

    /** Suspend function modifier. */
    SUSPEND,

    /** Companion object modifier. */
    COMPANION,

    /** Object declaration modifier. */
    OBJECT,

    /** Kotlin Multiplatform expect declaration modifier. */
    EXPECT,

    /** Kotlin Multiplatform actual declaration modifier. */
    ACTUAL,

    /** Const property modifier. */
    CONST,

    /** Lateinit property modifier. */
    LATEINIT,
}
