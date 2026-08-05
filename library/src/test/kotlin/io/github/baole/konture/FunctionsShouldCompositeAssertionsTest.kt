/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FunctionsShouldCompositeAssertionsTest : RuleBuildersTestBase() {
    @Test
    fun `test satisfy overloads`() {
        val func =
            FunctionDeclaration(
                name = "calculate",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Int",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "Calculator", ":app", "/src/Calc.kt")

        // 1. Boolean predicate
        val rulePred = FunctionsRuleBuilder(projectGraph).should().satisfy { it.declaration.name == "calculate" }
        val violationsPred = mutableListOf<String>()
        rulePred.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsPred)
        assertTrue(violationsPred.isEmpty())

        // 2. Description + predicate
        val ruleDesc =
            FunctionsRuleBuilder(
                projectGraph,
            ).should().satisfy("return type is String") { f, _ -> f.declaration.returnType == "String" }
        val violationsDesc = mutableListOf<String>()
        ruleDesc.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsDesc)
        assertEquals(1, violationsDesc.size)
        assertTrue(violationsDesc[0].contains("return type is String"))

        // 3. Lambda with violations
        val ruleLambda =
            FunctionsRuleBuilder(projectGraph).should().satisfy { f, violations ->
                if (f.declaration.visibility != Visibility.PUBLIC) {
                    violations.add("Must be public")
                }
            }
        val violationsLambda = mutableListOf<String>()
        ruleLambda.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsLambda)
        assertTrue(violationsLambda.isEmpty())
    }

    @Test
    fun `test anyOf allOf noneOf composite assertions`() {
        val func =
            FunctionDeclaration(
                name = "process",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "Service", ":app", "/src/Service.kt")

        // anyOf
        val ruleAny =
            FunctionsRuleBuilder(projectGraph).should().anyOf(
                { resideInAPackage("com.example") },
                { resideInAPackage("com.other") },
            )
        val violationsAny = mutableListOf<String>()
        ruleAny.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsAny)
        assertTrue(violationsAny.isEmpty())

        val ruleAnyFail =
            FunctionsRuleBuilder(projectGraph).should().anyOf(
                { resideInAPackage("com.other") },
                { resideInAPackage("com.none") },
            )
        val violationsAnyFail = mutableListOf<String>()
        ruleAnyFail.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsAnyFail)
        assertEquals(1, violationsAnyFail.size)

        // allOf
        val ruleAll =
            FunctionsRuleBuilder(projectGraph).should().allOf(
                { resideInAPackage("com.example") },
                { haveNameStartingWith("pro") },
            )
        val violationsAll = mutableListOf<String>()
        ruleAll.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsAll)
        assertTrue(violationsAll.isEmpty())

        // noneOf
        val ruleNone =
            FunctionsRuleBuilder(projectGraph).should().noneOf(
                { resideInAPackage("com.other") },
                { haveNameStartingWith("bad") },
            )
        val violationsNone = mutableListOf<String>()
        ruleNone.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsNone)
        assertTrue(violationsNone.isEmpty())

        val ruleNoneFail =
            FunctionsRuleBuilder(projectGraph).should().noneOf(
                { resideInAPackage("com.example") },
            )
        val violationsNoneFail = mutableListOf<String>()
        ruleNoneFail.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsNoneFail)
        assertEquals(1, violationsNoneFail.size)
    }
}
