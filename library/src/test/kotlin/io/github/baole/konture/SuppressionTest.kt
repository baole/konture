/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.SourceLocation
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.SuppressionKind
import io.github.baole.konture.core.model.SuppressionMetadata
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.impl.report.JsonReportExporter
import io.github.baole.konture.impl.report.ReportAccumulator
import io.github.baole.konture.impl.report.SarifReportExporter
import io.github.baole.konture.impl.suppression.SuppressionEvaluator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SuppressionTest : RuleBuildersTestBase() {

    @BeforeEach
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `test mandatory non-blank reason validation`() {
        val builder = RuleSuppressionBuilder()
        assertThrows(IllegalArgumentException::class.java) {
            builder.classFqName("com.example.Foo", reason = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            builder.classFqName("com.example.Foo", reason = "   ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            builder.file("Foo.kt", reason = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            builder.function("foo", reason = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            builder.property("bar", reason = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            builder.module(":app", reason = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            builder.slice("domain", reason = "")
        }
    }

    @Test
    fun `test in-source class suppression token matching`() {
        val suppressedClass = ClassDeclaration(
            name = "SuppressedClass",
            fqName = "com.example.SuppressedClass",
            packageName = "com.example",
            isInterface = false,
            isAbstract = false,
            annotations = listOf(
                AnnotationDeclaration(
                    name = "Suppress",
                    fqName = "kotlin.Suppress",
                    arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:architecture.rule\"")),
                ),
            ),
            imports = emptyList(),
            referencedTypes = emptySet(),
            filePath = "/src/SuppressedClass.kt",
        )

        val metadata = SuppressionEvaluator.evaluateClassSuppression(
            ruleId = "architecture.rule",
            cls = suppressedClass,
        )

        assertNotNull(metadata)
        assertEquals(SuppressionKind.IN_SOURCE, metadata?.kind)
        assertTrue(metadata?.reason?.contains("konture:architecture.rule") == true)
    }

    @Test
    fun `test in-source wildcard suppression`() {
        val suppressedClass = ClassDeclaration(
            name = "SuppressedClass",
            fqName = "com.example.SuppressedClass",
            packageName = "com.example",
            isInterface = false,
            isAbstract = false,
            annotations = listOf(
                AnnotationDeclaration(
                    name = "Suppress",
                    fqName = "kotlin.Suppress",
                    arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:*\"")),
                ),
            ),
            imports = emptyList(),
            referencedTypes = emptySet(),
            filePath = "/src/SuppressedClass.kt",
        )

        val metadata = SuppressionEvaluator.evaluateClassSuppression(
            ruleId = "any.custom.rule",
            cls = suppressedClass,
        )

        assertNotNull(metadata)
        assertEquals(SuppressionKind.IN_SOURCE, metadata?.kind)
    }

    @Test
    fun `test file-level in-source suppression cascading to classes`() {
        val fileDecl = FileDeclaration(
            name = "MyFile.kt",
            packageName = "com.example",
            filePath = "/src/MyFile.kt",
            annotations = listOf(
                AnnotationDeclaration(
                    name = "Suppress",
                    fqName = "kotlin.Suppress",
                    arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:clean.arch\"")),
                ),
            ),
        )

        val cls = ClassDeclaration(
            name = "InnerClass",
            fqName = "com.example.InnerClass",
            packageName = "com.example",
            isInterface = false,
            isAbstract = false,
            annotations = emptyList(),
            imports = emptyList(),
            referencedTypes = emptySet(),
            filePath = "/src/MyFile.kt",
        )

        val metadata = SuppressionEvaluator.evaluateClassSuppression(
            ruleId = "clean.arch",
            cls = cls,
            file = fileDecl,
        )

        assertNotNull(metadata)
        assertEquals(SuppressionKind.IN_SOURCE, metadata?.kind)
    }

    @Test
    fun `test programmatic class suppression with reason`() {
        val cls = ClassDeclaration(
            name = "LegacyService",
            fqName = "com.example.LegacyService",
            packageName = "com.example",
            isInterface = false,
            isAbstract = false,
            annotations = emptyList(),
            imports = emptyList(),
            referencedTypes = emptySet(),
            filePath = "/src/LegacyService.kt",
        )

        val suppressions = RuleSuppressionBuilder().apply {
            classFqName("com.example.LegacyService", reason = "Ticket #123 legacy refactoring")
        }.suppressions

        val metadata = SuppressionEvaluator.evaluateClassSuppression(
            ruleId = "service.naming",
            cls = cls,
            programmaticSuppressions = suppressions,
        )

        assertNotNull(metadata)
        assertEquals(SuppressionKind.PROGRAMMATIC, metadata?.kind)
        assertEquals("Ticket #123 legacy refactoring", metadata?.reason)
    }

    @Test
    fun `test classes rule builder with programmatic suppression`() {
        val badClass = ClassDeclaration(
            name = "BadClass",
            fqName = "com.example.BadClass",
            packageName = "com.example",
            isInterface = false,
            isAbstract = false,
            annotations = emptyList(),
            imports = emptyList(),
            referencedTypes = emptySet(),
            filePath = "/src/BadClass.kt",
        )
        val fileDecl = FileDeclaration("BadClass.kt", "com.example", filePath = "/src/BadClass.kt", classes = listOf(badClass))
        val mod = Module(
            buildId = ":",
            path = ":testModule",
            projectDir = "testModule",
            appliedPlugins = emptyList(),
            sourceSets = emptyList(),
            dependencies = emptyList(),
            files = listOf(fileDecl),
        )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        // When not suppressed, check should throw AssertionError
        assertThrows(AssertionError::class.java) {
            ClassesRuleBuilder(graph)
                .that()
                .haveNameStartingWith("BadClass")
                .should()
                .beInterfaces()
                .check()
        }

        // When programmatic suppression is configured, check succeeds
        ClassesRuleBuilder(graph)
            .that()
            .haveNameStartingWith("BadClass")
            .suppress {
                classFqName("com.example.BadClass", reason = "Acceptable deviation until v2.0")
            }
            .should()
            .beInterfaces()
            .check()
    }

    @Test
    fun `test functions rule builder with in-source suppression`() {
        val func = FunctionDeclaration(
            name = "forbiddenHelper",
            visibility = Visibility.PUBLIC,
            modifiers = emptySet(),
            returnType = "Unit",
            parameters = emptyList(),
            annotations = listOf(
                AnnotationDeclaration(
                    name = "Suppress",
                    fqName = "kotlin.Suppress",
                    arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:functions.rule\"")),
                ),
            ),
            kdocText = null,
            isExtension = false,
            sourceLine = 15,
        )
        val fileDecl = FileDeclaration("Helper.kt", "com.example", filePath = "/src/Helper.kt", topLevelFunctions = listOf(func))
        val mod = Module(
            buildId = ":",
            path = ":util",
            projectDir = "util",
            appliedPlugins = emptyList(),
            sourceSets = emptyList(),
            dependencies = emptyList(),
            files = listOf(fileDecl),
        )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        // Check should pass because function has @Suppress("konture:functions.rule")
        FunctionsRuleBuilder(graph)
            .that()
            .haveNameStartingWith("forbidden")
            .should()
            .haveNameEndingWith("Allowed")
            .check()
    }

    @Test
    fun `test properties rule builder with programmatic suppression`() {
        val prop = PropertyDeclaration(
            name = "badProperty",
            visibility = Visibility.PUBLIC,
            modifiers = emptySet(),
            type = "String",
            isVal = true,
            annotations = emptyList(),
            kdocText = null,
            sourceLine = 20,
        )
        val fileDecl = FileDeclaration("Data.kt", "com.example", filePath = "/src/Data.kt", topLevelProperties = listOf(prop))
        val mod = Module(
            buildId = ":",
            path = ":data",
            projectDir = "data",
            appliedPlugins = emptyList(),
            sourceSets = emptyList(),
            dependencies = emptyList(),
            files = listOf(fileDecl),
        )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        // Suppressed property check
        PropertiesRuleBuilder(graph)
            .that()
            .haveNameStartingWith("bad")
            .suppress {
                property("badProperty", reason = "Temporary backwards compatibility")
            }
            .should()
            .beConst()
            .check()
    }

    @Test
    fun `test modules rule builder with programmatic suppression`() {
        val mod = Module(
            buildId = ":",
            path = ":legacy:feature",
            projectDir = "legacy/feature",
            appliedPlugins = listOf("kotlin"),
            sourceSets = emptyList(),
            dependencies = emptyList(),
            files = emptyList(),
        )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        ModulesRuleBuilder(graph)
            .that()
            .haveNameStartingWith(":legacy")
            .suppress {
                module(":legacy:feature", reason = "Exempt legacy feature from strict plugin enforcement")
            }
            .should()
            .applyPlugin("java")
            .check()
    }

    @Test
    fun `test json and sarif report export with suppression metadata`() {
        val unsuppressedViolation = Violation(
            ruleId = "rule.alpha",
            subject = Subject.ClassSubject("com.example.Alpha", "Alpha"),
            message = "Alpha fails check",
            severity = Severity.ERROR,
            isSuppressed = false,
        )
        val suppressedViolation = Violation(
            ruleId = "rule.beta",
            subject = Subject.ClassSubject("com.example.Beta", "Beta"),
            message = "Beta fails check",
            severity = Severity.WARNING,
            isSuppressed = true,
            suppression = SuppressionMetadata(
                kind = SuppressionKind.IN_SOURCE,
                reason = "Rule suppressed in source with @Suppress(\"konture:rule.beta\")",
            ),
        )

        val eval = ReportAccumulator.RuleEvaluation(
            ruleId = "rule.beta",
            metadata = null,
            unsuppressedViolations = listOf(unsuppressedViolation),
            suppressedViolations = listOf(suppressedViolation),
        )

        val jsonReport = JsonReportExporter.generateReport(listOf(eval))
        val jsonContent = JsonReportExporter.exportToString(jsonReport)
        assertTrue(jsonContent.contains("\"isSuppressed\": true") || jsonContent.contains("\"isSuppressed\":true"))
        assertTrue(jsonContent.contains("in_source"))
        assertTrue(jsonContent.contains("Rule suppressed in source"))

        val sarifReport = SarifReportExporter.generateReport(listOf(eval))
        val sarifContent = SarifReportExporter.exportToString(sarifReport)
        assertTrue(sarifContent.contains("\"kind\": \"inSource\""))
        assertTrue(sarifContent.contains("Rule suppressed in source"))
    }
}
