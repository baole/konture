package io.github.baole.konture.core

import kotlinx.serialization.Serializable

/**
 * Represents a single Gradle project/module.
 *
 * @property path The unique Gradle project path (e.g., `:feature:profile` or `:core:database`).
 * @property projectDir The absolute path to the directory containing this project.
 * @property appliedPlugins List of plugin IDs applied to this project (e.g., `kotlin-jvm`, `android-library`).
 * @property sourceSets Source sets defined in this project, categorized by kind (JVM, Android, KMP).
 * @property dependencies List of declared project dependency edges originating from this project.
 */
@Serializable
data class ModuleModel(
    val path: String,
    val projectDir: String,
    val appliedPlugins: List<String>,
    val sourceSets: List<SourceSetModel>,
    val dependencies: List<DependencyEdge>,
)
