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
 * Represents a scope containing a set of function declaration contexts for checking function-level rules in a Konsist-inspired fluent DSL.
 *
 * A scope acts as the starting point or container for filtering, querying, and running assertions against
 * Kotlin function declarations (both member and top-level functions) in your codebase.
 *
 * @property functions The list of [FunctionDeclarationContext] structures included in this scope.
 */
public class KontureFunctionScope(
    /** Filter or assertion criteria for functions. */
    public val functions: List<FunctionDeclarationContext>,
) {
    /** Factory methods for constructing function scopes. */
    public companion object {
        /**
         * Creates a [KontureFunctionScope] representing all functions across the project.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector (defaults to [SourceSets.production]).
         * @return A [KontureFunctionScope] containing function contexts from matching files.
         */
        public fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            /** Filter or assertion criteria for funcs. */
            val funcs =
                graph.getAllModules().flatMap { module ->
                    module.files
                        .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                        .flatMap { file ->
                            /** Filter or assertion criteria for top. */
                            val top =
                                file.topLevelFunctions.map {
                                    FunctionDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                                }

                            /** Filter or assertion criteria for mem. */
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
        public fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            /** Filter or assertion criteria for module. */
            val module =
                graph.getAllModules().find { it.path == path }
                    ?: throw IllegalArgumentException("Module $path not found in project graph")

            /** Filter or assertion criteria for funcs. */
            val funcs =
                module.files
                    .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                    .flatMap { file ->
                        /** Filter or assertion criteria for top. */
                        val top =
                            file.topLevelFunctions.map {
                                FunctionDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                            }

                        /** Filter or assertion criteria for mem. */
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
        public fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            /** Filter or assertion criteria for funcs. */
            val funcs =
                fromProject(graph, sourceSets).functions.filter {
                    it.packageName == packageName || it.packageName.startsWith("$packageName.")
                }
            return KontureFunctionScope(funcs)
        }
    }
}

/** Combines two [KontureFunctionScope] scopes into a single unified scope. */
public operator fun KontureFunctionScope.plus(other: KontureFunctionScope): KontureFunctionScope =
    KontureFunctionScope(this.functions + other.functions)

/** Subtracts the functions present in [other] from this [KontureFunctionScope]. */
public operator fun KontureFunctionScope.minus(other: KontureFunctionScope): KontureFunctionScope {
    /** Filter or assertion criteria for other names. */
    val otherNames = other.functions.map { it.qualifiedName }.toSet()
    return KontureFunctionScope(this.functions.filterNot { it.qualifiedName in otherNames })
}

// Filtering extensions

/** Filters the list of functions to include only those whose names end with [suffix]. */
public fun List<FunctionDeclarationContext>.haveNameEndingWith(suffix: String): List<FunctionDeclarationContext> =
    filter { it.declaration.name.endsWith(suffix) }

/** Filters the list of functions to include only those whose names end with [suffix]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("haveNameEndingWith(suffix)"),
    level = DeprecationLevel.WARNING,
)
public fun List<FunctionDeclarationContext>.withNameEndingWith(suffix: String): List<FunctionDeclarationContext> =
    haveNameEndingWith(suffix)

/** Filters the list of functions to include only those whose names start with [prefix]. */
public fun List<FunctionDeclarationContext>.haveNameStartingWith(prefix: String): List<FunctionDeclarationContext> =
    filter { it.declaration.name.startsWith(prefix) }

/** Filters the list of functions to include only those whose names start with [prefix]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("haveNameStartingWith(prefix)"),
    level = DeprecationLevel.WARNING,
)
public fun List<FunctionDeclarationContext>.withNameStartingWith(prefix: String): List<FunctionDeclarationContext> =
    haveNameStartingWith(prefix)

/** Filters the list of functions to include only those matching the glob pattern [pattern]. */
public fun List<FunctionDeclarationContext>.withNameMatching(pattern: String): List<FunctionDeclarationContext> =
    filter { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }

/** Filters the list of functions to include only those residing in packages matching [packagePattern]. */
public fun List<FunctionDeclarationContext>.resideInAPackage(packagePattern: String): List<FunctionDeclarationContext> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

/** Filters the list of functions to include only those residing in packages matching [packagePattern]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("resideInAPackage(packagePattern)"),
    level = DeprecationLevel.WARNING,
)
public fun List<FunctionDeclarationContext>.withPackage(packagePattern: String): List<FunctionDeclarationContext> =
    resideInAPackage(packagePattern)

/** Filters the list of functions to include only member/class functions. */
public fun List<FunctionDeclarationContext>.memberFunctions(): List<FunctionDeclarationContext> =
    filter {
        it.className != null
    }

/** Filters the list of functions to include only top-level functions. */
public fun List<FunctionDeclarationContext>.topLevelFunctions(): List<FunctionDeclarationContext> =
    filter {
        it.className == null
    }

/** Filters the list of functions to include only extension functions. */
public fun List<FunctionDeclarationContext>.extensionFunctions(): List<FunctionDeclarationContext> =
    filter {
        it.declaration.isExtension
    }

/** Filters the list of functions to include only those residing in module [modulePath]. */
public fun List<FunctionDeclarationContext>.withModule(modulePath: String): List<FunctionDeclarationContext> {
    /** Filter or assertion criteria for norm. */
    val norm = if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) ":$modulePath" else modulePath
    return filter { it.modulePath == norm }
}

/** Filters functions in this scope to include only those residing in module [modulePath]. */
public fun KontureFunctionScope.withModule(modulePath: String): KontureFunctionScope =
    KontureFunctionScope(functions.withModule(modulePath))

/** Filters functions in this scope to include only extension functions. */
public fun KontureFunctionScope.extensionFunctions(): KontureFunctionScope =
    KontureFunctionScope(functions.extensionFunctions())

/** Filters functions in this scope to include only top-level functions. */
public fun KontureFunctionScope.topLevelFunctions(): KontureFunctionScope =
    KontureFunctionScope(
        functions.topLevelFunctions(),
    )

/** Filters functions in this scope to include only member/class functions. */
public fun KontureFunctionScope.memberFunctions(): KontureFunctionScope =
    KontureFunctionScope(
        functions.memberFunctions(),
    )

/** Filters functions in this scope to include only those with return type [returnType]. */
public fun KontureFunctionScope.withReturnType(returnType: String): KontureFunctionScope =
    KontureFunctionScope(functions.filter { it.returnType == returnType })

/** Filters functions in this scope to include only those with parameter of type [paramType]. */
public fun KontureFunctionScope.withParameterOf(paramType: String): KontureFunctionScope =
    KontureFunctionScope(
        functions.filter {
                func ->
            func.parameters.any { p -> p.type == paramType || p.type.endsWith(".$paramType") }
        },
    )

/** Filters functions in this scope to include only those with annotation [annotationName]. */
public fun KontureFunctionScope.withAnnotationOf(annotationName: String): KontureFunctionScope =
    KontureFunctionScope(functions.filter { it.hasAnnotation(annotationName) })

/** Filters functions in this scope ending with [suffix]. */
public fun KontureFunctionScope.haveNameEndingWith(suffix: String): KontureFunctionScope =
    KontureFunctionScope(functions.haveNameEndingWith(suffix))

/** Filters functions in this scope ending with [suffix]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("haveNameEndingWith(suffix)"),
    level = DeprecationLevel.WARNING,
)
public fun KontureFunctionScope.withNameEndingWith(suffix: String): KontureFunctionScope = haveNameEndingWith(suffix)

/** Filters functions in this scope starting with [prefix]. */
public fun KontureFunctionScope.haveNameStartingWith(prefix: String): KontureFunctionScope =
    KontureFunctionScope(functions.haveNameStartingWith(prefix))

/** Filters functions in this scope starting with [prefix]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("haveNameStartingWith(prefix)"),
    level = DeprecationLevel.WARNING,
)
public fun KontureFunctionScope.withNameStartingWith(prefix: String): KontureFunctionScope =
    haveNameStartingWith(prefix)

/** Filters functions in this scope residing in packages matching [packagePattern]. */
public fun KontureFunctionScope.resideInAPackage(packagePattern: String): KontureFunctionScope =
    KontureFunctionScope(functions.resideInAPackage(packagePattern))

/** Filters functions in this scope residing in packages matching [packagePattern]. */
@Deprecated(
    message = "Renamed for consistency across Konture DSL scopes.",
    replaceWith = ReplaceWith("resideInAPackage(packagePattern)"),
    level = DeprecationLevel.WARNING,
)
public fun KontureFunctionScope.withPackage(packagePattern: String): KontureFunctionScope =
    resideInAPackage(packagePattern)

/** Filters functions in this scope to include only those with visibility [visibility]. */
public fun KontureFunctionScope.withVisibility(visibility: Visibility): KontureFunctionScope =
    KontureFunctionScope(functions.filter { it.visibility == visibility })

// Assertion extensions on KontureFunctionScope

/**
 * Asserts that all functions in this scope satisfy the given [predicate].
 *
 * @param additionalMessage Optional message prepended to the failure trace if the assertion fails.
 * @param predicate The condition that every function in the scope must satisfy.
 * @throws AssertionError If any function fails the predicate.
 */
public fun KontureFunctionScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (FunctionDeclarationContext) -> Boolean,
) {
    /** Filter or assertion criteria for failing. */
    val failing = functions.filterNot(predicate)
    if (failing.isNotEmpty()) {
        /** Filter or assertion criteria for details. */
        val details = failing.joinToString("\n - ") { "${it.qualifiedName} (at ${ViolationLocation.format(it)})" }

        /** Filter or assertion criteria for prefix. */
        val prefix = additionalMessage?.let { "$it\n" } ?: ""
        throw AssertionError(getMessage("function.scope.failedAssertionHeader", prefix, details))
    }
}

/** Asserts that all functions in this scope have KDoc documentation. */
public fun KontureFunctionScope.assertHasKDoc(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { it.kdocText?.isNotBlank() == true }
}
