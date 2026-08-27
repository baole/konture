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
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.impl.report.JsonReportExporter
import io.github.baole.konture.impl.report.ReportAccumulator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Files

internal class BaselineDeltaTest : KontureScopeTestFixture() {
    private lateinit var tempDir: File

    @BeforeEach
    fun initDeltaTest() {
        tempDir = Files.createTempDirectory("konture_delta_test").toFile()
        Konture.reset()
        BaselineManager.resetForTest()
        ReportAccumulator.clear()
        System.setProperty(Konture.PROPERTY_BASELINE_DIR, tempDir.absolutePath)
    }

    @AfterEach
    fun tearDown() {
        System.clearProperty(Konture.PROPERTY_BASELINE_DIR)
        System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
        System.clearProperty(Konture.PROPERTY_FAIL_ON_RESOLVED_VIOLATIONS)
        System.clearProperty(Konture.PROPERTY_REPORT_RESOLVED_VIOLATIONS)
        Konture.reset()
        BaselineManager.resetForTest()
        ReportAccumulator.clear()
        tempDir.deleteRecursively()
    }

    @Test
    fun `test delta tracking computes resolved, active, and new violations correctly`() {
        val manager = BaselineManager()
        manager.resetForTest()

        // 1. Pre-populate baseline file with 2 violations (V1, V2)
        val v1 =
            FlatBaselineViolation(
                testClass = "io.github.baole.konture.impl.BaselineDeltaTest",
                testMethod = "test delta tracking computes resolved, active, and new violations correctly",
                location = "src/A.kt",
                message = "Class com.example.A violates dependency rule",
            )
        val v2 =
            FlatBaselineViolation(
                testClass = "io.github.baole.konture.impl.BaselineDeltaTest",
                testMethod = "test delta tracking computes resolved, active, and new violations correctly",
                location = "src/B.kt",
                message = "Class com.example.B violates dependency rule",
            )
        BaselineSerializer.writeViolationsToFile(manager.baselineFile, listOf(v1, v2))
        manager.resetForTest()

        assertEquals(2, manager.existingViolations.size)

        // 2. Evaluate rule: only V1 is observed, and a new violation V3 is observed
        // V1 is in baseline, so it won't throw; V3 is new, but let's test handleViolations
        // Under generateBaseline = true or verification mode:
        System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "true")
        try {
            manager.handleViolations(
                listOf(
                    "Class com.example.A violates dependency rule (at src/A.kt)",
                    "Class com.example.C violates dependency rule (at src/C.kt)",
                ),
                "Rule failure:",
            )
        } finally {
            System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
        }

        val active = manager.getActiveBaselineViolations()
        val resolved = manager.getResolvedViolations()
        val newViolations = manager.getNewViolations()

        assertEquals(1, active.size)
        assertTrue(active.any { it.location == "src/A.kt" })

        assertEquals(1, resolved.size)
        assertTrue(resolved.any { it.location == "src/B.kt" })

        assertEquals(1, newViolations.size)
        assertTrue(newViolations.any { it.location == "src/C.kt" })
    }

    @Test
    fun `test ratchet mode fails when resolved violations exist`() {
        val manager = BaselineManager()
        manager.resetForTest()

        // Pre-populate baseline with V1
        val v1 =
            FlatBaselineViolation(
                testClass = "io.github.baole.konture.impl.BaselineDeltaTest",
                testMethod = "test ratchet mode fails when resolved violations exist",
                location = "src/Old.kt",
                message = "Class com.example.Old violates rule",
            )
        BaselineSerializer.writeViolationsToFile(manager.baselineFile, listOf(v1))
        manager.resetForTest()

        // Enable ratchet mode
        Konture.failOnResolvedViolations = true

        // No rules evaluated (or V1 was fixed / resolved)
        val error =
            assertThrows<AssertionError> {
                Konture.checkRatchet()
            }

        assertTrue(
            error.message?.contains("1 resolved architecture violation(s)") == true ||
                error.message?.contains("generateKontureBaseline") == true,
        )
    }

    @Test
    fun `test ratchet mode succeeds when no resolved violations exist`() {
        val baselineFile = File(tempDir, "konture-baseline.json")
        val v1 =
            FlatBaselineViolation(
                testClass = "io.github.baole.konture.impl.BaselineDeltaTest",
                testMethod = "test ratchet mode succeeds when no resolved violations exist",
                location = "src/Active.kt",
                message = "Class com.example.Active violates rule",
            )
        BaselineSerializer.writeViolationsToFile(baselineFile, listOf(v1))
        BaselineManager.resetForTest()

        Konture.failOnResolvedViolations = true

        // Simulate active violation being evaluated on current thread-local BaselineManager
        BaselineManager.handleViolations(
            listOf("Class com.example.Active violates rule (at src/Active.kt)"),
            "Rule failure:",
        )

        // Should not throw
        Konture.checkRatchet()
    }

    @Test
    fun `test baseline regeneration prunes resolved violations and deletes empty baseline`() {
        System.clearProperty(Konture.PROPERTY_BASELINE_DIR)

        val rootDir = File(tempDir, "project").apply { mkdirs() }
        val moduleAppDir = File(rootDir, "app").apply { mkdirs() }
        val moduleCoreDir = File(rootDir, "core").apply { mkdirs() }

        val appBaseline = File(moduleAppDir, "konture-baseline.json")
        val coreBaseline = File(moduleCoreDir, "konture-baseline.json")

        // Pre-create baselines
        val vApp =
            FlatBaselineViolation(
                testClass = "Test",
                testMethod = "test",
                location = ":app, main) (app/src/App.kt",
                message = "App violation",
            )
        val vCore =
            FlatBaselineViolation(
                testClass = "Test",
                testMethod = "test",
                location = ":core, main) (core/src/Core.kt",
                message = "Core violation",
            )
        BaselineSerializer.writeViolationsToFile(appBaseline, listOf(vApp))
        BaselineSerializer.writeViolationsToFile(coreBaseline, listOf(vCore))

        assertTrue(appBaseline.exists())
        assertTrue(coreBaseline.exists())

        val moduleApp =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = moduleAppDir.canonicalPath,
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = emptyList(),
            )
        val moduleCore =
            Module(
                buildId = ":",
                path = ":core",
                projectDir = moduleCoreDir.canonicalPath,
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = emptyList(),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(moduleApp, moduleCore)))

        System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "true")

        KontureRuntimeStateProvider.currentState =
            KontureRuntimeStateProvider.currentState.copy(
                projectGraph = graph,
            )

        BaselineManager.resetForTest()

        // In regeneration run, only app has a live violation recorded, core is fixed
        val newAppViolation =
            FlatBaselineViolation(
                testClass = "Test",
                testMethod = "test",
                location = ":app, main) (app/src/NewApp.kt",
                message = "New App violation",
            )
        KontureRuntimeStateProvider.currentState.baselineManager.recordedViolations.add(newAppViolation)

        BaselineManager.writeBaseline()

        // App baseline should exist and contain only newAppViolation
        assertTrue(appBaseline.exists())
        val writtenApp = BaselineSerializer.loadViolationsFromFile(appBaseline)
        assertEquals(1, writtenApp.size)
        assertEquals("New App violation", writtenApp.first().message)

        // Core baseline should be pruned/deleted since core has 0 violations!
        assertFalse(coreBaseline.exists())
    }

    @Test
    fun `test json report includes delta metrics in summary`() {
        val manager = BaselineManager()
        manager.resetForTest()

        val vExisting =
            FlatBaselineViolation(
                testClass = "Test",
                testMethod = "test",
                location = "src/Old.kt",
                message = "Old violation",
            )
        BaselineSerializer.writeViolationsToFile(manager.baselineFile, listOf(vExisting))
        manager.resetForTest()

        val evaluation =
            ReportAccumulator.RuleEvaluation(
                ruleId = "test-rule",
                metadata = null,
                unsuppressedViolations =
                    listOf(
                        Violation(
                            ruleId = "test-rule",
                            subject = Subject.ClassSubject(fqName = "com.example.NewClass", simpleName = "NewClass"),
                            message = "New violation",
                            severity = Severity.ERROR,
                        ),
                    ),
                suppressedViolations = emptyList(),
            )

        val report = JsonReportExporter.generateReport(listOf(evaluation), tempDir)

        assertEquals(1, report.summary.resolvedCount)
        assertEquals(0, report.summary.activeBaselineCount)
        assertEquals(1, report.summary.newViolationsCount)
    }
}
