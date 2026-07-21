/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PropertiesRuleBuilderLogicTest : RuleBuildersTestBase() {
    @Test
    fun `test properties rule builder logic gates and basic filters`() {
        val p1 =
            PropertyDeclaration(
                name = "p1",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                type = "kotlin.Int",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
            )
        val p2 =
            PropertyDeclaration(
                name = "p2",
                visibility = Visibility.PRIVATE,
                modifiers = emptySet(),
                type = "kotlin.String",
                isVal = false,
                annotations = emptyList(),
                kdocText = null,
            )

        val fileDecl =
            FileDeclaration(
                name = "Sample.kt",
                packageName = "com.example",
                classes = emptyList(),
                topLevelProperties = listOf(p1, p2),
                filePath = "/src/Sample.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        val contexts =
            listOf(
                PropertyDeclarationContext(p1, "com.example", null, ":app", "/src/Sample.kt"),
                PropertyDeclarationContext(p2, "com.example", null, ":app", "/src/Sample.kt"),
            )

        // filtering tests
        val ruleName = PropertiesRuleBuilder(graph).that().haveNameStartingWith("p")
        assertEquals(2, contexts.filter(ruleName.getThatPredicate()!!).size)

        val ruleNameEnding = PropertiesRuleBuilder(graph).that().haveNameEndingWith("2")
        assertEquals(1, contexts.filter(ruleNameEnding.getThatPredicate()!!).size)

        val ruleNameMatch = PropertiesRuleBuilder(graph).that().haveNameMatching("*1")
        assertEquals(1, contexts.filter(ruleNameMatch.getThatPredicate()!!).size)

        val ruleSatisfy = PropertiesRuleBuilder(graph).that().satisfy { it.declaration.type == "kotlin.Int" }
        assertEquals(1, contexts.filter(ruleSatisfy.getThatPredicate()!!).size)

        // logic gates on filters
        val ruleXor =
            PropertiesRuleBuilder(graph).that().haveNameStartingWith("p").xor().satisfy {
                it.declaration.type ==
                    "kotlin.Int"
            }
        // p1: starts with p (T) xor type is Int (T) -> F
        // p2: starts with p (T) xor type is Int (F) -> T
        val filtered = contexts.filter(ruleXor.getThatPredicate()!!)
        assertEquals(1, filtered.size)
        assertEquals("p2", filtered[0].declaration.name)

        // logic gates on assertions
        val ruleAndShould =
            PropertiesRuleBuilder(graph)
                .should()
                .bePublic()
                .andShould()
                .beVal()
        val assertAnd = ruleAndShould.getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAnd(contexts[0], contexts, v1)
        assertTrue(v1.isEmpty())

        val ruleOrShould =
            PropertiesRuleBuilder(graph)
                .should()
                .bePublic()
                .orShould()
                .bePrivate()
        val assertOr = ruleOrShould.getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertOr(contexts[0], contexts, v2)
        assertTrue(v2.isEmpty())
        assertOr(contexts[1], contexts, v2)
        assertTrue(v2.isEmpty())

        val ruleNotShould = PropertiesRuleBuilder(graph).notShould().bePrivate()
        val assertNot = ruleNotShould.getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertNot(contexts[0], contexts, v3)
        assertTrue(v3.isEmpty())
        assertNot(contexts[1], contexts, v3)
        assertEquals(1, v3.size)
    }

    @Test
    fun `test properties rule builder additional filters and gates`() {
        val prop =
            PropertyDeclaration(
                name = "myVar",
                visibility = Visibility.PROTECTED,
                modifiers = setOf(Modifier.OPEN),
                type = "kotlin.String",
                isVal = false, // var
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )

        val fileDecl =
            FileDeclaration(
                name = "Sample.kt",
                packageName = "com.example",
                classes = emptyList(),
                topLevelProperties = listOf(prop),
                filePath = "/src/Sample.kt",
            )
        val mockModule =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileDecl),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(mockModule)))
        val context = PropertyDeclarationContext(prop, "com.example", null, ":app", "/src/Sample.kt")

        // resideInAPackage(predicate)
        assertTrue(
            PropertiesRuleBuilder(graph).that().resideInAPackage { it == "com.example" }.getThatPredicate()!!(context),
        )
        assertFalse(
            PropertiesRuleBuilder(graph).that().resideInAPackage { it == "other" }.getThatPredicate()!!(context),
        )

        // haveModifier, haveVisibility, haveType in PropertiesThat
        assertTrue(PropertiesRuleBuilder(graph).that().haveModifier(Modifier.OPEN).getThatPredicate()!!(context))
        assertTrue(
            PropertiesRuleBuilder(graph).that().haveVisibility(Visibility.PROTECTED).getThatPredicate()!!(context),
        )
        assertTrue(PropertiesRuleBuilder(graph).that().haveType("kotlin.String").getThatPredicate()!!(context))

        // resideInAPackage(packagePattern) and resideInAPackage(predicate) in PropertiesShould
        val shouldPackPattern = PropertiesRuleBuilder(graph).should().resideInAPackage("other").getShouldAssertion()!!
        val vPack1 = mutableListOf<String>()
        shouldPackPattern(context, emptyList(), vPack1)
        assertEquals(1, vPack1.size)
        assertTrue(vPack1[0].contains("should reside in package 'other'"))

        val shouldPackPred =
            PropertiesRuleBuilder(
                graph,
            ).should().resideInAPackage { it == "other" }.getShouldAssertion()!!
        val vPack2 = mutableListOf<String>()
        shouldPackPred(context, emptyList(), vPack2)
        assertEquals(1, vPack2.size)
        assertTrue(vPack2[0].contains("should reside in package matching predicate"))

        // beVar, haveModifier, haveVisibility, satisfy in PropertiesShould
        val assertionsRule =
            PropertiesRuleBuilder(graph)
                .should()
                .beVar()
                .andShould()
                .haveModifier(Modifier.OPEN)
                .andShould()
                .haveVisibility(Visibility.PROTECTED)
                .andShould()
                .satisfy { it.declaration.name == "myVar" }
                .andShould()
                .satisfy { p, violations -> if (p.declaration.name != "myVar") violations.add("oops") }

        val vAssert = mutableListOf<String>()
        assertionsRule.getShouldAssertion()!!(context, emptyList(), vAssert)
        assertTrue(vAssert.isEmpty(), "Expected no violations but got: $vAssert")

        // check failures
        val assertFailRule =
            PropertiesRuleBuilder(graph)
                .should()
                .haveModifier(Modifier.CONST)
                .andShould()
                .haveVisibility(Visibility.PUBLIC)
                .andShould()
                .satisfy { false }

        val vAssertFail = mutableListOf<String>()
        assertFailRule.getShouldAssertion()!!(context, emptyList(), vAssertFail)
        assertEquals(3, vAssertFail.size)

        // Logical AND/OR/NOT in builder filters
        val ruleAndNot =
            PropertiesRuleBuilder(graph)
                .that()
                .resideInAPackage("com.example")
                .not()
                .beMember()
        assertTrue(ruleAndNot.getThatPredicate()!!(context))

        val ruleOr =
            PropertiesRuleBuilder(graph)
                .that()
                .haveNameStartingWith("other")
                .or()
                .beTopLevel()
        assertTrue(ruleOr.getThatPredicate()!!(context))

        // xorShould in assertions
        val ruleXorPass =
            PropertiesRuleBuilder(graph)
                .should()
                .beVar()
                .xorShould()
                .bePrivate()
        val vXorPass = mutableListOf<String>()
        ruleXorPass.getShouldAssertion()!!(context, emptyList(), vXorPass)
        assertTrue(vXorPass.isEmpty())

        val ruleXorFail =
            PropertiesRuleBuilder(graph)
                .should()
                .beVar()
                .xorShould()
                .haveType("kotlin.String")
        val vXorFail = mutableListOf<String>()
        ruleXorFail.getShouldAssertion()!!(context, emptyList(), vXorFail)
        assertEquals(1, vXorFail.size)
        assertTrue(vXorFail[0].contains("should satisfy exactly one of the XOR assertions"))

        // check() method success/fail
        val passingBuilder =
            PropertiesRuleBuilder(graph)
                .that()
                .beTopLevel()
                .should()
                .beVar()
        passingBuilder.check() // should not throw

        val failingBuilder =
            PropertiesRuleBuilder(graph)
                .that()
                .beTopLevel()
                .should()
                .beVal()
        assertThrows(AssertionError::class.java) {
            failingBuilder.check()
        }
    }
}
