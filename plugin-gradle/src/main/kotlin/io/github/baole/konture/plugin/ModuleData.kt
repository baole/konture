/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

/**
 * Serialized representation of a Gradle module captured during configuration analysis.
 *
 * @property path The Gradle project path (e.g., ":core").
 * @property projectDir Absolute filesystem path to the project directory.
 * @property appliedPlugins List of plugin IDs applied to this project.
 * @property sourceSets Source sets configured in this module.
 * @property dependencies Direct dependencies declared in this module.
 */
public data class ModuleData(
    val path: String,
    val projectDir: String,
    val appliedPlugins: List<String>,
    val sourceSets: List<SourceSetData>,
    val dependencies: List<DependencyData>,
) : Serializable
