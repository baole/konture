/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DslParityTest : RuleBuildersTestBase() {
    @Test
    fun `test classes DSL parity - resideInAModule, notCall, and notReferenceClass`() {
        val spykCall =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "io.mockk.spyk",
                filePath = "/src/TestClass.kt",
                line = 10,
                column = 5,
                enclosingClass = "com.example.TestClass",
                rawExpression = "spyk()",
            )
        val refUsage =
            SourceUsage(
                kind = UsageKind.CLASS_REFERENCE,
                targetFqName = "com.example.ForbiddenType",
                filePath = "/src/TestClass.kt",
                line = 12,
                column = 5,
                enclosingClass = "com.example.TestClass",
                rawExpression = "ForbiddenType",
            )

        val testClass =
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
                modulePath = ":featureModule",
                usages = listOf(spykCall, refUsage),
            )

        val testFile =
            FileDeclaration(
                name = "TestClass.kt",
                packageName = "com.example",
                classes = listOf(testClass),
                filePath = "/src/TestClass.kt",
                usages = listOf(spykCall, refUsage),
            )

        val testModule =
            Module(
                buildId = ":",
                path = ":featureModule",
                projectDir = "featureModule",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(testFile),
            )

        val graph = ProjectGraph(builds = mapOf(":" to listOf(testModule)))

        // 1. resideInAModule filter
        val moduleRule = ClassesRuleBuilder(graph).that().resideInAModule(":featureModule")
        val pred = moduleRule.getThatPredicate()!!
        assertTrue(pred(testClass))

        // 2. notCall assertion
        val notCallRule = ClassesRuleBuilder(graph).should().notCall("io.mockk.spyk")
        val vCall = mutableListOf<String>()
        notCallRule.getShouldAssertion()!!(testClass, listOf(testClass), vCall)
        assertEquals(1, vCall.size)
        assertTrue(vCall[0].contains("io.mockk.spyk"))

        // 3. notReferenceClass assertion
        val notRefRule = ClassesRuleBuilder(graph).should().notReferenceClass("com.example.ForbiddenType")
        val vRef = mutableListOf<String>()
        notRefRule.getShouldAssertion()!!(testClass, listOf(testClass), vRef)
        assertEquals(1, vRef.size)
        assertTrue(vRef[0].contains("com.example.ForbiddenType"))
    }

    @Test
    fun `test functions DSL parity - resideInAModule, belongToClass, notReferenceClass, and haveName`() {
        val func =
            FunctionDeclaration(
                name = "getUser",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "User",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcContext =
            FunctionDeclarationContext(
                declaration = func,
                packageName = "com.example",
                className = "UserService",
                modulePath = ":coreModule",
                filePath = "/src/UserService.kt",
                usages =
                    listOf(
                        SourceUsage(
                            kind = UsageKind.CLASS_REFERENCE,
                            targetFqName = "com.internal.InternalApi",
                            filePath = "/src/UserService.kt",
                            line = 15,
                            column = 8,
                            rawExpression = "InternalApi",
                        ),
                    ),
            )

        // 1. resideInAModule filter
        val moduleRule = FunctionsRuleBuilder(projectGraph).that().resideInAModule(":coreModule")
        assertTrue(moduleRule.getThatPredicate()!!(funcContext))

        // 2. belongToClass filter
        val belongRule = FunctionsRuleBuilder(projectGraph).that().belongToClass("UserService")
        assertTrue(belongRule.getThatPredicate()!!(funcContext))

        // 3. haveName filter
        val nameFilterRule = FunctionsRuleBuilder(projectGraph).that().haveName { it.startsWith("get") }
        assertTrue(nameFilterRule.getThatPredicate()!!(funcContext))

        // 4. notReferenceClass assertion
        val notRefRule = FunctionsRuleBuilder(projectGraph).should().notReferenceClass("com.internal.InternalApi")
        val vRef = mutableListOf<String>()
        notRefRule.getShouldAssertion()!!(funcContext, listOf(funcContext), vRef)
        assertEquals(1, vRef.size)
        assertTrue(vRef[0].contains("InternalApi"))

        // 5. haveName assertion
        val nameAssertionRule = FunctionsRuleBuilder(projectGraph).should().haveName("start with get") { it.startsWith("get") }
        val vName = mutableListOf<String>()
        nameAssertionRule.getShouldAssertion()!!(funcContext, listOf(funcContext), vName)
        assertTrue(vName.isEmpty())
    }

    @Test
    fun `test properties DSL parity - resideInAModule, belongToClass, notCall, notReferenceClass, and haveName`() {
        val prop =
            PropertyDeclaration(
                name = "logger",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "Logger",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )
        val propContext =
            PropertyDeclarationContext(
                declaration = prop,
                packageName = "com.example",
                className = "OrderService",
                modulePath = ":appModule",
                filePath = "/src/OrderService.kt",
                usages =
                    listOf(
                        SourceUsage(
                            kind = UsageKind.CALL,
                            targetFqName = "io.mockk.spyk",
                            filePath = "/src/OrderService.kt",
                            line = 5,
                            column = 12,
                            rawExpression = "spyk()",
                        ),
                        SourceUsage(
                            kind = UsageKind.CLASS_REFERENCE,
                            targetFqName = "com.internal.InternalConfig",
                            filePath = "/src/OrderService.kt",
                            line = 6,
                            column = 12,
                            rawExpression = "InternalConfig",
                        ),
                    ),
            )

        // 1. resideInAModule filter
        val moduleRule = PropertiesRuleBuilder(projectGraph).that().resideInAModule(":appModule")
        assertTrue(moduleRule.getThatPredicate()!!(propContext))

        // 2. belongToClass filter
        val belongRule = PropertiesRuleBuilder(projectGraph).that().belongToClass("OrderService")
        assertTrue(belongRule.getThatPredicate()!!(propContext))

        // 3. haveName filter
        val nameFilterRule = PropertiesRuleBuilder(projectGraph).that().haveName { it == "logger" }
        assertTrue(nameFilterRule.getThatPredicate()!!(propContext))

        // 4. notCall assertion
        val notCallRule = PropertiesRuleBuilder(projectGraph).should().notCall("io.mockk.spyk")
        val vCall = mutableListOf<String>()
        notCallRule.getShouldAssertion()!!(propContext, listOf(propContext), vCall)
        assertEquals(1, vCall.size)
        assertTrue(vCall[0].contains("spyk"))

        // 5. notReferenceClass assertion
        val notRefRule = PropertiesRuleBuilder(projectGraph).should().notReferenceClass("com.internal.InternalConfig")
        val vRef = mutableListOf<String>()
        notRefRule.getShouldAssertion()!!(propContext, listOf(propContext), vRef)
        assertEquals(1, vRef.size)
        assertTrue(vRef[0].contains("InternalConfig"))

        // 6. haveName assertion
        val nameAssertRule = PropertiesRuleBuilder(projectGraph).should().haveName("lower camel case") { it[0].isLowerCase() }
        val vName = mutableListOf<String>()
        nameAssertRule.getShouldAssertion()!!(propContext, listOf(propContext), vName)
        assertTrue(vName.isEmpty())
    }

    @Test
    fun `test files DSL parity - file annotations and custom name predicate`() {
        val fileDecl =
            FileDeclaration(
                name = "App.kt",
                packageName = "com.example",
                filePath = "/src/App.kt",
                annotations =
                    listOf(
                        AnnotationDeclaration("OptIn", "kotlin.OptIn"),
                    ),
            )
        val fileContext =
            FileDeclarationContext(
                declaration = fileDecl,
                modulePath = ":app",
            )

        // 1. file annotations filter
        val annoRule = FilesRuleBuilder(projectGraph).that().haveAnnotationOf("OptIn")
        assertTrue(annoRule.getThatPredicate()!!(fileContext))

        val allAnnosRule = FilesRuleBuilder(projectGraph).that().haveAllAnnotationsOf("OptIn")
        assertTrue(allAnnosRule.getThatPredicate()!!(fileContext))

        val anyAnnosRule = FilesRuleBuilder(projectGraph).that().haveAnyAnnotationOf("OptIn", "Suppress")
        assertTrue(anyAnnosRule.getThatPredicate()!!(fileContext))

        // 2. custom name filter & assertion
        val nameFilterRule = FilesRuleBuilder(projectGraph).that().haveName { it.endsWith(".kt") }
        assertTrue(nameFilterRule.getThatPredicate()!!(fileContext))

        val nameAssertRule = FilesRuleBuilder(projectGraph).should().haveName("end with .kt") { it.endsWith(".kt") }
        val vName = mutableListOf<String>()
        nameAssertRule.getShouldAssertion()!!(fileContext, listOf(fileContext), vName)
        assertTrue(vName.isEmpty())
    }
}
