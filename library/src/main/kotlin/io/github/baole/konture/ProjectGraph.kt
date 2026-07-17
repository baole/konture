/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.DependencyGraphModel
import io.github.baole.konture.impl.ModuleKey

/**
 * Represents the complete structural graph of the multi-project/composite build.
 * It provides core querying capabilities, circular dependency verification, and access to the parsed
 * class/declaration hierarchy of the entire workspace.
 *
 * @property builds Map of build ID to the list of modules contained inside that build.
 */
data class ProjectGraph(
    val builds: Map<String, List<Module>>,
    private val externalDependenciesLoader: () -> DependencyGraphModel = {
        DependencyGraphModel()
    },
) {
    val externalDependencies: DependencyGraphModel by lazy {
        externalDependenciesLoader()
    }
    private val moduleMap: Map<ModuleKey, Module> =
        builds
            .flatMap { (buildId, modules) ->
                modules.map { ModuleKey(buildId, it.path) to it }
            }.toMap()

    /**
     * Looks up a module within the project graph by its build ID and Gradle project path.
     *
     * @param buildId The build containing the module (e.g., ":" for the root build).
     * @param path The Gradle project path (e.g., ":domain" or ":feature:profile").
     * @return The matching [Module] if found, or null.
     */
    fun findModule(
        buildId: String,
        path: String,
    ): Module? = moduleMap[ModuleKey(buildId, path)]

    /**
     * Returns a flat list of all modules across all builds in this graph.
     */
    fun getAllModules(): List<Module> = moduleMap.values.toList()

    /**
     * Detects dependency cycles in the module graph and throws an [AssertionError] if a cycle is found.
     * The verification is performed using a Depth-First Search (DFS) traversal.
     *
     * @throws AssertionError if any circular dependency is detected.
     */
    fun assertNoCycles() {
        val visited = mutableSetOf<ModuleKey>()
        val recursionStack = mutableSetOf<ModuleKey>()
        val cycle = mutableListOf<ModuleKey>()

        for (key in moduleMap.keys) {
            if (key !in visited) {
                dfs(key, visited, recursionStack, cycle)
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun dfs(
        key: ModuleKey,
        visited: MutableSet<ModuleKey>,
        recursionStack: MutableSet<ModuleKey>,
        cycle: MutableList<ModuleKey>,
    ): Boolean {
        visited.add(key)
        recursionStack.add(key)
        cycle.add(key)

        val module = moduleMap[key]
        if (module != null) {
            for (dep in module.dependencies) {
                val depKey = ModuleKey(dep.targetBuildId, dep.targetPath)
                if (depKey in recursionStack) {
                    cycle.add(depKey)
                    val cycleStartIndex = cycle.indexOf(depKey)
                    val cyclePath =
                        cycle
                            .subList(
                                cycleStartIndex,
                                cycle.size,
                            ).joinToString(" -> ") { "${it.buildId}${it.path}" }
                    throw AssertionError("Circular dependency detected in project graph: $cyclePath")
                }
                if (depKey !in visited) {
                    if (dfs(depKey, visited, recursionStack, cycle)) return true
                }
            }
        }

        recursionStack.remove(key)
        cycle.removeAt(cycle.size - 1)
        return false
    }

    companion object {
        private var defaultGraph: ProjectGraph? = null

        /**
         * Checks if the default ProjectGraph is initialized.
         */
        internal fun isDefaultInitialized(): Boolean = defaultGraph != null

        /**
         * Sets the default ProjectGraph for the current JVM runtime session.
         */
        internal fun setDefault(graph: ProjectGraph) {
            defaultGraph = graph
        }

        /**
         * Retrieves the default ProjectGraph for the JVM session.
         *
         * @throws IllegalStateException if the default graph has not been initialized.
         */
        internal fun getDefault(): ProjectGraph =
            defaultGraph ?: throw IllegalStateException(
                "Default ProjectGraph has not been initialized. " +
                    "Make sure to apply the plugin or load a graph first.",
            )
    }
}
