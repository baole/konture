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
class KontureModuleScope(
    val modules: List<Module>,
) {
    companion object {
        /**
         * Creates a [KontureModuleScope] representing all modules in the project.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         */
        fun fromProject(graph: ProjectGraph = Konture.projectGraph): KontureModuleScope =
            KontureModuleScope(graph.getAllModules())
    }

    /**
     * Filters this scope to modules whose path matches the specified path pattern.
     */
    fun byPath(pathPattern: String): KontureModuleScope {
        val normalized = if (!pathPattern.startsWith(":") && !pathPattern.startsWith("**") && pathPattern.isNotEmpty()) {
            ":$pathPattern"
        } else {
            pathPattern
        }
        return KontureModuleScope(modules.filter { it.path == normalized || PatternMatchers.matchesSimpleGlob(normalized, it.path) })
    }

    /**
     * Filters this scope to modules that have the specified plugin applied.
     */
    fun withPlugin(pluginId: String): KontureModuleScope =
        KontureModuleScope(modules.filter { module -> module.appliedPlugins.contains(pluginId) })

    /**
     * Asserts that all modules in this scope satisfy the given predicate.
     */
    fun assertAll(
        message: (Module) -> String = { "Module ${it.path} failed assertion in KontureModuleScope" },
        predicate: (Module) -> Boolean,
    ) {
        val failures = modules.filterNot(predicate)
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n") { message(it) })
        }
    }

    /**
     * Asserts that at least one module in this scope satisfies the given predicate.
     */
    fun assertAny(
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
    fun assertNone(
        message: (Module) -> String = { "Module ${it.path} unexpectedly satisfied predicate in KontureModuleScope" },
        predicate: (Module) -> Boolean,
    ) {
        val failures = modules.filter(predicate)
        if (failures.isNotEmpty()) {
            throw AssertionError(failures.joinToString("\n") { message(it) })
        }
    }
}
