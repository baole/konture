/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.maven

import io.github.baole.konture.core.LayoutModel
import java.io.File
import kotlinx.serialization.json.Json
import org.apache.maven.model.Build
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.logging.SystemStreamLog
import org.apache.maven.project.MavenProject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GenerateLayoutMojoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `skips layout generation for root POM project`(@TempDir tempDir: File) {
        val mojo = GenerateLayoutMojo()
        val rootProject = createMavenProject(tempDir, "root-pom", "pom", isRoot = true)

        setPrivateField(mojo, "project", rootProject)
        setPrivateField(mojo, "reactorProjects", listOf(rootProject))

        mojo.execute()

        val outputDir = File(tempDir, "target/test-classes/konture")
        assertFalse(outputDir.exists(), "Output dir should not be created for root POM project")
    }

    @Test
    fun `generates layout and dependency models for single jvm project`(@TempDir tempDir: File) {
        val mojo = GenerateLayoutMojo()
        val project = createMavenProject(tempDir, "my-app", "jar", isRoot = true)
        val srcDir = File(tempDir, "src/main/kotlin").apply { mkdirs() }
        File(srcDir, "Main.kt").writeText("package com.example\nclass Main")

        setPrivateField(mojo, "project", project)
        setPrivateField(mojo, "reactorProjects", listOf(project))
        setPrivateField(mojo, "excludeModules", listOf("sample-excluded"))

        mojo.execute()

        val outputDir = File(tempDir, "target/test-classes/konture")
        assertTrue(outputDir.exists())

        val layoutFile = File(outputDir, "layout_v2.json")
        val depsFile = File(outputDir, "dependencies.json")

        assertTrue(layoutFile.exists())
        assertTrue(depsFile.exists())

        val layoutText = layoutFile.readText()
        val layout = json.decodeFromString(LayoutModel.serializer(), layoutText)

        assertEquals("sample-excluded", layout.exclusions.excludeModules.first())
        assertEquals(1, layout.builds.first().modules.size)
        assertEquals(":", layout.builds.first().modules.first().path)
    }

    @Test
    fun `computes hierarchical paths for nested reactor projects`(@TempDir tempDir: File) {
        val rootDir = tempDir
        val parentProj = createMavenProject(rootDir, "root", "pom", isRoot = true)

        val featureDir = File(rootDir, "feature/profile/api").apply { mkdirs() }
        val featureProj = createMavenProject(featureDir, "profile-api", "jar", isRoot = false)

        val coreDir = File(rootDir, "core/model").apply { mkdirs() }
        val coreProj = createMavenProject(coreDir, "core-model", "jar", isRoot = false)

        val dep = Dependency().apply {
            groupId = "com.example"
            artifactId = "core-model"
            scope = "compile"
        }
        featureProj.dependencies.add(dep)

        val mojo = GenerateLayoutMojo()
        setPrivateField(mojo, "project", featureProj)
        setPrivateField(mojo, "reactorProjects", listOf(parentProj, featureProj, coreProj))

        mojo.execute()

        val outputDir = File(featureDir, "target/test-classes/konture")
        assertTrue(outputDir.exists())

        val layoutFile = File(outputDir, "layout_v2.json")
        val layout = json.decodeFromString(LayoutModel.serializer(), layoutFile.readText())

        val modules = layout.builds.first().modules
        assertEquals(2, modules.size)

        val featureModule = modules.first { it.projectDir == featureDir.canonicalPath }
        assertEquals(":feature:profile:api", featureModule.path)

        val coreModule = modules.first { it.projectDir == coreDir.canonicalPath }
        assertEquals(":core:model", coreModule.path)

        assertEquals(":core:model", featureModule.dependencies.first().targetPath)
    }

    private fun createMavenProject(
        dir: File,
        artifactId: String,
        packaging: String,
        isRoot: Boolean
    ): MavenProject {
        return MavenProject().apply {
            this.artifactId = artifactId
            this.groupId = "com.example"
            this.version = "1.0.0"
            this.packaging = packaging
            this.file = File(dir, "pom.xml")
            this.isExecutionRoot = isRoot
            this.build = Build().apply {
                directory = File(dir, "target").canonicalPath
                testOutputDirectory = File(dir, "target/test-classes").canonicalPath
                sourceDirectory = File(dir, "src/main/java").canonicalPath
            }
        }
    }

    private fun setPrivateField(obj: Any, fieldName: String, value: Any) {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }
}
