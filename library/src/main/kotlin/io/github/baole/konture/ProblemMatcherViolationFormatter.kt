/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.ViolationReport

/**
 * Formatter for building IDE and CI problem-matcher compatible error outputs from architecture violation reports.
 */
public object ProblemMatcherViolationFormatter {
    /**
     * Formats a [ViolationReport] into a single-line per violation string matching standard compiler
     * and IDE problem matcher formats (`path:line:col: Konture [ruleId]: message`).
     *
     * @param report The violation report to format.
     * @return Formatted problem-matcher string representation of the report.
     */
    public fun format(report: ViolationReport): String {
        if (report.violations.isEmpty()) return ""

        return report.violations.joinToString("\n") { v ->
            val loc = v.sourceLocation ?: v.subject.location
            val path = loc?.filePath?.ifBlank { null } ?: "unknown"
            val line = loc?.line ?: 1
            val col = loc?.column ?: 1
            val activeRuleId = v.ruleId.ifBlank { report.ruleId }
            val cleanMessage = v.message.substringBeforeLast(" (at ").trim()
            "$path:$line:$col: Konture [$activeRuleId]: $cleanMessage"
        }
    }
}
