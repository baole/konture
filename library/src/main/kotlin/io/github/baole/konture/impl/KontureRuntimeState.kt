/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.OutputFormat
import io.github.baole.konture.ProgrammaticSuppression
import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.core.model.RuleMetadata
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.impl.report.ReportAccumulator
import java.util.Locale

@Suppress("LongParameterList")
internal class KontureRuntimeState(
    val baselinePath: String = "konture-baseline.json",
    val generateBaseline: Boolean = false,
    val projectGraph: ProjectGraph? = null,
    val baselineManager: BaselineManager = BaselineManager(),
    val locale: Locale = Locale.ENGLISH,
    val isLocaleOverridden: Boolean = false,
    val currentRuleMetadata: RuleMetadata? = null,
    val outputFormat: OutputFormat = OutputFormat.HUMAN,
    val reportPath: String = KontureConstants.DEFAULT_HTML_REPORT_PATH,
    val jsonReportPath: String = KontureConstants.DEFAULT_JSON_REPORT_PATH,
    val sarifReportPath: String = KontureConstants.DEFAULT_SARIF_REPORT_PATH,
    val htmlReportPath: String = KontureConstants.DEFAULT_HTML_REPORT_PATH,
    val activeProgrammaticSuppressions: List<ProgrammaticSuppression> = emptyList(),
    val failOnSeverity: Severity? = Severity.ERROR,
    val isFailOnSeverityOverridden: Boolean = false,
    val reportResolvedViolations: Boolean = KontureConstants.DEFAULT_REPORT_RESOLVED_VIOLATIONS,
    val isReportResolvedViolationsOverridden: Boolean = false,
    val failOnResolvedViolations: Boolean = KontureConstants.DEFAULT_FAIL_ON_RESOLVED_VIOLATIONS,
    val isFailOnResolvedViolationsOverridden: Boolean = false,
) {
    val projectGraphLoader: ProjectGraphLoader = ProjectGraphLoader()

    fun copy(
        baselinePath: String = this.baselinePath,
        generateBaseline: Boolean = this.generateBaseline,
        projectGraph: ProjectGraph? = this.projectGraph,
        locale: Locale = this.locale,
        isLocaleOverridden: Boolean = this.isLocaleOverridden,
        currentRuleMetadata: RuleMetadata? = this.currentRuleMetadata,
        outputFormat: OutputFormat = this.outputFormat,
        reportPath: String = this.reportPath,
        jsonReportPath: String = this.jsonReportPath,
        sarifReportPath: String = this.sarifReportPath,
        htmlReportPath: String = this.htmlReportPath,
        activeProgrammaticSuppressions: List<ProgrammaticSuppression> = this.activeProgrammaticSuppressions,
        failOnSeverity: Severity? = this.failOnSeverity,
        isFailOnSeverityOverridden: Boolean = this.isFailOnSeverityOverridden,
        reportResolvedViolations: Boolean = this.reportResolvedViolations,
        isReportResolvedViolationsOverridden: Boolean = this.isReportResolvedViolationsOverridden,
        failOnResolvedViolations: Boolean = this.failOnResolvedViolations,
        isFailOnResolvedViolationsOverridden: Boolean = this.isFailOnResolvedViolationsOverridden,
    ): KontureRuntimeState {
        return KontureRuntimeState(
            baselinePath = baselinePath,
            generateBaseline = generateBaseline,
            projectGraph = projectGraph,
            baselineManager = this.baselineManager,
            locale = locale,
            isLocaleOverridden = isLocaleOverridden,
            currentRuleMetadata = currentRuleMetadata,
            outputFormat = outputFormat,
            reportPath = reportPath,
            jsonReportPath = jsonReportPath,
            sarifReportPath = sarifReportPath,
            htmlReportPath = htmlReportPath,
            activeProgrammaticSuppressions = activeProgrammaticSuppressions,
            failOnSeverity = failOnSeverity,
            isFailOnSeverityOverridden = isFailOnSeverityOverridden,
            reportResolvedViolations = reportResolvedViolations,
            isReportResolvedViolationsOverridden = isReportResolvedViolationsOverridden,
            failOnResolvedViolations = failOnResolvedViolations,
            isFailOnResolvedViolationsOverridden = isFailOnResolvedViolationsOverridden,
        )
    }
}

internal object KontureRuntimeStateProvider {
    private val threadLocalState = ThreadLocal.withInitial { KontureRuntimeState() }

    var currentState: KontureRuntimeState
        get() = threadLocalState.get()
        set(value) {
            threadLocalState.set(value)
        }

    /**
     * Resets the current thread-local state to default values and clears the shared report accumulator.
     * Note: This method is intended for test harness setup and teardown.
     */
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun reset() {
        try {
            threadLocalState.get()?.baselineManager?.resetForTest()
            ReportAccumulator.clear()
        } catch (e: Exception) {
            // Ignore
        }
        threadLocalState.set(KontureRuntimeState())
    }

    inline fun <T> runWithState(
        state: KontureRuntimeState,
        block: () -> T,
    ): T {
        val previous = currentState
        currentState = state
        try {
            return block()
        } finally {
            currentState = previous
        }
    }
}
