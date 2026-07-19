/*
 * Copyright 2026 Bao Le Duc
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

@Suppress("LargeClass")
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
        val helperDir = File(tempDir, "helper").apply { mkdirs() }
        val mainFile =
            File(appDir, "Consumer.kt").apply {
                writeText(
                    """
                    package app

                    class Consumer {
                        fun load(): Result<String> = TODO()
                    }
                    """.trimIndent(),
                )
            }
        val testFile =
            File(appDir, "TestResult.kt").apply {
                writeText(
                    """
                    package app

                    class Result<T>
                    """.trimIndent(),
                )
            }
        val helperFile =
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
                        SourceSetModel("main", SourceSetKind.KOTLIN_JVM, true, listOf(appDir.absolutePath), listOf(mainFile.absolutePath)),
                        SourceSetModel("test", SourceSetKind.KOTLIN_JVM, false, listOf(appDir.absolutePath), listOf(testFile.absolutePath)),
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
                        SourceSetModel("main", SourceSetKind.KOTLIN_JVM, true, listOf(helperDir.absolutePath), listOf(helperFile.absolutePath)),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, helper))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single { it.name == "Consumer.kt" }.classes.single()

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
        val aliasFile = File(libraryDir, "PublicUser.kt").apply { writeText("package api\ntypealias PublicUser = User") }
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
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

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
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals("api.Api.User", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP platform source sets see common code but not incompatible platform code`() {
        val moduleDir = File(tempDir, "kmp").apply { mkdirs() }
        val commonFile = File(moduleDir, "Shared.kt").apply { writeText("package sample\nclass Shared") }
        val jvmFile =
            File(moduleDir, "JvmConsumer.kt").apply {
                writeText("package sample\nclass JvmConsumer { fun load(): Shared = TODO(); fun invalid(): IosOnly = TODO() }")
            }
        val iosFile = File(moduleDir, "IosOnly.kt").apply { writeText("package sample\nclass IosOnly") }
        val module =
            ModuleModel(
                path = ":kmp",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(moduleDir.absolutePath),
                            listOf(commonFile.absolutePath),
                            platforms = listOf("jvm", "native"),
                        ),
                        SourceSetModel(
                            "jvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(moduleDir.absolutePath),
                            listOf(jvmFile.absolutePath),
                            platforms = listOf("jvm"),
                            dependsOnSourceSets = listOf("commonMain"),
                        ),
                        SourceSetModel(
                            "iosMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(moduleDir.absolutePath),
                            listOf(iosFile.absolutePath),
                            platforms = listOf("native"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(module))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val jvmConsumer = graph.getAllModules().single().files.single { it.name == "JvmConsumer.kt" }.classes.single()

        assertEquals("sample.Shared", jvmConsumer.functions.single { it.name == "load" }.resolvedReturnType)
        assertEquals(null, jvmConsumer.functions.single { it.name == "invalid" }.resolvedReturnType)
    }

    @Test
    fun `KMP native targets do not resolve declarations from another native target`() {
        val moduleDir = File(tempDir, "native-kmp").apply { mkdirs() }
        val commonFile = File(moduleDir, "Shared.kt").apply { writeText("package sample\nclass Shared") }
        val iosFile =
            File(moduleDir, "IosConsumer.kt").apply {
                writeText("package sample\nclass IosConsumer { fun shared(): Shared = TODO(); fun invalid(): LinuxOnly = TODO() }")
            }
        val linuxFile = File(moduleDir, "LinuxOnly.kt").apply { writeText("package sample\nclass LinuxOnly") }
        val module =
            ModuleModel(
                path = ":native-kmp",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(moduleDir.absolutePath),
                            listOf(commonFile.absolutePath),
                            platforms = listOf("native", "iosArm64", "linuxX64"),
                        ),
                        SourceSetModel(
                            "iosMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(moduleDir.absolutePath),
                            listOf(iosFile.absolutePath),
                            platforms = listOf("native", "iosArm64"),
                            dependsOnSourceSets = listOf("commonMain"),
                        ),
                        SourceSetModel(
                            "linuxMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(moduleDir.absolutePath),
                            listOf(linuxFile.absolutePath),
                            platforms = listOf("native", "linuxX64"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(module))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val iosConsumer = graph.getAllModules().single().files.single { it.name == "IosConsumer.kt" }.classes.single()

        assertEquals("sample.Shared", iosConsumer.functions.single { it.name == "shared" }.resolvedReturnType)
        assertEquals(null, iosConsumer.functions.single { it.name == "invalid" }.resolvedReturnType)
    }

    @Test
    fun `KMP unrelated source sets for one JVM target are isolated`() {
        val moduleDir = File(tempDir, "jvm-kmp").apply { mkdirs() }
        val consumerFile =
            File(moduleDir, "FirstConsumer.kt").apply {
                writeText("package sample\nclass FirstConsumer { fun invalid(): SecondOnly = TODO() }")
            }
        val secondFile = File(moduleDir, "SecondOnly.kt").apply { writeText("package sample\nclass SecondOnly") }
        val module =
            ModuleModel(
                path = ":jvm-kmp",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "firstJvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(moduleDir.absolutePath),
                            listOf(consumerFile.absolutePath),
                        ),
                        SourceSetModel(
                            "secondJvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(moduleDir.absolutePath),
                            listOf(secondFile.absolutePath),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(module))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single().files.single { it.name == "FirstConsumer.kt" }.classes.single()

        assertEquals(null, consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP project dependencies do not require matching source set names`() {
        val appDir = File(tempDir, "kmp-app").apply { mkdirs() }
        val libraryDir = File(tempDir, "kmp-library").apply { mkdirs() }
        val consumerFile =
            File(appDir, "Consumer.kt").apply {
                writeText("package sample\nclass Consumer { fun load(): DesktopOnly = TODO() }")
            }
        val desktopFile = File(libraryDir, "DesktopOnly.kt").apply { writeText("package sample\nclass DesktopOnly") }
        val app =
            ModuleModel(
                path = ":app",
                projectDir = appDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "appJvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(appDir.absolutePath),
                            listOf(consumerFile.absolutePath),
                            platforms = listOf("jvm"),
                            dependencyConfigurations = listOf("appJvmMainImplementation"),
                        ),
                    ),
                dependencies = listOf(DependencyEdge("appJvmMainImplementation", ":", ":library")),
            )
        val library =
            ModuleModel(
                path = ":library",
                projectDir = libraryDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "desktopMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(libraryDir.absolutePath),
                            listOf(desktopFile.absolutePath),
                            platforms = listOf("jvm"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals("sample.DesktopOnly", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP common source set cannot resolve JVM-only dependency declarations`() {
        val appDir = File(tempDir, "common-app").apply { mkdirs() }
        val libraryDir = File(tempDir, "jvm-library").apply { mkdirs() }
        val consumerFile =
            File(appDir, "CommonConsumer.kt").apply {
                writeText("package sample\nclass CommonConsumer { fun invalid(): JvmOnly = TODO() }")
            }
        val jvmFile = File(libraryDir, "JvmOnly.kt").apply { writeText("package sample\nclass JvmOnly") }
        val app =
            ModuleModel(
                path = ":app",
                projectDir = appDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(appDir.absolutePath),
                            listOf(consumerFile.absolutePath),
                            platforms = listOf("jvm", "native"),
                            targetNames = listOf("IOS_ARM64"),
                            dependencyConfigurations = listOf("commonMainImplementation"),
                        ),
                    ),
                dependencies = listOf(DependencyEdge("commonMainImplementation", ":", ":library")),
            )
        val library =
            ModuleModel(
                path = ":library",
                projectDir = libraryDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "jvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(libraryDir.absolutePath),
                            listOf(jvmFile.absolutePath),
                            platforms = listOf("jvm"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals(null, consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP common source set resolves a dependency common to its native targets`() {
        val appDir = File(tempDir, "native-common-app").apply { mkdirs() }
        val libraryDir = File(tempDir, "native-common-library").apply { mkdirs() }
        val consumerFile =
            File(appDir, "CommonConsumer.kt").apply {
                writeText("package sample\nclass CommonConsumer { fun load(): Shared = TODO() }")
            }
        val sharedFile = File(libraryDir, "Shared.kt").apply { writeText("package sample\nclass Shared") }
        val app =
            ModuleModel(
                path = ":app",
                projectDir = appDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(appDir.absolutePath),
                            listOf(consumerFile.absolutePath),
                            platforms = listOf("jvm", "native"),
                            targetNames = listOf("IOS_ARM64"),
                            dependencyConfigurations = listOf("commonMainImplementation"),
                        ),
                    ),
                dependencies = listOf(DependencyEdge("commonMainImplementation", ":", ":library")),
            )
        val library =
            ModuleModel(
                path = ":library",
                projectDir = libraryDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(libraryDir.absolutePath),
                            listOf(sharedFile.absolutePath),
                            platforms = listOf("jvm", "native"),
                            targetNames = listOf("IOS_ARM64"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals("sample.Shared", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP source set dependency is visible only to its owning compilation`() {
        val appDir = File(tempDir, "scoped-dependency-app").apply { mkdirs() }
        val libraryDir = File(tempDir, "scoped-dependency-library").apply { mkdirs() }
        val jvmFile =
            File(appDir, "JvmConsumer.kt").apply {
                writeText("package sample\nclass JvmConsumer { fun invalid(): LibraryCommon = TODO() }")
            }
        val iosFile =
            File(appDir, "IosConsumer.kt").apply {
                writeText("package sample\nclass IosConsumer { fun load(): LibraryCommon = TODO() }")
            }
        val libraryFile = File(libraryDir, "LibraryCommon.kt").apply { writeText("package sample\nclass LibraryCommon") }
        val app =
            ModuleModel(
                path = ":app",
                projectDir = appDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(appDir.absolutePath),
                            emptyList(),
                            platforms = listOf("jvm", "native"),
                            targetNames = listOf("IOS_ARM64"),
                            dependencyConfigurations = listOf("commonMainImplementation"),
                        ),
                        SourceSetModel(
                            "jvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(appDir.absolutePath),
                            listOf(jvmFile.absolutePath),
                            platforms = listOf("jvm"),
                            dependsOnSourceSets = listOf("commonMain"),
                            dependencyConfigurations = listOf("jvmMainImplementation"),
                        ),
                        SourceSetModel(
                            "iosMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(appDir.absolutePath),
                            listOf(iosFile.absolutePath),
                            platforms = listOf("native"),
                            targetNames = listOf("IOS_ARM64"),
                            dependsOnSourceSets = listOf("commonMain"),
                            dependencyConfigurations = listOf("iosMainImplementation"),
                        ),
                    ),
                dependencies = listOf(DependencyEdge("iosMainImplementation", ":", ":library")),
            )
        val library =
            ModuleModel(
                path = ":library",
                projectDir = libraryDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(libraryDir.absolutePath),
                            listOf(libraryFile.absolutePath),
                            platforms = listOf("jvm", "native"),
                            targetNames = listOf("IOS_ARM64"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val appModule = graph.getAllModules().single { it.path == ":app" }
        val jvmConsumer = appModule.files.single { it.name == "JvmConsumer.kt" }.classes.single()
        val iosConsumer = appModule.files.single { it.name == "IosConsumer.kt" }.classes.single()

        assertEquals(null, jvmConsumer.functions.single().resolvedReturnType)
        assertEquals("sample.LibraryCommon", iosConsumer.functions.single().resolvedReturnType)
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
        val layoutModel = LayoutModel(schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(buildModel))
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
