/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.i18n.getMessage

/**
 * Formatter for building semantic HTML error reports from architecture violation reports.
 */
public object HtmlViolationFormatter {
    /**
     * Formats a [ViolationReport] into an HTML snippet suitable for HTML test reporting tools.
     *
     * @param report The violation report to format.
     * @param customHeader Optional custom header or description to include below the rule header.
     * @return Formatted HTML string representation of the report.
     */
    @JvmOverloads
    public fun format(
        report: ViolationReport,
        customHeader: String? = null,
    ): String {
        if (report.violations.isEmpty()) return ""

        val metaDescription = report.violations.firstOrNull()?.metadata?.description
        val header = customHeader?.takeIf { it.isNotBlank() } ?: metaDescription?.takeIf { it.isNotBlank() }

        return buildString {
            appendLine("<div class=\"konture-report\">")
            appendLine(
                "  <h2 class=\"konture-header\">${escapeHtml(getMessage("report.ruleHeader", report.ruleId))}</h2>",
            )
            if (header != null) {
                appendLine("  <p class=\"konture-description\">${escapeHtml(header)}</p>")
            }
            appendLine(
                "  <p class=\"konture-summary\"><strong>${escapeHtml(
                    getMessage("report.violationsFound", report.violations.size),
                )}</strong></p>",
            )
            appendLine("  <ol class=\"konture-violations\">")
            report.violations.forEach { v ->
                appendLine("    <li class=\"konture-violation\">")
                appendLine("      <span class=\"konture-subject\">${escapeHtml(v.subject.name)}</span>")
                val cleanMessage = v.message.substringBeforeLast(" (at ").trim()
                appendLine(
                    "      <div class=\"konture-message\">${escapeHtml(
                        getMessage("report.messageLabel", cleanMessage),
                    )}</div>",
                )
                val loc = v.sourceLocation ?: v.subject.location
                if (loc != null) {
                    val lineSuffix = if (loc.line != null) ":${loc.line}" else ""
                    val fileLocation = "${loc.filePath}$lineSuffix"
                    val fileObj = java.io.File(loc.filePath)
                    val absolutePath = fileObj.absolutePath.replace("\\", "/")
                    val normalizedPath = if (absolutePath.startsWith("/")) absolutePath else "/$absolutePath"
                    val lineHash = if (loc.line != null) "#L${loc.line}" else ""
                    val href = "file://$normalizedPath$lineHash"
                    appendLine(
                        "      <div class=\"konture-location\"><a href=\"${escapeHtml(
                            href,
                        )}\" class=\"konture-file-link\" target=\"_blank\"><code>${escapeHtml(
                            getMessage("report.fileLabel", fileLocation),
                        )}</code></a></div>",
                    )
                }
                appendLine("    </li>")
            }
            appendLine("  </ol>")
            append("</div>")
        }
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
