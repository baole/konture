/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.Subject
import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ShortestPathFinder
import io.github.baole.konture.impl.SliceCycleDetector
import io.github.baole.konture.impl.StructuredMessageList
import io.github.baole.konture.impl.isTestConfiguration
import io.github.baole.konture.impl.normalizeModulePath

/** Outbound dependency assertions for Gradle module rules. */
public interface ModulesShouldDependencyAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: ModulesRuleBuilder

    /** Filter or assertion criteria for must not depend on module. */
    public infix fun mustNotDependOn(targetPath: String): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
            val offending =
                module.dependencies.filter { dep ->
                    dep.targetPath == normalizedTarget ||
                        PatternMatchers.matchesModuleGlob(normalizedTarget, dep.targetPath)
                }
            if (offending.isNotEmpty()) {
                val msg =
                    getMessage(
                        "module.should.mustNotDependOn",
                        module.path,
                        normalizedTarget,
                        offending.joinToString { it.targetPath },
                    )
                val targetSubject = Subject.ModuleSubject(offending.first().targetPath)
                val dependencyPath = listOf(Subject.ModuleSubject(module.path), targetSubject)
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                } else {
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for must not depend on module. */
    public infix fun mustNotDependOn(targetPaths: List<String>): ModulesRuleBuilder {
        val normalizedTargets = targetPaths.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
            val offending =
                module.dependencies.filter { dep ->
                    normalizedTargets.any { targetPath ->
                        dep.targetPath == targetPath || PatternMatchers.matchesModuleGlob(targetPath, dep.targetPath)
                    }
                }
            if (offending.isNotEmpty()) {
                val msg =
                    getMessage(
                        "module.should.mustNotDependOn",
                        module.path,
                        normalizedTargets.joinToString(),
                        offending.joinToString { it.targetPath },
                    )
                val targetSubject = Subject.ModuleSubject(offending.first().targetPath)
                val dependencyPath = listOf(Subject.ModuleSubject(module.path), targetSubject)
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                } else {
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for must not depend on module. */
    public fun mustNotDependOn(vararg targetPaths: String): ModulesRuleBuilder = mustNotDependOn(targetPaths.asList())

    /** Filter or assertion criteria for must not depend on module. */
    public infix fun mustNotDependOn(predicate: (String) -> Boolean): ModulesRuleBuilder =
        mustNotDependOn("custom predicate", predicate)

    /** Filter or assertion criteria for must not depend on module. */
    public fun mustNotDependOn(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val offending = module.dependencies.filter { dep -> predicate(dep.targetPath) }
            if (offending.isNotEmpty()) {
                val msg =
                    getMessage(
                        "module.should.mustNotDependOn",
                        module.path,
                        description,
                        offending.joinToString { it.targetPath },
                    )
                val targetSubject = Subject.ModuleSubject(offending.first().targetPath)
                val dependencyPath = listOf(Subject.ModuleSubject(module.path), targetSubject)
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                } else {
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOn(targetPath: String): ModulesRuleBuilder = mustNotDependOn(targetPath)

    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOn(targetPaths: List<String>): ModulesRuleBuilder = mustNotDependOn(targetPaths)

    /** Filter or assertion criteria for not depend on module. */
    public fun notDependOn(vararg targetPaths: String): ModulesRuleBuilder = mustNotDependOn(*targetPaths)

    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOn(predicate: (String) -> Boolean): ModulesRuleBuilder = mustNotDependOn(predicate)

    /** Filter or assertion criteria for not depend on module. */
    public fun notDependOn(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder = mustNotDependOn(description, predicate)

    @Deprecated("Use mustNotDependOn instead.", ReplaceWith("mustNotDependOn(targetPath)"))
    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOnModule(targetPath: String): ModulesRuleBuilder = mustNotDependOn(targetPath)

    @Deprecated("Use mustNotDependOn instead.", ReplaceWith("mustNotDependOn(targetPaths)"))
    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOnModule(targetPaths: List<String>): ModulesRuleBuilder = mustNotDependOn(targetPaths)

    @Deprecated("Use mustNotDependOn instead.", ReplaceWith("mustNotDependOn(*targetPaths)"))
    /** Filter or assertion criteria for not depend on module. */
    public fun notDependOnModule(vararg targetPaths: String): ModulesRuleBuilder = mustNotDependOn(*targetPaths)

    @Deprecated("Use mustNotDependOn instead.", ReplaceWith("mustNotDependOn(predicate)"))
    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOnModule(predicate: (String) -> Boolean): ModulesRuleBuilder = mustNotDependOn(predicate)

    @Deprecated("Use mustNotDependOn instead.", ReplaceWith("mustNotDependOn(description, predicate)"))
    /** Filter or assertion criteria for not depend on module. */
    public fun notDependOnModule(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder = mustNotDependOn(description, predicate)

    @Deprecated("Use mustNotDependOn instead.", ReplaceWith("mustNotDependOn(targetPath)"))
    /** Filter or assertion criteria for not depend on modules. */
    public infix fun notDependOnModules(targetPath: String): ModulesRuleBuilder = mustNotDependOn(targetPath)

    @Deprecated("Use mustNotDependOn instead.", ReplaceWith("mustNotDependOn(targetPaths)"))
    /** Filter or assertion criteria for not depend on modules. */
    public infix fun notDependOnModules(targetPaths: List<String>): ModulesRuleBuilder = mustNotDependOn(targetPaths)

    @Deprecated("Use mustNotDependOn instead.", ReplaceWith("mustNotDependOn(*targetPaths)"))
    /** Filter or assertion criteria for not depend on modules. */
    public fun notDependOnModules(vararg targetPaths: String): ModulesRuleBuilder = mustNotDependOn(*targetPaths)

    /** Filter or assertion criteria for may depend on module. */
    public infix fun mayDependOn(targetPath: String): ModulesRuleBuilder = mayDependOn(listOf(targetPath))

    /** Filter or assertion criteria for may depend on module. */
    public infix fun mayDependOn(targetPaths: List<String>): ModulesRuleBuilder {
        val normalizedPatterns = targetPaths.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
                if (dep.isTestConfiguration()) continue
                val isAllowed =
                    normalizedPatterns.any { pattern ->
                        dep.targetPath == pattern || PatternMatchers.matchesModuleGlob(pattern, dep.targetPath)
                    }
                if (!isAllowed) {
                    val msg =
                        getMessage(
                            "module.should.mayDependOn",
                            module.path,
                            normalizedPatterns.joinToString(),
                            dep.targetPath,
                        )
                    val targetSubject = Subject.ModuleSubject(dep.targetPath)
                    val dependencyPath = listOf(Subject.ModuleSubject(module.path), targetSubject)
                    if (violations is StructuredMessageList) {
                        violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                    } else {
                        violations.add(msg)
                    }
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for may depend on module. */
    public fun mayDependOn(vararg targetPaths: String): ModulesRuleBuilder = mayDependOn(targetPaths.asList())

    /** Filter or assertion criteria for may depend on module. */
    public infix fun mayDependOn(predicate: (String) -> Boolean): ModulesRuleBuilder =
        mayDependOn("custom predicate", predicate)

    /** Filter or assertion criteria for may depend on module. */
    public fun mayDependOn(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
                if (dep.isTestConfiguration()) continue
                if (!predicate(dep.targetPath)) {
                    val msg =
                        getMessage(
                            "module.should.mayDependOn",
                            module.path,
                            description,
                            dep.targetPath,
                        )
                    val targetSubject = Subject.ModuleSubject(dep.targetPath)
                    val dependencyPath = listOf(Subject.ModuleSubject(module.path), targetSubject)
                    if (violations is StructuredMessageList) {
                        violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                    } else {
                        violations.add(msg)
                    }
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for only depend on module. */
    public infix fun onlyDependOn(targetPath: String): ModulesRuleBuilder = onlyDependOn(listOf(targetPath))

    /** Filter or assertion criteria for only depend on module. */
    public infix fun onlyDependOn(targetPaths: List<String>): ModulesRuleBuilder {
        val normalizedPatterns = targetPaths.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
                if (dep.isTestConfiguration()) continue
                val isAllowed =
                    normalizedPatterns.any { pattern ->
                        dep.targetPath == pattern || PatternMatchers.matchesModuleGlob(pattern, dep.targetPath)
                    }
                if (!isAllowed) {
                    val msg =
                        getMessage(
                            "module.should.onlyDependOn",
                            module.path,
                            normalizedPatterns.joinToString(),
                            dep.targetPath,
                        )
                    val targetSubject = Subject.ModuleSubject(dep.targetPath)
                    val dependencyPath = listOf(Subject.ModuleSubject(module.path), targetSubject)
                    if (violations is StructuredMessageList) {
                        violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                    } else {
                        violations.add(msg)
                    }
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for only depend on module. */
    public fun onlyDependOn(vararg targetPaths: String): ModulesRuleBuilder = onlyDependOn(targetPaths.asList())

    /** Filter or assertion criteria for only depend on module. */
    public infix fun onlyDependOn(predicate: (String) -> Boolean): ModulesRuleBuilder =
        onlyDependOn("custom predicate", predicate)

    /** Filter or assertion criteria for only depend on module. */
    public fun onlyDependOn(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
                if (dep.isTestConfiguration()) continue
                if (!predicate(dep.targetPath)) {
                    val msg =
                        getMessage(
                            "module.should.onlyDependOn",
                            module.path,
                            description,
                            dep.targetPath,
                        )
                    val targetSubject = Subject.ModuleSubject(dep.targetPath)
                    val dependencyPath = listOf(Subject.ModuleSubject(module.path), targetSubject)
                    if (violations is StructuredMessageList) {
                        violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                    } else {
                        violations.add(msg)
                    }
                }
            }
        }
        return builder
    }

    @Deprecated("Use onlyDependOn instead.", ReplaceWith("onlyDependOn(allowedPattern)"))
    /** Filter or assertion criteria for only depend on modules. */
    public infix fun onlyDependOnModules(allowedPattern: String): ModulesRuleBuilder = onlyDependOn(allowedPattern)

    @Deprecated("Use onlyDependOn instead.", ReplaceWith("onlyDependOn(allowedPatterns)"))
    /** Filter or assertion criteria for only depend on modules. */
    public infix fun onlyDependOnModules(allowedPatterns: List<String>): ModulesRuleBuilder =
        onlyDependOn(
            allowedPatterns,
        )

    @Deprecated("Use onlyDependOn instead.", ReplaceWith("onlyDependOn(*allowedPatterns)"))
    /** Filter or assertion criteria for only depend on modules. */
    public fun onlyDependOnModules(vararg allowedPatterns: String): ModulesRuleBuilder = onlyDependOn(*allowedPatterns)

    @Deprecated("Use onlyDependOn instead.", ReplaceWith("onlyDependOn(predicate)"))
    /** Filter or assertion criteria for only depend on modules. */
    public infix fun onlyDependOnModules(predicate: (String) -> Boolean): ModulesRuleBuilder = onlyDependOn(predicate)

    @Deprecated("Use onlyDependOn instead.", ReplaceWith("onlyDependOn(description, predicate)"))
    /** Filter or assertion criteria for only depend on modules. */
    public fun onlyDependOnModules(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder = onlyDependOn(description, predicate)

    /** Filter or assertion criteria for depend on module. */
    public infix fun dependOnModule(targetPath: String): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
            val dependsOnTarget =
                module.dependencies.any { dep ->
                    dep.targetPath == normalizedTarget ||
                        PatternMatchers.matchesModuleGlob(normalizedTarget, dep.targetPath)
                }
            if (!dependsOnTarget) {
                val msg = getMessage("module.should.dependOnModule", module.path, normalizedTarget)
                val targetSubject = Subject.ModuleSubject(normalizedTarget)
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject)
                } else {
                    violations.add(msg)
                }
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
            val adjacency =
                graph.getAllModules().associate { module ->
                    module.path to module.dependencies.map { it.targetPath }.toSet()
                }

            val cycles = SliceCycleDetector.findCycles(adjacency)
            if (cycles.isNotEmpty()) {
                for (cycle in cycles) {
                    val rendered = (cycle + cycle.first()).joinToString(" -> ")
                    val cyclePath = (cycle + cycle.first()).map { Subject.ModuleSubject(it) }
                    val msg = getMessage("module.should.beFreeOfCycles", rendered)
                    if (violations is StructuredMessageList) {
                        violations.add(
                            msg,
                            target = Subject.ModuleSubject(cycle.first()),
                            dependencyPath = cyclePath,
                        )
                    } else {
                        violations.add(msg)
                    }
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
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, _, violations ->
            val matches =
                module.dependencies.any { dep ->
                    (dep.targetPath == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, dep.targetPath)) &&
                        dep.configuration.equals(configuration, ignoreCase = true)
                }
            if (!matches) {
                val msg =
                    getMessage("module.should.dependOnModuleViaConfig", module.path, normalizedTarget, configuration)
                val targetSubject = Subject.ModuleSubject(normalizedTarget)
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject)
                } else {
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on module via configuration. */
    public fun notDependOnModuleViaConfiguration(
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
                val msg =
                    getMessage(
                        "module.should.notDependOnModuleViaConfig",
                        module.path,
                        normalizedTarget,
                        configuration,
                    )
                val targetSubject = Subject.ModuleSubject(normalizedTarget)
                val dependencyPath = listOf(Subject.ModuleSubject(module.path), targetSubject)
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                } else {
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for depend on module transitively. */
    public infix fun dependOnModuleTransitively(targetPath: String): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, graph, violations ->
            val path =
                ShortestPathFinder.findShortestModulePathMatching(
                    graph = graph,
                    startModule = module,
                    targetPredicate = { it.path == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, it.path) },
                )
            if (path == null) {
                val msg = getMessage("module.should.transitiveDependOn", module.path, normalizedTarget)
                val targetSubject = Subject.ModuleSubject(normalizedTarget)
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject)
                } else {
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on module transitively. */
    public infix fun notDependOnModuleTransitively(targetPath: String): ModulesRuleBuilder {
        val normalizedTarget = normalizeModulePath(targetPath)
        builder.setShould { module, graph, violations ->
            val path =
                ShortestPathFinder.findShortestModulePathMatching(
                    graph = graph,
                    startModule = module,
                    targetPredicate = { it.path == normalizedTarget || PatternMatchers.matchesModuleGlob(normalizedTarget, it.path) },
                )
            if (path != null && path.size >= 2) {
                val msg = getMessage("module.should.notTransitiveDependOn", module.path, normalizedTarget)
                val targetSubject = Subject.ModuleSubject(path.last())
                val dependencyPath = path.map { Subject.ModuleSubject(it) }
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                } else {
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be standalone. */
    public fun beStandalone(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.dependencies.isNotEmpty()) {
                val msg =
                    getMessage(
                        "module.should.beStandalone",
                        module.path,
                        module.dependencies.map { it.targetPath }.toString(),
                    )
                val targetSubject = module.dependencies.firstOrNull()?.let { Subject.ModuleSubject(it.targetPath) }
                val dependencyPath =
                    if (targetSubject != null) {
                        listOf(Subject.ModuleSubject(module.path), targetSubject)
                    } else {
                        emptyList()
                    }
                if (violations is StructuredMessageList) {
                    violations.add(msg, target = targetSubject, dependencyPath = dependencyPath)
                } else {
                    violations.add(msg)
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be leaf module. */
    public fun beLeafModule(): ModulesRuleBuilder = beStandalone()
}
