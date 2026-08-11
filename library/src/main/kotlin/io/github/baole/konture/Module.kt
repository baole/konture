/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
    /** Filter or assertion criteria for build id. */
    val buildId: String,
    /** Filter or assertion criteria for path. */
    val path: String,
    /** Filter or assertion criteria for project dir. */
    val projectDir: String,
    /** Filter or assertion criteria for applied plugins. */
    val appliedPlugins: List<String>,
    /** Filter or assertion criteria for source sets. */
    val sourceSets: List<SourceSet>,
    /** Filter or assertion criteria for dependencies. */
    val dependencies: List<Dependency>,
    /** Filter or assertion criteria for files. */
    val files: List<FileDeclaration> = emptyList(),
) {
    /** Filter or assertion criteria for classes. */
    val classes: List<ClassDeclaration> get() = files.flatMap { it.classes }
}
