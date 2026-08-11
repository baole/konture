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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File

internal class ProjectGraphLoaderKmpTest : KontureScopeTestFixture() {
    @TempDir
    lateinit var tempDir: File
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `KMP platform source sets see common code but not incompatible platform code`() {
        val moduleDir = File(tempDir, "kmp").apply { mkdirs() }
        val commonDir = File(moduleDir, "commonMain").apply { mkdirs() }
        val jvmDir = File(moduleDir, "jvmMain").apply { mkdirs() }
        val iosDir = File(moduleDir, "iosMain").apply { mkdirs() }
        File(commonDir, "Shared.kt").apply { writeText("package sample\nclass Shared") }
        File(jvmDir, "JvmConsumer.kt").apply {
            writeText(
                "package sample\nclass JvmConsumer { fun load(): Shared = TODO(); fun invalid(): IosOnly = TODO() }",
            )
        }
        File(iosDir, "IosOnly.kt").apply { writeText("package sample\nclass IosOnly") }
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
                            listOf(commonDir.absolutePath),
                            platforms = listOf("jvm", "native"),
                        ),
                        SourceSetModel(
                            "jvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(jvmDir.absolutePath),
                            platforms = listOf("jvm"),
                            dependsOnSourceSets = listOf("commonMain"),
                        ),
                        SourceSetModel(
                            "iosMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(iosDir.absolutePath),
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
        val commonDir = File(moduleDir, "commonMain").apply { mkdirs() }
        val iosDir = File(moduleDir, "iosMain").apply { mkdirs() }
        val linuxDir = File(moduleDir, "linuxMain").apply { mkdirs() }
        File(commonDir, "Shared.kt").apply { writeText("package sample\nclass Shared") }
        File(iosDir, "IosConsumer.kt").apply {
            writeText(
                "package sample\nclass IosConsumer { fun shared(): Shared = TODO(); fun invalid(): LinuxOnly = TODO() }",
            )
        }
        File(linuxDir, "LinuxOnly.kt").apply { writeText("package sample\nclass LinuxOnly") }
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
                            listOf(commonDir.absolutePath),
                            platforms = listOf("native", "iosArm64", "linuxX64"),
                        ),
                        SourceSetModel(
                            "iosMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(iosDir.absolutePath),
                            platforms = listOf("native", "iosArm64"),
                            dependsOnSourceSets = listOf("commonMain"),
                        ),
                        SourceSetModel(
                            "linuxMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(linuxDir.absolutePath),
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
        val firstDir = File(moduleDir, "firstJvmMain").apply { mkdirs() }
        val secondDir = File(moduleDir, "secondJvmMain").apply { mkdirs() }
        File(firstDir, "FirstConsumer.kt").apply {
            writeText("package sample\nclass FirstConsumer { fun invalid(): SecondOnly = TODO() }")
        }
        File(secondDir, "SecondOnly.kt").apply { writeText("package sample\nclass SecondOnly") }
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
                            listOf(firstDir.absolutePath),
                        ),
                        SourceSetModel(
                            "secondJvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(secondDir.absolutePath),
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
        File(appDir, "Consumer.kt").apply {
            writeText("package sample\nclass Consumer { fun load(): DesktopOnly = TODO() }")
        }
        File(libraryDir, "DesktopOnly.kt").apply { writeText("package sample\nclass DesktopOnly") }
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
                            platforms = listOf("jvm"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout =
            LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals("sample.DesktopOnly", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP common source set cannot resolve JVM-only dependency declarations`() {
        val appDir = File(tempDir, "common-app").apply { mkdirs() }
        val libraryDir = File(tempDir, "jvm-library").apply { mkdirs() }
        File(appDir, "CommonConsumer.kt").apply {
            writeText("package sample\nclass CommonConsumer { fun invalid(): JvmOnly = TODO() }")
        }
        File(libraryDir, "JvmOnly.kt").apply { writeText("package sample\nclass JvmOnly") }
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
                            platforms = listOf("jvm"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout =
            LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals(null, consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP common source set resolves a dependency common to its native targets`() {
        val appDir = File(tempDir, "native-common-app").apply { mkdirs() }
        val libraryDir = File(tempDir, "native-common-library").apply { mkdirs() }
        File(appDir, "CommonConsumer.kt").apply {
            writeText("package sample\nclass CommonConsumer { fun load(): Shared = TODO() }")
        }
        File(libraryDir, "Shared.kt").apply { writeText("package sample\nclass Shared") }
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
                            platforms = listOf("jvm", "native"),
                            targetNames = listOf("IOS_ARM64"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout =
            LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val consumer = graph.getAllModules().single { it.path == ":app" }.files.single().classes.single()

        assertEquals("sample.Shared", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `KMP source set dependency is visible only to its owning compilation`() {
        val appDir = File(tempDir, "scoped-dependency-app").apply { mkdirs() }
        val jvmDir = File(appDir, "jvmMain").apply { mkdirs() }
        val iosDir = File(appDir, "iosMain").apply { mkdirs() }
        val libraryDir = File(tempDir, "scoped-dependency-library").apply { mkdirs() }
        File(jvmDir, "JvmConsumer.kt").apply {
            writeText("package sample\nclass JvmConsumer { fun invalid(): LibraryCommon = TODO() }")
        }
        File(iosDir, "IosConsumer.kt").apply {
            writeText("package sample\nclass IosConsumer { fun load(): LibraryCommon = TODO() }")
        }
        File(
            libraryDir,
            "LibraryCommon.kt",
        ).apply { writeText("package sample\nclass LibraryCommon") }
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
                            platforms = listOf("jvm", "native"),
                            targetNames = listOf("IOS_ARM64"),
                            dependencyConfigurations = listOf("commonMainImplementation"),
                        ),
                        SourceSetModel(
                            "jvmMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(jvmDir.absolutePath),
                            platforms = listOf("jvm"),
                            dependsOnSourceSets = listOf("commonMain"),
                            dependencyConfigurations = listOf("jvmMainImplementation"),
                        ),
                        SourceSetModel(
                            "iosMain",
                            SourceSetKind.KMP,
                            true,
                            listOf(iosDir.absolutePath),
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
                            platforms = listOf("jvm", "native"),
                            targetNames = listOf("IOS_ARM64"),
                        ),
                    ),
                dependencies = emptyList(),
            )
        val layout =
            LayoutModel(LayoutModel.CURRENT_SCHEMA_VERSION, builds = listOf(BuildModel(":", listOf(app, library))))

        val graph = ProjectGraphLoader.loadFromStream(ByteArrayInputStream(json.encodeToString(layout).toByteArray()))
        val appModule = graph.getAllModules().single { it.path == ":app" }
        val jvmConsumer = appModule.files.single { it.name == "JvmConsumer.kt" }.classes.single()
        val iosConsumer = appModule.files.single { it.name == "IosConsumer.kt" }.classes.single()

        assertEquals(null, jvmConsumer.functions.single().resolvedReturnType)
        assertEquals("sample.LibraryCommon", iosConsumer.functions.single().resolvedReturnType)
    }
}
