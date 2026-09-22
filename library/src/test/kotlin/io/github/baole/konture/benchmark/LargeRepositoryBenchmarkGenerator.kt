/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.benchmark

import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.core.BuildModel
import io.github.baole.konture.core.DependencyEdge
import io.github.baole.konture.core.LayoutModel
import io.github.baole.konture.core.ModuleModel
import io.github.baole.konture.core.SourceSetKind
import io.github.baole.konture.core.SourceSetModel
import io.github.baole.konture.impl.ProjectGraphLoader
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Synthetic repository generator for large-scale benchmarks (100+ modules).
 */
object LargeRepositoryBenchmarkGenerator {
    private val json =
        Json {
            prettyPrint = false
            ignoreUnknownKeys = true
        }

    fun generateRepository(
        rootTempDir: File,
        moduleCount: Int = 100,
    ): ProjectGraph {
        val modules = mutableListOf<ModuleModel>()

        // Layer distribution:
        // Core: ~20%
        // Domain: ~30%
        // Feature: ~35%
        // App: ~15%
        val coreCount = maxOf(1, (moduleCount * 0.20).toInt())
        val domainCount = maxOf(1, (moduleCount * 0.30).toInt())
        val featureCount = maxOf(1, (moduleCount * 0.35).toInt())
        val appCount = maxOf(1, moduleCount - coreCount - domainCount - featureCount)

        var moduleIndex = 0

        val coreModules =
            (1..coreCount).map { i ->
                moduleIndex++
                createModule(rootTempDir, "core-$i", "core", emptyList())
            }
        modules.addAll(coreModules)

        val domainModules =
            (1..domainCount).map { i ->
                moduleIndex++
                val coreDep = coreModules[(i - 1) % coreModules.size].path
                createModule(rootTempDir, "domain-$i", "domain", listOf(coreDep))
            }
        modules.addAll(domainModules)

        val featureModules =
            (1..featureCount).map { i ->
                moduleIndex++
                val domainDep = domainModules[(i - 1) % domainModules.size].path
                val coreDep = coreModules[(i - 1) % coreModules.size].path
                createModule(rootTempDir, "feature-$i", "feature", listOf(domainDep, coreDep))
            }
        modules.addAll(featureModules)

        val appModules =
            (1..appCount).map { i ->
                moduleIndex++
                val featDep = featureModules[(i - 1) % featureModules.size].path
                val domainDep = domainModules[(i - 1) % domainModules.size].path
                createModule(rootTempDir, "app-$i", "app", listOf(featDep, domainDep))
            }
        modules.addAll(appModules)

        val buildModel =
            BuildModel(
                id = ":",
                modules = modules,
            )

        val layoutModel =
            LayoutModel(
                schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
                builds = listOf(buildModel),
            )

        val jsonString = json.encodeToString(layoutModel)
        val inputStream = ByteArrayInputStream(jsonString.toByteArray(Charsets.UTF_8))
        return ProjectGraphLoader.loadFromStream(inputStream)
    }

    private fun createModule(
        rootDir: File,
        name: String,
        layer: String,
        dependencyPaths: List<String>,
    ): ModuleModel {
        val moduleDir = File(rootDir, name).apply { mkdirs() }
        val srcDir = File(moduleDir, "src/main/kotlin/com/example/$layer/$name").apply { mkdirs() }

        val className = name.replace("-", "_").replaceFirstChar { it.uppercase() } + "Service"
        val modelName = name.replace("-", "_").replaceFirstChar { it.uppercase() } + "Model"

        File(srcDir, "$className.kt").writeText(
            """
            package com.example.$layer.$name

            class $className {
                val model = $modelName(1)
                fun execute(): String = "$layer-$name"
            }
            """.trimIndent(),
        )

        File(srcDir, "$modelName.kt").writeText(
            """
            package com.example.$layer.$name

            data class $modelName(val id: Int)
            """.trimIndent(),
        )

        val sourceSet =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf(srcDir.absolutePath),
            )

        val dependencies =
            dependencyPaths.map { depPath ->
                DependencyEdge(
                    configuration = "implementation",
                    targetBuildId = ":",
                    targetPath = depPath,
                )
            }

        return ModuleModel(
            path = ":$name",
            projectDir = moduleDir.absolutePath,
            appliedPlugins = listOf("kotlin", "java"),
            sourceSets = listOf(sourceSet),
            dependencies = dependencies,
        )
    }
}
