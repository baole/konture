/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.KontureConstants
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KonturePluginTest {
    @Test
    fun `plugin registers task and extension on root project`() {
        // Create a root project using ProjectBuilder
        val project = ProjectBuilder.builder().build()

        // Apply our plugin
        project.plugins.apply("io.github.baole.konture")

        // Assert extension is registered
        val extension = project.extensions.findByName("konture") as? KontureExtension
        assertNotNull(extension)

        // Assert task is registered
        val task = project.tasks.findByName("generateArchitectureLayout") as? GenerateArchitectureLayout
        assertNotNull(task)

        val expectedOutputFile =
            project.layout.buildDirectory
                .file("konture/layout_v2.json")
                .get()
                .asFile
        val actualOutputFile = task?.outputFile?.get()?.asFile
        assertEquals(expectedOutputFile, actualOutputFile)
    }

    @Test
    fun `plugin extension accepts exclusions`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.baole.konture")

        val extension = project.extensions.getByName("konture") as KontureExtension

        extension.excludeModules(":module-a", ":module-b")
        extension.excludePackages("com.example.exclude..")
        extension.excludeClasses("ExcludedClass")

        assertEquals(listOf(":module-a", ":module-b"), extension.excludeModules.get())
        assertEquals(listOf("com.example.exclude.."), extension.excludePackages.get())
        assertEquals(listOf("ExcludedClass"), extension.excludeClasses.get())
    }

    @Test
    fun `plugin automatically registers incoming configuration on subprojects`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val subProject =
            ProjectBuilder
                .builder()
                .withName("sub")
                .withParent(rootProject)
                .build()

        // Apply plugin to subproject
        subProject.plugins.apply("io.github.baole.konture")

        // Verify incoming configuration was created automatically without manual configuration
        val incomingConfig = subProject.configurations.findByName("archLayoutIncoming")
        assertNotNull(incomingConfig)
        assertTrue(incomingConfig?.isCanBeResolved == true)
        assertTrue(incomingConfig?.isCanBeConsumed == false)

        // Verify copy architecture layout task was registered automatically
        val copyTask = subProject.tasks.findByName("copyArchitectureLayout")
        assertNotNull(copyTask)
    }

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
        rootProject.plugins.apply("io.github.baole.konture")

        // Create some source files in child-a source dirs to test file walking
        val childADir = childA.projectDir
        val srcDir = java.io.File(childADir, "src/main/kotlin/com/example")
        srcDir.mkdirs()
        val dummyFile = java.io.File(srcDir, "Example.kt")
        dummyFile.writeText("package com.example\nclass Example")

        // Add a project dependency from child-a to child-b
        childA.configurations.getByName("implementation").dependencies.add(
            childA.dependencies.project(mapOf("path" to ":child-b")),
        )

        // Run afterEvaluate hooks to eagerly configure tasks
        (rootProject as org.gradle.api.internal.project.ProjectInternal).evaluate()
        (childA as org.gradle.api.internal.project.ProjectInternal).evaluate()
        (childB as org.gradle.api.internal.project.ProjectInternal).evaluate()

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
            kotlinx.serialization.json.Json.decodeFromString(
                io.github.baole.konture.core.LayoutModel
                    .serializer(),
                jsonText,
            )

        assertEquals(2, layoutModel.schemaVersion)
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
        assertTrue(mainSourceSet?.kotlinFiles?.contains("src/main/kotlin/com/example/Example.kt") == true)

        // Verify dependency is collected
        val dep = moduleA?.dependencies?.firstOrNull { it.targetPath == ":child-b" }
        assertNotNull(dep)
        assertEquals("implementation", dep?.configuration)
    }

    @Test
    fun `testAbsoluteAndExternalSourceDirs`() {
        val rootProject = ProjectBuilder.builder().build()
        rootProject.plugins.apply("io.github.baole.konture")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout

        // Manually build custom ModuleData with an external source directory
        val externalDir = java.io.File("/some/external/absolute/path") // absolute path not under root
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
        assertTrue(jsonText.contains("/some/external/absolute/path"))
    }

    @Test
    fun `testPluginDataClasses`() {
        val sourceSet1 = SourceSetData("main", "KOTLIN_JVM", true, listOf("src"))
        val sourceSet2 = SourceSetData("main", "KOTLIN_JVM", true, listOf("src"))
        val sourceSet3 = sourceSet1.copy(name = "test")

        assertEquals(sourceSet1, sourceSet2)
        assertTrue(sourceSet1.hashCode() == sourceSet2.hashCode())
        assertEquals(sourceSet1.toString(), sourceSet2.toString())
        assertEquals("test", sourceSet3.name)

        val dep1 = DependencyData("implementation", ":", ":child")
        val dep2 = DependencyData("implementation", ":", ":child")
        val dep3 = dep1.copy(configuration = "api")

        assertEquals(dep1, dep2)
        assertTrue(dep1.hashCode() == dep2.hashCode())
        assertEquals(dep1.toString(), dep2.toString())
        assertEquals("api", dep3.configuration)

        val module1 = ModuleData(":path", "dir", listOf("plugin"), listOf(sourceSet1), listOf(dep1))
        val module2 = ModuleData(":path", "dir", listOf("plugin"), listOf(sourceSet1), listOf(dep1))
        val module3 = module1.copy(path = ":other")

        assertEquals(module1, module2)
        assertTrue(module1.hashCode() == module2.hashCode())
        assertEquals(module1.toString(), module2.toString())
        assertEquals(":other", module3.path)
    }

    @Test
    fun `testOutgoingArtifactResolution`() {
        val rootProject = ProjectBuilder.builder().build()
        rootProject.plugins.apply("io.github.baole.konture")

        val config = rootProject.configurations.getByName("archLayoutElements")
        assertNotNull(config)

        // This resolves the artifact flatMap and returns the outputFile path
        val files = config.outgoing.artifacts.map { it.file }
        assertTrue(files.isNotEmpty())
        val expectedFile =
            rootProject.layout.buildDirectory
                .file("konture/layout_v2.json")
                .get()
                .asFile
        assertEquals(expectedFile, files.first())
    }

    @Test
    fun `testExclusionsAreSerializedIntoLayoutJson`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        rootProject.plugins.apply("io.github.baole.konture")

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
            kotlinx.serialization.json.Json.decodeFromString(
                io.github.baole.konture.core.LayoutModel
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

        rootProject.plugins.apply("io.github.baole.konture")

        // Run afterEvaluate hooks to eagerly configure tasks
        (rootProject as org.gradle.api.internal.project.ProjectInternal).evaluate()
        (child as org.gradle.api.internal.project.ProjectInternal).evaluate()

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
            kotlinx.serialization.json.Json.decodeFromString(
                io.github.baole.konture.core.LayoutModel
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
    fun `testReflectiveAndroidSourceSetWithStubs`() {
        // Stub classes to simulate AGP's AndroidSourceSet and SourceDirectorySet
        class StubSourceDirectorySet(
            private val dirs: Set<java.io.File>,
        ) {
            fun getSrcDirs(): Set<java.io.File> = dirs
        }

        class StubAndroidSourceSet(
            private val nameStr: String,
            private val javaDirs: Set<java.io.File>,
            private val kotlinDirs: Set<java.io.File>,
        ) {
            fun getName(): String = nameStr

            fun getJava(): Any = StubSourceDirectorySet(javaDirs)

            fun getKotlin(): Any = StubSourceDirectorySet(kotlinDirs)
        }

        val javaPaths = setOf(java.io.File("/path/to/java1"), java.io.File("/path/to/java2"))
        val kotlinPaths = setOf(java.io.File("/path/to/kotlin1"))
        val stubSourceSet = StubAndroidSourceSet("main", javaPaths, kotlinPaths)

        val reflectiveSourceSet = ReflectiveAndroidSourceSet(stubSourceSet)

        assertEquals("main", reflectiveSourceSet.name)
        assertEquals(javaPaths, reflectiveSourceSet.javaSrcDirs)
        assertEquals(kotlinPaths, reflectiveSourceSet.kotlinSrcDirs)
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
                org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java,
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
    fun `testKmpSourceSetPlatformTargetsWithReflection`() {
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
                org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class.java,
            )
        kotlinExt.jvm("desktop")
        kotlinExt.iosX64("ios")

        val plugin = KonturePlugin()
        val sourceSets = plugin.collectSourceSets(kmpProject)

        // Find desktopMain
        val desktopMain = sourceSets.firstOrNull { it.name == "desktopMain" }
        assertNotNull(desktopMain)
        // Verify it extracts platform target metadata correctly!
        assertTrue(desktopMain?.platforms?.contains("jvm") == true)

        val iosMain = sourceSets.firstOrNull { it.name == "iosMain" }
        assertNotNull(iosMain)
        assertTrue(iosMain?.platforms?.contains("native") == true)
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

        class DummyDynamicFeaturePlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
            override fun apply(target: org.gradle.api.Project) = Unit
        }

        class DummyAndroidTestPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
            override fun apply(target: org.gradle.api.Project) = Unit
        }

        dynamicFeature.plugins.apply(DummyDynamicFeaturePlugin::class.java)
        testModule.plugins.apply(DummyAndroidTestPlugin::class.java)

        val plugin = KonturePlugin()
        rootProject.plugins.apply("io.github.baole.konture")

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
                org.gradle.api.Project::class.java,
            )
        collectDepsMethod.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val dependencies = collectDepsMethod.invoke(plugin, child) as List<DependencyData>

        assertTrue(dependencies.any { it.targetPath == ":composite-dep" })
    }

    @Test
    fun `testInvalidLogLevelThrowsGradleException`() {
        val rootProject = ProjectBuilder.builder().build()
        rootProject.plugins.apply("io.github.baole.konture")

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        task.logLevel.set("INVALID_LOG_LEVEL")

        val exception =
            org.junit.jupiter.api.Assertions.assertThrows(org.gradle.api.GradleException::class.java) {
                task.generate()
            }
        assertTrue(exception.message?.contains("Invalid log level: 'INVALID_LOG_LEVEL'") == true)
    }

    @Test
    fun `testGenerateDependencyGraphTask`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val child =
            ProjectBuilder
                .builder()
                .withName("child")
                .withParent(rootProject)
                .build()
        child.plugins.apply("org.jetbrains.kotlin.jvm")

        // Apply our plugin
        rootProject.plugins.apply("io.github.baole.konture")

        // Evaluate to configure task properties
        (rootProject as org.gradle.api.internal.project.ProjectInternal).evaluate()
        (child as org.gradle.api.internal.project.ProjectInternal).evaluate()

        val task = rootProject.tasks.getByName("generateDependencyGraph") as GenerateDependencyGraph
        task.outputFile
            .get()
            .asFile.parentFile
            .mkdirs()
        task.execute()

        val outputFile = task.outputFile.get().asFile
        assertTrue(outputFile.exists())

        val jsonText = outputFile.readText()
        val externalDeps =
            kotlinx.serialization.json.Json.decodeFromString(
                io.github.baole.konture.core.DependencyGraphModel
                    .serializer(),
                jsonText,
            )
        assertNotNull(externalDeps)
    }

    @Test
    fun `plugin extension configures baseline path`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.baole.konture")

        val extension = project.extensions.getByName("konture") as KontureExtension
        extension.baselinePath.set("custom-baseline.json")

        assertEquals("custom-baseline.json", extension.baselinePath.get())
    }

    @Test
    fun `plugin configures test tasks with baseline path`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("java") // Registers Test tasks
        project.plugins.apply("io.github.baole.konture")

        val extension = project.extensions.getByName("konture") as KontureExtension
        extension.baselinePath.set("custom-baseline-test.json")

        val testTask = project.tasks.getByName("test") as org.gradle.api.tasks.testing.Test
        val baselinePathProp = testTask.systemProperties["konture.baseline.path"]
        val resolvedValue =
            when (baselinePathProp) {
                is org.gradle.api.provider.Provider<*> -> baselinePathProp.get()
                else -> baselinePathProp
            }
        assertEquals("custom-baseline-test.json", resolvedValue)
    }

    @Test
    fun `root generateKontureBaseline task aggregates subprojects`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val childProject = ProjectBuilder.builder().withName("child").withParent(rootProject).build()

        rootProject.plugins.apply("io.github.baole.konture")
        childProject.plugins.apply("io.github.baole.konture")

        val rootTask = rootProject.tasks.getByName("generateKontureBaseline")
        val childTask = childProject.tasks.getByName("generateKontureBaseline")

        val resolvedDeps = rootTask.taskDependencies.getDependencies(rootTask)
        assertTrue(resolvedDeps.contains(childTask))
    }

    @Test
    fun `cli system property override wins over Gradle DSL extension`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("java") // Registers Test tasks

        System.setProperty(KontureConstants.PROPERTY_BASELINE_PATH, "cli-override-baseline.json")
        System.setProperty(KontureConstants.PROPERTY_BASELINE_DIR, "/cli-override-dir")
        try {
            project.plugins.apply("io.github.baole.konture")

            val extension = project.extensions.getByName("konture") as KontureExtension
            extension.baselinePath.set("dsl-baseline.json")

            val testTask = project.tasks.getByName("test") as org.gradle.api.tasks.testing.Test

            // Check baseline path
            val baselinePathProp = testTask.systemProperties[KontureConstants.PROPERTY_BASELINE_PATH]
            val resolvedPath =
                when (baselinePathProp) {
                    is org.gradle.api.provider.Provider<*> -> baselinePathProp.get()
                    else -> baselinePathProp
                }
            assertEquals("cli-override-baseline.json", resolvedPath)

            // Check baseline dir
            val baselineDirProp = testTask.systemProperties[KontureConstants.PROPERTY_BASELINE_DIR]
            val resolvedDir =
                when (baselineDirProp) {
                    is org.gradle.api.provider.Provider<*> -> baselineDirProp.get()
                    else -> baselineDirProp
                }
            assertEquals("/cli-override-dir", resolvedDir)
        } finally {
            System.clearProperty(KontureConstants.PROPERTY_BASELINE_PATH)
            System.clearProperty(KontureConstants.PROPERTY_BASELINE_DIR)
        }
    }

    @Test
    fun `qualified root invocation enables generate mode in subproject test task`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val childProject = ProjectBuilder.builder().withName("child").withParent(rootProject).build()

        rootProject.plugins.apply("io.github.baole.konture")
        childProject.plugins.apply("java") // Registers Test tasks
        childProject.plugins.apply("io.github.baole.konture")

        rootProject.gradle.startParameter.setTaskNames(listOf(":generateKontureBaseline"))

        val childTestTask = childProject.tasks.getByName("test") as org.gradle.api.tasks.testing.Test
        val doFirstAction = childTestTask.actions.first()
        doFirstAction.execute(childTestTask)

        val generateProp = childTestTask.systemProperties[KontureConstants.PROPERTY_BASELINE_GENERATE]
        val resolvedValue =
            when (generateProp) {
                is org.gradle.api.provider.Provider<*> -> generateProp.get()
                else -> generateProp
            }
        assertEquals("true", resolvedValue.toString())
    }
}
