/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.Slice
import io.github.baole.konture.collectDependencyPackages

/** The slice dependency graph: the slices and the directed edges between them. */
internal class SliceGraph(
    val slices: List<Slice>,
    val adjacency: Map<String, Set<String>>,
)

/**
 * Builds the directed dependency graph between slices and detects cycles within it.
 *
 * An edge runs from slice S to slice T when any class in S depends on a package belonging to T,
 * resolved through the existing source-level dependency resolution. Cycle detection uses the same
 * depth-first back-edge search as the module-level `assertNoCycles`, but produces deterministic
 * output — sorted traversal and cycles rotated to start at their smallest key — so recorded
 * baselines stay stable across runs.
 */
internal object SliceCycleDetector {
    fun buildGraph(
        slices: List<Slice>,
        packageToSlice: Map<String, String>,
        allClasses: List<ClassDeclaration>,
        slicePattern: String,
    ): SliceGraph {
        val sliceKeys = slices.mapTo(mutableSetOf()) { it.key }
        val adjacency = sortedMapOf<String, MutableSet<String>>()
        slices.forEach { adjacency[it.key] = sortedSetOf() }
        for (slice in slices) {
            for (cls in slice.classes) {
                for (dependencyPackage in cls.collectDependencyPackages(allClasses)) {
                    // A dependency package may have no directly-declared classes (e.g. a star import
                    // of a package whose classes live only in subpackages), so fall back to deriving
                    // the key from the pattern. Only edges to an actual slice are recorded.
                    val targetKey =
                        packageToSlice[dependencyPackage]
                            ?: PatternMatchers.sliceKeyFor(slicePattern, dependencyPackage)
                    if (targetKey != null && targetKey != slice.key && targetKey in sliceKeys) {
                        adjacency.getValue(slice.key).add(targetKey)
                    }
                }
            }
        }
        return SliceGraph(slices, adjacency)
    }

    /**
     * Returns the distinct cycles in the slice graph. Each cycle is the list of slice keys in
     * dependency order, rotated to start at the smallest key; render it as `A -> B -> A` by
     * appending the first key.
     */
    fun findCycles(adjacency: Map<String, Set<String>>): List<List<String>> {
        val visited = mutableSetOf<String>()
        val stack = mutableListOf<String>()
        val onStack = mutableSetOf<String>()
        val cycles = linkedSetOf<List<String>>()

        fun visit(node: String) {
            visited.add(node)
            stack.add(node)
            onStack.add(node)
            for (next in (adjacency[node] ?: emptySet()).sorted()) {
                if (next in onStack) {
                    val start = stack.indexOf(next)
                    cycles.add(canonicalize(stack.subList(start, stack.size).toList()))
                } else if (next !in visited) {
                    visit(next)
                }
            }
            stack.removeAt(stack.size - 1)
            onStack.remove(node)
        }

        for (node in adjacency.keys.sorted()) {
            if (node !in visited) visit(node)
        }
        return cycles.toList()
    }

    private fun canonicalize(cycle: List<String>): List<String> {
        val minIndex = cycle.indices.minByOrNull { cycle[it] } ?: 0
        return cycle.subList(minIndex, cycle.size) + cycle.subList(0, minIndex)
    }
}
