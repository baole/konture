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

    @Test
    fun `assertNoCycles excludes test configurations by default`() {
        val first =
            module(
                buildId = ":",
                path = ":first",
                dependencies = listOf(Dependency("testImplementation", "included", ":second")),
            )
        val second =
            module(
                buildId = "included",
                path = ":second",
                dependencies = listOf(Dependency("implementation", ":", ":first")),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(first), "included" to listOf(second)))

        // By default, test configurations are ignored, so no cycle is detected.
        assertDoesNotThrow { graph.assertNoCycles() }
        assertDoesNotThrow { graph.assertNoCycles(includeTestConfigurations = false) }

        // When test configurations are included, the cycle is detected.
        val error =
            assertThrows(AssertionError::class.java) {
                graph.assertNoCycles(includeTestConfigurations = true)
            }
        assertEquals(
            "Circular dependency detected in project graph: ::first -> included:second -> ::first",
            error.message,
        )
    }

    @Test
    fun `assertNoCycles excludes Maven test scope by default`() {
        val first =
            module(
                buildId = ":",
                path = ":first",
                dependencies = listOf(Dependency("test", "included", ":second")),
            )
        val second =
            module(
                buildId = "included",
                path = ":second",
                dependencies = listOf(Dependency("compile", ":", ":first")),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(first), "included" to listOf(second)))

        // By default, Maven "test" scope is ignored, so no cycle is detected.
        assertDoesNotThrow { graph.assertNoCycles() }

        // When test configurations are included, the cycle is detected.
        val error =
            assertThrows(AssertionError::class.java) {
                graph.assertNoCycles(includeTestConfigurations = true)
            }
        assertEquals(
            "Circular dependency detected in project graph: ::first -> included:second -> ::first",
            error.message,
        )
    }

    @Test
    fun `assertNoCycles ignores mixed cycles by default but detects on opt-in`() {
        val first =
            module(
                buildId = ":",
                path = ":first",
                dependencies = listOf(Dependency("implementation", "included", ":second")),
            )
        val second =
            module(
                buildId = "included",
                path = ":second",
                dependencies = listOf(Dependency("testImplementation", ":", ":first")),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(first), "included" to listOf(second)))

        // Mixed cycle containing 'testImplementation' should be ignored by default
        assertDoesNotThrow { graph.assertNoCycles() }

        // Mixed cycle should be detected when opted in
        val error =
            assertThrows(AssertionError::class.java) {
                graph.assertNoCycles(includeTestConfigurations = true)
            }
        assertEquals(
            "Circular dependency detected in project graph: ::first -> included:second -> ::first",
            error.message,
        )
    }

    @Test
    fun `assertNoCycles is case-insensitive for test configurations`() {
        val first =
            module(
                buildId = ":",
                path = ":first",
                dependencies = listOf(Dependency("ANDROIDTESTImplementation", "included", ":second")),
            )
        val second =
            module(
                buildId = "included",
                path = ":second",
                dependencies = listOf(Dependency("ANDROIDTESTImplementation", ":", ":first")),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(first), "included" to listOf(second)))

        // Both 'ANDROIDTESTImplementation' edges must be classified as test-only and skipped, so no cycle is detected.
        assertDoesNotThrow { graph.assertNoCycles() }

        // Detected when opted in
        assertThrows(AssertionError::class.java) {
            graph.assertNoCycles(includeTestConfigurations = true)
        }
    }

    @Test
    fun `assertNoCycles detects production cycles even if separate test cycles exist`() {
        val prod1 =
            module(
                buildId = ":",
                path = ":prod1",
                dependencies = listOf(Dependency("implementation", ":", ":prod2")),
            )
        val prod2 =
            module(
                buildId = ":",
                path = ":prod2",
                dependencies = listOf(Dependency("api", ":", ":prod1")),
            )
        val testOnly =
            module(
                buildId = ":",
                path = ":testOnly",
                dependencies = listOf(Dependency("testImplementation", ":", ":prod1")),
            )

        // This graph contains a production cycle (:prod1 <-> :prod2) and a test link (:testOnly -> :prod1)
        val graph = ProjectGraph(mapOf(":" to listOf(prod1, prod2, testOnly)))

        // Even with test configurations excluded, the production cycle must be caught
        val errorDefault =
            assertThrows(AssertionError::class.java) {
                graph.assertNoCycles()
            }
        assertEquals(
            "Circular dependency detected in project graph: ::prod1 -> ::prod2 -> ::prod1",
            errorDefault.message,
        )
    }

    @Test
    fun `assertNoCycles does not exclude non-test configurations containing test substring`() {
        val first =
            module(
                buildId = ":",
                path = ":first",
                dependencies = listOf(Dependency("latestImplementation", "included", ":second")),
            )
        val second =
            module(
                buildId = "included",
                path = ":second",
                dependencies = listOf(Dependency("implementation", ":", ":first")),
            )
        val graph = ProjectGraph(mapOf(":" to listOf(first), "included" to listOf(second)))

        // Since latestImplementation is not a test configuration, the cycle must be detected even by default
        val error =
            assertThrows(AssertionError::class.java) {
                graph.assertNoCycles()
            }
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
