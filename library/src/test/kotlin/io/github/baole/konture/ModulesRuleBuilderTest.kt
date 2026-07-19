/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ModulesRuleBuilderTest : RuleBuildersTestBase() {
    @Test
    fun `test modules rule builder logical and - or - xor - not filtering`() {
        // 1. name starts with :module AND applied plugin kotlin
        val rule1 =
            ModulesRuleBuilder(projectGraph)
                .that()
                .haveNameMatching(":module*")
                .and()
                .matching { it.appliedPlugins.contains("kotlin") }
        val pred1 = rule1.getThatPredicate()!!
        assertTrue(pred1(moduleA))
        assertTrue(pred1(moduleB))
        assertFalse(pred1(moduleC))

        // 2. name is :moduleA OR name is :moduleC
        val rule2 =
            ModulesRuleBuilder(projectGraph)
                .that()
                .haveNamePath(":moduleA")
                .or()
                .haveNamePath(":moduleC")
        val pred2 = rule2.getThatPredicate()!!
        assertTrue(pred2(moduleA))
        assertFalse(pred2(moduleB))
        assertTrue(pred2(moduleC))

        // 2b. name is in list of (:moduleA, :moduleC)
        val rule2List =
            ModulesRuleBuilder(projectGraph)
                .that()
                .haveNamePath(listOf(":moduleA", ":moduleC"))
        val pred2List = rule2List.getThatPredicate()!!
        assertTrue(pred2List(moduleA))
        assertFalse(pred2List(moduleB))
        assertTrue(pred2List(moduleC))

        // 2c. name is in vararg (:moduleA, :moduleC)
        val rule2Vararg =
            ModulesRuleBuilder(projectGraph)
                .that()
                .haveNamePath(":moduleA", ":moduleC")
        val pred2Vararg = rule2Vararg.getThatPredicate()!!
        assertTrue(pred2Vararg(moduleA))
        assertFalse(pred2Vararg(moduleB))
        assertTrue(pred2Vararg(moduleC))

        // 3. name path starts with :module XOR applied plugin java (A: kotlin, B: kotlin+java, C: none)
        // moduleA: starts with :module (T) XOR has java (F) -> T
        // moduleB: starts with :module (T) XOR has java (T) -> F
        // moduleC: starts with :module (T) XOR has java (F) -> T
        val rule3 =
            ModulesRuleBuilder(projectGraph)
                .that()
                .haveNameMatching(":module*")
                .xor()
                .matching { it.appliedPlugins.contains("java") }
        val pred3 = rule3.getThatPredicate()!!
        assertTrue(pred3(moduleA))
        assertFalse(pred3(moduleB))
        assertTrue(pred3(moduleC))

        // 4. NOT name is :moduleA
        val rule4 =
            ModulesRuleBuilder(projectGraph)
                .not()
                .haveNamePath(":moduleA")
        val pred4 = rule4.getThatPredicate()!!
        assertFalse(pred4(moduleA))
        assertTrue(pred4(moduleB))
        assertTrue(pred4(moduleC))
    }

    @Test
    fun `test modules rule builder satisfy filtering`() {
        val rule =
            ModulesRuleBuilder(projectGraph)
                .that()
                .satisfy { it.appliedPlugins.size == 2 }
        val pred = rule.getThatPredicate()!!
        assertFalse(pred(moduleA))
        assertTrue(pred(moduleB))
        assertFalse(pred(moduleC))
    }

    @Test
    fun `test modules rule builder anyOf - allOf - noneOf filtering`() {
        // anyOf: starts with :moduleA or starts with :moduleB
        val ruleAny =
            ModulesRuleBuilder(projectGraph)
                .that()
                .anyOf(
                    { haveNamePath(":moduleA") },
                    { haveNamePath(":moduleB") },
                )
        val predAny = ruleAny.getThatPredicate()!!
        assertTrue(predAny(moduleA))
        assertTrue(predAny(moduleB))
        assertFalse(predAny(moduleC))

        // allOf: starts with :module AND has plugin java
        val ruleAll =
            ModulesRuleBuilder(projectGraph)
                .that()
                .allOf(
                    { haveNameMatching(":module*") },
                    { matching { it.appliedPlugins.contains("java") } },
                )
        val predAll = ruleAll.getThatPredicate()!!
        assertFalse(predAll(moduleA))
        assertTrue(predAll(moduleB))
        assertFalse(predAll(moduleC))

        // noneOf: neither starts with :moduleA nor has plugin java
        val ruleNone =
            ModulesRuleBuilder(projectGraph)
                .that()
                .noneOf(
                    { haveNamePath(":moduleA") },
                    { matching { it.appliedPlugins.contains("java") } },
                )
        val predNone = ruleNone.getThatPredicate()!!
        assertFalse(predNone(moduleA))
        assertFalse(predNone(moduleB))
        assertTrue(predNone(moduleC))
    }

    @Test
    fun `test modules rule builder assertions logical and - or - xor - not`() {
        // Satisfy check 1 AND satisfy check 2
        val rule1 =
            ModulesRuleBuilder(projectGraph)
                .should()
                .satisfy { mod, violations ->
                    if (mod.appliedPlugins.isEmpty()) {
                        violations.add("no plugins")
                    }
                }.andShould()
                .satisfy { mod, violations ->
                    if (!mod.path.endsWith("A")) {
                        violations.add("not A")
                    }
                }

        val assertion1 = rule1.getShouldAssertion()!!

        // moduleA has 1 plugin, path ends with A -> passes both
        val vA = mutableListOf<String>()
        assertion1(moduleA, projectGraph, vA)
        assertTrue(vA.isEmpty())

        // moduleB has 2 plugins, path ends with B -> fails second
        val vB = mutableListOf<String>()
        assertion1(moduleB, projectGraph, vB)
        assertEquals(1, vB.size)

        // moduleC has 0 plugins, path ends with C -> fails both
        val vC = mutableListOf<String>()
        assertion1(moduleC, projectGraph, vC)
        assertEquals(2, vC.size)
    }

    @Test
    fun `test modules rule builder assertions anyOf - allOf - noneOf`() {
        // anyOf: at least one assertion passes
        val ruleAny =
            ModulesRuleBuilder(projectGraph)
                .should()
                .anyOf(
                    {
                        satisfy { mod, violations ->
                            if (!mod.path.endsWith("A")) {
                                violations.add("error")
                            }
                        }
                    },
                    {
                        satisfy { mod, violations ->
                            if (!mod.path.endsWith("B")) {
                                violations.add("error")
                            }
                        }
                    },
                )
        val assertionAny = ruleAny.getShouldAssertion()!!

        val vA = mutableListOf<String>()
        assertionAny(moduleA, projectGraph, vA)
        assertTrue(vA.isEmpty()) // passes first block

        val vC = mutableListOf<String>()
        assertionAny(moduleC, projectGraph, vC)
        assertFalse(vC.isEmpty()) // fails both blocks
    }

    @Test
    fun `test modules dependencies assertions`() {
        // Prepare modules with explicit dependencies
        val depB = Dependency("implementation", ":", ":moduleB")
        val depC = Dependency("api", ":", ":moduleC")

        val modWithDeps =
            Module(
                buildId = ":",
                path = ":moduleA",
                projectDir = "moduleA",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = listOf(depB, depC),
                files = emptyList(),
            )

        val graph = ProjectGraph(mapOf(":" to listOf(modWithDeps, moduleB, moduleC)))

        // 1. notDependOnModule
        val rule1 = ModulesRuleBuilder(graph).should().notDependOnModule(":moduleC")
        val assert1 = rule1.getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assert1(modWithDeps, graph, v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("should not depend on :moduleC"))

        // 1b. notDependOnModule without leading colon normalizes path
        val rule1NoColon = ModulesRuleBuilder(graph).should().notDependOnModule("moduleC")
        val assert1NoColon = rule1NoColon.getShouldAssertion()!!
        val v1NoColon = mutableListOf<String>()
        assert1NoColon(modWithDeps, graph, v1NoColon)
        assertEquals(1, v1NoColon.size)
        assertTrue(v1NoColon[0].contains("should not depend on :moduleC"))

        // test overload with predicate
        val rule1Pred = ModulesRuleBuilder(graph).should().notDependOnModule { it.endsWith("C") }
        val assert1Pred = rule1Pred.getShouldAssertion()!!
        val v1Pred = mutableListOf<String>()
        assert1Pred(modWithDeps, graph, v1Pred)
        assertEquals(1, v1Pred.size)
        assertTrue(v1Pred[0].contains("should not depend on custom predicate"))

        // test overload with description and predicate
        val rule1Desc = ModulesRuleBuilder(graph).should().notDependOnModule("C modules") { it == ":moduleC" }
        val assert1Desc = rule1Desc.getShouldAssertion()!!
        val v1Desc = mutableListOf<String>()
        assert1Desc(modWithDeps, graph, v1Desc)
        assertEquals(1, v1Desc.size)
        assertTrue(v1Desc[0].contains("should not depend on C modules"))

        // 2. onlyDependOnModules
        val rule2 = ModulesRuleBuilder(graph).should().onlyDependOnModules(":moduleB", ":moduleC")
        val assert2 = rule2.getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assert2(modWithDeps, graph, v2)
        assertTrue(v2.isEmpty())

        val rule2Fail = ModulesRuleBuilder(graph).should().onlyDependOnModules(":moduleB")
        val assert2Fail = rule2Fail.getShouldAssertion()!!
        val v2Fail = mutableListOf<String>()
        assert2Fail(modWithDeps, graph, v2Fail)
        assertEquals(1, v2Fail.size)
        assertTrue(v2Fail[0].contains("depends on :moduleC, which is not allowed"))

        // test onlyDependOnModules with predicate
        val rule2Pred = ModulesRuleBuilder(graph).should().onlyDependOnModules { it == ":moduleB" }
        val assert2Pred = rule2Pred.getShouldAssertion()!!
        val v2Pred = mutableListOf<String>()
        assert2Pred(modWithDeps, graph, v2Pred)
        assertEquals(1, v2Pred.size)
        assertTrue(v2Pred[0].contains("not allowed by: custom predicate"))

        // test onlyDependOnModules with description
        val rule2Desc = ModulesRuleBuilder(graph).should().onlyDependOnModules("B only") { it == ":moduleB" }
        val assert2Desc = rule2Desc.getShouldAssertion()!!
        val v2Desc = mutableListOf<String>()
        assert2Desc(modWithDeps, graph, v2Desc)
        assertEquals(1, v2Desc.size)
        assertTrue(v2Desc[0].contains("not allowed by: B only"))

        // 3. onlyBeDependedOnBy
        val rule3 = ModulesRuleBuilder(graph).should().onlyBeDependedOnBy(":moduleA")
        val assert3 = rule3.getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assert3(moduleB, graph, v3) // moduleB is depended on by moduleA
        assertTrue(v3.isEmpty())

        val rule3Fail = ModulesRuleBuilder(graph).should().onlyBeDependedOnBy(":moduleX")
        val assert3Fail = rule3Fail.getShouldAssertion()!!
        val v3Fail = mutableListOf<String>()
        assert3Fail(moduleB, graph, v3Fail)
        assertEquals(1, v3Fail.size)
        assertTrue(v3Fail[0].contains("is depended on by :moduleA, which is not allowed"))

        // test onlyBeDependedOnBy with predicate
        val rule3Pred = ModulesRuleBuilder(graph).should().onlyBeDependedOnBy { it == ":moduleX" }
        val assert3Pred = rule3Pred.getShouldAssertion()!!
        val v3Pred = mutableListOf<String>()
        assert3Pred(moduleB, graph, v3Pred)
        assertEquals(1, v3Pred.size)
        assertTrue(v3Pred[0].contains("not allowed by: custom predicate"))

        // test onlyBeDependedOnBy with description
        val rule3Desc = ModulesRuleBuilder(graph).should().onlyBeDependedOnBy("Allowed modules") { it == ":moduleX" }
        val assert3Desc = rule3Desc.getShouldAssertion()!!
        val v3Desc = mutableListOf<String>()
        assert3Desc(moduleB, graph, v3Desc)
        assertEquals(1, v3Desc.size)
        assertTrue(v3Desc[0].contains("not allowed by: Allowed modules"))
    }

    @Test
    fun `test modules satisfy assertions`() {
        val ruleSatisfy1 = ModulesRuleBuilder(projectGraph).should().satisfy { it.path == ":moduleA" }
        val assertSatisfy1 = ruleSatisfy1.getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertSatisfy1(moduleA, projectGraph, v1)
        assertTrue(v1.isEmpty())

        val v1Fail = mutableListOf<String>()
        assertSatisfy1(moduleB, projectGraph, v1Fail)
        assertEquals(1, v1Fail.size)
        assertTrue(v1Fail[0].contains("should satisfy: custom condition"))

        val ruleSatisfyDesc = ModulesRuleBuilder(projectGraph).should().satisfy("is module B")
        val assertSatisfyDesc = ruleSatisfyDesc.getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertSatisfyDesc(moduleA, projectGraph, v2)
        assertEquals(1, v2.size)
        assertTrue(v2[0].contains("should satisfy: is module B"))
    }

    @Test
    fun `test external dependencies assertions`() {
        val dep1 =
            io.github.baole.konture.core.ResolvedDependencyModel(
                "org.jetbrains.kotlin",
                "kotlin-stdlib",
                "1.9.0",
                "implementation",
                isTransitive = false,
            )
        val dep2 =
            io.github.baole.konture.core.ResolvedDependencyModel(
                "com.google.guava",
                "guava",
                "32.0.0",
                "implementation",
                isTransitive = true,
            )

        val externalDeps =
            io.github.baole.konture.core.DependencyGraphModel(
                modules =
                    mapOf(
                        ":moduleA" to listOf(dep1, dep2),
                    ),
            )

        val graph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA, moduleB)),
                externalDependenciesLoader = { externalDeps },
            )

        // Test notDependOnExternalLibraries with matching patterns
        val rule1 = ModulesRuleBuilder(graph).should().notDependOnExternalLibraries("com.google.guava:*")
        val assert1 = rule1.getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assert1(moduleA, graph, v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("should not depend on external libraries [com.google.guava:*]"))

        // Test notDependOnExternalLibraries with includeTransitive = false (guava is transitive, so should pass)
        val rule2 =
            ModulesRuleBuilder(
                graph,
            ).should().notDependOnExternalLibraries("com.google.guava:*", includeTransitive = false)
        val assert2 = rule2.getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assert2(moduleA, graph, v2)
        assertTrue(v2.isEmpty())

        // Test onlyDependOnExternalLibraries violating allowed list
        val rule3 = ModulesRuleBuilder(graph).should().onlyDependOnExternalLibraries("org.jetbrains.kotlin:*")
        val assert3 = rule3.getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assert3(moduleA, graph, v3)
        assertEquals(1, v3.size)
        assertTrue(v3[0].contains("depends on external libraries not in the allowed list [org.jetbrains.kotlin:*]"))

        // Test onlyDependOnExternalLibraries with includeTransitive = false (only direct is kotlin, which is allowed)
        val rule4 =
            ModulesRuleBuilder(
                graph,
            ).should().onlyDependOnExternalLibraries("org.jetbrains.kotlin:*", includeTransitive = false)
        val assert4 = rule4.getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assert4(moduleA, graph, v4)
        assertTrue(v4.isEmpty())
    }

    @Test
    fun `external dependency assertions fail when their graph is absent`() {
        val graph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA)),
                externalDependenciesLoader = { null },
            )

        val rule = ModulesRuleBuilder(graph).should().notDependOnExternalLibraries("com.example:library")

        val failure =
            assertThrows(IllegalStateException::class.java) {
                rule.getShouldAssertion()!!.invoke(moduleA, graph, mutableListOf())
            }

        assertTrue(failure.message!!.contains("dependencies.json"))
    }
}
