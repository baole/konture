/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CoreModelsTest {
    private val json = Json { prettyPrint = true }

    @Test
    fun testSourceSetKindEnum() {
        assertEquals(3, SourceSetKind.entries.size)
        assertEquals(SourceSetKind.KOTLIN_JVM, SourceSetKind.valueOf("KOTLIN_JVM"))
        assertEquals(SourceSetKind.ANDROID_VARIANT, SourceSetKind.valueOf("ANDROID_VARIANT"))
        assertEquals(SourceSetKind.KMP, SourceSetKind.valueOf("KMP"))
    }

    @Test
    fun testLogLevelEnum() {
        assertEquals(5, LogLevel.entries.size)
        assertEquals(LogLevel.TRACE, LogLevel.valueOf("TRACE"))
        assertEquals(LogLevel.DEBUG, LogLevel.valueOf("DEBUG"))
        assertEquals(LogLevel.INFO, LogLevel.valueOf("INFO"))
        assertEquals(LogLevel.WARNING, LogLevel.valueOf("WARNING"))
        assertEquals(LogLevel.ERROR, LogLevel.valueOf("ERROR"))
    }

    @Test
    fun testKontureConstants() {
        assertEquals("konture.baseline.path", KontureConstants.PROPERTY_BASELINE_PATH)
        assertEquals("konture.locale", KontureConstants.PROPERTY_LOCALE)
        assertEquals("konture.baseline.generate", KontureConstants.PROPERTY_BASELINE_GENERATE)
        assertEquals("konture.baseline.dir", KontureConstants.PROPERTY_BASELINE_DIR)
        assertEquals("konture-baseline.json", KontureConstants.DEFAULT_BASELINE_FILENAME)
    }

    @Test
    fun testExclusionsModelDefaultsAndSerialization() {
        val exclusions = ExclusionsModel()
        assertTrue(exclusions.excludeModules.isEmpty())
        assertTrue(exclusions.excludePackages.isEmpty())
        assertTrue(exclusions.excludeClasses.isEmpty())
        assertEquals(listOf("test*", "benchmark*", "profile", "testedapks"), exclusions.excludeConfigurations)

        val customExclusions =
            ExclusionsModel(
                excludeModules = listOf(":internal"),
                excludePackages = listOf("com.example.internal"),
                excludeClasses = listOf("com.example.InternalClass"),
                excludeConfigurations = listOf("custom"),
            )
        val encoded = json.encodeToString(customExclusions)
        val decoded: ExclusionsModel = json.decodeFromString(encoded)
        assertEquals(customExclusions, decoded)
    }

    @Test
    fun testDependencyEdgeSerialization() {
        val edge =
            DependencyEdge(
                configuration = "implementation",
                targetBuildId = ":",
                targetPath = ":core:model",
            )
        assertEquals("implementation", edge.configuration)
        assertEquals(":", edge.targetBuildId)
        assertEquals(":core:model", edge.targetPath)

        val encoded = json.encodeToString(edge)
        val decoded: DependencyEdge = json.decodeFromString(encoded)
        assertEquals(edge, decoded)
    }

    @Test
    fun testSourceSetModelSerialization() {
        val ss =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf("src/main/kotlin"),
                platforms = listOf("jvm"),
                targetNames = listOf("jvmTarget"),
                dependsOnSourceSets = listOf("commonMain"),
                dependencyConfigurations = listOf("implementation"),
                compileClasspath = listOf("lib.jar"),
                jvmTarget = "17",
            )
        assertEquals("main", ss.name)
        assertEquals(SourceSetKind.KOTLIN_JVM, ss.kind)
        assertTrue(ss.production)
        assertEquals("17", ss.jvmTarget)

        val encoded = json.encodeToString(ss)
        val decoded: SourceSetModel = json.decodeFromString(encoded)
        assertEquals(ss, decoded)
    }

    @Test
    fun testModuleModelAndBuildModelSerialization() {
        val ss =
            SourceSetModel(
                name = "main",
                kind = SourceSetKind.KOTLIN_JVM,
                production = true,
                srcDirs = listOf("src/main/kotlin"),
            )
        val edge = DependencyEdge("implementation", ":", ":core")
        val module =
            ModuleModel(
                path = ":app",
                projectDir = "/path/to/app",
                appliedPlugins = listOf("kotlin-jvm"),
                sourceSets = listOf(ss),
                dependencies = listOf(edge),
            )
        val build = BuildModel(id = ":", modules = listOf(module))

        assertEquals(":", build.id)
        assertEquals(1, build.modules.size)
        assertEquals(":app", build.modules[0].path)

        val encoded = json.encodeToString(build)
        val decoded: BuildModel = json.decodeFromString(encoded)
        assertEquals(build, decoded)
    }

    @Test
    fun testLayoutModelSerialization() {
        val build = BuildModel(id = ":", modules = emptyList())
        val layout =
            LayoutModel(
                schemaVersion = LayoutModel.CURRENT_SCHEMA_VERSION,
                builds = listOf(build),
                logLevel = "DEBUG",
            )
        assertEquals(2, LayoutModel.CURRENT_SCHEMA_VERSION)
        assertEquals(2, layout.schemaVersion)
        assertEquals("DEBUG", layout.logLevel)

        val encoded = json.encodeToString(layout)
        val decoded: LayoutModel = json.decodeFromString(encoded)
        assertEquals(layout, decoded)
    }

    @Test
    fun testResolvedDependencyModelAndDependencyGraphModel() {
        val dep =
            ResolvedDependencyModel(
                group = "org.jetbrains.kotlin",
                name = "kotlin-stdlib",
                version = "1.9.20",
                configuration = "runtimeClasspath",
                isTransitive = false,
            )
        assertEquals("org.jetbrains.kotlin", dep.group)
        assertEquals("kotlin-stdlib", dep.name)
        assertEquals("1.9.20", dep.version)
        assertEquals("runtimeClasspath", dep.configuration)
        assertFalse(dep.isTransitive)

        val graph =
            DependencyGraphModel(
                schemaVersion = 1,
                modules = mapOf(":app" to listOf(dep)),
            )
        assertEquals(1, graph.schemaVersion)
        assertEquals(1, graph.modules[":app"]?.size)

        val encoded = json.encodeToString(graph)
        val decoded: DependencyGraphModel = json.decodeFromString(encoded)
        assertEquals(graph, decoded)
    }
}
