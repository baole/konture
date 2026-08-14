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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProblemMatcherViolationFormatterTest {
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
        assertEquals("", ProblemMatcherViolationFormatter.format(report))
    }

    @Test
    fun `format formats single violation into problem matcher style`() {
        val violation =
            Violation(
                ruleId = "domain.repositories.must-be-interfaces",
                subject = Subject.ClassSubject("com.example.domain.UserRepository", "UserRepository"),
                sourceLocation = SourceLocation("domain/src/main/kotlin/com/example/domain/UserRepository.kt", 18, 1),
                message = "Expected interface, but found class",
            )
        val report = ViolationReport(ruleId = "domain.repositories.must-be-interfaces", violations = listOf(violation))

        val expected = "domain/src/main/kotlin/com/example/domain/UserRepository.kt:18:1: Konture [domain.repositories.must-be-interfaces]: Expected interface, but found class"
        assertEquals(expected, ProblemMatcherViolationFormatter.format(report))
    }

    @Test
    fun `format formats multiple violations separated by newlines`() {
        val v1 =
            Violation(
                ruleId = "test.rule",
                subject = Subject.ClassSubject("Foo", "Foo"),
                sourceLocation = SourceLocation("src/Foo.kt", 10, 5),
                message = "First violation",
            )
        val v2 =
            Violation(
                ruleId = "test.rule",
                subject = Subject.ClassSubject("Bar", "Bar"),
                sourceLocation = SourceLocation("src/Bar.kt", 20, 12),
                message = "Second violation",
            )
        val report = ViolationReport(ruleId = "test.rule", violations = listOf(v1, v2))

        val expected =
            "src/Foo.kt:10:5: Konture [test.rule]: First violation\n" +
                "src/Bar.kt:20:12: Konture [test.rule]: Second violation"
        assertEquals(expected, ProblemMatcherViolationFormatter.format(report))
    }

    @Test
    fun `format falls back to defaults when source location fields are missing`() {
        val violation =
            Violation(
                ruleId = "test.rule",
                subject = Subject.ClassSubject("Foo", "Foo"),
                sourceLocation = SourceLocation("", null, null),
                message = "Missing location details",
            )
        val report = ViolationReport(ruleId = "test.rule", violations = listOf(violation))

        val expected = "unknown:1:1: Konture [test.rule]: Missing location details"
        assertEquals(expected, ProblemMatcherViolationFormatter.format(report))
    }

    @Test
    fun `format strips location suffix from message when present`() {
        val violation =
            Violation(
                ruleId = "test.rule",
                subject = Subject.ClassSubject("Foo", "Foo"),
                sourceLocation = SourceLocation("src/Foo.kt", 15, 1),
                message = "Class Foo violates naming rule (at :module/main/src/Foo.kt:15)",
            )
        val report = ViolationReport(ruleId = "test.rule", violations = listOf(violation))

        val expected = "src/Foo.kt:15:1: Konture [test.rule]: Class Foo violates naming rule"
        assertEquals(expected, ProblemMatcherViolationFormatter.format(report))
    }

    @Test
    fun `Konture outputFormat respects system property and programmatically configured value`() {
        assertEquals(OutputFormat.HUMAN, Konture.outputFormat)

        Konture.outputFormat = OutputFormat.PROBLEM_MATCHER
        assertEquals(OutputFormat.PROBLEM_MATCHER, Konture.outputFormat)

        KontureRuntimeStateProvider.reset()
        System.setProperty(Konture.PROPERTY_OUTPUT_FORMAT, "problem_matcher")
        assertEquals(OutputFormat.PROBLEM_MATCHER, Konture.outputFormat)
    }
}
