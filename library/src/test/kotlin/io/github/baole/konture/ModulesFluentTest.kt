/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ModulesFluentTest : KontureScopeTestFixture() {
    @Test
    fun `test ModulesRuleBuilder that and should fluent extensions`() {
        val module =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = emptyList(),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(module)))

        val rule =
            ModulesRuleBuilder(graph)
                .that { path == ":app" }
                .should {
                    check(appliedPlugins.contains("kotlin"), "Must apply kotlin plugin")
                }

        val thatPred = rule.getThatPredicate()!!
        assertTrue(thatPred(module))

        val violations = mutableListOf<String>()
        rule.getShouldAssertion()!!(module, graph, violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `test ModuleShouldContext property accessors and check helpers`() {
        val module =
            Module(
                buildId = ":build",
                path = ":feature",
                projectDir = "feature",
                appliedPlugins = listOf("kotlin-android", "hilt"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileA),
            )
        val graph = ProjectGraph(mapOf(":build" to listOf(module)))

        val violations = mutableListOf<String>()
        val context = ModuleShouldContext(module, graph, violations)

        assertEquals(":build", context.buildId)
        assertEquals(":feature", context.path)
        assertEquals("feature", context.projectDir)
        assertEquals(listOf("kotlin-android", "hilt"), context.appliedPlugins)
        assertTrue(context.sourceSets.isEmpty())
        assertTrue(context.dependencies.isEmpty())
        assertEquals(listOf(fileA), context.files)
        assertEquals(listOf(classA), context.classes)

        context.addViolation("Module error")
        assertEquals(1, violations.size)
        assertEquals("Module error", violations[0])

        violations.clear()
        context.check(false)
        assertEquals(1, violations.size)

        violations.clear()
        context.check(false, "Failed custom condition")
        assertEquals("Failed custom condition", violations[0])
    }
}
