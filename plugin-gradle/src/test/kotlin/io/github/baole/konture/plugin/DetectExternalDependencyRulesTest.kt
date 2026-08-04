/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.DependencyGraphModel
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlinx.serialization.json.Json

class DetectExternalDependencyRulesTest {
    @Test
    fun `external dependency rule detector follows Kotlin test sources`() {
        val project = ProjectBuilder.builder().build()
        val source = project.file("src/test/kotlin/ArchitectureTest.kt")
        source.parentFile.mkdirs()
        source.writeText("// should().notDependOnExternalLibraries(\"a:b\")\nclass ArchitectureTest")
        val result = project.layout.buildDirectory.file("konture/requires-dependencies.txt").get().asFile
        val task = project.tasks.create("detectExternalRules", DetectExternalDependencyRules::class.java)
        task.testSources.from(source)
        task.resultFile.set(result)

        task.detect()
        assertEquals("false", result.readText())

        source.writeText("class ArchitectureTest { fun rule() = should().notDependOnExternalLibraries(\"a:b\") }")
        task.detect()
        assertEquals("true", result.readText())
    }

    @Test
    fun `root detector includes custom test source sets of Konture consumers`() {
        val root = ProjectBuilder.builder().withName("root").build()
        root.plugins.apply("io.github.baole.konture")
        val consumer = ProjectBuilder.builder().withName("architecture").withParent(root).build()
        consumer.plugins.apply("io.github.baole.konture")
        val source = consumer.file("src/commonTest/kotlin/ArchitectureTest.kt")
        source.parentFile.mkdirs()
        source.writeText("fun rule() = should().onlyDependOnExternalLibraries(\"a:b\")")

        val detector = root.tasks.getByName("detectKontureExternalDependencyRules") as DetectExternalDependencyRules
        detector.detect()

        assertEquals("true", detector.resultFile.get().asFile.readText())
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
        (rootProject as ProjectInternal).evaluate()
        (child as ProjectInternal).evaluate()

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
            Json.decodeFromString(
                DependencyGraphModel
                    .serializer(),
                jsonText,
            )
        assertNotNull(externalDeps)
    }
}
