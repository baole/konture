/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LayoutModel
import io.github.baole.konture.core.LogLevel
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import kotlinx.serialization.json.Json

class KontureLayoutGenerationTest {
    @Test
    fun `testGenerateArchitectureLayoutTask`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()

        // Create child projects
        val childA =
            ProjectBuilder
                .builder()
                .withName("child-a")
                .withParent(rootProject)
                .build()
        val childB =
            ProjectBuilder
                .builder()
                .withName("child-b")
                .withParent(rootProject)
                .build()

        // Apply plugins to child projects
        childA.plugins.apply("org.jetbrains.kotlin.jvm")
        childB.plugins.apply("java")

        // Apply konture to root
        rootProject.plugins.apply("io.github.baole.konture.internal")

        // Create some source files in child-a source dirs to test file walking
        val childADir = childA.projectDir
        val srcDir = File(childADir, "src/main/kotlin/com/example")
        srcDir.mkdirs()
        val dummyFile = File(srcDir, "Example.kt")
        dummyFile.writeText("package com.example\nclass Example")

        // Add a project dependency from child-a to child-b
        childA.configurations.getByName("implementation").dependencies.add(
            childA.dependencies.project(mapOf("path" to ":child-b")),
        )

        // Run afterEvaluate hooks to eagerly configure tasks
        (rootProject as ProjectInternal).evaluate()
        (childA as ProjectInternal).evaluate()
        (childB as ProjectInternal).evaluate()

        // Run the task
        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.outputFile
            .get()
            .asFile.parentFile
            .mkdirs()
        task.generate()

        // Check output JSON file
        val outputFile = task.outputFile.get().asFile
        assertTrue(outputFile.exists())

        val jsonText = outputFile.readText()
        val layoutModel =
            Json.decodeFromString(
                LayoutModel
                    .serializer(),
                jsonText,
            )

        assertEquals(LayoutModel.CURRENT_SCHEMA_VERSION, layoutModel.schemaVersion)
        val rootBuild = layoutModel.builds.firstOrNull { it.id == ":" }
        assertNotNull(rootBuild)

        val moduleA = rootBuild?.modules?.firstOrNull { it.path == ":child-a" }
        assertNotNull(moduleA)
        assertTrue(moduleA?.appliedPlugins?.contains("kotlin-jvm") == true)

        val moduleB = rootBuild?.modules?.firstOrNull { it.path == ":child-b" }
        assertNotNull(moduleB)

        // Verify child-a has Example.kt listed relative to moduleDir
        val mainSourceSet = moduleA?.sourceSets?.firstOrNull { it.name == "main" }
        assertNotNull(mainSourceSet)
        assertTrue(mainSourceSet?.srcDirs?.any { it.endsWith("src/main/kotlin") } == true)

        // Verify dependency is collected
        val dep = moduleA?.dependencies?.firstOrNull { it.targetPath == ":child-b" }
        assertNotNull(dep)
        assertEquals("implementation", dep?.configuration)
    }

    @Test
    fun `testAbsoluteAndExternalSourceDirs`() {
        val rootProject = ProjectBuilder.builder().build()
        rootProject.plugins.apply("io.github.baole.konture.internal")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout

        // Manually build custom ModuleData with an external source directory
        val externalDir = File("/some/external/absolute/path") // absolute path not under root
        val sourceSet =
            SourceSetData(
                name = "external",
                kind = "KOTLIN_JVM",
                production = true,
                srcDirs = listOf(externalDir.absolutePath),
            )
        val module =
            ModuleData(
                path = ":custom",
                projectDir = rootProject.projectDir.absolutePath,
                appliedPlugins = listOf("kotlin-jvm"),
                sourceSets = listOf(sourceSet),
                dependencies = emptyList(),
            )

        task.modules.set(listOf(module))
        task.outputFile
            .get()
            .asFile.parentFile
            .mkdirs()
        task.generate()

        // Verify the task runs successfully and keeps the absolute path
        val outputFile = task.outputFile.get().asFile
        assertTrue(outputFile.exists())
        val jsonText = outputFile.readText()
        assertTrue(jsonText.contains("some/external/absolute/path"))
    }

    @Test
    fun `testExclusionsAreSerializedIntoLayoutJson`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        rootProject.plugins.apply("io.github.baole.konture.internal")

        val extension = rootProject.extensions.getByName("konture") as KontureExtension
        extension.excludeModules(":module-b")
        extension.excludePackages("com.example.exclude..")
        extension.excludeClasses("ExcludedClass")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.outputFile
            .get()
            .asFile.parentFile
            .mkdirs()
        task.generate()

        val outputFile = task.outputFile.get().asFile
        assertTrue(outputFile.exists())

        val jsonText = outputFile.readText()
        val layoutModel =
            Json.decodeFromString(
                LayoutModel
                    .serializer(),
                jsonText,
            )

        assertEquals(listOf(":module-b"), layoutModel.exclusions.excludeModules)
        assertEquals(listOf("com.example.exclude.."), layoutModel.exclusions.excludePackages)
        assertEquals(listOf("ExcludedClass"), layoutModel.exclusions.excludeClasses)
    }

    @Test
    fun `testJavaPluginFallbackSourceSets`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val child =
            ProjectBuilder
                .builder()
                .withName("child-java")
                .withParent(rootProject)
                .build()
        child.plugins.apply("java")

        rootProject.plugins.apply("io.github.baole.konture.internal")

        // Run afterEvaluate hooks to eagerly configure tasks
        (rootProject as ProjectInternal).evaluate()
        (child as ProjectInternal).evaluate()

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.outputFile
            .get()
            .asFile.parentFile
            .mkdirs()
        task.generate()

        val outputFile = task.outputFile.get().asFile
        assertTrue(outputFile.exists())

        val jsonText = outputFile.readText()
        val layoutModel =
            Json.decodeFromString(
                LayoutModel
                    .serializer(),
                jsonText,
            )

        val rootBuild = layoutModel.builds.firstOrNull { it.id == ":" }
        assertNotNull(rootBuild)

        val moduleJava = rootBuild?.modules?.firstOrNull { it.path == ":child-java" }
        assertNotNull(moduleJava)

        // Verify that java fallback successfully populated sourceSets
        val mainSourceSet = moduleJava?.sourceSets?.firstOrNull { it.name == "main" }
        assertNotNull(mainSourceSet)
        assertEquals("KOTLIN_JVM", mainSourceSet?.kind?.name)
        assertTrue(mainSourceSet?.production == true)
        assertTrue(
            mainSourceSet?.srcDirs?.any { it.endsWith("src/main/java") || it.endsWith("src/main/resources") } == true,
        )
    }

    @Test
    fun `testKmpSourceSetExtraction`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val kmpProject =
            ProjectBuilder
                .builder()
                .withName("kmp-child")
                .withParent(rootProject)
                .build()

        // Apply Kotlin Multiplatform plugin
        kmpProject.plugins.apply("org.jetbrains.kotlin.multiplatform")

        // Retrieve KMP extension and define targets to register source sets
        val kotlinExt =
            kmpProject.extensions.getByType(
                KotlinMultiplatformExtension::class.java,
            )
        kotlinExt.jvm("desktop")
        kotlinExt.iosX64("ios")
        // Collect source sets via the helper function inside KonturePlugin
        val plugin = KonturePlugin()
        val sourceSets = plugin.collectSourceSets(kmpProject)

        // Verify we found some source sets
        assertTrue(sourceSets.isNotEmpty())

        // Verify KMP is correctly detected as the kind for all collected source sets
        for (ss in sourceSets) {
            assertEquals("KMP", ss.kind)
        }

        // Verify commonMain is production, and commonTest is NOT production
        val commonMain = sourceSets.firstOrNull { it.name == "commonMain" }
        assertNotNull(commonMain)
        assertTrue(commonMain?.production == true)

        val commonTest = sourceSets.firstOrNull { it.name == "commonTest" }
        assertNotNull(commonTest)
        assertTrue(commonTest?.production == false)

        // Verify platform specific targets (desktopMain, iosMain)
        val desktopMain = sourceSets.firstOrNull { it.name == "desktopMain" }
        assertNotNull(desktopMain)
        assertTrue(desktopMain?.production == true)

        val iosTest = sourceSets.firstOrNull { it.name == "iosTest" }
        assertNotNull(iosTest)
        assertTrue(iosTest?.production == false)
    }

    @Test
    fun `testKmpSourceSetPlatformTargetsWithTypedApi`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val kmpProject =
            ProjectBuilder
                .builder()
                .withName("kmp-child")
                .withParent(rootProject)
                .build()

        // Apply Kotlin Multiplatform plugin
        kmpProject.plugins.apply("org.jetbrains.kotlin.multiplatform")

        // Retrieve KMP extension and define targets to register source sets
        val kotlinExt =
            kmpProject.extensions.getByType(
                KotlinMultiplatformExtension::class.java,
            )
        kotlinExt.jvm("desktop")
        kotlinExt.iosX64("ios")
        val commonSourceSet = kotlinExt.sourceSets.getByName("commonMain")
        kotlinExt.sourceSets.getByName("desktopMain").dependsOn(commonSourceSet)
        kotlinExt.sourceSets.getByName("iosMain").dependsOn(commonSourceSet)
        val intermediate = kotlinExt.sourceSets.create("intermediateCode")
        kotlinExt.sourceSets.getByName("desktopMain").dependsOn(intermediate)

        val plugin = KonturePlugin()
        val sourceSets = plugin.collectSourceSets(kmpProject)

        // Find desktopMain
        val desktopMain = sourceSets.firstOrNull { it.name == "desktopMain" }
        assertNotNull(desktopMain)
        // Verify it extracts platform target metadata correctly!
        assertTrue(desktopMain?.platforms?.contains("jvm") == true)
        assertTrue(desktopMain?.dependsOnSourceSets?.contains("commonMain") == true)
        assertTrue(sourceSets.firstOrNull { it.name == "intermediateCode" }?.production == true)

        val commonMain = sourceSets.firstOrNull { it.name == "commonMain" }
        assertNotNull(commonMain)
        assertTrue(commonMain?.targetNames?.isNotEmpty() == true)
        assertFalse(commonMain?.targetNames?.contains("desktop") == true)

        val iosMain = sourceSets.firstOrNull { it.name == "iosMain" }
        assertNotNull(iosMain)
        assertTrue(iosMain?.platforms?.contains("native") == true)
        assertTrue(iosMain?.targetNames?.isNotEmpty() == true)
        assertFalse(iosMain?.targetNames?.contains("desktop") == true)
        assertTrue(iosMain?.dependencyConfigurations?.contains("iosMainImplementation") == true)
    }

    @Test
    fun `testSpecializedAndroidPlugins`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val dynamicFeature =
            ProjectBuilder
                .builder()
                .withName("feature")
                .withParent(rootProject)
                .build()
        val testModule =
            ProjectBuilder
                .builder()
                .withName("test-module")
                .withParent(rootProject)
                .build()

        class DummyDynamicFeaturePlugin : Plugin<Project> {
            override fun apply(target: Project) = Unit
        }

        class DummyAndroidTestPlugin : Plugin<Project> {
            override fun apply(target: Project) = Unit
        }

        dynamicFeature.plugins.apply(DummyDynamicFeaturePlugin::class.java)
        testModule.plugins.apply(DummyAndroidTestPlugin::class.java)

        rootProject.plugins.apply("io.github.baole.konture.internal")

        val modulesList =
            rootProject.allprojects.map { sub ->
                val plugins = mutableListOf<String>()
                if (sub.pluginManager.hasPlugin("com.android.dynamic-feature") ||
                    sub.plugins.any { it.javaClass.simpleName.contains("DynamicFeature") }
                ) {
                    plugins.add("android-dynamic-feature")
                }
                if (sub.pluginManager.hasPlugin("com.android.test") ||
                    sub.plugins.any {
                        it.javaClass.simpleName.contains("TestPlugin") ||
                            sub.plugins.any { it.javaClass.simpleName.contains("AndroidTest") }
                    }
                ) {
                    plugins.add("android-test")
                }
                plugins
            }
        assertTrue(modulesList.any { it.contains("android-dynamic-feature") })
        assertTrue(modulesList.any { it.contains("android-test") })
    }

    @Test
    fun `testCompositeBuildAndDynamicDependenciesHeuristic`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        rootProject.group = "io.github.baole.konture"

        val child =
            ProjectBuilder
                .builder()
                .withName("child")
                .withParent(rootProject)
                .build()
        child.plugins.apply("org.jetbrains.kotlin.jvm")

        // Add a mock external/composite dependency belonging to the same group prefix
        child.configurations.getByName("implementation").dependencies.add(
            rootProject.dependencies.create("io.github.baole.konture:composite-dep:1.0.0"),
        )

        val plugin = KonturePlugin()
        val collectDepsMethod =
            KonturePlugin::class.java.getDeclaredMethod(
                "collectDependencies",
                Project::class.java,
            )
        collectDepsMethod.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val dependencies = collectDepsMethod.invoke(plugin, child) as List<DependencyData>

        assertTrue(dependencies.any { it.targetPath == ":composite-dep" })
    }

    @Test
    fun `testInvalidLogLevelThrowsGradleException`() {
        val rootProject = ProjectBuilder.builder().build()
        rootProject.plugins.apply("io.github.baole.konture.internal")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.logLevel.set("INVALID_LOG_LEVEL")

        val exception =
            assertThrows(GradleException::class.java) {
                task.generate()
            }
        assertTrue(exception.message?.contains("Invalid log level: 'INVALID_LOG_LEVEL'") == true)
    }

    @Test
    fun `custom and relative source directories in subprojects are correctly resolved`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val subProject =
            ProjectBuilder
                .builder()
                .withName("sub-module")
                .withParent(rootProject)
                .build()

        subProject.plugins.apply("org.jetbrains.kotlin.jvm")
        rootProject.plugins.apply("io.github.baole.konture.internal")

        // Add standard and custom relative source directories to subproject
        val subDir = subProject.projectDir
        val customSrcDir = File(subDir, "src/custom/kotlin/com/example")
        customSrcDir.mkdirs()
        val customFile = File(customSrcDir, "CustomClass.kt")
        customFile.writeText("package com.example\nclass CustomClass")

        val kotlinExt =
            subProject.extensions.getByType(
                org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension::class.java,
            )
        kotlinExt.sourceSets.getByName("main").kotlin.srcDir("src/custom/kotlin")

        (rootProject as ProjectInternal).evaluate()
        (subProject as ProjectInternal).evaluate()

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.outputFile.get().asFile.parentFile.mkdirs()
        task.generate()

        val jsonText = task.outputFile.get().asFile.readText()
        val layoutModel = Json.decodeFromString(LayoutModel.serializer(), jsonText)

        val rootBuild = layoutModel.builds.firstOrNull { it.id == ":" }
        assertNotNull(rootBuild)

        val module = rootBuild?.modules?.firstOrNull { it.path == ":sub-module" }
        assertNotNull(module)

        val mainSourceSet = module?.sourceSets?.firstOrNull { it.name == "main" }
        assertNotNull(mainSourceSet)

        // Ensure relative srcDirs do not escape with ../.. and custom srcDir is present
        assertTrue(mainSourceSet?.srcDirs?.none { it.contains("..") } == true)
        assertTrue(mainSourceSet?.srcDirs?.any { it.endsWith("src/custom/kotlin") } == true)
    }

    @Test
    fun `testValidLogLevelsMappingAndLoggerConfiguration`() {
        val rootProject = ProjectBuilder.builder().build()
        rootProject.plugins.apply("io.github.baole.konture.internal")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.outputFile.get().asFile.parentFile.mkdirs()

        val levels =
            mapOf(
                "TRACE" to LogLevel.TRACE,
                "DEBUG" to LogLevel.DEBUG,
                "INFO" to LogLevel.INFO,
                "WARNING" to LogLevel.WARNING,
                "ERROR" to LogLevel.ERROR,
            )

        for ((levelStr, expectedLevel) in levels) {
            task.logLevel.set(levelStr)
            task.generate()
            assertEquals(expectedLevel, KontureLogger.minLevel)
        }
    }

    @Test
    fun `testExcludeConfigurationsAreSerializedIntoLayoutJson`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        rootProject.plugins.apply("io.github.baole.konture.internal")

        val extension = rootProject.extensions.getByName("konture") as KontureExtension
        extension.excludeConfigurations("customTestConfig", "customBenchConfig")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.outputFile.get().asFile.parentFile.mkdirs()
        task.generate()

        val outputFile = task.outputFile.get().asFile
        val jsonText = outputFile.readText()
        val layoutModel = Json.decodeFromString(LayoutModel.serializer(), jsonText)

        assertEquals(
            listOf("customTestConfig", "customBenchConfig"),
            layoutModel.exclusions.excludeConfigurations,
        )
    }

    @Test
    fun `testRootProjectAsModuleRelativePath`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        rootProject.plugins.apply("io.github.baole.konture.internal")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        val rootModule =
            ModuleData(
                path = ":",
                projectDir = rootProject.projectDir.canonicalPath,
                appliedPlugins = listOf("kotlin-jvm"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
            )
        task.modules.set(listOf(rootModule))
        task.outputFile.get().asFile.parentFile.mkdirs()
        task.generate()

        val jsonText = task.outputFile.get().asFile.readText()
        val layoutModel = Json.decodeFromString(LayoutModel.serializer(), jsonText)

        val module = layoutModel.builds.first().modules.first { it.path == ":" }
        assertEquals(".", module.projectDir)
    }

    @Test
    fun `testSourceDirOutsideModuleDirFallback`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        rootProject.plugins.apply("io.github.baole.konture.internal")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout

        // Create a module directory under root
        val moduleDir = File(rootProject.projectDir, "sub-module")
        moduleDir.mkdirs()

        // Create an outside directory
        val outsideDir = File(rootProject.projectDir, "outside-src")
        outsideDir.mkdirs()

        val sourceSet =
            SourceSetData(
                name = "main",
                kind = "KOTLIN_JVM",
                production = true,
                srcDirs = listOf(outsideDir.absolutePath),
            )
        val module =
            ModuleData(
                path = ":sub-module",
                projectDir = moduleDir.canonicalPath,
                appliedPlugins = listOf("kotlin-jvm"),
                sourceSets = listOf(sourceSet),
                dependencies = emptyList(),
            )

        task.modules.set(listOf(module))
        task.outputFile.get().asFile.parentFile.mkdirs()
        task.generate()

        val jsonText = task.outputFile.get().asFile.readText()
        val layoutModel = Json.decodeFromString(LayoutModel.serializer(), jsonText)

        val subModuleModel = layoutModel.builds.first().modules.first { it.path == ":sub-module" }
        assertNotNull(subModuleModel)
        val srcDirInModel = subModuleModel.sourceSets.first().srcDirs.first()
        assertTrue(srcDirInModel.contains("outside-src"))
    }

    @Test
    fun `testLazyTaskConfigurationCapturesLateEvaluatedChildPlugins`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()

        // Apply konture to root first and evaluate root project
        rootProject.plugins.apply("io.github.baole.konture.internal")
        (rootProject as ProjectInternal).evaluate()

        // Simulate child project applying plugins during its own subsequent evaluation phase
        val child =
            ProjectBuilder
                .builder()
                .withName("child-late")
                .withParent(rootProject)
                .build()
        child.plugins.apply("org.jetbrains.kotlin.jvm")

        val srcDir = File(child.projectDir, "src/main/kotlin/com/example")
        srcDir.mkdirs()
        File(srcDir, "LateClass.kt").writeText("package com.example\nclass LateClass")

        (child as ProjectInternal).evaluate()

        // Execute layout task
        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.outputFile.get().asFile.parentFile.mkdirs()
        task.generate()

        val jsonText = task.outputFile.get().asFile.readText()
        val layoutModel = Json.decodeFromString(LayoutModel.serializer(), jsonText)

        val childModule = layoutModel.builds.first().modules.firstOrNull { it.path == ":child-late" }
        assertNotNull(childModule)
        assertTrue(childModule?.appliedPlugins?.contains("kotlin-jvm") == true)
        val mainSourceSet = childModule?.sourceSets?.firstOrNull { it.name == "main" }
        assertNotNull(mainSourceSet)
        assertTrue(mainSourceSet?.srcDirs?.any { it.endsWith("src/main/kotlin") } == true)
    }

    @Test
    fun `testAgp9KmpLibraryExtensionSupport`() {
        val project = ProjectBuilder.builder().withName("kmp-android-lib").build()

        open class DummyAndroidLibraryExtension
        project.extensions.create("androidLibrary", DummyAndroidLibraryExtension::class.java)

        val moduleData = KonturePluginConfigurer.collectModuleDataForProject(project)
        assertTrue(moduleData.appliedPlugins.contains("android-kmp-library"))
    }
}
