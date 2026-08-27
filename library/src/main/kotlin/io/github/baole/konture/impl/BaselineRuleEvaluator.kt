/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.HtmlViolationFormatter
import io.github.baole.konture.HumanReadableViolationFormatter
import io.github.baole.konture.Konture
import io.github.baole.konture.OutputFormat
import io.github.baole.konture.ProblemMatcherViolationFormatter
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SuppressionKind
import io.github.baole.konture.core.model.SuppressionMetadata
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.report.ReportAccumulator
import java.util.Locale

internal class BaselineRuleEvaluator(
    private val baselineManager: BaselineManager,
) {
    fun handleViolations(
        violations: List<String>,
        header: String,
    ) {
        baselineManager.captureContextSnapshot()
        if (violations.isEmpty()) return

        val testLoc = TestLocationFinder.findTestLocation()
        val testClass = testLoc?.className ?: "UnknownTest"
        val testMethod = testLoc?.methodName ?: "unknownMethod"
        val buildRoot = baselineManager.buildRoot

        val normalizedViolations =
            violations.map {
                val normMsg = BaselineNormalizer.normalize(it, buildRoot)
                val (location, cleanMsg) = BaselineNormalizer.parseLocationAndMessage(normMsg, buildRoot)
                Pair(
                    FlatBaselineViolation(
                        testClass = testClass,
                        testMethod = testMethod,
                        location = location,
                        message = cleanMsg,
                    ),
                    normMsg,
                )
            }

        baselineManager.evaluatedViolations.addAll(normalizedViolations.map { it.first })

        if (baselineManager.generateBaseline) {
            baselineManager.recordedViolations.addAll(normalizedViolations.map { it.first })
            KontureLogger.log(
                LogLevel.INFO,
                "Recorded ${violations.size} violations to baseline (current total recorded in JVM: ${baselineManager.recordedViolations.size})",
            )
            return
        }

        val newViolations =
            normalizedViolations.filter { (norm, _) ->
                !baselineManager.existingViolations.contains(norm)
            }.map { it.first }

        if (newViolations.isNotEmpty()) {
            val message =
                buildString {
                    appendLine(header)
                    newViolations.forEach {
                        if (it.location != null) {
                            appendLine("  - ${it.message} (at ${it.location})")
                        } else {
                            appendLine("  - ${it.message}")
                        }
                    }
                    append(getMessage("rule.violationCount", newViolations.size))
                }
            throw AssertionError(message)
        }
    }

    fun checkRule(
        violationHeader: String,
        runCheck: (MutableList<String>) -> Unit,
    ) {
        val userLocale = Konture.locale

        val englishViolations = mutableListOf<String>()
        Konture.locale = Locale.ENGLISH
        try {
            runCheck(englishViolations)
        } finally {
            Konture.locale = userLocale
        }

        if (englishViolations.isEmpty()) return

        val unmatchedIndices = getNewViolationIndices(englishViolations)

        if (unmatchedIndices.isNotEmpty() || baselineManager.generateBaseline) {
            if (baselineManager.generateBaseline) {
                handleViolations(englishViolations, violationHeader)
            } else {
                val localizedViolations = mutableListOf<String>()
                runCheck(localizedViolations)

                val unmatchedLocalized =
                    unmatchedIndices.map { index ->
                        if (index < localizedViolations.size) localizedViolations[index] else englishViolations[index]
                    }

                throwNewViolations(unmatchedLocalized, violationHeader)
            }
        }
    }

    fun checkRuleReport(
        ruleId: String,
        violationHeader: String,
        runCheckReport: (MutableList<Violation>) -> Unit,
    ): ViolationReport {
        val userLocale = Konture.locale

        val englishViolations = mutableListOf<Violation>()
        Konture.locale = Locale.ENGLISH
        try {
            runCheckReport(englishViolations)
        } finally {
            Konture.locale = userLocale
        }

        val report = ViolationReport(ruleId = ruleId, violations = englishViolations)
        val englishStrings = englishViolations.map { it.message }
        val unmatchedIndices = getNewViolationIndices(englishStrings)

        val unsuppressedViolations = mutableListOf<Violation>()
        val suppressedViolations = mutableListOf<Violation>()
        if (baselineManager.generateBaseline) {
            suppressedViolations.addAll(englishViolations)
        } else {
            englishViolations.forEachIndexed { index, violation ->
                if (violation.isSuppressed) {
                    suppressedViolations.add(violation)
                } else if (!unmatchedIndices.contains(index)) {
                    val baselineSuppression =
                        SuppressionMetadata(
                            kind = SuppressionKind.BASELINE,
                            reason = "Suppressed by baseline entry",
                        )
                    suppressedViolations.add(
                        violation.copy(
                            isSuppressed = true,
                            suppression = baselineSuppression,
                        ),
                    )
                } else {
                    unsuppressedViolations.add(violation)
                }
            }
        }

        val ruleMetadata = KontureRuntimeStateProvider.currentState.currentRuleMetadata
        ReportAccumulator.recordEvaluation(
            ruleId = ruleId,
            metadata = ruleMetadata,
            unsuppressedViolations = unsuppressedViolations,
            suppressedViolations = suppressedViolations,
        )
        ReportAccumulator.writeReports(baselineManager.buildRoot)

        val failThreshold = Konture.failOnSeverity
        val nonSuppressedUnmatchedIndices =
            if (baselineManager.generateBaseline) {
                emptyList()
            } else {
                unmatchedIndices.filter { index -> !englishViolations[index].isSuppressed }
            }

        val failingIndices =
            if (failThreshold == null) {
                emptyList()
            } else {
                nonSuppressedUnmatchedIndices.filter { index ->
                    englishViolations[index].severity.ordinal >= failThreshold.ordinal
                }
            }

        val subThresholdIndices =
            if (baselineManager.generateBaseline) {
                emptyList()
            } else {
                nonSuppressedUnmatchedIndices.filter { index ->
                    failThreshold == null || englishViolations[index].severity.ordinal < failThreshold.ordinal
                }
            }

        if (failingIndices.isNotEmpty() || subThresholdIndices.isNotEmpty() || baselineManager.generateBaseline) {
            if (baselineManager.generateBaseline) {
                val unsuppressedEnglish = englishViolations.filter { !it.isSuppressed }.map { it.message }
                handleViolations(unsuppressedEnglish, violationHeader)
            } else {
                val localizedViolations = mutableListOf<Violation>()
                runCheckReport(localizedViolations)

                for (index in subThresholdIndices) {
                    val v = if (index < localizedViolations.size) localizedViolations[index] else englishViolations[index]
                    val logLevel =
                        when (v.severity) {
                            Severity.ERROR -> LogLevel.ERROR
                            Severity.WARNING -> LogLevel.WARNING
                            Severity.INFO -> LogLevel.INFO
                        }
                    val activeRuleId = v.ruleId.ifBlank { ruleId }
                    val logMessage =
                        getMessage("diagnostic.subThresholdViolation", v.severity.name, activeRuleId, v.message)
                    KontureLogger.log(logLevel, logMessage)
                }

                if (failingIndices.isNotEmpty()) {
                    val failingLocalized =
                        failingIndices.map { index ->
                            if (index < localizedViolations.size) localizedViolations[index] else englishViolations[index]
                        }

                    throwNewViolationsReport(ruleId, failingLocalized, violationHeader)
                }
            }
        }

        return report
    }

    private fun getNewViolationIndices(violations: List<String>): List<Int> {
        baselineManager.captureContextSnapshot()
        if (violations.isEmpty()) return emptyList()

        val testLoc = TestLocationFinder.findTestLocation()
        val testClass = testLoc?.className ?: "UnknownTest"
        val testMethod = testLoc?.methodName ?: "unknownMethod"
        val buildRoot = baselineManager.buildRoot

        val indices = mutableListOf<Int>()
        violations.forEachIndexed { index, violation ->
            val normMsg = BaselineNormalizer.normalize(violation, buildRoot)
            val (location, cleanMsg) = BaselineNormalizer.parseLocationAndMessage(normMsg, buildRoot)
            val norm =
                FlatBaselineViolation(
                    testClass = testClass,
                    testMethod = testMethod,
                    location = location,
                    message = cleanMsg,
                )
            baselineManager.evaluatedViolations.add(norm)
            if (!baselineManager.existingViolations.contains(norm)) {
                indices.add(index)
            }
        }
        return indices
    }

    private fun throwNewViolationsReport(
        ruleId: String,
        unmatchedViolations: List<Violation>,
        violationHeader: String? = null,
    ) {
        if (unmatchedViolations.isEmpty()) return
        val report = ViolationReport(ruleId = ruleId, violations = unmatchedViolations)
        val message =
            when (Konture.outputFormat) {
                OutputFormat.PROBLEM_MATCHER -> ProblemMatcherViolationFormatter.format(report)
                OutputFormat.HTML -> HtmlViolationFormatter.format(report, customHeader = violationHeader)
                OutputFormat.HUMAN, OutputFormat.JSON, OutputFormat.SARIF ->
                    HumanReadableViolationFormatter.format(report, customHeader = violationHeader)
            }
        throw AssertionError(message)
    }

    private fun throwNewViolations(
        unmatchedLocalized: List<String>,
        header: String,
    ) {
        if (unmatchedLocalized.isEmpty()) return
        val buildRoot = baselineManager.buildRoot
        val message =
            buildString {
                appendLine(header)
                unmatchedLocalized.forEach { rawViolation ->
                    val normMsg = BaselineNormalizer.normalize(rawViolation, buildRoot)
                    val (location, cleanMsg) = BaselineNormalizer.parseLocationAndMessage(normMsg, buildRoot)
                    if (location != null) {
                        appendLine("  - $cleanMsg (at $location)")
                    } else {
                        appendLine("  - $cleanMsg")
                    }
                }
                append(getMessage("rule.violationCount", unmatchedLocalized.size))
            }
        throw AssertionError(message)
    }
}
