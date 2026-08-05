/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("LargeClass", "LongMethod")
internal class ClassesCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test ClassesShouldPackageAssertions package, module, and name`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vPkgSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAPackage("com.example")
            .getShouldAssertion()!!(classA, listOf(classA), vPkgSingle)
        assertTrue(vPkgSingle.isEmpty())

        val vPkgList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAPackage(listOf("com.example"))
            .getShouldAssertion()!!(classA, listOf(classA), vPkgList)
        assertTrue(vPkgList.isEmpty())

        val vPkgVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAPackage("com.example", "com.other")
            .getShouldAssertion()!!(classA, listOf(classA), vPkgVararg)
        assertTrue(vPkgVararg.isEmpty())

        val vPkgPred = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAPackage { it.startsWith("com") }
            .getShouldAssertion()!!(classA, listOf(classA), vPkgPred)
        assertTrue(vPkgPred.isEmpty())

        val vNotPkgSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notResideInAPackage("com.other")
            .getShouldAssertion()!!(classA, listOf(classA), vNotPkgSingle)
        assertTrue(vNotPkgSingle.isEmpty())

        val vNotPkgList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notResideInAPackage(listOf("com.other"))
            .getShouldAssertion()!!(classA, listOf(classA), vNotPkgList)
        assertTrue(vNotPkgList.isEmpty())

        val vNotPkgVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notResideInAPackage("com.other", "org.wrong")
            .getShouldAssertion()!!(classA, listOf(classA), vNotPkgVararg)
        assertTrue(vNotPkgVararg.isEmpty())

        val vModSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAModule("app")
            .getShouldAssertion()!!(classA, listOf(classA), vModSingle)
        assertTrue(vModSingle.isEmpty())

        val vModList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAModule(listOf("app"))
            .getShouldAssertion()!!(classA, listOf(classA), vModList)
        assertTrue(vModList.isEmpty())

        val vModVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAModule("app", "core")
            .getShouldAssertion()!!(classA, listOf(classA), vModVararg)
        assertTrue(vModVararg.isEmpty())

        val vModAlias = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInModule("app")
            .getShouldAssertion()!!(classA, listOf(classA), vModAlias)
        assertTrue(vModAlias.isEmpty())

        val vModsAlias = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInModules(listOf("app"))
            .getShouldAssertion()!!(classA, listOf(classA), vModsAlias)
        assertTrue(vModsAlias.isEmpty())

        val vModsVarargAlias = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInModules("app", "core")
            .getShouldAssertion()!!(classA, listOf(classA), vModsVarargAlias)
        assertTrue(vModsVarargAlias.isEmpty())

        val vNotModSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notResideInAModule("core")
            .getShouldAssertion()!!(classA, listOf(classA), vNotModSingle)
        assertTrue(vNotModSingle.isEmpty())

        val vNotModList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notResideInAModule(listOf("core"))
            .getShouldAssertion()!!(classA, listOf(classA), vNotModList)
        assertTrue(vNotModList.isEmpty())

        val vNotModVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notResideInAModule("core", "feature")
            .getShouldAssertion()!!(classA, listOf(classA), vNotModVararg)
        assertTrue(vNotModVararg.isEmpty())

        val vEndSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameEndingWith("A")
            .getShouldAssertion()!!(classA, listOf(classA), vEndSingle)
        assertTrue(vEndSingle.isEmpty())

        val vEndList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameEndingWith(listOf("A"))
            .getShouldAssertion()!!(classA, listOf(classA), vEndList)
        assertTrue(vEndList.isEmpty())

        val vEndVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameEndingWith("A", "B")
            .getShouldAssertion()!!(classA, listOf(classA), vEndVararg)
        assertTrue(vEndVararg.isEmpty())

        val vStartSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameStartingWith("Class")
            .getShouldAssertion()!!(classA, listOf(classA), vStartSingle)
        assertTrue(vStartSingle.isEmpty())

        val vStartList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameStartingWith(listOf("Class"))
            .getShouldAssertion()!!(classA, listOf(classA), vStartList)
        assertTrue(vStartList.isEmpty())

        val vStartVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameStartingWith("Class", "My")
            .getShouldAssertion()!!(classA, listOf(classA), vStartVararg)
        assertTrue(vStartVararg.isEmpty())

        val vMatchSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameMatching("Class*")
            .getShouldAssertion()!!(classA, listOf(classA), vMatchSingle)
        assertTrue(vMatchSingle.isEmpty())

        val vMatchList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameMatching(listOf("Class*"))
            .getShouldAssertion()!!(classA, listOf(classA), vMatchList)
        assertTrue(vMatchList.isEmpty())

        val vMatchVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNameMatching("Class*", "My*")
            .getShouldAssertion()!!(classA, listOf(classA), vMatchVararg)
        assertTrue(vMatchVararg.isEmpty())

        val vNameSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveName("ClassA")
            .getShouldAssertion()!!(classA, listOf(classA), vNameSingle)
        assertTrue(vNameSingle.isEmpty())

        val vNameList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveName(listOf("ClassA"))
            .getShouldAssertion()!!(classA, listOf(classA), vNameList)
        assertTrue(vNameList.isEmpty())

        val vNameVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveName("ClassA", "ClassB")
            .getShouldAssertion()!!(classA, listOf(classA), vNameVararg)
        assertTrue(vNameVararg.isEmpty())

        val vNotNameSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveName("ClassB")
            .getShouldAssertion()!!(classA, listOf(classA), vNotNameSingle)
        assertTrue(vNotNameSingle.isEmpty())

        val vNotNameList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveName(listOf("ClassB"))
            .getShouldAssertion()!!(classA, listOf(classA), vNotNameList)
        assertTrue(vNotNameList.isEmpty())

        val vNotNameVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveName("ClassB", "ClassC")
            .getShouldAssertion()!!(classA, listOf(classA), vNotNameVararg)
        assertTrue(vNotNameVararg.isEmpty())

        val vNotMatchSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameMatching("Wrong*")
            .getShouldAssertion()!!(classA, listOf(classA), vNotMatchSingle)
        assertTrue(vNotMatchSingle.isEmpty())

        val vNotMatchList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameMatching(listOf("Wrong*"))
            .getShouldAssertion()!!(classA, listOf(classA), vNotMatchList)
        assertTrue(vNotMatchList.isEmpty())

        val vNotMatchVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameMatching("Wrong*", "Bad*")
            .getShouldAssertion()!!(classA, listOf(classA), vNotMatchVararg)
        assertTrue(vNotMatchVararg.isEmpty())

        val vNotStartSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameStartingWith("Wrong")
            .getShouldAssertion()!!(classA, listOf(classA), vNotStartSingle)
        assertTrue(vNotStartSingle.isEmpty())

        val vNotStartList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameStartingWith(listOf("Wrong"))
            .getShouldAssertion()!!(classA, listOf(classA), vNotStartList)
        assertTrue(vNotStartList.isEmpty())

        val vNotStartVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameStartingWith("Wrong", "Bad")
            .getShouldAssertion()!!(classA, listOf(classA), vNotStartVararg)
        assertTrue(vNotStartVararg.isEmpty())

        val vNotEndSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameEndingWith("Wrong")
            .getShouldAssertion()!!(classA, listOf(classA), vNotEndSingle)
        assertTrue(vNotEndSingle.isEmpty())

        val vNotEndList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameEndingWith(listOf("Wrong"))
            .getShouldAssertion()!!(classA, listOf(classA), vNotEndList)
        assertTrue(vNotEndList.isEmpty())

        val vNotEndVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notHaveNameEndingWith("Wrong", "Bad")
            .getShouldAssertion()!!(classA, listOf(classA), vNotEndVararg)
        assertTrue(vNotEndVararg.isEmpty())
    }

    @Test
    fun `test ClassesShouldMetadataAssertions annotations, modifiers, types, and structure`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vAnnotSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnnotationOf("MyAnnotation")
            .getShouldAssertion()!!(classAnnotated, listOf(classAnnotated), vAnnotSingle)
        assertTrue(vAnnotSingle.isEmpty())

        val vAllAnnotSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAllAnnotationsOf("MyAnnotation")
            .getShouldAssertion()!!(classAnnotated, listOf(classAnnotated), vAllAnnotSingle)
        assertTrue(vAllAnnotSingle.isEmpty())

        val vAllAnnotList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAllAnnotationsOf(listOf("MyAnnotation"))
            .getShouldAssertion()!!(classAnnotated, listOf(classAnnotated), vAllAnnotList)
        assertTrue(vAllAnnotList.isEmpty())

        val vAllAnnotVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAllAnnotationsOf("MyAnnotation", "Other")
            .getShouldAssertion()!!(classAnnotated, listOf(classAnnotated), vAllAnnotVararg)
        assertEquals(1, vAllAnnotVararg.size)

        val vAnyAnnotSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnyAnnotationOf("MyAnnotation")
            .getShouldAssertion()!!(classAnnotated, listOf(classAnnotated), vAnyAnnotSingle)
        assertTrue(vAnyAnnotSingle.isEmpty())

        val vAnyAnnotList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnyAnnotationOf(listOf("MyAnnotation"))
            .getShouldAssertion()!!(classAnnotated, listOf(classAnnotated), vAnyAnnotList)
        assertTrue(vAnyAnnotList.isEmpty())

        val vAnyAnnotVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnyAnnotationOf("MyAnnotation", "Other")
            .getShouldAssertion()!!(classAnnotated, listOf(classAnnotated), vAnyAnnotVararg)
        assertTrue(vAnyAnnotVararg.isEmpty())

        val vInterface = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInterfaces()
            .getShouldAssertion()!!(classInterface, listOf(classInterface), vInterface)
        assertTrue(vInterface.isEmpty())

        val vAbstract = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAbstract()
            .getShouldAssertion()!!(classAbstract, listOf(classAbstract), vAbstract)
        assertTrue(vAbstract.isEmpty())

        val vSealed = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beSealed()
            .getShouldAssertion()!!(classSealed, listOf(classSealed), vSealed)
        assertTrue(vSealed.isEmpty())

        val vData = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beData()
            .getShouldAssertion()!!(classData, listOf(classData), vData)
        assertTrue(vData.isEmpty())

        val vInline = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInline()
            .getShouldAssertion()!!(classInline, listOf(classInline), vInline)
        assertTrue(vInline.isEmpty())

        val vTopLevel = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beTopLevel()
            .getShouldAssertion()!!(classA, listOf(classA), vTopLevel)
        assertTrue(vTopLevel.isEmpty())

        val vModifier = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveModifier(Modifier.DATA)
            .getShouldAssertion()!!(classData, listOf(classData), vModifier)
        assertTrue(vModifier.isEmpty())

        val vAllModifiersSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAllModifiers(Modifier.DATA)
            .getShouldAssertion()!!(classData, listOf(classData), vAllModifiersSingle)
        assertTrue(vAllModifiersSingle.isEmpty())

        val vAllModifiersList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAllModifiers(listOf(Modifier.DATA))
            .getShouldAssertion()!!(classData, listOf(classData), vAllModifiersList)
        assertTrue(vAllModifiersList.isEmpty())

        val vAllModifiersVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAllModifiers(Modifier.DATA)
            .getShouldAssertion()!!(classData, listOf(classData), vAllModifiersVararg)
        assertTrue(vAllModifiersVararg.isEmpty())

        val vAnyModifierSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnyModifier(Modifier.DATA)
            .getShouldAssertion()!!(classData, listOf(classData), vAnyModifierSingle)
        assertTrue(vAnyModifierSingle.isEmpty())

        val vAnyModifierList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnyModifier(listOf(Modifier.DATA))
            .getShouldAssertion()!!(classData, listOf(classData), vAnyModifierList)
        assertTrue(vAnyModifierList.isEmpty())

        val vAnyModifierVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnyModifier(Modifier.DATA, Modifier.SEALED)
            .getShouldAssertion()!!(classData, listOf(classData), vAnyModifierVararg)
        assertTrue(vAnyModifierVararg.isEmpty())

        val vVisSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveVisibility(Visibility.INTERNAL)
            .getShouldAssertion()!!(classInternal, listOf(classInternal), vVisSingle)
        assertTrue(vVisSingle.isEmpty())

        val vVisList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnyVisibility(listOf(Visibility.INTERNAL, Visibility.PUBLIC))
            .getShouldAssertion()!!(classInternal, listOf(classInternal), vVisList)
        assertTrue(vVisList.isEmpty())

        val vVisVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveAnyVisibility(Visibility.INTERNAL, Visibility.PUBLIC)
            .getShouldAssertion()!!(classInternal, listOf(classInternal), vVisVararg)
        assertTrue(vVisVararg.isEmpty())

        val vPub = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePublic()
            .getShouldAssertion()!!(classA, listOf(classA), vPub)
        assertTrue(vPub.isEmpty())

        val vInternal = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInternal()
            .getShouldAssertion()!!(classInternal, listOf(classInternal), vInternal)
        assertTrue(vInternal.isEmpty())

        val vPrivate = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePrivate()
            .getShouldAssertion()!!(classPrivate, listOf(classPrivate), vPrivate)
        assertTrue(vPrivate.isEmpty())

        val vProtected = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beProtected()
            .getShouldAssertion()!!(classProtected, listOf(classProtected), vProtected)
        assertTrue(vProtected.isEmpty())

        val vAssignTo = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableTo("com.example.ParentType")
            .getShouldAssertion()!!(classWithParent, listOf(classWithParent), vAssignTo)
        assertTrue(vAssignTo.isEmpty())

        val vAssignToAnySingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableToAnyOf("com.example.ParentType")
            .getShouldAssertion()!!(classWithParent, listOf(classWithParent), vAssignToAnySingle)
        assertTrue(vAssignToAnySingle.isEmpty())

        val vAssignToAnyList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableToAnyOf(listOf("com.example.ParentType"))
            .getShouldAssertion()!!(classWithParent, listOf(classWithParent), vAssignToAnyList)
        assertTrue(vAssignToAnyList.isEmpty())

        val vAssignToAnyVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableToAnyOf("com.example.ParentType", "Other")
            .getShouldAssertion()!!(classWithParent, listOf(classWithParent), vAssignToAnyVararg)
        assertTrue(vAssignToAnyVararg.isEmpty())

        val vAssignToAllSingle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableToAllOf("com.example.ParentType")
            .getShouldAssertion()!!(classWithParent, listOf(classWithParent), vAssignToAllSingle)
        assertTrue(vAssignToAllSingle.isEmpty())

        val vAssignToAllList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableToAllOf(listOf("com.example.ParentType"))
            .getShouldAssertion()!!(classWithParent, listOf(classWithParent), vAssignToAllList)
        assertTrue(vAssignToAllList.isEmpty())

        val vAssignToAllVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableToAllOf("com.example.ParentType")
            .getShouldAssertion()!!(classWithParent, listOf(classWithParent), vAssignToAllVararg)
        assertTrue(vAssignToAllVararg.isEmpty())

        val vAssignFrom = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAssignableFrom("ClassWithParent")
            .getShouldAssertion()!!(classWithParent, listOf(classWithParent), vAssignFrom)
        assertTrue(vAssignFrom.isEmpty())

        // Enum, Inner, Nested, notBe* methods
        val classEnum =
            ClassDeclaration("MyEnum", "com.example.MyEnum", "com.example", isInterface = false, isAbstract = false, isEnum = true, annotations = emptyList(), imports = emptyList(), referencedTypes = emptySet(), filePath = "/src/MyEnum.kt")
        val classInner =
            ClassDeclaration(
                "MyInner", "com.example.MyInner", "com.example", isInterface = false, isAbstract = false, annotations = emptyList(), imports = emptyList(), referencedTypes = emptySet(), filePath = "/src/MyInner.kt",
                modifiers =
                    setOf(
                        Modifier.INNER,
                    ),
            )
        val classNested =
            ClassDeclaration("Nested", "com.example.Outer.Nested", "com.example", isInterface = false, isAbstract = false, annotations = emptyList(), imports = emptyList(), referencedTypes = emptySet(), filePath = "/src/Outer.kt")

        val vEnum = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beEnums().getShouldAssertion()!!(classEnum, listOf(classEnum), vEnum)
        assertTrue(vEnum.isEmpty())

        val vInner = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInner().getShouldAssertion()!!(classInner, listOf(classInner), vInner)
        assertTrue(vInner.isEmpty())

        val vNested = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beNested().getShouldAssertion()!!(classNested, listOf(classNested), vNested)
        assertTrue(vNested.isEmpty())

        val vNotAbs = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeAbstract().getShouldAssertion()!!(classA, listOf(classA), vNotAbs)
        assertTrue(vNotAbs.isEmpty())

        val vNotSealed = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeSealed().getShouldAssertion()!!(classA, listOf(classA), vNotSealed)
        assertTrue(vNotSealed.isEmpty())

        val vNotData = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeData().getShouldAssertion()!!(classA, listOf(classA), vNotData)
        assertTrue(vNotData.isEmpty())

        val vNotInline = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeInline().getShouldAssertion()!!(classA, listOf(classA), vNotInline)
        assertTrue(vNotInline.isEmpty())

        val vNotOpen = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeOpen().getShouldAssertion()!!(classA, listOf(classA), vNotOpen)
        assertTrue(vNotOpen.isEmpty())

        val vNotInner = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeInner().getShouldAssertion()!!(classA, listOf(classA), vNotInner)
        assertTrue(vNotInner.isEmpty())

        val vNotInterface = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeInterface().getShouldAssertion()!!(classA, listOf(classA), vNotInterface)
        assertTrue(vNotInterface.isEmpty())

        // Property & Function containment
        val propDecl = PropertyDeclaration("p1", Visibility.PUBLIC, emptySet(), "String", true, emptyList(), null)
        val funcDecl =
            FunctionDeclaration("f1", Visibility.PUBLIC, emptySet(), "Unit", emptyList(), emptyList(), null, false)
        val classWithPropFunc =
            ClassDeclaration(
                "ClassWithPropFunc", "com.example.ClassWithPropFunc", "com.example", isInterface = false, isAbstract = false, annotations = emptyList(), imports = emptyList(), referencedTypes = emptySet(), filePath = "/src/ClassWithPropFunc.kt",
                properties =
                    listOf(
                        propDecl,
                    ),
                functions = listOf(funcDecl),
            )

        val vContainPropStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperty(
            "p1",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainPropStr)
        assertTrue(vContainPropStr.isEmpty())

        val vContainPropList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperty(
            listOf("p1"),
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainPropList)
        assertTrue(vContainPropList.isEmpty())

        val vContainPropVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperty(
            "p1",
            "p2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainPropVararg)
        assertEquals(1, vContainPropVararg.size)

        val vContainPropsList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperties(
            listOf("p1"),
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainPropsList)
        assertTrue(vContainPropsList.isEmpty())

        val vContainPropsVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperties(
            "p1",
            "p2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainPropsVararg)
        assertEquals(1, vContainPropsVararg.size)

        val vNotContainPropStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperty(
            "p2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainPropStr)
        assertTrue(vNotContainPropStr.isEmpty())

        val vNotContainPropList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperty(
            listOf("p1"),
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainPropList)
        assertEquals(1, vNotContainPropList.size)

        val vNotContainPropVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperty(
            "p1",
            "p2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainPropVararg)
        assertEquals(1, vNotContainPropVararg.size)

        val vNotContainPropsList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperties(
            listOf("p1"),
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainPropsList)
        assertEquals(1, vNotContainPropsList.size)

        val vNotContainPropsVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperties(
            "p1",
            "p2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainPropsVararg)
        assertEquals(1, vNotContainPropsVararg.size)

        val vContainFuncStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunction(
            "f1",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainFuncStr)
        assertTrue(vContainFuncStr.isEmpty())

        val vContainFuncList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunction(
            listOf("f1"),
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainFuncList)
        assertTrue(vContainFuncList.isEmpty())

        val vContainFuncVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunction(
            "f1",
            "f2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainFuncVararg)
        assertEquals(1, vContainFuncVararg.size)

        val vContainFuncsList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunctions(
            listOf("f1"),
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainFuncsList)
        assertTrue(vContainFuncsList.isEmpty())

        val vContainFuncsVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunctions(
            "f1",
            "f2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainFuncsVararg)
        assertEquals(1, vContainFuncsVararg.size)

        val vNotContainFuncStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunction(
            "f2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainFuncStr)
        assertTrue(vNotContainFuncStr.isEmpty())

        val vNotContainFuncList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunction(
            listOf("f1"),
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainFuncList)
        assertEquals(1, vNotContainFuncList.size)

        val vNotContainFuncVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunction(
            "f1",
            "f2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainFuncVararg)
        assertEquals(1, vNotContainFuncVararg.size)

        val vNotContainFuncsList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunctions(
            listOf("f1"),
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainFuncsList)
        assertEquals(1, vNotContainFuncsList.size)

        val vNotContainFuncsVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunctions(
            "f1",
            "f2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vNotContainFuncsVararg)
        assertEquals(1, vNotContainFuncsVararg.size)
    }

    @Test
    fun `test ClassesThat filters`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val pPkgSingle = ClassesRuleBuilder(graph).that().resideInAPackage("com.example").getThatPredicate()!!
        assertTrue(pPkgSingle(classA))
        assertFalse(pPkgSingle(classC))

        val pPkgList = ClassesRuleBuilder(graph).that().resideInAPackage(listOf("com.example")).getThatPredicate()!!
        assertTrue(pPkgList(classA))
        assertFalse(pPkgList(classC))

        val pPkgVararg =
            ClassesRuleBuilder(
                graph,
            ).that().resideInAPackage("com.example", "com.example.service").getThatPredicate()!!
        assertTrue(pPkgVararg(classA))
        assertFalse(pPkgVararg(classC))

        val pPkgPred =
            ClassesRuleBuilder(
                graph,
            ).that().resideInAPackage { it.startsWith("com.example") }.getThatPredicate()!!
        assertTrue(pPkgPred(classA))
        assertFalse(pPkgPred(classC))

        val pNameSingle = ClassesRuleBuilder(graph).that().haveName("ClassA").getThatPredicate()!!
        assertTrue(pNameSingle(classA))
        assertFalse(pNameSingle(classB))

        val pNameList = ClassesRuleBuilder(graph).that().haveName(listOf("ClassA")).getThatPredicate()!!
        assertTrue(pNameList(classA))
        assertFalse(pNameList(classB))

        val pNameVararg = ClassesRuleBuilder(graph).that().haveName("ClassA", "ClassB").getThatPredicate()!!
        assertTrue(pNameVararg(classA))
        assertFalse(pNameVararg(classInterface))

        val pNotNameSingle = ClassesRuleBuilder(graph).that().notHaveName("ClassB").getThatPredicate()!!
        assertTrue(pNotNameSingle(classA))
        assertFalse(pNotNameSingle(classB))

        val pNotNameList = ClassesRuleBuilder(graph).that().notHaveName(listOf("ClassB")).getThatPredicate()!!
        assertTrue(pNotNameList(classA))
        assertFalse(pNotNameList(classB))

        val pNotNameVararg = ClassesRuleBuilder(graph).that().notHaveName("ClassB", "ClassC").getThatPredicate()!!
        assertTrue(pNotNameVararg(classA))
        assertFalse(pNotNameVararg(classB))

        val pEndSingle = ClassesRuleBuilder(graph).that().haveNameEndingWith("A").getThatPredicate()!!
        assertTrue(pEndSingle(classA))
        assertFalse(pEndSingle(classB))

        val pEndList = ClassesRuleBuilder(graph).that().haveNameEndingWith(listOf("A")).getThatPredicate()!!
        assertTrue(pEndList(classA))
        assertFalse(pEndList(classB))

        val pEndVararg = ClassesRuleBuilder(graph).that().haveNameEndingWith("A", "B").getThatPredicate()!!
        assertTrue(pEndVararg(classA))
        assertFalse(pEndVararg(classInterface))

        val nonMatchingClass =
            ClassDeclaration("X", "com.other.X", "com.other", false, false, emptyList(), emptyList(), emptySet(), "/src/X.kt")

        val pStartSingle = ClassesRuleBuilder(graph).that().haveNameStartingWith("Class").getThatPredicate()!!
        assertTrue(pStartSingle(classA))
        assertFalse(pStartSingle(nonMatchingClass))

        val pStartList = ClassesRuleBuilder(graph).that().haveNameStartingWith(listOf("Class")).getThatPredicate()!!
        assertTrue(pStartList(classA))
        assertFalse(pStartList(nonMatchingClass))

        val pStartVararg = ClassesRuleBuilder(graph).that().haveNameStartingWith("Class", "My").getThatPredicate()!!
        assertTrue(pStartVararg(classA))
        assertFalse(pStartVararg(nonMatchingClass))

        val pMatchSingle = ClassesRuleBuilder(graph).that().haveNameMatching("Class*").getThatPredicate()!!
        assertTrue(pMatchSingle(classA))
        assertFalse(pMatchSingle(nonMatchingClass))

        val pMatchList = ClassesRuleBuilder(graph).that().haveNameMatching(listOf("Class*")).getThatPredicate()!!
        assertTrue(pMatchList(classA))
        assertFalse(pMatchList(nonMatchingClass))

        val pMatchVararg = ClassesRuleBuilder(graph).that().haveNameMatching("Class*", "Other*").getThatPredicate()!!
        assertTrue(pMatchVararg(classA))
        assertFalse(pMatchVararg(nonMatchingClass))

        val pAnnotSingle = ClassesRuleBuilder(graph).that().haveAnnotationOf("MyAnnotation").getThatPredicate()!!
        assertTrue(pAnnotSingle(classAnnotated))
        assertFalse(pAnnotSingle(classA))

        val pAnnotAllSingle = ClassesRuleBuilder(graph).that().haveAllAnnotationsOf("MyAnnotation").getThatPredicate()!!
        assertTrue(pAnnotAllSingle(classAnnotated))
        assertFalse(pAnnotAllSingle(classA))

        val pAnnotAllList =
            ClassesRuleBuilder(
                graph,
            ).that().haveAllAnnotationsOf(listOf("MyAnnotation")).getThatPredicate()!!
        assertTrue(pAnnotAllList(classAnnotated))
        assertFalse(pAnnotAllList(classA))

        val pAnnotAllVararg = ClassesRuleBuilder(graph).that().haveAllAnnotationsOf("MyAnnotation").getThatPredicate()!!
        assertTrue(pAnnotAllVararg(classAnnotated))
        assertFalse(pAnnotAllVararg(classA))

        val pAnnotAnySingle = ClassesRuleBuilder(graph).that().haveAnyAnnotationOf("MyAnnotation").getThatPredicate()!!
        assertTrue(pAnnotAnySingle(classAnnotated))
        assertFalse(pAnnotAnySingle(classA))

        val pAnnotAnyList =
            ClassesRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf(listOf("MyAnnotation")).getThatPredicate()!!
        assertTrue(pAnnotAnyList(classAnnotated))
        assertFalse(pAnnotAnyList(classA))

        val pAnnotAnyVararg =
            ClassesRuleBuilder(
                graph,
            ).that().haveAnyAnnotationOf("MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pAnnotAnyVararg(classAnnotated))
        assertFalse(pAnnotAnyVararg(classA))

        val pInterface = ClassesRuleBuilder(graph).that().areInterfaces().getThatPredicate()!!
        assertTrue(pInterface(classInterface))
        assertFalse(pInterface(classA))

        val pAbstract = ClassesRuleBuilder(graph).that().areAbstract().getThatPredicate()!!
        assertTrue(pAbstract(classAbstract))
        assertFalse(pAbstract(classA))

        val pVisSingle = ClassesRuleBuilder(graph).that().haveVisibility(Visibility.INTERNAL).getThatPredicate()!!
        assertTrue(pVisSingle(classInternal))
        assertFalse(pVisSingle(classA))

        val pVisList =
            ClassesRuleBuilder(
                graph,
            ).that().haveAnyVisibility(listOf(Visibility.INTERNAL)).getThatPredicate()!!
        assertTrue(pVisList(classInternal))
        assertFalse(pVisList(classA))

        val pVisVararg =
            ClassesRuleBuilder(
                graph,
            ).that().haveAnyVisibility(Visibility.INTERNAL, Visibility.PUBLIC).getThatPredicate()!!
        assertTrue(pVisVararg(classInternal))
        assertFalse(pVisVararg(classPrivate))

        val pPub = ClassesRuleBuilder(graph).that().bePublic().getThatPredicate()!!
        assertTrue(pPub(classA))
        assertFalse(pPub(classInternal))

        val pInternal = ClassesRuleBuilder(graph).that().beInternal().getThatPredicate()!!
        assertTrue(pInternal(classInternal))
        assertFalse(pInternal(classA))

        val pPrivate = ClassesRuleBuilder(graph).that().bePrivate().getThatPredicate()!!
        assertTrue(pPrivate(classPrivate))
        assertFalse(pPrivate(classA))

        val pProtected = ClassesRuleBuilder(graph).that().beProtected().getThatPredicate()!!
        assertTrue(pProtected(classProtected))
        assertFalse(pProtected(classA))

        val pModifier = ClassesRuleBuilder(graph).that().haveModifier(Modifier.DATA).getThatPredicate()!!
        assertTrue(pModifier(classData))
        assertFalse(pModifier(classA))

        val pAnyModifierList =
            ClassesRuleBuilder(
                graph,
            ).that().haveAnyModifier(listOf(Modifier.DATA)).getThatPredicate()!!
        assertTrue(pAnyModifierList(classData))
        assertFalse(pAnyModifierList(classA))

        val pAnyModifierVararg =
            ClassesRuleBuilder(
                graph,
            ).that().haveAnyModifier(Modifier.DATA, Modifier.SEALED).getThatPredicate()!!
        assertTrue(pAnyModifierVararg(classData))
        assertFalse(pAnyModifierVararg(classA))

        val pAllModifiersList =
            ClassesRuleBuilder(
                graph,
            ).that().haveAllModifiers(listOf(Modifier.DATA)).getThatPredicate()!!
        assertTrue(pAllModifiersList(classData))
        assertFalse(pAllModifiersList(classA))

        val pAllModifiersVararg = ClassesRuleBuilder(graph).that().haveAllModifiers(Modifier.DATA).getThatPredicate()!!
        assertTrue(pAllModifiersVararg(classData))
        assertFalse(pAllModifiersVararg(classA))

        val pSealed = ClassesRuleBuilder(graph).that().beSealed().getThatPredicate()!!
        assertTrue(pSealed(classSealed))
        assertFalse(pSealed(classA))

        val pData = ClassesRuleBuilder(graph).that().beData().getThatPredicate()!!
        assertTrue(pData(classData))
        assertFalse(pData(classA))

        val pInline = ClassesRuleBuilder(graph).that().beInline().getThatPredicate()!!
        assertTrue(pInline(classInline))
        assertFalse(pInline(classA))

        val pAssignTo = ClassesRuleBuilder(graph).that().areAssignableTo("com.example.ParentType").getThatPredicate()!!
        assertTrue(pAssignTo(classWithParent))
        assertFalse(pAssignTo(classA))

        val pAssignToAnySingle =
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAnyOf("com.example.ParentType").getThatPredicate()!!
        assertTrue(pAssignToAnySingle(classWithParent))
        assertFalse(pAssignToAnySingle(classA))

        val pAssignToAnyList =
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAnyOf(listOf("com.example.ParentType")).getThatPredicate()!!
        assertTrue(pAssignToAnyList(classWithParent))
        assertFalse(pAssignToAnyList(classA))

        val pAssignToAnyVararg =
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAnyOf("com.example.ParentType", "Other").getThatPredicate()!!
        assertTrue(pAssignToAnyVararg(classWithParent))
        assertFalse(pAssignToAnyVararg(classA))

        val pAssignToAllSingle =
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAllOf("com.example.ParentType").getThatPredicate()!!
        assertTrue(pAssignToAllSingle(classWithParent))
        assertFalse(pAssignToAllSingle(classA))

        val pAssignToAllList =
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAllOf(listOf("com.example.ParentType")).getThatPredicate()!!
        assertTrue(pAssignToAllList(classWithParent))
        assertFalse(pAssignToAllList(classA))

        val pAssignToAllVararg =
            ClassesRuleBuilder(
                graph,
            ).that().areAssignableToAllOf("com.example.ParentType").getThatPredicate()!!
        assertTrue(pAssignToAllVararg(classWithParent))
        assertFalse(pAssignToAllVararg(classA))

        val pAssignFrom = ClassesRuleBuilder(graph).that().areAssignableFrom("ClassWithParent").getThatPredicate()!!
        assertTrue(pAssignFrom(classWithParent))
        assertFalse(pAssignFrom(classA))

        val pDoc = ClassesRuleBuilder(graph).that().beDocumentedWithKDoc().getThatPredicate()!!
        assertTrue(pDoc(classWithKdoc))
        assertFalse(pDoc(classA))

        val pAnyOf =
            ClassesRuleBuilder(graph).that().anyOf(
                { haveName("ClassA") },
                { haveName("ClassB") },
            ).getThatPredicate()!!
        assertTrue(pAnyOf(classA))

        val pAllOf =
            ClassesRuleBuilder(graph).that().allOf(
                { haveName("ClassA") },
                { resideInAPackage("com.example") },
            ).getThatPredicate()!!
        assertTrue(pAllOf(classA))

        val pNoneOf =
            ClassesRuleBuilder(graph).that().noneOf(
                { haveName("ClassB") },
            ).getThatPredicate()!!
        assertTrue(pNoneOf(classA))
    }

    @Test
    fun `test ClassesShould failure messages`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val v1 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg").getShouldAssertion()!!(classA, listOf(classA), v1)
        assertEquals(1, v1.size)

        val v2 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAPackage(listOf("wrong.pkg")).getShouldAssertion()!!(classA, listOf(classA), v2)
        assertEquals(1, v2.size)

        val v3 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAPackage("wrong.pkg", "other").getShouldAssertion()!!(classA, listOf(classA), v3)
        assertEquals(1, v3.size)

        val v4 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAPackage { false }.getShouldAssertion()!!(classA, listOf(classA), v4)
        assertEquals(1, v4.size)

        val v5 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAPackage("com.example").getShouldAssertion()!!(classA, listOf(classA), v5)
        assertEquals(1, v5.size)

        val v6 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAPackage(listOf("com.example")).getShouldAssertion()!!(classA, listOf(classA), v6)
        assertEquals(1, v6.size)

        val v7 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameEndingWith("Wrong").getShouldAssertion()!!(classA, listOf(classA), v7)
        assertEquals(1, v7.size)

        val v8 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameEndingWith(listOf("Wrong")).getShouldAssertion()!!(classA, listOf(classA), v8)
        assertEquals(1, v8.size)

        val v9 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameStartingWith("Wrong").getShouldAssertion()!!(classA, listOf(classA), v9)
        assertEquals(1, v9.size)

        val v10 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameStartingWith(listOf("Wrong")).getShouldAssertion()!!(classA, listOf(classA), v10)
        assertEquals(1, v10.size)

        val v11 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameMatching("wrong*").getShouldAssertion()!!(classA, listOf(classA), v11)
        assertEquals(1, v11.size)

        val v12 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameMatching(listOf("wrong*")).getShouldAssertion()!!(classA, listOf(classA), v12)
        assertEquals(1, v12.size)

        val v13 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationOf("MissingAnnotation").getShouldAssertion()!!(classA, listOf(classA), v13)
        assertEquals(1, v13.size)

        val v14 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf("MissingAnnotation").getShouldAssertion()!!(classA, listOf(classA), v14)
        assertEquals(1, v14.size)

        val v15 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf("MissingAnnotation").getShouldAssertion()!!(classA, listOf(classA), v15)
        assertEquals(1, v15.size)

        val v16 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInterfaces().getShouldAssertion()!!(classA, listOf(classA), v16)
        assertEquals(1, v16.size)

        val v17 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAbstract().getShouldAssertion()!!(classA, listOf(classA), v17)
        assertEquals(1, v17.size)

        val v18 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beSealed().getShouldAssertion()!!(classA, listOf(classA), v18)
        assertEquals(1, v18.size)

        val v19 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beData().getShouldAssertion()!!(classA, listOf(classA), v19)
        assertEquals(1, v19.size)

        val v20 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInline().getShouldAssertion()!!(classA, listOf(classA), v20)
        assertEquals(1, v20.size)

        val v21 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePublic().getShouldAssertion()!!(classInternal, listOf(classInternal), v21)
        assertEquals(1, v21.size)

        val v22 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInternal().getShouldAssertion()!!(classA, listOf(classA), v22)
        assertEquals(1, v22.size)

        val v23 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePrivate().getShouldAssertion()!!(classA, listOf(classA), v23)
        assertEquals(1, v23.size)

        val v24 = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beProtected().getShouldAssertion()!!(classA, listOf(classA), v24)
        assertEquals(1, v24.size)

        val v25 = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beAssignableTo("com.example.NonExistentParent").getShouldAssertion()!!(classA, listOf(classA), v25)
        assertEquals(1, v25.size)
    }

    @Test
    fun `test ClassesShouldDependencyAssertions access, dependency, and usage assertions`() {
        val accessor =
            ClassDeclaration(
                "Accessor", "com.forbidden.Accessor", "com.forbidden", false, false, emptyList(), emptyList(),
                setOf(
                    "com.example.ClassA",
                ),
                "/src/Accessor.kt",
            )
        val graph =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA, FileDeclaration("Accessor.kt", "com.forbidden", classes = listOf(accessor))))),
                ),
            )

        val vOnlyAccessVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyBeAccessedByAnyPackage("com.allowed")
            .getShouldAssertion()!!(classA, listOf(classA, accessor), vOnlyAccessVararg)
        assertEquals(1, vOnlyAccessVararg.size)

        val vOnlyAccessStr = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyBeAccessedByAnyPackage("com.allowed")
            .getShouldAssertion()!!(classA, listOf(classA, accessor), vOnlyAccessStr)
        assertEquals(1, vOnlyAccessStr.size)

        val vOnlyAccessList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyBeAccessedByAnyPackage(listOf("com.allowed"))
            .getShouldAssertion()!!(classA, listOf(classA, accessor), vOnlyAccessList)
        assertEquals(1, vOnlyAccessList.size)

        val vNotAccessVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeAccessedByAnyPackage("com.forbidden")
            .getShouldAssertion()!!(classA, listOf(classA, accessor), vNotAccessVararg)
        assertEquals(1, vNotAccessVararg.size)

        val vNotAccessStr = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeAccessedByAnyPackage("com.forbidden")
            .getShouldAssertion()!!(classA, listOf(classA, accessor), vNotAccessStr)
        assertEquals(1, vNotAccessStr.size)

        val vNotAccessList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeAccessedByAnyPackage(listOf("com.forbidden"))
            .getShouldAssertion()!!(classA, listOf(classA, accessor), vNotAccessList)
        assertEquals(1, vNotAccessList.size)

        val classWithDep =
            ClassDeclaration(
                "WithDep", "com.example.WithDep", "com.example", false, false, emptyList(),
                listOf(
                    "com.forbidden.Accessor",
                ),
                setOf("com.forbidden.Accessor"), "/src/WithDep.kt",
            )
        val vOnlyDepClassesVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyDependOnClassesInAnyPackage("com.allowed")
            .getShouldAssertion()!!(classWithDep, listOf(classWithDep), vOnlyDepClassesVararg)
        assertEquals(1, vOnlyDepClassesVararg.size)

        val vOnlyDepClassesStr = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyDependOnClassesInAnyPackage("com.allowed")
            .getShouldAssertion()!!(classWithDep, listOf(classWithDep), vOnlyDepClassesStr)
        assertEquals(1, vOnlyDepClassesStr.size)

        val vOnlyDepClassesList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().onlyDependOnClassesInAnyPackage(listOf("com.allowed"))
            .getShouldAssertion()!!(classWithDep, listOf(classWithDep), vOnlyDepClassesList)
        assertEquals(1, vOnlyDepClassesList.size)

        val vNotDepClassesVararg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesInAnyPackage("com.forbidden")
            .getShouldAssertion()!!(classWithDep, listOf(classWithDep), vNotDepClassesVararg)
        assertEquals(1, vNotDepClassesVararg.size)

        val vNotDepClassesStr = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesInAnyPackage("com.forbidden")
            .getShouldAssertion()!!(classWithDep, listOf(classWithDep), vNotDepClassesStr)
        assertEquals(1, vNotDepClassesStr.size)

        val vNotDepClassesList = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesInAnyPackage(listOf("com.forbidden"))
            .getShouldAssertion()!!(classWithDep, listOf(classWithDep), vNotDepClassesList)
        assertEquals(1, vNotDepClassesList.size)

        // Usages: notCall and notReferenceClass
        val usageCall =
            SourceUsage(
                UsageKind.CALL,
                "com.example.Target.foo",
                "Usage.kt",
                1,
                1,
                rawExpression = "Target.foo()",
                enclosingClass = "com.example.ClassWithUsage",
            )
        val usageRef =
            SourceUsage(
                UsageKind.CLASS_REFERENCE,
                "com.example.TargetClass",
                "Usage.kt",
                2,
                1,
                rawExpression = "TargetClass::class",
                enclosingClass = "com.example.ClassWithUsage",
            )
        val classWithUsage =
            ClassDeclaration("ClassWithUsage", "com.example.ClassWithUsage", "com.example", false, false, emptyList(), emptyList(), emptySet(), "/src/Usage.kt")
        val fileUsage =
            FileDeclaration(
                "Usage.kt",
                "com.example",
                classes = listOf(classWithUsage),
                usages = listOf(usageCall, usageRef),
            )
        val graphUsage =
            ProjectGraph(
                mapOf(
                    ":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileUsage))),
                ),
            )

        val vCallStr = mutableListOf<String>()
        ClassesRuleBuilder(graphUsage).should().notCall("com.example.Target.foo")
            .getShouldAssertion()!!(classWithUsage, listOf(classWithUsage), vCallStr)
        assertEquals(1, vCallStr.size)

        val vCallKClass = mutableListOf<String>()
        ClassesRuleBuilder(graphUsage).should().notCall(String::class)
            .getShouldAssertion()!!(classWithUsage, listOf(classWithUsage), vCallKClass)
        assertTrue(vCallKClass.isEmpty())

        val vCallReified = mutableListOf<String>()
        ClassesRuleBuilder(graphUsage).should().notCall<String>()
            .getShouldAssertion()!!(classWithUsage, listOf(classWithUsage), vCallReified)
        assertTrue(vCallReified.isEmpty())

        val vRefStr = mutableListOf<String>()
        ClassesRuleBuilder(graphUsage).should().notReferenceClass("com.example.TargetClass")
            .getShouldAssertion()!!(classWithUsage, listOf(classWithUsage), vRefStr)
        assertEquals(1, vRefStr.size)

        val vRefKClass = mutableListOf<String>()
        ClassesRuleBuilder(graphUsage).should().notReferenceClass(String::class)
            .getShouldAssertion()!!(classWithUsage, listOf(classWithUsage), vRefKClass)
        assertTrue(vRefKClass.isEmpty())

        val vRefReified = mutableListOf<String>()
        ClassesRuleBuilder(graphUsage).should().notReferenceClass<String>()
            .getShouldAssertion()!!(classWithUsage, listOf(classWithUsage), vRefReified)
        assertTrue(vRefReified.isEmpty())
    }
}
