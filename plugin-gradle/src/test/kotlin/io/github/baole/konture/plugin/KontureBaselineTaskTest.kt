/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.KontureConstants
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test as GradleTestTask
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class KontureBaselineTaskTest {
    @Test
    fun `plugin extension configures baseline path`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.baole.konture.internal")

        val extension = project.extensions.getByName("konture") as KontureExtension
        extension.baselinePath.set("custom-baseline.json")

        assertEquals("custom-baseline.json", extension.baselinePath.get())
    }

    @Test
    fun `plugin configures test tasks with baseline path`() {
        val originalBaselinePath = System.getProperty(KontureConstants.PROPERTY_BASELINE_PATH)
        System.clearProperty(KontureConstants.PROPERTY_BASELINE_PATH)
        try {
            val project = ProjectBuilder.builder().build()
            project.plugins.apply("java") // Registers Test tasks
            project.plugins.apply("io.github.baole.konture.internal")

            val extension = project.extensions.getByName("konture") as KontureExtension
            extension.baselinePath.set("custom-baseline-test.json")

            val testTask = project.tasks.getByName("test") as GradleTestTask
            val baselinePathProp = testTask.systemProperties["konture.baseline.path"]
            val resolvedValue =
                when (baselinePathProp) {
                    is Provider<*> -> baselinePathProp.get()
                    else -> baselinePathProp
                }
            assertEquals("custom-baseline-test.json", resolvedValue)
        } finally {
            if (originalBaselinePath != null) {
                System.setProperty(KontureConstants.PROPERTY_BASELINE_PATH, originalBaselinePath)
            } else {
                System.clearProperty(KontureConstants.PROPERTY_BASELINE_PATH)
            }
        }
    }

    @Test
    fun `test task accepts a missing baseline file`(
        @TempDir projectDir: Path,
    ) {
        projectDir.resolve("settings.gradle.kts").toFile().writeText(
            """
            plugins {
                id("io.github.baole.konture")
            }
            rootProject.name = "missing-baseline"
            """.trimIndent(),
        )
        projectDir.resolve("build.gradle.kts").toFile().writeText(
            """
            plugins {
                java
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation("org.junit.jupiter:junit-jupiter:5.12.0")
                testRuntimeOnly("org.junit.platform:junit-platform-launcher")
            }

            tasks.test {
                useJUnitPlatform()
            }
            """.trimIndent(),
        )
        val sourceFile = projectDir.resolve("src/test/java/PassingTest.java").toFile()
        sourceFile.parentFile.mkdirs()
        sourceFile.writeText(
            """
            import org.junit.jupiter.api.Test;

            class PassingTest {
                @Test void passes() { }
            }
            """.trimIndent(),
        )

        val result =
            GradleRunner
                .create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments("test", "--stacktrace")
                .build()

        assertEquals("SUCCESS", result.task(":test")?.outcome?.name)
    }

    @Test
    fun `root generateKontureBaseline task aggregates subprojects`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val childProject = ProjectBuilder.builder().withName("child").withParent(rootProject).build()

        rootProject.plugins.apply("io.github.baole.konture.internal")
        childProject.plugins.apply("io.github.baole.konture.internal")

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
            project.plugins.apply("io.github.baole.konture.internal")

            val extension = project.extensions.getByName("konture") as KontureExtension
            extension.baselinePath.set("dsl-baseline.json")

            val testTask = project.tasks.getByName("test") as GradleTestTask

            // Check baseline path
            val baselinePathProp = testTask.systemProperties[KontureConstants.PROPERTY_BASELINE_PATH]
            val resolvedPath =
                when (baselinePathProp) {
                    is Provider<*> -> baselinePathProp.get()
                    else -> baselinePathProp
                }
            assertEquals("cli-override-baseline.json", resolvedPath)

            // Check baseline dir
            val baselineDirProp = testTask.systemProperties[KontureConstants.PROPERTY_BASELINE_DIR]
            val resolvedDir =
                when (baselineDirProp) {
                    is Provider<*> -> baselineDirProp.get()
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

        rootProject.gradle.startParameter.setTaskNames(listOf(":generateKontureBaseline"))

        rootProject.plugins.apply("io.github.baole.konture.internal")
        childProject.plugins.apply("java") // Registers Test tasks
        childProject.plugins.apply("io.github.baole.konture.internal")

        val childTestTask = childProject.tasks.getByName("test") as GradleTestTask
        val generateProp = childTestTask.systemProperties[KontureConstants.PROPERTY_BASELINE_GENERATE]
        val resolvedValue =
            when (generateProp) {
                is Provider<*> -> generateProp.get()
                else -> generateProp
            }
        assertEquals("true", resolvedValue.toString())
    }
}
