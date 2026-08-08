/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.DependencyGraphModel
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlinx.serialization.json.Json

class GenerateDependencyGraphTest {
    @Test
    fun `generate dependency graph resolves direct and transitive dependencies correctly`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("testGenerateDependencyGraph", GenerateDependencyGraph::class.java)

        val outputFile = project.layout.buildDirectory.file("konture/dependencies.json").get().asFile
        outputFile.parentFile.mkdirs()
        task.outputFile.set(outputFile)

        task.declaredDependencies.set(
            mapOf(
                ":app:compileClasspath" to listOf("org.jetbrains.kotlin:kotlin-stdlib"),
            ),
        )

        task.resolvedDependencies.set(
            mapOf(
                ":app:compileClasspath" to
                    listOf(
                        "org.jetbrains.kotlin:kotlin-stdlib:1.9.20",
                        "org.jetbrains:annotations:13.0",
                        "invalid-coord",
                    ),
                "invalidKeyWithoutColon" to listOf("a:b:1.0"),
            ),
        )

        task.execute()

        assertTrue(outputFile.exists())
        val jsonText = outputFile.readText()
        val model = Json.decodeFromString(DependencyGraphModel.serializer(), jsonText)

        assertNotNull(model)
        assertEquals(1, model.schemaVersion)
        assertTrue(model.modules.containsKey(":app"))

        val appDeps = model.modules[":app"]
        assertNotNull(appDeps)
        assertEquals(2, appDeps!!.size)

        val directDep = appDeps.find { it.name == "kotlin-stdlib" }
        assertNotNull(directDep)
        assertEquals("org.jetbrains.kotlin", directDep!!.group)
        assertEquals("1.9.20", directDep.version)
        assertEquals("compileClasspath", directDep.configuration)
        assertFalse(directDep.isTransitive)

        val transitiveDep = appDeps.find { it.name == "annotations" }
        assertNotNull(transitiveDep)
        assertEquals("org.jetbrains", transitiveDep!!.group)
        assertEquals("13.0", transitiveDep.version)
        assertEquals("compileClasspath", transitiveDep.configuration)
        assertTrue(transitiveDep.isTransitive)
    }

    @Test
    fun `generate dependency graph deduplicates identical dependencies`() {
        val project = ProjectBuilder.builder().build()
        val task = project.tasks.create("testGenerateDependencyGraphDeduplicate", GenerateDependencyGraph::class.java)

        val outputFile = project.layout.buildDirectory.file("konture/dependencies_dedup.json").get().asFile
        outputFile.parentFile.mkdirs()
        task.outputFile.set(outputFile)

        task.declaredDependencies.set(
            mapOf(
                ":core:compileClasspath" to listOf("com.example:lib"),
            ),
        )

        task.resolvedDependencies.set(
            mapOf(
                ":core:compileClasspath" to
                    listOf(
                        "com.example:lib:1.0.0",
                        "com.example:lib:1.0.0",
                    ),
            ),
        )

        task.execute()

        val jsonText = outputFile.readText()
        val model = Json.decodeFromString(DependencyGraphModel.serializer(), jsonText)

        val coreDeps = model.modules[":core"]
        assertNotNull(coreDeps)
        assertEquals(1, coreDeps!!.size)
    }
}
