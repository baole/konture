/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.DependencyGraphModel
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ProjectGraphTest {
    @Test
    fun `findModule distinguishes modules with the same path in different builds`() {
        val rootModule = module(buildId = ":", path = ":shared")
        val includedBuildModule = module(buildId = "included", path = ":shared")
        val graph =
            ProjectGraph(
                builds = mapOf(":" to listOf(rootModule), "included" to listOf(includedBuildModule)),
            )

        assertSame(rootModule, graph.findModule(":", ":shared"))
        assertSame(includedBuildModule, graph.findModule("included", ":shared"))
        assertEquals(null, graph.findModule("missing", ":shared"))
        assertEquals(listOf(rootModule, includedBuildModule), graph.getAllModules())
    }

    @Test
    fun `external dependencies are loaded only once`() {
        var loadCount = 0
        val dependencies = DependencyGraphModel()
        val graph =
            ProjectGraph(emptyMap()) {
                loadCount += 1
                dependencies
            }

        assertSame(dependencies, graph.externalDependencies)
        assertSame(dependencies, graph.externalDependencies)
        assertEquals(1, loadCount)
    }

    @Test
    fun `assertNoCycles accepts acyclic graphs and dependencies outside the graph`() {
        val api = module(buildId = ":", path = ":api")
        val feature =
            module(
                buildId = "included",
                path = ":feature",
                dependencies =
                    listOf(
                        Dependency("implementation", ":", ":api"),
                        Dependency("implementation", "missing", ":not-present"),
                    ),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(api), "included" to listOf(feature)))

        assertDoesNotThrow(graph::assertNoCycles)
    }

    @Test
    fun `assertNoCycles reports the complete cycle path`() {
        val first =
            module(
                buildId = ":",
                path = ":first",
                dependencies = listOf(Dependency("api", "included", ":second")),
            )
        val second =
            module(
                buildId = "included",
                path = ":second",
                dependencies = listOf(Dependency("implementation", ":", ":first")),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(first), "included" to listOf(second)))

        val error = assertThrows(AssertionError::class.java, graph::assertNoCycles)

        assertEquals(
            "Circular dependency detected in project graph: ::first -> included:second -> ::first",
            error.message,
        )
    }

    private fun module(
        buildId: String,
        path: String,
        dependencies: List<Dependency> = emptyList(),
    ): Module =
        Module(
            buildId = buildId,
            path = path,
            projectDir = path.removePrefix(":"),
            appliedPlugins = emptyList(),
            sourceSets = emptyList(),
            dependencies = dependencies,
        )
}
