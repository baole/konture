/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Represents a functional scope containing a set of derived [Slice] objects for inspection and assertions.
 *
 * @property slices The list of [Slice] objects included in this scope.
 */
class KontureSliceScope(
    val slices: List<Slice>,
) {
    companion object {
        /**
         * Derives a [KontureSliceScope] from the project graph based on a slice pattern.
         */
        fun fromProject(
            pattern: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureSliceScope {
            val allClasses =
                graph.getAllModules().flatMap { module ->
                    module.files.flatMap { file ->
                        if (file.membershipsFor(module.path).any(sourceSets::matches)) file.classes else emptyList()
                    }
                }.distinctBy { it.fqName to it.filePath }

            val classesByKey = linkedMapOf<String, MutableList<ClassDeclaration>>()
            val packagesByKey = linkedMapOf<String, MutableSet<String>>()
            for (cls in allClasses) {
                val key = PatternMatchers.sliceKeyFor(pattern, cls.packageName) ?: continue
                classesByKey.getOrPut(key) { mutableListOf() }.add(cls)
                packagesByKey.getOrPut(key) { mutableSetOf() }.add(cls.packageName)
            }
            val derived = classesByKey.keys.sorted().map {
                Slice(it, packagesByKey.getValue(it), classesByKey.getValue(it))
            }
            return KontureSliceScope(derived)
        }
    }

    /**
     * Filters this scope to slices whose key matches the specified key pattern.
     */
    fun byKey(keyPattern: String): KontureSliceScope =
        KontureSliceScope(slices.filter { PatternMatchers.matchesSimpleGlob(keyPattern, it.key) })

    /**
     * Asserts that all slices in this scope satisfy the given predicate.
     */
    fun assertAll(
        message: (Slice) -> String = { "Slice ${it.key} failed assertion in KontureSliceScope" },
        predicate: (Slice) -> Boolean,
    ) {
        val failures = slices.filterNot(predicate)
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n") { message(it) })
        }
    }

    /**
     * Asserts that at least one slice in this scope satisfies the given predicate.
     */
    fun assertAny(
        message: String = "No slices in KontureSliceScope satisfied the predicate",
        predicate: (Slice) -> Boolean,
    ) {
        if (slices.none(predicate)) {
            throw AssertionError(message)
        }
    }

    /**
     * Asserts that no slices in this scope satisfy the given predicate.
     */
    fun assertNone(
        message: (Slice) -> String = { "Slice ${it.key} unexpectedly satisfied predicate in KontureSliceScope" },
        predicate: (Slice) -> Boolean,
    ) {
        val failures = slices.filter(predicate)
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n") { message(it) })
        }
    }
}

operator fun KontureSliceScope.plus(other: KontureSliceScope): KontureSliceScope =
    KontureSliceScope((this.slices + other.slices).distinctBy { it.key })

operator fun KontureSliceScope.minus(other: KontureSliceScope): KontureSliceScope =
    KontureSliceScope(this.slices.filterNot { otherSlice -> other.slices.any { it.key == otherSlice.key } })
