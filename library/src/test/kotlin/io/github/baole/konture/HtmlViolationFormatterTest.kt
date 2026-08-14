/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HtmlViolationFormatterTest {
    @BeforeEach
    fun setUp() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
    }

    @AfterEach
    fun tearDown() {
        KontureRuntimeStateProvider.reset()
        System.clearProperty(Konture.PROPERTY_OUTPUT_FORMAT)
    }

    @Test
    fun `format returns empty string when report has no violations`() {
        val report = ViolationReport(ruleId = "test.rule", violations = emptyList())
        assertEquals("", HtmlViolationFormatter.format(report))
    }

    @Test
    fun `format formats report into semantic HTML structure`() {
        val violation =
            Violation(
                ruleId = "domain.repositories.must-be-interfaces",
                subject = Subject.ClassSubject("com.example.domain.UserRepository", "UserRepository"),
                sourceLocation = SourceLocation("domain/src/main/kotlin/com/example/domain/UserRepository.kt", 18, 1),
                message = "Expected interface, but found class",
            )
        val report = ViolationReport(ruleId = "domain.repositories.must-be-interfaces", violations = listOf(violation))

        val html = HtmlViolationFormatter.format(report, customHeader = "Classes in domain layer must be interfaces")

        assertTrue(html.contains("<div class=\"konture-report\">"))
        assertTrue(
            html.contains("<h2 class=\"konture-header\">\u2717 Rule: domain.repositories.must-be-interfaces</h2>"),
        )
        assertTrue(html.contains("<p class=\"konture-description\">Classes in domain layer must be interfaces</p>"))
        assertTrue(html.contains("<strong>1 violation(s) found:</strong>"))
        assertTrue(html.contains("<span class=\"konture-subject\">com.example.domain.UserRepository</span>"))
        assertTrue(html.contains("<div class=\"konture-message\">Message: Expected interface, but found class</div>"))
        assertTrue(html.contains("class=\"konture-file-link\""))
        assertTrue(html.contains("<code>File: domain/src/main/kotlin/com/example/domain/UserRepository.kt:18</code>"))
    }

    @Test
    fun `format escapes HTML special characters in messages and subjects`() {
        val violation =
            Violation(
                ruleId = "test.rule",
                subject = Subject.ClassSubject("Foo<Bar>", "Foo<Bar>"),
                sourceLocation = SourceLocation("src/Foo.kt", 10, 1),
                message = "Message with <script>alert('xss')</script> & \"quotes\"",
            )
        val report = ViolationReport(ruleId = "test.rule", violations = listOf(violation))

        val html = HtmlViolationFormatter.format(report)

        assertTrue(html.contains("Foo&lt;Bar&gt;"))
        assertTrue(
            html.contains("Message with &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt; &amp; &quot;quotes&quot;"),
        )
    }

    @Test
    fun `Konture outputFormat supports HTML option`() {
        Konture.outputFormat = OutputFormat.HTML
        assertEquals(OutputFormat.HTML, Konture.outputFormat)

        KontureRuntimeStateProvider.reset()
        System.setProperty(Konture.PROPERTY_OUTPUT_FORMAT, "html")
        assertEquals(OutputFormat.HTML, Konture.outputFormat)
    }
}
