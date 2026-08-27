/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.isTestConfiguration
import io.github.baole.konture.impl.normalizeModulePath

/** Inbound dependency assertions for Gradle module rules. */
public interface ModulesShouldInboundDependencyAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: ModulesRuleBuilder

    /** Filter or assertion criteria for may be depended on by. */
    public infix fun mayBeDependedOnBy(dependentPath: String): ModulesRuleBuilder =
        mayBeDependedOnBy(listOf(dependentPath))

    /** Filter or assertion criteria for may be depended on by. */
    public infix fun mayBeDependedOnBy(dependentPaths: List<String>): ModulesRuleBuilder {
        val normalizedPatterns = dependentPaths.map { normalizeModulePath(it) }
        builder.setShould { module, graph, violations ->
            val dependents =
                graph.getAllModules().filter { other ->
                    other.dependencies.any { dep -> !dep.isTestConfiguration() && dep.targetPath == module.path }
                }
            for (dep in dependents) {
                val isAllowed =
                    normalizedPatterns.any { pattern ->
                        dep.path == pattern || PatternMatchers.matchesModuleGlob(pattern, dep.path)
                    }
                if (!isAllowed) {
                    violations.add(
                        getMessage(
                            "module.should.mayBeDependedOnBy",
                            module.path,
                            normalizedPatterns.joinToString(),
                            dep.path,
                        ),
                    )
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for may be depended on by. */
    public fun mayBeDependedOnBy(vararg dependentPaths: String): ModulesRuleBuilder =
        mayBeDependedOnBy(dependentPaths.asList())

    /** Filter or assertion criteria for may be depended on by. */
    public infix fun mayBeDependedOnBy(predicate: (String) -> Boolean): ModulesRuleBuilder =
        mayBeDependedOnBy("custom predicate", predicate)

    /** Filter or assertion criteria for may be depended on by. */
    public fun mayBeDependedOnBy(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            val dependents =
                graph.getAllModules().filter { other ->
                    other.dependencies.any { dep -> !dep.isTestConfiguration() && dep.targetPath == module.path }
                }
            for (dep in dependents) {
                if (!predicate(dep.path)) {
                    violations.add(
                        getMessage(
                            "module.should.mayBeDependedOnBy",
                            module.path,
                            description,
                            dep.path,
                        ),
                    )
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for must not be depended on by. */
    public infix fun mustNotBeDependedOnBy(dependentPath: String): ModulesRuleBuilder =
        mustNotBeDependedOnBy(listOf(dependentPath))

    /** Filter or assertion criteria for must not be depended on by. */
    public infix fun mustNotBeDependedOnBy(dependentPaths: List<String>): ModulesRuleBuilder {
        val normalizedTargets = dependentPaths.map { normalizeModulePath(it) }
        builder.setShould { module, graph, violations ->
            val dependents =
                graph.getAllModules().filter { other ->
                    other.dependencies.any { dep -> dep.targetPath == module.path }
                }
            val offending =
                dependents.filter { other ->
                    normalizedTargets.any { target ->
                        other.path == target || PatternMatchers.matchesModuleGlob(target, other.path)
                    }
                }
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "module.should.mustNotBeDependedOnBy",
                        module.path,
                        normalizedTargets.joinToString(),
                        offending.joinToString { it.path },
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for must not be depended on by. */
    public fun mustNotBeDependedOnBy(vararg dependentPaths: String): ModulesRuleBuilder =
        mustNotBeDependedOnBy(dependentPaths.asList())

    /** Filter or assertion criteria for must not be depended on by. */
    public infix fun mustNotBeDependedOnBy(predicate: (String) -> Boolean): ModulesRuleBuilder =
        mustNotBeDependedOnBy("custom predicate", predicate)

    /** Filter or assertion criteria for must not be depended on by. */
    public fun mustNotBeDependedOnBy(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            val dependents =
                graph.getAllModules().filter { other ->
                    other.dependencies.any { dep -> dep.targetPath == module.path }
                }
            val offending = dependents.filter { other -> predicate(other.path) }
            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "module.should.mustNotBeDependedOnBy",
                        module.path,
                        description,
                        offending.joinToString { it.path },
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for only be depended on by. */
    public infix fun onlyBeDependedOnBy(allowedPattern: String): ModulesRuleBuilder = mayBeDependedOnBy(allowedPattern)

    /** Filter or assertion criteria for only be depended on by. */
    public infix fun onlyBeDependedOnBy(allowedPatterns: List<String>): ModulesRuleBuilder =
        mayBeDependedOnBy(allowedPatterns)

    /** Filter or assertion criteria for only be depended on by. */
    public fun onlyBeDependedOnBy(vararg allowedPatterns: String): ModulesRuleBuilder =
        mayBeDependedOnBy(*allowedPatterns)

    /** Filter or assertion criteria for only be depended on by. */
    public infix fun onlyBeDependedOnBy(predicate: (String) -> Boolean): ModulesRuleBuilder =
        mayBeDependedOnBy(predicate)

    /** Filter or assertion criteria for only be depended on by. */
    public fun onlyBeDependedOnBy(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder = mayBeDependedOnBy(description, predicate)
}
