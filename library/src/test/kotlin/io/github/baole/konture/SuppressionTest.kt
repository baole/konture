/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.core.model.SuppressionKind
import io.github.baole.konture.core.model.SuppressionMetadata
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.impl.BaselineManager
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import io.github.baole.konture.impl.report.JsonReportExporter
import io.github.baole.konture.impl.report.ReportAccumulator
import io.github.baole.konture.impl.report.SarifReportExporter
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
    fun `test classes rule builder with programmatic suppression`() {
        val badClass =
            ClassDeclaration(
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
        val fileDecl =
            FileDeclaration("BadClass.kt", "com.example", filePath = "/src/BadClass.kt", classes = listOf(badClass))
        val mod =
            Module(
                buildId = ":",
                path = ":testModule",
                projectDir = "testModule",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        assertThrows(AssertionError::class.java) {
            ClassesRuleBuilder(graph)
                .that()
                .haveNameStartingWith("BadClass")
                .should()
                .beInterfaces()
                .check()
        }

        ClassesRuleBuilder(graph)
            .that()
            .haveNameStartingWith("BadClass")
            .suppress {
                classFqName("com.example.BadClass", reason = "Acceptable deviation until v2.0")
                classes(reason = "Predicate match") { it.name.startsWith("Bad") }
                files(reason = "File match") { it.name == "BadClass.kt" }
            }
            .should()
            .beInterfaces()
            .check()
    }

    @Test
    fun `test functions rule builder with in-source and programmatic suppression`() {
        val func =
            FunctionDeclaration(
                name = "forbiddenHelper",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:*\"")),
                        ),
                    ),
                kdocText = null,
                isExtension = false,
                sourceStartOffset = 0,
                sourceEndOffset = 10,
                resolvedReturnType = null,
                sourceLine = 10,
            )
        val fileDecl =
            FileDeclaration("Helpers.kt", "com.example", filePath = "/src/Helpers.kt", topLevelFunctions = listOf(func))
        val mod =
            Module(
                buildId = ":",
                path = ":helpers",
                projectDir = "helpers",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        // In-source suppression
        FunctionsRuleBuilder(graph)
            .that()
            .haveNameStartingWith("forbidden")
            .should()
            .haveNameStartingWith("allowed")
            .check()

        // Programmatic function suppression
        val cleanFunc =
            FunctionDeclaration(
                name = "anotherHelper",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                sourceStartOffset = 0,
                sourceEndOffset = 10,
                resolvedReturnType = null,
                sourceLine = 20,
            )
        val cleanFile =
            FileDeclaration(
                name = "CleanHelpers.kt",
                packageName = "com.example",
                filePath = "/src/CleanHelpers.kt",
                topLevelFunctions = listOf(cleanFunc),
            )
        val cleanMod =
            Module(
                buildId = ":",
                path = ":clean",
                projectDir = "clean",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(cleanFile),
            )
        val cleanGraph = ProjectGraph(builds = mapOf(":" to listOf(cleanMod)))

        FunctionsRuleBuilder(cleanGraph)
            .that()
            .haveNameStartingWith("another")
            .suppress {
                function("anotherHelper", reason = "Refactoring planned in next sprint")
                functions(reason = "Predicate match") { it.declaration.name == "anotherHelper" }
            }
            .should()
            .haveNameStartingWith("allowed")
            .check()
    }

    @Test
    fun `test properties rule builder with programmatic suppression`() {
        val prop =
            PropertyDeclaration(
                name = "badProperty",
                type = "String",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                resolvedType = null,
                sourceLine = 20,
            )
        val fileDecl =
            FileDeclaration("Data.kt", "com.example", filePath = "/src/Data.kt", topLevelProperties = listOf(prop))
        val mod =
            Module(
                buildId = ":",
                path = ":data",
                projectDir = "data",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        PropertiesRuleBuilder(graph)
            .that()
            .haveNameStartingWith("bad")
            .suppress {
                property("badProperty", reason = "Temporary backwards compatibility")
                properties(reason = "Predicate match") { it.declaration.name == "badProperty" }
            }
            .should()
            .beConst()
            .check()
    }

    @Test
    fun `test files, modules, and slices rule builders with programmatic suppression`() {
        val cls =
            ClassDeclaration(
                name = "TestClass",
                fqName = "com.example.TestClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/TestClass.kt",
            )
        val file = FileDeclaration("TestClass.kt", "com.example", filePath = "/src/TestClass.kt", classes = listOf(cls))
        val mod =
            Module(
                buildId = ":",
                path = ":legacy:feature",
                projectDir = "legacy/feature",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(file),
            )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        FilesRuleBuilder(graph)
            .that()
            .haveNameEndingWith(".kt")
            .suppress {
                file("TestClass.kt", reason = "Exempt file")
            }
            .should()
            .resideInPackage("com.other")
            .check()

        ModulesRuleBuilder(graph)
            .that()
            .haveNameStartingWith(":legacy")
            .suppress {
                module(":legacy:feature", reason = "Exempt legacy feature from strict plugin enforcement")
            }
            .should()
            .applyPlugin("java")
            .check()

        val sliceGraph =
            ProjectGraph(
                mapOf(
                    ":" to
                        listOf(
                            Module(
                                buildId = ":",
                                path = ":app",
                                projectDir = "app",
                                appliedPlugins = listOf("kotlin"),
                                sourceSets = emptyList(),
                                dependencies = emptyList(),
                                files =
                                    listOf(
                                        FileDeclaration(
                                            "ServiceA.kt",
                                            "com.app.a",
                                            classes =
                                                listOf(
                                                    ClassDeclaration(
                                                        name = "ServiceA",
                                                        fqName = "com.app.a.ServiceA",
                                                        packageName = "com.app.a",
                                                        isInterface = false,
                                                        isAbstract = false,
                                                        annotations = emptyList(),
                                                        imports = listOf("com.app.b.ServiceB"),
                                                        referencedTypes = emptySet(),
                                                        filePath = "/src/ServiceA.kt",
                                                    ),
                                                ),
                                            filePath = "/src/ServiceA.kt",
                                        ),
                                        FileDeclaration(
                                            "ServiceB.kt",
                                            "com.app.b",
                                            classes =
                                                listOf(
                                                    ClassDeclaration(
                                                        name = "ServiceB",
                                                        fqName = "com.app.b.ServiceB",
                                                        packageName = "com.app.b",
                                                        isInterface = false,
                                                        isAbstract = false,
                                                        annotations = emptyList(),
                                                        imports = listOf("com.app.a.ServiceA"),
                                                        referencedTypes = emptySet(),
                                                        filePath = "/src/ServiceB.kt",
                                                    ),
                                                ),
                                            filePath = "/src/ServiceB.kt",
                                        ),
                                    ),
                            ),
                        ),
                ),
            )

        SlicesRuleBuilder(sliceGraph)
            .matching("com.app.(*)..")
            .suppress {
                slice("com.app.(*)..", reason = "Cycle allowed in app slices")
            }
            .should()
            .beFreeOfCycles()
            .check()
    }

    @Test
    fun `test json and sarif report export with suppression metadata`() {
        val unsuppressedViolation =
            Violation(
                ruleId = "rule.alpha",
                subject = Subject.ClassSubject("com.example.Alpha", "Alpha"),
                message = "Alpha fails check",
                severity = Severity.ERROR,
                isSuppressed = false,
            )
        val inSourceViolation =
            Violation(
                ruleId = "rule.beta",
                subject = Subject.ClassSubject("com.example.Beta", "Beta"),
                message = "Beta fails check",
                severity = Severity.WARNING,
                isSuppressed = true,
                suppression =
                    SuppressionMetadata(
                        kind = SuppressionKind.IN_SOURCE,
                        reason = "Rule suppressed in source with @Suppress(\"konture:rule.beta\")",
                    ),
            )
        val programmaticViolation =
            Violation(
                ruleId = "rule.gamma",
                subject = Subject.ClassSubject("com.example.Gamma", "Gamma"),
                message = "Gamma fails check",
                severity = Severity.WARNING,
                isSuppressed = true,
                suppression =
                    SuppressionMetadata(
                        kind = SuppressionKind.PROGRAMMATIC,
                        reason = "Explicit programmatic exclusion",
                    ),
            )
        val baselineViolation =
            Violation(
                ruleId = "rule.delta",
                subject = Subject.ClassSubject("com.example.Delta", "Delta"),
                message = "Delta fails check",
                severity = Severity.WARNING,
                isSuppressed = true,
                suppression =
                    SuppressionMetadata(
                        kind = SuppressionKind.BASELINE,
                        reason = "Baseline suppression",
                    ),
            )

        val eval =
            ReportAccumulator.RuleEvaluation(
                ruleId = "rule.beta",
                metadata = null,
                unsuppressedViolations = listOf(unsuppressedViolation),
                suppressedViolations = listOf(inSourceViolation, programmaticViolation, baselineViolation),
            )

        val jsonReport = JsonReportExporter.generateReport(listOf(eval))
        val jsonContent = JsonReportExporter.exportToString(jsonReport)
        assertTrue(jsonContent.contains("\"isSuppressed\": true") || jsonContent.contains("\"isSuppressed\":true"))
        assertTrue(jsonContent.contains("in_source"))
        assertTrue(jsonContent.contains("programmatic"))
        assertTrue(jsonContent.contains("baseline"))

        val sarifReport = SarifReportExporter.generateReport(listOf(eval))
        val sarifContent = SarifReportExporter.exportToString(sarifReport)
        assertTrue(sarifContent.contains("\"kind\": \"inSource\""))
        assertTrue(sarifContent.contains("\"kind\": \"external\""))
    }

    @Test
    fun `test nested class inherits enclosing class in-source suppression in classes rule`() {
        val outerClass =
            ClassDeclaration(
                name = "Outer",
                fqName = "com.example.Outer",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:classes.rule\"")),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Outer.kt",
            )
        val innerClass =
            ClassDeclaration(
                name = "Inner",
                fqName = "com.example.Outer.Inner",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Outer.kt",
            )
        val fileDecl =
            FileDeclaration(
                "Outer.kt",
                "com.example",
                filePath = "/src/Outer.kt",
                classes = listOf(outerClass, innerClass),
            )
        val mod =
            Module(
                buildId = ":",
                path = ":testModule",
                projectDir = "testModule",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        ClassesRuleBuilder(graph)
            .that()
            .haveName("Inner")
            .should()
            .beInterfaces()
            .check()
    }

    @Test
    fun `test slice granular suppression does not suppress unrelated cycle`() {
        val classA =
            ClassDeclaration(
                name = "ServiceA",
                fqName = "com.example.featureA.ServiceA",
                packageName = "com.example.featureA",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = setOf("com.example.featureB.ServiceB"),
                filePath = "/src/ServiceA.kt",
            )
        val classB =
            ClassDeclaration(
                name = "ServiceB",
                fqName = "com.example.featureB.ServiceB",
                packageName = "com.example.featureB",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = setOf("com.example.featureA.ServiceA"),
                filePath = "/src/ServiceB.kt",
            )
        val fileA =
            FileDeclaration(
                "ServiceA.kt",
                "com.example.featureA",
                filePath = "/src/ServiceA.kt",
                classes = listOf(classA),
            )
        val fileB =
            FileDeclaration(
                "ServiceB.kt",
                "com.example.featureB",
                filePath = "/src/ServiceB.kt",
                classes = listOf(classB),
            )
        val mod =
            Module(
                buildId = ":",
                path = ":testModule",
                projectDir = "testModule",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileA, fileB),
            )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        assertThrows(AssertionError::class.java) {
            SlicesRuleBuilder(graph)
                .matching("com.example.(*)..")
                .suppress {
                    slice("other", reason = "Mute unrelated slice")
                }
                .should()
                .beFreeOfCycles()
                .check()
        }

        SlicesRuleBuilder(graph)
            .matching("com.example.(*)..")
            .suppress {
                slice("featureA", reason = "Mute featureA")
            }
            .should()
            .beFreeOfCycles()
            .check()
    }

    @Test
    fun `test baseline generation excludes already suppressed violations`() {
        val suppressedClass =
            ClassDeclaration(
                name = "SuppressedClass",
                fqName = "com.example.SuppressedClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "Suppress",
                            fqName = "kotlin.Suppress",
                            arguments = listOf(AnnotationArgumentDeclaration(null, "\"konture:classes.rule\"")),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/SuppressedClass.kt",
            )
        val unsuppressedClass =
            ClassDeclaration(
                name = "UnsuppressedClass",
                fqName = "com.example.UnsuppressedClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/UnsuppressedClass.kt",
            )
        val fileDecl =
            FileDeclaration(
                "Classes.kt",
                "com.example",
                filePath = "/src/Classes.kt",
                classes = listOf(suppressedClass, unsuppressedClass),
            )
        val mod =
            Module(
                buildId = ":",
                path = ":testModule",
                projectDir = "testModule",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(builds = mapOf(":" to listOf(mod)))

        val tempDir = java.nio.file.Files.createTempDirectory("konture_suppression_baseline_test").toFile()
        tempDir.deleteOnExit()
        System.setProperty(Konture.PROPERTY_BASELINE_DIR, tempDir.absolutePath)
        System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "true")
        try {
            BaselineManager.resetForTest()
            ClassesRuleBuilder(graph)
                .that()
                .resideInAPackage("com.example")
                .should()
                .beInterfaces()
                .check()

            val recorded = KontureRuntimeStateProvider.currentState.baselineManager.recordedViolations
            org.junit.jupiter.api.Assertions.assertEquals(1, recorded.size)
            assertTrue(recorded.first().message.contains("UnsuppressedClass"))
            assertTrue(!recorded.first().message.contains("SuppressedClass"))
        } finally {
            System.clearProperty(Konture.PROPERTY_BASELINE_DIR)
            System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
            BaselineManager.resetForTest()
        }
    }
}
