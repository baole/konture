/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.normalizeModulePath

/**
 * Fluent API for defining filtering conditions on Gradle modules.
 */
@KontureDsl
class ModulesThat internal constructor(
    private val builder: ModulesRuleBuilder,
) {
    /** Logical NOT operator for negating the next filter condition. */
    fun not(): ModulesThat = builder.not()

    infix fun haveNamePath(path: String): ModulesRuleBuilder {
        val normalized = normalizeModulePath(path)
        builder.setThat { it.path == normalized }
        return builder
    }

    /**
     * Restricts the rules to modules with an exact matching Gradle path in the specified list of paths.
     *
     * @param paths The list of Gradle paths of the module (e.g., ":core", ":app").
     */
    infix fun haveNamePath(paths: List<String>): ModulesRuleBuilder {
        val normalizedPaths = paths.map { normalizeModulePath(it) }
        builder.setThat { normalizedPaths.contains(it.path) }
        return builder
    }

    /**
     * Restricts the rules to modules with an exact matching Gradle path in the specified paths.
     *
     * @param paths The vararg list of Gradle paths of the module (e.g., ":core", ":app").
     */
    fun haveNamePath(vararg paths: String): ModulesRuleBuilder = haveNamePath(paths.asList())

    /**
     * Restricts the rules to modules with a Gradle path matching the given predicate.
     *
     * @param predicate The predicate to match the module path.
     */
    infix fun haveNamePath(predicate: (String) -> Boolean): ModulesRuleBuilder {
        builder.setThat { predicate(it.path) }
        return builder
    }

    infix fun haveName(path: String): ModulesRuleBuilder = haveNamePath(path)

    infix fun haveName(paths: List<String>): ModulesRuleBuilder = haveNamePath(paths)

    fun haveName(vararg paths: String): ModulesRuleBuilder = haveNamePath(*paths)

    infix fun haveNameStartingWith(prefix: String): ModulesRuleBuilder {
        builder.setThat { it.path.removePrefix(":").startsWith(prefix.removePrefix(":")) }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): ModulesRuleBuilder {
        builder.setThat { module ->
            prefixes.any { module.path.removePrefix(":").startsWith(it.removePrefix(":")) }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): ModulesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameEndingWith(suffix: String): ModulesRuleBuilder {
        builder.setThat { it.path.endsWith(suffix) }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> suffixes.any { module.path.endsWith(it) } }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): ModulesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /**
     * Restricts the rules to modules whose Gradle path matches the specified glob pattern.
     *
     * @param pattern Glob pattern (e.g., ":feature-*", ":core-**").
     */
    infix fun haveNameMatching(pattern: String): ModulesRuleBuilder {
        builder.setThat { PatternMatchers.matchesModuleGlob(pattern, it.path) }
        return builder
    }

    /**
     * Restricts the rules to modules whose Gradle path matches any of the specified glob patterns.
     *
     * @param patterns Glob patterns (e.g., ":feature-*", ":core-**").
     */
    infix fun haveNameMatching(patterns: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> patterns.any { PatternMatchers.matchesModuleGlob(it, module.path) } }
        return builder
    }

    /**
     * Restricts the rules to modules whose Gradle path matches any of the specified glob patterns.
     *
     * @param patterns Glob patterns (e.g., ":feature-*", ":core-**").
     */
    fun haveNameMatching(vararg patterns: String): ModulesRuleBuilder = haveNameMatching(patterns.toList())

    infix fun dependOnModule(modulePath: String): ModulesRuleBuilder {
        val normalized = normalizeModulePath(modulePath)
        builder.setThat { module ->
            module.dependencies.any { normalizeModulePath(it.targetPath) == normalized }
        }
        return builder
    }

    infix fun dependOnModules(modulePaths: List<String>): ModulesRuleBuilder {
        val normalized = modulePaths.map { normalizeModulePath(it) }
        builder.setThat { module ->
            module.dependencies.any { normalized.contains(normalizeModulePath(it.targetPath)) }
        }
        return builder
    }

    fun dependOnModules(vararg modulePaths: String): ModulesRuleBuilder = dependOnModules(modulePaths.toList())

    infix fun applyPlugin(pluginId: String): ModulesRuleBuilder {
        builder.setThat { module -> module.appliedPlugins.contains(pluginId) }
        return builder
    }

    infix fun havePlugin(pluginId: String): ModulesRuleBuilder = applyPlugin(pluginId)

    fun havePlugins(vararg pluginIds: String): ModulesRuleBuilder {
        builder.setThat { module -> pluginIds.all { module.appliedPlugins.contains(it) } }
        return builder
    }

    /**
     * Restricts the rules to modules matching the specified predicate.
     *
     * @param predicate The predicate to filter modules.
     */
    infix fun matching(predicate: (Module) -> Boolean): ModulesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Satisfies an arbitrary custom predicate logic.
     */
    fun satisfy(predicate: (Module) -> Boolean): ModulesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Matches if any of the nested condition blocks are satisfied.
     */
    fun anyOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /**
     * Matches if all of the nested condition blocks are satisfied.
     */
    fun allOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    infix fun haveSourceSet(sourceSetName: String): ModulesRuleBuilder {
        builder.setThat { module -> module.sourceSets.any { it.name == sourceSetName } }
        return builder
    }

    infix fun haveSourceSet(sourceSetNames: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> sourceSetNames.all { name -> module.sourceSets.any { it.name == name } } }
        return builder
    }

    fun haveSourceSet(vararg sourceSetNames: String): ModulesRuleBuilder = haveSourceSet(sourceSetNames.toList())

    /**
     * Matches if none of the nested condition blocks are satisfied.
     */
    fun noneOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }

    infix fun applyPlugin(pluginIds: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> pluginIds.all { module.appliedPlugins.contains(it) } }
        return builder
    }

    infix fun havePlugins(pluginIds: List<String>): ModulesRuleBuilder = applyPlugin(pluginIds)

    infix fun notDependOnModule(modulePath: String): ModulesRuleBuilder {
        val normalized = normalizeModulePath(modulePath)
        builder.setThat { module ->
            module.dependencies.none { normalizeModulePath(it.targetPath) == normalized }
        }
        return builder
    }

    infix fun notDependOnModules(modulePaths: List<String>): ModulesRuleBuilder {
        val normalized = modulePaths.map { normalizeModulePath(it) }
        builder.setThat { module ->
            module.dependencies.none { normalized.contains(normalizeModulePath(it.targetPath)) }
        }
        return builder
    }

    fun notDependOnModules(vararg modulePaths: String): ModulesRuleBuilder = notDependOnModules(modulePaths.toList())

    infix fun notApplyPlugin(pluginId: String): ModulesRuleBuilder {
        builder.setThat { module -> !module.appliedPlugins.contains(pluginId) }
        return builder
    }

    infix fun notHavePlugin(pluginId: String): ModulesRuleBuilder = notApplyPlugin(pluginId)

    infix fun notHavePlugins(pluginIds: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> pluginIds.none { module.appliedPlugins.contains(it) } }
        return builder
    }

    fun notHavePlugins(vararg pluginIds: String): ModulesRuleBuilder = notHavePlugins(pluginIds.toList())

    infix fun notHaveSourceSet(sourceSetName: String): ModulesRuleBuilder {
        builder.setThat { module -> module.sourceSets.none { it.name == sourceSetName } }
        return builder
    }

    infix fun notHaveName(path: String): ModulesRuleBuilder {
        val normalized = normalizeModulePath(path)
        builder.setThat { it.path != normalized }
        return builder
    }

    infix fun notHaveNameMatching(pattern: String): ModulesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesModuleGlob(pattern, it.path) }
        return builder
    }

    infix fun notHaveNameStartingWith(prefix: String): ModulesRuleBuilder {
        builder.setThat { !it.path.removePrefix(":").startsWith(prefix.removePrefix(":")) }
        return builder
    }

    infix fun notHaveNameEndingWith(suffix: String): ModulesRuleBuilder {
        builder.setThat { !it.path.endsWith(suffix) }
        return builder
    }

    infix fun haveBuildId(buildId: String): ModulesRuleBuilder {
        builder.setThat { it.buildId == buildId }
        return builder
    }

    infix fun notHaveBuildId(buildId: String): ModulesRuleBuilder {
        builder.setThat { it.buildId != buildId }
        return builder
    }

    infix fun haveProjectDir(dirPattern: String): ModulesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(dirPattern, it.projectDir) }
        return builder
    }

    infix fun notHaveProjectDir(dirPattern: String): ModulesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(dirPattern, it.projectDir) }
        return builder
    }

    infix fun containClassesInPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        }
        return builder
    }

    infix fun notContainClassesInPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classes.none { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        }
        return builder
    }

    infix fun containClassesWithAnnotation(annotationFqName: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classes.any { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    infix fun notContainClassesWithAnnotation(annotationFqName: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classesFor(builder.sourceSets).none { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    infix fun containClass(fqName: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classesFor(builder.sourceSets).any { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    infix fun notContainClass(fqName: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classesFor(builder.sourceSets).none { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    infix fun containClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): ModulesRuleBuilder =
        containClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun notContainClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): ModulesRuleBuilder =
        notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun containClass(type: kotlin.reflect.KClass<*>): ModulesRuleBuilder =
        containClass(type.kontureQualifiedName())

    infix fun notContainClass(type: kotlin.reflect.KClass<*>): ModulesRuleBuilder =
        notContainClass(type.kontureQualifiedName())

    infix fun dependOnExternalLibrary(coordinate: String): ModulesRuleBuilder = dependOnExternalLibraries(coordinate)

    fun dependOnExternalLibraries(vararg coordinates: String): ModulesRuleBuilder {
        builder.setThat { module ->
            val resolvedDeps = builder.graph.requireExternalDependencies().modules[module.path] ?: emptyList()
            resolvedDeps.any { dep ->
                coordinates.any { pattern ->
                    if (pattern.contains(":")) {
                        PatternMatchers.matchesSimpleGlob(pattern, "${dep.group}:${dep.name}")
                    } else {
                        PatternMatchers.matchesSimpleGlob(pattern, dep.group) ||
                            PatternMatchers.matchesSimpleGlob(pattern, dep.name)
                    }
                }
            }
        }
        return builder
    }

    /**
     * Restricts the rules to modules with a Gradle path matching the specified module path pattern.
     */
    infix fun resideInAModule(modulePath: String): ModulesRuleBuilder = haveNameMatching(modulePath)

    /**
     * Restricts the rules to modules with a Gradle path matching any of the specified module path patterns.
     */
    infix fun resideInAModule(modulePaths: List<String>): ModulesRuleBuilder = haveNameMatching(modulePaths)

    /**
     * Restricts the rules to modules with a Gradle path matching any of the specified module path patterns.
     */
    fun resideInAModule(vararg modulePaths: String): ModulesRuleBuilder = haveNameMatching(modulePaths.toList())

    /**
     * Alias for [resideInAModule].
     */
    infix fun resideInModule(modulePath: String): ModulesRuleBuilder = resideInAModule(modulePath)

    /**
     * Alias for [resideInAModule].
     */
    infix fun resideInModules(modulePaths: List<String>): ModulesRuleBuilder = resideInAModule(modulePaths)

    /**
     * Alias for [resideInAModule].
     */
    fun resideInModules(vararg modulePaths: String): ModulesRuleBuilder = resideInAModule(modulePaths.toList())

    /**
     * Restricts the rules to modules containing files in a package matching the specified package pattern.
     */
    infix fun containPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.files.any { file -> PatternMatchers.matchesPackage(packagePattern, file.packageName) }
        }
        return builder
    }

    /**
     * Restricts the rules to modules containing files in a package matching any of the specified package patterns.
     */
    infix fun containPackage(packagePatterns: List<String>): ModulesRuleBuilder {
        builder.setThat { module ->
            module.files.any { file -> packagePatterns.any { PatternMatchers.matchesPackage(it, file.packageName) } }
        }
        return builder
    }

    /**
     * Restricts the rules to modules containing files in a package matching any of the specified package patterns.
     */
    fun containPackage(vararg packagePatterns: String): ModulesRuleBuilder = containPackage(packagePatterns.toList())

    /**
     * Alias for [containPackage].
     */
    infix fun resideInAPackage(packagePattern: String): ModulesRuleBuilder = containPackage(packagePattern)

    /**
     * Alias for [containPackage].
     */
    infix fun resideInAPackage(packagePatterns: List<String>): ModulesRuleBuilder = containPackage(packagePatterns)

    /**
     * Alias for [containPackage].
     */
    fun resideInAPackage(vararg packagePatterns: String): ModulesRuleBuilder = containPackage(packagePatterns.toList())
}

internal fun Module.classesFor(sourceSets: SourceSetSelector?): List<ClassDeclaration> {
    if (sourceSets == null) return classes
    return files.filter { file ->
        file.membershipsFor(path).any(sourceSets::matches)
    }.flatMap { it.classes }
}
