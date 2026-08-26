/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionsRuleBuilderViolationLocationTest : KontureScopeTestFixture() {
    @Test
    fun `notCall violations use usage source location instead of declaration location`() {
        val function =
            FunctionDeclaration(
                name = "authenticate",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                sourceStartOffset = 100,
                sourceEndOffset = 200,
                sourceLine = 26,
            )

        val usage =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "org.jetbrains.compose.resources.getString",
                filePath = "/src/BiometricHelper.kt",
                line = 132,
                column = 21,
                rawExpression = "getString",
                unresolvedPossibleUsage = true,
                enclosingFunctionStartOffset = 100,
                enclosingFunctionEndOffset = 200,
            )

        val file =
            FileDeclaration(
                name = "BiometricHelper.kt",
                packageName = "com.example.auth",
                topLevelFunctions = listOf(function),
                filePath = "/src/BiometricHelper.kt",
                usages = listOf(usage),
            )

        val module =
            Module(
                buildId = ":",
                path = ":feature-authentication",
                projectDir = "feature-authentication",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(file),
            )

        val graph = ProjectGraph(mapOf(":" to listOf(module)))

        val error =
            assertThrows(AssertionError::class.java) {
                FunctionsRuleBuilder(graph)
                    .that()
                    .haveName("authenticate")
                    .should()
                    .notCall("org.jetbrains.compose.resources.getString")
                    .check()
            }

        val message = error.message.orEmpty()
        assertTrue(message.contains("File: /src/BiometricHelper.kt:132"))
        assertTrue(!message.contains("File: /src/BiometricHelper.kt:26"))
    }
}
