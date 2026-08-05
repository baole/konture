/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ViolationLocation

/**
 * Represents a scope containing a set of property declaration contexts for checking property-level rules in a Konsist-inspired fluent DSL.
 *
 * A scope acts as the starting point or container for filtering, querying, and running assertions against
 * Kotlin property declarations (both member and top-level properties) in your codebase.
 *
 * @property properties The list of [PropertyDeclarationContext] structures included in this scope.
 */
class KonturePropertyScope(
    val properties: List<PropertyDeclarationContext>,
) {
    companion object {
        /**
         * Creates a [KonturePropertyScope] representing all properties across the project.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector (defaults to [SourceSets.production]).
         * @return A [KonturePropertyScope] containing property contexts from matching files.
         */
        fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            val props =
                graph.getAllModules().flatMap { module ->
                    module.files
                        .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                        .flatMap { file ->
                            val top =
                                file.topLevelProperties.map {
                                    PropertyDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                                }
                            val mem =
                                file.classes.flatMap { cls ->
                                    cls.properties.map {
                                        PropertyDeclarationContext(it, file.packageName, cls.name, module.path, file.filePath)
                                    }
                                }
                            top + mem
                        }
                }
            return KonturePropertyScope(props)
        }

        /**
         * Creates a [KonturePropertyScope] for a specific Gradle module path.
         *
         * @param path The Gradle module path (e.g. ":core", ":app").
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector (defaults to [SourceSets.production]).
         * @return A [KonturePropertyScope] containing properties defined in the specified module.
         * @throws IllegalArgumentException If the specified module path is not found in the project graph.
         */
        fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            val module =
                graph.getAllModules().find { it.path == path }
                    ?: throw IllegalArgumentException("Module $path not found in project graph")
            val props =
                module.files
                    .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                    .flatMap { file ->
                        val top =
                            file.topLevelProperties.map {
                                PropertyDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                            }
                        val mem =
                            file.classes.flatMap { cls ->
                                cls.properties.map {
                                    PropertyDeclarationContext(it, file.packageName, cls.name, module.path, file.filePath)
                                }
                            }
                        top + mem
                    }
            return KonturePropertyScope(props)
        }

        /**
         * Creates a [KonturePropertyScope] containing properties in a specific package or its subpackages.
         *
         * @param packageName The package FQN prefix.
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector (defaults to [SourceSets.production]).
         * @return A [KonturePropertyScope] containing properties matching the package or nested packages.
         */
        fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            val props =
                fromProject(graph, sourceSets).properties.filter {
                    it.packageName == packageName || it.packageName.startsWith("$packageName.")
                }
            return KonturePropertyScope(props)
        }
    }
}

/** Combines two [KonturePropertyScope] scopes into a single unified scope. */
operator fun KonturePropertyScope.plus(other: KonturePropertyScope): KonturePropertyScope = KonturePropertyScope(this.properties + other.properties)

/** Subtracts the properties present in [other] from this [KonturePropertyScope]. */
operator fun KonturePropertyScope.minus(other: KonturePropertyScope): KonturePropertyScope {
    val otherNames = other.properties.map { it.qualifiedName }.toSet()
    return KonturePropertyScope(this.properties.filterNot { it.qualifiedName in otherNames })
}

// Filtering extensions

/** Filters the list of properties to include only those whose names end with [suffix]. */
fun List<PropertyDeclarationContext>.withNameEndingWith(suffix: String): List<PropertyDeclarationContext> =
    filter { it.declaration.name.endsWith(suffix) }

/** Filters the list of properties to include only those whose names start with [prefix]. */
fun List<PropertyDeclarationContext>.withNameStartingWith(prefix: String): List<PropertyDeclarationContext> =
    filter { it.declaration.name.startsWith(prefix) }

/** Filters the list of properties to include only those matching the glob pattern [pattern]. */
fun List<PropertyDeclarationContext>.withNameMatching(pattern: String): List<PropertyDeclarationContext> =
    filter { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }

/** Filters the list of properties to include only those residing in packages matching [packagePattern]. */
fun List<PropertyDeclarationContext>.withPackage(packagePattern: String): List<PropertyDeclarationContext> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

/** Filters the list of properties to include only read-only (`val`) properties. */
fun List<PropertyDeclarationContext>.valProperties(): List<PropertyDeclarationContext> = filter { it.declaration.isVal }

/** Filters the list of properties to include only mutable (`var`) properties. */
fun List<PropertyDeclarationContext>.varProperties(): List<PropertyDeclarationContext> = filter { !it.declaration.isVal }

/** Filters the list of properties to include only member/class properties. */
fun List<PropertyDeclarationContext>.memberProperties(): List<PropertyDeclarationContext> = filter { it.className != null }

/** Filters the list of properties to include only top-level properties. */
fun List<PropertyDeclarationContext>.topLevelProperties(): List<PropertyDeclarationContext> = filter { it.className == null }

// Assertion extensions on KonturePropertyScope

/**
 * Asserts that all properties in this scope satisfy the given [predicate].
 *
 * @param additionalMessage Optional message prepended to the failure trace if the assertion fails.
 * @param predicate The condition that every property in the scope must satisfy.
 * @throws AssertionError If any property fails the predicate.
 */
fun KonturePropertyScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (PropertyDeclarationContext) -> Boolean,
) {
    val failing = properties.filterNot(predicate)
    if (failing.isNotEmpty()) {
        val details = failing.joinToString("\n - ") { "${it.qualifiedName} (at ${ViolationLocation.format(it)})" }
        val prefix = additionalMessage?.let { "$it\n" } ?: ""
        throw AssertionError("${prefix}Properties failed assertion:\n - $details")
    }
}
