/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

/**
 * Gradle task input data model representing an extracted module source set configuration.
 *
 * @property name Source set name (e.g., `main`, `test`, `commonMain`).
 * @property kind Source set kind classification string.
 * @property production True if this source set represents production code.
 * @property srcDirs List of source directory paths belonging to this source set.
 * @property platforms List of target platforms supported (e.g. `JVM`, `ANDROID`, `NATIVE`).
 * @property targetNames List of target names.
 * @property dependsOnSourceSets List of parent source set names depended on (for KMP hierarchies).
 * @property dependencyConfigurations List of dependency configuration names associated with this source set.
 * @property compileClasspath Compile classpath entry paths.
 * @property jvmTarget Target JVM bytecode level if applicable.
 */
data class SourceSetData(
    val name: String,
    val kind: String,
    val production: Boolean,
    val srcDirs: List<String>,
    val platforms: List<String> = emptyList(),
    val targetNames: List<String> = emptyList(),
    val dependsOnSourceSets: List<String> = emptyList(),
    val dependencyConfigurations: List<String> = emptyList(),
    val compileClasspath: List<String> = emptyList(),
    val jvmTarget: String? = null,
) : Serializable
