/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Represents a functional scope containing a set of Gradle [Module] structures for inspection and assertions.
 *
 * @property modules The list of [Module] structures included in this scope.
 */
public class KontureModuleScope(
    /** Filter or assertion criteria for modules. */
    public val modules: List<Module>,
) {
    /** Factory methods for constructing module scopes. */
    public companion object {
        /**
         * Creates a [KontureModuleScope] representing all modules in the project.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         * @param sourceSets The source set selector filter.
         */
        public fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector? = null,
        ): KontureModuleScope {
            /** Filter or assertion criteria for all modules. */
            val allModules = graph.getAllModules()
            if (sourceSets == null) {
                return KontureModuleScope(allModules)
            }
            /** Filter or assertion criteria for filtered modules. */
            val filteredModules =
                allModules.map { module ->
                    /** Filter or assertion criteria for matching sets. */
                    val matchingSets =
                        module.sourceSets.filter { sourceSet ->
                            /** Filter or assertion criteria for kind enum. */
                            val kindEnum =
                                when (sourceSet.kind) {
                                    "ANDROID_VARIANT" -> SourceSetKind.ANDROID
                                    "KMP" -> SourceSetKind.KMP
                                    else -> SourceSetKind.JVM
                                }

                            /** Filter or assertion criteria for role enum. */
                            val roleEnum = if (sourceSet.production) SourceSetRole.PRODUCTION else SourceSetRole.TEST
                            sourceSets.matches(SourceSetId(module.path, sourceSet.name, kindEnum, roleEnum))
                        }
                    module.copy(sourceSets = matchingSets)
                }
            return KontureModuleScope(filteredModules)
        }
    }

    /**
     * Filters this scope to modules whose path matches the specified path pattern.
     */
    public fun byPath(pathPattern: String): KontureModuleScope {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!pathPattern.startsWith(":") && !pathPattern.startsWith("**") && pathPattern.isNotEmpty()) {
                ":$pathPattern"
            } else {
                pathPattern
            }
        return KontureModuleScope(
            modules.filter {
                it.path == normalized || PatternMatchers.matchesSimpleGlob(normalized, it.path)
            },
        )
    }

    /**
     * Filters this scope to modules that have the specified plugin applied.
     */
    public fun withPlugin(pluginId: String): KontureModuleScope =
        KontureModuleScope(modules.filter { module -> module.appliedPlugins.contains(pluginId) })

    /**
     * Asserts that all modules in this scope satisfy the given predicate.
     */
    public fun assertAll(
        message: (Module) -> String = { "Module ${it.path} failed assertion in KontureModuleScope" },
        predicate: (Module) -> Boolean,
    ) {
        /** Filter or assertion criteria for failures. */
        val failures = modules.filterNot(predicate)
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n") { message(it) })
        }
    }

    /**
     * Asserts that at least one module in this scope satisfies the given predicate.
     */
    public fun assertAny(
        message: String = "No modules in KontureModuleScope satisfied the predicate",
        predicate: (Module) -> Boolean,
    ) {
        if (modules.none(predicate)) {
            throw AssertionError(message)
        }
    }

    /**
     * Asserts that no modules in this scope satisfy the given predicate.
     */
    public fun assertNone(
        message: (Module) -> String = { "Module ${it.path} unexpectedly satisfied predicate in KontureModuleScope" },
        predicate: (Module) -> Boolean,
    ) {
        /** Filter or assertion criteria for failures. */
        val failures = modules.filter(predicate)
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n") { message(it) })
        }
    }
}

/** Combines two module scopes into a new scope containing distinct modules by path. */
public operator fun KontureModuleScope.plus(other: KontureModuleScope): KontureModuleScope =
    KontureModuleScope((this.modules + other.modules).distinctBy { it.path })

/** Removes modules present in [other] scope from this scope by path. */
public operator fun KontureModuleScope.minus(other: KontureModuleScope): KontureModuleScope =
    KontureModuleScope(this.modules.filterNot { otherModule -> other.modules.any { it.path == otherModule.path } })
