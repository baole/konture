/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.normalizeModulePath

/**
 * Fluent API for defining assertion rules on Gradle modules.
 */
@Suppress("LargeClass")
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
                    getMessage("module.should.notDependOnModule", module.path, normalizedTarget),
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
    infix fun notDependOnModule(predicate: (String) -> Boolean): ModulesRuleBuilder =
        notDependOnModule("custom predicate", predicate)

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
                    getMessage("module.should.notDependOnModulePredicate", module.path, description, paths),
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
    infix fun onlyDependOnModules(allowedPattern: String): ModulesRuleBuilder =
        onlyDependOnModules(listOf(allowedPattern))

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

    /**
     * Asserts that selected modules depend only on modules matching the specified allowed patterns.
     *
     * @param allowedPatterns Glob patterns representing modules that are permitted to be dependencies.
     */
    fun onlyDependOnModules(vararg allowedPatterns: String): ModulesRuleBuilder =
        onlyDependOnModules(allowedPatterns.asList())

    /**
     * Asserts that selected modules depend only on modules matching the predicate.
     *
     * @param predicate Predicate checking target module path.
     */
    infix fun onlyDependOnModules(predicate: (String) -> Boolean): ModulesRuleBuilder =
        onlyDependOnModules("custom predicate", predicate)

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

    /**
     * Asserts that selected modules are depended on only by modules matching the specified allowed pattern.
     *
     * @param allowedPattern Glob pattern of a module allowed to depend on the selected modules.
     */
    infix fun onlyBeDependedOnBy(allowedPattern: String): ModulesRuleBuilder =
        onlyBeDependedOnBy(listOf(allowedPattern))

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
                    violations.add(
                        getMessage("module.should.notBeDependedOnByModules", module.path, dep.path),
                    )
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
    fun onlyBeDependedOnBy(vararg allowedPatterns: String): ModulesRuleBuilder =
        onlyBeDependedOnBy(allowedPatterns.asList())

    /**
     * Asserts that selected modules are depended on only by modules matching the predicate.
     *
     * @param predicate Predicate checking dependent module path.
     */
    infix fun onlyBeDependedOnBy(predicate: (String) -> Boolean): ModulesRuleBuilder =
        onlyBeDependedOnBy("custom predicate", predicate)

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

    /**
     * Asserts that selected modules satisfy a custom condition.
     *
     * @param assertion Custom assertion checking the module.
     */
    infix fun satisfy(assertion: (Module) -> Boolean): ModulesRuleBuilder =
        satisfy("custom condition") { module, _ -> assertion(module) }

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
                violations.add(
                    getMessage("module.should.satisfyCustom", module.path, description),
                )
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
                violations.add(
                    getMessage("module.should.satisfyAtLeastOneNested", module.path),
                )
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
                    violations.add(
                        getMessage("module.should.notSatisfyNested", module.path),
                    )
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

    /**
     * Asserts that the module graph has no cyclic dependencies between modules.
     */
    fun beFreeOfCycles(): ModulesRuleBuilder {
        builder.setShould { _, graph, violations ->
            val adjacency =
                graph.getAllModules().associate { module ->
                    module.path to module.dependencies.map { it.targetPath }.toSet()
                }
            val cycles = io.github.baole.konture.impl.SliceCycleDetector.findCycles(adjacency)
            if (cycles.isNotEmpty()) {
                for (cycle in cycles) {
                    val rendered = (cycle + cycle.first()).joinToString(" -> ")
                    violations.add(getMessage("module.should.beFreeOfCycles", rendered))
                }
            }
        }
        return builder
    }

    infix fun applyPlugin(pluginId: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (!module.appliedPlugins.contains(pluginId)) {
                violations.add(getMessage("module.should.applyPlugin", module.path, pluginId))
            }
        }
        return builder
    }

    infix fun havePlugin(pluginId: String): ModulesRuleBuilder = applyPlugin(pluginId)

    infix fun havePlugins(pluginIds: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val missing = pluginIds.filter { !module.appliedPlugins.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(getMessage("module.should.applyPlugin", module.path, missing.joinToString()))
            }
        }
        return builder
    }

    fun havePlugins(vararg pluginIds: String): ModulesRuleBuilder = havePlugins(pluginIds.toList())

    infix fun notApplyPlugin(pluginId: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.appliedPlugins.contains(pluginId)) {
                violations.add(getMessage("module.should.notApplyPlugin", module.path, pluginId))
            }
        }
        return builder
    }

    infix fun notHavePlugin(pluginId: String): ModulesRuleBuilder = notApplyPlugin(pluginId)

    infix fun notHavePlugins(pluginIds: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val applied = pluginIds.filter { module.appliedPlugins.contains(it) }
            if (applied.isNotEmpty()) {
                violations.add(getMessage("module.should.notApplyPlugin", module.path, applied.joinToString()))
            }
        }
        return builder
    }

    fun notHavePlugins(vararg pluginIds: String): ModulesRuleBuilder = notHavePlugins(pluginIds.toList())

    fun containClasses(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.classes.isEmpty()) {
                violations.add(getMessage("module.should.containClasses", module.path))
            }
        }
        return builder
    }

    fun notContainClasses(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.classes.isNotEmpty()) {
                violations.add(getMessage("module.should.notContainClasses", module.path))
            }
        }
        return builder
    }

    infix fun haveSourceSet(sourceSetName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val hasSourceSet = module.sourceSets.any { it.name == sourceSetName }
            if (!hasSourceSet) {
                violations.add(getMessage("module.should.haveSourceSet", module.path, sourceSetName))
            }
        }
        return builder
    }

    infix fun haveSourceSets(sourceSetNames: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val missing = sourceSetNames.filter { name -> module.sourceSets.none { it.name == name } }
            if (missing.isNotEmpty()) {
                violations.add(getMessage("module.should.haveSourceSet", module.path, missing.joinToString()))
            }
        }
        return builder
    }

    fun haveSourceSets(vararg sourceSetNames: String): ModulesRuleBuilder = haveSourceSets(sourceSetNames.toList())

    fun containFiles(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.files.isEmpty()) {
                violations.add(getMessage("module.should.containFiles", module.path))
            }
        }
        return builder
    }

    fun beEmpty(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.files.isNotEmpty() || module.classes.isNotEmpty()) {
                violations.add(getMessage("module.should.beEmpty", module.path))
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
                    "Module '${module.path}' should depend on '$normalizedTarget' via configuration '$configuration'",
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
                    "Module '${module.path}' should not depend on '$normalizedTarget' via configuration '$configuration'",
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
                violations.add("Module '${module.path}' does not transitively depend on '$normalizedTarget'")
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
                violations.add("Module '${module.path}' transitively depends on prohibited module '$normalizedTarget'")
            }
        }
        return builder
    }

    fun beStandalone(): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.dependencies.isNotEmpty()) {
                violations.add(
                    "Module '${module.path}' should be standalone but has dependencies: ${module.dependencies.map { it.targetPath }}",
                )
            }
        }
        return builder
    }

    fun beLeafModule(): ModulesRuleBuilder = beStandalone()

    infix fun haveBuildId(buildId: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.buildId != buildId) {
                violations.add("Module '${module.path}' has buildId '${module.buildId}', expected '$buildId'")
            }
        }
        return builder
    }

    infix fun notHaveBuildId(buildId: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.buildId == buildId) {
                violations.add("Module '${module.path}' has prohibited buildId '$buildId'")
            }
        }
        return builder
    }

    infix fun haveProjectDir(dirPattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(dirPattern, module.projectDir)) {
                violations.add("Module '${module.path}' projectDir '${module.projectDir}' does not match '$dirPattern'")
            }
        }
        return builder
    }

    infix fun notHaveProjectDir(dirPattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(dirPattern, module.projectDir)) {
                violations.add(
                    "Module '${module.path}' projectDir '${module.projectDir}' matches prohibited '$dirPattern'",
                )
            }
        }
        return builder
    }

    infix fun containClassesInPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (!module.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }) {
                violations.add("Module '${module.path}' does not contain classes in package '$packagePattern'")
            }
        }
        return builder
    }

    infix fun notContainClassesInPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }) {
                violations.add("Module '${module.path}' contains classes in prohibited package '$packagePattern'")
            }
        }
        return builder
    }

    infix fun containClassesWithAnnotation(annotationFqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val matches =
                module.classes.any { cls ->
                    cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
                }
            if (!matches) {
                violations.add("Module '${module.path}' does not contain classes with annotation '$annotationFqName'")
            }
        }
        return builder
    }

    infix fun notContainClassesWithAnnotation(annotationFqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val matches =
                module.classes.any { cls ->
                    cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
                }
            if (matches) {
                violations.add(
                    "Module '${module.path}' contains classes with prohibited annotation '$annotationFqName'",
                )
            }
        }
        return builder
    }

    infix fun containClass(fqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (!module.classes.any { it.fqName == fqName || it.name == fqName }) {
                violations.add("Module '${module.path}' does not contain class '$fqName'")
            }
        }
        return builder
    }

    infix fun notContainClass(fqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            if (module.classes.any { it.fqName == fqName || it.name == fqName }) {
                violations.add("Module '${module.path}' contains prohibited class '$fqName'")
            }
        }
        return builder
    }

    infix fun containClass(type: kotlin.reflect.KClass<*>): ModulesRuleBuilder =
        containClass(type.kontureQualifiedName())

    infix fun notContainClass(type: kotlin.reflect.KClass<*>): ModulesRuleBuilder =
        notContainClass(type.kontureQualifiedName())

    infix fun containClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): ModulesRuleBuilder =
        containClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun notContainClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): ModulesRuleBuilder =
        notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun notHaveName(path: String): ModulesRuleBuilder {
        val normalized = normalizeModulePath(path)
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (module.path == normalized || simpleName == path) {
                violations.add(getMessage("module.should.notHaveName", module.path, path))
            }
        }
        return builder
    }

    infix fun notHaveName(paths: List<String>): ModulesRuleBuilder {
        val normalized = paths.map { normalizeModulePath(it) }
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (normalized.contains(module.path) || paths.contains(simpleName)) {
                violations.add(getMessage("module.should.notHaveNameAny", module.path, paths.joinToString()))
            }
        }
        return builder
    }

    fun notHaveName(vararg paths: String): ModulesRuleBuilder = notHaveName(paths.toList())

    infix fun notHaveNameStartingWith(prefix: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (simpleName.startsWith(prefix) || module.path.startsWith(prefix)) {
                violations.add(getMessage("module.should.notHaveNameStartingWith", module.path, prefix))
            }
        }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            val matching = prefixes.filter { simpleName.startsWith(it) || module.path.startsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("module.should.notHaveNameStartingWithAny", module.path, matching.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): ModulesRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

    infix fun notHaveNameEndingWith(suffix: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (simpleName.endsWith(suffix) || module.path.endsWith(suffix)) {
                violations.add(getMessage("module.should.notHaveNameEndingWith", module.path, suffix))
            }
        }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            val matching = suffixes.filter { simpleName.endsWith(it) || module.path.endsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("module.should.notHaveNameEndingWithAny", module.path, matching.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): ModulesRuleBuilder = notHaveNameEndingWith(suffixes.toList())

    infix fun notHaveNameMatching(pattern: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            if (PatternMatchers.matchesModuleGlob(pattern, module.path) || PatternMatchers.matchesSimpleGlob(pattern, simpleName)) {
                violations.add(getMessage("module.should.notHaveNameMatching", module.path, pattern))
            }
        }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            val simpleName = module.path.substringAfterLast(":")
            val matching =
                patterns.filter {
                    PatternMatchers.matchesModuleGlob(it, module.path) || PatternMatchers.matchesSimpleGlob(it, simpleName)
                }
            if (matching.isNotEmpty()) {
                violations.add(getMessage("module.should.notHaveNameMatchingAny", module.path, matching.joinToString()))
            }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): ModulesRuleBuilder = notHaveNameMatching(patterns.toList())

    /** Fails for every call usage matching [fqName] in any file in selected modules. */
    fun notCall(fqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (file in module.files) {
                val calls = file.usages.filter { PatternMatchers.isCallUsageMatch(it, fqName) }
                for (usage in calls) {
                    violations.add(
                        "Module '${module.path}' file '${file.name}' calls prohibited target '$fqName' (expression: '${usage.rawExpression}')",
                    )
                }
            }
        }
        return builder
    }

    fun notCall(kClass: kotlin.reflect.KClass<*>): ModulesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every class reference usage matching [fqName] in any file in selected modules. */
    fun notReferenceClass(fqName: String): ModulesRuleBuilder {
        builder.setShould { module, _, violations ->
            for (file in module.files) {
                val refs = file.usages.filter { it.kind == UsageKind.CLASS_REFERENCE && it.targetFqName == fqName }
                for (usage in refs) {
                    violations.add(
                        "Module '${module.path}' file '${file.name}' references prohibited class '$fqName'",
                    )
                }
            }
        }
        return builder
    }

    fun notReferenceClass(kClass: kotlin.reflect.KClass<*>): ModulesRuleBuilder =
        notReferenceClass(
            kClass.kontureQualifiedName(),
        )
}

inline fun <reified T : Any> ModulesShould.notCall(): ModulesRuleBuilder = notCall(T::class)

inline fun <reified T : Any> ModulesShould.notReferenceClass(): ModulesRuleBuilder = notReferenceClass(T::class)
