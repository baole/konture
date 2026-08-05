/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
internal class FilesShouldCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test FilesShould module residency assertions`() {
        val fileCtx = FileDeclarationContext(fileA, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vModSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAModule("app")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vModSingle)
        assertTrue(vModSingle.isEmpty())

        val vModList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInModules(listOf(":app", ":core"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vModList)
        assertTrue(vModList.isEmpty())

        val vModVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInModules(":core", ":feature")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vModVararg)
        assertEquals(1, vModVararg.size)

        val vNotModSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInAModule("core")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotModSingle)
        assertTrue(vNotModSingle.isEmpty())

        val vNotModList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInModules(listOf(":app"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotModList)
        assertEquals(1, vNotModList.size)

        val vNotModVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notResideInModules(":app", ":core")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotModVararg)
        assertEquals(1, vNotModVararg.size)
    }

    @Test
    fun `test FilesShould containClass and import assertions`() {
        val fileWithClassAndImport =
            FileDeclaration(
                name = "Test.kt",
                packageName = "com.example",
                classes = listOf(classA),
                imports = listOf("com.example.ClassA"),
            )
        val fileCtx = FileDeclarationContext(fileWithClassAndImport, ":app")
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileWithClassAndImport))),
                ),
            )

        // containClass / notContainClass
        val vClsSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().containClass("com.example.ClassA")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vClsSingle)
        assertTrue(vClsSingle.isEmpty())

        val vClsKClass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().containClass(String::class)
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vClsKClass)
        assertEquals(1, vClsKClass.size)

        val vNotCls = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notContainClass("com.example.ClassA")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotCls)
        assertEquals(1, vNotCls.size)

        val vClsList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().containClass(listOf("com.example.ClassA"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vClsList)
        assertTrue(vClsList.isEmpty())

        val vClsVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().containClass("com.example.ClassA", "com.example.ClassB")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vClsVararg)
        assertEquals(1, vClsVararg.size)

        // haveImportOf / notHaveImportOf
        val vImpSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveImportOf("com.example.ClassA")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vImpSingle)
        assertTrue(vImpSingle.isEmpty())

        val vImpKClass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveImportOf(String::class)
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vImpKClass)
        assertEquals(1, vImpKClass.size)

        val vNotImp = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveImportOf("com.example.ClassA")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotImp)
        assertEquals(1, vNotImp.size)

        val vImpList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveImportOf(listOf("com.example.ClassA"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vImpList)
        assertTrue(vImpList.isEmpty())

        val vNotImpList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveImportOf(listOf("com.example.ClassA"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotImpList)
        assertEquals(1, vNotImpList.size)
    }

    @Test
    fun `test FilesShould top level and structural elements`() {
        val func =
            FunctionDeclaration("myFunc", Visibility.PUBLIC, emptySet(), "Unit", emptyList(), emptyList(), null, false)
        val prop = PropertyDeclaration("myProp", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null)
        val fileWithTopLevel =
            FileDeclaration(
                name = "Top.kt",
                packageName = "com.example",
                topLevelFunctions = listOf(func),
                topLevelProperties = listOf(prop),
                classes = listOf(classA),
            )
        val fileCtx = FileDeclarationContext(fileWithTopLevel, ":app")
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileWithTopLevel))),
                ),
            )

        val vTopFunc = mutableListOf<String>()
        FilesRuleBuilder(graph).should().containTopLevelFunctions()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vTopFunc)
        assertTrue(vTopFunc.isEmpty())

        val vNotTopFunc = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notContainTopLevelFunctions()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotTopFunc)
        assertEquals(1, vNotTopFunc.size)

        val vTopProp = mutableListOf<String>()
        FilesRuleBuilder(graph).should().containTopLevelProperties()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vTopProp)
        assertTrue(vTopProp.isEmpty())

        val vNotTopProp = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notContainTopLevelProperties()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotTopProp)
        assertEquals(1, vNotTopProp.size)

        val vHaveFunc = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveTopLevelFunctions()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vHaveFunc)
        assertTrue(vHaveFunc.isEmpty())

        val vNotHaveFunc = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveTopLevelFunctions()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotHaveFunc)
        assertEquals(1, vNotHaveFunc.size)

        val vHaveProp = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveTopLevelProperties()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vHaveProp)
        assertTrue(vHaveProp.isEmpty())

        val vNotHaveProp = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveTopLevelProperties()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotHaveProp)
        assertEquals(1, vNotHaveProp.size)

        val vHaveClasses = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveClasses()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vHaveClasses)
        assertTrue(vHaveClasses.isEmpty())

        val vNotHaveClasses = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveClasses()
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotHaveClasses)
        assertEquals(1, vNotHaveClasses.size)

        // haveNoWildcardImports & haveAnnotationOf
        val fileNoWild = FileDeclaration("Clean.kt", "com.example", classes = listOf(classAnnotated))
        val fileCtxClean = FileDeclarationContext(fileNoWild, ":app")

        val vNoWild = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveNoWildcardImports()
            .getShouldAssertion()!!(fileCtxClean, listOf(fileCtxClean), vNoWild)
        assertTrue(vNoWild.isEmpty())

        val vAnnotStr = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveAnnotationOf("MyAnnotation")
            .getShouldAssertion()!!(fileCtxClean, listOf(fileCtxClean), vAnnotStr)
        assertTrue(vAnnotStr.isEmpty())
    }

    @Test
    fun `test FilesShould name assertions and package dependencies`() {
        val file = FileDeclaration("MyFile.kt", "com.example")
        val fileCtx = FileDeclarationContext(file, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file)))),
            )

        val vNameSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveName("MyFile.kt")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNameSingle)
        assertTrue(vNameSingle.isEmpty())

        val vNameList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveName(listOf("MyFile.kt", "Other.kt"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNameList)
        assertTrue(vNameList.isEmpty())

        val vNameVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveName("MyFile.kt", "Other.kt")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNameVararg)
        assertTrue(vNameVararg.isEmpty())

        val vNamePred = mutableListOf<String>()
        FilesRuleBuilder(graph).should().haveName { it.startsWith("My") }
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNamePred)
        assertTrue(vNamePred.isEmpty())

        val vNotNameSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveName("MyFile.kt")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotNameSingle)
        assertEquals(1, vNotNameSingle.size)

        val vNotNameList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveName(listOf("MyFile.kt"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotNameList)
        assertEquals(1, vNotNameList.size)

        val vNotNameVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveName("MyFile.kt", "Other.kt")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotNameVararg)
        assertEquals(1, vNotNameVararg.size)

        val vNotMatchSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameMatching("My*")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotMatchSingle)
        assertEquals(1, vNotMatchSingle.size)

        val vNotMatchList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameMatching(listOf("My*"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotMatchList)
        assertEquals(1, vNotMatchList.size)

        val vNotMatchVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameMatching("My*", "Other*")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotMatchVararg)
        assertEquals(1, vNotMatchVararg.size)

        val vNotStartSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameStartingWith("My")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotStartSingle)
        assertEquals(1, vNotStartSingle.size)

        val vNotStartList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameStartingWith(listOf("My"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotStartList)
        assertEquals(1, vNotStartList.size)

        val vNotStartVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameStartingWith("My", "Other")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotStartVararg)
        assertEquals(1, vNotStartVararg.size)

        val vNotEndSingle = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameEndingWith(".kt")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotEndSingle)
        assertEquals(1, vNotEndSingle.size)

        val vNotEndList = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameEndingWith(listOf(".kt"))
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotEndList)
        assertEquals(1, vNotEndList.size)

        val vNotEndVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notHaveNameEndingWith(".kt", ".java")
            .getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNotEndVararg)
        assertEquals(1, vNotEndVararg.size)

        // Packages & Modules dependency assertions
        val fileWithImp = FileDeclaration("Imp.kt", "com.example", imports = listOf("com.other.Feature"))
        val fileCtxImp = FileDeclarationContext(fileWithImp, ":app")

        val vOnlyPkg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().onlyDependOnPackages(listOf("com.other"))
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyPkg)
        assertTrue(vOnlyPkg.isEmpty())

        val vOnlyPkgVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().onlyDependOnPackages("com.other")
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyPkgVararg)
        assertTrue(vOnlyPkgVararg.isEmpty())

        val vNotPkg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notDependOnPackages(listOf("com.other"))
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotPkg)
        assertEquals(1, vNotPkg.size)

        val vNotPkgVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notDependOnPackages("com.other")
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotPkgVararg)
        assertEquals(1, vNotPkgVararg.size)

        val vOnlyMod = mutableListOf<String>()
        FilesRuleBuilder(graph).should().onlyDependOnModules(listOf(":app", ":core"))
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyMod)
        assertTrue(vOnlyMod.isEmpty())

        val vOnlyModVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().onlyDependOnModules(":app", ":core")
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyModVararg)
        assertTrue(vOnlyModVararg.isEmpty())

        val vNotMod = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notDependOnModules(listOf(":core"))
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotMod)
        assertTrue(vNotMod.isEmpty())

        val vNotModVararg = mutableListOf<String>()
        FilesRuleBuilder(graph).should().notDependOnModules(":core")
            .getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotModVararg)
        assertTrue(vNotModVararg.isEmpty())
    }

    @Test
    fun `test FilesShould failure messages`() {
        val file = FileDeclaration("MyFile.kt", "com.example")
        val fileCtx = FileDeclarationContext(file, ":app")
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(file)))),
            )

        val v1 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v1)
        assertEquals(1, v1.size)

        val v2 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().resideInAPackage(listOf("wrong.pkg")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg", "other").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v3)
        assertEquals(1, v3.size)

        val v4 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().resideInAPackage { false }.getShouldAssertion()!!(fileCtx, listOf(fileCtx), v4)
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notResideInAPackage("com.example").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v5)
        assertEquals(1, v5.size)

        val v6 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notResideInAPackage(listOf("com.example")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v6)
        assertEquals(1, v6.size)

        val v7 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveNameEndingWith("Wrong").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v7)
        assertEquals(1, v7.size)

        val v8 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveNameStartingWith("Wrong").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v9)
        assertEquals(1, v9.size)

        val v10 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveNameStartingWith(listOf("Wrong")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v10)
        assertEquals(1, v10.size)

        val v11 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveNameMatching("wrong*").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v11)
        assertEquals(1, v11.size)

        val v12 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveNameMatching(listOf("wrong*")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v12)
        assertEquals(1, v12.size)

        val v13 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().containClass("MissingClass").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v13)
        assertEquals(1, v13.size)

        val v14 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().containClass(listOf("MissingClass")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v14)
        assertEquals(1, v14.size)

        val v15 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveImportOf("MissingImport").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v15)
        assertEquals(1, v15.size)

        val v16 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveImportOf(listOf("MissingImport")).getShouldAssertion()!!(fileCtx, listOf(fileCtx), v16)
        assertEquals(1, v16.size)

        val v17 = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveAnnotationOf("MissingAnnotation").getShouldAssertion()!!(fileCtx, listOf(fileCtx), v17)
        assertEquals(1, v17.size)
    }

    @Test
    fun `test FilesShould usages, matching, and composite assertions`() {
        val usageCall =
            SourceUsage(
                UsageKind.CALL,
                "com.example.Foo.bar",
                "Test.kt",
                10,
                5,
                rawExpression = "Foo.bar()",
                unresolvedPossibleUsage = true,
            )
        val usageRef =
            SourceUsage(
                UsageKind.CLASS_REFERENCE,
                "com.example.TargetClass",
                "Test.kt",
                12,
                5,
                rawExpression = "TargetClass::class",
            )
        val fileWithUsages =
            FileDeclaration(
                "Test.kt",
                "com.example",
                classes = listOf(classA, classB),
                usages = listOf(usageCall, usageRef),
                imports = listOf("com.wrong.*"),
            )
        val fileCtx = FileDeclarationContext(fileWithUsages, ":app")
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileWithUsages))),
                ),
            )

        // notCall
        val vCallStr = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notCall("com.example.Foo.bar").getShouldAssertion()!!(fileCtx, listOf(fileCtx), vCallStr)
        assertEquals(1, vCallStr.size)

        val vCallKClass = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notCall(String::class).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vCallKClass)
        assertTrue(vCallKClass.isEmpty())

        val vCallReified = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notCall<String>().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vCallReified)
        assertTrue(vCallReified.isEmpty())

        // notReferenceClass
        val vRefStr = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notReferenceClass(
            "com.example.TargetClass",
        ).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vRefStr)
        assertEquals(1, vRefStr.size)

        val vRefKClass = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notReferenceClass(String::class).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vRefKClass)
        assertTrue(vRefKClass.isEmpty())

        val vRefReified = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notReferenceClass<String>().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vRefReified)
        assertTrue(vRefReified.isEmpty())

        // Wildcards & structural checks
        val vWildcard = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notHaveWildcardImports().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vWildcard)
        assertEquals(1, vWildcard.size)

        val vOneClass = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveOnlyOneClassPerFile().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vOneClass)
        assertEquals(1, vOneClass.size)

        val vMatchClsName = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().haveNameMatchingClassName().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vMatchClsName)
        assertEquals(1, vMatchClsName.size)

        val vKdoc = mutableListOf<String>()
        FilesRuleBuilder(graph).should().beDocumentedWithKDoc().getShouldAssertion()!!(fileCtx, listOf(fileCtx), vKdoc)
        assertEquals(1, vKdoc.size)

        // satisfy & composites
        val vSatisfy1 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().satisfy {
            it.declaration.name == "Test.kt"
        }.getShouldAssertion()!!(fileCtx, listOf(fileCtx), vSatisfy1)
        assertTrue(vSatisfy1.isEmpty())

        val vSatisfy2 = mutableListOf<String>()
        FilesRuleBuilder(graph).should().satisfy {
                _,
                v,
            ->
            v.add("error")
        }.getShouldAssertion()!!(fileCtx, listOf(fileCtx), vSatisfy2)
        assertEquals(1, vSatisfy2.size)

        val vAnyOfPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().anyOf({
            haveName("Test.kt")
        }, { haveName("Wrong.kt") }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vAnyOfPass)
        assertTrue(vAnyOfPass.isEmpty())

        val vAnyOfFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().anyOf({
            haveName("Wrong1.kt")
        }, { haveName("Wrong2.kt") }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vAnyOfFail)
        assertEquals(1, vAnyOfFail.size)

        val vAllOfPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().allOf({
            haveName("Test.kt")
        }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vAllOfPass)
        assertTrue(vAllOfPass.isEmpty())

        val vNoneOfPass = mutableListOf<String>()
        FilesRuleBuilder(graph).should().noneOf({
            haveName("Wrong.kt")
        }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNoneOfPass)
        assertTrue(vNoneOfPass.isEmpty())

        val vNoneOfFail = mutableListOf<String>()
        FilesRuleBuilder(graph).should().noneOf({
            haveName("Test.kt")
        }).getShouldAssertion()!!(fileCtx, listOf(fileCtx), vNoneOfFail)
        assertEquals(1, vNoneOfFail.size)

        // Unauthorized packages & modules
        val fileCtxImp =
            FileDeclarationContext(
                FileDeclaration("Imp.kt", "com.example", imports = listOf("com.prohibited.Feature")),
                ":app",
            )
        val vOnlyPkg = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().onlyDependOnPackages("com.allowed").getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vOnlyPkg)
        assertEquals(1, vOnlyPkg.size)

        val vNotPkg = mutableListOf<String>()
        FilesRuleBuilder(
            graph,
        ).should().notDependOnPackages("com.prohibited").getShouldAssertion()!!(fileCtxImp, listOf(fileCtxImp), vNotPkg)
        assertEquals(1, vNotPkg.size)

        val otherMod = Module(":", ":other", "other", emptyList(), emptyList(), emptyList(), listOf(fileA))
        val graphWithModules =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileWithUsages)), otherMod),
                ),
            )
        val fileWithOtherModUsage =
            FileDeclaration(
                "Usage.kt",
                "com.example",
                usages = listOf(SourceUsage(UsageKind.CLASS_REFERENCE, "com.example.ClassA", "Usage.kt", 1, 1)),
            )
        val fileCtxOtherUsage = FileDeclarationContext(fileWithOtherModUsage, ":other")

        val vOnlyMod = mutableListOf<String>()
        FilesRuleBuilder(
            graphWithModules,
        ).should().onlyDependOnModules(
            ":allowed",
        ).getShouldAssertion()!!(fileCtxOtherUsage, listOf(fileCtxOtherUsage), vOnlyMod)
        assertEquals(1, vOnlyMod.size)

        val vNotMod = mutableListOf<String>()
        FilesRuleBuilder(
            graphWithModules,
        ).should().notDependOnModules(
            ":app",
        ).getShouldAssertion()!!(fileCtxOtherUsage, listOf(fileCtxOtherUsage), vNotMod)
        assertEquals(1, vNotMod.size)
    }
}
