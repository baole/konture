/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ViolationLocation

/**
 * Represents a scope containing a set of function declaration contexts for checking function-level rules in a Konsist-inspired fluent DSL.
 *
 * A scope acts as the starting point or container for filtering, querying, and running assertions against
 * Kotlin function declarations (both member and top-level functions) in your codebase.
 *
 * @property functions The list of [FunctionDeclarationContext] structures included in this scope.
 */
class KontureFunctionScope(
    val functions: List<FunctionDeclarationContext>,
) {
    companion object {
        /**
         * Creates a [KontureFunctionScope] representing all functions across the project.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector (defaults to [SourceSets.production]).
         * @return A [KontureFunctionScope] containing function contexts from matching files.
         */
        fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            val funcs =
                graph.getAllModules().flatMap { module ->
                    module.files
                        .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                        .flatMap { file ->
                            val top =
                                file.topLevelFunctions.map {
                                    FunctionDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                                }
                            val mem =
                                file.classes.flatMap { cls ->
                                    cls.functions.map {
                                        FunctionDeclarationContext(
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
            return KontureFunctionScope(funcs)
        }

        /**
         * Creates a [KontureFunctionScope] for a specific Gradle module path.
         *
         * @param path The Gradle module path (e.g. ":core", ":app").
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector (defaults to [SourceSets.production]).
         * @return A [KontureFunctionScope] containing functions defined in the specified module.
         * @throws IllegalArgumentException If the specified module path is not found in the project graph.
         */
        fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            val module =
                graph.getAllModules().find { it.path == path }
                    ?: throw IllegalArgumentException("Module $path not found in project graph")
            val funcs =
                module.files
                    .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                    .flatMap { file ->
                        val top =
                            file.topLevelFunctions.map {
                                FunctionDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                            }
                        val mem =
                            file.classes.flatMap { cls ->
                                cls.functions.map {
                                    FunctionDeclarationContext(
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
            return KontureFunctionScope(funcs)
        }

        /**
         * Creates a [KontureFunctionScope] containing functions in a specific package or its subpackages.
         *
         * @param packageName The package FQN prefix.
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector (defaults to [SourceSets.production]).
         * @return A [KontureFunctionScope] containing functions matching the package or nested packages.
         */
        fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            val funcs =
                fromProject(graph, sourceSets).functions.filter {
                    it.packageName == packageName || it.packageName.startsWith("$packageName.")
                }
            return KontureFunctionScope(funcs)
        }
    }
}

/** Combines two [KontureFunctionScope] scopes into a single unified scope. */
operator fun KontureFunctionScope.plus(other: KontureFunctionScope): KontureFunctionScope =
    KontureFunctionScope(this.functions + other.functions)

/** Subtracts the functions present in [other] from this [KontureFunctionScope]. */
operator fun KontureFunctionScope.minus(other: KontureFunctionScope): KontureFunctionScope {
    val otherNames = other.functions.map { it.qualifiedName }.toSet()
    return KontureFunctionScope(this.functions.filterNot { it.qualifiedName in otherNames })
}

// Filtering extensions

/** Filters the list of functions to include only those whose names end with [suffix]. */
fun List<FunctionDeclarationContext>.withNameEndingWith(suffix: String): List<FunctionDeclarationContext> =
    filter { it.declaration.name.endsWith(suffix) }

/** Filters the list of functions to include only those whose names start with [prefix]. */
fun List<FunctionDeclarationContext>.withNameStartingWith(prefix: String): List<FunctionDeclarationContext> =
    filter { it.declaration.name.startsWith(prefix) }

/** Filters the list of functions to include only those matching the glob pattern [pattern]. */
fun List<FunctionDeclarationContext>.withNameMatching(pattern: String): List<FunctionDeclarationContext> =
    filter { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }

/** Filters the list of functions to include only those residing in packages matching [packagePattern]. */
fun List<FunctionDeclarationContext>.withPackage(packagePattern: String): List<FunctionDeclarationContext> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

/** Filters the list of functions to include only member/class functions. */
fun List<FunctionDeclarationContext>.memberFunctions(): List<FunctionDeclarationContext> =
    filter {
        it.className != null
    }

/** Filters the list of functions to include only top-level functions. */
fun List<FunctionDeclarationContext>.topLevelFunctions(): List<FunctionDeclarationContext> =
    filter {
        it.className == null
    }

/** Filters the list of functions to include only extension functions. */
fun List<FunctionDeclarationContext>.extensionFunctions(): List<FunctionDeclarationContext> =
    filter {
        it.declaration.isExtension
    }

fun List<FunctionDeclarationContext>.withModule(modulePath: String): List<FunctionDeclarationContext> {
    val norm = if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) ":$modulePath" else modulePath
    return filter { it.modulePath == norm }
}

fun KontureFunctionScope.withModule(modulePath: String) = KontureFunctionScope(functions.withModule(modulePath))

fun KontureFunctionScope.extensionFunctions(): KontureFunctionScope =
    KontureFunctionScope(functions.extensionFunctions())

fun KontureFunctionScope.topLevelFunctions(): KontureFunctionScope =
    KontureFunctionScope(functions.topLevelFunctions())

fun KontureFunctionScope.memberFunctions(): KontureFunctionScope =
    KontureFunctionScope(functions.memberFunctions())





// Assertion extensions on KontureFunctionScope

/**
 * Asserts that all functions in this scope satisfy the given [predicate].
 *
 * @param additionalMessage Optional message prepended to the failure trace if the assertion fails.
 * @param predicate The condition that every function in the scope must satisfy.
 * @throws AssertionError If any function fails the predicate.
 */
fun KontureFunctionScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (FunctionDeclarationContext) -> Boolean,
) {
    val failing = functions.filterNot(predicate)
    if (failing.isNotEmpty()) {
        val details = failing.joinToString("\n - ") { "${it.qualifiedName} (at ${ViolationLocation.format(it)})" }
        val prefix = additionalMessage?.let { "$it\n" } ?: ""
        throw AssertionError("${prefix}Functions failed assertion:\n - $details")
    }
}
