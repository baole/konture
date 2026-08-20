/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.report

import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KontureJsonReportTest {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @Test
    fun `serialize and deserialize KontureJsonReport roundtrip`() {
        val report =
            KontureJsonReport(
                schemaVersion = "1.0.0",
                tool = ToolMetadata(name = "Konture", version = "0.8.1"),
                timestamp = "2026-08-20T10:00:00Z",
                summary =
                    ReportSummary(
                        totalRules = 2,
                        passedRules = 1,
                        failedRules = 1,
                        totalViolations = 2,
                        errorCount = 1,
                        warningCount = 1,
                        infoCount = 0,
                    ),
                rules =
                    listOf(
                        ReportRule(
                            id = "layers-check",
                            description = "Enforce layer access rules",
                            severity = Severity.ERROR,
                            tags = setOf("architecture", "layers"),
                        ),
                    ),
                violations =
                    listOf(
                        ReportViolation(
                            ruleId = "layers-check",
                            severity = Severity.ERROR,
                            message = "Domain must not access presentation",
                            subject =
                                Subject.ClassSubject(
                                    fqName = "com.example.domain.Order",
                                    simpleName = "Order",
                                    location =
                                        SourceLocation(
                                            filePath = "src/main/kotlin/Order.kt",
                                            line = 10,
                                            column = 5,
                                        ),
                                ),
                            target =
                                Subject.ClassSubject(
                                    fqName = "com.example.ui.OrderActivity",
                                    simpleName = "OrderActivity",
                                ),
                            sourceLocation =
                                SourceLocation(
                                    filePath = "src/main/kotlin/Order.kt",
                                    line = 10,
                                    column = 5,
                                ),
                            isSuppressed = false,
                        ),
                        ReportViolation(
                            ruleId = "layers-check",
                            severity = Severity.WARNING,
                            message = "Legacy violation",
                            subject =
                                Subject.ClassSubject(
                                    fqName = "com.example.domain.LegacyHelper",
                                    simpleName = "LegacyHelper",
                                ),
                            isSuppressed = true,
                            suppressionReason = "Legacy component exception",
                            suppressionKind = "in_source",
                        ),
                    ),
            )

        val encoded = json.encodeToString(report)
        assertTrue(encoded.contains("\"schemaVersion\": \"1.0.0\""))
        assertTrue(encoded.contains("\"totalViolations\": 2"))
        assertTrue(encoded.contains("\"isSuppressed\": true"))
        assertTrue(encoded.contains("\"suppressionReason\": \"Legacy component exception\""))
        assertTrue(encoded.contains("\"suppressionKind\": \"in_source\""))

        val decoded = json.decodeFromString<KontureJsonReport>(encoded)
        assertEquals(report.schemaVersion, decoded.schemaVersion)
        assertEquals(report.tool.name, decoded.tool.name)
        assertEquals(2, decoded.summary.totalViolations)
        assertEquals(2, decoded.violations.size)
        assertEquals(true, decoded.violations[1].isSuppressed)
        assertEquals("Legacy component exception", decoded.violations[1].suppressionReason)
        assertEquals("in_source", decoded.violations[1].suppressionKind)
    }

    @Test
    fun `empty report serialization is valid`() {
        val report =
            KontureJsonReport(
                timestamp = "2026-08-20T10:00:00Z",
                summary =
                    ReportSummary(
                        totalRules = 0,
                        passedRules = 0,
                        failedRules = 0,
                        totalViolations = 0,
                        errorCount = 0,
                        warningCount = 0,
                        infoCount = 0,
                    ),
            )

        val encoded = json.encodeToString(report)
        val decoded = json.decodeFromString<KontureJsonReport>(encoded)
        assertEquals(0, decoded.violations.size)
        assertEquals(0, decoded.rules.size)
    }
}
