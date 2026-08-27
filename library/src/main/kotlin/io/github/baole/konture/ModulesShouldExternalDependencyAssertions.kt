/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

/** External dependency assertions for Gradle module rules. */
public interface ModulesShouldExternalDependencyAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: ModulesRuleBuilder

    /** Filter or assertion criteria for not depend on external libraries. */
    public fun notDependOnExternalLibraries(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            /** Filter or assertion criteria for resolved deps. */
            val resolvedDeps = graph.requireExternalDependencies().modules[module.path] ?: emptyList()

            /** Filter or assertion criteria for offending. */
            val offending =
                resolvedDeps.filter { dep ->
                    if (!includeTransitive && dep.isTransitive) return@filter false
                    coordinates.any { pattern ->
                        if (pattern.contains(":")) {
                            PatternMatchers.matchesSimpleGlob(pattern, "${dep.group}:${dep.name}")
                        } else {
                            PatternMatchers.matchesSimpleGlob(pattern, dep.group) ||
                                PatternMatchers.matchesSimpleGlob(pattern, dep.name)
                        }
                    }
                }
            if (offending.isNotEmpty()) {
                /** Filter or assertion criteria for coords. */
                val coords =
                    offending.joinToString {
                        "${it.group}:${it.name}:${it.version}${if (it.isTransitive) " (transitive)" else ""}"
                    }
                violations.add(
                    getMessage(
                        "module.should.notDependOnExternalLibraries",
                        module.path,
                        coordinates.joinToString(),
                        coords,
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for only depend on external libraries. */
    public fun onlyDependOnExternalLibraries(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            /** Filter or assertion criteria for resolved deps. */
            val resolvedDeps = graph.requireExternalDependencies().modules[module.path] ?: emptyList()

            /** Filter or assertion criteria for offending. */
            val offending =
                resolvedDeps.filter { dep ->
                    if (!includeTransitive && dep.isTransitive) return@filter false
                    coordinates.none { pattern ->
                        if (pattern.contains(":")) {
                            PatternMatchers.matchesSimpleGlob(pattern, "${dep.group}:${dep.name}")
                        } else {
                            PatternMatchers.matchesSimpleGlob(pattern, dep.group) ||
                                PatternMatchers.matchesSimpleGlob(pattern, dep.name)
                        }
                    }
                }
            if (offending.isNotEmpty()) {
                /** Filter or assertion criteria for coords. */
                val coords =
                    offending.joinToString {
                        "${it.group}:${it.name}:${it.version}${if (it.isTransitive) " (transitive)" else ""}"
                    }
                violations.add(
                    getMessage(
                        "module.should.onlyDependOnExternalLibraries",
                        module.path,
                        coordinates.joinToString(),
                        coords,
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on external libraries. */
    public fun notDependOnExternalLibraries(
        coordinates: List<String>,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder =
        notDependOnExternalLibraries(
            *coordinates.toTypedArray(),
            includeTransitive = includeTransitive,
        )

    /** Filter or assertion criteria for only depend on external libraries. */
    public fun onlyDependOnExternalLibraries(
        coordinates: List<String>,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder =
        onlyDependOnExternalLibraries(
            *coordinates.toTypedArray(),
            includeTransitive = includeTransitive,
        )

    /** Filter or assertion criteria for depend on external library. */
    public fun dependOnExternalLibrary(
        coordinate: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = dependOnExternalLibraries(coordinate, includeTransitive = includeTransitive)

    /** Filter or assertion criteria for have dependency. */
    public fun haveDependency(
        coordinate: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = dependOnExternalLibrary(coordinate, includeTransitive = includeTransitive)

    /** Filter or assertion criteria for have dependencies. */
    public fun haveDependencies(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = dependOnExternalLibraries(*coordinates, includeTransitive = includeTransitive)

    /** Filter or assertion criteria for not have dependency. */
    public fun notHaveDependency(
        coordinate: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = notDependOnExternalLibraries(coordinate, includeTransitive = includeTransitive)

    /** Filter or assertion criteria for not have dependencies. */
    public fun notHaveDependencies(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = notDependOnExternalLibraries(*coordinates, includeTransitive = includeTransitive)

    /** Filter or assertion criteria for depend on external libraries. */
    public fun dependOnExternalLibraries(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            /** Filter or assertion criteria for resolved deps. */
            val resolvedDeps = graph.requireExternalDependencies().modules[module.path] ?: emptyList()

            /** Filter or assertion criteria for matched. */
            val matched =
                resolvedDeps.any { dep ->
                    if (!includeTransitive && dep.isTransitive) return@any false
                    coordinates.any { pattern ->
                        if (pattern.contains(":")) {
                            PatternMatchers.matchesSimpleGlob(pattern, "${dep.group}:${dep.name}")
                        } else {
                            PatternMatchers.matchesSimpleGlob(pattern, dep.group) ||
                                PatternMatchers.matchesSimpleGlob(pattern, dep.name)
                        }
                    }
                }
            if (!matched) {
                violations.add(
                    getMessage(
                        "module.should.dependOnExternalLibraries",
                        module.path,
                        coordinates.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for depend on external libraries. */
    public fun dependOnExternalLibraries(
        coordinates: List<String>,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder =
        dependOnExternalLibraries(
            *coordinates.toTypedArray(),
            includeTransitive = includeTransitive,
        )
}
