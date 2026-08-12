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
public class ModulesThat internal constructor(
    private val builder: ModulesRuleBuilder,
) {
    /** Logical NOT operator for negating the next filter condition. */
    public fun not(): ModulesThat = builder.not()

    /** Filter or assertion criteria for have name path. */
    public infix fun haveNamePath(path: String): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized = normalizeModulePath(path)
        builder.setThat { it.path == normalized }
        return builder
    }

    /**
     * Restricts the rules to modules with an exact matching Gradle path in the specified list of paths.
     *
     * @param paths The list of Gradle paths of the module (e.g., ":core", ":app").
     */
    public infix fun haveNamePath(paths: List<String>): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized paths. */
        val normalizedPaths = paths.map { normalizeModulePath(it) }
        builder.setThat { normalizedPaths.contains(it.path) }
        return builder
    }

    /**
     * Restricts the rules to modules with an exact matching Gradle path in the specified paths.
     *
     * @param paths The vararg list of Gradle paths of the module (e.g., ":core", ":app").
     */
    public fun haveNamePath(vararg paths: String): ModulesRuleBuilder = haveNamePath(paths.asList())

    /**
     * Restricts the rules to modules with a Gradle path matching the given predicate.
     *
     * @param predicate The predicate to match the module path.
     */
    public infix fun haveNamePath(predicate: (String) -> Boolean): ModulesRuleBuilder {
        builder.setThat { predicate(it.path) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(path: String): ModulesRuleBuilder = haveNamePath(path)

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(paths: List<String>): ModulesRuleBuilder = haveNamePath(paths)

    /** Filter or assertion criteria for have name. */
    public fun haveName(vararg paths: String): ModulesRuleBuilder = haveNamePath(*paths)

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefix: String): ModulesRuleBuilder {
        builder.setThat { it.path.removePrefix(":").startsWith(prefix.removePrefix(":")) }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefixes: List<String>): ModulesRuleBuilder {
        builder.setThat { module ->
            prefixes.any { module.path.removePrefix(":").startsWith(it.removePrefix(":")) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(vararg prefixes: String): ModulesRuleBuilder =
        haveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffix: String): ModulesRuleBuilder {
        builder.setThat { it.path.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffixes: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> suffixes.any { module.path.endsWith(it) } }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(vararg suffixes: String): ModulesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /**
     * Restricts the rules to modules whose Gradle path matches the specified glob pattern.
     *
     * @param pattern Glob pattern (e.g., ":feature-*", ":core-**").
     */
    public infix fun haveNameMatching(pattern: String): ModulesRuleBuilder {
        builder.setThat { PatternMatchers.matchesModuleGlob(pattern, it.path) }
        return builder
    }

    /**
     * Restricts the rules to modules whose Gradle path matches any of the specified glob patterns.
     *
     * @param patterns Glob patterns (e.g., ":feature-*", ":core-**").
     */
    public infix fun haveNameMatching(patterns: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> patterns.any { PatternMatchers.matchesModuleGlob(it, module.path) } }
        return builder
    }

    /**
     * Restricts the rules to modules whose Gradle path matches any of the specified glob patterns.
     *
     * @param patterns Glob patterns (e.g., ":feature-*", ":core-**").
     */
    public fun haveNameMatching(vararg patterns: String): ModulesRuleBuilder = haveNameMatching(patterns.toList())

    /** Filter or assertion criteria for depend on module. */
    public infix fun dependOnModule(modulePath: String): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized = normalizeModulePath(modulePath)
        builder.setThat { module ->
            module.dependencies.any { normalizeModulePath(it.targetPath) == normalized }
        }
        return builder
    }

    /** Filter or assertion criteria for depend on modules. */
    public infix fun dependOnModules(modulePaths: List<String>): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized = modulePaths.map { normalizeModulePath(it) }
        builder.setThat { module ->
            module.dependencies.any { normalized.contains(normalizeModulePath(it.targetPath)) }
        }
        return builder
    }

    /** Filter or assertion criteria for depend on modules. */
    public fun dependOnModules(vararg modulePaths: String): ModulesRuleBuilder = dependOnModules(modulePaths.toList())

    /** Filter or assertion criteria for apply plugin. */
    public infix fun applyPlugin(pluginId: String): ModulesRuleBuilder {
        builder.setThat { module -> module.appliedPlugins.contains(pluginId) }
        return builder
    }

    /** Filter or assertion criteria for have plugin. */
    public infix fun havePlugin(pluginId: String): ModulesRuleBuilder = applyPlugin(pluginId)

    /** Filter or assertion criteria for have plugins. */
    public fun havePlugins(vararg pluginIds: String): ModulesRuleBuilder {
        builder.setThat { module -> pluginIds.all { module.appliedPlugins.contains(it) } }
        return builder
    }

    /**
     * Restricts the rules to modules matching the specified predicate.
     *
     * @param predicate The predicate to filter modules.
     */
    public infix fun matching(predicate: (Module) -> Boolean): ModulesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Satisfies an arbitrary custom predicate logic.
     */
    public fun satisfy(predicate: (Module) -> Boolean): ModulesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Matches if any of the nested condition blocks are satisfied.
     */
    public fun anyOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
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
    public fun allOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for have source set. */
    public infix fun haveSourceSet(sourceSetName: String): ModulesRuleBuilder {
        builder.setThat { module -> module.sourceSets.any { it.name == sourceSetName } }
        return builder
    }

    /** Filter or assertion criteria for have source set. */
    public infix fun haveSourceSet(sourceSetNames: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> sourceSetNames.all { name -> module.sourceSets.any { it.name == name } } }
        return builder
    }

    /** Filter or assertion criteria for have source set. */
    public fun haveSourceSet(vararg sourceSetNames: String): ModulesRuleBuilder = haveSourceSet(sourceSetNames.toList())

    /**
     * Matches if none of the nested condition blocks are satisfied.
     */
    public fun noneOf(vararg blocks: ModulesThat.() -> Unit): ModulesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ModulesRuleBuilder(builder.graph)
                ModulesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for apply plugin. */
    public infix fun applyPlugin(pluginIds: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> pluginIds.all { module.appliedPlugins.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have plugins. */
    public infix fun havePlugins(pluginIds: List<String>): ModulesRuleBuilder = applyPlugin(pluginIds)

    /** Filter or assertion criteria for not depend on module. */
    public infix fun notDependOnModule(modulePath: String): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized = normalizeModulePath(modulePath)
        builder.setThat { module ->
            module.dependencies.none { normalizeModulePath(it.targetPath) == normalized }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on modules. */
    public infix fun notDependOnModules(modulePaths: List<String>): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized = modulePaths.map { normalizeModulePath(it) }
        builder.setThat { module ->
            module.dependencies.none { normalized.contains(normalizeModulePath(it.targetPath)) }
        }
        return builder
    }

    /** Filter or assertion criteria for not depend on modules. */
    public fun notDependOnModules(vararg modulePaths: String): ModulesRuleBuilder =
        notDependOnModules(modulePaths.toList())

    /** Filter or assertion criteria for not apply plugin. */
    public infix fun notApplyPlugin(pluginId: String): ModulesRuleBuilder {
        builder.setThat { module -> !module.appliedPlugins.contains(pluginId) }
        return builder
    }

    /** Filter or assertion criteria for not have plugin. */
    public infix fun notHavePlugin(pluginId: String): ModulesRuleBuilder = notApplyPlugin(pluginId)

    /** Filter or assertion criteria for not have plugins. */
    public infix fun notHavePlugins(pluginIds: List<String>): ModulesRuleBuilder {
        builder.setThat { module -> pluginIds.none { module.appliedPlugins.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for not have plugins. */
    public fun notHavePlugins(vararg pluginIds: String): ModulesRuleBuilder = notHavePlugins(pluginIds.toList())

    /** Filter or assertion criteria for not have source set. */
    public infix fun notHaveSourceSet(sourceSetName: String): ModulesRuleBuilder {
        builder.setThat { module -> module.sourceSets.none { it.name == sourceSetName } }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(path: String): ModulesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized = normalizeModulePath(path)
        builder.setThat { it.path != normalized }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(pattern: String): ModulesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesModuleGlob(pattern, it.path) }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefix: String): ModulesRuleBuilder {
        builder.setThat { !it.path.removePrefix(":").startsWith(prefix.removePrefix(":")) }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffix: String): ModulesRuleBuilder {
        builder.setThat { !it.path.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for have build id. */
    public infix fun haveBuildId(buildId: String): ModulesRuleBuilder {
        builder.setThat { it.buildId == buildId }
        return builder
    }

    /** Filter or assertion criteria for not have build id. */
    public infix fun notHaveBuildId(buildId: String): ModulesRuleBuilder {
        builder.setThat { it.buildId != buildId }
        return builder
    }

    /** Filter or assertion criteria for have project dir. */
    public infix fun haveProjectDir(dirPattern: String): ModulesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(dirPattern, it.projectDir) }
        return builder
    }

    /** Filter or assertion criteria for not have project dir. */
    public infix fun notHaveProjectDir(dirPattern: String): ModulesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(dirPattern, it.projectDir) }
        return builder
    }

    /** Filter or assertion criteria for contain classes in package. */
    public infix fun containClassesInPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain classes in package. */
    public infix fun notContainClassesInPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classes.none { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for contain classes with annotation. */
    public infix fun containClassesWithAnnotation(annotationFqName: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classes.any { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain classes with annotation. */
    public infix fun notContainClassesWithAnnotation(annotationFqName: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classesFor(builder.sourceSets).none { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(fqName: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classesFor(builder.sourceSets).any { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain class. */
    public infix fun notContainClass(fqName: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.classesFor(builder.sourceSets).none { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    /** Filter or assertion criteria for contain classes with annotation. */
    public infix fun containClassesWithAnnotation(
        annotation: kotlin.reflect.KClass<out Annotation>,
    ): ModulesRuleBuilder = containClassesWithAnnotation(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for not contain classes with annotation. */
    public infix fun notContainClassesWithAnnotation(
        annotation: kotlin.reflect.KClass<out Annotation>,
    ): ModulesRuleBuilder = notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(type: kotlin.reflect.KClass<*>): ModulesRuleBuilder =
        containClass(type.kontureQualifiedName())

    /** Filter or assertion criteria for not contain class. */
    public infix fun notContainClass(type: kotlin.reflect.KClass<*>): ModulesRuleBuilder =
        notContainClass(type.kontureQualifiedName())

    /** Filter or assertion criteria for depend on external library. */
    public infix fun dependOnExternalLibrary(coordinate: String): ModulesRuleBuilder =
        dependOnExternalLibraries(coordinate)

    /** Filter or assertion criteria for depend on external libraries. */
    public fun dependOnExternalLibraries(vararg coordinates: String): ModulesRuleBuilder {
        builder.setThat { module ->
            /** Filter or assertion criteria for resolved deps. */
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
    public infix fun resideInAModule(modulePath: String): ModulesRuleBuilder = haveNameMatching(modulePath)

    /**
     * Restricts the rules to modules with a Gradle path matching any of the specified module path patterns.
     */
    public infix fun resideInAModule(modulePaths: List<String>): ModulesRuleBuilder = haveNameMatching(modulePaths)

    /**
     * Restricts the rules to modules with a Gradle path matching any of the specified module path patterns.
     */
    public fun resideInAModule(vararg modulePaths: String): ModulesRuleBuilder = haveNameMatching(modulePaths.toList())

    /**
     * Alias for [resideInAModule].
     */
    public infix fun resideInModule(modulePath: String): ModulesRuleBuilder = resideInAModule(modulePath)

    /**
     * Alias for [resideInAModule].
     */
    public infix fun resideInModules(modulePaths: List<String>): ModulesRuleBuilder = resideInAModule(modulePaths)

    /**
     * Alias for [resideInAModule].
     */
    public fun resideInModules(vararg modulePaths: String): ModulesRuleBuilder = resideInAModule(modulePaths.toList())

    /**
     * Restricts the rules to modules containing files in a package matching the specified package pattern.
     */
    public infix fun resideInAPackage(packagePattern: String): ModulesRuleBuilder {
        builder.setThat { module ->
            module.files.any { file -> PatternMatchers.matchesPackage(packagePattern, file.packageName) }
        }
        return builder
    }

    /**
     * Restricts the rules to modules containing files in a package matching any of the specified package patterns.
     */
    public infix fun resideInAPackage(packagePatterns: List<String>): ModulesRuleBuilder {
        builder.setThat { module ->
            module.files.any { file -> packagePatterns.any { PatternMatchers.matchesPackage(it, file.packageName) } }
        }
        return builder
    }

    /**
     * Restricts the rules to modules containing files in a package matching any of the specified package patterns.
     */
    public fun resideInAPackage(vararg packagePatterns: String): ModulesRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    /**
     * Deprecated alias for [resideInAPackage].
     */
    @Deprecated(
        message = "Renamed for consistency with resideInAPackage across all scopes.",
        replaceWith = ReplaceWith("resideInAPackage(packagePattern)"),
        level = DeprecationLevel.WARNING,
    )
    public infix fun containPackage(packagePattern: String): ModulesRuleBuilder = resideInAPackage(packagePattern)

    /**
     * Deprecated alias for [resideInAPackage].
     */
    @Deprecated(
        message = "Renamed for consistency with resideInAPackage across all scopes.",
        replaceWith = ReplaceWith("resideInAPackage(packagePatterns)"),
        level = DeprecationLevel.WARNING,
    )
    public infix fun containPackage(packagePatterns: List<String>): ModulesRuleBuilder =
        resideInAPackage(packagePatterns)

    /**
     * Deprecated alias for [resideInAPackage].
     */
    @Deprecated(
        message = "Renamed for consistency with resideInAPackage across all scopes.",
        replaceWith = ReplaceWith("resideInAPackage(*packagePatterns)"),
        level = DeprecationLevel.WARNING,
    )
    public fun containPackage(vararg packagePatterns: String): ModulesRuleBuilder = resideInAPackage(*packagePatterns)
}

internal fun Module.classesFor(sourceSets: SourceSetSelector?): List<ClassDeclaration> {
    if (sourceSets == null) return classes
    return files.filter { file ->
        file.membershipsFor(path).any(sourceSets::matches)
    }.flatMap { it.classes }
}
