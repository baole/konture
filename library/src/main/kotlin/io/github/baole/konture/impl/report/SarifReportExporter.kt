/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.report

import io.github.baole.konture.Konture
import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.report.sarif.SarifArtifactLocation
import io.github.baole.konture.core.report.sarif.SarifCodeFlow
import io.github.baole.konture.core.report.sarif.SarifDriver
import io.github.baole.konture.core.report.sarif.SarifLocation
import io.github.baole.konture.core.report.sarif.SarifMessage
import io.github.baole.konture.core.report.sarif.SarifPhysicalLocation
import io.github.baole.konture.core.report.sarif.SarifRegion
import io.github.baole.konture.core.report.sarif.SarifReport
import io.github.baole.konture.core.report.sarif.SarifReportingConfiguration
import io.github.baole.konture.core.report.sarif.SarifResult
import io.github.baole.konture.core.report.sarif.SarifRule
import io.github.baole.konture.core.report.sarif.SarifRuleProperties
import io.github.baole.konture.core.report.sarif.SarifRun
import io.github.baole.konture.core.report.sarif.SarifSuppression
import io.github.baole.konture.core.report.sarif.SarifThreadFlow
import io.github.baole.konture.core.report.sarif.SarifThreadFlowLocation
import io.github.baole.konture.core.report.sarif.SarifTool
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Exporter responsible for generating and writing SARIF v2.1.0 static analysis reports.
 */
public object SarifReportExporter {
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    /**
     * Serializes a [SarifReport] into a pretty-printed JSON string.
     */
    public fun exportToString(report: SarifReport): String = json.encodeToString(report)

    /**
     * Generates a [SarifReport] from accumulated rule evaluation results.
     */
    public fun generateReport(
        evaluations: List<ReportAccumulator.RuleEvaluation>,
        buildRoot: File? = null,
    ): SarifReport {
        val root = buildRoot ?: File(System.getProperty("user.dir"))

        val rulesMap = linkedMapOf<String, SarifRule>()
        val results = mutableListOf<SarifResult>()

        evaluations.forEach { eval ->
            val meta = eval.metadata
            val ruleId = eval.ruleId
            val defaultSeverity = meta?.severity ?: eval.unsuppressedViolations.firstOrNull()?.severity ?: Severity.ERROR

            if (!rulesMap.containsKey(ruleId)) {
                rulesMap[ruleId] =
                    SarifRule(
                        id = ruleId,
                        name = ruleId,
                        shortDescription = SarifMessage(text = meta?.description ?: ruleId),
                        defaultConfiguration = SarifReportingConfiguration(level = defaultSeverity.toSarifLevel()),
                        properties = SarifRuleProperties(tags = (meta?.tags ?: emptySet()).toList()),
                    )
            }

            fun addResult(
                v: Violation,
                isSuppressed: Boolean,
            ) {
                val loc = v.sourceLocation ?: v.subject.location
                val sarifLocations =
                    if (loc != null) {
                        val normalizedPath = JsonReportExporter.normalizePathString(loc.filePath, root)
                        listOf(
                            SarifLocation(
                                physicalLocation =
                                    SarifPhysicalLocation(
                                        artifactLocation =
                                            SarifArtifactLocation(
                                                uri = normalizedPath,
                                                uriBaseId = "%SRCROOT%",
                                            ),
                                        region =
                                            if (loc.line != null) {
                                                SarifRegion(
                                                    startLine = loc.line,
                                                    startColumn = loc.column,
                                                )
                                            } else {
                                                null
                                            },
                                    ),
                            ),
                        )
                    } else {
                        null
                    }

                val codeFlows =
                    if (v.target != null && v.target?.location != null && loc != null) {
                        val targetLoc = v.target?.location!!
                        val normalizedTarget = JsonReportExporter.normalizePathString(targetLoc.filePath, root)
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
                                                                                uri =
                                                                                    JsonReportExporter
                                                                                        .normalizePathString(
                                                                                            loc.filePath,
                                                                                            root,
                                                                                        ),
                                                                                uriBaseId = "%SRCROOT%",
                                                                            ),
                                                                        region =
                                                                            if (loc.line != null) {
                                                                                SarifRegion(
                                                                                    startLine = loc.line,
                                                                                    startColumn = loc.column,
                                                                                )
                                                                            } else {
                                                                                null
                                                                            },
                                                                    ),
                                                                message =
                                                                    SarifMessage(
                                                                        text = "Origin: ${v.subject.name}",
                                                                    ),
                                                            ),
                                                        importance = "essential",
                                                    ),
                                                    SarifThreadFlowLocation(
                                                        location =
                                                            SarifLocation(
                                                                physicalLocation =
                                                                    SarifPhysicalLocation(
                                                                        artifactLocation =
                                                                            SarifArtifactLocation(
                                                                                uri = normalizedTarget,
                                                                                uriBaseId = "%SRCROOT%",
                                                                            ),
                                                                        region =
                                                                            if (targetLoc.line != null) {
                                                                                SarifRegion(
                                                                                    startLine = targetLoc.line,
                                                                                    startColumn = targetLoc.column,
                                                                                )
                                                                            } else {
                                                                                null
                                                                            },
                                                                    ),
                                                                message =
                                                                    SarifMessage(
                                                                        text = "Target: ${v.target?.name}",
                                                                    ),
                                                            ),
                                                        importance = "essential",
                                                    ),
                                                ),
                                        ),
                                    ),
                            ),
                        )
                    } else {
                        null
                    }

                val suppressions =
                    if (isSuppressed) {
                        listOf(
                            SarifSuppression(
                                kind = "external",
                                status = "accepted",
                                justification = "Architecture baseline record",
                            ),
                        )
                    } else {
                        null
                    }

                results.add(
                    SarifResult(
                        ruleId = ruleId,
                        level = v.severity.toSarifLevel(),
                        message = SarifMessage(text = v.message),
                        locations = sarifLocations ?: emptyList(),
                        codeFlows = codeFlows,
                        suppressions = suppressions,
                    ),
                )
            }

            eval.unsuppressedViolations.forEach { addResult(it, isSuppressed = false) }
            eval.suppressedViolations.forEach { addResult(it, isSuppressed = true) }
        }

        val run =
            SarifRun(
                tool =
                    SarifTool(
                        driver =
                            SarifDriver(
                                name = "Konture",
                                version = KontureConstants.VERSION,
                                informationUri = "https://github.com/baole/konture",
                                rules = rulesMap.values.toList(),
                            ),
                    ),
                results = results,
            )

        return SarifReport(
            schema = "https://json.schemastore.org/sarif-2.1.0.json",
            version = "2.1.0",
            runs = listOf(run),
        )
    }

    /**
     * Writes the given [SarifReport] to [targetFile].
     */
    @Suppress("TooGenericExceptionCaught")
    public fun writeReport(
        report: SarifReport,
        targetFile: File = File(Konture.sarifReportPath),
    ) {
        try {
            targetFile.parentFile?.mkdirs()
            targetFile.writeText(exportToString(report))
        } catch (_: Exception) {
            // Ignore file write exceptions in restricted environments
        }
    }

    private fun Severity.toSarifLevel(): String =
        when (this) {
            Severity.ERROR -> "error"
            Severity.WARNING -> "warning"
            Severity.INFO -> "note"
        }
}
