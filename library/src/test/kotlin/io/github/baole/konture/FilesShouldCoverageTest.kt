/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FilesShouldCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test FilesShould module residency assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertModSingle = FilesRuleBuilder(graph).should().resideInAModule(":app").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertModSingle(fileCtx, listOf(fileCtx), v1)
        assertTrue(v1.isEmpty())

        val assertModList = FilesRuleBuilder(graph).should().resideInAModule(listOf(":app")).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertModList(fileCtx, listOf(fileCtx), v2)
        assertTrue(v2.isEmpty())

        val assertModVararg =
            FilesRuleBuilder(
                graph,
            ).should().resideInAModule(":app", ":core").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertModVararg(fileCtx, listOf(fileCtx), v3)
        assertTrue(v3.isEmpty())

        val assertNotModSingle =
            FilesRuleBuilder(
                graph,
            ).should().notResideInAModule(":forbidden").getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotModSingle(fileCtx, listOf(fileCtx), v4)
        assertTrue(v4.isEmpty())

        val assertNotModList =
            FilesRuleBuilder(
                graph,
            ).should().notResideInAModule(listOf(":forbidden")).getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNotModList(fileCtx, listOf(fileCtx), v5)
        assertTrue(v5.isEmpty())

        val assertNotModVararg =
            FilesRuleBuilder(
                graph,
            ).should().notResideInAModule(":forbidden", ":other").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotModVararg(fileCtx, listOf(fileCtx), v6)
        assertTrue(v6.isEmpty())
    }

    @Test
    fun `test FilesShould containClass and import assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertClsSingle = FilesRuleBuilder(graph).should().containClass("ClassA").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertClsSingle(fileCtx, listOf(fileCtx), v1)
        assertTrue(v1.isEmpty())

        val assertClsList =
            FilesRuleBuilder(
                graph,
            ).should().containClass(listOf("ClassA")).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertClsList(fileCtx, listOf(fileCtx), v2)
        assertTrue(v2.isEmpty())

        val assertClsVararg =
            FilesRuleBuilder(
                graph,
            ).should().containClass("ClassA", "Other").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertClsVararg(fileCtx, listOf(fileCtx), v3)
        assertTrue(v3.isEmpty())

        val assertNotClsSingle =
            FilesRuleBuilder(
                graph,
            ).should().notContainClass("Missing").getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotClsSingle(fileCtx, listOf(fileCtx), v4)
        assertTrue(v4.isEmpty())

        val assertNotClsList =
            FilesRuleBuilder(
                graph,
            ).should().notContainClass(listOf("Missing")).getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNotClsList(fileCtx, listOf(fileCtx), v5)
        assertTrue(v5.isEmpty())

        val assertNotClsVararg =
            FilesRuleBuilder(
                graph,
            ).should().notContainClass("Missing", "Bad").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotClsVararg(fileCtx, listOf(fileCtx), v6)
        assertTrue(v6.isEmpty())

        val fileWithImports = FileDeclaration("Imp.kt", "com.example", imports = listOf("com.example.ClassB"))
        val fileCtxImp = FileDeclarationContext(fileWithImports, ":app")

        val assertImpSingle =
            FilesRuleBuilder(
                graph,
            ).should().haveImportOf("com.example.ClassB").getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertImpSingle(fileCtxImp, listOf(fileCtxImp), v7)
        assertTrue(v7.isEmpty())

        val assertImpList =
            FilesRuleBuilder(
                graph,
            ).should().haveImportOf(listOf("com.example.ClassB")).getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertImpList(fileCtxImp, listOf(fileCtxImp), v8)
        assertTrue(v8.isEmpty())

        val assertImpVararg =
            FilesRuleBuilder(
                graph,
            ).should().haveImportOf("com.example.ClassB", "com.other.Other").getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertImpVararg(fileCtxImp, listOf(fileCtxImp), v9)
        assertTrue(v9.isEmpty())

        val assertNotImpSingle =
            FilesRuleBuilder(
                graph,
            ).should().notHaveImportOf("com.other.Missing").getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertNotImpSingle(fileCtxImp, listOf(fileCtxImp), v10)
        assertTrue(v10.isEmpty())

        val assertNotImpList =
            FilesRuleBuilder(
                graph,
            ).should().notHaveImportOf(listOf("com.other.Missing")).getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertNotImpList(fileCtxImp, listOf(fileCtxImp), v11)
        assertTrue(v11.isEmpty())

        val assertNotImpVararg =
            FilesRuleBuilder(
                graph,
            ).should().notHaveImportOf("com.other.Missing", "com.other.Bad").getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertNotImpVararg(fileCtxImp, listOf(fileCtxImp), v12)
        assertTrue(v12.isEmpty())
    }

    @Test
    fun `test FilesShould top level and structural elements`() {
        val topFunc = FunctionDeclaration("topFun", Visibility.PUBLIC, emptySet(), "Unit", emptyList(), emptyList(), null)
        val topProp = PropertyDeclaration("topProp", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null)
        val fileWithTop = FileDeclaration("Top.kt", "com.example", topLevelFunctions = listOf(topFunc), topLevelProperties = listOf(topProp))
        val fileCtxTop = FileDeclarationContext(fileWithTop, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileWithTop)))),
            )

        val assertTopFuncs = FilesRuleBuilder(graph).should().haveTopLevelFunctions().getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertTopFuncs(fileCtxTop, listOf(fileCtxTop), v1)
        assertTrue(v1.isEmpty())

        val assertTopProps = FilesRuleBuilder(graph).should().haveTopLevelProperties().getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertTopProps(fileCtxTop, listOf(fileCtxTop), v2)
        assertTrue(v2.isEmpty())

        val fileNoTop = FileDeclaration("NoTop.kt", "com.example", classes = listOf(classA))
        val fileCtxNoTop = FileDeclarationContext(fileNoTop, ":app")

        val assertNotTopFuncs = FilesRuleBuilder(graph).should().notHaveTopLevelFunctions().getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertNotTopFuncs(fileCtxNoTop, listOf(fileCtxNoTop), v3)
        assertTrue(v3.isEmpty())

        val assertNotTopProps = FilesRuleBuilder(graph).should().notHaveTopLevelProperties().getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotTopProps(fileCtxNoTop, listOf(fileCtxNoTop), v4)
        assertTrue(v4.isEmpty())

        val assertClasses = FilesRuleBuilder(graph).should().haveClasses().getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertClasses(fileCtxNoTop, listOf(fileCtxNoTop), v5)
        assertTrue(v5.isEmpty())

        val assertNotClasses = FilesRuleBuilder(graph).should().notHaveClasses().getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotClasses(fileCtxTop, listOf(fileCtxTop), v6)
        assertTrue(v6.isEmpty())
    }

    @Test
    fun `test FilesShould name assertions and package dependencies`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertNameSingle = FilesRuleBuilder(graph).should().haveName("ClassA.kt").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertNameSingle(fileCtx, listOf(fileCtx), v1)
        assertTrue(v1.isEmpty())

        val assertNameList = FilesRuleBuilder(graph).should().haveName(listOf("ClassA.kt")).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNameList(fileCtx, listOf(fileCtx), v2)
        assertTrue(v2.isEmpty())

        val assertNameVararg =
            FilesRuleBuilder(
                graph,
            ).should().haveName("ClassA.kt", "FileB.kt").getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertNameVararg(fileCtx, listOf(fileCtx), v3)
        assertTrue(v3.isEmpty())

        val assertNamePred =
            FilesRuleBuilder(
                graph,
            ).should().haveName { it.startsWith("ClassA") }.getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNamePred(fileCtx, listOf(fileCtx), v4)
        assertTrue(v4.isEmpty())

        val assertNamePredDesc =
            FilesRuleBuilder(
                graph,
            ).should().haveName("custom desc") { it.startsWith("ClassA") }.getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNamePredDesc(fileCtx, listOf(fileCtx), v5)
        assertTrue(v5.isEmpty())

        val assertNotNameSingle = FilesRuleBuilder(graph).should().notHaveName("Other.kt").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotNameSingle(fileCtx, listOf(fileCtx), v6)
        assertTrue(v6.isEmpty())

        val assertNotNameList =
            FilesRuleBuilder(
                graph,
            ).should().notHaveName(listOf("Other.kt")).getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertNotNameList(fileCtx, listOf(fileCtx), v7)
        assertTrue(v7.isEmpty())

        val assertNotNameVararg =
            FilesRuleBuilder(
                graph,
            ).should().notHaveName("Other.kt", "Wrong.kt").getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertNotNameVararg(fileCtx, listOf(fileCtx), v8)
        assertTrue(v8.isEmpty())

        val assertNotNamePred =
            FilesRuleBuilder(
                graph,
            ).should().notHaveName { it.startsWith("Other") }.getShouldAssertion()!!
        val v9 = mutableListOf<String>()
        assertNotNamePred(fileCtx, listOf(fileCtx), v9)
        assertTrue(v9.isEmpty())

        val assertEndSingle = FilesRuleBuilder(graph).should().haveNameEndingWith(".kt").getShouldAssertion()!!
        val v10 = mutableListOf<String>()
        assertEndSingle(fileCtx, listOf(fileCtx), v10)
        assertTrue(v10.isEmpty())

        val assertEndList = FilesRuleBuilder(graph).should().haveNameEndingWith(listOf(".kt")).getShouldAssertion()!!
        val v11 = mutableListOf<String>()
        assertEndList(fileCtx, listOf(fileCtx), v11)
        assertTrue(v11.isEmpty())

        val assertEndVararg =
            FilesRuleBuilder(
                graph,
            ).should().haveNameEndingWith(".kt", ".kts").getShouldAssertion()!!
        val v12 = mutableListOf<String>()
        assertEndVararg(fileCtx, listOf(fileCtx), v12)
        assertTrue(v12.isEmpty())

        val assertNotEndSingle =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith(".java").getShouldAssertion()!!
        val v13 = mutableListOf<String>()
        assertNotEndSingle(fileCtx, listOf(fileCtx), v13)
        assertTrue(v13.isEmpty())

        val assertNotEndList =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith(listOf(".java")).getShouldAssertion()!!
        val v14 = mutableListOf<String>()
        assertNotEndList(fileCtx, listOf(fileCtx), v14)
        assertTrue(v14.isEmpty())

        val assertNotEndVararg =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameEndingWith(".java", ".cpp").getShouldAssertion()!!
        val v15 = mutableListOf<String>()
        assertNotEndVararg(fileCtx, listOf(fileCtx), v15)
        assertTrue(v15.isEmpty())

        val assertStartSingle = FilesRuleBuilder(graph).should().haveNameStartingWith("Class").getShouldAssertion()!!
        val v16 = mutableListOf<String>()
        assertStartSingle(fileCtx, listOf(fileCtx), v16)
        assertTrue(v16.isEmpty())

        val assertStartList =
            FilesRuleBuilder(
                graph,
            ).should().haveNameStartingWith(listOf("Class")).getShouldAssertion()!!
        val v17 = mutableListOf<String>()
        assertStartList(fileCtx, listOf(fileCtx), v17)
        assertTrue(v17.isEmpty())

        val assertStartVararg =
            FilesRuleBuilder(
                graph,
            ).should().haveNameStartingWith("Class", "File").getShouldAssertion()!!
        val v18 = mutableListOf<String>()
        assertStartVararg(fileCtx, listOf(fileCtx), v18)
        assertTrue(v18.isEmpty())

        val assertNotStartSingle =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("Other").getShouldAssertion()!!
        val v19 = mutableListOf<String>()
        assertNotStartSingle(fileCtx, listOf(fileCtx), v19)
        assertTrue(v19.isEmpty())

        val assertNotStartList =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith(listOf("Other")).getShouldAssertion()!!
        val v20 = mutableListOf<String>()
        assertNotStartList(fileCtx, listOf(fileCtx), v20)
        assertTrue(v20.isEmpty())

        val assertNotStartVararg =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameStartingWith("Other", "Wrong").getShouldAssertion()!!
        val v21 = mutableListOf<String>()
        assertNotStartVararg(fileCtx, listOf(fileCtx), v21)
        assertTrue(v21.isEmpty())

        val assertMatchSingle = FilesRuleBuilder(graph).should().haveNameMatching("Class*").getShouldAssertion()!!
        val v22 = mutableListOf<String>()
        assertMatchSingle(fileCtx, listOf(fileCtx), v22)
        assertTrue(v22.isEmpty())

        val assertMatchList =
            FilesRuleBuilder(
                graph,
            ).should().haveNameMatching(listOf("Class*")).getShouldAssertion()!!
        val v23 = mutableListOf<String>()
        assertMatchList(fileCtx, listOf(fileCtx), v23)
        assertTrue(v23.isEmpty())

        val assertMatchVararg =
            FilesRuleBuilder(
                graph,
            ).should().haveNameMatching("Class*", "File*").getShouldAssertion()!!
        val v24 = mutableListOf<String>()
        assertMatchVararg(fileCtx, listOf(fileCtx), v24)
        assertTrue(v24.isEmpty())

        val assertNotMatchSingle =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameMatching("Other*").getShouldAssertion()!!
        val v25 = mutableListOf<String>()
        assertNotMatchSingle(fileCtx, listOf(fileCtx), v25)
        assertTrue(v25.isEmpty())

        val assertNotMatchList =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameMatching(listOf("Other*")).getShouldAssertion()!!
        val v26 = mutableListOf<String>()
        assertNotMatchList(fileCtx, listOf(fileCtx), v26)
        assertTrue(v26.isEmpty())

        val assertNotMatchVararg =
            FilesRuleBuilder(
                graph,
            ).should().notHaveNameMatching("Other*", "Wrong*").getShouldAssertion()!!
        val v27 = mutableListOf<String>()
        assertNotMatchVararg(fileCtx, listOf(fileCtx), v27)
        assertTrue(v27.isEmpty())

        val fileWithImports = FileDeclaration("Imp.kt", "com.example", imports = listOf("com.example.ClassB"))
        val fileCtxImp = FileDeclarationContext(fileWithImports, ":app")

        val assertOnlyPkgSingle =
            FilesRuleBuilder(
                graph,
            ).should().onlyDependOnPackages("com.example.*").getShouldAssertion()!!
        val v28 = mutableListOf<String>()
        assertOnlyPkgSingle(fileCtxImp, listOf(fileCtxImp), v28)
        assertTrue(v28.isEmpty())

        val assertOnlyPkgList =
            FilesRuleBuilder(
                graph,
            ).should().onlyDependOnPackages(listOf("com.example.*")).getShouldAssertion()!!
        val v29 = mutableListOf<String>()
        assertOnlyPkgList(fileCtxImp, listOf(fileCtxImp), v29)
        assertTrue(v29.isEmpty())

        val assertOnlyPkgVararg =
            FilesRuleBuilder(
                graph,
            ).should().onlyDependOnPackages("com.example.*", "com.other.*").getShouldAssertion()!!
        val v30 = mutableListOf<String>()
        assertOnlyPkgVararg(fileCtxImp, listOf(fileCtxImp), v30)
        assertTrue(v30.isEmpty())

        val assertNotPkgSingle =
            FilesRuleBuilder(
                graph,
            ).should().notDependOnPackages("com.forbidden.*").getShouldAssertion()!!
        val v31 = mutableListOf<String>()
        assertNotPkgSingle(fileCtxImp, listOf(fileCtxImp), v31)
        assertTrue(v31.isEmpty())

        val assertNotPkgList =
            FilesRuleBuilder(
                graph,
            ).should().notDependOnPackages(listOf("com.forbidden.*")).getShouldAssertion()!!
        val v32 = mutableListOf<String>()
        assertNotPkgList(fileCtxImp, listOf(fileCtxImp), v32)
        assertTrue(v32.isEmpty())

        val assertNotPkgVararg =
            FilesRuleBuilder(
                graph,
            ).should().notDependOnPackages("com.forbidden.*", "org.wrong.*").getShouldAssertion()!!
        val v33 = mutableListOf<String>()
        assertNotPkgVararg(fileCtxImp, listOf(fileCtxImp), v33)
        assertTrue(v33.isEmpty())

        val vOnlyModSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().onlyDependOnModules(":app")
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyModSingle)
        assertTrue(vOnlyModSingle.isEmpty())

        val vOnlyModList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().onlyDependOnModules(listOf(":app"))
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyModList)
        assertTrue(vOnlyModList.isEmpty())

        val vOnlyModVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().onlyDependOnModules(":app", ":core")
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyModVararg)
        assertTrue(vOnlyModVararg.isEmpty())

        val vNotModSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notDependOnModules(":core")
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotModSingle)
        assertTrue(vNotModSingle.isEmpty())

        val vNotModList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notDependOnModules(listOf(":core"))
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotModList)
        assertTrue(vNotModList.isEmpty())

        val vNotModVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notDependOnModules(":core")
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotModVararg)
        assertTrue(vNotModVararg.isEmpty())
    }
}
