/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.SliceGraph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SlicesCoverageTest : KontureScopeTestFixture() {
    @Test
    fun `test SlicesShould cycle and dependency assertions`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val sliceA = Slice("payment", setOf("com.example.payment"), listOf(classA))
        val sliceB = Slice("billing", setOf("com.example.billing"), listOf(classB))

        val cyclicGraph =
            SliceGraph(
                slices = listOf(sliceA, sliceB),
                adjacency = mapOf("payment" to setOf("billing"), "billing" to setOf("payment")),
            )

        val vCycle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().beFreeOfCycles()
            .checkRuleAssertions(cyclicGraph, vCycle)
        assertEquals(1, vCycle.size)

        val vIsolation = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notDependOnEachOther()
            .checkRuleAssertions(cyclicGraph, vIsolation)
        assertEquals(2, vIsolation.size)

        val vOnlyDepList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().onlyDependOnSlices(listOf("billing"))
            .checkRuleAssertions(cyclicGraph, vOnlyDepList)
        assertEquals(1, vOnlyDepList.size) // payment -> billing is allowed, billing -> payment is violation

        val vOnlyDepVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().onlyDependOnSlices("billing")
            .checkRuleAssertions(cyclicGraph, vOnlyDepVararg)
        assertEquals(1, vOnlyDepVararg.size)

        val vNotDepSingle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notDependOnSlice("billing")
            .checkRuleAssertions(cyclicGraph, vNotDepSingle)
        assertEquals(1, vNotDepSingle.size)

        val vNotDepList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notDependOnSlices(listOf("billing"))
            .checkRuleAssertions(cyclicGraph, vNotDepList)
        assertEquals(1, vNotDepList.size)

        val vNotDepVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notDependOnSlices("billing", "shipping")
            .checkRuleAssertions(cyclicGraph, vNotDepVararg)
        assertEquals(1, vNotDepVararg.size)

        val vDepSingle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().dependOnSlice("billing")
            .checkRuleAssertions(cyclicGraph, vDepSingle)
        assertTrue(vDepSingle.isEmpty()) // payment depends on billing, billing doesn't depend on itself

        val vDepList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().dependOnSlices(listOf("billing"))
            .checkRuleAssertions(cyclicGraph, vDepList)
        assertTrue(vDepList.isEmpty())

        val vDepVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().dependOnSlices("billing")
            .checkRuleAssertions(cyclicGraph, vDepVararg)
        assertTrue(vDepVararg.isEmpty())
    }

    @Test
    fun `test SlicesShould class, package and annotation assertions`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val sliceA = Slice("payment", setOf("com.example.payment"), listOf(classAnnotated))
        val sliceEmpty = Slice("empty", setOf("com.example.empty"), emptyList())

        val sliceGraph =
            SliceGraph(
                slices = listOf(sliceA, sliceEmpty),
                adjacency = mapOf("payment" to emptySet(), "empty" to emptySet()),
            )

        val vClasses = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClasses()
            .checkRuleAssertions(sliceGraph, vClasses)
        assertEquals(1, vClasses.size)

        val vNotClasses = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClasses()
            .checkRuleAssertions(sliceGraph, vNotClasses)
        assertEquals(1, vNotClasses.size)

        val vFiles = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containFiles()
            .checkRuleAssertions(sliceGraph, vFiles)
        assertEquals(1, vFiles.size)

        val vNotFiles = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainFiles()
            .checkRuleAssertions(sliceGraph, vNotFiles)
        assertEquals(1, vNotFiles.size)

        val vNotClassSingle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClass("ClassAnnotated")
            .checkRuleAssertions(sliceGraph, vNotClassSingle)
        assertEquals(1, vNotClassSingle.size)

        val vPkgSingle = mutableListOf<String>()
        val sliceGraph1 = SliceGraph(slices = listOf(sliceA), adjacency = mapOf("payment" to emptySet()))
        SlicesRuleBuilder(graph).should().containClassesInPackage("com.example..")
            .checkRuleAssertions(sliceGraph1, vPkgSingle)
        assertTrue(vPkgSingle.isEmpty())

        val vPkgList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesInPackage(listOf("com.example.."))
            .checkRuleAssertions(sliceGraph1, vPkgList)
        assertTrue(vPkgList.isEmpty())

        val vPkgVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesInPackage("com.example..", "org.other..")
            .checkRuleAssertions(sliceGraph1, vPkgVararg)
        assertTrue(vPkgVararg.isEmpty())

        val vNotPkgSingle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClassesInPackage("com.example..")
            .checkRuleAssertions(sliceGraph, vNotPkgSingle)
        assertEquals(1, vNotPkgSingle.size)

        val vAnnotSingle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesWithAnnotation("MyAnnotation")
            .checkRuleAssertions(sliceGraph, vAnnotSingle)
        assertEquals(1, vAnnotSingle.size) // sliceEmpty fails

        val vAnnotList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesWithAnnotation(listOf("MyAnnotation"))
            .checkRuleAssertions(sliceGraph, vAnnotList)
        assertEquals(1, vAnnotList.size)

        val vAnnotVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().containClassesWithAnnotation("MyAnnotation", "Other")
            .checkRuleAssertions(sliceGraph, vAnnotVararg)
        assertEquals(1, vAnnotVararg.size)

        val vNotAnnotSingle = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notContainClassesWithAnnotation("MyAnnotation")
            .checkRuleAssertions(sliceGraph, vNotAnnotSingle)
        assertEquals(1, vNotAnnotSingle.size)

        val vHaveName = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().haveName("billing")
            .checkRuleAssertions(sliceGraph, vHaveName)
        assertEquals(2, vHaveName.size)

        val vNotHaveName = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notHaveName("billing")
            .checkRuleAssertions(sliceGraph, vNotHaveName)
        assertTrue(vNotHaveName.isEmpty())

        val vResideMod = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().resideInModule("src")
            .checkRuleAssertions(sliceGraph1, vResideMod)
        assertTrue(vResideMod.isEmpty())

        val vResideModsList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().resideInModules(listOf("src"))
            .checkRuleAssertions(sliceGraph1, vResideModsList)
        assertTrue(vResideModsList.isEmpty())

        val vResideModsVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().resideInModules("src", "core")
            .checkRuleAssertions(sliceGraph1, vResideModsVararg)
        assertTrue(vResideModsVararg.isEmpty())

        val vNotResideMod = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notResideInModule("core")
            .checkRuleAssertions(sliceGraph, vNotResideMod)
        assertTrue(vNotResideMod.isEmpty())

        val vNotResideModsList = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notResideInModules(listOf("core"))
            .checkRuleAssertions(sliceGraph, vNotResideModsList)
        assertTrue(vNotResideModsList.isEmpty())

        val vNotResideModsVararg = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().notResideInModules("core", "feature")
            .checkRuleAssertions(sliceGraph, vNotResideModsVararg)
        assertTrue(vNotResideModsVararg.isEmpty())

        val vAnyOf = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().anyOf(
            { dependOnSlice("billing") },
            { notDependOnSlice("wrong") },
        ).checkRuleAssertions(sliceGraph, vAnyOf)
        assertTrue(vAnyOf.isEmpty())

        val vAllOf = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().allOf(
            { notDependOnSlice("wrong") },
        ).checkRuleAssertions(sliceGraph, vAllOf)
        assertTrue(vAllOf.isEmpty())

        val vNoneOf = mutableListOf<String>()
        SlicesRuleBuilder(graph).should().noneOf(
            { dependOnSlice("wrong") },
        ).checkRuleAssertions(sliceGraph, vNoneOf)
        assertTrue(vNoneOf.isEmpty())
    }

    @Test
    fun `test SlicesThat filters`() {
        val graph =
            ProjectGraph(
                mapOf(":" to listOf(Module(":", ":app", "app", emptyList(), emptyList(), emptyList(), listOf(fileA)))),
            )
        val slice = Slice("payment", setOf("com.example.payment"), listOf(classAnnotated))

        val pKeySingle = SlicesRuleBuilder(graph).that().haveKey("payment").getThatPredicate()!!
        assertTrue(pKeySingle(slice))

        val pKeyList = SlicesRuleBuilder(graph).that().haveKey(listOf("payment")).getThatPredicate()!!
        assertTrue(pKeyList(slice))

        val pKeyVararg = SlicesRuleBuilder(graph).that().haveKey("payment", "billing").getThatPredicate()!!
        assertTrue(pKeyVararg(slice))

        val pKeyPred = SlicesRuleBuilder(graph).that().haveKey { it.startsWith("pay") }.getThatPredicate()!!
        assertTrue(pKeyPred(slice))

        val pStartSingle = SlicesRuleBuilder(graph).that().haveKeyStartingWith("pay").getThatPredicate()!!
        assertTrue(pStartSingle(slice))

        val pStartList = SlicesRuleBuilder(graph).that().haveKeyStartingWith(listOf("pay")).getThatPredicate()!!
        assertTrue(pStartList(slice))

        val pStartVararg = SlicesRuleBuilder(graph).that().haveKeyStartingWith("pay", "bil").getThatPredicate()!!
        assertTrue(pStartVararg(slice))

        val pEndSingle = SlicesRuleBuilder(graph).that().haveKeyEndingWith("ment").getThatPredicate()!!
        assertTrue(pEndSingle(slice))

        val pEndList = SlicesRuleBuilder(graph).that().haveKeyEndingWith(listOf("ment")).getThatPredicate()!!
        assertTrue(pEndList(slice))

        val pEndVararg = SlicesRuleBuilder(graph).that().haveKeyEndingWith("ment", "ing").getThatPredicate()!!
        assertTrue(pEndVararg(slice))

        val pMatchSingle = SlicesRuleBuilder(graph).that().haveKeyMatching("pay*").getThatPredicate()!!
        assertTrue(pMatchSingle(slice))

        val pMatchList = SlicesRuleBuilder(graph).that().haveKeyMatching(listOf("pay*")).getThatPredicate()!!
        assertTrue(pMatchList(slice))

        val pMatchVararg = SlicesRuleBuilder(graph).that().haveKeyMatching("pay*", "bil*").getThatPredicate()!!
        assertTrue(pMatchVararg(slice))

        val pNotKeySingle = SlicesRuleBuilder(graph).that().notHaveKey("billing").getThatPredicate()!!
        assertTrue(pNotKeySingle(slice))

        val pNotKeyList = SlicesRuleBuilder(graph).that().notHaveKey(listOf("billing")).getThatPredicate()!!
        assertTrue(pNotKeyList(slice))

        val pNotKeyVararg = SlicesRuleBuilder(graph).that().notHaveKey("billing", "shipping").getThatPredicate()!!
        assertTrue(pNotKeyVararg(slice))

        val pNotStartSingle = SlicesRuleBuilder(graph).that().notHaveKeyStartingWith("bil").getThatPredicate()!!
        assertTrue(pNotStartSingle(slice))

        val pNotStartList = SlicesRuleBuilder(graph).that().notHaveKeyStartingWith(listOf("bil")).getThatPredicate()!!
        assertTrue(pNotStartList(slice))

        val pNotStartVararg = SlicesRuleBuilder(graph).that().notHaveKeyStartingWith("bil", "ship").getThatPredicate()!!
        assertTrue(pNotStartVararg(slice))

        val pNotEndSingle = SlicesRuleBuilder(graph).that().notHaveKeyEndingWith("ing").getThatPredicate()!!
        assertTrue(pNotEndSingle(slice))

        val pNotEndList = SlicesRuleBuilder(graph).that().notHaveKeyEndingWith(listOf("ing")).getThatPredicate()!!
        assertTrue(pNotEndList(slice))

        val pNotEndVararg = SlicesRuleBuilder(graph).that().notHaveKeyEndingWith("ing", "ed").getThatPredicate()!!
        assertTrue(pNotEndVararg(slice))

        val pNotMatchSingle = SlicesRuleBuilder(graph).that().notHaveKeyMatching("bil*").getThatPredicate()!!
        assertTrue(pNotMatchSingle(slice))

        val pNotMatchList = SlicesRuleBuilder(graph).that().notHaveKeyMatching(listOf("bil*")).getThatPredicate()!!
        assertTrue(pNotMatchList(slice))

        val pNotMatchVararg = SlicesRuleBuilder(graph).that().notHaveKeyMatching("bil*", "ship*").getThatPredicate()!!
        assertTrue(pNotMatchVararg(slice))

        val pClassSingle = SlicesRuleBuilder(graph).that().containClass("ClassAnnotated").getThatPredicate()!!
        assertTrue(pClassSingle(slice))

        val pClassList = SlicesRuleBuilder(graph).that().containClass(listOf("ClassAnnotated")).getThatPredicate()!!
        assertTrue(pClassList(slice))

        val pClassVararg = SlicesRuleBuilder(graph).that().containClass("ClassAnnotated", "ClassB").getThatPredicate()!!
        assertTrue(pClassVararg(slice))

        val pPkgSingle = SlicesRuleBuilder(graph).that().containClassesInPackage("com.example..").getThatPredicate()!!
        assertTrue(pPkgSingle(slice))

        val pPkgList =
            SlicesRuleBuilder(
                graph,
            ).that().containClassesInPackage(listOf("com.example..")).getThatPredicate()!!
        assertTrue(pPkgList(slice))

        val pPkgVararg =
            SlicesRuleBuilder(
                graph,
            ).that().containClassesInPackage("com.example..", "org.other..").getThatPredicate()!!
        assertTrue(pPkgVararg(slice))

        val pPkgPred =
            SlicesRuleBuilder(graph).that().containClassesInPackage {
                it.startsWith("com.example")
            }.getThatPredicate()!!
        assertTrue(pPkgPred(slice))

        val pSatisfy = SlicesRuleBuilder(graph).that().satisfy { it.key == "payment" }.getThatPredicate()!!
        assertTrue(pSatisfy(slice))

        val pAnnotSingle =
            SlicesRuleBuilder(
                graph,
            ).that().containClassesWithAnnotation("MyAnnotation").getThatPredicate()!!
        assertTrue(pAnnotSingle(slice))

        val pAnnotList =
            SlicesRuleBuilder(
                graph,
            ).that().containClassesWithAnnotation(listOf("MyAnnotation")).getThatPredicate()!!
        assertTrue(pAnnotList(slice))

        val pAnnotVararg =
            SlicesRuleBuilder(
                graph,
            ).that().containClassesWithAnnotation("MyAnnotation", "Other").getThatPredicate()!!
        assertTrue(pAnnotVararg(slice))

        val pNotClassSingle = SlicesRuleBuilder(graph).that().notContainClass("MissingClass").getThatPredicate()!!
        assertTrue(pNotClassSingle(slice))

        val pNotPkgSingle =
            SlicesRuleBuilder(
                graph,
            ).that().notContainClassesInPackage("org.missing..").getThatPredicate()!!
        assertTrue(pNotPkgSingle(slice))

        val pNotAnnotSingle =
            SlicesRuleBuilder(
                graph,
            ).that().notContainClassesWithAnnotation("MissingAnnot").getThatPredicate()!!
        assertTrue(pNotAnnotSingle(slice))

        val pNameSingle = SlicesRuleBuilder(graph).that().haveName("payment").getThatPredicate()!!
        assertTrue(pNameSingle(slice))

        val pNameList = SlicesRuleBuilder(graph).that().haveName(listOf("payment")).getThatPredicate()!!
        assertTrue(pNameList(slice))

        val pNameVararg = SlicesRuleBuilder(graph).that().haveName("payment", "billing").getThatPredicate()!!
        assertTrue(pNameVararg(slice))

        val pMatchNameSingle = SlicesRuleBuilder(graph).that().haveNameMatching("pay*").getThatPredicate()!!
        assertTrue(pMatchNameSingle(slice))

        val pMatchNameList = SlicesRuleBuilder(graph).that().haveNameMatching(listOf("pay*")).getThatPredicate()!!
        assertTrue(pMatchNameList(slice))

        val pMatchNameVararg = SlicesRuleBuilder(graph).that().haveNameMatching("pay*", "bil*").getThatPredicate()!!
        assertTrue(pMatchNameVararg(slice))

        val pStartNameSingle = SlicesRuleBuilder(graph).that().haveNameStartingWith("pay").getThatPredicate()!!
        assertTrue(pStartNameSingle(slice))

        val pStartNameList = SlicesRuleBuilder(graph).that().haveNameStartingWith(listOf("pay")).getThatPredicate()!!
        assertTrue(pStartNameList(slice))

        val pStartNameVararg = SlicesRuleBuilder(graph).that().haveNameStartingWith("pay", "bil").getThatPredicate()!!
        assertTrue(pStartNameVararg(slice))

        val pEndNameSingle = SlicesRuleBuilder(graph).that().haveNameEndingWith("ment").getThatPredicate()!!
        assertTrue(pEndNameSingle(slice))

        val pEndNameList = SlicesRuleBuilder(graph).that().haveNameEndingWith(listOf("ment")).getThatPredicate()!!
        assertTrue(pEndNameList(slice))

        val pEndNameVararg = SlicesRuleBuilder(graph).that().haveNameEndingWith("ment", "ing").getThatPredicate()!!
        assertTrue(pEndNameVararg(slice))

        val pNotNameSingle = SlicesRuleBuilder(graph).that().notHaveName("billing").getThatPredicate()!!
        assertTrue(pNotNameSingle(slice))

        val pNotMatchNameSingle = SlicesRuleBuilder(graph).that().notHaveNameMatching("bil*").getThatPredicate()!!
        assertTrue(pNotMatchNameSingle(slice))

        val pNotStartNameSingle = SlicesRuleBuilder(graph).that().notHaveNameStartingWith("bil").getThatPredicate()!!
        assertTrue(pNotStartNameSingle(slice))

        val pNotEndNameSingle = SlicesRuleBuilder(graph).that().notHaveNameEndingWith("ing").getThatPredicate()!!
        assertTrue(pNotEndNameSingle(slice))

        val pResideModSingle = SlicesRuleBuilder(graph).that().resideInModule("src").getThatPredicate()!!
        assertTrue(pResideModSingle(slice))

        val pResideModsList = SlicesRuleBuilder(graph).that().resideInModules(listOf("src")).getThatPredicate()!!
        assertTrue(pResideModsList(slice))

        val pResideModsVararg = SlicesRuleBuilder(graph).that().resideInModules("src", "core").getThatPredicate()!!
        assertTrue(pResideModsVararg(slice))

        val pNotResideModSingle = SlicesRuleBuilder(graph).that().notResideInModule("core").getThatPredicate()!!
        assertTrue(pNotResideModSingle(slice))

        val pNotResideModsList = SlicesRuleBuilder(graph).that().notResideInModules(listOf("core")).getThatPredicate()!!
        assertTrue(pNotResideModsList(slice))

        val pNotResideModsVararg =
            SlicesRuleBuilder(
                graph,
            ).that().notResideInModules("core", "feature").getThatPredicate()!!
        assertTrue(pNotResideModsVararg(slice))

        val pResidePkgSingle = SlicesRuleBuilder(graph).that().resideInAPackage("com.example..").getThatPredicate()!!
        assertTrue(pResidePkgSingle(slice))

        val pResidePkgList =
            SlicesRuleBuilder(
                graph,
            ).that().resideInAPackage(listOf("com.example..")).getThatPredicate()!!
        assertTrue(pResidePkgList(slice))

        val pResidePkgVararg =
            SlicesRuleBuilder(
                graph,
            ).that().resideInAPackage("com.example..", "org.other..").getThatPredicate()!!
        assertTrue(pResidePkgVararg(slice))

        val pNotResidePkgSingle =
            SlicesRuleBuilder(
                graph,
            ).that().notResideInAPackage("org.other..").getThatPredicate()!!
        assertTrue(pNotResidePkgSingle(slice))

        val pAnyOf =
            SlicesRuleBuilder(graph).that().anyOf(
                { haveKey("payment") },
                { haveKey("billing") },
            ).getThatPredicate()!!
        assertTrue(pAnyOf(slice))

        val pAllOf =
            SlicesRuleBuilder(graph).that().allOf(
                { haveKey("payment") },
                { containClassesInPackage("com.example..") },
            ).getThatPredicate()!!
        assertTrue(pAllOf(slice))

        val pNoneOf =
            SlicesRuleBuilder(graph).that().noneOf(
                { haveKey("billing") },
            ).getThatPredicate()!!
        assertTrue(pNoneOf(slice))
    }
}
