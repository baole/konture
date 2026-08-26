/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.report

import io.github.baole.konture.Konture
import io.github.baole.konture.OutputFormat
import io.github.baole.konture.core.model.RuleMetadata
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files

class ReportAccumulatorTest {
    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_JSON_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_SARIF_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_HTML_PATH)
    }

    @AfterEach
    fun tearDown() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_JSON_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_SARIF_PATH)
        System.clearProperty(Konture.PROPERTY_REPORT_HTML_PATH)
    }

    @Test
    fun `recordEvaluation records results thread-safely and writes multiple report sinks`() {
        val jsonReportFile = File(tempDir, "out/report.json")
        val sarifReportFile = File(tempDir, "out/report.sarif")
        val htmlReportFile = File(tempDir, "out/report.html")
        File(tempDir, ".git/config").apply {
            parentFile.mkdirs()
            writeText(
                """
                [remote "origin"]
                    url = https://github.com/example/konture-sample.git
                """.trimIndent(),
            )
        }

        Konture.jsonReportPath = jsonReportFile.absolutePath
        Konture.sarifReportPath = sarifReportFile.absolutePath
        Konture.htmlReportPath = htmlReportFile.absolutePath

        System.setProperty(Konture.PROPERTY_REPORT_JSON_PATH, jsonReportFile.absolutePath)
        System.setProperty(Konture.PROPERTY_REPORT_SARIF_PATH, sarifReportFile.absolutePath)
        System.setProperty(Konture.PROPERTY_REPORT_HTML_PATH, htmlReportFile.absolutePath)

        val violation =
            Violation(
                ruleId = "rule-1",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.Service",
                        simpleName = "Service",
                        location = SourceLocation(filePath = "src/Service.kt", line = 10),
                    ),
                severity = Severity.ERROR,
                message = "Architecture violation in Service",
            )

        ReportAccumulator.recordEvaluation(
            ruleId = "rule-1",
            metadata = RuleMetadata(id = "rule-1", description = "Service rule"),
            unsuppressedViolations = listOf(violation),
            suppressedViolations = emptyList(),
        )

        ReportAccumulator.writeReports(tempDir)

        val evaluations = ReportAccumulator.getAllEvaluations()
        assertEquals(1, evaluations.size)
        assertEquals("rule-1", evaluations.first().ruleId)

        assertTrue(jsonReportFile.exists())
        assertTrue(sarifReportFile.exists())
        assertTrue(htmlReportFile.exists())

        val jsonContent = Files.readString(jsonReportFile.toPath())
        assertTrue(jsonContent.contains("\"totalViolations\": 1"))

        val sarifContent = Files.readString(sarifReportFile.toPath())
        assertTrue(sarifContent.contains("\"ruleId\": \"rule-1\""))

        val htmlContent = Files.readString(htmlReportFile.toPath())
        assertTrue(htmlContent.contains("Architecture violation in Service"))
        assertTrue(htmlContent.contains("Project:"))
        assertTrue(htmlContent.contains("https://github.com/example/konture-sample"))
        assertTrue(htmlContent.contains("Konture:"))
        assertTrue(htmlContent.contains("https://github.com/baole/konture"))
    }

    @Test
    fun `outputFormat JSON automatically triggers JSON report export`() {
        val jsonReportFile = File(tempDir, "out/auto-report.json")
        Konture.jsonReportPath = jsonReportFile.absolutePath
        Konture.outputFormat = OutputFormat.JSON

        ReportAccumulator.recordEvaluation(
            ruleId = "rule-auto",
            metadata = null,
            unsuppressedViolations = emptyList(),
            suppressedViolations = emptyList(),
        )

        ReportAccumulator.writeReports(tempDir)

        assertTrue(jsonReportFile.exists())
        val jsonContent = Files.readString(jsonReportFile.toPath())
        assertTrue(jsonContent.contains("\"passedRules\": 1"))
    }

    @Test
    fun `outputFormat SARIF automatically triggers SARIF report export`() {
        val sarifReportFile = File(tempDir, "out/auto-report.sarif")
        Konture.sarifReportPath = sarifReportFile.absolutePath
        Konture.outputFormat = OutputFormat.SARIF

        ReportAccumulator.recordEvaluation(
            ruleId = "rule-auto-sarif",
            metadata = null,
            unsuppressedViolations = emptyList(),
            suppressedViolations = emptyList(),
        )

        ReportAccumulator.writeReports(tempDir)

        assertTrue(sarifReportFile.exists())
        val sarifContent = Files.readString(sarifReportFile.toPath())
        assertTrue(sarifContent.contains("\"version\": \"2.1.0\""))
    }

    @Test
    fun `setting programmatic report paths triggers export without system properties or format changes`() {
        val customJsonFile = File(tempDir, "custom/programmatic.json")
        val customSarifFile = File(tempDir, "custom/programmatic.sarif")

        Konture.jsonReportPath = customJsonFile.absolutePath
        Konture.sarifReportPath = customSarifFile.absolutePath

        ReportAccumulator.recordEvaluation(
            ruleId = "rule-programmatic",
            metadata = null,
            unsuppressedViolations = emptyList(),
            suppressedViolations = emptyList(),
        )

        ReportAccumulator.writeReports(tempDir)

        assertTrue(customJsonFile.exists())
        assertTrue(customSarifFile.exists())
    }
}
