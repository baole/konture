/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ClassesCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test ClassesShouldPackageAssertions package, module, and name`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vPkgSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg").getShouldAssertion()!!(classA, listOf(classA), vPkgSingle)
        assertEquals(1, vPkgSingle.size)

        val vPkgList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAPackage(listOf("wrong.pkg")).getShouldAssertion()!!(classA, listOf(classA), vPkgList)
        assertEquals(1, vPkgList.size)

        val vPkgVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg", "other").getShouldAssertion()!!(classA, listOf(classA), vPkgVararg)
        assertEquals(1, vPkgVararg.size)

        val vNotPkgSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAPackage("com.example").getShouldAssertion()!!(classA, listOf(classA), vNotPkgSingle)
        assertEquals(1, vNotPkgSingle.size)

        val vNotPkgList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAPackage(
            listOf("com.example"),
        ).getShouldAssertion()!!(classA, listOf(classA), vNotPkgList)
        assertEquals(1, vNotPkgList.size)

        val vNotPkgVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAPackage(
            "com.example",
            "other",
        ).getShouldAssertion()!!(classA, listOf(classA), vNotPkgVararg)
        assertEquals(1, vNotPkgVararg.size)

        val vModSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAModule(":other").getShouldAssertion()!!(classA, listOf(classA), vModSingle)
        assertEquals(1, vModSingle.size)

        val vModList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAModule(listOf(":other")).getShouldAssertion()!!(classA, listOf(classA), vModList)
        assertEquals(1, vModList.size)

        val vModVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAModule(":other", ":wrong").getShouldAssertion()!!(classA, listOf(classA), vModVararg)
        assertEquals(1, vModVararg.size)

        val vNotModSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAModule(":app").getShouldAssertion()!!(classA, listOf(classA), vNotModSingle)
        assertEquals(1, vNotModSingle.size)

        val vNotModList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAModule(listOf(":app")).getShouldAssertion()!!(classA, listOf(classA), vNotModList)
        assertEquals(1, vNotModList.size)

        val vNotModVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAModule(":app", ":other").getShouldAssertion()!!(classA, listOf(classA), vNotModVararg)
        assertEquals(1, vNotModVararg.size)

        val vNameSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveName("Other").getShouldAssertion()!!(classA, listOf(classA), vNameSingle)
        assertEquals(1, vNameSingle.size)

        val vNameList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveName(listOf("Other")).getShouldAssertion()!!(classA, listOf(classA), vNameList)
        assertEquals(1, vNameList.size)

        val vNameVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveName("Other", "Wrong").getShouldAssertion()!!(classA, listOf(classA), vNameVararg)
        assertEquals(1, vNameVararg.size)

        val vNotNameSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveName("ClassA").getShouldAssertion()!!(classA, listOf(classA), vNotNameSingle)
        assertEquals(1, vNotNameSingle.size)

        val vNotNameList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveName(listOf("ClassA")).getShouldAssertion()!!(classA, listOf(classA), vNotNameList)
        assertEquals(1, vNotNameList.size)

        val vNotNameVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveName("ClassA", "Other").getShouldAssertion()!!(classA, listOf(classA), vNotNameVararg)
        assertEquals(1, vNotNameVararg.size)

        val vStartSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameStartingWith("Other").getShouldAssertion()!!(classA, listOf(classA), vStartSingle)
        assertEquals(1, vStartSingle.size)

        val vStartList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameStartingWith(listOf("Other")).getShouldAssertion()!!(classA, listOf(classA), vStartList)
        assertEquals(1, vStartList.size)

        val vStartVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameStartingWith("Other", "X").getShouldAssertion()!!(classA, listOf(classA), vStartVararg)
        assertEquals(1, vStartVararg.size)

        val vNotStartSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameStartingWith("Class").getShouldAssertion()!!(classA, listOf(classA), vNotStartSingle)
        assertEquals(1, vNotStartSingle.size)

        val vNotStartList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameStartingWith(listOf("Class")).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vNotStartList,
        )
        assertEquals(1, vNotStartList.size)

        val vNotStartVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameStartingWith("Class", "Other").getShouldAssertion()!!(
            classA,
            listOf(classA),
            vNotStartVararg,
        )
        assertEquals(1, vNotStartVararg.size)

        val vEndSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameEndingWith("Other").getShouldAssertion()!!(classA, listOf(classA), vEndSingle)
        assertEquals(1, vEndSingle.size)

        val vEndList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameEndingWith(listOf("Other")).getShouldAssertion()!!(classA, listOf(classA), vEndList)
        assertEquals(1, vEndList.size)

        val vEndVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameEndingWith("Other", "X").getShouldAssertion()!!(classA, listOf(classA), vEndVararg)
        assertEquals(1, vEndVararg.size)

        val vNotEndSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameEndingWith("A").getShouldAssertion()!!(classA, listOf(classA), vNotEndSingle)
        assertEquals(1, vNotEndSingle.size)

        val vNotEndList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameEndingWith(listOf("A")).getShouldAssertion()!!(classA, listOf(classA), vNotEndList)
        assertEquals(1, vNotEndList.size)

        val vNotEndVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameEndingWith("A", "Other").getShouldAssertion()!!(classA, listOf(classA), vNotEndVararg)
        assertEquals(1, vNotEndVararg.size)

        val vMatchSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameMatching("Other*").getShouldAssertion()!!(classA, listOf(classA), vMatchSingle)
        assertEquals(1, vMatchSingle.size)

        val vMatchList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameMatching(listOf("Other*")).getShouldAssertion()!!(classA, listOf(classA), vMatchList)
        assertEquals(1, vMatchList.size)

        val vMatchVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameMatching("Other*", "X*").getShouldAssertion()!!(classA, listOf(classA), vMatchVararg)
        assertEquals(1, vMatchVararg.size)

        val vNotMatchSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameMatching("Class*").getShouldAssertion()!!(classA, listOf(classA), vNotMatchSingle)
        assertEquals(1, vNotMatchSingle.size)

        val vNotMatchList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameMatching(listOf("Class*")).getShouldAssertion()!!(classA, listOf(classA), vNotMatchList)
        assertEquals(1, vNotMatchList.size)

        val vNotMatchVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveNameMatching("Class*", "Other*").getShouldAssertion()!!(
            classA,
            listOf(classA),
            vNotMatchVararg,
        )
        assertEquals(1, vNotMatchVararg.size)
    }
}
