/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.KontureScopeTestFixture
import io.github.baole.konture.core.BuildModel
import io.github.baole.konture.core.DependencyEdge
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

internal class ProjectGraphLoaderKmpTestSourceSetTest : KontureScopeTestFixture() {
    @TempDir
    lateinit var tempDir: File
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `KMP commonTest resolves same-package types and supertypes from commonMain without import`() {
        val moduleDir = File(tempDir, "issue-115").apply { mkdirs() }
        val commonMainDir = File(moduleDir, "commonMain").apply { mkdirs() }
        val commonTestDir = File(moduleDir, "commonTest").apply { mkdirs() }

        File(commonMainDir, "DeviceIdGenerator.kt").apply {
            writeText(
                """
                package com.example.device

                interface DeviceIdGenerator
                typealias GeneratorAlias = DeviceIdGenerator
                """.trimIndent(),
            )
        }
        File(commonTestDir, "DeviceIdGeneratorFake.kt").apply {
            writeText(
                """
                package com.example.device

                class DeviceIdGeneratorFake : DeviceIdGenerator
                class DeviceIdGeneratorAliasFake : GeneratorAlias
                class DeviceIdConsumer {
                    fun create(): DeviceIdGenerator = DeviceIdGeneratorFake()
                }
                """.trimIndent(),
            )
        }

        val module =
            ModuleModel(
                path = ":issue-115",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(commonMainDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                        ),
                        SourceSetModel(
                            "commonTest",
                            SourceSetKind.KMP,
                            false,
                            listOf(commonTestDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                            dependsOnSourceSets = emptyList(),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(module))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val testFile = graph.getAllModules().single().files.single { it.name == "DeviceIdGeneratorFake.kt" }

        val fakeClass = testFile.classes.single { it.name == "DeviceIdGeneratorFake" }
        assertEquals(listOf("com.example.device.DeviceIdGenerator"), fakeClass.supertypes)

        val aliasFakeClass = testFile.classes.single { it.name == "DeviceIdGeneratorAliasFake" }
        assertEquals(listOf("com.example.device.DeviceIdGenerator"), aliasFakeClass.supertypes)

        val consumerClass = testFile.classes.single { it.name == "DeviceIdConsumer" }
        assertEquals("com.example.device.DeviceIdGenerator", consumerClass.functions.single().resolvedReturnType)

        assertTrue(testFile.usages.any { it.targetFqName == "com.example.device.DeviceIdGenerator" })
    }

    @Test
    fun `KMP platform test source set resolves same-package types from its platform main and commonMain`() {
        val moduleDir = File(tempDir, "platform-test-kmp").apply { mkdirs() }
        val commonMainDir = File(moduleDir, "commonMain").apply { mkdirs() }
        val jvmMainDir = File(moduleDir, "jvmMain").apply { mkdirs() }
        val jvmTestDir = File(moduleDir, "jvmTest").apply { mkdirs() }

        File(commonMainDir, "CommonInterface.kt").apply {
            writeText("package sample\ninterface CommonInterface")
        }
        File(jvmMainDir, "JvmBase.kt").apply {
            writeText("package sample\nopen class JvmBase")
        }
        File(jvmTestDir, "JvmTestClass.kt").apply {
            writeText("package sample\nclass JvmTestClass : JvmBase(), CommonInterface")
        }

        val module =
            ModuleModel(
                path = ":platform-test-kmp",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(commonMainDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                        ),
                        SourceSetModel(
                            "jvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(jvmMainDir.absolutePath),
                            platforms = listOf("jvm"),
                            dependsOnSourceSets = listOf("commonMain"),
                        ),
                        SourceSetModel(
                            "commonTest",
                            SourceSetKind.KMP,
                            false,
                            emptyList(),
                            platforms = listOf("jvm", "native"),
                        ),
                        SourceSetModel(
                            "jvmTest",
                            SourceSetKind.KMP,
                            false,
                            listOf(jvmTestDir.absolutePath),
                            platforms = listOf("jvm"),
                            dependsOnSourceSets = listOf("commonTest"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(module))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val testClass = graph.getAllModules().single().files.single { it.name == "JvmTestClass.kt" }.classes.single()

        assertEquals(listOf("sample.JvmBase", "sample.CommonInterface"), testClass.supertypes)
    }

    @Test
    fun `KMP test source set cannot resolve types from incompatible platform main`() {
        val moduleDir = File(tempDir, "incompatible-kmp").apply { mkdirs() }
        val commonMainDir = File(moduleDir, "commonMain").apply { mkdirs() }
        val jvmMainDir = File(moduleDir, "jvmMain").apply { mkdirs() }
        val commonTestDir = File(moduleDir, "commonTest").apply { mkdirs() }
        val iosTestDir = File(moduleDir, "iosTest").apply { mkdirs() }

        File(commonMainDir, "Shared.kt").apply { writeText("package sample\nclass Shared") }
        File(jvmMainDir, "JvmOnly.kt").apply { writeText("package sample\nclass JvmOnly") }
        File(commonTestDir, "CommonTestConsumer.kt").apply {
            writeText("package sample\nclass CommonTestConsumer { fun invalid(): JvmOnly = TODO() }")
        }
        File(iosTestDir, "IosTestConsumer.kt").apply {
            writeText("package sample\nclass IosTestConsumer { fun invalid(): JvmOnly = TODO() }")
        }

        val module =
            ModuleModel(
                path = ":incompatible-kmp",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(commonMainDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                        ),
                        SourceSetModel(
                            "jvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(jvmMainDir.absolutePath),
                            platforms = listOf("jvm"),
                            dependsOnSourceSets = listOf("commonMain"),
                        ),
                        SourceSetModel(
                            "commonTest",
                            SourceSetKind.KMP,
                            false,
                            listOf(commonTestDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                        ),
                        SourceSetModel(
                            "iosTest",
                            SourceSetKind.KMP,
                            false,
                            listOf(iosTestDir.absolutePath),
                            platforms = listOf("native"),
                            dependsOnSourceSets = listOf("commonTest"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(module))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val commonConsumer = graph.getAllModules().single().files.single { it.name == "CommonTestConsumer.kt" }.classes.single()
        val iosConsumer = graph.getAllModules().single().files.single { it.name == "IosTestConsumer.kt" }.classes.single()

        assertEquals(null, commonConsumer.functions.single().resolvedReturnType)
        assertEquals(null, iosConsumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP test source sets for unrelated same-platform targets are isolated`() {
        val moduleDir = File(tempDir, "multi-jvm-kmp").apply { mkdirs() }
        val desktopMainDir = File(moduleDir, "desktopMain").apply { mkdirs() }
        val desktopTestDir = File(moduleDir, "desktopTest").apply { mkdirs() }
        val serverMainDir = File(moduleDir, "serverMain").apply { mkdirs() }

        File(desktopMainDir, "DesktopOnly.kt").apply { writeText("package sample\nclass DesktopOnly") }
        File(serverMainDir, "ServerOnly.kt").apply { writeText("package sample\nclass ServerOnly") }
        File(desktopTestDir, "DesktopTestConsumer.kt").apply {
            writeText(
                "package sample\nclass DesktopTestConsumer { fun desktop(): DesktopOnly = TODO(); fun invalid(): ServerOnly = TODO() }",
            )
        }

        val module =
            ModuleModel(
                path = ":multi-jvm-kmp",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "desktopMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(desktopMainDir.absolutePath),
                            platforms = listOf("jvm"),
                        ),
                        SourceSetModel(
                            "serverMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(serverMainDir.absolutePath),
                            platforms = listOf("jvm"),
                        ),
                        SourceSetModel(
                            "desktopTest",
                            SourceSetKind.KMP,
                            false,
                            listOf(desktopTestDir.absolutePath),
                            platforms = listOf("jvm"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(module))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single().files.single { it.name == "DesktopTestConsumer.kt" }.classes.single()

        assertEquals("sample.DesktopOnly", consumer.functions.single { it.name == "desktop" }.resolvedReturnType)
        assertEquals(null, consumer.functions.single { it.name == "invalid" }.resolvedReturnType)
    }

    @Test
    fun `KMP commonTest resolves project dependencies declared on commonMainImplementation`() {
        val appDir = File(tempDir, "app").apply { mkdirs() }
        val appCommonMainDir = File(appDir, "commonMain").apply { mkdirs() }
        val appCommonTestDir = File(appDir, "commonTest").apply { mkdirs() }
        val libDir = File(tempDir, "lib").apply { mkdirs() }
        val libCommonMainDir = File(libDir, "commonMain").apply { mkdirs() }

        File(libCommonMainDir, "LibraryService.kt").apply {
            writeText("package lib\ninterface LibraryService")
        }
        File(appCommonMainDir, "AppService.kt").apply {
            writeText("package app\nclass AppService")
        }
        File(appCommonTestDir, "AppTest.kt").apply {
            writeText("package app\nimport lib.LibraryService\nclass AppTest { fun test(): LibraryService = TODO() }")
        }

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
                            listOf(appCommonMainDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                            dependencyConfigurations = listOf("commonMainImplementation"),
                        ),
                        SourceSetModel(
                            "commonTest",
                            SourceSetKind.KMP,
                            false,
                            listOf(appCommonTestDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                            dependsOnSourceSets = emptyList(),
                        ),
                    ),
                dependencies = listOf(DependencyEdge("commonMainImplementation", ":", ":lib")),
            )
        val lib =
            ModuleModel(
                path = ":lib",
                projectDir = libDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "commonMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(libCommonMainDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, lib))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val appTestClass = graph.getAllModules().single { it.path == ":app" }.files.single { it.name == "AppTest.kt" }.classes.single()

        assertEquals("lib.LibraryService", appTestClass.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP test source set isolates targets when one target name prefixes another`() {
        val moduleDir = File(tempDir, "prefix-kmp").apply { mkdirs() }
        val apiMainDir = File(moduleDir, "apiMain").apply { mkdirs() }
        val apiV2TestDir = File(moduleDir, "apiV2Test").apply { mkdirs() }

        File(apiMainDir, "ApiOnly.kt").apply { writeText("package sample\nclass ApiOnly") }
        File(apiV2TestDir, "ApiV2TestConsumer.kt").apply {
            writeText("package sample\nclass ApiV2TestConsumer { fun invalid(): ApiOnly = TODO() }")
        }

        val module =
            ModuleModel(
                path = ":prefix-kmp",
                projectDir = moduleDir.absolutePath,
                appliedPlugins = listOf("kotlin-multiplatform"),
                sourceSets =
                    listOf(
                        SourceSetModel(
                            "apiMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(apiMainDir.absolutePath),
                            platforms = listOf("jvm"),
                        ),
                        SourceSetModel(
                            "apiV2Test",
                            SourceSetKind.KMP,
                            false,
                            listOf(apiV2TestDir.absolutePath),
                            platforms = listOf("jvm"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout = LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(module))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single().files.single { it.name == "ApiV2TestConsumer.kt" }.classes.single()

        assertEquals(null, consumer.functions.single().resolvedReturnType)
    }
}

