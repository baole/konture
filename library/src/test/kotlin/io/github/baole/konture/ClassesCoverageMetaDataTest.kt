/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ClassesCoverageMetaDataTest : KontureScopeTestFixture() {
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
        ClassesRuleBuilder(
            graph,
        ).should().notBeInner().getShouldAssertion()!!(classA, listOf(classA), vNotInner)
        assertEquals(0, vNotInner.size)

        val vOpen = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beOpen().getShouldAssertion()!!(classA, listOf(classA), vOpen)
        assertEquals(1, vOpen.size)

        val vVisSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveVisibility(Visibility.PRIVATE).getShouldAssertion()!!(classA, listOf(classA), vVisSingle)
        assertEquals(1, vVisSingle.size)

        val vVisList = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().haveAnyVisibility(
            listOf(Visibility.PRIVATE),
        ).getShouldAssertion()!!(classA, listOf(classA), vVisList)
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

        val vPub = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePublic().getShouldAssertion()!!(classInternal, listOf(classInternal), vPub)
        assertEquals(1, vPub.size)

        val vInternal = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beInternal().getShouldAssertion()!!(classA, listOf(classA), vInternal)
        assertEquals(1, vInternal.size)

        val vPrivate = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().bePrivate().getShouldAssertion()!!(classA, listOf(classA), vPrivate)
        assertEquals(1, vPrivate.size)

        val vProtected = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beProtected().getShouldAssertion()!!(classA, listOf(classA), vProtected)
        assertEquals(1, vProtected.size)

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
        ).should().notHaveModifier(
            Modifier.DATA,
        ).getShouldAssertion()!!(classData, listOf(classData), vNotModifierSingle)
        assertEquals(1, vNotModifierSingle.size)

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

        val vAssignFromSingle = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().beAssignableFrom("com.example.ClassA").getShouldAssertion()!!(
            classWithParent,
            listOf(classWithParent),
            vAssignFromSingle,
        )
        assertEquals(1, vAssignFromSingle.size)

        val vDoc = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beDocumentedWithKDoc().getShouldAssertion()!!(classA, listOf(classA), vDoc)
        assertEquals(1, vDoc.size)

        val classWithPropFunc =
            ClassDeclaration(
                name = "ClassWithPF",
                fqName = "com.example.ClassWithPF",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                properties =
                    listOf(
                        PropertyDeclaration(
                            name = "prop1",
                            visibility = Visibility.PUBLIC,
                            modifiers = emptySet(),
                            type = "String",
                            isVal = true,
                            annotations = emptyList(),
                            kdocText = null,
                            isExtension = false,
                        ),
                    ),
                functions =
                    listOf(
                        FunctionDeclaration(
                            name = "func1",
                            visibility = Visibility.PUBLIC,
                            modifiers = emptySet(),
                            returnType = "Unit",
                            parameters = emptyList(),
                            annotations = emptyList(),
                            kdocText = null,
                            isExtension = false,
                        ),
                    ),
                filePath = "/src/PF.kt",
            )

        val vContainPropStr = mutableListOf<String>()
        ClassesRuleBuilder(
            graph,
        ).should().containProperty(
            "p2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainPropStr)
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
        ).should().containFunction(
            "f2",
        ).getShouldAssertion()!!(classWithPropFunc, listOf(classWithPropFunc), vContainFuncStr)
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
