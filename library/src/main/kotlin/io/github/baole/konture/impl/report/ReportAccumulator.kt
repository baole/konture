/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.report

import io.github.baole.konture.Konture
import io.github.baole.konture.OutputFormat
import io.github.baole.konture.core.model.RuleMetadata
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.impl.HtmlReportWriter
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Thread-safe accumulator for architectural rule evaluations and multi-sink report writer.
 *
 * Evaluations are accumulated across test executions. In parallel test runners,
 * calling [clear] should be reserved for isolated test suite setup/teardown.
 */
internal class ReportAccumulator {
    /**
     * Data class capturing the result of a single architecture rule evaluation.
     */
    data class RuleEvaluation(
        val ruleId: String,
        val metadata: RuleMetadata?,
        val unsuppressedViolations: List<Violation>,
        val suppressedViolations: List<Violation>,
        val passed: Boolean = unsuppressedViolations.isEmpty(),
    )

    private val evaluations = ConcurrentLinkedQueue<RuleEvaluation>()

    /**
     * Records a single rule evaluation.
     */
    fun recordEvaluation(
        ruleId: String,
        metadata: RuleMetadata?,
        unsuppressedViolations: List<Violation>,
        suppressedViolations: List<Violation>,
    ) {
        evaluations.add(
            RuleEvaluation(
                ruleId = ruleId,
                metadata = metadata,
                unsuppressedViolations = unsuppressedViolations,
                suppressedViolations = suppressedViolations,
            ),
        )
    }

    /**
     * Returns a snapshot list of all accumulated rule evaluations.
     */
    fun getAllEvaluations(): List<RuleEvaluation> = evaluations.toList()

    /**
     * Clears all accumulated evaluations.
     */
    fun clear() {
        evaluations.clear()
    }

    /**
     * Generates and writes configured reports (JSON, SARIF, HTML) based on output format and system properties.
     */
    fun writeReports(buildRoot: File? = null) {
        val currentEvaluations = getAllEvaluations()
        val currentFormat = Konture.outputFormat

        val state = io.github.baole.konture.impl.KontureRuntimeStateProvider.currentState

        val isJsonEnabled =
            currentFormat == OutputFormat.JSON ||
                System.getProperty(Konture.PROPERTY_REPORT_JSON_PATH) != null ||
                (System.getProperty(Konture.PROPERTY_REPORT_PATH)?.endsWith(".json") == true) ||
                state.jsonReportPath != io.github.baole.konture.core.KontureConstants.DEFAULT_JSON_REPORT_PATH ||
                state.reportPath.endsWith(".json")
        if (isJsonEnabled) {
            val jsonReport = JsonReportExporter.generateReport(currentEvaluations, buildRoot)
            JsonReportExporter.writeReport(jsonReport, File(Konture.jsonReportPath))
        }

        val isSarifEnabled =
            currentFormat == OutputFormat.SARIF ||
                System.getProperty(Konture.PROPERTY_REPORT_SARIF_PATH) != null ||
                (System.getProperty(Konture.PROPERTY_REPORT_PATH)?.endsWith(".sarif") == true) ||
                state.sarifReportPath != io.github.baole.konture.core.KontureConstants.DEFAULT_SARIF_REPORT_PATH ||
                state.reportPath.endsWith(".sarif")
        if (isSarifEnabled) {
            val sarifReport = SarifReportExporter.generateReport(currentEvaluations, buildRoot)
            SarifReportExporter.writeReport(sarifReport, File(Konture.sarifReportPath))
        }

        val isHtmlEnabled =
            currentFormat == OutputFormat.HTML ||
                System.getProperty(Konture.PROPERTY_REPORT_HTML_PATH) != null ||
                (System.getProperty(Konture.PROPERTY_REPORT_PATH)?.endsWith(".html") == true) ||
                state.htmlReportPath != io.github.baole.konture.core.KontureConstants.DEFAULT_HTML_REPORT_PATH ||
                state.reportPath.endsWith(".html")
        if (isHtmlEnabled) {
            val unsuppressed = currentEvaluations.flatMap { it.unsuppressedViolations }
            val htmlViolationReport =
                ViolationReport(
                    ruleId = currentEvaluations.lastOrNull()?.ruleId ?: "konture",
                    violations = unsuppressed,
                )
            HtmlReportWriter.writeReport(
                htmlViolationReport,
                targetFile = File(Konture.htmlReportPath),
                projectRoot = buildRoot,
            )
        }
    }

    companion object {
        private val globalInstance = ReportAccumulator()

        fun getInstance(): ReportAccumulator = globalInstance

        fun recordEvaluation(
            ruleId: String,
            metadata: RuleMetadata?,
            unsuppressedViolations: List<Violation>,
            suppressedViolations: List<Violation>,
        ) {
            globalInstance.recordEvaluation(ruleId, metadata, unsuppressedViolations, suppressedViolations)
        }

        fun getAllEvaluations(): List<RuleEvaluation> = globalInstance.getAllEvaluations()

        fun clear() {
            globalInstance.clear()
        }

        fun writeReports(buildRoot: File? = null) {
            globalInstance.writeReports(buildRoot)
        }
    }
}
