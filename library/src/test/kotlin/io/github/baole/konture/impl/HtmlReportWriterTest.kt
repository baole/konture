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
import java.io.File

class HtmlReportWriterTest {
    private val testReportFile = File("build/tmp/test-reports/html-writer-test.html")

    @BeforeEach
    fun setUp() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_REPORT_PATH)
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
    }
}
