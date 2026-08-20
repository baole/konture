/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ViolationModelTest {
    private val json = Json { prettyPrint = true }

    @Test
    fun testSeverityEnum() {
        assertEquals(3, Severity.entries.size)
        assertEquals(Severity.INFO, Severity.valueOf("INFO"))
        assertEquals(Severity.WARNING, Severity.valueOf("WARNING"))
        assertEquals(Severity.ERROR, Severity.valueOf("ERROR"))

        val encoded = json.encodeToString(Severity.WARNING)
        val decoded: Severity = json.decodeFromString(encoded)
        assertEquals(Severity.WARNING, decoded)
    }

    @Test
    fun testSourceLocation() {
        val loc = SourceLocation(filePath = "src/Main.kt", line = 10, column = 5)
        assertEquals("src/Main.kt", loc.filePath)
        assertEquals(10, loc.line)
        assertEquals(5, loc.column)

        val encoded = json.encodeToString(loc)
        val decoded: SourceLocation = json.decodeFromString(encoded)
        assertEquals(loc, decoded)
    }

    @Test
    fun testSubjectHierarchy() {
        val loc = SourceLocation(filePath = "module/build.gradle.kts")
        val moduleSubject: Subject = Subject.ModuleSubject(path = ":core", location = loc)
        assertEquals(":core", moduleSubject.name)
        assertEquals(loc, moduleSubject.location)

        val classSubject: Subject =
            Subject.ClassSubject(
                fqName = "com.example.Foo",
                simpleName = "Foo",
                location = SourceLocation(filePath = "com/example/Foo.kt", line = 12),
            )
        assertEquals("com.example.Foo", classSubject.name)

        val funcSubject: Subject =
            Subject.FunctionSubject(
                fqName = "com.example.Foo.bar",
                location = SourceLocation(filePath = "com/example/Foo.kt", line = 20),
            )
        assertEquals("com.example.Foo.bar", funcSubject.name)

        val customSubject: Subject =
            Subject.CustomSubject(
                name = "custom-node",
                location = null,
            )
        assertEquals("custom-node", customSubject.name)
        assertNull(customSubject.location)

        val subjects: List<Subject> = listOf(moduleSubject, classSubject, funcSubject, customSubject)
        val encoded = json.encodeToString(subjects)
        val decoded: List<Subject> = json.decodeFromString(encoded)
        assertEquals(subjects, decoded)
    }

    @Test
    fun testViolationAndDefaults() {
        val classSubject =
            Subject.ClassSubject(
                fqName = "com.example.Foo",
                simpleName = "Foo",
                location = SourceLocation(filePath = "com/example/Foo.kt", line = 5),
            )
        val violation =
            Violation(
                ruleId = "classes.no-cycle",
                subject = classSubject,
                message = "Class Foo has a cyclic dependency",
            )

        assertEquals("classes.no-cycle", violation.ruleId)
        assertEquals(classSubject, violation.subject)
        assertNull(violation.target)
        assertEquals(classSubject.location, violation.sourceLocation)
        assertTrue(violation.dependencyPath.isEmpty())
        assertEquals("Class Foo has a cyclic dependency", violation.message)
        assertEquals(Severity.ERROR, violation.severity)

        val encoded = json.encodeToString(violation)
        val decoded: Violation = json.decodeFromString(encoded)
        assertEquals(violation, decoded)
    }

    @Test
    fun testViolationReportFlagsAndSerialization() {
        val sub1 = Subject.ClassSubject("a.A", "A")
        val sub2 = Subject.ClassSubject("b.B", "B")

        val errViolation =
            Violation(
                ruleId = "rule.1",
                subject = sub1,
                message = "Error violation",
                severity = Severity.ERROR,
            )
        val warnViolation =
            Violation(
                ruleId = "rule.1",
                subject = sub2,
                message = "Warning violation",
                severity = Severity.WARNING,
            )

        val reportWithErrors =
            ViolationReport(
                ruleId = "rule.1",
                violations = listOf(errViolation, warnViolation),
            )
        assertTrue(reportWithErrors.hasErrors)
        assertTrue(reportWithErrors.hasWarnings)

        val reportWarningsOnly =
            ViolationReport(
                ruleId = "rule.1",
                violations = listOf(warnViolation),
                severity = Severity.WARNING,
            )
        assertFalse(reportWarningsOnly.hasErrors)
        assertTrue(reportWarningsOnly.hasWarnings)

        val encoded = json.encodeToString(reportWithErrors)
        val decoded: ViolationReport = json.decodeFromString(encoded)
        assertEquals(reportWithErrors, decoded)
    }

    @Test
    fun testRuleMetadataAndViolationMetadata() {
        val metadata =
            RuleMetadata(
                id = "domain.repositories.must-be-interfaces",
                description = "Domain repositories must be interfaces to enforce DIP",
                severity = Severity.ERROR,
                tags = setOf("architecture", "domain"),
            )
        val subject = Subject.ClassSubject("com.example.Repo", "Repo")
        val violation =
            Violation(
                ruleId = metadata.id,
                subject = subject,
                message = "Repo is not an interface",
                severity = metadata.severity,
                metadata = metadata,
            )

        assertEquals(metadata, violation.metadata)
        val encoded = json.encodeToString(violation)
        val decoded: Violation = json.decodeFromString(encoded)
        assertEquals(violation, decoded)
    }

    @Test
    fun testSuppressionMetadataAndKind() {
        assertEquals(3, SuppressionKind.entries.size)
        assertEquals(SuppressionKind.IN_SOURCE, SuppressionKind.valueOf("IN_SOURCE"))
        assertEquals(SuppressionKind.PROGRAMMATIC, SuppressionKind.valueOf("PROGRAMMATIC"))
        assertEquals(SuppressionKind.BASELINE, SuppressionKind.valueOf("BASELINE"))

        val loc = SourceLocation(filePath = "src/Test.kt", line = 5)
        val metadata =
            SuppressionMetadata(
                kind = SuppressionKind.IN_SOURCE,
                reason = "Intentional architecture exception",
                location = loc,
            )
        assertEquals(SuppressionKind.IN_SOURCE, metadata.kind)
        assertEquals("Intentional architecture exception", metadata.reason)
        assertEquals(loc, metadata.location)

        val encoded = json.encodeToString(metadata)
        val decoded: SuppressionMetadata = json.decodeFromString(encoded)
        assertEquals(metadata, decoded)

        val violation =
            Violation(
                ruleId = "rule.suppressed",
                subject = Subject.ClassSubject("com.example.Suppressed", "Suppressed"),
                message = "Violation message",
                severity = Severity.WARNING,
                isSuppressed = true,
                suppression = metadata,
            )
        assertTrue(violation.isSuppressed)
        assertEquals(metadata, violation.suppression)

        val violationEncoded = json.encodeToString(violation)
        val violationDecoded: Violation = json.decodeFromString(violationEncoded)
        assertEquals(violation, violationDecoded)
    }
}
