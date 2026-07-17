/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a source set within a module at test runtime.
 *
 * @property name Name of the source set (e.g., `main`, `test`).
 * @property kind Technological type of the source set, i.e., "KOTLIN_JVM", "ANDROID_VARIANT", or "KMP".
 * @property production True if this represents a production source set, false otherwise.
 * @property srcDirs Source directories mapped to this source set.
 * @property kotlinFiles List of relative Kotlin file paths.
 * @property platforms List of target platforms associated with this source set (e.g., "jvm", "js", "native").
 */
data class SourceSet(
    val name: String,
    // "KOTLIN_JVM", "ANDROID_VARIANT", "KMP"
    val kind: String,
    val production: Boolean,
    val srcDirs: List<String>,
    val kotlinFiles: List<String>,
    val platforms: List<String> = emptyList(),
)
