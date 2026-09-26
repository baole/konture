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

        val pPkgSingle = FilesRuleBuilder(graph).that().inPackage("com.example").getThatPredicate()!!
        assertTrue(pPkgSingle(fileCtx))

        val pPkgList = FilesRuleBuilder(graph).that().inPackage(listOf("com.example")).getThatPredicate()!!
        assertTrue(pPkgList(fileCtx))

        val pPkgVararg =
            FilesRuleBuilder(
                graph,
            ).that().inPackage("com.example", "com.other").getThatPredicate()!!
        assertTrue(pPkgVararg(fileCtx))

        val pPkgPred = FilesRuleBuilder(graph).that().inPackage { it.startsWith("com") }.getThatPredicate()!!
        assertTrue(pPkgPred(fileCtx))

        val pNameSingle = FilesRuleBuilder(graph).that().named("ClassA.kt").getThatPredicate()!!
        assertTrue(pNameSingle(fileCtx))

        val pNameList = FilesRuleBuilder(graph).that().named(listOf("ClassA.kt")).getThatPredicate()!!
        assertTrue(pNameList(fileCtx))

        val pNameVararg = FilesRuleBuilder(graph).that().named("ClassA.kt", "ClassB.kt").getThatPredicate()!!
        assertTrue(pNameVararg(fileCtx))

        val pNamePred = FilesRuleBuilder(graph).that().named { it.endsWith(".kt") }.getThatPredicate()!!
        assertTrue(pNamePred(fileCtx))

        val pNameDescPred = FilesRuleBuilder(graph).that().named("desc", { it.endsWith(".kt") }).getThatPredicate()!!
        assertTrue(pNameDescPred(fileCtx))

        val pStartList = FilesRuleBuilder(graph).that().nameStartsWith(listOf("Class")).getThatPredicate()!!
        assertTrue(pStartList(fileCtx))

        val pStartVararg = FilesRuleBuilder(graph).that().nameStartsWith("Class", "File").getThatPredicate()!!
        assertTrue(pStartVararg(fileCtx))

        val pEndList = FilesRuleBuilder(graph).that().nameEndsWith(listOf(".kt")).getThatPredicate()!!
        assertTrue(pEndList(fileCtx))

        val pEndVararg = FilesRuleBuilder(graph).that().nameEndsWith(".kt", ".java").getThatPredicate()!!
        assertTrue(pEndVararg(fileCtx))

        val pMatchList = FilesRuleBuilder(graph).that().nameMatches(listOf("Class*.kt")).getThatPredicate()!!
        assertTrue(pMatchList(fileCtx))

        val pMatchVararg = FilesRuleBuilder(graph).that().nameMatches("Class*.kt", "File*.kt").getThatPredicate()!!
        assertTrue(pMatchVararg(fileCtx))
    }

    @Test
    fun `test FilesThat module residency and negations`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val pModSingleStd = FilesRuleBuilder(graph).that().inModule("app").getThatPredicate()!!
        assertTrue(pModSingleStd(fileCtx))

        val pModListStd = FilesRuleBuilder(graph).that().inModules(listOf(":app")).getThatPredicate()!!
        assertTrue(pModListStd(fileCtx))

        val pModVarargStd = FilesRuleBuilder(graph).that().inModules(":app", ":core").getThatPredicate()!!
        assertTrue(pModVarargStd(fileCtx))

        val pNotModSingleStd = FilesRuleBuilder(graph).that().notInModule("core").getThatPredicate()!!
        assertTrue(pNotModSingleStd(fileCtx))

        val pNotModListStd = FilesRuleBuilder(graph).that().notInModules(listOf(":core")).getThatPredicate()!!
        assertTrue(pNotModListStd(fileCtx))

        val pNotModVarargStd = FilesRuleBuilder(graph).that().notInModules(":core", ":feature").getThatPredicate()!!
        assertTrue(pNotModVarargStd(fileCtx))

        val pNotNameSingleStd = FilesRuleBuilder(graph).that().notNamed("Other.kt").getThatPredicate()!!
        assertTrue(pNotNameSingleStd(fileCtx))

        val pNotNameListStd = FilesRuleBuilder(graph).that().notNamed(listOf("Other.kt")).getThatPredicate()!!
        assertTrue(pNotNameListStd(fileCtx))

        val pNotNameVarargStd = FilesRuleBuilder(graph).that().notNamed("Other.kt", "Wrong.kt").getThatPredicate()!!
        assertTrue(pNotNameVarargStd(fileCtx))

        val pNotNamePredStd = FilesRuleBuilder(graph).that().notNamed { it.endsWith(".java") }.getThatPredicate()!!
        assertTrue(pNotNamePredStd(fileCtx))

        val pNotStartSingleStd = FilesRuleBuilder(graph).that().notNameStartsWith("Wrong").getThatPredicate()!!
        assertTrue(pNotStartSingleStd(fileCtx))

        val pNotStartListStd = FilesRuleBuilder(graph).that().notNameStartsWith(listOf("Wrong")).getThatPredicate()!!
        assertTrue(pNotStartListStd(fileCtx))

        val pNotStartVarargStd =
            FilesRuleBuilder(
                graph,
            ).that().notNameStartsWith("Wrong", "Bad").getThatPredicate()!!
        assertTrue(pNotStartVarargStd(fileCtx))

        val pNotEndSingleStd = FilesRuleBuilder(graph).that().notNameEndsWith(".java").getThatPredicate()!!
        assertTrue(pNotEndSingleStd(fileCtx))

        val pNotEndListStd = FilesRuleBuilder(graph).that().notNameEndsWith(listOf(".java")).getThatPredicate()!!
        assertTrue(pNotEndListStd(fileCtx))

        val pNotEndVarargStd = FilesRuleBuilder(graph).that().notNameEndsWith(".java", ".txt").getThatPredicate()!!
        assertTrue(pNotEndVarargStd(fileCtx))

        val pNotMatchSingleStd = FilesRuleBuilder(graph).that().notNameMatches("Wrong*").getThatPredicate()!!
        assertTrue(pNotMatchSingleStd(fileCtx))

        val pNotMatchListStd = FilesRuleBuilder(graph).that().notNameMatches(listOf("Wrong*")).getThatPredicate()!!
        assertTrue(pNotMatchListStd(fileCtx))

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
            ).that().annotatedWith("com.example.MyAnnotation").getThatPredicate()!!
        assertTrue(pHaveAnnot(fileCtx))

        val pHaveAllAnnot =
            FilesRuleBuilder(
                graph,
            ).that().annotatedWithAllOf("com.example.MyAnnotation").getThatPredicate()!!
        assertTrue(pHaveAllAnnot(fileCtx))

        val pHaveAllAnnotVararg =
            FilesRuleBuilder(
                graph,
            ).that().annotatedWithAllOf("com.example.MyAnnotation").getThatPredicate()!!
        assertTrue(pHaveAllAnnotVararg(fileCtx))

        val pHaveAnyAnnot =
            FilesRuleBuilder(
                graph,
            ).that().annotatedWithAnyOf("com.example.MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pHaveAnyAnnot(fileCtx))

        val pHaveAnyAnnotVararg =
            FilesRuleBuilder(
                graph,
            ).that().annotatedWithAnyOf("com.example.MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pHaveAnyAnnotVararg(fileCtx))

        // Composite & Negations
        val pAnyOf =
            FilesRuleBuilder(graph).that().anyOf(
                { named("Test.kt") },
                { named("Other.kt") },
            ).getThatPredicate()!!
        assertTrue(pAnyOf(fileCtx))

        val pAllOf =
            FilesRuleBuilder(graph).that().allOf(
                { named("Test.kt") },
                { inPackage("com.example") },
            ).getThatPredicate()!!
        assertTrue(pAllOf(fileCtx))

        val pNoneOf =
            FilesRuleBuilder(graph).that().noneOf(
                { named("Other.kt") },
            ).getThatPredicate()!!
        assertTrue(pNoneOf(fileCtx))

        val pNotPkgSingle = FilesRuleBuilder(graph).that().notInPackage("com.other").getThatPredicate()!!
        assertTrue(pNotPkgSingle(fileCtx))

        val pNotPkgList = FilesRuleBuilder(graph).that().notInPackage(listOf("com.other")).getThatPredicate()!!
        assertTrue(pNotPkgList(fileCtx))

        val pNotPkgVararg =
            FilesRuleBuilder(
                graph,
            ).that().notInPackage("com.other", "org.wrong").getThatPredicate()!!
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
