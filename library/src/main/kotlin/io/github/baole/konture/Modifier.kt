/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents declaration modifiers in Kotlin source code (e.g. abstract, open, sealed, suspend, override).
 */
public enum class Modifier {
    /** Sealed class or interface modifier. */
    SEALED,

    /** Open class or member modifier. */
    OPEN,

    /** Abstract class, interface, or member modifier. */
    ABSTRACT,

    /** Data class modifier. */
    DATA,

    /** Value class modifier. */
    VALUE,

    /** Inner class modifier. */
    INNER,

    /** Inline class or function modifier. */
    INLINE,

    /** Suspend function modifier. */
    SUSPEND,

    /** Companion object modifier. */
    COMPANION,

    /** Object declaration modifier. */
    OBJECT,

    /** Expect multiplatform declaration modifier. */
    EXPECT,

    /** Actual multiplatform declaration modifier. */
    ACTUAL,

    /** Const property modifier. */
    CONST,

    /** Lateinit property modifier. */
    LATEINIT,

    /** Operator function modifier. */
    OPERATOR,

    /** Infix function modifier. */
    INFIX,

    /** Override member modifier. */
    OVERRIDE,
}
