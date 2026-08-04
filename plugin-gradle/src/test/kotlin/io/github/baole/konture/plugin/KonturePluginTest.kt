/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

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
}
