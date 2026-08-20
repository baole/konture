/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.report

import io.github.baole.konture.core.report.sarif.SarifArtifactLocation
import io.github.baole.konture.core.report.sarif.SarifCodeFlow
import io.github.baole.konture.core.report.sarif.SarifDriver
import io.github.baole.konture.core.report.sarif.SarifLocation
import io.github.baole.konture.core.report.sarif.SarifMessage
import io.github.baole.konture.core.report.sarif.SarifPhysicalLocation
import io.github.baole.konture.core.report.sarif.SarifRegion
import io.github.baole.konture.core.report.sarif.SarifReport
import io.github.baole.konture.core.report.sarif.SarifResult
import io.github.baole.konture.core.report.sarif.SarifRule
import io.github.baole.konture.core.report.sarif.SarifRuleProperties
import io.github.baole.konture.core.report.sarif.SarifRun
import io.github.baole.konture.core.report.sarif.SarifSuppression
import io.github.baole.konture.core.report.sarif.SarifThreadFlow
import io.github.baole.konture.core.report.sarif.SarifThreadFlowLocation
import io.github.baole.konture.core.report.sarif.SarifTool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SarifReportTest {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @Test
    fun `serialize and deserialize SarifReport roundtrip`() {
        val report =
            SarifReport(
                runs =
                    listOf(
                        SarifRun(
                            tool =
                                SarifTool(
                                    driver =
                                        SarifDriver(
                                            rules =
                                                listOf(
                                                    SarifRule(
                                                        id = "rule-001",
                                                        shortDescription =
                                                            SarifMessage(
                                                                text = "Must not depend on feature",
                                                            ),
                                                        properties =
                                                            SarifRuleProperties(
                                                                tags = listOf("architecture", "boundaries"),
                                                            ),
                                                    ),
                                                ),
                                        ),
                                ),
                            results =
                                listOf(
                                    SarifResult(
                                        ruleId = "rule-001",
                                        level = "error",
                                        message = SarifMessage(text = "Forbidden dependency detected"),
                                        locations =
                                            listOf(
                                                SarifLocation(
                                                    physicalLocation =
                                                        SarifPhysicalLocation(
                                                            artifactLocation =
                                                                SarifArtifactLocation(
                                                                    uri = "core/src/main/kotlin/Core.kt",
                                                                    uriBaseId = "%SRCROOT%",
                                                                ),
                                                            region =
                                                                SarifRegion(
                                                                    startLine = 12,
                                                                    startColumn = 4,
                                                                ),
                                                        ),
                                                ),
                                            ),
                                        codeFlows =
                                            listOf(
                                                SarifCodeFlow(
                                                    threadFlows =
                                                        listOf(
                                                            SarifThreadFlow(
                                                                locations =
                                                                    listOf(
                                                                        SarifThreadFlowLocation(
                                                                            location =
                                                                                SarifLocation(
                                                                                    physicalLocation =
                                                                                        SarifPhysicalLocation(
                                                                                            artifactLocation =
                                                                                                SarifArtifactLocation(
                                                                                                    uri = "core/src/main/kotlin/Core.kt",
                                                                                                ),
                                                                                        ),
                                                                                ),
                                                                            importance = "essential",
                                                                        ),
                                                                    ),
                                                            ),
                                                        ),
                                                ),
                                            ),
                                        suppressions =
                                            listOf(
                                                SarifSuppression(
                                                    kind = "external",
                                                    status = "accepted",
                                                    justification = "Architecture baseline record",
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    ),
            )

        val encoded = json.encodeToString(report)
        assertTrue(encoded.contains("\"\$schema\": \"https://json.schemastore.org/sarif-2.1.0.json\""))
        assertTrue(encoded.contains("\"version\": \"2.1.0\""))
        assertTrue(encoded.contains("\"uriBaseId\": \"%SRCROOT%\""))
        assertTrue(encoded.contains("\"suppressions\""))

        val decoded = json.decodeFromString<SarifReport>(encoded)
        assertEquals("2.1.0", decoded.version)
        assertEquals(1, decoded.runs.size)
        val run = decoded.runs.first()
        assertEquals("Konture", run.tool.driver.name)
        assertEquals(1, run.results.size)
        val result = run.results.first()
        assertEquals("rule-001", result.ruleId)
        assertEquals(12, result.locations.first().physicalLocation.region?.startLine)
        assertNotNull(result.codeFlows)
        assertEquals(1, result.suppressions?.size)
    }

    @Test
    fun `empty SARIF run is valid`() {
        val report =
            SarifReport(
                runs =
                    listOf(
                        SarifRun(
                            tool =
                                SarifTool(
                                    driver =
                                        SarifDriver(
                                            rules = emptyList(),
                                        ),
                                ),
                            results = emptyList(),
                        ),
                    ),
            )

        val encoded = json.encodeToString(report)
        val decoded = json.decodeFromString<SarifReport>(encoded)
        assertTrue(decoded.runs.first().results.isEmpty())
    }
}
