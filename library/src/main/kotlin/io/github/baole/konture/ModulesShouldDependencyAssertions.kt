/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.SliceCycleDetector
import io.github.baole.konture.impl.isTestConfiguration
import io.github.baole.konture.impl.normalizeModulePath

/** Dependency assertions for Gradle module rules. */
public interface ModulesShouldDependencyAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: ModulesRuleBuilder

    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOnModule(targetPath: String): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized target. */
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
            /** Filter or assertion criteria for depends on target. */
            val dependsOnTarget =
                module.dependencies.any { dep ->
                    dep.targetPath == normalizedTarget ||
                        PatternMatchers.matchesModuleGlob(normalizedTarget, dep.targetPath)
                }
            if (dependsOnTarget) {
                violations.add(
                    getMessage("module.should.notDependOnModule", module.path, normalizedTarget),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOnModule(targetPaths: List<String>): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized targets. */
        val normalizedTargets = targetPaths.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
            /** Filter or assertion criteria for offending. */
            val offending =
                module.dependencies.filter { dep ->
                    normalizedTargets.any { targetPath ->
                        dep.targetPath == targetPath || PatternMatchers.matchesModuleGlob(targetPath, dep.targetPath)
                    }
                }
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "module.should.notDependOnModuleAny",
                        module.path,
                        normalizedTargets.joinToString(),
                        offending.joinToString { it.targetPath },
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on module. */
    public fun notDependOnModule(vararg targetPaths: String): ModulesRuleBuilder =
        notDependOnModule(targetPaths.asList())

    @Deprecated("Use notDependOnModule instead.", ReplaceWith("notDependOnModule(targetPath)"))
    /** Filter or assertion criteria for not depend on modules. */
    public infix fun notDependOnModules(targetPath: String): ModulesRuleBuilder = notDependOnModule(targetPath)

    @Deprecated("Use notDependOnModule instead.", ReplaceWith("notDependOnModule(targetPaths)"))
    /** Filter or assertion criteria for not depend on modules. */
    public infix fun notDependOnModules(targetPaths: List<String>): ModulesRuleBuilder = notDependOnModule(targetPaths)

    /** Filter or assertion criteria for not depend on modules. */
    @Deprecated("Use notDependOnModule instead.", ReplaceWith("notDependOnModule(*targetPaths)"))
    public fun notDependOnModules(vararg targetPaths: String): ModulesRuleBuilder = notDependOnModule(*targetPaths)

    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOnModule(predicate: (String) -> Boolean): ModulesRuleBuilder =
        notDependOnModule("custom predicate", predicate)

    /** Filter or assertion criteria for not depend on module. */
    public fun notDependOnModule(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            /** Filter or assertion criteria for offending deps. */
            val offendingDeps = module.dependencies.filter { dep -> predicate(dep.targetPath) }
            if (offendingDeps.isNotEmpty()) {
                /** Filter or assertion criteria for paths. */
                val paths = offendingDeps.joinToString { it.targetPath }
                violations.add(
                    getMessage("module.should.notDependOnModulePredicate", module.path, description, paths),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for only depend on modules. */
    public infix fun onlyDependOnModules(allowedPattern: String): ModulesRuleBuilder =
        onlyDependOnModules(listOf(allowedPattern))

    /** Filter or assertion criteria for only depend on modules. */
    public infix fun onlyDependOnModules(allowedPatterns: List<String>): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized patterns. */
        val normalizedPatterns = allowedPatterns.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
                // "Only depend on X" is a production-architecture rule.  Test-configuration edges
                // (testImplementation, testRuntimeOnly, androidTestImplementation, …) are expected
                // to pull in test frameworks and fixtures and are intentionally excluded from this
                // allowlist check.  Use notDependOnModule() to enforce bans across all configurations.
                if (dep.isTestConfiguration()) continue
                /** Filter or assertion criteria for is allowed. */
                val isAllowed =
                    normalizedPatterns.any { pattern ->
                        dep.targetPath == pattern || PatternMatchers.matchesModuleGlob(pattern, dep.targetPath)
                    }
                if (!isAllowed) {
                    violations.add(
                        getMessage(
                            "module.should.onlyDependOnModulesPattern",
                            module.path,
                            dep.targetPath,
                            normalizedPatterns.joinToString(),
                        ),
                    )
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for only depend on modules. */
    public fun onlyDependOnModules(vararg allowedPatterns: String): ModulesRuleBuilder =
        onlyDependOnModules(allowedPatterns.asList())

    /** Filter or assertion criteria for only depend on modules. */
    public infix fun onlyDependOnModules(predicate: (String) -> Boolean): ModulesRuleBuilder =
        onlyDependOnModules("custom predicate", predicate)

    /** Filter or assertion criteria for only depend on modules. */
    public fun onlyDependOnModules(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
                if (dep.isTestConfiguration()) continue
                if (!predicate(dep.targetPath)) {
                    violations.add(
                        getMessage(
                            "module.should.onlyDependOnModulesPredicate",
                            module.path,
                            dep.targetPath,
                            description,
                        ),
                    )
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for only be depended on by. */
    public infix fun onlyBeDependedOnBy(allowedPattern: String): ModulesRuleBuilder =
        onlyBeDependedOnBy(listOf(allowedPattern))

    /** Filter or assertion criteria for only be depended on by. */
    public infix fun onlyBeDependedOnBy(allowedPatterns: List<String>): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized patterns. */
        val normalizedPatterns = allowedPatterns.map { normalizeModulePath(it) }
        builder.setShould { module, graph, violations ->
            /** Filter or assertion criteria for dependents. */
            val dependents =
                graph.getAllModules().filter { other ->
                    other.dependencies.any { dep -> !dep.isTestConfiguration() && dep.targetPath == module.path }
                }
            for (dep in dependents) {
                /** Filter or assertion criteria for is allowed. */
                val isAllowed =
                    normalizedPatterns.any { pattern ->
                        dep.path == pattern || PatternMatchers.matchesModuleGlob(pattern, dep.path)
                    }
                if (!isAllowed) {
                    violations.add(
                        getMessage("module.should.notBeDependedOnByModules", module.path, dep.path),
                    )
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for only be depended on by. */
    public fun onlyBeDependedOnBy(vararg allowedPatterns: String): ModulesRuleBuilder =
        onlyBeDependedOnBy(allowedPatterns.asList())

    /** Filter or assertion criteria for only be depended on by. */
    public infix fun onlyBeDependedOnBy(predicate: (String) -> Boolean): ModulesRuleBuilder =
        onlyBeDependedOnBy("custom predicate", predicate)

    /** Filter or assertion criteria for only be depended on by. */
    public fun onlyBeDependedOnBy(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            /** Filter or assertion criteria for dependents. */
            val dependents =
                graph.getAllModules().filter { other ->
                    other.dependencies.any { dep -> !dep.isTestConfiguration() && dep.targetPath == module.path }
                }
            for (dep in dependents) {
                if (!predicate(dep.path)) {
                    violations.add(
                        getMessage(
                            "module.should.notBeDependedOnByModulesPredicate",
                            module.path,
                            dep.path,
                            description,
                        ),
                    )
                }
            }
        }
        return builder
    }

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

    /** Filter or assertion criteria for depend on module. */
    public infix fun dependOnModule(targetPath: String): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized target. */
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
            /** Filter or assertion criteria for depends on target. */
            val dependsOnTarget =
                module.dependencies.any { dep ->
                    dep.targetPath == normalizedTarget ||
                        PatternMatchers.matchesModuleGlob(normalizedTarget, dep.targetPath)
                }
            if (!dependsOnTarget) {
                violations.add(
                    getMessage("module.should.dependOnModule", module.path, normalizedTarget),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for depend on modules. */
    public infix fun dependOnModules(targetPaths: List<String>): ModulesRuleBuilder {
        targetPaths.forEach { dependOnModule(it) }
        return builder
    }

    /** Filter or assertion criteria for depend on modules. */
    public fun dependOnModules(vararg targetPaths: String): ModulesRuleBuilder = dependOnModules(targetPaths.toList())

    /** Filter or assertion criteria for be free of cycles. */
    public fun beFreeOfCycles(): ModulesRuleBuilder {
        builder.setShould { _, graph, violations ->
            /** Filter or assertion criteria for adjacency. */
            val adjacency =
                graph.getAllModules().associate { module ->
                    module.path to module.dependencies.map { it.targetPath }.toSet()
                }

            /** Filter or assertion criteria for cycles. */
            val cycles = SliceCycleDetector.findCycles(adjacency)
            if (cycles.isNotEmpty()) {
                for (cycle in cycles) {
                    /** Filter or assertion criteria for rendered. */
                    val rendered = (cycle + cycle.first()).joinToString(" -> ")
                    violations.add(getMessage("module.should.beFreeOfCycles", rendered))
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules do not contain dependency cycles.
     * Alias for [beFreeOfCycles].
     */
    public fun notContainCycles(): ModulesRuleBuilder = beFreeOfCycles()

    /** Filter or assertion criteria for depend on module api. */
    public infix fun dependOnModuleApi(targetPath: String): ModulesRuleBuilder =
        dependOnModuleViaConfiguration(targetPath, "api")

    /** Filter or assertion criteria for depend on module implementation. */
    public infix fun dependOnModuleImplementation(targetPath: String): ModulesRuleBuilder =
        dependOnModuleViaConfiguration(targetPath, "implementation")

    /** Filter or assertion criteria for depend on module via configuration. */
    public fun dependOnModuleViaConfiguration(
        targetPath: String,
        configuration: String,
    ): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized target. */
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches =
                module.dependencies.any { dep ->
                    (dep.targetPath == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, dep.targetPath)) &&
                        dep.configuration.equals(configuration, ignoreCase = true)
                }
            if (!matches) {
                violations.add(
                    getMessage("module.should.dependOnModuleViaConfig", module.path, normalizedTarget, configuration),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on module via configuration. */
    public fun notDependOnModuleViaConfiguration(
        targetPath: String,
        configuration: String,
    ): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized target. */
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches =
                module.dependencies.any { dep ->
                    (dep.targetPath == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, dep.targetPath)) &&
                        dep.configuration.equals(configuration, ignoreCase = true)
                }
            if (matches) {
                violations.add(
                    getMessage(
                        "module.should.notDependOnModuleViaConfig",
                        module.path,
                        normalizedTarget,
                        configuration,
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for depend on module transitively. */
    public infix fun dependOnModuleTransitively(targetPath: String): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized target. */
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, graph, violations ->
            /** Filter or assertion criteria for visited. */
            val visited = mutableSetOf<String>()

            /** Filter or assertion criteria for queue. */
            val queue = ArrayDeque<String>()
            queue.addAll(module.dependencies.map { it.targetPath })
            var found = false
            while (queue.isNotEmpty()) {
                /** Filter or assertion criteria for current. */
                val current = queue.removeFirst()
                if (current == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, current)) {
                    found = true
                    break
                }
                if (visited.add(current)) {
                    /** Filter or assertion criteria for current mod. */
                    val currentMod = graph.getAllModules().find { it.path == current }
                    if (currentMod != null) {
                        queue.addAll(currentMod.dependencies.map { it.targetPath })
                    }
                }
            }
            if (!found) {
                violations.add(getMessage("module.should.transitiveDependOn", module.path, normalizedTarget))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on module transitively. */
    public infix fun notDependOnModuleTransitively(targetPath: String): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized target. */
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, graph, violations ->
            /** Filter or assertion criteria for visited. */
            val visited = mutableSetOf<String>()

            /** Filter or assertion criteria for queue. */
            val queue = ArrayDeque<String>()
            queue.addAll(module.dependencies.map { it.targetPath })
            var found = false
            while (queue.isNotEmpty()) {
                /** Filter or assertion criteria for current. */
                val current = queue.removeFirst()
                if (current == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, current)) {
                    found = true
                    break
                }
                if (visited.add(current)) {
                    /** Filter or assertion criteria for current mod. */
                    val currentMod = graph.getAllModules().find { it.path == current }
                    if (currentMod != null) {
                        queue.addAll(currentMod.dependencies.map { it.targetPath })
                    }
                }
            }
            if (found) {
                violations.add(getMessage("module.should.notTransitiveDependOn", module.path, normalizedTarget))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be standalone. */
    public fun beStandalone(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.dependencies.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "module.should.beStandalone",
                        module.path,
                        module.dependencies.map { it.targetPath }.toString(),
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be leaf module. */
    public fun beLeafModule(): ModulesRuleBuilder = beStandalone()
}
