/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FilesThatCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test FilesThat package and name filters`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val pPkgSingle = FilesRuleBuilder(graph).that().resideInAPackage("com.example").getThatPredicate()!!
        assertTrue(pPkgSingle(fileCtx))

        val pPkgList = FilesRuleBuilder(graph).that().resideInAPackage(listOf("com.example")).getThatPredicate()!!
        assertTrue(pPkgList(fileCtx))

        val pPkgVararg =
            FilesRuleBuilder(
                graph,
            ).that().resideInAPackage("com.example", "com.other").getThatPredicate()!!
        assertTrue(pPkgVararg(fileCtx))

        val pPkgPred = FilesRuleBuilder(graph).that().resideInAPackage { it.startsWith("com") }.getThatPredicate()!!
        assertTrue(pPkgPred(fileCtx))

        val pNameSingle = FilesRuleBuilder(graph).that().haveName("ClassA.kt").getThatPredicate()!!
        assertTrue(pNameSingle(fileCtx))

        val pNameList = FilesRuleBuilder(graph).that().haveName(listOf("ClassA.kt")).getThatPredicate()!!
        assertTrue(pNameList(fileCtx))

        val pNameVararg = FilesRuleBuilder(graph).that().haveName("ClassA.kt", "ClassB.kt").getThatPredicate()!!
        assertTrue(pNameVararg(fileCtx))

        val pNamePred = FilesRuleBuilder(graph).that().haveName { it.endsWith(".kt") }.getThatPredicate()!!
        assertTrue(pNamePred(fileCtx))

        val pNameDescPred = FilesRuleBuilder(graph).that().haveName("desc", { it.endsWith(".kt") }).getThatPredicate()!!
        assertTrue(pNameDescPred(fileCtx))

        val pStartList = FilesRuleBuilder(graph).that().haveNameStartingWith(listOf("Class")).getThatPredicate()!!
        assertTrue(pStartList(fileCtx))

        val pStartVararg = FilesRuleBuilder(graph).that().haveNameStartingWith("Class", "File").getThatPredicate()!!
        assertTrue(pStartVararg(fileCtx))

        val pEndList = FilesRuleBuilder(graph).that().haveNameEndingWith(listOf(".kt")).getThatPredicate()!!
        assertTrue(pEndList(fileCtx))

        val pEndVararg = FilesRuleBuilder(graph).that().haveNameEndingWith(".kt", ".java").getThatPredicate()!!
        assertTrue(pEndVararg(fileCtx))

        val pMatchList = FilesRuleBuilder(graph).that().haveNameMatching(listOf("Class*.kt")).getThatPredicate()!!
        assertTrue(pMatchList(fileCtx))

        val pMatchVararg = FilesRuleBuilder(graph).that().haveNameMatching("Class*.kt", "File*.kt").getThatPredicate()!!
        assertTrue(pMatchVararg(fileCtx))
    }

    @Test
    fun `test FilesThat module residency and negations`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val pModSingle = FilesRuleBuilder(graph).that().resideInAModule("app").getThatPredicate()!!
        assertTrue(pModSingle(fileCtx))

        val pModSingleStd = FilesRuleBuilder(graph).that().inModule("app").getThatPredicate()!!
        assertTrue(pModSingleStd(fileCtx))

        val pModList = FilesRuleBuilder(graph).that().resideInModules(listOf(":app")).getThatPredicate()!!
        assertTrue(pModList(fileCtx))

        val pModListStd = FilesRuleBuilder(graph).that().inModules(listOf(":app")).getThatPredicate()!!
        assertTrue(pModListStd(fileCtx))

        val pModVararg = FilesRuleBuilder(graph).that().resideInModules(":app", ":core").getThatPredicate()!!
        assertTrue(pModVararg(fileCtx))

        val pModVarargStd = FilesRuleBuilder(graph).that().inModules(":app", ":core").getThatPredicate()!!
        assertTrue(pModVarargStd(fileCtx))

        val pNotModSingle = FilesRuleBuilder(graph).that().notResideInAModule("core").getThatPredicate()!!
        assertTrue(pNotModSingle(fileCtx))

        val pNotModSingleStd = FilesRuleBuilder(graph).that().notInModule("core").getThatPredicate()!!
        assertTrue(pNotModSingleStd(fileCtx))

        val pNotModList = FilesRuleBuilder(graph).that().notResideInModules(listOf(":core")).getThatPredicate()!!
        assertTrue(pNotModList(fileCtx))

        val pNotModListStd = FilesRuleBuilder(graph).that().notInModules(listOf(":core")).getThatPredicate()!!
        assertTrue(pNotModListStd(fileCtx))

        val pNotModVararg = FilesRuleBuilder(graph).that().notResideInModules(":core", ":feature").getThatPredicate()!!
        assertTrue(pNotModVararg(fileCtx))

        val pNotModVarargStd = FilesRuleBuilder(graph).that().notInModules(":core", ":feature").getThatPredicate()!!
        assertTrue(pNotModVarargStd(fileCtx))

        val pNotNameSingle = FilesRuleBuilder(graph).that().notHaveName("Other.kt").getThatPredicate()!!
        assertTrue(pNotNameSingle(fileCtx))

        val pNotNameSingleStd = FilesRuleBuilder(graph).that().notNamed("Other.kt").getThatPredicate()!!
        assertTrue(pNotNameSingleStd(fileCtx))

        val pNotNameList = FilesRuleBuilder(graph).that().notHaveName(listOf("Other.kt")).getThatPredicate()!!
        assertTrue(pNotNameList(fileCtx))

        val pNotNameListStd = FilesRuleBuilder(graph).that().notNamed(listOf("Other.kt")).getThatPredicate()!!
        assertTrue(pNotNameListStd(fileCtx))

        val pNotNameVararg = FilesRuleBuilder(graph).that().notHaveName("Other.kt", "Wrong.kt").getThatPredicate()!!
        assertTrue(pNotNameVararg(fileCtx))

        val pNotNameVarargStd = FilesRuleBuilder(graph).that().notNamed("Other.kt", "Wrong.kt").getThatPredicate()!!
        assertTrue(pNotNameVarargStd(fileCtx))

        val pNotNamePred = FilesRuleBuilder(graph).that().notHaveName { it.endsWith(".java") }.getThatPredicate()!!
        assertTrue(pNotNamePred(fileCtx))

        val pNotNamePredStd = FilesRuleBuilder(graph).that().notNamed { it.endsWith(".java") }.getThatPredicate()!!
        assertTrue(pNotNamePredStd(fileCtx))

        val pNotStartSingle = FilesRuleBuilder(graph).that().notHaveNameStartingWith("Wrong").getThatPredicate()!!
        assertTrue(pNotStartSingle(fileCtx))

        val pNotStartSingleStd = FilesRuleBuilder(graph).that().notNameStartsWith("Wrong").getThatPredicate()!!
        assertTrue(pNotStartSingleStd(fileCtx))

        val pNotStartList = FilesRuleBuilder(graph).that().notHaveNameStartingWith(listOf("Wrong")).getThatPredicate()!!
        assertTrue(pNotStartList(fileCtx))

        val pNotStartListStd = FilesRuleBuilder(graph).that().notNameStartsWith(listOf("Wrong")).getThatPredicate()!!
        assertTrue(pNotStartListStd(fileCtx))

        val pNotStartVararg =
            FilesRuleBuilder(
                graph,
            ).that().notHaveNameStartingWith("Wrong", "Bad").getThatPredicate()!!
        assertTrue(pNotStartVararg(fileCtx))

        val pNotStartVarargStd =
            FilesRuleBuilder(
                graph,
            ).that().notNameStartsWith("Wrong", "Bad").getThatPredicate()!!
        assertTrue(pNotStartVarargStd(fileCtx))

        val pNotEndSingle = FilesRuleBuilder(graph).that().notHaveNameEndingWith(".java").getThatPredicate()!!
        assertTrue(pNotEndSingle(fileCtx))

        val pNotEndSingleStd = FilesRuleBuilder(graph).that().notNameEndsWith(".java").getThatPredicate()!!
        assertTrue(pNotEndSingleStd(fileCtx))

        val pNotEndList = FilesRuleBuilder(graph).that().notHaveNameEndingWith(listOf(".java")).getThatPredicate()!!
        assertTrue(pNotEndList(fileCtx))

        val pNotEndListStd = FilesRuleBuilder(graph).that().notNameEndsWith(listOf(".java")).getThatPredicate()!!
        assertTrue(pNotEndListStd(fileCtx))

        val pNotEndVararg = FilesRuleBuilder(graph).that().notHaveNameEndingWith(".java", ".txt").getThatPredicate()!!
        assertTrue(pNotEndVararg(fileCtx))

        val pNotEndVarargStd = FilesRuleBuilder(graph).that().notNameEndsWith(".java", ".txt").getThatPredicate()!!
        assertTrue(pNotEndVarargStd(fileCtx))

        val pNotMatchSingle = FilesRuleBuilder(graph).that().notHaveNameMatching("Wrong*").getThatPredicate()!!
        assertTrue(pNotMatchSingle(fileCtx))

        val pNotMatchSingleStd = FilesRuleBuilder(graph).that().notNameMatches("Wrong*").getThatPredicate()!!
        assertTrue(pNotMatchSingleStd(fileCtx))

        val pNotMatchList = FilesRuleBuilder(graph).that().notHaveNameMatching(listOf("Wrong*")).getThatPredicate()!!
        assertTrue(pNotMatchList(fileCtx))

        val pNotMatchListStd = FilesRuleBuilder(graph).that().notNameMatches(listOf("Wrong*")).getThatPredicate()!!
        assertTrue(pNotMatchListStd(fileCtx))

        val pNotMatchVararg = FilesRuleBuilder(graph).that().notHaveNameMatching("Wrong*", "Bad*").getThatPredicate()!!
        assertTrue(pNotMatchVararg(fileCtx))

        val pNotMatchVarargStd = FilesRuleBuilder(graph).that().notNameMatches("Wrong*", "Bad*").getThatPredicate()!!
        assertTrue(pNotMatchVarargStd(fileCtx))
    }

    @Test
    fun `test FilesThat class, import and annotation filters`() {
        val func =
            FunctionDeclaration("f", Visibility.PUBLIC, emptySet(), "Unit", emptyList(), emptyList(), null, false)
        val prop = PropertyDeclaration("p", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null)
        val fileWithAll =
            FileDeclaration(
                name = "Test.kt",
                packageName = "com.example",
                classes = listOf(classA, classAnnotated),
                imports = listOf("com.example.ClassA", "com.example.ClassB"),
                topLevelFunctions = listOf(func),
                topLevelProperties = listOf(prop),
            )
        val fileCtx = FileDeclarationContext(fileWithAll, ":app")
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileWithAll))),
                ),
            )

        // containClass / containClassesWithAnnotation / haveImportOf
        val pClsSingle = FilesRuleBuilder(graph).that().containClass("com.example.ClassA").getThatPredicate()!!
        assertTrue(pClsSingle(fileCtx))

        val pClsList = FilesRuleBuilder(graph).that().containClass(listOf("com.example.ClassA")).getThatPredicate()!!
        assertTrue(pClsList(fileCtx))

        val pClsVararg =
            FilesRuleBuilder(
                graph,
            ).that().containClass("com.example.ClassA", "com.example.ClassB").getThatPredicate()!!
        assertTrue(pClsVararg(fileCtx))

        val pClsKClass = FilesRuleBuilder(graph).that().containClass(String::class).getThatPredicate()!!
        assertFalse(pClsKClass(fileCtx))

        val pClsKClassVararg =
            FilesRuleBuilder(
                graph,
            ).that().containClass(String::class, Int::class).getThatPredicate()!!
        assertFalse(pClsKClassVararg(fileCtx))

        val pAnnotStr =
            FilesRuleBuilder(
                graph,
            ).that().containClassesWithAnnotation("com.example.MyAnnotation").getThatPredicate()!!
        assertTrue(pAnnotStr(fileCtx))

        val pImpSingle = FilesRuleBuilder(graph).that().haveImportOf("com.example.ClassA").getThatPredicate()!!
        assertTrue(pImpSingle(fileCtx))

        val pImpList = FilesRuleBuilder(graph).that().haveImportOf(listOf("com.example.ClassA")).getThatPredicate()!!
        assertTrue(pImpList(fileCtx))

        val pImpVararg =
            FilesRuleBuilder(
                graph,
            ).that().haveImportOf("com.example.ClassA", "com.example.ClassB").getThatPredicate()!!
        assertTrue(pImpVararg(fileCtx))

        val pImpKClass = FilesRuleBuilder(graph).that().haveImportOf(String::class).getThatPredicate()!!
        assertFalse(pImpKClass(fileCtx))

        val pImpKClassVararg =
            FilesRuleBuilder(
                graph,
            ).that().haveImportOf(String::class, Int::class).getThatPredicate()!!
        assertFalse(pImpKClassVararg(fileCtx))

        // Top level & structure
        val pTopFunc = FilesRuleBuilder(graph).that().containTopLevelFunctions().getThatPredicate()!!
        assertTrue(pTopFunc(fileCtx))

        val pNotTopFunc = FilesRuleBuilder(graph).that().notContainTopLevelFunctions().getThatPredicate()!!
        assertFalse(pNotTopFunc(fileCtx))

        val pTopProp = FilesRuleBuilder(graph).that().containTopLevelProperties().getThatPredicate()!!
        assertTrue(pTopProp(fileCtx))

        val pNotTopProp = FilesRuleBuilder(graph).that().notContainTopLevelProperties().getThatPredicate()!!
        assertFalse(pNotTopProp(fileCtx))

        val pContainClasses = FilesRuleBuilder(graph).that().containClasses().getThatPredicate()!!
        assertTrue(pContainClasses(fileCtx))

        val pNotContainClasses = FilesRuleBuilder(graph).that().notContainClasses().getThatPredicate()!!
        assertFalse(pNotContainClasses(fileCtx))

        val pSatisfy = FilesRuleBuilder(graph).that().satisfy { it.declaration.name == "Test.kt" }.getThatPredicate()!!
        assertTrue(pSatisfy(fileCtx))

        val pHaveAnnot =
            FilesRuleBuilder(
                graph,
            ).that().haveAnnotationOf("com.example.MyAnnotation").getThatPredicate()!!
        assertTrue(pHaveAnnot(fileCtx))

        val pHaveAllAnnot =
            FilesRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf("com.example.MyAnnotation").getThatPredicate()!!
        assertTrue(pHaveAllAnnot(fileCtx))

        val pHaveAllAnnotVararg =
            FilesRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf("com.example.MyAnnotation").getThatPredicate()!!
        assertTrue(pHaveAllAnnotVararg(fileCtx))

        val pHaveAnyAnnot =
            FilesRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("com.example.MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pHaveAnyAnnot(fileCtx))

        val pHaveAnyAnnotVararg =
            FilesRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("com.example.MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pHaveAnyAnnotVararg(fileCtx))

        // Composite & Negations
        val pAnyOf =
            FilesRuleBuilder(graph).that().anyOf(
                { haveName("Test.kt") },
                { haveName("Other.kt") },
            ).getThatPredicate()!!
        assertTrue(pAnyOf(fileCtx))

        val pAllOf =
            FilesRuleBuilder(graph).that().allOf(
                { haveName("Test.kt") },
                { resideInAPackage("com.example") },
            ).getThatPredicate()!!
        assertTrue(pAllOf(fileCtx))

        val pNoneOf =
            FilesRuleBuilder(graph).that().noneOf(
                { haveName("Other.kt") },
            ).getThatPredicate()!!
        assertTrue(pNoneOf(fileCtx))

        val pNotPkgSingle = FilesRuleBuilder(graph).that().notResideInAPackage("com.other").getThatPredicate()!!
        assertTrue(pNotPkgSingle(fileCtx))

        val pNotPkgList = FilesRuleBuilder(graph).that().notResideInAPackage(listOf("com.other")).getThatPredicate()!!
        assertTrue(pNotPkgList(fileCtx))

        val pNotPkgVararg =
            FilesRuleBuilder(
                graph,
            ).that().notResideInAPackage("com.other", "org.wrong").getThatPredicate()!!
        assertTrue(pNotPkgVararg(fileCtx))

        val pNotCls = FilesRuleBuilder(graph).that().notContainClass("com.example.Missing").getThatPredicate()!!
        assertTrue(pNotCls(fileCtx))

        val pNotClsKClass = FilesRuleBuilder(graph).that().notContainClass(String::class).getThatPredicate()!!
        assertTrue(pNotClsKClass(fileCtx))

        val pNotClsAnnot =
            FilesRuleBuilder(
                graph,
            ).that().notContainClassesWithAnnotation("com.example.MissingAnnot").getThatPredicate()!!
        assertTrue(pNotClsAnnot(fileCtx))

        val pNotImp = FilesRuleBuilder(graph).that().notHaveImportOf("com.example.Missing").getThatPredicate()!!
        assertTrue(pNotImp(fileCtx))

        val pNotImpList =
            FilesRuleBuilder(
                graph,
            ).that().notHaveImportOf(listOf("com.example.Missing")).getThatPredicate()!!
        assertTrue(pNotImpList(fileCtx))

        val pNotImpVararg =
            FilesRuleBuilder(
                graph,
            ).that().notHaveImportOf("com.example.Missing", "com.example.Wrong").getThatPredicate()!!
        assertTrue(pNotImpVararg(fileCtx))

        val pNotImpKClass = FilesRuleBuilder(graph).that().notHaveImportOf(String::class).getThatPredicate()!!
        assertTrue(pNotImpKClass(fileCtx))
    }
}
