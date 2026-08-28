/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Dependency
import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ShortestPathFinderTest {
    @Test
    fun `direct path returns two nodes`() {
        val graph =
            mapOf(
                ":app" to listOf(":core:model"),
                ":core:model" to emptyList(),
            )

        val path = ShortestPathFinder.findShortestPath(":app", ":core:model") { graph[it].orEmpty() }
        assertEquals(listOf(":app", ":core:model"), path)
    }

    @Test
    fun `transitive path returns full path from origin to target`() {
        val graph =
            mapOf(
                ":app" to listOf(":feature:login"),
                ":feature:login" to listOf(":core:network"),
                ":core:network" to listOf(":core:database"),
                ":core:database" to emptyList(),
            )

        val path = ShortestPathFinder.findShortestPath(":app", ":core:database") { graph[it].orEmpty() }
        assertEquals(listOf(":app", ":feature:login", ":core:network", ":core:database"), path)
    }

    @Test
    fun `returns null when no path exists`() {
        val graph =
            mapOf(
                ":app" to listOf(":feature:login"),
                ":feature:login" to emptyList(),
                ":core:database" to emptyList(),
            )

        val path = ShortestPathFinder.findShortestPath(":app", ":core:database") { graph[it].orEmpty() }
        assertNull(path)
    }

    @Test
    fun `start equals target returns single node`() {
        val path = ShortestPathFinder.findShortestPath(":app", ":app") { emptyList() }
        assertEquals(listOf(":app"), path)
    }

    @Test
    fun `cycle safety avoids infinite loop and finds target`() {
        val graph =
            mapOf(
                ":a" to listOf(":b"),
                ":b" to listOf(":c", ":a"),
                ":c" to listOf(":d", ":b"),
                ":d" to listOf(":a"),
            )

        val path = ShortestPathFinder.findShortestPath(":a", ":d") { graph[it].orEmpty() }
        assertEquals(listOf(":a", ":b", ":c", ":d"), path)
    }

    @Test
    fun `deterministic tie-breaking chooses alphabetically lowest neighbor sequence`() {
        // Both paths :app -> :beta -> :core and :app -> :alpha -> :core have length 3
        val graph =
            mapOf(
                ":app" to listOf(":beta", ":alpha"),
                ":alpha" to listOf(":core"),
                ":beta" to listOf(":core"),
                ":core" to emptyList(),
            )

        val path = ShortestPathFinder.findShortestPath(":app", ":core") { graph[it].orEmpty() }
        assertEquals(listOf(":app", ":alpha", ":core"), path)
    }

    @Test
    fun `findShortestModulePath with ProjectGraph`() {
        val app =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = listOf(Dependency("implementation", ":", ":feature:onboarding")),
            )
        val feature =
            Module(
                buildId = ":",
                path = ":feature:onboarding",
                projectDir = "feature/onboarding",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = listOf(Dependency("api", ":", ":core:database")),
            )
        val db =
            Module(
                buildId = ":",
                path = ":core:database",
                projectDir = "core/database",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(app, feature, db)))

        val path = ShortestPathFinder.findShortestModulePath(graph, app, ":core:database")
        assertEquals(listOf(":app", ":feature:onboarding", ":core:database"), path)
    }

    @Test
    fun `findShortestModulePathMatching with predicate matches glob targets`() {
        val app =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = listOf(Dependency("implementation", ":", ":feature:home")),
            )
        val home =
            Module(
                buildId = ":",
                path = ":feature:home",
                projectDir = "feature/home",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = listOf(Dependency("implementation", ":", ":legacy:db")),
            )
        val legacy =
            Module(
                buildId = ":",
                path = ":legacy:db",
                projectDir = "legacy/db",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(app, home, legacy)))

        val path =
            ShortestPathFinder.findShortestModulePathMatching(
                graph = graph,
                startModule = app,
                targetPredicate = { PatternMatchers.matchesModuleGlob(":legacy:*", it.path) },
            )
        assertEquals(listOf(":app", ":feature:home", ":legacy:db"), path)
    }
}
