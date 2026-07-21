/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

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
