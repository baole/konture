/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("DEPRECATION")
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
    fun `test FilesShould package assertions and deprecated aliases`() {
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

        // Deprecated aliases for resideInPackage / resideInAPackage
        val vDep1 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInPackage("com.example")
            .checkAssertions(fileCtx, listOf(fileCtx), vDep1)
        assertTrue(vDep1.isEmpty())

        val vDep2 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInPackage(listOf("com.example"))
            .checkAssertions(fileCtx, listOf(fileCtx), vDep2)
        assertTrue(vDep2.isEmpty())

        val vDep3 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInPackage("com.example", "com.other")
            .checkAssertions(fileCtx, listOf(fileCtx), vDep3)
        assertTrue(vDep3.isEmpty())

        val vDep4 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInPackage { it == "com.example" }
            .checkAssertions(fileCtx, listOf(fileCtx), vDep4)
        assertTrue(vDep4.isEmpty())

        val vDep5 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAPackage("com.example")
            .checkAssertions(fileCtx, listOf(fileCtx), vDep5)
        assertTrue(vDep5.isEmpty())

        val vDep6 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAPackage(listOf("com.example"))
            .checkAssertions(fileCtx, listOf(fileCtx), vDep6)
        assertTrue(vDep6.isEmpty())

        val vDep7 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAPackage("com.example", "com.other")
            .checkAssertions(fileCtx, listOf(fileCtx), vDep7)
        assertTrue(vDep7.isEmpty())

        val vDep8 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAPackage { it == "com.example" }
            .checkAssertions(fileCtx, listOf(fileCtx), vDep8)
        assertTrue(vDep8.isEmpty())
    }

    @Test
    fun `test FilesShould module assertions and deprecated aliases`() {
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

        // Deprecated aliases
        val vD1 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAModule(":app")
            .checkAssertions(fileCtx, listOf(fileCtx), vD1)
        assertTrue(vD1.isEmpty())

        val vD2 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAModule(listOf(":app"))
            .checkAssertions(fileCtx, listOf(fileCtx), vD2)
        assertTrue(vD2.isEmpty())

        val vD3 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAModule(":app", ":lib")
            .checkAssertions(fileCtx, listOf(fileCtx), vD3)
        assertTrue(vD3.isEmpty())

        val vD4 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInModule(":lib")
            .checkAssertions(fileCtx, listOf(fileCtx), vD4)
        assertTrue(vD4.isEmpty())

        val vD5 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInModules(listOf(":lib"))
            .checkAssertions(fileCtx, listOf(fileCtx), vD5)
        assertTrue(vD5.isEmpty())

        val vD6 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInModules(":lib", ":other")
            .checkAssertions(fileCtx, listOf(fileCtx), vD6)
        assertTrue(vD6.isEmpty())

        val vD7 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInAModule(":lib")
            .checkAssertions(fileCtx, listOf(fileCtx), vD7)
        assertTrue(vD7.isEmpty())

        val vD8 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInAModule(listOf(":lib"))
            .checkAssertions(fileCtx, listOf(fileCtx), vD8)
        assertTrue(vD8.isEmpty())

        val vD9 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInAModule(":lib", ":other")
            .checkAssertions(fileCtx, listOf(fileCtx), vD9)
        assertTrue(vD9.isEmpty())
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

    @Test
    fun `test FilesShould deprecated aliases for haveName, notHaveName, haveNameMatching, etc`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "/app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val v1 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveName("ClassA.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), v1)
        assertTrue(v1.isEmpty())

        val v2 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveName(listOf("ClassA.kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), v2)
        assertTrue(v2.isEmpty())

        val v3 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveName("ClassA.kt", "Other.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), v3)
        assertTrue(v3.isEmpty())

        val v4 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveName { it.startsWith("Class") }
            .checkAssertions(fileCtx, listOf(fileCtx), v4)
        assertTrue(v4.isEmpty())

        val v5 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameIn(listOf("ClassA.kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), v5)
        assertTrue(v5.isEmpty())

        val v6 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameIn("ClassA.kt", "Other.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), v6)
        assertTrue(v6.isEmpty())

        val v7 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveName("Other.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), v7)
        assertTrue(v7.isEmpty())

        val v8 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveName(listOf("Other.kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), v8)
        assertTrue(v8.isEmpty())

        val v9 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveName("Other1.kt", "Other2.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), v9)
        assertTrue(v9.isEmpty())

        val v10 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameIn(listOf("Other.kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), v10)
        assertTrue(v10.isEmpty())

        val v11 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameIn("Other1.kt", "Other2.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), v11)
        assertTrue(v11.isEmpty())

        val v12 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameMatching("*.kt")
            .checkAssertions(fileCtx, listOf(fileCtx), v12)
        assertTrue(v12.isEmpty())

        val v13 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameMatching(listOf("*.kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), v13)
        assertTrue(v13.isEmpty())

        val v14 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameMatching("*.kt", "*.java")
            .checkAssertions(fileCtx, listOf(fileCtx), v14)
        assertTrue(v14.isEmpty())

        val v15 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameStartingWith("Class")
            .checkAssertions(fileCtx, listOf(fileCtx), v15)
        assertTrue(v15.isEmpty())

        val v16 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameStartingWith(listOf("Class"))
            .checkAssertions(fileCtx, listOf(fileCtx), v16)
        assertTrue(v16.isEmpty())

        val v17 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameStartingWith("Class", "Foo")
            .checkAssertions(fileCtx, listOf(fileCtx), v17)
        assertTrue(v17.isEmpty())

        val v18 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameEndingWith(".kt")
            .checkAssertions(fileCtx, listOf(fileCtx), v18)
        assertTrue(v18.isEmpty())

        val v19 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameEndingWith(listOf(".kt"))
            .checkAssertions(fileCtx, listOf(fileCtx), v19)
        assertTrue(v19.isEmpty())

        val v20 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNameEndingWith(".kt", ".java")
            .checkAssertions(fileCtx, listOf(fileCtx), v20)
        assertTrue(v20.isEmpty())

        val v21 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameMatching("*.java")
            .checkAssertions(fileCtx, listOf(fileCtx), v21)
        assertTrue(v21.isEmpty())

        val v22 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameMatching(listOf("*.java"))
            .checkAssertions(fileCtx, listOf(fileCtx), v22)
        assertTrue(v22.isEmpty())

        val v23 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameMatching("*.java", "*.cpp")
            .checkAssertions(fileCtx, listOf(fileCtx), v23)
        assertTrue(v23.isEmpty())

        val v24 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameStartingWith("Foo")
            .checkAssertions(fileCtx, listOf(fileCtx), v24)
        assertTrue(v24.isEmpty())

        val v25 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameStartingWith(listOf("Foo"))
            .checkAssertions(fileCtx, listOf(fileCtx), v25)
        assertTrue(v25.isEmpty())

        val v26 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameStartingWith("Foo", "Bar")
            .checkAssertions(fileCtx, listOf(fileCtx), v26)
        assertTrue(v26.isEmpty())

        val v27 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameEndingWith(".java")
            .checkAssertions(fileCtx, listOf(fileCtx), v27)
        assertTrue(v27.isEmpty())

        val v28 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameEndingWith(listOf(".java"))
            .checkAssertions(fileCtx, listOf(fileCtx), v28)
        assertTrue(v28.isEmpty())

        val v29 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameEndingWith(".java", ".cpp")
            .checkAssertions(fileCtx, listOf(fileCtx), v29)
        assertTrue(v29.isEmpty())
    }
}
