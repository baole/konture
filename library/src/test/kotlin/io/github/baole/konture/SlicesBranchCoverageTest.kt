/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SlicesBranchCoverageTest : RuleBuildersTestBase() {
    @Test
    fun `test SlicesThat filter branches`() {
        val classA =
            ClassDeclaration(
                name = "ClassA",
                fqName = "com.example.feature.ClassA",
                packageName = "com.example.feature",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassA.kt",
            )
        val classB =
            ClassDeclaration(
                name = "ClassB",
                fqName = "com.example.core.ClassB",
                packageName = "com.example.core",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassB.kt",
            )
        val sliceA = Slice("feature", setOf("com.example.feature"), listOf(classA))
        val sliceB = Slice("core", setOf("com.example.core"), listOf(classB))

        val graph = ProjectGraph(emptyMap())

        // haveKey / haveKeyMatching
        val pKeyPass = SlicesRuleBuilder(graph).that().haveKey("feature").getThatPredicate()!!
        assertTrue(pKeyPass(sliceA))
        assertFalse(pKeyPass(sliceB))

        val pKeyGlob = SlicesRuleBuilder(graph).that().haveKey("*ture").getThatPredicate()!!
        assertTrue(pKeyGlob(sliceA))
        assertFalse(pKeyGlob(sliceB))

        val pKeyList = SlicesRuleBuilder(graph).that().haveKey(listOf("feature", "core")).getThatPredicate()!!
        assertTrue(pKeyList(sliceA))
        assertTrue(pKeyList(sliceB))

        val pKeyPred = SlicesRuleBuilder(graph).that().haveKey { it.startsWith("feat") }.getThatPredicate()!!
        assertTrue(pKeyPred(sliceA))
        assertFalse(pKeyPred(sliceB))

        // haveKeyStartingWith / haveKeyEndingWith
        val pKeyStart = SlicesRuleBuilder(graph).that().haveKeyStartingWith("feat").getThatPredicate()!!
        assertTrue(pKeyStart(sliceA))
        assertFalse(pKeyStart(sliceB))

        val pKeyStartList =
            SlicesRuleBuilder(
                graph,
            ).that().haveKeyStartingWith(listOf("feat", "other")).getThatPredicate()!!
        assertTrue(pKeyStartList(sliceA))
        assertFalse(pKeyStartList(sliceB))

        val pKeyEnd = SlicesRuleBuilder(graph).that().haveKeyEndingWith("ture").getThatPredicate()!!
        assertTrue(pKeyEnd(sliceA))
        assertFalse(pKeyEnd(sliceB))

        val pKeyEndList =
            SlicesRuleBuilder(
                graph,
            ).that().haveKeyEndingWith(listOf("ture", "other")).getThatPredicate()!!
        assertTrue(pKeyEndList(sliceA))
        assertFalse(pKeyEndList(sliceB))

        // notHaveKey branches
        val pNotKey = SlicesRuleBuilder(graph).that().notHaveKey("feature").getThatPredicate()!!
        assertFalse(pNotKey(sliceA))
        assertTrue(pNotKey(sliceB))

        val pNotKeyGlob = SlicesRuleBuilder(graph).that().notHaveKey("*ture").getThatPredicate()!!
        assertFalse(pNotKeyGlob(sliceA))
        assertTrue(pNotKeyGlob(sliceB))

        val pNotKeyList = SlicesRuleBuilder(graph).that().notHaveKey(listOf("feature")).getThatPredicate()!!
        assertFalse(pNotKeyList(sliceA))
        assertTrue(pNotKeyList(sliceB))

        val pNotKeyStart = SlicesRuleBuilder(graph).that().notHaveKeyStartingWith("feat").getThatPredicate()!!
        assertFalse(pNotKeyStart(sliceA))
        assertTrue(pNotKeyStart(sliceB))

        val pNotKeyStartList =
            SlicesRuleBuilder(
                graph,
            ).that().notHaveKeyStartingWith(listOf("feat")).getThatPredicate()!!
        assertFalse(pNotKeyStartList(sliceA))
        assertTrue(pNotKeyStartList(sliceB))

        val pNotKeyEnd = SlicesRuleBuilder(graph).that().notHaveKeyEndingWith("ture").getThatPredicate()!!
        assertFalse(pNotKeyEnd(sliceA))
        assertTrue(pNotKeyEnd(sliceB))

        val pNotKeyEndList = SlicesRuleBuilder(graph).that().notHaveKeyEndingWith(listOf("ture")).getThatPredicate()!!
        assertFalse(pNotKeyEndList(sliceA))
        assertTrue(pNotKeyEndList(sliceB))
    }
}
