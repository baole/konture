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

internal class ClassesShouldTest : KontureScopeTestFixture() {
    @Test
    fun `test classes rule builder assertions anyOf - allOf - noneOf`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        // satisfy predicate
        val builderSatisfy = ClassesRuleBuilder(graph)
        builderSatisfy.should().satisfy { it.name == "ClassA" }
        val assertSatisfy = builderSatisfy.getShouldAssertion()!!
        val vSat1 = mutableListOf<String>()
        assertSatisfy(classA, listOf(classA), vSat1)
        assertTrue(vSat1.isEmpty())

        val vSat2 = mutableListOf<String>()
        assertSatisfy(classB, listOf(classB), vSat2)
        assertEquals(1, vSat2.size)

        // satisfy with custom description
        val builderSatisfyDesc = ClassesRuleBuilder(graph)
        builderSatisfyDesc.should().satisfy("have A in name") { it.name.contains("A") }
        val assertSatisfyDesc = builderSatisfyDesc.getShouldAssertion()!!
        val vSatDesc = mutableListOf<String>()
        assertSatisfyDesc(classB, listOf(classB), vSatDesc)
        assertEquals(1, vSatDesc.size)
        assertTrue(vSatDesc[0].contains("have A in name"))

        // satisfy with custom violations builder
        val builderSatisfyCustom = ClassesRuleBuilder(graph)
        builderSatisfyCustom.should().satisfy { cls, violations ->
            if (!cls.name.contains("A")) {
                violations.add("Custom failure for ${cls.name}")
            }
        }
        val assertSatisfyCustom = builderSatisfyCustom.getShouldAssertion()!!
        val vSatCustom = mutableListOf<String>()
        assertSatisfyCustom(classB, listOf(classB), vSatCustom)
        assertEquals(1, vSatCustom.size)
        assertEquals("Custom failure for ClassB", vSatCustom[0])

        // anyOf
        val builderAnyOf = ClassesRuleBuilder(graph)
        builderAnyOf.should().anyOf(
            { resideInAPackage("com.example") },
            { haveName("ClassB") },
        )
        val assertAnyOf = builderAnyOf.getShouldAssertion()!!
        val vAny1 = mutableListOf<String>()
        assertAnyOf(classA, listOf(classA), vAny1)
        assertTrue(vAny1.isEmpty())

        val vAny2 = mutableListOf<String>()
        assertAnyOf(classC, listOf(classC), vAny2)
        assertEquals(1, vAny2.size)

        // allOf
        val builderAllOf = ClassesRuleBuilder(graph)
        builderAllOf.should().allOf(
            { resideInAPackage("com.example") },
            { haveName("ClassA") },
        )
        val assertAllOf = builderAllOf.getShouldAssertion()!!
        val vAll1 = mutableListOf<String>()
        assertAllOf(classA, listOf(classA), vAll1)
        assertTrue(vAll1.isEmpty())

        val vAll2 = mutableListOf<String>()
        assertAllOf(classB, listOf(classB), vAll2)
        assertEquals(1, vAll2.size)

        // noneOf
        val builderNoneOf = ClassesRuleBuilder(graph)
        builderNoneOf.should().noneOf(
            { resideInAPackage("com.other") },
            { haveName("ClassB") },
        )
        val assertNoneOf = builderNoneOf.getShouldAssertion()!!
        val vNone1 = mutableListOf<String>()
        assertNoneOf(classA, listOf(classA), vNone1)
        assertTrue(vNone1.isEmpty())

        val vNone2 = mutableListOf<String>()
        assertNoneOf(classB, listOf(classB), vNone2)
        assertEquals(1, vNone2.size)
    }

    @Test
    fun `test ClassesShould additions`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertNotAnnotSingle =
            ClassesRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf("com.example.MyAnnotation").getShouldAssertion()!!
        val vNotAnnotSingle = mutableListOf<String>()
        assertNotAnnotSingle(classA, listOf(classA), vNotAnnotSingle)
        assertTrue(vNotAnnotSingle.isEmpty())

        val assertNotAnnotList =
            ClassesRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf(listOf("com.example.MyAnnotation")).getShouldAssertion()!!
        val vNotAnnotList = mutableListOf<String>()
        assertNotAnnotList(classA, listOf(classA), vNotAnnotList)
        assertTrue(vNotAnnotList.isEmpty())

        val assertNotAnnotVararg =
            ClassesRuleBuilder(
                graph,
            ).should().notHaveAnnotationOf("com.example.MyAnnotation", "Other").getShouldAssertion()!!
        val vNotAnnotVararg = mutableListOf<String>()
        assertNotAnnotVararg(classA, listOf(classA), vNotAnnotVararg)
        assertTrue(vNotAnnotVararg.isEmpty())

        val assertNotInterfaces = ClassesRuleBuilder(graph).should().notBeInterfaces().getShouldAssertion()!!
        val vNotInterfaces = mutableListOf<String>()
        assertNotInterfaces(classA, listOf(classA), vNotInterfaces)
        assertTrue(vNotInterfaces.isEmpty())

        val assertNotAbstract = ClassesRuleBuilder(graph).should().notBeAbstract().getShouldAssertion()!!
        val vNotAbstract = mutableListOf<String>()
        assertNotAbstract(classA, listOf(classA), vNotAbstract)
        assertTrue(vNotAbstract.isEmpty())

        val assertNotSealed = ClassesRuleBuilder(graph).should().notBeSealed().getShouldAssertion()!!
        val vNotSealed = mutableListOf<String>()
        assertNotSealed(classA, listOf(classA), vNotSealed)
        assertTrue(vNotSealed.isEmpty())

        val assertNotData = ClassesRuleBuilder(graph).should().notBeData().getShouldAssertion()!!
        val vNotData = mutableListOf<String>()
        assertNotData(classA, listOf(classA), vNotData)
        assertTrue(vNotData.isEmpty())

        val assertNotValue = ClassesRuleBuilder(graph).should().notBeValue().getShouldAssertion()!!
        val vNotValue = mutableListOf<String>()
        assertNotValue(classA, listOf(classA), vNotValue)
        assertTrue(vNotValue.isEmpty())

        val assertNotInline = ClassesRuleBuilder(graph).should().notBeInline().getShouldAssertion()!!
        val vNotInline = mutableListOf<String>()
        assertNotInline(classA, listOf(classA), vNotInline)
        assertTrue(vNotInline.isEmpty())

        val assertNotInner = ClassesRuleBuilder(graph).should().notBeInner().getShouldAssertion()!!
        val vNotInner = mutableListOf<String>()
        assertNotInner(classA, listOf(classA), vNotInner)
        assertTrue(vNotInner.isEmpty())

        val assertNotOpen = ClassesRuleBuilder(graph).should().notBeOpen().getShouldAssertion()!!
        val vNotOpen = mutableListOf<String>()
        assertNotOpen(classA, listOf(classA), vNotOpen)
        assertTrue(vNotOpen.isEmpty())

        val assertNotFinal = ClassesRuleBuilder(graph).should().notBeFinal().getShouldAssertion()!!
        val vNotFinal = mutableListOf<String>()
        assertNotFinal(classOpen, listOf(classOpen), vNotFinal)
        assertTrue(vNotFinal.isEmpty())
    }

    @Test
    fun `test additional classes modifiers should`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertNotVisSingle =
            ClassesRuleBuilder(
                graph,
            ).should().notHaveVisibility(Visibility.PRIVATE).getShouldAssertion()!!
        val vNotVisSingle = mutableListOf<String>()
        assertNotVisSingle(classA, listOf(classA), vNotVisSingle)
        assertTrue(vNotVisSingle.isEmpty())

        val assertNotVisList =
            ClassesRuleBuilder(
                graph,
            ).should().notHaveVisibility(listOf(Visibility.PRIVATE)).getShouldAssertion()!!
        val vNotVisList = mutableListOf<String>()
        assertNotVisList(classA, listOf(classA), vNotVisList)
        assertTrue(vNotVisList.isEmpty())

        val assertNotVisVararg =
            ClassesRuleBuilder(
                graph,
            ).should().notHaveVisibility(Visibility.PRIVATE, Visibility.INTERNAL).getShouldAssertion()!!
        val vNotVisVararg = mutableListOf<String>()
        assertNotVisVararg(classA, listOf(classA), vNotVisVararg)
        assertTrue(vNotVisVararg.isEmpty())

        val assertNotModifierSingle =
            ClassesRuleBuilder(
                graph,
            ).should().notHaveModifier(Modifier.DATA).getShouldAssertion()!!
        val vNotModifierSingle = mutableListOf<String>()
        assertNotModifierSingle(classA, listOf(classA), vNotModifierSingle)
        assertTrue(vNotModifierSingle.isEmpty())
    }

    @Test
    fun `test class dependencies and access constraints`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertOnlyAccessVararg =
            ClassesRuleBuilder(
                graph,
            ).should().onlyBeAccessedByAnyPackage("com.example").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertOnlyAccessVararg(classA, listOf(classA), v1)
        assertTrue(v1.isEmpty())

        val assertOnlyAccessStr =
            ClassesRuleBuilder(
                graph,
            ).should().onlyBeAccessedByAnyPackage("com.example").getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertOnlyAccessStr(classA, listOf(classA), v2)
        assertTrue(v2.isEmpty())

        val assertOnlyAccessList =
            ClassesRuleBuilder(
                graph,
            ).should().onlyBeAccessedByAnyPackage(listOf("com.example")).getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertOnlyAccessList(classA, listOf(classA), v3)
        assertTrue(v3.isEmpty())

        val assertNotAccessVararg =
            ClassesRuleBuilder(
                graph,
            ).should().notBeAccessedByAnyPackage("com.forbidden").getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotAccessVararg(classA, listOf(classA), v4)
        assertTrue(v4.isEmpty())

        val assertNotAccessStr =
            ClassesRuleBuilder(
                graph,
            ).should().notBeAccessedByAnyPackage("com.forbidden").getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNotAccessStr(classA, listOf(classA), v5)
        assertTrue(v5.isEmpty())

        val assertNotAccessList =
            ClassesRuleBuilder(
                graph,
            ).should().notBeAccessedByAnyPackage(listOf("com.forbidden")).getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotAccessList(classA, listOf(classA), v6)
        assertTrue(v6.isEmpty())
    }

    @Test
    fun `test notHaveSignaturesWithTypesAnnotatedWith`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val assertNoSigLeakStr =
            ClassesRuleBuilder(
                graph,
            ).should().notHaveSignaturesWithTypesAnnotatedWith("com.example.MyAnnotation").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertNoSigLeakStr(classA, listOf(classA), v1)
        assertTrue(v1.isEmpty())
    }

    @Test
    fun `test classes rule builder multi-parameter rules should`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )

        val classMulti =
            ClassDeclaration(
                name = "MultiClass",
                fqName = "com.example.MultiClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MultiClass.kt",
                visibility = Visibility.PUBLIC,
            )

        val assertVis =
            ClassesRuleBuilder(
                graph,
            ).should().haveVisibility(Visibility.PUBLIC).getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertVis(classMulti, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertAnyVis =
            ClassesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL).getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertAnyVis(classMulti, emptyList(), v2)
        assertTrue(v2.isEmpty())
    }
}
