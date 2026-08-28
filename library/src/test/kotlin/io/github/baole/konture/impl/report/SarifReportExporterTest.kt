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
import io.github.baole.konture.core.report.sarif.SarifReport
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SarifReportExporterTest {
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
        System.clearProperty(Konture.PROPERTY_REPORT_SARIF_PATH)
    }

    @AfterEach
    fun tearDown() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
        System.clearProperty(Konture.PROPERTY_REPORT_SARIF_PATH)
    }

    @Test
    fun `generateReport converts evaluations into standard SARIF v2_1_0 model`() {
        val buildRoot = File(tempDir, "app")
        val originFile = File(buildRoot, "feature/src/Feature.kt")
        val targetFile = File(buildRoot, "internal/src/Internal.kt")

        val hopFile = File(buildRoot, "intermediate/src/Bridge.kt")
        val unsuppressedViolation =
            Violation(
                ruleId = "feature-isolation",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.feature.FeatureScreen",
                        simpleName = "FeatureScreen",
                        location = SourceLocation(filePath = originFile.absolutePath, line = 25, column = 10),
                    ),
                target =
                    Subject.ClassSubject(
                        fqName = "com.example.internal.SecretEngine",
                        simpleName = "SecretEngine",
                        location = SourceLocation(filePath = targetFile.absolutePath, line = 5, column = 1),
                    ),
                sourceLocation = SourceLocation(filePath = originFile.absolutePath, line = 25, column = 10),
                dependencyPath =
                    listOf(
                        Subject.ClassSubject(
                            fqName = "com.example.intermediate.Bridge",
                            simpleName = "Bridge",
                            location = SourceLocation(filePath = hopFile.absolutePath, line = 12, column = 4),
                        ),
                    ),
                severity = Severity.ERROR,
                message = "Access to internal engine forbidden",
            )

        val suppressedViolation =
            Violation(
                ruleId = "feature-isolation",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.feature.LegacyView",
                        simpleName = "LegacyView",
                    ),
                severity = Severity.WARNING,
                message = "Legacy debt",
            )

        val metadata =
            RuleMetadata(
                id = "feature-isolation",
                description = "Isolate internal packages",
                severity = Severity.ERROR,
                tags = setOf("modularization", "security"),
            )

        val evaluation =
            ReportAccumulator.RuleEvaluation(
                ruleId = "feature-isolation",
                metadata = metadata,
                unsuppressedViolations = listOf(unsuppressedViolation),
                suppressedViolations = listOf(suppressedViolation),
            )

        val report = SarifReportExporter.generateReport(listOf(evaluation), buildRoot)

        assertEquals("https://json.schemastore.org/sarif-2.1.0.json", report.schema)
        assertEquals("2.1.0", report.version)
        assertEquals(1, report.runs.size)

        val run = report.runs.first()
        assertEquals("Konture", run.tool.driver.name)
        assertEquals(KontureConstants.VERSION, run.tool.driver.version)
        assertEquals("https://github.com/baole/konture", run.tool.driver.informationUri)

        assertEquals(1, run.tool.driver.rules.size)
        val rule = run.tool.driver.rules.first()
        assertEquals("feature-isolation", rule.id)
        assertEquals("error", rule.defaultConfiguration?.level)
        assertTrue(rule.properties?.tags?.contains("security") == true)

        assertEquals(2, run.results.size)

        val r0 = run.results[0]
        assertEquals("feature-isolation", r0.ruleId)
        assertEquals("error", r0.level)
        assertEquals("Access to internal engine forbidden", r0.message.text)
        assertNull(r0.suppressions)
        assertNotNull(r0.locations)
        val loc = r0.locations!!.first().physicalLocation
        assertEquals("feature/src/Feature.kt", loc.artifactLocation.uri)
        assertEquals("%SRCROOT%", loc.artifactLocation.uriBaseId)
        assertEquals(25, loc.region?.startLine)
        assertEquals(10, loc.region?.startColumn)

        assertNotNull(r0.codeFlows)
        val threadFlow = r0.codeFlows!!.first().threadFlows.first()
        assertEquals(3, threadFlow.locations.size)
        assertEquals("feature/src/Feature.kt", threadFlow.locations[0].location.physicalLocation.artifactLocation.uri)
        assertEquals(
            "intermediate/src/Bridge.kt",
            threadFlow.locations[1].location.physicalLocation.artifactLocation.uri,
        )
        assertEquals("internal/src/Internal.kt", threadFlow.locations[2].location.physicalLocation.artifactLocation.uri)

        val r1 = run.results[1]
        assertEquals("warning", r1.level)
        assertNotNull(r1.suppressions)
        assertEquals("external", r1.suppressions!!.first().kind)
        assertEquals("accepted", r1.suppressions!!.first().status)

        val sarifJson = SarifReportExporter.exportToString(report)
        assertTrue(sarifJson.contains("\"\$schema\": \"https://json.schemastore.org/sarif-2.1.0.json\""))
        assertTrue(sarifJson.contains("\"version\": \"2.1.0\""))

        val decoded = json.decodeFromString<SarifReport>(sarifJson)
        assertEquals(2, decoded.runs.first().results.size)
    }

    @Test
    fun `writeReport creates SARIF file`() {
        val targetFile = File(tempDir, "sarif-out/report.sarif")
        val report = SarifReportExporter.generateReport(emptyList())

        SarifReportExporter.writeReport(report, targetFile)

        assertTrue(targetFile.exists())
        val content = Files.readString(targetFile.toPath())
        assertTrue(content.contains("\"version\": \"2.1.0\""))
    }

    @Test
    fun `generateReport falls back to suppressed violation severity when rule has no metadata and zero unsuppressed violations`() {
        val suppressedWarning =
            Violation(
                ruleId = "suppressed-warning-rule",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.Suppressed",
                        simpleName = "Suppressed",
                    ),
                severity = Severity.WARNING,
                message = "Suppressed warning violation",
            )

        val eval =
            ReportAccumulator.RuleEvaluation(
                ruleId = "suppressed-warning-rule",
                metadata = null,
                unsuppressedViolations = emptyList(),
                suppressedViolations = listOf(suppressedWarning),
            )

        val report = SarifReportExporter.generateReport(listOf(eval))
        val rules = report.runs.first().tool.driver.rules
        assertEquals(1, rules.size)
        assertEquals("warning", rules[0].defaultConfiguration?.level)
    }

    @Test
    fun `generateReport handles full sequence dependencyPath with origin intermediate and target`() {
        val buildRoot = File(tempDir, "app")
        val originFile = File(buildRoot, "feature/src/Feature.kt")
        val stepFile = File(buildRoot, "intermediate/src/Bridge.kt")
        val targetFile = File(buildRoot, "internal/src/Internal.kt")

        val violation =
            Violation(
                ruleId = "transitive-isolation",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.feature.FeatureScreen",
                        simpleName = "FeatureScreen",
                        location = SourceLocation(filePath = originFile.absolutePath, line = 25, column = 10),
                    ),
                target =
                    Subject.ClassSubject(
                        fqName = "com.example.internal.SecretEngine",
                        simpleName = "SecretEngine",
                        location = SourceLocation(filePath = targetFile.absolutePath, line = 5, column = 1),
                    ),
                sourceLocation = SourceLocation(filePath = originFile.absolutePath, line = 25, column = 10),
                dependencyPath =
                    listOf(
                        Subject.ClassSubject(
                            fqName = "com.example.feature.FeatureScreen",
                            simpleName = "FeatureScreen",
                            location = SourceLocation(filePath = originFile.absolutePath, line = 25, column = 10),
                        ),
                        Subject.ClassSubject(
                            fqName = "com.example.intermediate.Bridge",
                            simpleName = "Bridge",
                            location = SourceLocation(filePath = stepFile.absolutePath, line = 12, column = 4),
                        ),
                        Subject.ClassSubject(
                            fqName = "com.example.internal.SecretEngine",
                            simpleName = "SecretEngine",
                            location = SourceLocation(filePath = targetFile.absolutePath, line = 5, column = 1),
                        ),
                    ),
                severity = Severity.ERROR,
                message = "Transitive dependency forbidden",
            )

        val eval =
            ReportAccumulator.RuleEvaluation(
                ruleId = "transitive-isolation",
                metadata = null,
                unsuppressedViolations = listOf(violation),
                suppressedViolations = emptyList(),
            )

        val report = SarifReportExporter.generateReport(listOf(eval), buildRoot)
        val threadFlow = report.runs.first().results.first().codeFlows!!.first().threadFlows.first()
        assertEquals(3, threadFlow.locations.size)
        assertEquals("feature/src/Feature.kt", threadFlow.locations[0].location.physicalLocation.artifactLocation.uri)
        assertEquals(
            "intermediate/src/Bridge.kt",
            threadFlow.locations[1].location.physicalLocation.artifactLocation.uri,
        )
        assertEquals("internal/src/Internal.kt", threadFlow.locations[2].location.physicalLocation.artifactLocation.uri)
    }
}
