/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KontureFunctionScopeTest : RuleBuildersTestBase() {
    @Test
    fun `test function scope companion builders`() {
        val scopeFromProj = KontureFunctionScope.fromProject(projectGraph)
        assertNotNull(scopeFromProj)

        val scopeFromMod = KontureFunctionScope.fromModule(":moduleA", projectGraph)
        assertNotNull(scopeFromMod)

        assertThrows<IllegalArgumentException> {
            KontureFunctionScope.fromModule(":nonexistent", projectGraph)
        }

        val scopeFromPkg = KontureFunctionScope.fromPackage("com.example", projectGraph)
        assertNotNull(scopeFromPkg)
        assertTrue(scopeFromPkg.functions.all { it.packageName.startsWith("com.example") })
    }

    @Test
    fun `test function scope operators plus and minus`() {
        val func1 =
            FunctionDeclaration(
                name = "func1",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val ctx1 = FunctionDeclarationContext(func1, "com.example", "Class1", ":moduleA", "/src/Class1.kt")

        val func2 =
            FunctionDeclaration(
                name = "func2",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val ctx2 = FunctionDeclarationContext(func2, "com.example", "Class2", ":moduleA", "/src/Class2.kt")

        val scope1 = KontureFunctionScope(listOf(ctx1))
        val scope2 = KontureFunctionScope(listOf(ctx2))

        val combined = scope1 + scope2
        assertEquals(2, combined.functions.size)
        assertTrue(combined.functions.contains(ctx1))
        assertTrue(combined.functions.contains(ctx2))

        val subtracted = combined - scope2
        assertEquals(1, subtracted.functions.size)
        assertTrue(subtracted.functions.contains(ctx1))
        assertFalse(subtracted.functions.contains(ctx2))
    }

    @Test
    fun `test function list filtering extensions`() {
        val topLevelFunc =
            FunctionDeclaration(
                name = "topLevelAction",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val topCtx = FunctionDeclarationContext(topLevelFunc, "com.example.service", null, ":moduleA", "/src/Top.kt")

        val memberFunc =
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
        val memCtx = FunctionDeclarationContext(memberFunc, "com.example.ui", "MyClass", ":moduleA", "/src/MyClass.kt")

        val list = listOf(topCtx, memCtx)

        assertEquals(1, list.withNameEndingWith("Action").size)
        assertEquals(topCtx, list.withNameEndingWith("Action").first())

        assertEquals(1, list.withNameStartingWith("process").size)
        assertEquals(memCtx, list.withNameStartingWith("process").first())

        assertEquals(1, list.withNameMatching("*Data").size)
        assertEquals(memCtx, list.withNameMatching("*Data").first())

        assertEquals(1, list.withPackage("..service").size)
        assertEquals(topCtx, list.withPackage("..service").first())

        assertEquals(1, list.topLevelFunctions().size)
        assertEquals(topCtx, list.topLevelFunctions().first())

        assertEquals(1, list.memberFunctions().size)
        assertEquals(memCtx, list.memberFunctions().first())
    }

    @Test
    fun `test function scope assertTrue assertion success and failures`() {
        val func =
            FunctionDeclaration(
                name = "validFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val ctx = FunctionDeclarationContext(func, "com.example", null, ":moduleA", "/src/Valid.kt")

        val scope = KontureFunctionScope(listOf(ctx))

        // Success
        scope.assertTrue { it.declaration.name == "validFunc" }

        // Failure without additional message
        val err1 =
            assertThrows<AssertionError> {
                scope.assertTrue { it.declaration.name == "invalid" }
            }
        assertTrue(err1.message!!.contains("Functions failed assertion:"))
        assertTrue(err1.message!!.contains("validFunc"))

        // Failure with additional message
        val err2 =
            assertThrows<AssertionError> {
                scope.assertTrue("Custom failure details") { it.declaration.name == "invalid" }
            }
        assertTrue(err2.message!!.startsWith("Custom failure details\nFunctions failed assertion:"))
    }
}
