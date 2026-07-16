/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.normalizeModulePath

/**
 * Fluent API for defining assertion rules on Gradle modules.
 */
@KontureDsl
class ModulesShould internal constructor(
    private val builder: ModulesRuleBuilder,
) {
    /**
     * Asserts that selected modules do not depend on the specified target module.
     *
     * @param targetPath The Gradle path or glob pattern of the module that should not be depended on.
     */
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
                    "Module ${module.path} should not depend on $normalizedTarget, but a dependency was found.",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules do not depend on any of the specified target modules.
     *
     * @param targetPaths The list of Gradle paths or glob patterns of the modules that should not be depended on.
     */
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
                    "Module ${module.path} should not depend on any of [${normalizedTargets.joinToString()}], but dependencies were found: ${offending.joinToString { it.targetPath }}",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules do not depend on any of the specified target modules.
     *
     * @param targetPaths The vararg list of Gradle paths or glob patterns of the modules that should not be depended on.
     */
    fun notDependOnModule(vararg targetPaths: String): ModulesRuleBuilder = notDependOnModule(targetPaths.asList())

    /**
     * Asserts that selected modules do not depend on any module matching the predicate.
     *
     * @param predicate Predicate checking target module path.
     */
    infix fun notDependOnModule(predicate: (String) -> Boolean): ModulesRuleBuilder = notDependOnModule("custom predicate", predicate)

    /**
     * Asserts that selected modules do not depend on any module matching the predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking target module path.
     */
    fun notDependOnModule(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val offendingDeps = module.dependencies.filter { dep -> predicate(dep.targetPath) }
            if (offendingDeps.isNotEmpty()) {
                val paths = offendingDeps.joinToString { it.targetPath }
                violations.add(
                    "Module ${module.path} should not depend on $description, but dependency found to: $paths",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules depend only on modules matching the specified allowed pattern.
     *
     * @param allowedPattern Glob pattern representing a module that is permitted to be a dependency.
     */
    infix fun onlyDependOnModules(allowedPattern: String): ModulesRuleBuilder = onlyDependOnModules(listOf(allowedPattern))

    /**
     * Asserts that selected modules depend only on modules matching the specified allowed patterns.
     *
     * @param allowedPatterns Glob patterns representing modules that are permitted to be dependencies.
     */
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
                        "Module ${module.path} depends on ${dep.targetPath}, which is not allowed by pattern(s): ${normalizedPatterns.joinToString()}",
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules depend only on modules matching the specified allowed patterns.
     *
     * @param allowedPatterns Glob patterns representing modules that are permitted to be dependencies.
     */
    fun onlyDependOnModules(vararg allowedPatterns: String): ModulesRuleBuilder = onlyDependOnModules(allowedPatterns.asList())

    /**
     * Asserts that selected modules depend only on modules matching the predicate.
     *
     * @param predicate Predicate checking target module path.
     */
    infix fun onlyDependOnModules(predicate: (String) -> Boolean): ModulesRuleBuilder = onlyDependOnModules("custom predicate", predicate)

    /**
     * Asserts that selected modules depend only on modules matching the predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking target module path.
     */
    fun onlyDependOnModules(
        description: String,
        predicate: (String) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (dep in module.dependencies) {
                if (!predicate(dep.targetPath)) {
                    violations.add(
                        "Module ${module.path} depends on ${dep.targetPath}, which is not allowed by: $description",
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules are depended on only by modules matching the specified allowed pattern.
     *
     * @param allowedPattern Glob pattern of a module allowed to depend on the selected modules.
     */
    infix fun onlyBeDependedOnBy(allowedPattern: String): ModulesRuleBuilder = onlyBeDependedOnBy(listOf(allowedPattern))

    /**
     * Asserts that selected modules are depended on only by modules matching the specified allowed patterns.
     *
     * @param allowedPatterns Glob patterns of modules allowed to depend on the selected modules.
     */
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
                    violations.add("Module ${module.path} is depended on by ${dep.path}, which is not allowed.")
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules are depended on only by modules matching the specified allowed patterns.
     *
     * @param allowedPatterns Glob patterns of modules allowed to depend on the selected modules.
     */
    fun onlyBeDependedOnBy(vararg allowedPatterns: String): ModulesRuleBuilder = onlyBeDependedOnBy(allowedPatterns.asList())

    /**
     * Asserts that selected modules are depended on only by modules matching the predicate.
     *
     * @param predicate Predicate checking dependent module path.
     */
    infix fun onlyBeDependedOnBy(predicate: (String) -> Boolean): ModulesRuleBuilder = onlyBeDependedOnBy("custom predicate", predicate)

    /**
     * Asserts that selected modules are depended on only by modules matching the predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking dependent module path.
     */
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
                        "Module ${module.path} is depended on by ${dep.path}, which is not allowed by: $description",
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules satisfy a custom condition.
     *
     * @param assertion Custom assertion checking the module.
     */
    infix fun satisfy(assertion: (Module) -> Boolean): ModulesRuleBuilder = satisfy("custom condition") { module, _ -> assertion(module) }

    /**
     * Asserts that selected modules satisfy a custom condition.
     *
     * @param description A descriptive string for the custom condition used in violations.
     * @param assertion Custom assertion checking the module.
     */
    infix fun satisfy(description: String): ModulesRuleBuilder = satisfy(description) { module, _ -> false }

    private fun satisfy(
        description: String,
        assertion: (Module, ProjectGraph) -> Boolean,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            if (!assertion(module, graph)) {
                violations.add("Module ${module.path} should satisfy: $description")
            }
        }
        return builder
    }

    /**
     * Satisfies an arbitrary custom assertion logic with custom violations builder.
     */
    fun satisfy(assertion: (Module, MutableList<String>) -> Unit): ModulesRuleBuilder {
        builder.setShould { module, _, violations -> assertion(module, violations) }
        return builder
    }

    /**
     * Asserts that at least one of the nested assertion blocks is satisfied.
     */
    fun anyOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { module, g, violations ->
            val tempViolationsList =
                assertions.map { assertion ->
                    val temp = mutableListOf<String>()
                    assertion(module, g, temp)
                    temp
                }
            if (tempViolationsList.all { it.isNotEmpty() }) {
                violations.add("Module ${module.path} should satisfy at least one of the nested assertions.")
            }
        }
        return builder
    }

    /**
     * Asserts that all of the nested assertion blocks are satisfied.
     */
    fun allOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { module, g, violations ->
            assertions.forEach { assertion ->
                assertion(module, g, violations)
            }
        }
        return builder
    }

    /**
     * Asserts that none of the nested assertion blocks are satisfied.
     */
    fun noneOf(vararg blocks: ModulesShould.() -> Unit): ModulesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { module, g, violations ->
            assertions.forEach { assertion ->
                val temp = mutableListOf<String>()
                assertion(module, g, temp)
                if (temp.isEmpty()) {
                    violations.add("Module ${module.path} should not satisfy the nested assertion.")
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules do not depend on the specified external maven libraries.
     *
     * Supports simple glob matching (e.g. "org.jetbrains.kotlin:*" or "com.google.*").
     * If the coordinate pattern does not contain ':', it matches against either group or name.
     *
     * @param coordinates Maven coordinate pattern(s).
     * @param includeTransitive True to assert against both direct and transitive external dependencies (defaults to true).
     */
    fun notDependOnExternalLibraries(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            val resolvedDeps = graph.externalDependencies.modules[module.path] ?: emptyList()
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
                    "Module ${module.path} should not depend on external libraries [${coordinates.joinToString()}], but found: $coords",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected modules only depend on the specified external maven libraries.
     *
     * Any external dependencies of the selected modules not matching the specified patterns will
     * cause a violation.
     *
     * Supports simple glob matching (e.g. "org.jetbrains.kotlin:*" or "com.google.*").
     * If the coordinate pattern does not contain ':', it matches against either group or name.
     *
     * @param coordinates Permitted Maven coordinate pattern(s).
     * @param includeTransitive True to assert against both direct and transitive external dependencies (defaults to true).
     */
    fun onlyDependOnExternalLibraries(
        vararg coordinates: String,
        includeTransitive: Boolean = true,
    ): ModulesRuleBuilder {
        builder.setShould { module, graph, violations ->
            val resolvedDeps = graph.externalDependencies.modules[module.path] ?: emptyList()
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
                    "Module ${module.path} depends on external libraries not in the allowed list [${coordinates.joinToString()}]: $coords",
                )
            }
        }
        return builder
    }
}
