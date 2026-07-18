/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.core.BuildModel
import io.github.baole.konture.core.ExclusionsModel
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LayoutModel
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.ModuleModel
import io.github.baole.konture.core.SourceSetKind
import io.github.baole.konture.core.SourceSetModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProjectGraphLoaderTest {
    @TempDir
    lateinit var tempDir: File

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    @Test
    fun `test invalid log level fallback`() {
        val layoutModel =
            LayoutModel(
                schemaVersion = 2,
                logLevel = "SUPER_VERBOSE_UNKNOWN_LEVEL",
                builds = emptyList(),
            )
        val jsonString = json.encodeToString(layoutModel)
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())

        ProjectGraphLoader.loadFromStream(inputStream)
        assertEquals(LogLevel.INFO, KontureLogger.minLevel)
    }

    @Test
    fun `test settings gradle lookup fallback to user dir`() {
        val originalUserDir = System.getProperty("user.dir")
        val uniqueTempUserDir = File(tempDir, "fallback-user-dir-${System.currentTimeMillis()}")
        assertTrue(uniqueTempUserDir.mkdirs())

        try {
            System.setProperty("user.dir", uniqueTempUserDir.absolutePath)

            val moduleModel =
                ModuleModel(
                    path = ":module-a",
                    projectDir = "relative-subdir",
                    appliedPlugins = listOf("kotlin"),
                    sourceSets = emptyList(),
                    dependencies = emptyList(),
                )
            val buildModel =
                BuildModel(
                    id = ":",
                    modules = listOf(moduleModel),
                )
            val layoutModel =
                LayoutModel(
                    schemaVersion = 2,
                    builds = listOf(buildModel),
                )
            val jsonString = json.encodeToString(layoutModel)
            val inputStream = ByteArrayInputStream(jsonString.toByteArray())

            val graph = ProjectGraphLoader.loadFromStream(inputStream)
            val moduleA = graph.getAllModules().first()

            val expectedCanonicalPath = File(uniqueTempUserDir.canonicalFile, "relative-subdir").canonicalPath
            assertEquals(expectedCanonicalPath, moduleA.projectDir)
        } finally {
            System.setProperty("user.dir", originalUserDir)
            uniqueTempUserDir.deleteRecursively()
        }
    }

    @Test
    fun `test class exclusions with fully qualified names`() {
        val moduleDir = File(tempDir, "module-a").apply { mkdirs() }
        val testFile =
            File(moduleDir, "TestClass.kt").apply {
                writeText(
                    """
                    package com.example.domain

                    class TestClass
                    """.trimIndent(),
                )
            }

        val excludedFile =
            File(moduleDir, "ExcludedClass.kt").apply {
                writeText(
                    """
                    package com.example.domain

                    class ExcludedClass
                    """.trimIndent(),
                )
            }

        val exclusions =
            ExclusionsModel(
                excludeClasses = listOf("com.example.domain.ExcludedClass"),
            )

        val sourceSet =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf(moduleDir.absolutePath),
                kotlinFiles = listOf(testFile.absolutePath, excludedFile.absolutePath),
            )

        val module =
            ModuleModel(
                path = ":module-a",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets = listOf(sourceSet),
                dependencies = emptyList(),
            )

        val buildModel =
            BuildModel(
                id = ":",
                modules = listOf(module),
            )

        val layoutModel =
            LayoutModel(
                schemaVersion = 2,
                builds = listOf(buildModel),
                exclusions = exclusions,
            )

        val jsonString = json.encodeToString(layoutModel)
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())

        val graph = ProjectGraphLoader.loadFromStream(inputStream)
        val loadedModule = graph.getAllModules().first()

        assertEquals(1, loadedModule.classes.size)
        assertEquals("TestClass", loadedModule.classes.first().name)
    }

    @Test
    fun `test missing file warning recovery`() {
        val moduleDir = File(tempDir, "module-a").apply { mkdirs() }
        val sourceSet =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf(moduleDir.absolutePath),
                kotlinFiles = listOf(File(moduleDir, "NonExistentFile.kt").absolutePath),
            )

        val module =
            ModuleModel(
                path = ":module-a",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets = listOf(sourceSet),
                dependencies = emptyList(),
            )

        val buildModel =
            BuildModel(
                id = ":",
                modules = listOf(module),
            )

        val layoutModel =
            LayoutModel(
                schemaVersion = 2,
                builds = listOf(buildModel),
            )

        val jsonString = json.encodeToString(layoutModel)
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())

        val graph = ProjectGraphLoader.loadFromStream(inputStream)
        val loadedModule = graph.getAllModules().first()
        assertTrue(loadedModule.classes.isEmpty())
    }

    @Test
    fun `test non-existent resource file throwing IllegalArgumentException`() {
        val originalUserDir = System.getProperty("user.dir")
        val emptyTempDir = File(tempDir, "empty-user-dir").apply { mkdirs() }

        try {
            System.setProperty("user.dir", emptyTempDir.absolutePath)
            assertThrows(IllegalArgumentException::class.java) {
                ProjectGraphLoader.loadFromResource("/nonexistent-layout-file.json")
            }
        } finally {
            System.setProperty("user.dir", originalUserDir)
        }
    }

    @Test
    fun `test load default session from resource`() {
        val graph = ProjectGraph.fromResource("/test-layout.json")
        assertNotNull(graph)
        val defaultGraph = ProjectGraph.getDefault()
        assertEquals(graph, defaultGraph)
    }

    @Test
    fun `test lazy loading of external dependencies from directory`() {
        val buildModel = BuildModel(id = ":", modules = emptyList())
        val layoutModel = LayoutModel(schemaVersion = 2, builds = listOf(buildModel))
        val layoutFile = File(tempDir, "layout.json")
        layoutFile.writeText(json.encodeToString(layoutModel))

        val dep =
            io.github.baole.konture.core.ResolvedDependencyModel(
                "org.jetbrains",
                "annotations",
                "24.0.0",
                "implementation",
                isTransitive = false,
            )
        val extDeps =
            io.github.baole.konture.core
                .DependencyGraphModel(modules = mapOf(":module-x" to listOf(dep)))
        val depsFile = File(tempDir, "dependencies.json")
        depsFile.writeText(json.encodeToString(extDeps))

        val graph =
            ProjectGraphLoader.loadFromStream(
                inputStream = layoutFile.inputStream(),
                depsStreamLoader = { if (depsFile.exists()) depsFile.inputStream() else null },
            )
        assertNotNull(graph)

        // Retrieve and verify lazy loaded external dependencies
        val loadedDeps = graph.externalDependencies
        assertNotNull(loadedDeps)
        val modDeps = loadedDeps.modules[":module-x"]
        assertNotNull(modDeps)
        assertEquals(1, modDeps?.size)
        assertEquals("org.jetbrains", modDeps?.first()?.group)
        assertEquals("annotations", modDeps?.first()?.name)
        assertEquals("24.0.0", modDeps?.first()?.version)
    }

    @Test
    fun `test lazy loading fallback when dependencies file is missing`() {
        val buildModel = BuildModel(id = ":", modules = emptyList())
        val layoutModel = LayoutModel(schemaVersion = 2, builds = listOf(buildModel))
        val layoutFile = File(tempDir, "layout.json")
        layoutFile.writeText(json.encodeToString(layoutModel))

        // No dependencies.json is created.
        val graph =
            ProjectGraphLoader.loadFromStream(
                inputStream = layoutFile.inputStream(),
                depsStreamLoader = { null },
            )
        assertNotNull(graph)

        // Retrieving should fall back to empty model gracefully
        val loadedDeps = graph.externalDependencies
        assertNotNull(loadedDeps)
        assertTrue(loadedDeps.modules.isEmpty())
    }
}
