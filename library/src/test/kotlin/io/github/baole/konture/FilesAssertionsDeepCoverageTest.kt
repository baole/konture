/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FilesAssertionsDeepCoverageTest : KontureScopeTestFixture() {
    private fun FilesRuleBuilder.checkAssertions(
        file: FileDeclarationContext,
        all: List<FileDeclarationContext>,
        violations: MutableList<String>,
    ) {
        val assertion = this.getShouldAssertion() ?: return
        assertion(file, all, violations)
    }

    @Test
    fun `test FilesShould package assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().inPackage(listOf("com.example", "com.other"))
            .checkAssertions(fileCtx, listOf(fileCtx), vList)
        assertTrue(vList.isEmpty())

        val vVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().inPackage("com.example", "com.other")
            .checkAssertions(fileCtx, listOf(fileCtx), vVararg)
        assertTrue(vVararg.isEmpty())

        val vPred = mutableListOf<String>()
        FilesRuleBuilder(graph).should().inPackage { it.startsWith("com.example") }
            .checkAssertions(fileCtx, listOf(fileCtx), vPred)
        assertTrue(vPred.isEmpty())

        val vPredFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().inPackage { it.startsWith("com.other") }
            .checkAssertions(fileCtx, listOf(fileCtx), vPredFail)
        assertEquals(1, vPredFail.size)
    }

    @Test
    fun `test FilesShould module assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vInMod = mutableListOf<String>()
        FilesRuleBuilder(graph).should().inModule(":app")
            .checkAssertions(fileCtx, listOf(fileCtx), vInMod)
        assertTrue(vInMod.isEmpty())

        val vInMods = mutableListOf<String>()
        FilesRuleBuilder(graph).should().inModules(listOf(":app", ":lib"))
            .checkAssertions(fileCtx, listOf(fileCtx), vInMods)
        assertTrue(vInMods.isEmpty())

        val vInModsVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().inModules(":app", ":lib")
            .checkAssertions(fileCtx, listOf(fileCtx), vInModsVararg)
        assertTrue(vInModsVararg.isEmpty())

        val vNotInMod = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notInModule(":lib")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotInMod)
        assertTrue(vNotInMod.isEmpty())

        val vNotInModFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notInModule(":app")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotInModFail)
        assertEquals(1, vNotInModFail.size)

        val vNotInMods = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notInModules(listOf(":lib", ":other"))
            .checkAssertions(fileCtx, listOf(fileCtx), vNotInMods)
        assertTrue(vNotInMods.isEmpty())

        val vNotInModsFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notInModules(listOf(":app", ":lib"))
            .checkAssertions(fileCtx, listOf(fileCtx), vNotInModsFail)
        assertEquals(1, vNotInModsFail.size)

        val vNotInModsVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notInModules(":lib", ":other")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotInModsVararg)
        assertTrue(vNotInModsVararg.isEmpty())
    }

    @Test
    fun `test FilesShould name matching, start, end, and notNamed assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        // nameMatches
        val vMatch = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameMatches("*.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vMatch)
        assertTrue(vMatch.isEmpty())

        val vMatchList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameMatches(listOf("*.java", "*.kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), vMatchList)
        assertTrue(vMatchList.isEmpty())

        val vMatchVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameMatches("*.java", "*.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vMatchVararg)
        assertTrue(vMatchVararg.isEmpty())

        // nameStartsWith
        val vStart = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameStartsWith("Class")
            .checkAssertions(fileCtx, listOf(fileCtx), vStart)
        assertTrue(vStart.isEmpty())

        val vStartList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameStartsWith(listOf("Foo", "Class"))
            .checkAssertions(fileCtx, listOf(fileCtx), vStartList)
        assertTrue(vStartList.isEmpty())

        val vStartVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameStartsWith("Foo", "Class")
            .checkAssertions(fileCtx, listOf(fileCtx), vStartVararg)
        assertTrue(vStartVararg.isEmpty())

        // nameEndsWith
        val vEnd = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameEndsWith(".kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vEnd)
        assertTrue(vEnd.isEmpty())

        val vEndList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameEndsWith(listOf(".java", ".kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), vEndList)
        assertTrue(vEndList.isEmpty())

        val vEndVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().nameEndsWith(".java", ".kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vEndVararg)
        assertTrue(vEndVararg.isEmpty())

        // named
        val vNamed = mutableListOf<String>()
        FilesRuleBuilder(graph).should().named("ClassA.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vNamed)
        assertTrue(vNamed.isEmpty())

        val vNamedList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().named(listOf("ClassA.kt", "Other.kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), vNamedList)
        assertTrue(vNamedList.isEmpty())

        val vNamedVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().named("ClassA.kt", "Other.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vNamedVararg)
        assertTrue(vNamedVararg.isEmpty())

        val vNamedPred = mutableListOf<String>()
        FilesRuleBuilder(graph).should().named { it.endsWith(".kt") }
            .checkAssertions(fileCtx, listOf(fileCtx), vNamedPred)
        assertTrue(vNamedPred.isEmpty())

        // notNamed
        val vNotNamed = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNamed("Other.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotNamed)
        assertTrue(vNotNamed.isEmpty())

        val vNotNamedFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNamed("ClassA.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotNamedFail)
        assertEquals(1, vNotNamedFail.size)

        val vNotNamedList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNamed(listOf("ClassA.kt", "Other.kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), vNotNamedList)
        assertEquals(1, vNotNamedList.size)

        val vNotNamedVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNamed("ClassA.kt", "Other.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotNamedVararg)
        assertEquals(1, vNotNamedVararg.size)

        // notNameMatches
        val vNotMatch = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameMatches("*.java")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotMatch)
        assertTrue(vNotMatch.isEmpty())

        val vNotMatchFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameMatches("*.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotMatchFail)
        assertEquals(1, vNotMatchFail.size)

        val vNotMatchList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameMatches(listOf("*.kt", "*.java"))
            .checkAssertions(fileCtx, listOf(fileCtx), vNotMatchList)
        assertEquals(1, vNotMatchList.size)

        val vNotMatchVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameMatches("*.kt", "*.java")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotMatchVararg)
        assertEquals(1, vNotMatchVararg.size)

        // notNameStartsWith
        val vNotStart = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameStartsWith("Foo")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotStart)
        assertTrue(vNotStart.isEmpty())

        val vNotStartFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameStartsWith("Class")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotStartFail)
        assertEquals(1, vNotStartFail.size)

        val vNotStartList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameStartsWith(listOf("Class", "Foo"))
            .checkAssertions(fileCtx, listOf(fileCtx), vNotStartList)
        assertEquals(1, vNotStartList.size)

        val vNotStartVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameStartsWith("Class", "Foo")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotStartVararg)
        assertEquals(1, vNotStartVararg.size)

        // notNameEndsWith
        val vNotEnd = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameEndsWith(".java")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotEnd)
        assertTrue(vNotEnd.isEmpty())

        val vNotEndFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameEndsWith(".kt")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotEndFail)
        assertEquals(1, vNotEndFail.size)

        val vNotEndList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameEndsWith(listOf(".kt", ".java"))
            .checkAssertions(fileCtx, listOf(fileCtx), vNotEndList)
        assertEquals(1, vNotEndList.size)

        val vNotEndVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notNameEndsWith(".kt", ".java")
            .checkAssertions(fileCtx, listOf(fileCtx), vNotEndVararg)
        assertEquals(1, vNotEndVararg.size)
    }
}
