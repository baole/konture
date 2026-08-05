/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FunctionsShouldCallAssertionsTest : RuleBuildersTestBase() {
    @Test
    fun `test notCall with string fqName, KClass, and reified type`() {
        val func =
            FunctionDeclaration(
                name = "doSomething",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val usageCall =
            SourceUsage(
                kind = UsageKind.CALL,
                targetFqName = "com.example.Service.process",
                rawExpression = "process()",
                filePath = "/src/Func.kt",
                line = 10,
                column = 5,
            )
        val funcCtx =
            FunctionDeclarationContext(
                declaration = func,
                packageName = "com.example",
                className = "MyClass",
                modulePath = ":app",
                filePath = "/src/Func.kt",
                usages = listOf(usageCall),
            )

        // String overload
        val ruleString = FunctionsRuleBuilder(projectGraph).should().notCall("com.example.Service.process")
        val violationsString = mutableListOf<String>()
        ruleString.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsString)
        assertEquals(1, violationsString.size)
        assertTrue(violationsString[0].contains("process()"))

        // KClass overload
        val ruleKClass = FunctionsRuleBuilder(projectGraph).should().notCall(String::class)
        val violationsKClass = mutableListOf<String>()
        ruleKClass.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsKClass)
        assertTrue(violationsKClass.isEmpty())

        // Reified overload
        val ruleReified = FunctionsRuleBuilder(projectGraph).should().notCall<String>()
        val violationsReified = mutableListOf<String>()
        ruleReified.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsReified)
        assertTrue(violationsReified.isEmpty())
    }

    @Test
    fun `test notReferenceClass with string fqName and KClass`() {
        val func =
            FunctionDeclaration(
                name = "processData",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val usageRef =
            SourceUsage(
                kind = UsageKind.CLASS_REFERENCE,
                targetFqName = "com.example.DTO",
                rawExpression = "DTO",
                filePath = "/src/Func.kt",
                line = 8,
                column = 12,
            )
        val funcCtx =
            FunctionDeclarationContext(
                declaration = func,
                packageName = "com.example",
                className = "MyClass",
                modulePath = ":app",
                filePath = "/src/Func.kt",
                usages = listOf(usageRef),
            )

        val ruleString = FunctionsRuleBuilder(projectGraph).should().notReferenceClass("com.example.DTO")
        val violationsString = mutableListOf<String>()
        ruleString.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsString)
        assertEquals(1, violationsString.size)
        assertTrue(violationsString[0].contains("DTO"))

        val ruleKClass = FunctionsRuleBuilder(projectGraph).should().notReferenceClass(String::class)
        val violationsKClass = mutableListOf<String>()
        ruleKClass.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsKClass)
        assertTrue(violationsKClass.isEmpty())
    }
}
