/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

/**
 * Serialized representation of a Gradle source set captured during configuration analysis.
 *
 * @property name Name of the source set (e.g. "main", "test", "commonMain").
 * @property kind Classification of the source set (e.g. "KOTLIN_JVM", "ANDROID_VARIANT", "KMP").
 * @property production Whether this is a production source set versus a test/benchmark source set.
 * @property srcDirs Source directory filesystem paths.
 * @property platforms Target platforms for KMP source sets.
 * @property targetNames Target names for KMP source sets.
 * @property dependsOnSourceSets Names of other source sets this source set depends on.
 * @property dependencyConfigurations Dependency configuration names attached to this source set.
 * @property compileClasspath Resolved classpath file entries.
 * @property jvmTarget Configured JVM bytecode target level.
 */
public data class SourceSetData(
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
