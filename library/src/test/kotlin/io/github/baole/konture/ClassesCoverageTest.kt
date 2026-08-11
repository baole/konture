/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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

        val vPkgPred = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().resideInAPackage { false }.getShouldAssertion()!!(classA, listOf(classA), vPkgPred)
        assertEquals(1, vPkgPred.size)

        val vNotPkgSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAPackage("com.example").getShouldAssertion()!!(classA, listOf(classA), vNotPkgSingle)
        assertEquals(1, vNotPkgSingle.size)

        val vNotPkgList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAPackage(listOf("com.example")).getShouldAssertion()!!(classA, listOf(classA), vNotPkgList)
        assertEquals(1, vNotPkgList.size)

        val vNotPkgVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAPackage("com.example", "other").getShouldAssertion()!!(classA, listOf(classA), vNotPkgVararg)
        assertEquals(1, vNotPkgVararg.size)

        val vModuleSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAModule(":other").getShouldAssertion()!!(classA, listOf(classA), vModuleSingle)
        assertEquals(1, vModuleSingle.size)

        val vModuleList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAModule(listOf(":other")).getShouldAssertion()!!(classA, listOf(classA), vModuleList)
        assertEquals(1, vModuleList.size)

        val vModuleVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().resideInAModule(":other", ":app").getShouldAssertion()!!(classA, listOf(classA), vModuleVararg)
        assertTrue(vModuleVararg.isEmpty())

        val vNotModuleSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAModule(":app").getShouldAssertion()!!(classA, listOf(classA), vNotModuleSingle)
        assertEquals(1, vNotModuleSingle.size)

        val vNotModuleList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAModule(listOf(":app")).getShouldAssertion()!!(classA, listOf(classA), vNotModuleList)
        assertEquals(1, vNotModuleList.size)

        val vNotModuleVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notResideInAModule(":app", ":other").getShouldAssertion()!!(classA, listOf(classA), vNotModuleVararg)
        assertEquals(1, vNotModuleVararg.size)

        val vNameSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveName("ClassB").getShouldAssertion()!!(classA, listOf(classA), vNameSingle)
        assertEquals(1, vNameSingle.size)

        val vNameList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveName(listOf("ClassB")).getShouldAssertion()!!(classA, listOf(classA), vNameList)
        assertEquals(1, vNameList.size)

        val vNameVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveName("ClassB", "ClassC").getShouldAssertion()!!(classA, listOf(classA), vNameVararg)
        assertEquals(1, vNameVararg.size)

        val vNamePred = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveName { false }.getShouldAssertion()!!(classA, listOf(classA), vNamePred)
        assertEquals(1, vNamePred.size)

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
        ).should().notHaveName("ClassA", "ClassB").getShouldAssertion()!!(classA, listOf(classA), vNotNameVararg)
        assertEquals(1, vNotNameVararg.size)

        val vEndSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameEndingWith("B").getShouldAssertion()!!(classA, listOf(classA), vEndSingle)
        assertEquals(1, vEndSingle.size)

        val vEndList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameEndingWith(listOf("B")).getShouldAssertion()!!(classA, listOf(classA), vEndList)
        assertEquals(1, vEndList.size)

        val vEndVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameEndingWith("B", "C").getShouldAssertion()!!(classA, listOf(classA), vEndVararg)
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
        ).should().notHaveNameEndingWith("A", "B").getShouldAssertion()!!(classA, listOf(classA), vNotEndVararg)
        assertEquals(1, vNotEndVararg.size)

        val vStartSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameStartingWith("B").getShouldAssertion()!!(classA, listOf(classA), vStartSingle)
        assertEquals(1, vStartSingle.size)

        val vStartList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameStartingWith(listOf("B")).getShouldAssertion()!!(classA, listOf(classA), vStartList)
        assertEquals(1, vStartList.size)

        val vStartVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveNameStartingWith("B", "C").getShouldAssertion()!!(classA, listOf(classA), vStartVararg)
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

    @Test
    fun `test ClassesShouldMetadataAssertions annotations, modifiers, types, and structure`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val vAnnotSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationOf("Other").getShouldAssertion()!!(classA, listOf(classA), vAnnotSingle)
        assertEquals(1, vAnnotSingle.size)

        val vAnnotList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationOf(listOf("Other")).getShouldAssertion()!!(classA, listOf(classA), vAnnotList)
        assertEquals(1, vAnnotList.size)

        val vAnnotVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationOf("Other", "X").getShouldAssertion()!!(classA, listOf(classA), vAnnotVararg)
        assertEquals(1, vAnnotVararg.size)

        val vNotAnnotSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveAnnotationOf("MyAnnotation").getShouldAssertion()!!(
            classAnnotated,
            listOf(classAnnotated),
            vNotAnnotSingle,
        )
        assertEquals(1, vNotAnnotSingle.size)

        val vNotAnnotList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveAnnotationOf(listOf("MyAnnotation")).getShouldAssertion()!!(
            classAnnotated,
            listOf(classAnnotated),
            vNotAnnotList,
        )
        assertEquals(1, vNotAnnotList.size)

        val vNotAnnotVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveAnnotationOf("MyAnnotation", "Other").getShouldAssertion()!!(
            classAnnotated,
            listOf(classAnnotated),
            vNotAnnotVararg,
        )
        assertEquals(1, vNotAnnotVararg.size)

        val vAllAnnotSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf("Other").getShouldAssertion()!!(classA, listOf(classA), vAllAnnotSingle)
        assertEquals(1, vAllAnnotSingle.size)

        val vAllAnnotList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf(listOf("Other")).getShouldAssertion()!!(classA, listOf(classA), vAllAnnotList)
        assertEquals(1, vAllAnnotList.size)

        val vAllAnnotVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAllAnnotationsOf("Other", "X").getShouldAssertion()!!(classA, listOf(classA), vAllAnnotVararg)
        assertEquals(1, vAllAnnotVararg.size)

        val vAnyAnnotSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf("Other").getShouldAssertion()!!(classA, listOf(classA), vAnyAnnotSingle)
        assertEquals(1, vAnyAnnotSingle.size)

        val vAnyAnnotList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf(listOf("Other")).getShouldAssertion()!!(classA, listOf(classA), vAnyAnnotList)
        assertEquals(1, vAnyAnnotList.size)

        val vAnyAnnotVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyAnnotationOf("Other", "X").getShouldAssertion()!!(classA, listOf(classA), vAnyAnnotVararg)
        assertEquals(1, vAnyAnnotVararg.size)

        val vAnnotArg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnnotationWithArgument("MyAnnotation", "value", "wrong").getShouldAssertion()!!(
            classAnnotated,
            listOf(classAnnotated),
            vAnnotArg,
        )
        assertEquals(1, vAnnotArg.size)

        val vInterface = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInterfaces().getShouldAssertion()!!(classA, listOf(classA), vInterface)
        assertEquals(1, vInterface.size)

        val vNotInterface = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeInterfaces().getShouldAssertion()!!(classInterface, listOf(classInterface), vNotInterface)
        assertEquals(1, vNotInterface.size)

        val vAbstract = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beAbstract().getShouldAssertion()!!(classA, listOf(classA), vAbstract)
        assertEquals(1, vAbstract.size)

        val vNotAbstract = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeAbstract().getShouldAssertion()!!(classAbstract, listOf(classAbstract), vNotAbstract)
        assertEquals(1, vNotAbstract.size)

        val vSealed = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beSealed().getShouldAssertion()!!(classA, listOf(classA), vSealed)
        assertEquals(1, vSealed.size)

        val vNotSealed = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeSealed().getShouldAssertion()!!(classSealed, listOf(classSealed), vNotSealed)
        assertEquals(1, vNotSealed.size)

        val vData = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beData().getShouldAssertion()!!(classA, listOf(classA), vData)
        assertEquals(1, vData.size)

        val vNotData = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeData().getShouldAssertion()!!(classData, listOf(classData), vNotData)
        assertEquals(1, vNotData.size)

        val vValue = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beValue().getShouldAssertion()!!(classA, listOf(classA), vValue)
        assertEquals(1, vValue.size)

        val vNotValue = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeValue().getShouldAssertion()!!(classInline, listOf(classInline), vNotValue)
        assertEquals(1, vNotValue.size)

        val vInline = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInline().getShouldAssertion()!!(classA, listOf(classA), vInline)
        assertEquals(1, vInline.size)

        val vNotInline = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeInline().getShouldAssertion()!!(classInline, listOf(classInline), vNotInline)
        assertEquals(1, vNotInline.size)

        val vInner = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInner().getShouldAssertion()!!(classA, listOf(classA), vInner)
        assertEquals(1, vInner.size)

        val vNotInner = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeInner().getShouldAssertion()!!(classInner, listOf(classInner), vNotInner)
        assertEquals(1, vNotInner.size)

        val vOpen = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beOpen().getShouldAssertion()!!(classA, listOf(classA), vOpen)
        assertEquals(1, vOpen.size)

        val vNotOpen = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeOpen().getShouldAssertion()!!(classOpen, listOf(classOpen), vNotOpen)
        assertEquals(1, vNotOpen.size)

        val vFinal = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beFinal().getShouldAssertion()!!(classOpen, listOf(classOpen), vFinal)
        assertEquals(1, vFinal.size)

        val vNotFinal = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBeFinal().getShouldAssertion()!!(classA, listOf(classA), vNotFinal)
        assertEquals(1, vNotFinal.size)

        val vVisSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveVisibility(Visibility.PRIVATE).getShouldAssertion()!!(classA, listOf(classA), vVisSingle)
        assertEquals(1, vVisSingle.size)

        val vVisList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyVisibility(listOf(Visibility.PRIVATE)).getShouldAssertion()!!(classA, listOf(classA), vVisList)
        assertEquals(1, vVisList.size)

        val vVisVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyVisibility(Visibility.PRIVATE, Visibility.INTERNAL).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vVisVararg,
        )
        assertEquals(1, vVisVararg.size)

        val vNotVisSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveVisibility(Visibility.PUBLIC).getShouldAssertion()!!(classA, listOf(classA), vNotVisSingle)
        assertEquals(1, vNotVisSingle.size)

        val vNotVisList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveVisibility(listOf(Visibility.PUBLIC)).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vNotVisList,
        )
        assertEquals(1, vNotVisList.size)

        val vNotVisVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveVisibility(Visibility.PUBLIC, Visibility.INTERNAL).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vNotVisVararg,
        )
        assertEquals(1, vNotVisVararg.size)

        val vPub = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePublic().getShouldAssertion()!!(classInternal, listOf(classInternal), vPub)
        assertEquals(1, vPub.size)

        val vNotPub = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notBePublic().getShouldAssertion()!!(classA, listOf(classA), vNotPub)
        assertEquals(1, vNotPub.size)

        val vInternal = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInternal().getShouldAssertion()!!(classA, listOf(classA), vInternal)
        assertEquals(1, vInternal.size)

        val vNotInternal = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeInternal().getShouldAssertion()!!(classInternal, listOf(classInternal), vNotInternal)
        assertEquals(1, vNotInternal.size)

        val vPrivate = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePrivate().getShouldAssertion()!!(classA, listOf(classA), vPrivate)
        assertEquals(1, vPrivate.size)

        val vNotPrivate = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBePrivate().getShouldAssertion()!!(classPrivate, listOf(classPrivate), vNotPrivate)
        assertEquals(1, vNotPrivate.size)

        val vProtected = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beProtected().getShouldAssertion()!!(classA, listOf(classA), vProtected)
        assertEquals(1, vProtected.size)

        val vNotProtected = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeProtected().getShouldAssertion()!!(classProtected, listOf(classProtected), vNotProtected)
        assertEquals(1, vNotProtected.size)

        val vModifierSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveModifier(Modifier.DATA).getShouldAssertion()!!(classA, listOf(classA), vModifierSingle)
        assertEquals(1, vModifierSingle.size)

        val vAnyModifierList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyModifier(listOf(Modifier.DATA)).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vAnyModifierList,
        )
        assertEquals(1, vAnyModifierList.size)

        val vAnyModifierVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyModifier(Modifier.DATA, Modifier.SEALED).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vAnyModifierVararg,
        )
        assertEquals(1, vAnyModifierVararg.size)

        val vAllModifiersList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAllModifiers(listOf(Modifier.DATA)).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vAllModifiersList,
        )
        assertEquals(1, vAllModifiersList.size)

        val vAllModifiersVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAllModifiers(Modifier.DATA).getShouldAssertion()!!(classA, listOf(classA), vAllModifiersVararg)
        assertEquals(1, vAllModifiersVararg.size)

        val vNotModifierSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveModifier(Modifier.DATA).getShouldAssertion()!!(classData, listOf(classData), vNotModifierSingle)
        assertEquals(1, vNotModifierSingle.size)

        val vNotModifierList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveModifier(listOf(Modifier.DATA)).getShouldAssertion()!!(
            classData,
            listOf(classData),
            vNotModifierList,
        )
        assertEquals(1, vNotModifierList.size)

        val vNotModifierVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notHaveModifier(Modifier.DATA, Modifier.SEALED).getShouldAssertion()!!(
            classData,
            listOf(classData),
            vNotModifierVararg,
        )
        assertEquals(1, vNotModifierVararg.size)

        val vAssignSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beAssignableTo("com.example.Other").getShouldAssertion()!!(classA, listOf(classA), vAssignSingle)
        assertEquals(1, vAssignSingle.size)

        val vAssignAnyList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beAssignableToAnyOf(listOf("com.example.Other")).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vAssignAnyList,
        )
        assertEquals(1, vAssignAnyList.size)

        val vAssignAnyVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beAssignableToAnyOf("com.example.Other", "X").getShouldAssertion()!!(
            classA,
            listOf(classA),
            vAssignAnyVararg,
        )
        assertEquals(1, vAssignAnyVararg.size)

        val vAssignAllList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beAssignableToAllOf(listOf("com.example.Other")).getShouldAssertion()!!(
            classA,
            listOf(classA),
            vAssignAllList,
        )
        assertEquals(1, vAssignAllList.size)

        val vAssignAllVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beAssignableToAllOf("com.example.Other", "X").getShouldAssertion()!!(
            classA,
            listOf(classA),
            vAssignAllVararg,
        )
        assertEquals(1, vAssignAllVararg.size)

        val vChildSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beChildOf("com.example.Other").getShouldAssertion()!!(classA, listOf(classA), vChildSingle)
        assertEquals(1, vChildSingle.size)

        val vNotAssignSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeAssignableTo("com.example.ParentType").getShouldAssertion()!!(
            classWithParent,
            listOf(classWithParent),
            vNotAssignSingle,
        )
        assertEquals(1, vNotAssignSingle.size)

        val vAssignFromSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beAssignableFrom("com.example.ClassA").getShouldAssertion()!!(
            classWithParent,
            listOf(classWithParent),
            vAssignFromSingle,
        )
        assertEquals(1, vAssignFromSingle.size)

        val vNotAssignFromSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notBeAssignableFrom("ClassWithParent").getShouldAssertion()!!(
            classWithParent,
            listOf(classWithParent),
            vNotAssignFromSingle,
        )
        assertEquals(1, vNotAssignFromSingle.size)

        val vCompanion = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveCompanionObject().getShouldAssertion()!!(classA, listOf(classA), vCompanion)
        assertEquals(1, vCompanion.size)

        val vNoArg = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().haveNoArgConstructor().getShouldAssertion()!!(classA, listOf(classA), vNoArg)
        assertEquals(1, vNoArg.size)

        val vPrivPrimary = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().havePrivatePrimaryConstructor().getShouldAssertion()!!(classA, listOf(classA), vPrivPrimary)
        assertEquals(1, vPrivPrimary.size)

        val vDoc = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beDocumentedWithKDoc().getShouldAssertion()!!(classA, listOf(classA), vDoc)
        assertEquals(1, vDoc.size)

        val classWithPropFunc =
            ClassDeclaration(
                "ClassWithPF", "com.example.ClassWithPF", "com.example", false, false,
                properties = listOf(PropertyDeclaration("prop1", "String")),
                functions = listOf(FunctionDeclaration("func1", "Unit")),
                filePath = "/src/PF.kt",
            )

        val vContainPropStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperty("p2").getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainPropStr)
        assertEquals(1, vContainPropStr.size)

        val vContainPropList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperty(listOf("p2")).getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vContainPropList,
        )
        assertEquals(1, vContainPropList.size)

        val vContainPropVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperty("p1", "p2").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vContainPropVararg,
        )
        assertEquals(1, vContainPropVararg.size)

        val vContainPropsList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperties(listOf("p1", "p2")).getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vContainPropsList,
        )
        assertEquals(1, vContainPropsList.size)

        val vContainPropsVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperties("p1", "p2").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vContainPropsVararg,
        )
        assertEquals(1, vContainPropsVararg.size)

        val vNotContainPropStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperty("prop1").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainPropStr,
        )
        assertEquals(1, vNotContainPropStr.size)

        val vNotContainPropList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperty(listOf("prop1")).getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainPropList,
        )
        assertEquals(1, vNotContainPropList.size)

        val vNotContainPropVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperty("prop1", "p2").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainPropVararg,
        )
        assertEquals(1, vNotContainPropVararg.size)

        val vNotContainPropsList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperties(listOf("prop1")).getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainPropsList,
        )
        assertEquals(1, vNotContainPropsList.size)

        val vNotContainPropsVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainProperties("prop1", "p2").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainPropsVararg,
        )
        assertEquals(1, vNotContainPropsVararg.size)

        val vContainFuncStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunction("f2").getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainFuncStr)
        assertEquals(1, vContainFuncStr.size)

        val vContainFuncList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunction(listOf("f2")).getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vContainFuncList,
        )
        assertEquals(1, vContainFuncList.size)

        val vContainFuncVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunction("func1", "f2").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vContainFuncVararg,
        )
        assertEquals(1, vContainFuncVararg.size)

        val vContainFuncsList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunctions(listOf("func1", "f2")).getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vContainFuncsList,
        )
        assertEquals(1, vContainFuncsList.size)

        val vContainFuncsVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containFunctions("func1", "f2").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vContainFuncsVararg,
        )
        assertEquals(1, vContainFuncsVararg.size)

        val vNotContainFuncStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunction("func1").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainFuncStr,
        )
        assertEquals(1, vNotContainFuncStr.size)

        val vNotContainFuncList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunction(listOf("func1")).getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainFuncList,
        )
        assertEquals(1, vNotContainFuncList.size)

        val vNotContainFuncVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunction("func1", "f2").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainFuncVararg,
        )
        assertEquals(1, vNotContainFuncVararg.size)

        val vNotContainFuncsList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunctions(listOf("func1")).getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainFuncsList,
        )
        assertEquals(1, vNotContainFuncsList.size)

        val vNotContainFuncsVararg = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().notContainFunctions("func1", "f2").getShouldAssertion()!!(
            classWithPropFunc,
            listOf(classWithPropFunc),
            vNotContainFuncsVararg,
        )
        assertEquals(1, vNotContainFuncsVararg.size)
    }
}
