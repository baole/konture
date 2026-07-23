/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PropertiesRuleBuilderMultiParamTest : RuleBuildersTestBase() {
    @Test
    fun `test properties rule builder multi-parameter rules`() {
        val pMulti =
            PropertyDeclaration(
                name = "multiProp",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.CONST),
                type = "kotlin.Double",
                isVal = true,
                annotations =
                    listOf(
                        AnnotationDeclaration("A1", "com.example.A1"),
                        AnnotationDeclaration("A2", "com.example.A2"),
                    ),
                kdocText = null,
            )

        val graph = ProjectGraph(emptyMap())
        val context = PropertyDeclarationContext(pMulti, "com.example", null, ":app", "/src/MyService.kt")

        // 1. haveAllAnnotationsOf & haveAnyAnnotationOf in PropertiesThat
        val ruleAllAnno = PropertiesRuleBuilder(graph).that().haveAllAnnotationsOf("A1", "A2")
        assertTrue(ruleAllAnno.getThatPredicate()!!(context))

        val ruleAnyAnno = PropertiesRuleBuilder(graph).that().haveAnyAnnotationOf("A2", "A3")
        assertTrue(ruleAnyAnno.getThatPredicate()!!(context))

        val ruleNotAnno = PropertiesRuleBuilder(graph).that().haveAllAnnotationsOf("A1", "A3")
        assertFalse(ruleNotAnno.getThatPredicate()!!(context))

        // 2. haveAllModifiers & haveAnyModifier in PropertiesThat
        val ruleAllMod = PropertiesRuleBuilder(graph).that().haveAllModifiers(Modifier.CONST)
        assertTrue(ruleAllMod.getThatPredicate()!!(context))

        val ruleAnyMod = PropertiesRuleBuilder(graph).that().haveAnyModifier(Modifier.CONST, Modifier.LATEINIT)
        assertTrue(ruleAnyMod.getThatPredicate()!!(context))

        val ruleNotMod = PropertiesRuleBuilder(graph).that().haveAllModifiers(Modifier.CONST, Modifier.LATEINIT)
        assertFalse(ruleNotMod.getThatPredicate()!!(context))

        // 3. haveAnyVisibility in PropertiesThat
        val ruleAnyVis = PropertiesRuleBuilder(graph).that().haveAnyVisibility(Visibility.INTERNAL, Visibility.PRIVATE)
        assertTrue(ruleAnyVis.getThatPredicate()!!(context))

        // 4. haveType in PropertiesThat
        val ruleAnyType = PropertiesRuleBuilder(graph).that().haveType("kotlin.Double", "kotlin.Int")
        assertTrue(ruleAnyType.getThatPredicate()!!(context))

        val ruleNotType = PropertiesRuleBuilder(graph).that().haveType("kotlin.String", "kotlin.Int")
        assertFalse(ruleNotType.getThatPredicate()!!(context))

        // 5. Assertions in PropertiesShould
        val assertAllAnno =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf("A1", "A2").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAllAnno(context, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertNotAnno =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf("A1", "A3").getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNotAnno(context, emptyList(), v2)
        assertEquals(1, v2.size)

        val assertAnyVis =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(Visibility.INTERNAL).getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertAnyVis(context, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val assertNotVis =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(Visibility.PUBLIC).getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotVis(context, emptyList(), v4)
        assertEquals(1, v4.size)

        val assertAnyType =
            PropertiesRuleBuilder(
                graph,
            ).should().haveType("kotlin.Double", "kotlin.Int").getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertAnyType(context, emptyList(), v5)
        assertTrue(v5.isEmpty())

        val assertNotType = PropertiesRuleBuilder(graph).should().haveType("kotlin.String").getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertNotType(context, emptyList(), v6)
        assertEquals(1, v6.size)
    }

    @Test
    fun `test properties rule builder list-parameter rules`() {
        val pMulti =
            PropertyDeclaration(
                name = "multiProp",
                visibility = Visibility.INTERNAL,
                modifiers = setOf(Modifier.CONST),
                type = "kotlin.Double",
                isVal = true,
                annotations =
                    listOf(
                        AnnotationDeclaration("A1", "com.example.A1"),
                        AnnotationDeclaration("A2", "com.example.A2"),
                    ),
                kdocText = null,
            )

        val graph = ProjectGraph(emptyMap())
        val context = PropertyDeclarationContext(pMulti, "com.example", null, ":app", "/src/MyService.kt")

        // 1. haveAllAnnotationsOf & haveAnyAnnotationOf in PropertiesThat
        val ruleAllAnno = PropertiesRuleBuilder(graph).that().haveAllAnnotationsOf(listOf("A1", "A2"))
        assertTrue(ruleAllAnno.getThatPredicate()!!(context))

        val ruleAnyAnno = PropertiesRuleBuilder(graph).that().haveAnyAnnotationOf(listOf("A2", "A3"))
        assertTrue(ruleAnyAnno.getThatPredicate()!!(context))

        // 2. haveAllModifiers & haveAnyModifier in PropertiesThat
        val ruleAllMod = PropertiesRuleBuilder(graph).that().haveAllModifiers(listOf(Modifier.CONST))
        assertTrue(ruleAllMod.getThatPredicate()!!(context))

        val ruleAnyMod = PropertiesRuleBuilder(graph).that().haveAnyModifier(listOf(Modifier.CONST, Modifier.LATEINIT))
        assertTrue(ruleAnyMod.getThatPredicate()!!(context))

        // 3. haveAnyVisibility in PropertiesThat
        val ruleAnyVis =
            PropertiesRuleBuilder(
                graph,
            ).that().haveAnyVisibility(listOf(Visibility.INTERNAL, Visibility.PRIVATE))
        assertTrue(ruleAnyVis.getThatPredicate()!!(context))

        // 4. haveType in PropertiesThat
        val ruleAnyType = PropertiesRuleBuilder(graph).that().haveType(listOf("kotlin.Double", "kotlin.Int"))
        assertTrue(ruleAnyType.getThatPredicate()!!(context))

        // 5. Assertions in PropertiesShould
        val assertAllAnno =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllAnnotationsOf(listOf("A1", "A2")).getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAllAnno(context, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertAnyVis =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyVisibility(listOf(Visibility.INTERNAL)).getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertAnyVis(context, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val assertAnyType =
            PropertiesRuleBuilder(
                graph,
            ).should().haveType(listOf("kotlin.Double", "kotlin.Int")).getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertAnyType(context, emptyList(), v5)
        assertTrue(v5.isEmpty())

        val assertAllMod =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAllModifiers(listOf(Modifier.CONST)).getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertAllMod(context, emptyList(), v7)
        assertTrue(v7.isEmpty())

        val assertAnyMod =
            PropertiesRuleBuilder(
                graph,
            ).should().haveAnyModifier(listOf(Modifier.CONST, Modifier.LATEINIT)).getShouldAssertion()!!
        val v8 = mutableListOf<String>()
        assertAnyMod(context, emptyList(), v8)
        assertTrue(v8.isEmpty())
    }
}
