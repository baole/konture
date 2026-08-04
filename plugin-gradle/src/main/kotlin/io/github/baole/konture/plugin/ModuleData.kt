/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

/**
 * Gradle task input data model representing extracted module layout metadata.
 *
 * @property path Gradle module path (e.g. `:core`, `:app`).
 * @property projectDir Absolute filesystem path to module directory.
 * @property appliedPlugins List of plugin IDs applied to the module.
 * @property sourceSets Source set configurations declared in the module.
 * @property dependencies Project dependencies declared in the module.
 */
data class ModuleData(
    val path: String,
    val projectDir: String,
    val appliedPlugins: List<String>,
    val sourceSets: List<SourceSetData>,
    val dependencies: List<DependencyData>,
) : Serializable
