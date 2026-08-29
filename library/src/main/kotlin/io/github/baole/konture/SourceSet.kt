/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
public data class SourceSet(
    /** Filter or assertion criteria for name. */
    public val name: String,
    // "KOTLIN_JVM", "ANDROID_VARIANT", "KMP"
    public val kind: String,
    /** Filter or assertion criteria for production. */
    public val production: Boolean,
    /** Filter or assertion criteria for src dirs. */
    public val srcDirs: List<String>,
    /** Filter or assertion criteria for platforms. */
    public val platforms: List<String> = emptyList(),
    /** Source sets in this source set's Kotlin compilation/dependsOn visibility closure. */
    public val dependsOnSourceSets: List<String> = emptyList(),
)
