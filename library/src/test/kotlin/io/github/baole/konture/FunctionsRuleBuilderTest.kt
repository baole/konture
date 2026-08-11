/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionsRuleBuilderTest : KontureScopeTestFixture() {
    @Test
    fun `test functions rule builder filtering and assertions`() {
        val f1 =
            FunctionDeclaration(
                name = "getUserName",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.OPERATOR),
                returnType = "kotlin.String",
                parameters =
                    listOf(
                        ParameterDeclaration("userId", "kotlin.Int", hasDefaultValue = false, annotations = emptyList()),
                    ),
                annotations =
                    listOf(
                        AnnotationDeclaration("Deprecated", "kotlin.Deprecated"),
                    ),
                isExtension = false,
                kdocText = null,
            )
        val fileDecl =
            FileDeclaration(
                name = "UserFunctions.kt",
                packageName = "com.example.user",
                classes = emptyList(),
                topLevelFunctions = listOf(f1),
                filePath = "/src/UserFunctions.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":user",
                projectDir = "user",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))
        val context = FunctionDeclarationContext(f1, "com.example.user", null, ":user", "/src/UserFunctions.kt")

        val thatFilter =
            FunctionsRuleBuilder(graph)
                .that()
                .haveName("getUserName")
                .and()
                .resideInAPackage("com.example.user")
                .and()
                .resideInAModule(":user")
                .and()
                .haveParameterTypes("kotlin.Int")
                .and()
                .haveReturnType("kotlin.String")
                .and()
                .beOperator()
                .and()
                .bePublic()
                .and()
                .haveAnnotationOf("kotlin.Deprecated")
                .getThatPredicate()!!

        assertTrue(thatFilter(context))

        val shouldAssertion =
            FunctionsRuleBuilder(graph)
                .should()
                .haveName("getUserName")
                .andShould()
                .resideInAPackage("com.example.user")
                .andShould()
                .haveParameterTypes("kotlin.Int")
                .andShould()
                .haveReturnType("kotlin.String")
                .andShould()
                .notBeExtension()
                .andShould()
                .beOperator()
                .andShould()
                .notBeInfix()
                .andShould()
                .notBeInline()
                .andShould()
                .notBeSuspend()
                .andShould()
                .notBeOpen()
                .andShould()
                .bePublic()
                .andShould()
                .notBePrivate()
                .andShould()
                .notBeProtected()
                .andShould()
                .notBeInternal()
                .andShould()
                .haveAnnotationOf("kotlin.Deprecated")
                .getShouldAssertion()!!

        val violations = mutableListOf<String>()
        shouldAssertion(context, emptyList(), violations)
        assertTrue(violations.isEmpty(), "Violations found: $violations")
    }

    @Test
    fun `test functions rule builder logic gates and other predicates`() {
        val f2 =
            FunctionDeclaration(
                name = "processData",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.SUSPEND, Modifier.INLINE),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                isExtension = true,
                kdocText = "Documentation for processData",
            )
        val fileDecl2 =
            FileDeclaration(
                name = "ProcessData.kt",
                packageName = "com.example.process",
                classes = emptyList(),
                topLevelFunctions = listOf(f2),
                filePath = "/src/ProcessData.kt",
            )
        val mockModule2 =
            Module(
                buildId = ":",
                path = ":process",
                projectDir = "process",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl2),
            )
        val graph2 = ProjectGraph(mapOf(":" to listOf(mockModule2)))
        val context2 = FunctionDeclarationContext(f2, "com.example.process", null, ":process", "/src/ProcessData.kt")

        val thatFilter2 =
            FunctionsRuleBuilder(graph2)
                .that()
                .areExtension()
                .and()
                .beSuspend()
                .and()
                .beInline()
                .and()
                .beInternal()
                .getThatPredicate()!!

        assertTrue(thatFilter2(context2))

        val shouldAssertion2 =
            FunctionsRuleBuilder(graph2)
                .should()
                .beExtension()
                .andShould()
                .beSuspend()
                .andShould()
                .beInline()
                .andShould()
                .beInternal()
                .andShould()
                .beDocumentedWithKDoc()
                .andShould()
                .haveNoParameters()
                .getShouldAssertion()!!

        val violations2 = mutableListOf<String>()
        shouldAssertion2(context2, emptyList(), violations2)
        assertTrue(violations2.isEmpty(), "Violations found: $violations2")
    }

    @Test
    fun `test functions rule builder negative assertions and failures`() {
        val f3 =
            FunctionDeclaration(
                name = "doSomething",
                visibility = Visibility.PRIVATE,
                modifiers = setOf(Modifier.OPEN),
                returnType = "kotlin.Boolean",
                parameters =
                    listOf(
                        ParameterDeclaration("flag", "kotlin.Boolean", hasDefaultValue = true, annotations = emptyList()),
                    ),
                annotations =
                    listOf(
                        AnnotationDeclaration("TestAnno", "com.example.TestAnno"),
                    ),
                isExtension = false,
                kdocText = null,
            )
        val fileDecl3 =
            FileDeclaration(
                name = "DoSomething.kt",
                packageName = "com.example.test",
                classes = emptyList(),
                topLevelFunctions = listOf(f3),
                filePath = "/src/DoSomething.kt",
            )
        val mockModule3 =
            Module(
                buildId = ":",
                path = ":test",
                projectDir = "test",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl3),
            )
        val graph3 = ProjectGraph(mapOf(":" to listOf(mockModule3)))
        val context3 = FunctionDeclarationContext(f3, "com.example.test", null, ":test", "/src/DoSomething.kt")

        val shouldFailAssertion =
            FunctionsRuleBuilder(graph3)
                .should()
                .haveName("wrongName")
                .andShould()
                .resideInAPackage("wrong.pkg")
                .andShould()
                .resideInAModule(":wrong")
                .andShould()
                .haveParameterTypes("kotlin.String")
                .andShould()
                .haveReturnType("kotlin.Int")
                .andShould()
                .bePublic()
                .andShould()
                .notBePrivate()
                .andShould()
                .beProtected()
                .andShould()
                .beInternal()
                .andShould()
                .beExtension()
                .andShould()
                .beOperator()
                .andShould()
                .beInfix()
                .andShould()
                .beInline()
                .andShould()
                .beSuspend()
                .andShould()
                .notBeOpen()
                .andShould()
                .beDocumentedWithKDoc()
                .andShould()
                .haveNoParameters()
                .getShouldAssertion()!!

        val violations3 = mutableListOf<String>()
        shouldFailAssertion(context3, emptyList(), violations3)
        assertFalse(violations3.isEmpty(), "Expected violations but found none")
    }
}
