/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.report

import io.github.baole.konture.Konture
import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.core.model.RuleMetadata
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.report.KontureJsonReport
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class JsonReportExporterTest {
    @TempDir
    lateinit var tempDir: File

    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    @BeforeEach
    fun setUp() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_JSON_PATH)
    }

    @AfterEach
    fun tearDown() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_JSON_PATH)
    }

    @Test
    fun `generateReport converts evaluations into structured KontureJsonReport`() {
        val buildRoot = File(tempDir, "myproject")
        val originFile = File(buildRoot, "domain/src/Order.kt")
        val targetFile = File(buildRoot, "ui/src/OrderUi.kt")

        val intermediateFile = File(buildRoot, "domain/src/Intermediate.kt")
        val unsuppressedViolation =
            Violation(
                ruleId = "domain-rules",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.domain.Order",
                        simpleName = "Order",
                        location = SourceLocation(filePath = originFile.absolutePath, line = 15, column = 2),
                    ),
                target =
                    Subject.ClassSubject(
                        fqName = "com.example.ui.OrderUi",
                        simpleName = "OrderUi",
                        location = SourceLocation(filePath = targetFile.absolutePath, line = 40, column = 1),
                    ),
                sourceLocation = SourceLocation(filePath = originFile.absolutePath, line = 15, column = 2),
                dependencyPath =
                    listOf(
                        Subject.ClassSubject(
                            fqName = "com.example.domain.Intermediate",
                            simpleName = "Intermediate",
                            location = SourceLocation(filePath = intermediateFile.absolutePath, line = 25, column = 1),
                        ),
                    ),
                severity = Severity.ERROR,
                message = "Domain layer cannot depend on UI layer",
            )

        val suppressedViolation =
            Violation(
                ruleId = "domain-rules",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.domain.LegacyHelper",
                        simpleName = "LegacyHelper",
                        location = SourceLocation(filePath = originFile.absolutePath, line = 80, column = 1),
                    ),
                sourceLocation = SourceLocation(filePath = originFile.absolutePath, line = 80, column = 1),
                severity = Severity.WARNING,
                message = "Legacy dependency",
            )

        val metadata =
            RuleMetadata(
                id = "domain-rules",
                description = "Domain isolation check",
                severity = Severity.ERROR,
                tags = setOf("architecture", "domain"),
            )

        val evaluation =
            ReportAccumulator.RuleEvaluation(
                ruleId = "domain-rules",
                metadata = metadata,
                unsuppressedViolations = listOf(unsuppressedViolation),
                suppressedViolations = listOf(suppressedViolation),
            )

        val passedEvaluation =
            ReportAccumulator.RuleEvaluation(
                ruleId = "clean-code",
                metadata =
                    RuleMetadata(
                        id = "clean-code",
                        description = "Naming rules",
                        severity = Severity.INFO,
                    ),
                unsuppressedViolations = emptyList(),
                suppressedViolations = emptyList(),
            )

        val report = JsonReportExporter.generateReport(listOf(evaluation, passedEvaluation), buildRoot)

        assertEquals("1.0.0", report.schemaVersion)
        assertEquals("Konture", report.tool.name)
        assertEquals(KontureConstants.VERSION, report.tool.version)
        assertEquals(2, report.summary.totalRules)
        assertEquals(1, report.summary.passedRules)
        assertEquals(1, report.summary.failedRules)
        assertEquals(1, report.summary.totalViolations)
        assertEquals(1, report.summary.suppressedCount)
        assertEquals(1, report.summary.errorCount)
        assertEquals(0, report.summary.warningCount)
        assertEquals(0, report.summary.infoCount)

        assertEquals(2, report.rules.size)
        assertEquals("domain-rules", report.rules[0].id)
        assertEquals("Domain isolation check", report.rules[0].description)
        assertTrue(report.rules[0].tags.contains("domain"))

        assertEquals(2, report.violations.size)
        val v0 = report.violations[0]
        assertEquals("domain-rules", v0.ruleId)
        assertFalse(v0.isSuppressed)
        assertEquals("domain/src/Order.kt", v0.sourceLocation?.filePath)
        assertEquals("domain/src/Order.kt", v0.subject.location?.filePath)
        assertEquals("ui/src/OrderUi.kt", v0.target?.location?.filePath)
        assertEquals(1, v0.dependencyPath.size)
        assertEquals("domain/src/Intermediate.kt", v0.dependencyPath[0].location?.filePath)

        val v1 = report.violations[1]
        assertTrue(v1.isSuppressed)
        assertEquals(Severity.WARNING, v1.severity)

        val jsonStr = JsonReportExporter.exportToString(report)
        assertTrue(jsonStr.contains("\"schemaVersion\": \"1.0.0\""))
        assertTrue(jsonStr.contains("\"domain/src/Order.kt\""))

        val decoded = json.decodeFromString<KontureJsonReport>(jsonStr)
        assertEquals(report.schemaVersion, decoded.schemaVersion)
        assertEquals(2, decoded.violations.size)
    }

    @Test
    fun `writeReport creates target file and parent directories`() {
        val targetFile = File(tempDir, "sub/dir/reports/konture-report.json")
        val report =
            JsonReportExporter.generateReport(
                evaluations = emptyList(),
            )

        JsonReportExporter.writeReport(report, targetFile)

        assertTrue(targetFile.exists())
        val content = Files.readString(targetFile.toPath())
        assertTrue(content.contains("\"totalRules\": 0"))
    }
}
