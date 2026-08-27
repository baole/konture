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
                violations.add(
                    getMessage(
                        "module.should.mustNotDependOn",
                        module.path,
                        normalizedTarget,
                        offending.joinToString { it.targetPath },
                    ),
                )
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
                violations.add(
                    getMessage(
                        "module.should.mustNotDependOn",
                        module.path,
                        normalizedTargets.joinToString(),
                        offending.joinToString { it.targetPath },
                    ),
                )
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
                violations.add(
                    getMessage(
                        "module.should.mustNotDependOn",
                        module.path,
                        description,
                        offending.joinToString { it.targetPath },
                    ),
                )
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
                    violations.add(
                        getMessage(
                            "module.should.mayDependOn",
                            module.path,
                            normalizedPatterns.joinToString(),
                            dep.targetPath,
                        ),
                    )
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
                    violations.add(
                        getMessage(
                            "module.should.mayDependOn",
                            module.path,
                            description,
                            dep.targetPath,
                        ),
                    )
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
                    violations.add(
                        getMessage(
                            "module.should.onlyDependOn",
                            module.path,
                            normalizedPatterns.joinToString(),
                            dep.targetPath,
                        ),
                    )
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
                    violations.add(
                        getMessage(
                            "module.should.onlyDependOn",
                            module.path,
                            description,
                            dep.targetPath,
                        ),
                    )
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
