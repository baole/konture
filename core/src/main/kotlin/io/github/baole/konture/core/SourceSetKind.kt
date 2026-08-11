/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

/**
 * Categorizes the source set types.
 */
public enum class SourceSetKind {
    /** Plain Kotlin JVM or Kotlin-only source set. */
    KOTLIN_JVM,

    /** Android-specific build variant source set. */
    ANDROID_VARIANT,

    /** Kotlin Multiplatform shared or platform-specific source set. */
    KMP,
}
