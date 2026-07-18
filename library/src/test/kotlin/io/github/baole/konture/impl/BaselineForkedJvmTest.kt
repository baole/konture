/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.TimeUnit

class BaselineForkedJvmTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun testForkedJvmShutdownHookWritesBaselineUsingSystemProperties() {
        val javaHome = System.getProperty("java.home")
        val javaBin = File(javaHome, "bin/java").absolutePath
        val classpath = System.getProperty("java.class.path")

        val baselineFileName = "forked-baseline.json"
        val expectedFile = File(tempDir, baselineFileName)

        val process =
            ProcessBuilder(
                javaBin,
                "-cp",
                classpath,
                "-Dkonture.baseline.generate=true",
                "-Dkonture.baseline.path=$baselineFileName",
                "-Dkonture.baseline.dir=${tempDir.absolutePath}",
                "io.github.baole.konture.impl.ForkedJvmBaselineApp",
            ).redirectErrorStream(true).start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exited = process.waitFor(15, TimeUnit.SECONDS)

        assertTrue(exited, "Forked JVM process timed out. Output:\n$output")
        assertEquals(0, process.exitValue(), "Forked JVM exited with an error. Output:\n$output")
        assertTrue(
            "Failed to register baseline shutdown hook" !in output,
            "Shutdown must not initialize a new context or register another hook. Output:\n$output",
        )

        assertTrue(expectedFile.exists(), "Expected baseline file was not created. Process output:\n$output")

        val content = expectedFile.readText()
        assertTrue(content.contains("com.example.ForkedClass"), "Baseline content is missing recorded violation. Content:\n$content")
        assertTrue(content.contains("violates forked rule"), "Baseline content is missing recorded violation message. Content:\n$content")
    }

    @Test
    fun testForkedJvmShutdownHookWritesProgrammaticBaseline() {
        val javaHome = System.getProperty("java.home")
        val javaBin = File(javaHome, "bin/java").absolutePath
        val classpath = System.getProperty("java.class.path")

        val expectedFile = File(tempDir, "programmatic-baseline.json")

        val process =
            ProcessBuilder(
                javaBin,
                "-cp",
                classpath,
                "io.github.baole.konture.impl.ForkedJvmProgrammaticPathApp",
                expectedFile.absolutePath,
            ).redirectErrorStream(true).start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exited = process.waitFor(15, TimeUnit.SECONDS)

        assertTrue(exited, "Forked JVM process timed out. Output:\n$output")
        assertEquals(0, process.exitValue(), "Forked JVM exited with an error. Output:\n$output")

        assertTrue(expectedFile.exists(), "Expected programmatic baseline file was not created. Process output:\n$output")

        val content = expectedFile.readText()
        assertTrue(content.contains("com.example.ForkedProgrammaticClass"), "Content is missing recorded violation. Content:\n$content")
        assertTrue(content.contains("violates programmatic rule"), "Content is missing recorded violation message. Content:\n$content")
    }

    @Test
    fun testForkedJvmShutdownHookWritesDistributedBaselines() {
        val javaHome = System.getProperty("java.home")
        val javaBin = File(javaHome, "bin/java").absolutePath
        val classpath = System.getProperty("java.class.path")

        val moduleADir = File(tempDir, "module-a")
        val moduleBDir = File(tempDir, "module-b")

        val expectedFileA = File(moduleADir, "my-baseline.json")
        val expectedFileB = File(moduleBDir, "my-baseline.json")

        val process =
            ProcessBuilder(
                javaBin,
                "-cp",
                classpath,
                "io.github.baole.konture.impl.ForkedJvmDistributedBaselineApp",
                moduleADir.absolutePath,
                moduleBDir.absolutePath,
            ).redirectErrorStream(true).start()

        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exited = process.waitFor(15, TimeUnit.SECONDS)

        assertTrue(exited, "Forked JVM process timed out. Output:\n$output")
        assertEquals(0, process.exitValue(), "Forked JVM exited with an error. Output:\n$output")

        assertTrue(expectedFileA.exists(), "Expected module A baseline file was not created. Process output:\n$output")
        assertTrue(expectedFileB.exists(), "Expected module B baseline file was not created. Process output:\n$output")

        val contentA = expectedFileA.readText()
        assertTrue(contentA.contains("violates rule A"), "Content A is incorrect. Content:\n$contentA")

        val contentB = expectedFileB.readText()
        assertTrue(contentB.contains("violates rule B"), "Content B is incorrect. Content:\n$contentB")
    }
}

object ForkedJvmBaselineApp {
    @JvmStatic
    fun main(args: Array<String>) {
        BaselineManager.handleViolations(
            listOf("Class com.example.ForkedClass violates forked rule"),
            "detected",
        )
        println("ForkedJvmBaselineApp successfully recorded violation.")
    }
}

object ForkedJvmProgrammaticPathApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val customPath = args[0]
        io.github.baole.konture.Konture.generateBaseline = true
        io.github.baole.konture.Konture.baselinePath = customPath

        BaselineManager.handleViolations(
            listOf("Class com.example.ForkedProgrammaticClass violates programmatic rule"),
            "detected",
        )
        println("ForkedJvmProgrammaticPathApp successfully recorded violation.")
    }
}

object ForkedJvmDistributedBaselineApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val moduleADir = args[0]
        val moduleBDir = args[1]

        val moduleA =
            Module(
                buildId = ":",
                path = ":module-a",
                projectDir = moduleADir,
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
            )
        val moduleB =
            Module(
                buildId = ":",
                path = ":module-b",
                projectDir = moduleBDir,
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
            )
        val graph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA, moduleB)),
            )

        io.github.baole.konture.Konture.generateBaseline = true
        io.github.baole.konture.Konture.baselinePath = "my-baseline.json"
        io.github.baole.konture.ProjectGraph.setDefault(graph)

        BaselineManager.handleViolations(
            listOf(
                "Module :module-a violates rule A",
                "Module :module-b violates rule B",
            ),
            "detected",
        )
        println("ForkedJvmDistributedBaselineApp successfully recorded distributed violations.")
    }
}
