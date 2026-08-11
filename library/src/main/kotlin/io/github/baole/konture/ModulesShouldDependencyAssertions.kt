/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.SliceCycleDetector
import io.github.baole.konture.impl.normalizeModulePath

interface ModulesShouldDependencyAssertions {
    val builder: ModulesRuleBuilder

    infix fun notDependOnModule(targetPath: String): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
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

    infix fun notDependOnModule(targetPaths: List<String>): ModulesRuleBuilder {
        val normalizedTargets = targetPaths.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
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

    fun notDependOnModule(vararg targetPaths: String): ModulesRuleBuilder = notDependOnModule(targetPaths.asList())

    infix fun notDependOnModules(targetPath: String): ModulesRuleBuilder = notDependOnModule(targetPath)

    infix fun notDependOnModules(targetPaths: List<String>): ModulesRuleBuilder = notDependOnModule(targetPaths)

    fun notDependOnModules(vararg targetPaths: String): ModulesRuleBuilder = notDependOnModule(*targetPaths)

    infix fun notDependOnModule(predicate: (String) -> Boolean): ModulesRuleBuilder =
        notDependOnModule("custom predicate", predicate)

    fun notDependOnModule(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val offendingDeps = module.dependencies.filter { dep -> predicate(dep.targetPath) }
            if (offendingDeps.isNotEmpty()) {
                val paths = offendingDeps.joinToString { it.targetPath }
                violations.add(
                    getMessage("module.should.notDependOnModulePredicate", module.path, description, paths),
                )
            }
        }
        return builder
    }

    infix fun onlyDependOnModules(allowedPattern: String): ModulesRuleBuilder =
        onlyDependOnModules(listOf(allowedPattern))

    infix fun onlyDependOnModules(allowedPatterns: List<String>): ModulesRuleBuilder {
        val normalizedPatterns = allowedPatterns.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
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

    fun onlyDependOnModules(vararg allowedPatterns: String): ModulesRuleBuilder =
        onlyDependOnModules(allowedPatterns.asList())

    infix fun onlyDependOnModules(predicate: (String) -> Boolean): ModulesRuleBuilder =
        onlyDependOnModules("custom predicate", predicate)

    fun onlyDependOnModules(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
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

    infix fun onlyBeDependedOnBy(allowedPattern: String): ModulesRuleBuilder =
        onlyBeDependedOnBy(listOf(allowedPattern))

    infix fun onlyBeDependedOnBy(allowedPatterns: List<String>): ModulesRuleBuilder {
        val normalizedPatterns = allowedPatterns.map { normalizeModulePath(it) }
        builder.setShould { module, graph, violations ->
            val dependents =
                graph.getAllModules().filter { other ->
                    other.dependencies.any { dep -> dep.targetPath == module.path }
                }
            for (dep in dependents) {
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

    fun onlyBeDependedOnBy(vararg allowedPatterns: String): ModulesRuleBuilder =
        onlyBeDependedOnBy(allowedPatterns.asList())

    infix fun onlyBeDependedOnBy(predicate: (String) -> Boolean): ModulesRuleBuilder =
        onlyBeDependedOnBy("custom predicate", predicate)

    fun onlyBeDependedOnBy(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            val dependents =
                graph.getAllModules().filter { other ->
                    other.dependencies.any { dep -> dep.targetPath == module.path }
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

    fun notDependOnExternalLibraries(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            val resolvedDeps = graph.requireExternalDependencies().modules[module.path] ?: emptyList()
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

    fun onlyDependOnExternalLibraries(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            val resolvedDeps = graph.requireExternalDependencies().modules[module.path] ?: emptyList()
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

    fun notDependOnExternalLibraries(
        coordinates: List<String>,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder =
        notDependOnExternalLibraries(
            *coordinates.toTypedArray(),
            includeTransitive = includeTransitive,
        )

    fun onlyDependOnExternalLibraries(
        coordinates: List<String>,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder =
        onlyDependOnExternalLibraries(
            *coordinates.toTypedArray(),
            includeTransitive = includeTransitive,
        )

    fun dependOnExternalLibrary(
        coordinate: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = dependOnExternalLibraries(coordinate, includeTransitive = includeTransitive)

    fun haveDependency(
        coordinate: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = dependOnExternalLibrary(coordinate, includeTransitive = includeTransitive)

    fun haveDependencies(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = dependOnExternalLibraries(*coordinates, includeTransitive = includeTransitive)

    fun notHaveDependency(
        coordinate: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = notDependOnExternalLibraries(coordinate, includeTransitive = includeTransitive)

    fun notHaveDependencies(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder = notDependOnExternalLibraries(*coordinates, includeTransitive = includeTransitive)

    fun dependOnExternalLibraries(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            val resolvedDeps = graph.requireExternalDependencies().modules[module.path] ?: emptyList()
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

    fun dependOnExternalLibraries(
        coordinates: List<String>,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder =
        dependOnExternalLibraries(
            *coordinates.toTypedArray(),
            includeTransitive = includeTransitive,
        )

    infix fun dependOnModule(targetPath: String): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
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

    infix fun dependOnModules(targetPaths: List<String>): ModulesRuleBuilder {
        targetPaths.forEach { dependOnModule(it) }
        return builder
    }

    fun dependOnModules(vararg targetPaths: String): ModulesRuleBuilder = dependOnModules(targetPaths.toList())

    fun beFreeOfCycles(): ModulesRuleBuilder {
        builder.setShould { _, graph, violations ->
            val adjacency =
                graph.getAllModules().associate { module ->
                    module.path to module.dependencies.map { it.targetPath }.toSet()
                }
            val cycles = SliceCycleDetector.findCycles(adjacency)
            if (cycles.isNotEmpty()) {
                for (cycle in cycles) {
                    val rendered = (cycle + cycle.first()).joinToString(" -> ")
                    violations.add(getMessage("module.should.beFreeOfCycles", rendered))
                }
            }
        }
        return builder
    }

    infix fun dependOnModuleApi(targetPath: String): ModulesRuleBuilder =
        dependOnModuleViaConfiguration(targetPath, "api")

    infix fun dependOnModuleImplementation(targetPath: String): ModulesRuleBuilder =
        dependOnModuleViaConfiguration(targetPath, "implementation")

    fun dependOnModuleViaConfiguration(
        targetPath: String,
        configuration: String,
    ): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
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

    fun notDependOnModuleViaConfiguration(
        targetPath: String,
        configuration: String,
    ): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
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

    infix fun dependOnModuleTransitively(targetPath: String): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, graph, violations ->
            val visited = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.addAll(module.dependencies.map { it.targetPath })
            var found = false
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, current)) {
                    found = true
                    break
                }
                if (visited.add(current)) {
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

    infix fun notDependOnModuleTransitively(targetPath: String): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, graph, violations ->
            val visited = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.addAll(module.dependencies.map { it.targetPath })
            var found = false
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, current)) {
                    found = true
                    break
                }
                if (visited.add(current)) {
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

    fun beStandalone(): ModulesRuleBuilder {
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

    fun beLeafModule(): ModulesRuleBuilder = beStandalone()
}
