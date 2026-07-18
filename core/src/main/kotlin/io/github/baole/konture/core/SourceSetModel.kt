/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import kotlinx.serialization.Serializable

/**
 * Represents a source set within a Gradle project.
 *
 * @property name Name of the source set (e.g., `main`, `test`, `commonMain`, `debug`).
 * @property kind The technological category of the source set (JVM, Android, KMP).
 * @property production Whether this source set contains production/main code (true) or test/generated code (false).
 * @property srcDirs List of source directories belonging to this source set.
 * @property kotlinFiles List of relative paths (relative to project directory) of all Kotlin files found in this
 * source set.
 */
@Serializable
data class SourceSetModel(
    val name: String,
    val kind: SourceSetKind,
    val production: Boolean,
    val srcDirs: List<String>,
    val kotlinFiles: List<String>,
    val platforms: List<String> = emptyList(),
    /** Optional resolved compilation classpath used by v2 compiler-backed analysis. */
    val compileClasspath: List<String> = emptyList(),
    /** Optional Kotlin/JVM target metadata used to configure compiler analysis. */
    val jvmTarget: String? = null,
)
