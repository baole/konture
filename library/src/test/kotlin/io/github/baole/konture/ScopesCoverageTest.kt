/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class ScopesCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test KontureSliceScope creation, operators and assertions`() {
        val moduleApp = Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA))
        val graph = ProjectGraph(mapOf(":" to listOf(moduleApp)))

        val scopeProj = KontureSliceScope.fromProject("com.example.(*)..", graph)
        assertNotNull(scopeProj)

        val scopeMod = KontureSliceScope.fromModule("com.example.(*)..", "app", graph)
        assertNotNull(scopeMod)

        val scopePkg = KontureSliceScope.fromPackage("com.example.(*)..", "com.example", graph)
        assertNotNull(scopePkg)

        val slice1 = Slice("payment", setOf("com.example.payment"), listOf(classA))
        val slice2 = Slice("billing", setOf("com.example.billing"), listOf(classB))

        val s1 = KontureSliceScope(listOf(slice1))
        val s2 = KontureSliceScope(listOf(slice2))

        val sCombined = s1 + s2
        assertEquals(2, sCombined.slices.size)

        val sDiff = sCombined - s2
        assertEquals(1, sDiff.slices.size)
        assertEquals("payment", sDiff.slices[0].key)

        val sByKey = sCombined.byKey("pay*")
        assertEquals(1, sByKey.slices.size)

        sCombined.assertAll { it.key.isNotEmpty() }
        sCombined.assertAny { it.key == "payment" }
        sCombined.assertNone { it.key == "nonexistent" }
    }

    @Test
    fun `test KontureScope, KontureFileScope, KontureFunctionScope, KonturePropertyScope`() {
        val classes = listOf(classA, classB)
        val scope = KontureScope(classes)

        val s1 = scope.withNameEndingWith("A")
        assertEquals(1, s1.classes.size)

        val files = listOf(fileA, fileB)
        val fileScope = KontureFileScope(files)
        val f1 = fileScope.withNameEndingWith("A.kt")
        assertEquals(1, f1.files.size)

        val funcDecl =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val funcCtx = FunctionDeclarationContext(funcDecl, "com.example", "ClassA", ":app", "/src/ClassA.kt")
        val funcScope = KontureFunctionScope(listOf(funcCtx))
        val fn1 = funcScope.memberFunctions()
        assertEquals(1, fn1.functions.size)

        val propDecl = PropertyDeclaration("myProp", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null)
        val propCtx = PropertyDeclarationContext(propDecl, "com.example", "ClassA", ":app", "/src/ClassA.kt")
        val propScope = KonturePropertyScope(listOf(propCtx))
        val p1 = propScope.memberProperties()
        assertEquals(1, p1.properties.size)
    }
}
