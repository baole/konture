/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph

/**
 * Utility for computing deterministic shortest paths across directed dependency graphs using BFS.
 */
internal object ShortestPathFinder {
    /**
     * Computes the shortest path from [startNode] to [targetNode].
     */
    fun <T : Comparable<T>> findShortestPath(
        startNode: T,
        targetNode: T,
        getNeighbors: (T) -> Iterable<T>,
    ): List<T>? = findShortestPath(startNode, { it == targetNode }, getNeighbors)

    /**
     * Computes the shortest path from [startNode] to any node that satisfies [isTarget].
     *
     * @param startNode The starting node key.
     * @param isTarget Predicate determining if a visited node is the desired target.
     * @param getNeighbors Function returning outgoing neighbor node keys from a given node.
     * @return The ordered list of node keys from [startNode] to the target node, or null if no path exists.
     */
    fun <T : Comparable<T>> findShortestPath(
        startNode: T,
        isTarget: (T) -> Boolean,
        getNeighbors: (T) -> Iterable<T>,
    ): List<T>? {
        if (isTarget(startNode)) {
            return listOf(startNode)
        }

        val visited = mutableSetOf<T>()
        visited.add(startNode)

        val parent = mutableMapOf<T, T>()
        val queue = ArrayDeque<T>()
        queue.add(startNode)

        while (queue.isNotEmpty()) {
            val currentNode = queue.removeFirst()

            val neighbors = getNeighbors(currentNode).sorted()
            for (neighbor in neighbors) {
                if (isTarget(neighbor)) {
                    val path = mutableListOf<T>()
                    var curr: T? = currentNode
                    while (curr != null) {
                        path.add(curr)
                        curr = parent[curr]
                    }
                    path.reverse()
                    path.add(neighbor)
                    return path
                }
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    parent[neighbor] = currentNode
                    queue.add(neighbor)
                }
            }
        }

        return null
    }

    /**
     * Finds the shortest module path from [startModule] to [targetPath] in [graph].
     */
    fun findShortestModulePath(
        graph: ProjectGraph,
        startModule: Module,
        targetPath: String,
        includeTestConfigurations: Boolean = false,
    ): List<String>? {
        return findShortestModulePathMatching(
            graph = graph,
            startModule = startModule,
            targetPredicate = { it.path == targetPath },
            includeTestConfigurations = includeTestConfigurations,
        )
    }

    /**
     * Finds the shortest module path from [startModule] to any module matching [targetPredicate] in [graph].
     */
    fun findShortestModulePathMatching(
        graph: ProjectGraph,
        startModule: Module,
        targetPredicate: (Module) -> Boolean,
        includeTestConfigurations: Boolean = false,
    ): List<String>? {
        val allModulesMap = graph.getAllModules().associateBy { it.path }

        return findShortestPath(
            startNode = startModule.path,
            isTarget = { path ->
                if (path == startModule.path) {
                    false
                } else {
                    val mod = allModulesMap[path]
                    mod != null && targetPredicate(mod)
                }
            },
            getNeighbors = { path ->
                val mod = allModulesMap[path] ?: return@findShortestPath emptyList()
                mod.dependencies
                    .filter { dep -> includeTestConfigurations || !dep.isTestConfiguration() }
                    .map { it.targetPath }
                    .distinct()
            },
        )
    }
}
