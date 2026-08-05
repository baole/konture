/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.DependencyGraphModel
import io.github.baole.konture.core.ResolvedDependencyModel
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModulesThatCoverageTest : RuleBuildersTestBase() {
    @Test
    fun `test ModulesThat name and path filters`() {
        val graph = ProjectGraph(mapOf(":" to listOf(moduleA, moduleB, moduleC)))

        // haveNamePath overloads
        val predPathSingle = ModulesRuleBuilder(graph).that().haveNamePath(":moduleA").getThatPredicate()!!
        assertTrue(predPathSingle(moduleA))
        assertFalse(predPathSingle(moduleB))

        val predPathList =
            ModulesRuleBuilder(
                graph,
            ).that().haveNamePath(listOf(":moduleA", ":moduleB")).getThatPredicate()!!
        assertTrue(predPathList(moduleA))
        assertTrue(predPathList(moduleB))
        assertFalse(predPathList(moduleC))

        val predPathVararg = ModulesRuleBuilder(graph).that().haveNamePath(":moduleA", ":moduleB").getThatPredicate()!!
        assertTrue(predPathVararg(moduleA))

        val predPathPred =
            ModulesRuleBuilder(
                graph,
            ).that().haveNamePath { it.startsWith(":module") }.getThatPredicate()!!
        assertTrue(predPathPred(moduleA))

        // haveName overloads
        val predName = ModulesRuleBuilder(graph).that().haveName(":moduleA").getThatPredicate()!!
        assertTrue(predName(moduleA))

        val predNameList = ModulesRuleBuilder(graph).that().haveName(listOf(":moduleA")).getThatPredicate()!!
        assertTrue(predNameList(moduleA))

        val predNameVararg = ModulesRuleBuilder(graph).that().haveName(":moduleA", ":moduleB").getThatPredicate()!!
        assertTrue(predNameVararg(moduleA))

        // Prefix / Suffix / Glob
        val predStartList = ModulesRuleBuilder(graph).that().haveNameStartingWith(listOf("mod")).getThatPredicate()!!
        assertTrue(predStartList(moduleA))

        val predStartVararg = ModulesRuleBuilder(graph).that().haveNameStartingWith("mod", "app").getThatPredicate()!!
        assertTrue(predStartVararg(moduleA))

        val predEndList = ModulesRuleBuilder(graph).that().haveNameEndingWith(listOf("leA")).getThatPredicate()!!
        assertTrue(predEndList(moduleA))

        val predEndVararg = ModulesRuleBuilder(graph).that().haveNameEndingWith("leA", "leB").getThatPredicate()!!
        assertTrue(predEndVararg(moduleA))

        val predMatchList = ModulesRuleBuilder(graph).that().haveNameMatching(listOf(":module*")).getThatPredicate()!!
        assertTrue(predMatchList(moduleA))

        val predMatchVararg =
            ModulesRuleBuilder(
                graph,
            ).that().haveNameMatching(":module*", ":app*").getThatPredicate()!!
        assertTrue(predMatchVararg(moduleA))
    }

    @Test
    fun `test ModulesThat dependency and plugin filters`() {
        val depB = Dependency("implementation", ":", ":moduleB")
        val modAWithDep = moduleA.copy(dependencies = listOf(depB))
        val graph = ProjectGraph(mapOf(":" to listOf(modAWithDep, moduleB)))

        val predDepSingle = ModulesRuleBuilder(graph).that().dependOnModule(":moduleB").getThatPredicate()!!
        assertTrue(predDepSingle(modAWithDep))
        assertFalse(predDepSingle(moduleB))

        val predDepList = ModulesRuleBuilder(graph).that().dependOnModules(listOf(":moduleB")).getThatPredicate()!!
        assertTrue(predDepList(modAWithDep))

        val predDepVararg =
            ModulesRuleBuilder(
                graph,
            ).that().dependOnModules(":moduleB", ":moduleC").getThatPredicate()!!
        assertTrue(predDepVararg(modAWithDep))

        // notDependOnModule
        val predNotDepSingle = ModulesRuleBuilder(graph).that().notDependOnModule(":moduleB").getThatPredicate()!!
        assertFalse(predNotDepSingle(modAWithDep))
        assertTrue(predNotDepSingle(moduleB))

        val predNotDepList =
            ModulesRuleBuilder(
                graph,
            ).that().notDependOnModules(listOf(":moduleB")).getThatPredicate()!!
        assertFalse(predNotDepList(modAWithDep))

        val predNotDepVararg =
            ModulesRuleBuilder(
                graph,
            ).that().notDependOnModules(":moduleB", ":moduleC").getThatPredicate()!!
        assertFalse(predNotDepVararg(modAWithDep))

        // Plugins
        val predPluginList = ModulesRuleBuilder(graph).that().applyPlugin(listOf("kotlin")).getThatPredicate()!!
        assertTrue(predPluginList(moduleA))

        val predHavePlugin = ModulesRuleBuilder(graph).that().havePlugin("kotlin").getThatPredicate()!!
        assertTrue(predHavePlugin(moduleA))

        val predHavePluginsList = ModulesRuleBuilder(graph).that().havePlugins(listOf("kotlin")).getThatPredicate()!!
        assertTrue(predHavePluginsList(moduleA))

        val predHavePluginsVararg = ModulesRuleBuilder(graph).that().havePlugins("kotlin", "java").getThatPredicate()!!
        assertFalse(predHavePluginsVararg(moduleA))
        assertTrue(predHavePluginsVararg(moduleB))

        val predNotPlugin = ModulesRuleBuilder(graph).that().notApplyPlugin("java").getThatPredicate()!!
        assertTrue(predNotPlugin(moduleA))

        val predNotHavePlugin = ModulesRuleBuilder(graph).that().notHavePlugin("java").getThatPredicate()!!
        assertTrue(predNotHavePlugin(moduleA))

        val predNotHavePluginsList =
            ModulesRuleBuilder(
                graph,
            ).that().notHavePlugins(listOf("java")).getThatPredicate()!!
        assertTrue(predNotHavePluginsList(moduleA))

        val predNotHavePluginsVararg =
            ModulesRuleBuilder(
                graph,
            ).that().notHavePlugins("java", "cpp").getThatPredicate()!!
        assertTrue(predNotHavePluginsVararg(moduleA))
    }

    @Test
    fun `test ModulesThat composite and structure filters`() {
        val modWithSS = moduleA.copy(sourceSets = listOf(SourceSet("main", "KOTLIN_JVM", true, emptyList())))
        val graph = ProjectGraph(mapOf(":" to listOf(modWithSS, moduleB)))

        // SourceSets
        val predSS = ModulesRuleBuilder(graph).that().haveSourceSet("main").getThatPredicate()!!
        assertTrue(predSS(modWithSS))

        val predSSList = ModulesRuleBuilder(graph).that().haveSourceSet(listOf("main")).getThatPredicate()!!
        assertTrue(predSSList(modWithSS))

        val predSSVararg = ModulesRuleBuilder(graph).that().haveSourceSet("main", "test").getThatPredicate()!!
        assertFalse(predSSVararg(modWithSS))

        val predNotSS = ModulesRuleBuilder(graph).that().notHaveSourceSet("test").getThatPredicate()!!
        assertTrue(predNotSS(modWithSS))

        // composite functions
        val predAnyOf =
            ModulesRuleBuilder(graph).that().anyOf(
                { haveNamePath(":moduleA") },
                { haveNamePath(":moduleB") },
            ).getThatPredicate()!!
        assertTrue(predAnyOf(moduleA))
        assertTrue(predAnyOf(moduleB))

        val predAllOf =
            ModulesRuleBuilder(graph).that().allOf(
                { haveNamePath(":moduleA") },
                { applyPlugin("kotlin") },
            ).getThatPredicate()!!
        assertTrue(predAllOf(moduleA))

        val predNoneOf =
            ModulesRuleBuilder(graph).that().noneOf(
                { haveNamePath(":moduleB") },
            ).getThatPredicate()!!
        assertTrue(predNoneOf(moduleA))
        assertFalse(predNoneOf(moduleB))

        // satisfy & matching
        val predMatch = ModulesRuleBuilder(graph).that().matching { it.path == ":moduleA" }.getThatPredicate()!!
        assertTrue(predMatch(moduleA))

        val predSatisfy = ModulesRuleBuilder(graph).that().satisfy { it.path == ":moduleA" }.getThatPredicate()!!
        assertTrue(predSatisfy(moduleA))
    }

    @Test
    fun `test ModulesThat property and package filters`() {
        val graph = ProjectGraph(mapOf(":" to listOf(moduleA, moduleB)))

        val predNotName = ModulesRuleBuilder(graph).that().notHaveName(":moduleA").getThatPredicate()!!
        assertFalse(predNotName(moduleA))
        assertTrue(predNotName(moduleB))

        val predNotMatch = ModulesRuleBuilder(graph).that().notHaveNameMatching(":moduleA").getThatPredicate()!!
        assertFalse(predNotMatch(moduleA))

        val predNotStart = ModulesRuleBuilder(graph).that().notHaveNameStartingWith("moduleA").getThatPredicate()!!
        assertFalse(predNotStart(moduleA))

        val predNotEnd = ModulesRuleBuilder(graph).that().notHaveNameEndingWith("moduleA").getThatPredicate()!!
        assertFalse(predNotEnd(moduleA))

        val predBuildId = ModulesRuleBuilder(graph).that().haveBuildId(":").getThatPredicate()!!
        assertTrue(predBuildId(moduleA))

        val predNotBuildId = ModulesRuleBuilder(graph).that().notHaveBuildId(":").getThatPredicate()!!
        assertFalse(predNotBuildId(moduleA))

        val predProjDir = ModulesRuleBuilder(graph).that().haveProjectDir("moduleA").getThatPredicate()!!
        assertTrue(predProjDir(moduleA))

        val predNotProjDir = ModulesRuleBuilder(graph).that().notHaveProjectDir("moduleA").getThatPredicate()!!
        assertFalse(predNotProjDir(moduleA))

        val predPkgIn = ModulesRuleBuilder(graph).that().containClassesInPackage("com.example").getThatPredicate()!!
        assertTrue(predPkgIn(moduleA))

        val predNotPkgIn =
            ModulesRuleBuilder(
                graph,
            ).that().notContainClassesInPackage("com.example").getThatPredicate()!!
        assertFalse(predNotPkgIn(moduleA))

        val predAnnotIn =
            ModulesRuleBuilder(
                graph,
            ).that().containClassesWithAnnotation("com.example.MyAnnotation").getThatPredicate()!!
        assertTrue(predAnnotIn(moduleB))

        val predNotAnnotIn =
            ModulesRuleBuilder(
                graph,
            ).that().notContainClassesWithAnnotation("com.example.MyAnnotation").getThatPredicate()!!
        assertFalse(predNotAnnotIn(moduleB))

        val predClsIn = ModulesRuleBuilder(graph).that().containClass("com.example.ClassA").getThatPredicate()!!
        assertTrue(predClsIn(moduleA))

        val predNotClsIn = ModulesRuleBuilder(graph).that().notContainClass("com.example.ClassA").getThatPredicate()!!
        assertFalse(predNotClsIn(moduleA))

        // External library filter
        val extDep = ResolvedDependencyModel("com.example", "lib", "1.0", "implementation", false)
        val extGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA)),
                externalDependenciesLoader = { DependencyGraphModel(1, mapOf(":moduleA" to listOf(extDep))) },
            )
        val predExtSingle =
            ModulesRuleBuilder(
                extGraph,
            ).that().dependOnExternalLibrary("com.example:lib").getThatPredicate()!!
        assertTrue(predExtSingle(moduleA))

        val predExtVararg =
            ModulesRuleBuilder(
                extGraph,
            ).that().dependOnExternalLibraries("com.example:lib").getThatPredicate()!!
        assertTrue(predExtVararg(moduleA))

        // Reside / Contain package aliases
        val predResideMod = ModulesRuleBuilder(graph).that().resideInAModule(":moduleA").getThatPredicate()!!
        assertTrue(predResideMod(moduleA))

        val predResideModList =
            ModulesRuleBuilder(
                graph,
            ).that().resideInAModule(listOf(":moduleA")).getThatPredicate()!!
        assertTrue(predResideModList(moduleA))

        val predResideModVararg =
            ModulesRuleBuilder(
                graph,
            ).that().resideInAModule(":moduleA", ":moduleB").getThatPredicate()!!
        assertTrue(predResideModVararg(moduleA))

        val predResideSingle = ModulesRuleBuilder(graph).that().resideInModule(":moduleA").getThatPredicate()!!
        assertTrue(predResideSingle(moduleA))

        val predResideList = ModulesRuleBuilder(graph).that().resideInModules(listOf(":moduleA")).getThatPredicate()!!
        assertTrue(predResideList(moduleA))

        val predResideVararg = ModulesRuleBuilder(graph).that().resideInModules(":moduleA").getThatPredicate()!!
        assertTrue(predResideVararg(moduleA))

        val predPkgSingle = ModulesRuleBuilder(graph).that().containPackage("com.example").getThatPredicate()!!
        assertTrue(predPkgSingle(moduleA))

        val predPkgList = ModulesRuleBuilder(graph).that().containPackage(listOf("com.example")).getThatPredicate()!!
        assertTrue(predPkgList(moduleA))

        val predPkgVararg =
            ModulesRuleBuilder(
                graph,
            ).that().containPackage("com.example", "com.other").getThatPredicate()!!
        assertTrue(predPkgVararg(moduleA))

        val predResidePkgSingle = ModulesRuleBuilder(graph).that().resideInAPackage("com.example").getThatPredicate()!!
        assertTrue(predResidePkgSingle(moduleA))

        val predResidePkgList =
            ModulesRuleBuilder(
                graph,
            ).that().resideInAPackage(listOf("com.example")).getThatPredicate()!!
        assertTrue(predResidePkgList(moduleA))

        val predResidePkgVararg = ModulesRuleBuilder(graph).that().resideInAPackage("com.example").getThatPredicate()!!
        assertTrue(predResidePkgVararg(moduleA))
    }
}
