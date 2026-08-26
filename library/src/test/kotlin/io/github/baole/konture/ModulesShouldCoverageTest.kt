/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.DependencyGraphModel
import io.github.baole.konture.core.ResolvedDependencyModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModulesShouldCoverageTest : RuleBuildersTestBase() {
    @Test
    fun `test ModulesShould notDependOnModule variants`() {
        val dep = Dependency("implementation", ":", ":moduleB")
        val modAWithDep = moduleA.copy(dependencies = listOf(dep))
        val graph = ProjectGraph(mapOf(":" to listOf(modAWithDep, moduleB)))

        // List overload
        val violationsList = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOnModule(listOf(":moduleB", ":moduleC"))
            .getShouldAssertion()!!(modAWithDep, graph, violationsList)
        assertEquals(1, violationsList.size)

        // Vararg overload
        val violationsVararg = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOnModule(":moduleB", ":moduleC")
            .getShouldAssertion()!!(modAWithDep, graph, violationsVararg)
        assertEquals(1, violationsVararg.size)

        // Predicate overload
        val violationsPred = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOnModule("starts with B") { it.startsWith(":moduleB") }
            .getShouldAssertion()!!(modAWithDep, graph, violationsPred)
        assertEquals(1, violationsPred.size)
    }

    @Test
    fun `test ModulesShould onlyDependOnModules variants`() {
        val depB = Dependency("implementation", ":", ":moduleB")
        val depC = Dependency("implementation", ":", ":moduleC")
        val modAWithDeps = moduleA.copy(dependencies = listOf(depB, depC))
        val graph = ProjectGraph(mapOf(":" to listOf(modAWithDeps, moduleB, moduleC)))

        // String overload
        val v1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyDependOnModules(":moduleB")
            .getShouldAssertion()!!(modAWithDeps, graph, v1)
        assertEquals(1, v1.size)

        // Vararg overload
        val v2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyDependOnModules(":moduleB", ":moduleC")
            .getShouldAssertion()!!(modAWithDeps, graph, v2)
        assertTrue(v2.isEmpty())

        // Predicate overload
        val v3 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyDependOnModules { it == ":moduleB" }
            .getShouldAssertion()!!(modAWithDeps, graph, v3)
        assertEquals(1, v3.size)
    }

    @Test
    fun `test ModulesShould onlyBeDependedOnBy variants`() {
        val depA = Dependency("implementation", ":", ":moduleB")
        val modAWithDep = moduleA.copy(dependencies = listOf(depA))
        val graph = ProjectGraph(mapOf(":" to listOf(modAWithDep, moduleB)))

        // B is depended on by A.
        val v1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyBeDependedOnBy(":moduleC")
            .getShouldAssertion()!!(moduleB, graph, v1)
        assertEquals(1, v1.size)

        val v2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyBeDependedOnBy(":moduleA", ":moduleC")
            .getShouldAssertion()!!(moduleB, graph, v2)
        assertTrue(v2.isEmpty())

        val v3 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().onlyBeDependedOnBy { it == ":moduleC" }
            .getShouldAssertion()!!(moduleB, graph, v3)
        assertEquals(1, v3.size)

        // Verify test configurations like testImplementation or test scope are ignored
        val testDep = Dependency("testImplementation", ":", ":moduleB")
        val modWithTestDep = moduleA.copy(dependencies = listOf(testDep))
        val graphWithTestDepOnly = ProjectGraph(mapOf(":" to listOf(modWithTestDep, moduleB)))

        val v4 = mutableListOf<String>()
        ModulesRuleBuilder(graphWithTestDepOnly).should().onlyBeDependedOnBy(":moduleC")
            .getShouldAssertion()!!(moduleB, graphWithTestDepOnly, v4)
        assertTrue(v4.isEmpty(), "Test-only dependency should not trigger onlyBeDependedOnBy violation")

        val v5 = mutableListOf<String>()
        ModulesRuleBuilder(graphWithTestDepOnly).should().onlyBeDependedOnBy { it == ":moduleC" }
            .getShouldAssertion()!!(moduleB, graphWithTestDepOnly, v5)
        assertTrue(v5.isEmpty(), "Test-only dependency should not trigger onlyBeDependedOnBy predicate violation")
    }

    @Test
    fun `test ModulesShould nested assertions satisfy anyOf allOf noneOf`() {
        val graph = ProjectGraph(mapOf(":" to listOf(moduleA)))

        // satisfy
        val vSatisfy = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().satisfy("custom description")
            .getShouldAssertion()!!(moduleA, graph, vSatisfy)
        assertEquals(1, vSatisfy.size)

        val vSatisfyLambda = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().satisfy { _, violations -> violations.add("custom violation") }
            .getShouldAssertion()!!(moduleA, graph, vSatisfyLambda)
        assertEquals(1, vSatisfyLambda.size)

        // anyOf
        val vAnyOfPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().anyOf(
            { applyPlugin("kotlin") },
            { applyPlugin("nonexistent") },
        ).getShouldAssertion()!!(moduleA, graph, vAnyOfPass)
        assertTrue(vAnyOfPass.isEmpty())

        val vAnyOfFail = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().anyOf(
            { applyPlugin("java") },
            { applyPlugin("nonexistent") },
        ).getShouldAssertion()!!(moduleA, graph, vAnyOfFail)
        assertEquals(1, vAnyOfFail.size)

        // allOf
        val vAllOfPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().allOf(
            { applyPlugin("kotlin") },
        ).getShouldAssertion()!!(moduleA, graph, vAllOfPass)
        assertTrue(vAllOfPass.isEmpty())

        // noneOf
        val vNoneOfPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().noneOf(
            { applyPlugin("java") },
        ).getShouldAssertion()!!(moduleA, graph, vNoneOfPass)
        assertTrue(vNoneOfPass.isEmpty())

        val vNoneOfFail = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().noneOf(
            { applyPlugin("kotlin") },
        ).getShouldAssertion()!!(moduleA, graph, vNoneOfFail)
        assertEquals(1, vNoneOfFail.size)
    }

    @Test
    fun `test ModulesShould external libraries and cycles`() {
        val extDep = ResolvedDependencyModel("com.example", "lib", "1.0", "implementation", false)
        val extGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA)),
                externalDependenciesLoader = { DependencyGraphModel(1, mapOf(":moduleA" to listOf(extDep))) },
            )

        val vNotExt = mutableListOf<String>()
        ModulesRuleBuilder(extGraph).should().notDependOnExternalLibraries("com.example:*")
            .getShouldAssertion()!!(moduleA, extGraph, vNotExt)
        assertEquals(1, vNotExt.size)

        val vNotExtList = mutableListOf<String>()
        ModulesRuleBuilder(extGraph).should().notDependOnExternalLibraries(listOf("com.example:*"))
            .getShouldAssertion()!!(moduleA, extGraph, vNotExtList)
        assertEquals(1, vNotExtList.size)

        val vOnlyExt = mutableListOf<String>()
        ModulesRuleBuilder(extGraph).should().onlyDependOnExternalLibraries("org.other:*")
            .getShouldAssertion()!!(moduleA, extGraph, vOnlyExt)
        assertEquals(1, vOnlyExt.size)

        val vOnlyExtList = mutableListOf<String>()
        ModulesRuleBuilder(extGraph).should().onlyDependOnExternalLibraries(listOf("org.other:*"))
            .getShouldAssertion()!!(moduleA, extGraph, vOnlyExtList)
        assertEquals(1, vOnlyExtList.size)

        val vDepExt = mutableListOf<String>()
        ModulesRuleBuilder(extGraph).should().dependOnExternalLibrary("com.example:lib")
            .getShouldAssertion()!!(moduleA, extGraph, vDepExt)
        assertTrue(vDepExt.isEmpty())

        val vDepExtList = mutableListOf<String>()
        ModulesRuleBuilder(extGraph).should().dependOnExternalLibraries(listOf("com.example:lib"))
            .getShouldAssertion()!!(moduleA, extGraph, vDepExtList)
        assertTrue(vDepExtList.isEmpty())

        // Cycles
        val mod1 = Module(":", ":1", "1", emptyList(), emptyList(), listOf(Dependency("impl", ":", ":2")), emptyList())
        val mod2 = Module(":", ":2", "2", emptyList(), emptyList(), listOf(Dependency("impl", ":", ":1")), emptyList())
        val cycleGraph = ProjectGraph(mapOf(":" to listOf(mod1, mod2)))
        val vCycle = mutableListOf<String>()
        ModulesRuleBuilder(cycleGraph).should().beFreeOfCycles()
            .getShouldAssertion()!!(mod1, cycleGraph, vCycle)
        assertEquals(1, vCycle.size)
    }

    @Test
    fun `test ModulesShould plugins sourceSets and structure`() {
        val graph = ProjectGraph(mapOf(":" to listOf(moduleA)))

        // Plugins
        val vPlugin1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().havePlugins("kotlin")
            .getShouldAssertion()!!(moduleA, graph, vPlugin1)
        assertTrue(vPlugin1.isEmpty())

        val vPlugin2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().havePlugins(listOf("java"))
            .getShouldAssertion()!!(moduleA, graph, vPlugin2)
        assertEquals(1, vPlugin2.size)

        val vNotPlugin1 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHavePlugin("kotlin")
            .getShouldAssertion()!!(moduleA, graph, vNotPlugin1)
        assertEquals(1, vNotPlugin1.size)

        val vNotPlugin2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHavePlugins("kotlin")
            .getShouldAssertion()!!(moduleA, graph, vNotPlugin2)
        assertEquals(1, vNotPlugin2.size)

        val vNotPlugin3 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHavePlugins(listOf("java"))
            .getShouldAssertion()!!(moduleA, graph, vNotPlugin3)
        assertTrue(vNotPlugin3.isEmpty())

        // SourceSets & Structure
        val modWithSS = moduleA.copy(sourceSets = listOf(SourceSet("main", "KOTLIN_JVM", true, emptyList())))
        val vSS = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().haveSourceSet("main")
            .getShouldAssertion()!!(modWithSS, graph, vSS)
        assertTrue(vSS.isEmpty())

        val vSS2 = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().haveSourceSets("main", "test")
            .getShouldAssertion()!!(modWithSS, graph, vSS2)
        assertEquals(1, vSS2.size)

        val vSSList = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().haveSourceSets(listOf("main"))
            .getShouldAssertion()!!(modWithSS, graph, vSSList)
        assertTrue(vSSList.isEmpty())

        // containClasses / containFiles / beEmpty
        val emptyMod = Module(":", ":empty", "empty", emptyList(), emptyList(), emptyList(), emptyList())
        val vContainCls = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().containClasses()
            .getShouldAssertion()!!(emptyMod, graph, vContainCls)
        assertEquals(1, vContainCls.size)

        val vNotContainCls = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notContainClasses()
            .getShouldAssertion()!!(moduleA, graph, vNotContainCls)
        assertEquals(1, vNotContainCls.size)

        val vContainFiles = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().containFiles()
            .getShouldAssertion()!!(emptyMod, graph, vContainFiles)
        assertEquals(1, vContainFiles.size)

        val vBeEmpty = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().beEmpty()
            .getShouldAssertion()!!(moduleA, graph, vBeEmpty)
        assertEquals(1, vBeEmpty.size)
    }

    @Test
    fun `test ModulesShould dependencies configuration and transitive`() {
        val depApi = Dependency("api", ":", ":moduleB")
        val depImpl = Dependency("implementation", ":", ":moduleC")
        val modWithConfig = moduleA.copy(dependencies = listOf(depApi, depImpl))

        val mA = Module(":", ":A", "A", emptyList(), emptyList(), listOf(Dependency("impl", ":", ":B")), emptyList())
        val mB = Module(":", ":B", "B", emptyList(), emptyList(), listOf(Dependency("impl", ":", ":C")), emptyList())
        val mC = Module(":", ":C", "C", emptyList(), emptyList(), emptyList(), emptyList())
        val graph = ProjectGraph(mapOf(":" to listOf(mA, mB, mC)))

        // Configuration depend
        val vApi = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().dependOnModuleApi(":moduleB")
            .getShouldAssertion()!!(modWithConfig, graph, vApi)
        assertTrue(vApi.isEmpty())

        val vImpl = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().dependOnModuleImplementation(":moduleC")
            .getShouldAssertion()!!(modWithConfig, graph, vImpl)
        assertTrue(vImpl.isEmpty())

        val vNotConfig = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOnModuleViaConfiguration(":moduleB", "api")
            .getShouldAssertion()!!(modWithConfig, graph, vNotConfig)
        assertEquals(1, vNotConfig.size)

        // Transitive
        val vTransPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().dependOnModuleTransitively(":C")
            .getShouldAssertion()!!(mA, graph, vTransPass)
        assertTrue(vTransPass.isEmpty())

        val vTransFail = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().dependOnModuleTransitively(":D")
            .getShouldAssertion()!!(mA, graph, vTransFail)
        assertEquals(1, vTransFail.size)

        val vNotTransPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOnModuleTransitively(":D")
            .getShouldAssertion()!!(mA, graph, vNotTransPass)
        assertTrue(vNotTransPass.isEmpty())

        val vNotTransFail = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notDependOnModuleTransitively(":C")
            .getShouldAssertion()!!(mA, graph, vNotTransFail)
        assertEquals(1, vNotTransFail.size)
    }

    @Test
    fun `test ModulesShould properties and class checks`() {
        val graph = ProjectGraph(mapOf(":" to listOf(moduleA)))

        // beStandalone / beLeafModule
        val vStandPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().beStandalone()
            .getShouldAssertion()!!(moduleA, graph, vStandPass)
        assertTrue(vStandPass.isEmpty())

        val vLeafPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().beLeafModule()
            .getShouldAssertion()!!(moduleA, graph, vLeafPass)
        assertTrue(vLeafPass.isEmpty())

        // BuildId & ProjectDir
        val vBuildIdPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().haveBuildId(":")
            .getShouldAssertion()!!(moduleA, graph, vBuildIdPass)
        assertTrue(vBuildIdPass.isEmpty())

        val vBuildIdFail = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveBuildId(":")
            .getShouldAssertion()!!(moduleA, graph, vBuildIdFail)
        assertEquals(1, vBuildIdFail.size)

        val vDirPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().haveProjectDir("moduleA")
            .getShouldAssertion()!!(moduleA, graph, vDirPass)
        assertTrue(vDirPass.isEmpty())

        val vDirFail = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveProjectDir("moduleA")
            .getShouldAssertion()!!(moduleA, graph, vDirFail)
        assertEquals(1, vDirFail.size)

        // Class and Package checks
        val vPkgPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().containClassesInPackage("com.example")
            .getShouldAssertion()!!(moduleA, graph, vPkgPass)
        assertTrue(vPkgPass.isEmpty())

        val vPkgNot = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notContainClassesInPackage("com.example")
            .getShouldAssertion()!!(moduleA, graph, vPkgNot)
        assertEquals(1, vPkgNot.size)

        val vAnnotPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().containClassesWithAnnotation("com.example.MyAnnotation")
            .getShouldAssertion()!!(moduleB, graph, vAnnotPass)
        assertTrue(vAnnotPass.isEmpty())

        val vAnnotNot = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notContainClassesWithAnnotation("com.example.MyAnnotation")
            .getShouldAssertion()!!(moduleB, graph, vAnnotNot)
        assertEquals(1, vAnnotNot.size)

        val vClsPass = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().containClass("com.example.ClassA")
            .getShouldAssertion()!!(moduleA, graph, vClsPass)
        assertTrue(vClsPass.isEmpty())

        val vClsNot = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notContainClass("com.example.ClassA")
            .getShouldAssertion()!!(moduleA, graph, vClsNot)
        assertEquals(1, vClsNot.size)
    }

    @Test
    fun `test ModulesShould name negation assertions`() {
        val graph = ProjectGraph(mapOf(":" to listOf(moduleA)))

        val vNameSingle = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveName(":moduleA")
            .getShouldAssertion()!!(moduleA, graph, vNameSingle)
        assertEquals(1, vNameSingle.size)

        val vNameList = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveName(listOf(":moduleA"))
            .getShouldAssertion()!!(moduleA, graph, vNameList)
        assertEquals(1, vNameList.size)

        val vNameVararg = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveName(":moduleA", ":moduleB")
            .getShouldAssertion()!!(moduleA, graph, vNameVararg)
        assertEquals(1, vNameVararg.size)

        val vStart = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameStartingWith("mod")
            .getShouldAssertion()!!(moduleA, graph, vStart)
        assertEquals(1, vStart.size)

        val vStartList = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameStartingWith(listOf("mod"))
            .getShouldAssertion()!!(moduleA, graph, vStartList)
        assertEquals(1, vStartList.size)

        val vStartVararg = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameStartingWith("mod", "app")
            .getShouldAssertion()!!(moduleA, graph, vStartVararg)
        assertEquals(1, vStartVararg.size)

        val vEnd = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameEndingWith("leA")
            .getShouldAssertion()!!(moduleA, graph, vEnd)
        assertEquals(1, vEnd.size)

        val vEndList = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameEndingWith(listOf("leA"))
            .getShouldAssertion()!!(moduleA, graph, vEndList)
        assertEquals(1, vEndList.size)

        val vEndVararg = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameEndingWith("leA", "leB")
            .getShouldAssertion()!!(moduleA, graph, vEndVararg)
        assertEquals(1, vEndVararg.size)

        val vMatch = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameMatching(":mod*")
            .getShouldAssertion()!!(moduleA, graph, vMatch)
        assertEquals(1, vMatch.size)

        val vMatchList = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameMatching(listOf(":mod*"))
            .getShouldAssertion()!!(moduleA, graph, vMatchList)
        assertEquals(1, vMatchList.size)

        val vMatchVararg = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notHaveNameMatching(":mod*", ":app*")
            .getShouldAssertion()!!(moduleA, graph, vMatchVararg)
        assertEquals(1, vMatchVararg.size)
    }

    @Test
    fun `test ModulesShould call and reference assertions`() {
        val usageCall =
            SourceUsage(UsageKind.CALL, "com.example.Target.foo", "Test.kt", 1, 1, rawExpression = "Target.foo()")
        val usageRef =
            SourceUsage(
                UsageKind.CLASS_REFERENCE,
                "com.example.TargetClass",
                "Test.kt",
                2,
                1,
                rawExpression = "TargetClass::class",
            )
        val fileWithUsages = FileDeclaration("Test.kt", "com.example", usages = listOf(usageCall, usageRef))
        val modWithFile = moduleA.copy(files = listOf(fileWithUsages))
        val graph = ProjectGraph(mapOf(":" to listOf(modWithFile)))

        val vCall = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notCall("com.example.Target.foo")
            .getShouldAssertion()!!(modWithFile, graph, vCall)
        assertEquals(1, vCall.size)

        val vRef = mutableListOf<String>()
        ModulesRuleBuilder(graph).should().notReferenceClass("com.example.TargetClass")
            .getShouldAssertion()!!(modWithFile, graph, vRef)
        assertEquals(1, vRef.size)
    }
}
