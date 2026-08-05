/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Konture
import io.github.baole.konture.KontureScopeTestFixture
import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class BaselineManagerTest : KontureScopeTestFixture() {
    @Test
    fun `test BaselineNormalizer normalize`() {
        val root = File("/app/project").canonicalFile
        val normalized = BaselineNormalizer.normalize("Class /app/project/src/Main.kt violates rule", root)
        assertTrue(normalized.contains("<root>"))
    }

    @Test
    fun `test BaselineNormalizer parseLocationAndMessage`() {
        val root = File("/app/project").canonicalFile
        val res =
            BaselineNormalizer.parseLocationAndMessage(
                "Class com.example.Main violates rule (at src/Main.kt)",
                root,
            )
        assertEquals("src/Main.kt", res.first)
        assertEquals("Class com.example.Main violates rule", res.second)

        val resPrefix = BaselineNormalizer.parseLocationAndMessage("Class com.example.Main violates rule", root)
        assertEquals("com.example.Main", resPrefix.first)
        assertEquals("violates rule", resPrefix.second)
    }

    @Test
    fun `test BaselineNormalizer findModuleForViolation`() {
        val moduleApp = Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA))
        val graph = ProjectGraph(mapOf(":" to listOf(moduleApp)))
        val violation = FlatBaselineViolation("Test", "test", ":app, main source set) (src/ClassA.kt)", "msg")

        val mod = BaselineNormalizer.findModuleForViolation(violation, graph, null)
        assertNotNull(mod)
        assertEquals(":app", mod?.path)
    }

    @Test
    fun `test BaselineManager properties and resetForTest`() {
        val manager = BaselineManager()
        manager.resetForTest()

        assertNotNull(manager.baselineDir)
        assertNotNull(manager.baselineFile)
        assertNotNull(manager.existingViolations)
        assertEquals(0, manager.recordedViolations.size)
    }

    @Test
    fun `test BaselineManager handleViolations recording and verification`() {
        val manager = BaselineManager()
        manager.resetForTest()

        // 1. Empty violations
        manager.handleViolations(emptyList(), "Header")

        // 2. Single violation with custom System.setProperty(PROPERTY_BASELINE_GENERATE, "true")
        System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "true")
        try {
            manager.handleViolations(listOf("Class com.example.A violates rule (at src/A.kt)"), "Rule Failed:")
            assertEquals(1, manager.recordedViolations.size)
        } finally {
            System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
        }

        // 3. Verification mode with new violation -> throws AssertionError
        System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "false")
        try {
            org.junit.jupiter.api.assertThrows<AssertionError> {
                manager.handleViolations(listOf("Class com.example.A violates rule (at src/A.kt)"), "Rule Failed:")
            }
        } finally {
            System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
        }
    }

    @Test
    fun `test BaselineManager checkRule and writeBaseline`() {
        val tempDir = java.nio.file.Files.createTempDirectory("konture_baseline_test").toFile()
        tempDir.deleteOnExit()

        System.setProperty(Konture.PROPERTY_BASELINE_DIR, tempDir.absolutePath)
        System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "true")
        try {
            val manager = BaselineManager()
            manager.resetForTest()

            manager.checkRule("Rule Header:") { violations ->
                violations.add("Violation in checkRule (at src/B.kt)")
            }

            assertEquals(1, manager.recordedViolations.size)

            // Test writeBaseline
            manager.writeBaseline()
            assertTrue(manager.baselineFile.exists())
        } finally {
            System.clearProperty(Konture.PROPERTY_BASELINE_DIR)
            System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
        }
    }

    @Test
    fun `test BaselineManager companion object static methods`() {
        val manager = BaselineManager()
        manager.resetForTest()

        val normalized = BaselineManager.normalize("Class com.example.A (at src/A.kt)", null)
        assertNotNull(normalized)

        val moduleApp = Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA))
        val graph = ProjectGraph(mapOf(":" to listOf(moduleApp)))
        val violation = FlatBaselineViolation("Test", "test", ":app, main source set) (src/ClassA.kt)", "msg")

        val mod = BaselineManager.findModuleForViolation(violation, graph)
        assertEquals(":app", mod?.path)

        BaselineManager.resetForTest()
    }
}
