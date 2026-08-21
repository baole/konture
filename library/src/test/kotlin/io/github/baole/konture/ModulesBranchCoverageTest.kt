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

internal class ModulesBranchCoverageTest : RuleBuildersTestBase() {
    @Test
    fun `test ModulesThat branch coverage with glob, colon, empty, and matching variations`() {
        val modA =
            Module(
                buildId = ":",
                path = ":app:feature",
                projectDir = "app/feature",
                appliedPlugins = listOf("com.android.application", "kotlin-android"),
                sourceSets = emptyList(),
                dependencies =
                    listOf(
                        Dependency("implementation", ":", ":core:network"),
                        Dependency("api", ":", ":core:model"),
                    ),
                files = emptyList(),
            )
        val modB =
            Module(
                buildId = ":",
                path = ":core:network",
                projectDir = "core/network",
                appliedPlugins = listOf("org.jetbrains.kotlin.jvm"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = emptyList(),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(modA, modB)))

        // haveNamePath / resideInAModule branches
        val pPathNoColon = ModulesRuleBuilder(graph).that().haveNamePath("app:feature").getThatPredicate()!!
        assertTrue(pPathNoColon(modA))
        assertFalse(pPathNoColon(modB))

        val pPathGlob = ModulesRuleBuilder(graph).that().haveNameMatching("**:feature").getThatPredicate()!!
        assertTrue(pPathGlob(modA))
        assertFalse(pPathGlob(modB))

        val pPathEmpty = ModulesRuleBuilder(graph).that().haveNamePath("").getThatPredicate()!!
        assertFalse(pPathEmpty(modA))

        val pPathList =
            ModulesRuleBuilder(
                graph,
            ).that().haveNamePath(listOf("app:feature", ":core:network")).getThatPredicate()!!
        assertTrue(pPathList(modA))
        assertTrue(pPathList(modB))

        val pPathListNoColon = ModulesRuleBuilder(graph).that().haveNamePath(listOf("app:feature")).getThatPredicate()!!
        assertTrue(pPathListNoColon(modA))
        assertFalse(pPathListNoColon(modB))

        val pNotPathSingle = ModulesRuleBuilder(graph).that().notHaveName("app:feature").getThatPredicate()!!
        assertFalse(pNotPathSingle(modA))
        assertTrue(pNotPathSingle(modB))

        val pNotPathSingleGlob = ModulesRuleBuilder(graph).that().notHaveNameMatching("**:network").getThatPredicate()!!
        assertTrue(pNotPathSingleGlob(modA))
        assertFalse(pNotPathSingleGlob(modB))

        // dependOnModule / dependOnModules
        val pDepSingle = ModulesRuleBuilder(graph).that().dependOnModule("core:network").getThatPredicate()!!
        assertTrue(pDepSingle(modA))
        assertFalse(pDepSingle(modB))

        val pDepList =
            ModulesRuleBuilder(
                graph,
            ).that().dependOnModules(listOf("core:network", "other")).getThatPredicate()!!
        assertTrue(pDepList(modA))
        assertFalse(pDepList(modB))

        val pNotDepSingle = ModulesRuleBuilder(graph).that().notDependOnModule("core:network").getThatPredicate()!!
        assertFalse(pNotDepSingle(modA))
        assertTrue(pNotDepSingle(modB))

        val pNotDepList =
            ModulesRuleBuilder(
                graph,
            ).that().notDependOnModules(listOf("core:network")).getThatPredicate()!!
        assertFalse(pNotDepList(modA))
        assertTrue(pNotDepList(modB))

        // applyPlugin
        val pPlugin = ModulesRuleBuilder(graph).that().applyPlugin("com.android.application").getThatPredicate()!!
        assertTrue(pPlugin(modA))
        assertFalse(pPlugin(modB))

        val pPluginList =
            ModulesRuleBuilder(
                graph,
            ).that().applyPlugin(listOf("com.android.application", "other")).getThatPredicate()!!
        assertFalse(pPluginList(modA)) // because all must match
        assertFalse(pPluginList(modB))

        val pNotPlugin = ModulesRuleBuilder(graph).that().notApplyPlugin("com.android.application").getThatPredicate()!!
        assertFalse(pNotPlugin(modA))
        assertTrue(pNotPlugin(modB))

        val pNotPluginList =
            ModulesRuleBuilder(
                graph,
            ).that().notHavePlugins(listOf("com.android.application")).getThatPredicate()!!
        assertFalse(pNotPluginList(modA))
        assertTrue(pNotPluginList(modB))
    }

    @Test
    fun `test ModulesShouldDependencyAssertions and Structure with cycles and transitive dependencies`() {
        val depB = Dependency("implementation", ":", ":moduleB")
        val depC = Dependency("implementation", ":", ":moduleC")
        val depA = Dependency("implementation", ":", ":moduleA")

        val modA = Module(":", ":moduleA", "moduleA", listOf("kotlin-jvm"), emptyList(), listOf(depB), emptyList())
        val modB = Module(":", ":moduleB", "moduleB", listOf("kotlin-jvm"), emptyList(), listOf(depC), emptyList())
        val modC = Module(":", ":moduleC", "moduleC", listOf("kotlin-jvm"), emptyList(), listOf(depA), emptyList())
        val graphWithCycle = ProjectGraph(mapOf(":" to listOf(modA, modB, modC)))

        // Cycle assertions
        val vCycle = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().beFreeOfCycles()
            .getShouldAssertion()!!(modA, graphWithCycle, vCycle)
        assertEquals(1, vCycle.size)

        val vCycleAlias = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().notContainCycles()
            .getShouldAssertion()!!(modA, graphWithCycle, vCycleAlias)
        assertEquals(1, vCycleAlias.size)

        // onlyBeDependedOnBy branches
        val vDependedOn1 = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().onlyBeDependedOnBy(":moduleC")
            .getShouldAssertion()!!(modA, graphWithCycle, vDependedOn1)
        assertTrue(vDependedOn1.isEmpty())

        val vDependedOn2 = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().onlyBeDependedOnBy(":moduleB")
            .getShouldAssertion()!!(modA, graphWithCycle, vDependedOn2)
        assertEquals(1, vDependedOn2.size)

        val vDependedOnGlob = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().onlyBeDependedOnBy("**:moduleC")
            .getShouldAssertion()!!(modA, graphWithCycle, vDependedOnGlob)
        assertTrue(vDependedOnGlob.isEmpty())

        val vDependedOnPred = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().onlyBeDependedOnBy("not moduleC") { it != ":moduleC" }
            .getShouldAssertion()!!(modA, graphWithCycle, vDependedOnPred)
        assertEquals(1, vDependedOnPred.size)

        // Transitive dependencies
        val vTransPass = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().dependOnModuleTransitively(":moduleC")
            .getShouldAssertion()!!(modA, graphWithCycle, vTransPass)
        assertTrue(vTransPass.isEmpty())

        val vTransFail = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().dependOnModuleTransitively(":nonExistent")
            .getShouldAssertion()!!(modA, graphWithCycle, vTransFail)
        assertEquals(1, vTransFail.size)

        val vNotTransPass = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().notDependOnModuleTransitively(":nonExistent")
            .getShouldAssertion()!!(modA, graphWithCycle, vNotTransPass)
        assertTrue(vNotTransPass.isEmpty())

        val vNotTransFail = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().notDependOnModuleTransitively(":moduleC")
            .getShouldAssertion()!!(modA, graphWithCycle, vNotTransFail)
        assertEquals(1, vNotTransFail.size)

        // Standalone and leaf module
        val vStandaloneFail = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().beStandalone()
            .getShouldAssertion()!!(modA, graphWithCycle, vStandaloneFail)
        assertEquals(1, vStandaloneFail.size)

        val vLeafFail = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().beLeafModule()
            .getShouldAssertion()!!(modA, graphWithCycle, vLeafFail)
        assertEquals(1, vLeafFail.size)

        val modLeaf = Module(":", ":leaf", "leaf", emptyList(), emptyList(), emptyList(), emptyList())
        val vLeafPass = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().beLeafModule()
            .getShouldAssertion()!!(modLeaf, graphWithCycle, vLeafPass)
        assertTrue(vLeafPass.isEmpty())

        // Via configuration
        val vConfigPass = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().dependOnModuleImplementation(":moduleB")
            .getShouldAssertion()!!(modA, graphWithCycle, vConfigPass)
        assertTrue(vConfigPass.isEmpty())

        val vConfigFail = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().dependOnModuleApi(":moduleB")
            .getShouldAssertion()!!(modA, graphWithCycle, vConfigFail)
        assertEquals(1, vConfigFail.size)

        val vNotConfigPass = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().notDependOnModuleViaConfiguration(":moduleB", "api")
            .getShouldAssertion()!!(modA, graphWithCycle, vNotConfigPass)
        assertTrue(vNotConfigPass.isEmpty())

        val vNotConfigFail = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().notDependOnModuleViaConfiguration(":moduleB", "implementation")
            .getShouldAssertion()!!(modA, graphWithCycle, vNotConfigFail)
        assertEquals(1, vNotConfigFail.size)

        // Structure assertions (havePlugins, notHavePlugins, etc.)
        val vPluginsPass = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().havePlugins(listOf("kotlin-jvm"))
            .getShouldAssertion()!!(modA, graphWithCycle, vPluginsPass)
        assertTrue(vPluginsPass.isEmpty())

        val vPluginsFail = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().havePlugins("com.android.application", "kotlin-kapt")
            .getShouldAssertion()!!(modA, graphWithCycle, vPluginsFail)
        assertEquals(1, vPluginsFail.size)

        val vNotPluginPass = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().notHavePlugin("com.android.application")
            .getShouldAssertion()!!(modA, graphWithCycle, vNotPluginPass)
        assertTrue(vNotPluginPass.isEmpty())

        val vNotPluginFail = mutableListOf<String>()
        ModulesRuleBuilder(graphWithCycle).should().notHavePlugin("kotlin-jvm")
            .getShouldAssertion()!!(modA, graphWithCycle, vNotPluginFail)
        assertEquals(1, vNotPluginFail.size)
    }
}
