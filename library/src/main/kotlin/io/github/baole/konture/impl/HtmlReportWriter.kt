/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.HtmlViolationFormatter
import io.github.baole.konture.Konture
import io.github.baole.konture.core.model.ViolationReport
import java.io.File

internal object HtmlReportWriter {
    @Suppress("TooGenericExceptionCaught")
    fun writeReport(
        report: ViolationReport,
        customHeader: String? = null,
    ) {
        try {
            val reportFile = File(Konture.reportPath)
            reportFile.parentFile?.mkdirs()

            val htmlSnippet = HtmlViolationFormatter.format(report, customHeader)

            val fullDocument =
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Konture Architectural Violation Report</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; padding: 2rem; background-color: #f8f9fa; color: #212529; line-height: 1.5; }
                        .konture-report { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); max-width: 860px; margin: 0 auto; border-top: 4px solid #dc3545; }
                        .konture-header { color: #dc3545; font-size: 1.5rem; margin-top: 0; margin-bottom: 0.5rem; font-weight: 600; }
                        .konture-description { color: #6c757d; font-size: 1rem; margin-bottom: 1.5rem; }
                        .konture-summary { font-size: 1.1rem; margin-bottom: 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid #dee2e6; }
                        .konture-violations { padding-left: 1.2rem; margin: 0; }
                        .konture-violation { margin-bottom: 1.25rem; }
                        .konture-subject { font-weight: 600; color: #0d6efd; font-size: 1.05rem; }
                        .konture-message { margin: 0.25rem 0 0.5rem 0; color: #343a40; }
                        .konture-location code { background-color: #e9ecef; padding: 0.2rem 0.5rem; border-radius: 4px; font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace; font-size: 0.875rem; color: #495057; transition: all 0.15s ease-in-out; }
                        a.konture-file-link { text-decoration: none; color: inherit; }
                        a.konture-file-link:hover code { background-color: #0d6efd; color: #ffffff; }
                    </style>
                </head>
                <body>
                    $htmlSnippet
                </body>
                </html>
                """.trimIndent()

            reportFile.writeText(fullDocument)
        } catch (_: Exception) {
            // Ignore file write exceptions in test environment
        }
    }
}
