/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModulesRuleBuilderTest : RuleBuildersTestBase() {
    @Test
    fun `test ModulesRuleBuilder print matched and print all modules`() {
        var printMatchedCount = 0
        var printAllCount = 0

        val builder =
            ModulesRuleBuilder(projectGraph)
                .printMatchedModules { printMatchedCount++ }
                .printAllModules { printAllCount++ }

        assertEquals(3, printAllCount) // projectGraph from RuleBuildersTestBase has 3 modules

        val module = projectGraph.getAllModules().first()
        val violations = mutableListOf<String>()
        builder.getShouldAssertion()!!(module, projectGraph, violations)
        assertEquals(1, printMatchedCount)
    }

    @Test
    fun `test ModulesRuleBuilder allowEmpty and empty check exception`() {
        val emptyGraph = ProjectGraph(emptyMap())

        val builderNoEmpty =
            ModulesRuleBuilder(
                emptyGraph,
            ).that().haveNamePath(":nonexistent").should().notDependOnModule(":core")
        assertThrows(AssertionError::class.java) {
            builderNoEmpty.check()
        }

        val builderAllowEmpty =
            ModulesRuleBuilder(
                emptyGraph,
            ).allowEmpty().that().haveNamePath(":nonexistent").should().notDependOnModule(":core")
        // No assertion error should be thrown when allowEmpty is enabled
        builderAllowEmpty.check()
    }

    @Test
    fun `test ModulesRuleBuilder logical that operators and or xor not`() {
        val moduleA = Module(":", ":app", "app", listOf("kotlin"), emptyList(), emptyList(), emptyList())
        val moduleB = Module(":", ":core", "core", listOf("java"), emptyList(), emptyList(), emptyList())

        // AND
        val builderAnd = ModulesRuleBuilder().that().haveNamePath(":app").and().haveNameMatching(":app*")
        val predAnd = builderAnd.getThatPredicate()!!
        assertTrue(predAnd(moduleA))
        assertFalse(predAnd(moduleB))

        // OR
        val builderOr = ModulesRuleBuilder().that().haveNamePath(":app").or().haveNamePath(":core")
        val predOr = builderOr.getThatPredicate()!!
        assertTrue(predOr(moduleA))
        assertTrue(predOr(moduleB))

        // XOR
        val builderXor = ModulesRuleBuilder().that().haveNamePath(":app").xor().haveNameMatching(":app*")
        val predXor = builderXor.getThatPredicate()!!
        assertFalse(predXor(moduleA)) // true xor true -> false
        assertFalse(predXor(moduleB)) // false xor false -> false

        // NOT
        val builderNot = ModulesRuleBuilder().not().haveNamePath(":app")
        val predNot = builderNot.getThatPredicate()!!
        assertFalse(predNot(moduleA))
        assertTrue(predNot(moduleB))
    }

    @Test
    fun `test ModulesRuleBuilder logical should operators andShould orShould xorShould notShould`() {
        val module = Module(":", ":app", "app", listOf("kotlin"), emptyList(), emptyList(), emptyList())
        val graph = ProjectGraph(mapOf(":" to listOf(module)))

        // andShould
        val builderAnd =
            ModulesRuleBuilder(
                graph,
            ).should().notDependOnModule(":core").andShould().notDependOnModule(":feature")
        val violationsAnd = mutableListOf<String>()
        builderAnd.getShouldAssertion()!!(module, graph, violationsAnd)
        assertTrue(violationsAnd.isEmpty())

        // orShould
        val builderOr =
            ModulesRuleBuilder(
                graph,
            ).should().notDependOnModule(":core").orShould().notDependOnModule(":app")
        val violationsOr = mutableListOf<String>()
        builderOr.getShouldAssertion()!!(module, graph, violationsOr)
        assertTrue(violationsOr.isEmpty())

        // xorShould
        val builderXorPass = ModulesRuleBuilder(graph).should().notDependOnModule(":core").xorShould().satisfy { false }
        val violationsXorPass = mutableListOf<String>()
        builderXorPass.getShouldAssertion()!!(module, graph, violationsXorPass)
        assertTrue(violationsXorPass.isEmpty())

        val builderXorFail =
            ModulesRuleBuilder(
                graph,
            ).should().notDependOnModule(":core").xorShould().notDependOnModule(":feature")
        val violationsXorFail = mutableListOf<String>()
        builderXorFail.getShouldAssertion()!!(module, graph, violationsXorFail)
        assertEquals(1, violationsXorFail.size)

        // notShould
        val builderNotPass = ModulesRuleBuilder(graph).notShould().satisfy { false }
        val violationsNotPass = mutableListOf<String>()
        builderNotPass.getShouldAssertion()!!(module, graph, violationsNotPass)
        assertTrue(violationsNotPass.isEmpty())

        val builderNotFail = ModulesRuleBuilder(graph).notShould().notDependOnModule(":core")
        val violationsNotFail = mutableListOf<String>()
        builderNotFail.getShouldAssertion()!!(module, graph, violationsNotFail)
        assertEquals(1, violationsNotFail.size)
    }

    @Test
    fun `test ModulesRuleBuilder check execution success and failure`() {
        val module = Module(":", ":app", "app", listOf("kotlin"), emptyList(), emptyList(), emptyList())
        val graph = ProjectGraph(mapOf(":" to listOf(module)))

        // Success
        ModulesRuleBuilder(graph).that().haveNamePath(":app").should().notDependOnModule(":core").check()

        // Missing assertion rule exception
        val noAssertionBuilder = ModulesRuleBuilder(graph).that().haveNamePath(":app")
        assertThrows(AssertionError::class.java) {
            noAssertionBuilder.check()
        }
    }
}
