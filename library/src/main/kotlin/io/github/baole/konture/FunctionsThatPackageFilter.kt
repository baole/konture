/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/**
 * Trait interface for package and module residency filtering on functions.
 */
@Suppress("ComplexInterface")
public interface FunctionsThatPackageFilter : FunctionsThatScope {
    /** Filters functions residing in a package matching [packagePattern]. */
    public infix fun resideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Filters functions residing in any package matching [packagePatterns]. */
    public infix fun resideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    /** Filters functions residing in any package matching [packagePatterns]. */
    public fun resideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    /** Filters functions residing in a package satisfying [predicate]. */
    public infix fun resideInAPackage(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    /** Filters functions residing in the package of [type]. */
    public infix fun resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    /** Filters functions not residing in a package matching [packagePattern]. */
    public infix fun notResideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Filters functions not residing in any package matching [packagePatterns]. */
    public infix fun notResideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    /** Filters functions not residing in any package matching [packagePatterns]. */
    public fun notResideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        notResideInAPackage(packagePatterns.toList())

    /** Filters functions residing in a module matching [modulePath]. */
    public infix fun resideInAModule(modulePath: String): FunctionsRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { it.modulePath == normalized }
        return builder
    }

    /** Filters functions residing in any module matching [modulePaths]. */
    public infix fun resideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setThat { context -> normalizedPaths.contains(context.modulePath) }
        return builder
    }

    /** Filters functions residing in any module matching [modulePaths]. */
    public fun resideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filters functions residing in a module matching [modulePath]. */
    public infix fun resideInModule(modulePath: String): FunctionsRuleBuilder = resideInAModule(modulePath)

    /** Filters functions residing in any module matching [modulePaths]. */
    public infix fun resideInModules(modulePaths: List<String>): FunctionsRuleBuilder = resideInAModule(modulePaths)

    /** Filters functions residing in any module matching [modulePaths]. */
    public fun resideInModules(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filters functions not residing in a module matching [modulePath]. */
    public infix fun notResideInAModule(modulePath: String): FunctionsRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { context ->
            val match =
                context.modulePath == normalized || PatternMatchers.matchesModuleGlob(normalized, context.modulePath)
            !match
        }
        return builder
    }

    /** Filters functions not residing in any module matching [modulePaths]. */
    public infix fun notResideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setThat { context ->
            val match =
                normalized.any { target ->
                    context.modulePath == target || PatternMatchers.matchesModuleGlob(target, context.modulePath)
                }
            !match
        }
        return builder
    }

    /** Filters functions not residing in any module matching [modulePaths]. */
    public fun notResideInAModule(vararg modulePaths: String): FunctionsRuleBuilder =
        notResideInAModule(modulePaths.toList())

    /** Filters functions not residing in a module matching [modulePath]. */
    public infix fun notResideInModule(modulePath: String): FunctionsRuleBuilder = notResideInAModule(modulePath)

    /** Filters functions not residing in any module matching [modulePaths]. */
    public infix fun notResideInModules(modulePaths: List<String>): FunctionsRuleBuilder =
        notResideInAModule(modulePaths)

    /** Filters functions not residing in any module matching [modulePaths]. */
    public fun notResideInModules(vararg modulePaths: String): FunctionsRuleBuilder =
        notResideInAModule(modulePaths.toList())
}
