/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.core.BuildModel
import io.github.baole.konture.core.DependencyEdge
import io.github.baole.konture.core.ExclusionsModel
import io.github.baole.konture.core.LayoutModel
import io.github.baole.konture.core.ModuleModel
import io.github.baole.konture.core.SourceSetKind
import io.github.baole.konture.core.SourceSetModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProjectGraphLoaderExclusionTest {
    @TempDir
    lateinit var tempDir: File

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    @Test
    fun `test ProjectGraphLoader filters excluded modules, packages, and classes`() {
        // 1. Create temporary Kotlin source files for parsing
        val moduleADir = File(tempDir, "module-a").apply { mkdirs() }
        val moduleBDir = File(tempDir, "module-b").apply { mkdirs() }

        val serviceFile =
            File(moduleADir, "MyService.kt").apply {
                writeText(
                    """
                    package com.example.domain

                    class MyService
                    """.trimIndent(),
                )
            }

        val excludedPkgFile =
            File(moduleADir, "ExcludedPkgService.kt").apply {
                writeText(
                    """
                    package com.example.exclude.sub

                    class ExcludedPkgService
                    """.trimIndent(),
                )
            }

        val excludedClassFile =
            File(moduleADir, "ExcludedClass.kt").apply {
                writeText(
                    """
                    package com.example.domain

                    class ExcludedClass
                    """.trimIndent(),
                )
            }

        val classBFile =
            File(moduleBDir, "ServiceB.kt").apply {
                writeText(
                    """
                    package com.example.b

                    class ServiceB
                    """.trimIndent(),
                )
            }

        // 2. Build the LayoutModel with exclusions
        val exclusions =
            ExclusionsModel(
                excludeModules = listOf(":module-b"),
                excludePackages = listOf("com.example.exclude.."),
                excludeClasses = listOf("ExcludedClass"),
            )

        val sourceSetA =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf(moduleADir.absolutePath),
                kotlinFiles =
                    listOf(
                        serviceFile.absolutePath,
                        excludedPkgFile.absolutePath,
                        excludedClassFile.absolutePath,
                    ),
            )

        val sourceSetB =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf(moduleBDir.absolutePath),
                kotlinFiles = listOf(classBFile.absolutePath),
            )

        val moduleA =
            ModuleModel(
                path = ":module-a",
                projectDir = moduleADir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets = listOf(sourceSetA),
                dependencies =
                    listOf(
                        DependencyEdge("implementation", ":", ":module-b"),
                    ),
            )

        val moduleB =
            ModuleModel(
                path = ":module-b",
                projectDir = moduleBDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets = listOf(sourceSetB),
                dependencies = emptyList(),
            )

        val buildModel =
            BuildModel(
                id = ":",
                modules = listOf(moduleA, moduleB),
            )

        val layoutModel =
            LayoutModel(
                schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
                builds = listOf(buildModel),
                exclusions = exclusions,
            )

        val jsonString = json.encodeToString(layoutModel)
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())

        // 3. Load ProjectGraph using ProjectGraphLoader
        val projectGraph = ProjectGraphLoader.loadFromStream(inputStream)

        // 4. Assertions
        val allModules = projectGraph.getAllModules()

        // Ensure module-b was filtered out
        assertEquals(1, allModules.size)
        val loadedModuleA = allModules.first()
        assertEquals(":module-a", loadedModuleA.path)

        // Ensure dependencies to module-b were filtered out from module-a
        assertTrue(loadedModuleA.dependencies.isEmpty())

        // Ensure classes in module-a were filtered based on package and class name patterns
        val classes = loadedModuleA.classes
        assertEquals(1, classes.size)
        val remainingClass = classes.first()
        assertEquals("MyService", remainingClass.name)
        assertEquals("com.example.domain.MyService", remainingClass.fqName)
    }

    @Test
    fun `test backwards compatibility when exclusions field is missing`() {
        // If an older layout.json doesn't contain "exclusions", it should still load successfully with default empty exclusions.
        val jsonString =
            """
            {
              "schemaVersion": 2,
              "builds": [
                {
                  "id": ":",
                  "modules": [
                    {
                      "path": ":module-a",
                      "projectDir": "${tempDir.absolutePath.replace("\\", "\\\\")}",
                      "appliedPlugins": ["kotlin"],
                      "sourceSets": [],
                      "dependencies": []
                    }
                  ]
                }
              ]
            }
            """.trimIndent()

        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        val projectGraph = ProjectGraphLoader.loadFromStream(inputStream)

        val allModules = projectGraph.getAllModules()
        assertEquals(1, allModules.size)
        assertEquals(":module-a", allModules.first().path)
    }

    @Test
    fun `test ProjectGraphLoader simple glob class exclusion and configurations exclusion`() {
        // 1. Create temporary files
        val moduleDir = File(tempDir, "module-glob").apply { mkdirs() }

        val controllerFile =
            File(moduleDir, "MyController.kt").apply {
                writeText(
                    """
                    package com.example.web

                    class MyController
                    """.trimIndent(),
                )
            }

        val helperFile =
            File(moduleDir, "MyHelper.kt").apply {
                writeText(
                    """
                    package com.example.util

                    class MyHelper
                    """.trimIndent(),
                )
            }

        val testFile =
            File(moduleDir, "MyTest.kt").apply {
                writeText(
                    """
                    package com.example.util

                    class MyTest
                    """.trimIndent(),
                )
            }

        // 2. Build the LayoutModel with glob and configuration exclusions
        val exclusions =
            ExclusionsModel(
                excludeClasses = listOf("*Helper", "*Test"),
                excludeConfigurations = listOf("test*", "myCustom*"),
            )

        val sourceSet =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf(moduleDir.absolutePath),
                kotlinFiles = listOf(controllerFile.absolutePath, helperFile.absolutePath, testFile.absolutePath),
            )

        val dependencies =
            listOf(
                DependencyEdge(
                    configuration = "implementation",
                    targetBuildId = ":",
                    targetPath = ":other-module",
                ),
                DependencyEdge(
                    configuration = "testImplementation",
                    targetBuildId = ":",
                    targetPath = ":other-module",
                ),
                DependencyEdge(
                    configuration = "myCustomConfiguration",
                    targetBuildId = ":",
                    targetPath = ":other-module",
                ),
            )

        val moduleModel =
            ModuleModel(
                path = ":module-glob",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets = listOf(sourceSet),
                dependencies = dependencies,
            )

        val layoutModel =
            LayoutModel(
                schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
                builds =
                    listOf(
                        BuildModel(
                            id = ":",
                            modules = listOf(moduleModel),
                        ),
                    ),
                exclusions = exclusions,
            )

        val jsonString = json.encodeToString(LayoutModel.serializer(), layoutModel)
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        val projectGraph = ProjectGraphLoader.loadFromStream(inputStream)

        // 3. Assertions
        val allModules = projectGraph.getAllModules()
        assertEquals(1, allModules.size)
        val loadedModule = allModules.first()

        // Assert classes: MyHelper and MyTest should be excluded, leaving only MyController
        val classes = loadedModule.classes
        assertEquals(1, classes.size)
        assertEquals("MyController", classes.first().name)

        // Assert dependencies: testImplementation and myCustomConfiguration should be excluded, leaving only implementation
        val loadedDeps = loadedModule.dependencies
        assertEquals(1, loadedDeps.size)
        assertEquals("implementation", loadedDeps.first().configuration)
    }
}
