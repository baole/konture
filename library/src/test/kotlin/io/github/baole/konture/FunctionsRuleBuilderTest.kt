/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

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

        // 1. That filtering
        val thatFilter =
            FunctionsRuleBuilder(graph)
                .that()
                .haveName("getUserName")
                .and()
                .resideInAPackage("com.example.user")
                .and()
                .resideInAModule(":user")
                .and()
                .haveParameterOf("kotlin.Int")
                .and()
                .notHaveParameterOf("kotlin.String")
                .and()
                .haveReturnType("kotlin.String")
                .and()
                .notHaveReturnType("kotlin.Int")
                .and()
                .notBeExtension()
                .and()
                .areOperator()
                .and()
                .haveAnnotationOf("kotlin.Deprecated")
                .and()
                .notHaveAnnotationOf("kotlin.Suppress")
                .and()
                .arePublic()
                .getThatPredicate()!!

        assertTrue(thatFilter(context))

        // 2. Should assertions
        val shouldAssertion =
            FunctionsRuleBuilder(graph)
                .should()
                .haveName("getUserName")
                .andShould()
                .resideInAPackage("com.example.user")
                .andShould()
                .haveParameterOf("kotlin.Int")
                .andShould()
                .notHaveParameterOf("kotlin.String")
                .andShould()
                .haveReturnType("kotlin.String")
                .andShould()
                .notHaveReturnType("kotlin.Int")
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
                .andShould()
                .notHaveAnnotationOf("kotlin.Suppress")
                .getShouldAssertion()!!

        val violations = mutableListOf<String>()
        shouldAssertion(context, emptyList(), violations)
        assertTrue(violations.isEmpty(), "Violations found: $violations")
    }

    @Test
    fun `test functions rule builder logic gates and other predicates`() {
        val f2 =
            FunctionDeclaration(
                name = "internalHelper",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.INLINE, Modifier.SUSPEND),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                isExtension = true,
                kdocText = null,
            )
        val fileDecl =
            FileDeclaration(
                name = "UserFunctions.kt",
                packageName = "com.example.user",
                classes = emptyList(),
                topLevelFunctions = listOf(f2),
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
        val context = FunctionDeclarationContext(f2, "com.example.user", null, ":user", "/src/UserFunctions.kt")

        // 1. AnyOf filtering
        val anyOfFilter =
            FunctionsRuleBuilder(graph)
                .that()
                .anyOf(
                    { haveName("getUserName") },
                    { haveName("internalHelper") },
                ).getThatPredicate()!!

        assertTrue(anyOfFilter(context))

        // 2. AllOf filtering
        val allOfFilter =
            FunctionsRuleBuilder(graph)
                .that()
                .allOf(
                    { beInternal() },
                    { beInline() },
                ).getThatPredicate()!!

        assertTrue(allOfFilter(context))

        // 3. NoneOf filtering
        val noneOfFilter =
            FunctionsRuleBuilder(graph)
                .that()
                .noneOf(
                    { bePublic() },
                    { beOperator() },
                ).getThatPredicate()!!

        assertTrue(noneOfFilter(context))

        // 4. Assertions on f2
        val shouldAssertion =
            FunctionsRuleBuilder(graph)
                .should()
                .beInternal()
                .andShould()
                .beInline()
                .andShould()
                .beSuspend()
                .andShould()
                .beExtension()
                .getShouldAssertion()!!

        val violations = mutableListOf<String>()
        shouldAssertion(context, emptyList(), violations)
        assertTrue(violations.isEmpty(), "Violations found: $violations")
    }

    @Test
    fun `test functions rule builder multi-parameter rules`() {
        val f3 =
            FunctionDeclaration(
                name = "multiParamFunc",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.OPEN),
                returnType = "kotlin.Boolean",
                parameters =
                    listOf(
                        ParameterDeclaration("p1", "kotlin.String", hasDefaultValue = false, annotations = emptyList()),
                        ParameterDeclaration("p2", "kotlin.Int", hasDefaultValue = false, annotations = emptyList()),
                    ),
                annotations = emptyList(),
                isExtension = false,
                kdocText = null,
            )
        val fileDecl =
            FileDeclaration(
                name = "UserFunctions.kt",
                packageName = "com.example.user",
                classes = emptyList(),
                topLevelFunctions = listOf(f3),
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
        val context = FunctionDeclarationContext(f3, "com.example.user", null, ":user", "/src/UserFunctions.kt")

        // 1. Parameter count check
        val countFilter = FunctionsRuleBuilder(graph).that().haveParameterCount(2).getThatPredicate()!!
        assertTrue(countFilter(context))

        val countPredFilter = FunctionsRuleBuilder(graph).that().haveParameterCount { it > 1 }.getThatPredicate()!!
        assertTrue(countPredFilter(context))

        // 2. Exact parameter types check
        val exactParamsFilter =
            FunctionsRuleBuilder(
                graph,
            ).that().haveParameterTypes("kotlin.String", "kotlin.Int").getThatPredicate()!!
        assertTrue(exactParamsFilter(context))

        val exactParamsFilterMismatch =
            FunctionsRuleBuilder(graph).that().haveParameterTypes("kotlin.String").getThatPredicate()!!
        assertFalse(exactParamsFilterMismatch(context))

        // 3. Any parameter type check
        val anyParamFilter =
            FunctionsRuleBuilder(graph).that().haveAnyParameterType("kotlin.Int").getThatPredicate()!!
        assertTrue(anyParamFilter(context))

        val anyParamFilter2 =
            FunctionsRuleBuilder(
                graph,
            ).that().haveAnyParameterType("kotlin.Double", "kotlin.Int").getThatPredicate()!!
        assertTrue(anyParamFilter2(context))

        val anyParamFilterMismatch =
            FunctionsRuleBuilder(graph).that().haveAnyParameterType("kotlin.Double").getThatPredicate()!!
        assertFalse(anyParamFilterMismatch(context))

        // 4. Assertions for multi-parameters
        val assertCount = FunctionsRuleBuilder(graph).should().haveParameterCount(2).getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertCount(context, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertCountPred =
            FunctionsRuleBuilder(graph).should().haveParameterCount { it == 2 }.getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertCountPred(context, emptyList(), v2)
        assertTrue(v2.isEmpty())

        val assertExactParams =
            FunctionsRuleBuilder(
                graph,
            ).should().haveParameterTypes("kotlin.String", "kotlin.Int").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertExactParams(context, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val assertExactParamsList =
            FunctionsRuleBuilder(
                graph,
            ).should().haveParameterTypes(listOf("kotlin.String", "kotlin.Int")).getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertExactParamsList(context, emptyList(), v4)
        assertTrue(v4.isEmpty())

        val assertAnyParamSingle =
            FunctionsRuleBuilder(graph).should().haveAnyParameterType("kotlin.Int").getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertAnyParamSingle(context, emptyList(), v5)
        assertTrue(v5.isEmpty())

        val assertAnyParam =
            FunctionsRuleBuilder(
                graph,
            ).should().haveAnyParameterType("kotlin.Long", "kotlin.Int").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertAnyParam(context, emptyList(), v6)
        assertTrue(v6.isEmpty())
    }
}
