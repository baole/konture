/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.ViolationReport

/**
 * Formats a [ViolationReport] into a human-readable diagnostic error message.
 */
public object HumanReadableViolationFormatter {
    /**
     * Formats the given [report] into a structured, human-readable string.
     *
     * @param report The violation report to format.
     * @return Formatted human-readable diagnostic message.
     */
    public fun format(report: ViolationReport): String {
        if (report.violations.isEmpty()) return ""
        return buildString {
            appendLine("✗ Rule: ${report.ruleId}")
            appendLine()
            appendLine("${report.violations.size} violation(s) found:")
            appendLine()
            report.violations.forEachIndexed { index, v ->
                appendLine("${index + 1}. ${v.subject.name}")
                val cleanMessage = v.message.substringBefore(" (at ").trim()
                appendLine("   Message: $cleanMessage")
                val loc = v.sourceLocation ?: v.subject.location
                if (loc != null) {
                    appendLine("   File: ${loc.filePath}:${loc.line ?: 1}")
                }
                if (index < report.violations.size - 1) {
                    appendLine()
                }
            }
        }.trimEnd()
    }
}
