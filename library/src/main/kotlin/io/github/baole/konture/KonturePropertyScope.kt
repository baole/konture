/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
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
public class KonturePropertyScope(
    /** Filter or assertion criteria for properties. */
    public val properties: List<PropertyDeclarationContext>,
) {
    /** Factory methods for constructing property scopes. */
    public companion object {
        /**
         * Creates a [KonturePropertyScope] representing all properties across the project.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector (defaults to [SourceSets.production]).
         * @return A [KonturePropertyScope] containing property contexts from matching files.
         */
        public fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            /** Filter or assertion criteria for props. */
            val props =
                graph.getAllModules().flatMap { module ->
                    module.files
                        .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                        .flatMap { file ->
                            /** Filter or assertion criteria for top. */
                            val top =
                                file.topLevelProperties.map {
                                    PropertyDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                                }

                            /** Filter or assertion criteria for mem. */
                            val mem =
                                file.classes.flatMap { cls ->
                                    cls.properties.map {
                                        PropertyDeclarationContext(
                                            it,
                                            file.packageName,
                                            cls.name,
                                            module.path,
                                            file.filePath,
                                        )
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
        public fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            /** Filter or assertion criteria for module. */
            val module =
                graph.getAllModules().find { it.path == path }
                    ?: throw IllegalArgumentException("Module $path not found in project graph")

            /** Filter or assertion criteria for props. */
            val props =
                module.files
                    .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                    .flatMap { file ->
                        /** Filter or assertion criteria for top. */
                        val top =
                            file.topLevelProperties.map {
                                PropertyDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                            }

                        /** Filter or assertion criteria for mem. */
                        val mem =
                            file.classes.flatMap { cls ->
                                cls.properties.map {
                                    PropertyDeclarationContext(
                                        it,
                                        file.packageName,
                                        cls.name,
                                        module.path,
                                        file.filePath,
                                    )
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
        public fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            /** Filter or assertion criteria for props. */
            val props =
                fromProject(graph, sourceSets).properties.filter {
                    it.packageName == packageName || it.packageName.startsWith("$packageName.")
                }
            return KonturePropertyScope(props)
        }
    }
}

/** Combines two [KonturePropertyScope] scopes into a single unified scope. */
public operator fun KonturePropertyScope.plus(other: KonturePropertyScope): KonturePropertyScope =
    KonturePropertyScope(this.properties + other.properties)

/** Subtracts the properties present in [other] from this [KonturePropertyScope]. */
public operator fun KonturePropertyScope.minus(other: KonturePropertyScope): KonturePropertyScope {
    /** Filter or assertion criteria for other names. */
    val otherNames = other.properties.map { it.qualifiedName }.toSet()
    return KonturePropertyScope(this.properties.filterNot { it.qualifiedName in otherNames })
}

// Filtering extensions

/** Filters the list of properties to include only those whose names end with [suffix]. */
public fun List<PropertyDeclarationContext>.haveNameEndingWith(suffix: String): List<PropertyDeclarationContext> =
    filter { it.declaration.name.endsWith(suffix) }

/** Filters the list of properties to include only those whose names end with [suffix]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("haveNameEndingWith(suffix)"),
    level = DeprecationLevel.WARNING,
)
public fun List<PropertyDeclarationContext>.withNameEndingWith(suffix: String): List<PropertyDeclarationContext> =
    haveNameEndingWith(suffix)

/** Filters the list of properties to include only those whose names start with [prefix]. */
public fun List<PropertyDeclarationContext>.haveNameStartingWith(prefix: String): List<PropertyDeclarationContext> =
    filter { it.declaration.name.startsWith(prefix) }

/** Filters the list of properties to include only those whose names start with [prefix]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("haveNameStartingWith(prefix)"),
    level = DeprecationLevel.WARNING,
)
public fun List<PropertyDeclarationContext>.withNameStartingWith(prefix: String): List<PropertyDeclarationContext> =
    haveNameStartingWith(prefix)

/** Filters the list of properties to include only those matching the glob pattern [pattern]. */
public fun List<PropertyDeclarationContext>.withNameMatching(pattern: String): List<PropertyDeclarationContext> =
    filter { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }

/** Filters the list of properties to include only those residing in packages matching [packagePattern]. */
public fun List<PropertyDeclarationContext>.resideInAPackage(packagePattern: String): List<PropertyDeclarationContext> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

/** Filters the list of properties to include only those residing in packages matching [packagePattern]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("resideInAPackage(packagePattern)"),
    level = DeprecationLevel.WARNING,
)
public fun List<PropertyDeclarationContext>.withPackage(packagePattern: String): List<PropertyDeclarationContext> =
    resideInAPackage(packagePattern)

/** Filters the list of properties to include only read-only (`val`) properties. */
public fun List<PropertyDeclarationContext>.valProperties(): List<PropertyDeclarationContext> =
    filter {
        it.declaration.isVal
    }

/** Filters the list of properties to include only mutable (`var`) properties. */
public fun List<PropertyDeclarationContext>.varProperties(): List<PropertyDeclarationContext> =
    filter {
        !it.declaration.isVal
    }

/** Filters the list of properties to include only member/class properties. */
public fun List<PropertyDeclarationContext>.memberProperties(): List<PropertyDeclarationContext> =
    filter {
        it.className != null
    }

/** Filters the list of properties to include only top-level properties. */
public fun List<PropertyDeclarationContext>.topLevelProperties(): List<PropertyDeclarationContext> =
    filter {
        it.className == null
    }

/** Filters the list of properties to include only extension properties. */
public fun List<PropertyDeclarationContext>.extensionProperties(): List<PropertyDeclarationContext> =
    filter {
        it.declaration.isExtension
    }

/** Filters the list of properties to include only those residing in module [modulePath]. */
public fun List<PropertyDeclarationContext>.withModule(modulePath: String): List<PropertyDeclarationContext> {
    /** Filter or assertion criteria for norm. */
    val norm = if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) ":$modulePath" else modulePath
    return filter { it.modulePath == norm }
}

/** Filters properties in this scope to include only those residing in module [modulePath]. */
public fun KonturePropertyScope.withModule(modulePath: String): KonturePropertyScope =
    KonturePropertyScope(properties.withModule(modulePath))

/** Filters properties in this scope to include only read-only (`val`) properties. */
public fun KonturePropertyScope.valProperties(): KonturePropertyScope = KonturePropertyScope(properties.valProperties())

/** Filters properties in this scope to include only mutable (`var`) properties. */
public fun KonturePropertyScope.varProperties(): KonturePropertyScope = KonturePropertyScope(properties.varProperties())

/** Filters properties in this scope to include only member/class properties. */
public fun KonturePropertyScope.memberProperties(): KonturePropertyScope =
    KonturePropertyScope(
        properties.memberProperties(),
    )

/** Filters properties in this scope to include only top-level properties. */
public fun KonturePropertyScope.topLevelProperties(): KonturePropertyScope =
    KonturePropertyScope(properties.topLevelProperties())

/** Filters properties in this scope to include only extension properties. */
public fun KonturePropertyScope.extensionProperties(): KonturePropertyScope =
    KonturePropertyScope(properties.extensionProperties())

/** Filters properties in this scope ending with [suffix]. */
public fun KonturePropertyScope.haveNameEndingWith(suffix: String): KonturePropertyScope =
    KonturePropertyScope(properties.haveNameEndingWith(suffix))

/** Filters properties in this scope ending with [suffix]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("haveNameEndingWith(suffix)"),
    level = DeprecationLevel.WARNING,
)
public fun KonturePropertyScope.withNameEndingWith(suffix: String): KonturePropertyScope = haveNameEndingWith(suffix)

/** Filters properties in this scope starting with [prefix]. */
public fun KonturePropertyScope.haveNameStartingWith(prefix: String): KonturePropertyScope =
    KonturePropertyScope(properties.haveNameStartingWith(prefix))

/** Filters properties in this scope starting with [prefix]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("haveNameStartingWith(prefix)"),
    level = DeprecationLevel.WARNING,
)
public fun KonturePropertyScope.withNameStartingWith(prefix: String): KonturePropertyScope =
    haveNameStartingWith(prefix)

/** Filters properties in this scope residing in packages matching [packagePattern]. */
public fun KonturePropertyScope.resideInAPackage(packagePattern: String): KonturePropertyScope =
    KonturePropertyScope(properties.resideInAPackage(packagePattern))

/** Filters properties in this scope residing in packages matching [packagePattern]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("resideInAPackage(packagePattern)"),
    level = DeprecationLevel.WARNING,
)
public fun KonturePropertyScope.withPackage(packagePattern: String): KonturePropertyScope =
    resideInAPackage(packagePattern)

// Assertion extensions on KonturePropertyScope

/**
 * Asserts that all properties in this scope satisfy the given [predicate].
 *
 * @param additionalMessage Optional message prepended to the failure trace if the assertion fails.
 * @param predicate The condition that every property in the scope must satisfy.
 * @throws AssertionError If any property fails the predicate.
 */
public fun KonturePropertyScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (PropertyDeclarationContext) -> Boolean,
) {
    /** Filter or assertion criteria for failing. */
    val failing = properties.filterNot(predicate)
    if (failing.isNotEmpty()) {
        /** Filter or assertion criteria for details. */
        val details = failing.joinToString("\n - ") { "${it.qualifiedName} (at ${ViolationLocation.format(it)})" }

        /** Filter or assertion criteria for prefix. */
        val prefix = additionalMessage?.let { "$it\n" } ?: ""
        throw AssertionError(getMessage("property.scope.failedAssertionHeader", prefix, details))
    }
}
