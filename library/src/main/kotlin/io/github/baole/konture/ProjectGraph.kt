/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.DependencyGraphModel
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import io.github.baole.konture.impl.ModuleKey
import kotlin.jvm.JvmOverloads

/**
 * Represents the complete structural graph of the multi-project/composite build.
 * It provides core querying capabilities, circular dependency verification, and access to the parsed
 * class/declaration hierarchy of the entire workspace.
 *
 * @property builds Map of build ID to the list of modules contained inside that build.
 */
public data class ProjectGraph(
    /** Filter or assertion criteria for builds. */
    public val builds: Map<String, List<Module>>,
    private val externalDependenciesLoader: () -> DependencyGraphModel? = {
        DependencyGraphModel()
    },
) {
    private val loadedExternalDependencies: DependencyGraphModel? by lazy {
        externalDependenciesLoader()
    }

    /** Filter or assertion criteria for external dependencies. */
    public val externalDependencies: DependencyGraphModel by lazy {
        loadedExternalDependencies ?: DependencyGraphModel()
    }

    /**
     * Returns the external dependency graph or reports that the test was not prepared for an
     * external-dependency assertion. The Gradle plugin normally detects direct assertion usage
     * and generates this graph automatically.
     */
    internal fun requireExternalDependencies(): DependencyGraphModel =
        checkNotNull(loadedExternalDependencies) {
            getMessage("dependencyGraph.required")
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
    public fun findModule(
        buildId: String,
        path: String,
    ): Module? = moduleMap[ModuleKey(buildId, path)]

    /**
     * Returns a flat list of all modules across all builds in this graph.
     */
    public fun getAllModules(): List<Module> = moduleMap.values.toList()

    /**
     * Detects dependency cycles in the module graph and throws an [AssertionError] if any cycle is found.
     * The verification is performed using a Depth-First Search (DFS) traversal over module dependency edges.
     * Note: DFS depth corresponds to the maximum module dependency depth in the graph.
     *
     * @param includeTestConfigurations if true, test-related dependency configurations will also be analyzed
     * for cycles. If false (default), they are skipped.
     * @throws AssertionError if any circular dependency is detected.
     */
    @JvmOverloads
    public fun assertNoCycles(includeTestConfigurations: Boolean = false) {
        /** Filter or assertion criteria for visited. */
        val visited = mutableSetOf<ModuleKey>()

        /** Filter or assertion criteria for recursion stack. */
        val recursionStack = mutableListOf<ModuleKey>()

        /** Filter or assertion criteria for on stack. */
        val onStack = mutableSetOf<ModuleKey>()

        /** Filter or assertion criteria for cycles. */
        val cycles = linkedSetOf<List<ModuleKey>>()

        for (key in moduleMap.keys.sortedBy { "${it.buildId}${it.path}" }) {
            if (key !in visited) {
                dfs(key, visited, recursionStack, onStack, cycles, includeTestConfigurations)
            }
        }

        if (cycles.isNotEmpty()) {
            /** Filter or assertion criteria for rendered cycles. */
            val renderedCycles =
                cycles.map { cycle ->
                    (cycle + cycle.first()).joinToString(" -> ") { "${it.buildId}${it.path}" }
                }
            if (renderedCycles.size == 1) {
                throw AssertionError(getMessage("project.graph.circularDependency", renderedCycles.first()))
            } else {
                val details = renderedCycles.joinToString("\n") { "  - $it" }
                throw AssertionError(getMessage("project.graph.circularDependencies", details))
            }
        }
    }

    private fun Dependency.isTestConfiguration(): Boolean {
        /** Filter or assertion criteria for name. */
        val name = configuration
        var start = 0
        while (true) {
            /** Filter or assertion criteria for index. */
            val index = name.indexOf("test", start, ignoreCase = true)
            if (index == -1) break
            /** Filter or assertion criteria for end. */
            val end = index + 4

            // Check Left Boundary (starts the string, is an uppercase letter, or is preceded by a non-alphanumeric character)
            val leftOk = index == 0 || name[index].isUpperCase() || !name[index - 1].isLetterOrDigit()

            // Check Right Boundary (ends the string, is followed by an uppercase letter, or is followed by a non-alphanumeric character)
            val rightOk = end == name.length || name[end].isUpperCase() || !name[end].isLetterOrDigit()

            if (leftOk && rightOk) {
                return true
            }
            start = index + 1
        }
        return false
    }

    @Suppress("NestedBlockDepth")
    private fun dfs(
        key: ModuleKey,
        visited: MutableSet<ModuleKey>,
        recursionStack: MutableList<ModuleKey>,
        onStack: MutableSet<ModuleKey>,
        cycles: MutableSet<List<ModuleKey>>,
        includeTestConfigurations: Boolean,
    ) {
        visited.add(key)
        recursionStack.add(key)
        onStack.add(key)

        /** Filter or assertion criteria for module. */
        val module = moduleMap[key]
        if (module != null) {
            for (dep in module.dependencies) {
                if (!includeTestConfigurations && dep.isTestConfiguration()) {
                    continue
                }
                /** Filter or assertion criteria for dep key. */
                val depKey = ModuleKey(dep.targetBuildId, dep.targetPath)
                if (depKey in onStack) {
                    /** Filter or assertion criteria for cycle start index. */
                    val cycleStartIndex = recursionStack.indexOf(depKey)

                    /** Filter or assertion criteria for raw cycle. */
                    val rawCycle = recursionStack.subList(cycleStartIndex, recursionStack.size).toList()
                    cycles.add(canonicalizeCycle(rawCycle))
                } else if (depKey !in visited) {
                    dfs(depKey, visited, recursionStack, onStack, cycles, includeTestConfigurations)
                }
            }
        }

        onStack.remove(key)
        recursionStack.removeAt(recursionStack.size - 1)
    }

    private fun canonicalizeCycle(cycle: List<ModuleKey>): List<ModuleKey> {
        /** Filter or assertion criteria for keys. */
        val keys = cycle.map { "${it.buildId}${it.path}" }

        /** Filter or assertion criteria for min index. */
        val minIndex = keys.indices.minByOrNull { keys[it] } ?: 0
        return cycle.subList(minIndex, cycle.size) + cycle.subList(0, minIndex)
    }

    /** Factory and state management methods for default ProjectGraph instances. */
    public companion object {
        /**
         * Checks if the default ProjectGraph is initialized.
         */
        internal fun isDefaultInitialized(): Boolean = KontureRuntimeStateProvider.currentState.projectGraph != null

        /**
         * Sets the default ProjectGraph for the current JVM runtime session.
         */
        internal fun setDefault(graph: ProjectGraph) {
            KontureRuntimeStateProvider.currentState =
                KontureRuntimeStateProvider.currentState.copy(projectGraph = graph)
        }

        /**
         * Retrieves the default ProjectGraph for the JVM session.
         *
         * @throws IllegalStateException if the default graph has not been initialized.
         */
        internal fun getDefault(): ProjectGraph =
            KontureRuntimeStateProvider.currentState.projectGraph
                ?: throw IllegalStateException(
                    "Default ProjectGraph has not been initialized. " +
                        "Make sure to apply the plugin or load a graph first.",
                )
    }
}
