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
public class KontureSliceScope(
    /** Filter or assertion criteria for slices. */
    public val slices: List<Slice>,
) {
    /** Factory methods for constructing slice scopes. */
    public companion object {
        /**
         * Derives a [KontureSliceScope] from the project graph based on a slice pattern.
         */
        public fun fromProject(
            pattern: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureSliceScope {
            /** Filter or assertion criteria for all classes. */
            val allClasses =
                graph.getAllModules().flatMap { module ->
                    module.files.flatMap { file ->
                        if (file.membershipsFor(module.path).any(sourceSets::matches)) file.classes else emptyList()
                    }
                }.distinctBy { it.fqName to it.filePath }

            /** Filter or assertion criteria for classes by key. */
            val classesByKey = linkedMapOf<String, MutableList<ClassDeclaration>>()

            /** Filter or assertion criteria for packages by key. */
            val packagesByKey = linkedMapOf<String, MutableSet<String>>()
            for (cls in allClasses) {
                /** Filter or assertion criteria for key. */
                val key = PatternMatchers.sliceKeyFor(pattern, cls.packageName) ?: continue
                classesByKey.getOrPut(key) { mutableListOf() }.add(cls)
                packagesByKey.getOrPut(key) { mutableSetOf() }.add(cls.packageName)
            }
            /** Filter or assertion criteria for derived. */
            val derived =
                classesByKey.keys.sorted().map {
                    Slice(it, packagesByKey.getValue(it), classesByKey.getValue(it))
                }
            return KontureSliceScope(derived)
        }

        /**
         * Derives a [KontureSliceScope] for a specific Gradle module path based on a slice pattern.
         */
        public fun fromModule(
            pattern: String,
            modulePath: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureSliceScope {
            /** Filter or assertion criteria for norm. */
            val norm = if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) ":$modulePath" else modulePath

            /** Filter or assertion criteria for module. */
            val module =
                graph.getAllModules().find { it.path == norm }
                    ?: throw IllegalArgumentException("Module $modulePath not found in project graph")

            /** Filter or assertion criteria for module classes. */
            val moduleClasses =
                module.files.flatMap { file ->
                    if (file.membershipsFor(module.path).any(sourceSets::matches)) file.classes else emptyList()
                }.distinctBy { it.fqName to it.filePath }

            /** Filter or assertion criteria for classes by key. */
            val classesByKey = linkedMapOf<String, MutableList<ClassDeclaration>>()

            /** Filter or assertion criteria for packages by key. */
            val packagesByKey = linkedMapOf<String, MutableSet<String>>()
            for (cls in moduleClasses) {
                /** Filter or assertion criteria for key. */
                val key = PatternMatchers.sliceKeyFor(pattern, cls.packageName) ?: continue
                classesByKey.getOrPut(key) { mutableListOf() }.add(cls)
                packagesByKey.getOrPut(key) { mutableSetOf() }.add(cls.packageName)
            }
            /** Filter or assertion criteria for derived. */
            val derived =
                classesByKey.keys.sorted().map {
                    Slice(it, packagesByKey.getValue(it), classesByKey.getValue(it))
                }
            return KontureSliceScope(derived)
        }

        /**
         * Derives a [KontureSliceScope] for a specific package prefix based on a slice pattern.
         */
        public fun fromPackage(
            pattern: String,
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureSliceScope {
            /** Filter or assertion criteria for all slices. */
            val allSlices = fromProject(pattern, graph, sourceSets)

            /** Filter or assertion criteria for filtered slices. */
            val filteredSlices =
                allSlices.slices.mapNotNull { slice ->
                    /** Filter or assertion criteria for matching packages. */
                    val matchingPackages = slice.packages.filter { it == packageName || it.startsWith("$packageName.") }.toSet()
                    if (matchingPackages.isNotEmpty()) {
                        /** Filter or assertion criteria for matching classes. */
                        val matchingClasses = slice.classes.filter { it.packageName in matchingPackages }
                        Slice(slice.key, matchingPackages, matchingClasses)
                    } else {
                        null
                    }
                }
            return KontureSliceScope(filteredSlices)
        }
    }

    /**
     * Filters this scope to slices whose key matches the specified key pattern.
     */
    public fun byKey(keyPattern: String): KontureSliceScope =
        KontureSliceScope(slices.filter { PatternMatchers.matchesSimpleGlob(keyPattern, it.key) })

    /**
     * Asserts that all slices in this scope satisfy the given predicate.
     */
    public fun assertAll(
        message: (Slice) -> String = { "Slice ${it.key} failed assertion in KontureSliceScope" },
        predicate: (Slice) -> Boolean,
    ) {
        /** Filter or assertion criteria for failures. */
        val failures = slices.filterNot(predicate)
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n") { message(it) })
        }
    }

    /**
     * Asserts that at least one slice in this scope satisfies the given predicate.
     */
    public fun assertAny(
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
    public fun assertNone(
        message: (Slice) -> String = { "Slice ${it.key} unexpectedly satisfied predicate in KontureSliceScope" },
        predicate: (Slice) -> Boolean,
    ) {
        /** Filter or assertion criteria for failures. */
        val failures = slices.filter(predicate)
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n") { message(it) })
        }
    }
}

/** Combines two slice scopes into a new scope containing distinct slices by key. */
public operator fun KontureSliceScope.plus(other: KontureSliceScope): KontureSliceScope =
    KontureSliceScope((this.slices + other.slices).distinctBy { it.key })

/** Removes slices present in [other] scope from this scope by key. */
public operator fun KontureSliceScope.minus(other: KontureSliceScope): KontureSliceScope =
    KontureSliceScope(this.slices.filterNot { otherSlice -> other.slices.any { it.key == otherSlice.key } })
