/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.report

import io.github.baole.konture.core.KontureConstants
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import kotlinx.serialization.Serializable

/**
 * Root data structure for Konture's native JSON architecture verification report.
 *
 * @property schemaVersion Semantic schema version of this report.
 * @property tool Information about the Konture analyzer generating this report.
 * @property timestamp ISO-8601 UTC timestamp of the execution run.
 * @property summary High-level summary metrics of the test execution.
 * @property rules List of all architectural rules evaluated during this execution.
 * @property violations List of architectural violations detected.
 */
@Serializable
public data class KontureJsonReport(
    val schemaVersion: String = "1.0.0",
    val tool: ToolMetadata = ToolMetadata(),
    val timestamp: String,
    val summary: ReportSummary,
    val rules: List<ReportRule> = emptyList(),
    val violations: List<ReportViolation> = emptyList(),
)

/**
 * Metadata identifying the Konture tool and runtime.
 *
 * @property name Tool identifier name.
 * @property version Current version of Konture.
 * @property informationUri Public homepage/documentation URL of Konture.
 */
@Serializable
public data class ToolMetadata(
    val name: String = "Konture",
    val version: String = KontureConstants.VERSION,
    val informationUri: String = "https://github.com/baole/konture",
)

/**
 * Aggregated summary statistics for a Konture execution run.
 *
 * @property totalRules Total count of evaluated rules.
 * @property passedRules Count of rules that had 0 unsuppressed violations.
 * @property failedRules Count of rules with 1 or more unsuppressed violations.
 * @property totalViolations Total count of violations reported (unsuppressed + suppressed).
 * @property errorCount Count of violations with [Severity.ERROR].
 * @property warningCount Count of violations with [Severity.WARNING].
 * @property infoCount Count of violations with [Severity.INFO].
 */
@Serializable
public data class ReportSummary(
    val totalRules: Int,
    val passedRules: Int,
    val failedRules: Int,
    val totalViolations: Int,
    val errorCount: Int,
    val warningCount: Int,
    val infoCount: Int,
)

/**
 * Description of an evaluated architectural rule.
 *
 * @property id Unique rule identifier.
 * @property description Optional descriptive text or purpose of the rule.
 * @property severity Configured default severity level of the rule.
 * @property tags Categorization tags attached to the rule.
 */
@Serializable
public data class ReportRule(
    val id: String,
    val description: String? = null,
    val severity: Severity = Severity.ERROR,
    val tags: Set<String> = emptySet(),
)

/**
 * Detailed representation of an individual architectural violation for JSON reports.
 *
 * @property ruleId Identifier of the rule violated.
 * @property severity Severity level of the violation.
 * @property message Descriptive failure message.
 * @property subject The subject element committing the violation.
 * @property target Optional target subject involved in the violation.
 * @property sourceLocation Optional source file location of the subject.
 * @property dependencyPath Sequence of subjects representing the dependency or invocation path.
 * @property isSuppressed True if this violation was matched against an architecture baseline.
 */
@Serializable
public data class ReportViolation(
    val ruleId: String,
    val severity: Severity,
    val message: String,
    val subject: Subject,
    val target: Subject? = null,
    val sourceLocation: SourceLocation? = null,
    val dependencyPath: List<Subject> = emptyList(),
    val isSuppressed: Boolean = false,
)
