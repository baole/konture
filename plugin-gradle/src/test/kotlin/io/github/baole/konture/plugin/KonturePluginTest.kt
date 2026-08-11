/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.KontureConstants
import org.gradle.api.attributes.Usage
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test as GradleTestTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class KonturePluginTest {
    @Test
    fun `plugin registers task and extension on root project`() {
        // Create a root project using ProjectBuilder
        val project = ProjectBuilder.builder().build()

        // Apply our plugin
        project.plugins.apply("io.github.baole.konture.internal")

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
        project.plugins.apply("io.github.baole.konture.internal")

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
        subProject.plugins.apply("io.github.baole.konture.internal")

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
        rootProject.plugins.apply("io.github.baole.konture.internal")

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
    fun `testLocaleSystemPropertyOverrideAndDefault`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("java") // Registers Test tasks

        // Default locale from extension
        project.plugins.apply("io.github.baole.konture.internal")
        val testTask = project.tasks.getByName("test") as GradleTestTask
        val localeProp = testTask.systemProperties[KontureConstants.PROPERTY_LOCALE]
        val resolvedLocale =
            when (localeProp) {
                is Provider<*> -> localeProp.get()
                else -> localeProp
            }
        assertEquals("en", resolvedLocale)

        // System property override
        System.setProperty(KontureConstants.PROPERTY_LOCALE, "fr")
        try {
            val project2 = ProjectBuilder.builder().build()
            project2.plugins.apply("java")
            project2.plugins.apply("io.github.baole.konture.internal")
            val testTask2 = project2.tasks.getByName("test") as GradleTestTask
            val localeProp2 = testTask2.systemProperties[KontureConstants.PROPERTY_LOCALE]
            val resolvedLocale2 =
                when (localeProp2) {
                    is Provider<*> -> localeProp2.get()
                    else -> localeProp2
                }
            assertEquals("fr", resolvedLocale2)
        } finally {
            System.clearProperty(KontureConstants.PROPERTY_LOCALE)
        }
    }

    @Test
    fun `testOutgoingConfigurationsAttributes`() {
        val rootProject = ProjectBuilder.builder().build()
        rootProject.plugins.apply("io.github.baole.konture.internal")

        val layoutConfig = rootProject.configurations.getByName("archLayoutElements")
        val layoutUsage = layoutConfig.attributes.getAttribute(Usage.USAGE_ATTRIBUTE)?.name
        assertEquals("koarch-layout", layoutUsage)

        val depsConfig = rootProject.configurations.getByName("archDepsElements")
        val depsUsage = depsConfig.attributes.getAttribute(Usage.USAGE_ATTRIBUTE)?.name
        assertEquals("koarch-deps", depsUsage)
    }

    @Test
    fun `testGenerateDepsTaskIncludesSettingsAndVersionCatalog`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()

        val settingsFile = File(rootProject.rootDir, "settings.gradle.kts")
        settingsFile.writeText("rootProject.name = \"root\"")

        val libsDir = File(rootProject.rootDir, "gradle")
        libsDir.mkdirs()
        val versionCatalog = File(libsDir, "libs.versions.toml")
        versionCatalog.writeText("[versions]\n")

        rootProject.plugins.apply("io.github.baole.konture.internal")
        (rootProject as ProjectInternal).evaluate()

        val generateDepsTask = rootProject.tasks.getByName("generateDependencyGraph") as GenerateDependencyGraph
        val buildFiles = generateDepsTask.buildFiles.files

        assertTrue(buildFiles.contains(settingsFile.canonicalFile))
        assertTrue(buildFiles.contains(versionCatalog.canonicalFile))
    }

    @Test
    fun `testConsumerLayoutTaskDependenciesAndProcessTestResources`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val subProject = ProjectBuilder.builder().withName("sub").withParent(rootProject).build()

        subProject.plugins.apply("java")
        subProject.plugins.apply("io.github.baole.konture.internal")

        val processTestResources = subProject.tasks.getByName("processTestResources")
        val dependencies = processTestResources.taskDependencies.getDependencies(processTestResources)

        val depTaskNames = dependencies.map { it.name }.toSet()
        assertTrue(depTaskNames.contains("copyArchitectureLayout"))
        assertTrue(depTaskNames.contains("cleanArchitectureDependencyResource"))
        assertTrue(depTaskNames.contains("copyArchitectureDeps"))
    }

    @Test
    fun `testCollectAllSourceDirsFiltersBuildDir`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val child = ProjectBuilder.builder().withName("child").withParent(rootProject).build()
        child.plugins.apply("org.jetbrains.kotlin.jvm")

        // Add a normal source dir and a build generated source dir
        val normalSrcDir = File(child.projectDir, "src/main/kotlin")
        normalSrcDir.mkdirs()

        val buildDir = child.layout.buildDirectory.get().asFile
        val buildSrcDir = File(buildDir, "generated/kotlin")
        buildSrcDir.mkdirs()

        val kotlinExt = child.extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension::class.java)
        kotlinExt.sourceSets.getByName("main").kotlin.srcDir(buildSrcDir)

        rootProject.plugins.apply("io.github.baole.konture.internal")
        (rootProject as ProjectInternal).evaluate()
        (child as ProjectInternal).evaluate()

        val task = rootProject.tasks.getByName("generateArchitectureLayout") as GenerateArchitectureLayout
        val sourceDirs = task.sourceFiles.files

        assertTrue(sourceDirs.contains(normalSrcDir.canonicalFile))
        // Verify source dir inside buildDir was filtered out
        assertTrue(sourceDirs.none { it.canonicalPath.startsWith(buildDir.canonicalPath) })
    }
}
