/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/** Package dependency assertions for function rules. */
public interface FunctionsShouldDependencyAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: FunctionsRuleBuilder

    /** Asserts that selected functions only depend on types or usages in packages matching [patterns]. */
    public infix fun onlyDependOnPackages(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { function, _, violations ->
            val deps = function.collectDependencyPackages()
            val offending =
                deps.filterNot { pkg ->
                    pkg == function.packageName || patterns.any { PatternMatchers.matchesPackage(it, pkg) }
                }.sorted()

            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "function.should.onlyDependOnPackages",
                        function.qualifiedName,
                        offending.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    /** Asserts that selected functions only depend on types or usages in packages matching [patterns]. */
    public fun onlyDependOnPackages(vararg patterns: String): FunctionsRuleBuilder =
        onlyDependOnPackages(patterns.toList())

    /** Asserts that selected functions do not depend on types or usages in packages matching [patterns]. */
    public infix fun notDependOnPackages(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { function, _, violations ->
            val deps = function.collectDependencyPackages()
            val offending =
                deps.filter { pkg ->
                    patterns.any { PatternMatchers.matchesPackage(it, pkg) }
                }.sorted()

            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.notDependOnPackages", function.qualifiedName, offending.joinToString()),
                )
            }
        }
        return builder
    }

    /** Asserts that selected functions do not depend on types or usages in packages matching [patterns]. */
    public fun notDependOnPackages(vararg patterns: String): FunctionsRuleBuilder =
        notDependOnPackages(patterns.toList())

    /** Asserts that selected functions depend on types or usages in packages matching [patterns]. */
    public infix fun dependOnPackages(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { function, _, violations ->
            val deps = function.collectDependencyPackages()
            val matches =
                deps.any { pkg ->
                    patterns.any { PatternMatchers.matchesPackage(it, pkg) }
                }

            if (!matches) {
                violations.add(
                    getMessage("function.should.dependOnPackages", function.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /** Asserts that selected functions depend on types or usages in packages matching [patterns]. */
    public fun dependOnPackages(vararg patterns: String): FunctionsRuleBuilder = dependOnPackages(patterns.toList())

    /** Asserts that selected functions depend on package containing [classes]. */
    public fun dependOnPackageOf(vararg classes: KClass<*>): FunctionsRuleBuilder =
        dependOnPackages(classes.map { it.konturePackageName() })

    /** Asserts that selected functions only depend on package containing [classes]. */
    public fun onlyDependOnPackageOf(vararg classes: KClass<*>): FunctionsRuleBuilder =
        onlyDependOnPackages(classes.map { it.konturePackageName() })

    /** Asserts that selected functions do not depend on package containing [classes]. */
    public fun notDependOnPackageOf(vararg classes: KClass<*>): FunctionsRuleBuilder =
        notDependOnPackages(classes.map { it.konturePackageName() })
}
