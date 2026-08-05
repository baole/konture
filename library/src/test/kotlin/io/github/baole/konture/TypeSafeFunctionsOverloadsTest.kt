/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TypeSafeFunctionsOverloadsTest {
    @Test
    fun `typed function signatures match simple and parameterized source types`() {
        val function =
            FunctionDeclaration(
                name = "load",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "List<String>",
                parameters =
                    listOf(
                        ParameterDeclaration(
                            "id",
                            "String",
                            hasDefaultValue = false,
                            annotations = emptyList(),
                            resolvedType = "kotlin.String",
                        ),
                    ),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                resolvedReturnType = "kotlin.collections.List",
            )
        val graph = graphWith(function = function)
        val context = FunctionDeclarationContext(function, "example", null, ":app", "/src/Example.kt")

        val returnRule = FunctionsRuleBuilder(graph).that().haveReturnType(List::class)
        val parameterRule = FunctionsRuleBuilder(graph).that().haveParameterTypes(String::class)

        assertTrue(returnRule.getThatPredicate()!!(context))
        assertTrue(parameterRule.getThatPredicate()!!(context))
    }

    @Test
    fun `comprehensive functions type-safe overloads test`() {
        val packageName = TypeSafeMarker::class.java.packageName
        val function =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters = emptyList(),
                annotations = listOf(AnnotationDeclaration("TypeSafeMarker", TypeSafeMarker::class.qualifiedName!!)),
                kdocText = null,
                isExtension = false,
                resolvedReturnType = "kotlin.String",
            )
        val graph = graphWith(function = function, packageName = packageName)
        val context = FunctionDeclarationContext(function, packageName, null, ":app", "/src/Example.kt")

        // FunctionsThat
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveAnnotationOfType<TypeSafeMarker>().getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().haveAnyAnnotationOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(
            FunctionsRuleBuilder(graph).that().resideInPackageOf(TypeSafeMarker::class).getThatPredicate()!!(context),
        )
        assertTrue(FunctionsRuleBuilder(graph).that().resideInPackageOf<TypeSafeMarker>().getThatPredicate()!!(context))

        // FunctionsShould
        val violations = mutableListOf<String>()

        FunctionsRuleBuilder(
            graph,
        ).should().haveAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(
            graph,
        ).should().haveAnnotationOfType<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(
            graph,
        ).should().resideInPackageOf(TypeSafeMarker::class).getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())

        FunctionsRuleBuilder(
            graph,
        ).should().resideInPackageOf<TypeSafeMarker>().getShouldAssertion()!!(context, emptyList(), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `reified functions and properties usage overloads match referenced classes`() {
        val funcUsage =
            SourceUsage(
                UsageKind.CLASS_REFERENCE,
                "kotlin.String",
                "/src/Example.kt",
                1,
                1,
                enclosingFunction = "myFunc",
            )
        val propCallUsage =
            SourceUsage(UsageKind.CALL, "kotlin.String", "/src/Example.kt", 2, 1, enclosingProperty = "myProp")
        val propRefUsage =
            SourceUsage(
                UsageKind.CLASS_REFERENCE,
                "kotlin.String",
                "/src/Example.kt",
                3,
                1,
                enclosingProperty = "myProp",
            )

        val func =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val prop =
            PropertyDeclaration(
                name = "myProp",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )

        val file =
            FileDeclaration(
                name = "Example.kt",
                packageName = "example",
                topLevelFunctions = listOf(func),
                topLevelProperties = listOf(prop),
                filePath = "/src/Example.kt",
                usages = listOf(funcUsage, propCallUsage, propRefUsage),
            )
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file)))),
            )

        assertThrows(AssertionError::class.java) {
            FunctionsRuleBuilder(graph).should().notReferenceClass<String>().check()
        }

        assertThrows(AssertionError::class.java) {
            PropertiesRuleBuilder(graph).should().notCall<String>().check()
        }

        assertThrows(AssertionError::class.java) {
            PropertiesRuleBuilder(graph).should().notReferenceClass<String>().check()
        }
    }
}
