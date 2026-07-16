/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

/**
 * Categorizes the source set types.
 */
enum class SourceSetKind {
    /** Plain Kotlin JVM or Kotlin-only source set. */
    KOTLIN_JVM,

    /** Android-specific build variant source set. */
    ANDROID_VARIANT,

    /** Kotlin Multiplatform shared or platform-specific source set. */
    KMP,
}
