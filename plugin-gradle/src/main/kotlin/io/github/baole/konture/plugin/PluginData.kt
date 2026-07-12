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

data class SourceSetData(
    val name: String,
    val kind: String,
    val production: Boolean,
    val srcDirs: List<String>,
    val platforms: List<String> = emptyList(),
) : Serializable

data class DependencyData(
    val configuration: String,
    val targetBuildId: String,
    val targetPath: String,
) : Serializable
