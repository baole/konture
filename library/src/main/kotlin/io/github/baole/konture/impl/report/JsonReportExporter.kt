/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.report

import io.github.baole.konture.Konture
import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.report.KontureJsonReport
import io.github.baole.konture.core.report.ReportRule
import io.github.baole.konture.core.report.ReportSummary
import io.github.baole.konture.core.report.ReportViolation
import io.github.baole.konture.core.report.ToolMetadata
import java.io.File
import java.time.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Exporter responsible for generating and writing native Konture JSON reports.
 */
internal object JsonReportExporter {
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    /**
     * Serializes a [KontureJsonReport] into a pretty-printed JSON string.
     */
    fun exportToString(report: KontureJsonReport): String = json.encodeToString(report)

    /**
     * Generates a [KontureJsonReport] from accumulated rule evaluation results.
     */
    fun generateReport(
        evaluations: List<ReportAccumulator.RuleEvaluation>,
        buildRoot: File? = null,
    ): KontureJsonReport {
        val root = buildRoot ?: File(System.getProperty("user.dir"))

        val rulesMap = linkedMapOf<String, ReportRule>()
        val reportViolations = mutableListOf<ReportViolation>()

        var errorCount = 0
        var warningCount = 0
        var infoCount = 0

        evaluations.forEach { eval ->
            val meta = eval.metadata
            val ruleId = eval.ruleId
            val severity =
                meta?.severity
                    ?: eval.unsuppressedViolations.firstOrNull()?.severity
                    ?: eval.suppressedViolations.firstOrNull()?.severity
                    ?: Severity.ERROR

            if (!rulesMap.containsKey(ruleId)) {
                rulesMap[ruleId] =
                    ReportRule(
                        id = ruleId,
                        description = meta?.description ?: ruleId,
                        severity = severity,
                        tags = meta?.tags ?: emptySet(),
                    )
            }

            fun addViolation(
                v: Violation,
                isSuppressed: Boolean,
            ) {
                if (!isSuppressed) {
                    when (v.severity) {
                        Severity.ERROR -> errorCount++
                        Severity.WARNING -> warningCount++
                        Severity.INFO -> infoCount++
                    }
                }
                reportViolations.add(
                    ReportViolation(
                        ruleId = ruleId,
                        severity = v.severity,
                        message = v.message,
                        subject = normalizeSubject(v.subject, root),
                        target = v.target?.let { normalizeSubject(it, root) },
                        sourceLocation = normalizeLocation(v.sourceLocation ?: v.subject.location, root),
                        dependencyPath = v.dependencyPath.map { normalizeSubject(it, root) },
                        isSuppressed = isSuppressed,
                    ),
                )
            }

            eval.unsuppressedViolations.forEach { addViolation(it, isSuppressed = false) }
            eval.suppressedViolations.forEach { addViolation(it, isSuppressed = true) }
        }

        val totalRules = evaluations.size
        val failedRules = evaluations.count { !it.passed }
        val passedRules = totalRules - failedRules

        val summary =
            ReportSummary(
                totalRules = totalRules,
                passedRules = passedRules,
                failedRules = failedRules,
                totalViolations = reportViolations.count { !it.isSuppressed },
                suppressedCount = reportViolations.count { it.isSuppressed },
                errorCount = errorCount,
                warningCount = warningCount,
                infoCount = infoCount,
            )

        return KontureJsonReport(
            schemaVersion = "1.0.0",
            tool = ToolMetadata(name = "Konture", version = KontureConstants.VERSION),
            timestamp = Instant.now().toString(),
            summary = summary,
            rules = rulesMap.values.toList(),
            violations = reportViolations,
        )
    }

    /**
     * Writes the given [KontureJsonReport] to [targetFile].
     */
    @Suppress("TooGenericExceptionCaught")
    fun writeReport(
        report: KontureJsonReport,
        targetFile: File = File(Konture.jsonReportPath),
    ) {
        ReportFileUtil.writeAtomically(targetFile, exportToString(report))
    }

    internal fun normalizeSubject(
        subject: Subject,
        root: File,
    ): Subject {
        return when (subject) {
            is Subject.ClassSubject -> subject.copy(location = normalizeLocation(subject.location, root))
            is Subject.FunctionSubject -> subject.copy(location = normalizeLocation(subject.location, root))
            is Subject.ModuleSubject -> subject.copy(location = normalizeLocation(subject.location, root))
            is Subject.CustomSubject -> subject.copy(location = normalizeLocation(subject.location, root))
        }
    }

    internal fun normalizeLocation(
        loc: SourceLocation?,
        root: File,
    ): SourceLocation? {
        if (loc == null) return null
        val path = loc.filePath
        val normalizedPath = normalizePathString(path, root)
        return loc.copy(filePath = normalizedPath)
    }

    internal fun normalizePathString(
        path: String,
        root: File,
    ): String {
        val file = File(path)
        val relativePath =
            if (file.isAbsolute) {
                try {
                    file.absoluteFile.relativeTo(root.absoluteFile).path
                } catch (_: Exception) {
                    file.path
                }
            } else {
                path
            }
        return relativePath.replace('\\', '/')
    }
}
