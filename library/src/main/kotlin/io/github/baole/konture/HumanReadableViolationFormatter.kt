/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.i18n.getMessage

/**
 * Formatter for building human-readable error messages from architecture violation reports.
 */
public object HumanReadableViolationFormatter {

    /**
     * Formats a [ViolationReport] into a human-readable string suitable for test failure outputs.
     *
     * @param report The violation report to format.
     * @param customHeader Optional custom header or description to include below the rule header.
     * @return Formatted human-readable string representation of the report.
     */
    @JvmOverloads
    public fun format(report: ViolationReport, customHeader: String? = null): String {
        if (report.violations.isEmpty()) return ""

        val metaDescription = report.violations.firstOrNull()?.metadata?.description
        val header = customHeader?.takeIf { it.isNotBlank() } ?: metaDescription?.takeIf { it.isNotBlank() }

        return buildString {
            appendLine(getMessage("report.ruleHeader", report.ruleId))
            if (header != null) {
                appendLine(header)
            }
            appendLine()
            appendLine(getMessage("report.violationsFound", report.violations.size))
            appendLine()
            report.violations.forEachIndexed { index, v ->
                appendLine("${index + 1}. ${v.subject.name}")
                val cleanMessage = v.message.substringBeforeLast(" (at ").trim()
                appendLine("   ${getMessage("report.messageLabel", cleanMessage)}")
                val loc = v.sourceLocation ?: v.subject.location
                if (loc != null) {
                    val lineSuffix = if (loc.line != null) ":${loc.line}" else ""
                    val fileLocation = "${loc.filePath}$lineSuffix"
                    appendLine("   ${getMessage("report.fileLabel", fileLocation)}")
                }
                if (index < report.violations.size - 1) {
                    appendLine()
                }
            }
        }.trimEnd()
    }
}
