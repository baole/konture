/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.HtmlViolationFormatter
import io.github.baole.konture.Konture
import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.report.ReportFileUtil
import java.io.File

internal object HtmlReportWriter {
    private const val KONTURE_GITHUB_URL = "https://github.com/baole/konture"

    @Suppress("TooGenericExceptionCaught")
    fun writeReport(
        report: ViolationReport,
        targetFile: File = File(Konture.htmlReportPath),
        customHeader: String? = null,
        projectRoot: File? = null,
    ) {
        val htmlSnippet = HtmlViolationFormatter.format(report, customHeader)
        val projectSignature = HtmlReportProjectSignatureResolver.resolve(projectRoot, targetFile)
        val projectSignatureFooter = buildProjectSignatureFooterHtml(projectSignature)

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
                    .konture-report-footer { max-width: 860px; margin: 1rem auto 0; color: #6c757d; font-size: 0.95rem; text-align: center; }
                    .konture-project-signature { display: inline-flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.35rem; }
                    .konture-project-signature-item { display: inline-flex; align-items: center; justify-content: center; gap: 0.35rem; flex-wrap: wrap; }
                    .konture-project-signature-label { font-weight: 600; color: #495057; }
                    a.konture-signature-link { color: #0d6efd; text-decoration: none; word-break: break-all; }
                    a.konture-signature-link:hover { text-decoration: underline; }
                </style>
            </head>
            <body>
                $htmlSnippet
                $projectSignatureFooter
            </body>
            </html>
            """.trimIndent()

        ReportFileUtil.writeAtomically(targetFile, fullDocument)
    }

    private fun buildProjectSignatureFooterHtml(projectSignature: String?): String {
        val footerItems =
            buildList {
                if (!projectSignature.isNullOrBlank()) {
                    add(
                        buildSignatureItemHtml(
                            label = getMessage("report.projectSignatureLabel"),
                            signature = projectSignature,
                        ),
                    )
                }
                add(
                    buildSignatureItemHtml(
                        label = getMessage("report.kontureSignatureLabel"),
                        signature = KONTURE_GITHUB_URL,
                    ),
                )
            }

        return """
            <footer class="konture-report-footer">
                <div class="konture-project-signature">
                    ${footerItems.joinToString("\n")}
                </div>
            </footer>
            """.trimIndent()
    }

    private fun buildSignatureItemHtml(
        label: String,
        signature: String,
    ): String {
        val escapedLabel = escapeHtml(label)
        val escapedSignature = escapeHtml(signature)
        val signatureContent =
            if (signature.isHttpUrl()) {
                "<a href=\"$escapedSignature\" class=\"konture-signature-link\" target=\"_blank\" rel=\"noopener noreferrer\">$escapedSignature</a>"
            } else {
                "<span>$escapedSignature</span>"
            }

        return """
            <div class="konture-project-signature-item">
                <span class="konture-project-signature-label">$escapedLabel</span>
                $signatureContent
            </div>
            """.trimIndent()
    }

    private fun String.isHttpUrl(): Boolean = startsWith("https://") || startsWith("http://")

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
