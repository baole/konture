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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HumanReadableViolationFormatterTest : RuleBuildersTestBase() {
    @Test
    fun `format returns empty string when report has no violations`() {
        val report = ViolationReport(ruleId = "test.rule", violations = emptyList())
        assertEquals("", HumanReadableViolationFormatter.format(report))
    }

    @Test
    fun `format matches expected human-readable output structure for single violation`() {
        val violation =
            Violation(
                ruleId = "domain.repositories.must-be-interfaces",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.domain.UserRepository",
                        simpleName = "UserRepository",
                        location =
                            SourceLocation(
                                filePath = "domain/src/main/kotlin/com/example/domain/UserRepository.kt",
                                line = 18,
                            ),
                    ),
                message = "Expected interface, but found class",
            )
        val report =
            ViolationReport(
                ruleId = "domain.repositories.must-be-interfaces",
                violations = listOf(violation),
            )

        val formatted = HumanReadableViolationFormatter.format(report)
        val expected =
            """
            ✗ Rule: domain.repositories.must-be-interfaces

            1 violation(s) found:

            1. com.example.domain.UserRepository
               Message: Expected interface, but found class
               File: domain/src/main/kotlin/com/example/domain/UserRepository.kt:18
            """.trimIndent()

        assertEquals(expected, formatted)
    }

    @Test
    fun `format matches expected human-readable output for multiple violations`() {
        val v1 =
            Violation(
                ruleId = "domain.repositories.must-be-interfaces",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.domain.UserRepository",
                        simpleName = "UserRepository",
                        location =
                            SourceLocation(
                                filePath = "domain/src/main/kotlin/com/example/domain/UserRepository.kt",
                                line = 18,
                            ),
                    ),
                message = "Expected interface, but found class",
            )
        val v2 =
            Violation(
                ruleId = "domain.repositories.must-be-interfaces",
                subject =
                    Subject.ClassSubject(
                        fqName = "com.example.domain.PaymentRepository",
                        simpleName = "PaymentRepository",
                        location =
                            SourceLocation(
                                filePath = "domain/src/main/kotlin/com/example/domain/PaymentRepository.kt",
                                line = 12,
                            ),
                    ),
                message = "Expected interface, but found class",
            )
        val report =
            ViolationReport(
                ruleId = "domain.repositories.must-be-interfaces",
                violations = listOf(v1, v2),
            )

        val formatted = HumanReadableViolationFormatter.format(report)
        val expected =
            """
            ✗ Rule: domain.repositories.must-be-interfaces

            2 violation(s) found:

            1. com.example.domain.UserRepository
               Message: Expected interface, but found class
               File: domain/src/main/kotlin/com/example/domain/UserRepository.kt:18

            2. com.example.domain.PaymentRepository
               Message: Expected interface, but found class
               File: domain/src/main/kotlin/com/example/domain/PaymentRepository.kt:12
            """.trimIndent()

        assertEquals(expected, formatted)
    }

    @Test
    fun `format handles null source location gracefully`() {
        val violation =
            Violation(
                ruleId = "module.dependency.check",
                subject = Subject.ModuleSubject(path = ":core:domain", location = null),
                message = "Module :core:domain cannot depend on :app",
                sourceLocation = null,
            )
        val report =
            ViolationReport(
                ruleId = "module.dependency.check",
                violations = listOf(violation),
            )

        val formatted = HumanReadableViolationFormatter.format(report)
        val expected =
            """
            ✗ Rule: module.dependency.check

            1 violation(s) found:

            1. :core:domain
               Message: Module :core:domain cannot depend on :app
            """.trimIndent()

        assertEquals(expected, formatted)
    }

    @Test
    fun `assertion error thrown on rule failure uses formatted report output`() {
        val error =
            assertThrows(AssertionError::class.java) {
                rule("test.architecture.rule") {
                    classes {
                        that().haveNameStartingWith("ClassA")
                            .should().beInterfaces()
                    }
                }.check()
            }

        assertTrue(error.message!!.contains("✗ Rule: test.architecture.rule"))
        assertTrue(error.message!!.contains("1 violation(s) found:"))
        assertTrue(error.message!!.contains("Message:"))
    }

    @Test
    fun `format includes customHeader when provided`() {
        val violation =
            Violation(
                ruleId = "domain.rule",
                subject = Subject.ClassSubject("com.example.Foo", "Foo", location = null),
                message = "Foo must be an interface",
            )
        val report = ViolationReport("domain.rule", listOf(violation))

        val formatted = HumanReadableViolationFormatter.format(report, customHeader = "Custom Rule Description")
        assertTrue(formatted.contains("✗ Rule: domain.rule\nCustom Rule Description"))
    }

    @Test
    fun `format uses substringBeforeLast for location suffix removal`() {
        val violation =
            Violation(
                ruleId = "test.rule",
                subject = Subject.ClassSubject("com.example.Foo", "Foo", location = null),
                message = "Error in function (at line 5) (at com/example/Foo.kt:10)",
            )
        val report = ViolationReport("test.rule", listOf(violation))

        val formatted = HumanReadableViolationFormatter.format(report)
        assertTrue(formatted.contains("Message: Error in function (at line 5)"))
    }

    @Test
    fun `format handles location with null line without appending line number`() {
        val violation =
            Violation(
                ruleId = "test.rule",
                subject =
                    Subject.ClassSubject(
                        "com.example.Foo",
                        "Foo",
                        location = SourceLocation("com/example/Foo.kt", line = null),
                    ),
                message = "Error message",
            )
        val report = ViolationReport("test.rule", listOf(violation))

        val formatted = HumanReadableViolationFormatter.format(report)
        assertTrue(formatted.contains("File: com/example/Foo.kt"))
        assertTrue(!formatted.contains("com/example/Foo.kt:"))
    }

    @Test
    fun `format respects Konture locale setting for i18n messages`() {
        val originalLocale = Konture.locale
        try {
            Konture.locale = java.util.Locale.FRENCH
            val violation =
                Violation(
                    ruleId = "test.rule",
                    subject =
                        Subject.ClassSubject(
                            "com.example.Foo",
                            "Foo",
                            location = SourceLocation("com/example/Foo.kt", 12),
                        ),
                    message = "Error message",
                )
            val report = ViolationReport("test.rule", listOf(violation))

            val formatted = HumanReadableViolationFormatter.format(report)
            assertTrue(
                formatted.contains("✗ Régal") || formatted.contains("✗ Règle : test.rule") || formatted.contains("Règle"),
            )
            assertTrue(formatted.contains("violation(s) trouvée(s)"))
            assertTrue(formatted.contains("Fichier : com/example/Foo.kt:12"))
        } finally {
            Konture.locale = originalLocale
        }
    }

    @Test
    fun `format renders dependency path when present`() {
        val violation =
            Violation(
                ruleId = "module.dependency.check",
                subject = Subject.ModuleSubject(path = ":app"),
                target = Subject.ModuleSubject(path = ":core:database"),
                dependencyPath =
                    listOf(
                        Subject.ModuleSubject(path = ":app"),
                        Subject.ModuleSubject(path = ":feature:login"),
                        Subject.ModuleSubject(path = ":core:database"),
                    ),
                message = "Module :app cannot depend on :core:database",
            )
        val report =
            ViolationReport(
                ruleId = "module.dependency.check",
                violations = listOf(violation),
            )

        val formatted = HumanReadableViolationFormatter.format(report)
        val expected =
            """
            ✗ Rule: module.dependency.check

            1 violation(s) found:

            1. :app
               Message: Module :app cannot depend on :core:database
               Found illegal dependency path:
               :app
                 → :feature:login
                 → :core:database
            """.trimIndent()

        assertEquals(expected, formatted)
    }
}
