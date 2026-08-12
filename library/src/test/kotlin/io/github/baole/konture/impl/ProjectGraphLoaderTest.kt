/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.core.BuildModel
import io.github.baole.konture.core.DependencyEdge
import io.github.baole.konture.core.ExclusionsModel
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LayoutModel
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.ModuleModel
import io.github.baole.konture.core.ResolvedDependencyModel
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
                schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
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
                    schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
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
        File(moduleDir, "TestClass.kt").apply {
            writeText(
                """
                package com.example.domain

                class TestClass
                """.trimIndent(),
            )
        }

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
                schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
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
    fun `production parsing does not resolve types from test source sets or test dependencies`() {
        val appDir = File(tempDir, "app").apply { mkdirs() }
        val mainDir = File(appDir, "src/main").apply { mkdirs() }
        val testDir = File(appDir, "src/test").apply { mkdirs() }
        val helperDir = File(tempDir, "helper").apply { mkdirs() }
        File(mainDir, "Consumer.kt").apply {
            writeText(
                """
                package app

                class Consumer {
                    fun load(): Result<String> = TODO()
                }
                """.trimIndent(),
            )
        }
        File(testDir, "TestResult.kt").apply {
            writeText(
                """
                package app

                class Result<T>
                """.trimIndent(),
            )
        }
        File(helperDir, "HelperResult.kt").apply {
            writeText(
                """
                package app

                class Result<T>
                """.trimIndent(),
            )
        }

        val app =
            ModuleModel(
                path = ":app",
                projectDir = appDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets =
                    listOf(
                        SourceSetModel("main", SourceSetKind.KOTLIN_JVM, true, listOf(mainDir.absolutePath)),
                        SourceSetModel("test", SourceSetKind.KOTLIN_JVM, false, listOf(testDir.absolutePath)),
                    ),
                dependencies =
                    listOf(
                        DependencyEdge("runtimeOnly", ":", ":helper"),
                        DependencyEdge("testImplementation", ":", ":helper"),
                    ),
            )
        val helper =
            ModuleModel(
                path = ":helper",
                projectDir = helperDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets =
                    listOf(
                        SourceSetModel("main", SourceSetKind.KOTLIN_JVM, true, listOf(helperDir.absolutePath)),
                    ),
                dependencies = emptyList(),
            )
        val layout =
            LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, helper))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer =
            graph.getAllModules().single {
                it.path == ":app"
            }.files.single { it.name == "Consumer.kt" }.classes.single()

        assertEquals("kotlin.Result", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `project dependencies resolve imported type aliases to their underlying classes`() {
        val appDir = File(tempDir, "alias-app").apply { mkdirs() }
        val libraryDir = File(tempDir, "alias-library").apply { mkdirs() }
        val consumerFile =
            File(appDir, "Consumer.kt").apply {
                writeText(
                    """
                    package app
                    import api.PublicUser

                    class Consumer {
                        fun load(): PublicUser = TODO()
                    }
                    """.trimIndent(),
                )
            }
        val userFile = File(libraryDir, "User.kt").apply { writeText("package api\nclass User") }
        val aliasFile =
            File(
                libraryDir,
                "PublicUser.kt",
            ).apply { writeText("package api\ntypealias PublicUser = User") }
        val app =
            ModuleModel(
                path = ":app",
                projectDir = appDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "main",
                            SourceSetKind.KOTLIN_JVM,
                            true,
                            listOf(appDir.absolutePath),
                            listOf(consumerFile.absolutePath),
                        ),
                    ),
                dependencies = listOf(DependencyEdge("implementation", ":", ":library")),
            )
        val library =
            ModuleModel(
                path = ":library",
                projectDir = libraryDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "main",
                            SourceSetKind.KOTLIN_JVM,
                            true,
                            listOf(libraryDir.absolutePath),
                            listOf(userFile.absolutePath, aliasFile.absolutePath),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout =
            LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals("api.User", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `project dependencies resolve imported nested type aliases to their underlying classes`() {
        val appDir = File(tempDir, "nested-alias-app").apply { mkdirs() }
        val libraryDir = File(tempDir, "nested-alias-library").apply { mkdirs() }
        val consumerFile =
            File(appDir, "Consumer.kt").apply {
                writeText(
                    """
                    package app
                    import api.Api.PublicUser

                    class Consumer {
                        fun load(): PublicUser = TODO()
                    }
                    """.trimIndent(),
                )
            }
        val apiFile =
            File(libraryDir, "Api.kt").apply {
                writeText(
                    """
                    package api

                    class Api {
                        class User
                        typealias PublicUser = User
                    }
                    """.trimIndent(),
                )
            }
        val app =
            ModuleModel(
                path = ":app",
                projectDir = appDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "main",
                            SourceSetKind.KOTLIN_JVM,
                            true,
                            listOf(appDir.absolutePath),
                            listOf(consumerFile.absolutePath),
                        ),
                    ),
                dependencies = listOf(DependencyEdge("implementation", ":", ":library")),
            )
        val library =
            ModuleModel(
                path = ":library",
                projectDir = libraryDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "main",
                            SourceSetKind.KOTLIN_JVM,
                            true,
                            listOf(libraryDir.absolutePath),
                            listOf(apiFile.absolutePath),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout =
            LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals("api.Api.User", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `test missing file warning recovery`() {
        val moduleDir = File(tempDir, "module-a").apply { mkdirs() }
        val sourceSet =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf(File(moduleDir, "NonExistentDir").absolutePath),
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
                schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
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
        val layoutModel = LayoutModel(schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(buildModel))
        val layoutFile = File(tempDir, "layout.json")
        layoutFile.writeText(json.encodeToString(layoutModel))

        val dep =
            ResolvedDependencyModel(
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
        val layoutModel = LayoutModel(schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(buildModel))
        val layoutFile = File(tempDir, "layout.json")
        layoutFile.writeText(json.encodeToString(layoutModel))

        val graph =
            ProjectGraphLoader.loadFromStream(
                inputStream = layoutFile.inputStream(),
                depsStreamLoader = { null },
            )
        assertNotNull(graph)

        val loadedDeps = graph.externalDependencies
        assertNotNull(loadedDeps)
        assertTrue(loadedDeps.modules.isEmpty())
    }

    @Test
    fun `legacy layout_v2 json containing deprecated kotlinFiles or unknown properties is successfully parsed`() {
        val moduleDir = File(tempDir, "legacy-module").apply { mkdirs() }
        val srcDir = File(moduleDir, "src/main/kotlin").apply { mkdirs() }
        File(srcDir, "Sample.kt").writeText("package sample\nclass Sample")

        val rawJsonWithUnknownKeys =
            """
            {
              "schemaVersion": 2,
              "builds": [
                {
                  "id": ":",
                  "modules": [
                    {
                      "path": ":legacy-module",
                      "projectDir": "${moduleDir.absolutePath.replace("\\", "\\\\")}",
                      "appliedPlugins": ["kotlin"],
                      "unknownModuleProperty": "should_be_ignored",
                      "sourceSets": [
                        {
                          "name": "main",
                          "kind": "KOTLIN_JVM",
                          "production": true,
                          "srcDirs": ["${srcDir.absolutePath.replace("\\", "\\\\")}"],
                          "kotlinFiles": ["src/main/kotlin/Sample.kt"],
                          "unknownSourceSetProperty": 12345
                        }
                      ],
                      "dependencies": []
                    }
                  ]
                }
              ]
            }
            """.trimIndent()

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(rawJsonWithUnknownKeys.toByteArray()))
        val loadedModule = graph.getAllModules().single()
        assertEquals(":legacy-module", loadedModule.path)
        val loadedFile = loadedModule.files.single()
        assertEquals("Sample.kt", loadedFile.name)
    }

    @Test
    fun `dynamic file discovery traverses nested subdirectories, supports kt and kts files, and ignores non-kotlin files`() {
        val moduleDir = File(tempDir, "discovery-module").apply { mkdirs() }
        val mainSrcDir = File(moduleDir, "src/main/kotlin").apply { mkdirs() }
        val testSrcDir = File(moduleDir, "src/test/kotlin").apply { mkdirs() }

        val alphaDir = File(mainSrcDir, "com/example/a").apply { mkdirs() }
        val betaDir = File(mainSrcDir, "com/example/b").apply { mkdirs() }
        File(alphaDir, "Alpha.kt").writeText("package com.example.a\nclass Alpha")
        File(betaDir, "Beta.kt").writeText("package com.example.b\nclass Beta")
        File(mainSrcDir, "Script.kts").writeText("println(\"hello\")")

        File(mainSrcDir, "README.txt").writeText("documentation")
        val resDir = File(moduleDir, "src/main/resources").apply { mkdirs() }
        File(resDir, "config.json").writeText("{}")

        val testBetaDir = File(testSrcDir, "com/example/b").apply { mkdirs() }
        File(testSrcDir, "AlphaTest.kt").writeText("package com.example\nclass AlphaTest")
        File(testBetaDir, "BetaTest.kt").writeText("package com.example.b\nclass BetaTest")

        val moduleModel =
            ModuleModel(
                path = ":discovery-module",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            name = "main",
                            kind = SourceSetKind.KOTLIN_JVM,
                            production = true,
                            srcDirs = listOf("src/main/kotlin"),
                        ),
                        SourceSetModel(
                            name = "test",
                            kind = SourceSetKind.KOTLIN_JVM,
                            production = false,
                            srcDirs = listOf("src/test/kotlin"),
                        ),
                    ),
                dependencies = emptyList(),
            )

        val layoutModel =
            LayoutModel(
                schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
                builds = listOf(BuildModel(id = ":", modules = listOf(moduleModel))),
            )

        val graph =
            ProjectGraphLoader.loadFromStream(
                ByteArrayInputStream(json.encodeToString(layoutModel).toByteArray()),
            )

        val prodModule = graph.getAllModules().single { it.path == ":discovery-module" }
        assertNotNull(prodModule)
        val mainFileNames =
            prodModule.files.filter {
                    file ->
                file.sourceSets.any { it.name == "main" }
            }.map { it.name }.toSet()
        assertEquals(setOf("Alpha.kt", "Beta.kt", "Script.kts"), mainFileNames)

        val testFileNames =
            prodModule.files.filter {
                    file ->
                file.sourceSets.any { it.name == "test" }
            }.map { it.name }.toSet()
        assertEquals(setOf("AlphaTest.kt", "BetaTest.kt"), testFileNames)

        val allFileNames = prodModule.files.map { it.name }.toSet()
        assertEquals(setOf("Alpha.kt", "Beta.kt", "Script.kts", "AlphaTest.kt", "BetaTest.kt"), allFileNames)
    }
}
