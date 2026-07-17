/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a single Gradle module/project and all its structural and source declarations.
 *
 * @property buildId The ID of the build containing this module.
 * @property path The Gradle project path (e.g. `:core:database`).
 * @property projectDir The build-root-relative directory path.
 * @property appliedPlugins List of plugin IDs applied to this module (e.g., `kotlin-jvm`, `android-library`).
 * @property sourceSets The source sets present in this module.
 * @property dependencies Declared project dependencies of this module.
 * @property classes Parsed Kotlin class declarations contained inside this module's production source sets.
 */
data class Module(
    val buildId: String,
    val path: String,
    val projectDir: String,
    val appliedPlugins: List<String>,
    val sourceSets: List<SourceSet>,
    val dependencies: List<Dependency>,
    val files: List<FileDeclaration> = emptyList(),
) {
    val classes: List<ClassDeclaration> get() = files.flatMap { it.classes }
}
