/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FunctionsShouldNameAssertionsTest : RuleBuildersTestBase() {
    @Test
    fun `test resideInAPackage overloads and resideInPackageOf`() {
        val func =
            FunctionDeclaration(
                "testFunc",
                Visibility.PUBLIC,
                emptySet(),
                "Unit",
                emptyList(),
                emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "TestClass", ":app", "/src/Test.kt")

        // string pattern
        val ruleSingle = FunctionsRuleBuilder(projectGraph).should().resideInAPackage("com.example")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        // list
        val ruleList = FunctionsRuleBuilder(projectGraph).should().resideInAPackage(listOf("com.example", "com.other"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsList)
        assertTrue(violationsList.isEmpty())

        // vararg
        val ruleVararg = FunctionsRuleBuilder(projectGraph).should().resideInAPackage("com.other", "com.none")
        val violationsVararg = mutableListOf<String>()
        ruleVararg.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsVararg)
        assertEquals(1, violationsVararg.size)

        // predicate
        val rulePred = FunctionsRuleBuilder(projectGraph).should().resideInAPackage { it.startsWith("com") }
        val violationsPred = mutableListOf<String>()
        rulePred.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsPred)
        assertTrue(violationsPred.isEmpty())

        // resideInPackageOf
        val rulePkgOf = FunctionsRuleBuilder(projectGraph).should().resideInPackageOf(String::class)
        val violationsPkgOf = mutableListOf<String>()
        rulePkgOf.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsPkgOf)
        assertEquals(1, violationsPkgOf.size) // String package is kotlin
    }

    @Test
    fun `test haveNameEndingWith overloads`() {
        val func =
            FunctionDeclaration(
                "getData",
                Visibility.PUBLIC,
                emptySet(),
                "Unit",
                emptyList(),
                emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "Service", ":app", "/src/Service.kt")

        val ruleSingle = FunctionsRuleBuilder(projectGraph).should().haveNameEndingWith("Data")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = FunctionsRuleBuilder(projectGraph).should().haveNameEndingWith(listOf("Data", "Info"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = FunctionsRuleBuilder(projectGraph).should().haveNameEndingWith("Info", "Value")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }

    @Test
    fun `test haveNameStartingWith overloads`() {
        val func =
            FunctionDeclaration(
                "fetchUser",
                Visibility.PUBLIC,
                emptySet(),
                "Unit",
                emptyList(),
                emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "Service", ":app", "/src/Service.kt")

        val ruleSingle = FunctionsRuleBuilder(projectGraph).should().haveNameStartingWith("fetch")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = FunctionsRuleBuilder(projectGraph).should().haveNameStartingWith(listOf("get", "fetch"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = FunctionsRuleBuilder(projectGraph).should().haveNameStartingWith("put", "delete")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }

    @Test
    fun `test haveNameMatching overloads`() {
        val func =
            FunctionDeclaration(
                "updateProfile",
                Visibility.PUBLIC,
                emptySet(),
                "Unit",
                emptyList(),
                emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(func, "com.example", "Service", ":app", "/src/Service.kt")

        val ruleSingle = FunctionsRuleBuilder(projectGraph).should().haveNameMatching("update*")
        val violationsSingle = mutableListOf<String>()
        ruleSingle.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsSingle)
        assertTrue(violationsSingle.isEmpty())

        val ruleList = FunctionsRuleBuilder(projectGraph).should().haveNameMatching(listOf("update*", "save*"))
        val violationsList = mutableListOf<String>()
        ruleList.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsList)
        assertTrue(violationsList.isEmpty())

        val ruleVarargFail = FunctionsRuleBuilder(projectGraph).should().haveNameMatching("delete*", "remove*")
        val violationsVararg = mutableListOf<String>()
        ruleVarargFail.getShouldAssertion()!!(funcCtx, listOf(funcCtx), violationsVararg)
        assertEquals(1, violationsVararg.size)
    }
}
