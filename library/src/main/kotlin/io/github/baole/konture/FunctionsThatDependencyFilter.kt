/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/**
 * Trait interface for filtering functions by package dependencies.
 */
public interface FunctionsThatDependencyFilter : FunctionsThatScope {
    /** Filters functions that depend on packages matching any pattern in [patterns]. */
    public infix fun dependOnPackages(patterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { function ->
            val deps = function.collectDependencyPackages()
            deps.any { pkg -> patterns.any { PatternMatchers.matchesPackage(it, pkg) } }
        }
        return builder
    }

    /** Filters functions that depend on packages matching any pattern in [patterns]. */
    public fun dependOnPackages(vararg patterns: String): FunctionsRuleBuilder = dependOnPackages(patterns.toList())

    /** Filters functions that do not depend on packages matching any pattern in [patterns]. */
    public infix fun notDependOnPackages(patterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { function ->
            val deps = function.collectDependencyPackages()
            deps.none { pkg -> patterns.any { PatternMatchers.matchesPackage(it, pkg) } }
        }
        return builder
    }

    /** Filters functions that do not depend on packages matching any pattern in [patterns]. */
    public fun notDependOnPackages(vararg patterns: String): FunctionsRuleBuilder =
        notDependOnPackages(patterns.toList())

    /** Filters functions that depend on the package containing any of [classes]. */
    public fun dependOnPackageOf(vararg classes: KClass<*>): FunctionsRuleBuilder =
        dependOnPackages(classes.map { it.konturePackageName() })
}
