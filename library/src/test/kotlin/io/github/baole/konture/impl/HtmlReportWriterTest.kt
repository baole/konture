/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Konture
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.model.ViolationReport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Locale

class HtmlReportWriterTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var testReportFile: File

    @BeforeEach
    fun setUp() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
        testReportFile = File(tempDir, "build/tmp/test-reports/html-writer-test.html")
        testReportFile.delete()
    }

    @AfterEach
    fun tearDown() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
        testReportFile.delete()
    }

    @Test
    fun `writeReport creates standalone HTML document on disk`() {
        Konture.reportPath = testReportFile.path

        val violation =
            Violation(
                ruleId = "domain.rule",
                subject = Subject.ClassSubject("com.example.DomainClass", "DomainClass"),
                sourceLocation = SourceLocation("src/DomainClass.kt", 12, 1),
                message = "Domain violation message",
            )
        val report = ViolationReport(ruleId = "domain.rule", violations = listOf(violation))

        HtmlReportWriter.writeReport(report, customHeader = "Custom rule header")

        assertTrue(testReportFile.exists())
        val content = testReportFile.readText()
        assertTrue(content.contains("<!DOCTYPE html>"))
        assertTrue(content.contains("<title>Konture Architectural Violation Report</title>"))
        assertTrue(content.contains("Custom rule header"))
        assertTrue(content.contains("com.example.DomainClass"))
        assertTrue(content.contains("Konture:"))
        assertTrue(content.contains("https://github.com/baole/konture"))
    }

    @Test
    fun `writeReport adds localized project signature footer from git remote`() {
        Konture.locale = Locale.FRENCH
        val gitConfig = File(tempDir, ".git/config")
        gitConfig.parentFile.mkdirs()
        gitConfig.writeText(
            """
            [core]
                repositoryformatversion = 0
            [remote "origin"]
                url = git@github.com:example/acme-platform.git
            """.trimIndent(),
        )

        val violation =
            Violation(
                ruleId = "domain.rule",
                subject = Subject.ClassSubject("com.example.DomainClass", "DomainClass"),
                sourceLocation = SourceLocation("src/DomainClass.kt", 12, 1),
                message = "Domain violation message",
            )
        val report = ViolationReport(ruleId = "domain.rule", violations = listOf(violation))

        HtmlReportWriter.writeReport(report, targetFile = testReportFile, projectRoot = tempDir)

        val content = testReportFile.readText()
        assertTrue(content.contains("<footer class=\"konture-report-footer\">"))
        assertTrue(content.contains("Projet :"))
        assertTrue(content.contains("https://github.com/example/acme-platform"))
        assertTrue(content.contains("Konture :"))
        assertTrue(content.contains("https://github.com/baole/konture"))
        assertTrue(content.contains("class=\"konture-project-signature-item\""))
        assertTrue(content.contains("class=\"konture-signature-link\""))
    }
}
