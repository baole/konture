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

class KonturePropertyScopeTest : RuleBuildersTestBase() {
    @Test
    fun `test property scope companion builders`() {
        val scopeFromProj = KonturePropertyScope.fromProject(projectGraph)
        assertNotNull(scopeFromProj)

        val scopeFromMod = KonturePropertyScope.fromModule(":moduleA", projectGraph)
        assertNotNull(scopeFromMod)

        assertThrows<IllegalArgumentException> {
            KonturePropertyScope.fromModule(":nonexistent", projectGraph)
        }

        val scopeFromPkg = KonturePropertyScope.fromPackage("com.example", projectGraph)
        assertNotNull(scopeFromPkg)
        assertTrue(scopeFromPkg.properties.all { it.packageName.startsWith("com.example") })
    }

    @Test
    fun `test property scope operators plus and minus`() {
        val prop1 =
            PropertyDeclaration(
                name = "prop1",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )
        val ctx1 = PropertyDeclarationContext(prop1, "com.example", "Class1", ":moduleA", "/src/Class1.kt")

        val prop2 =
            PropertyDeclaration(
                name = "prop2",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "Int",
                isVal = false,
                annotations = emptyList(),
                kdocText = null,
            )
        val ctx2 = PropertyDeclarationContext(prop2, "com.example", "Class2", ":moduleA", "/src/Class2.kt")

        val scope1 = KonturePropertyScope(listOf(ctx1))
        val scope2 = KonturePropertyScope(listOf(ctx2))

        val combined = scope1 + scope2
        assertEquals(2, combined.properties.size)
        assertTrue(combined.properties.contains(ctx1))
        assertTrue(combined.properties.contains(ctx2))

        val subtracted = combined - scope2
        assertEquals(1, subtracted.properties.size)
        assertTrue(subtracted.properties.contains(ctx1))
        assertFalse(subtracted.properties.contains(ctx2))
    }

    @Test
    fun `test property list filtering extensions`() {
        val valProp =
            PropertyDeclaration(
                name = "topLevelVal",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )
        val topCtx = PropertyDeclarationContext(valProp, "com.example.service", null, ":moduleA", "/src/Top.kt")

        val varProp =
            PropertyDeclaration(
                name = "memberVar",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "Int",
                isVal = false,
                annotations = emptyList(),
                kdocText = null,
            )
        val memCtx = PropertyDeclarationContext(varProp, "com.example.ui", "MyClass", ":moduleA", "/src/MyClass.kt")

        val list = listOf(topCtx, memCtx)

        assertEquals(1, list.withNameEndingWith("Val").size)
        assertEquals(topCtx, list.withNameEndingWith("Val").first())

        assertEquals(1, list.withNameStartingWith("member").size)
        assertEquals(memCtx, list.withNameStartingWith("member").first())

        assertEquals(1, list.withNameMatching("*Var").size)
        assertEquals(memCtx, list.withNameMatching("*Var").first())

        assertEquals(1, list.withPackage("..service").size)
        assertEquals(topCtx, list.withPackage("..service").first())

        assertEquals(1, list.valProperties().size)
        assertEquals(topCtx, list.valProperties().first())

        assertEquals(1, list.varProperties().size)
        assertEquals(memCtx, list.varProperties().first())

        assertEquals(1, list.topLevelProperties().size)
        assertEquals(topCtx, list.topLevelProperties().first())

        assertEquals(1, list.memberProperties().size)
        assertEquals(memCtx, list.memberProperties().first())
    }

    @Test
    fun `test property scope assertTrue assertion success and failures`() {
        val prop =
            PropertyDeclaration(
                name = "validProp",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )
        val ctx = PropertyDeclarationContext(prop, "com.example", null, ":moduleA", "/src/Valid.kt")

        val scope = KonturePropertyScope(listOf(ctx))

        // Success
        scope.assertTrue { it.declaration.name == "validProp" }

        // Failure without additional message
        val err1 =
            assertThrows<AssertionError> {
                scope.assertTrue { it.declaration.name == "invalid" }
            }
        assertTrue(err1.message!!.contains("Properties failed assertion:"))
        assertTrue(err1.message!!.contains("validProp"))

        // Failure with additional message
        val err2 =
            assertThrows<AssertionError> {
                scope.assertTrue("Custom failure details") { it.declaration.name == "invalid" }
            }
        assertTrue(err2.message!!.startsWith("Custom failure details\nProperties failed assertion:"))
    }
}
