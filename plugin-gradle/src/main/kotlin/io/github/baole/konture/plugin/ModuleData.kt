/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

data class ModuleData(
    val path: String,
    val projectDir: String,
    val appliedPlugins: List<String>,
    val sourceSets: List<SourceSetData>,
    val dependencies: List<DependencyData>,
) : Serializable
