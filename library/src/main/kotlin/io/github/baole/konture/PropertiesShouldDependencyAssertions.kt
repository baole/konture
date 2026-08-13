/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/** Package dependency assertions for property rules. */
public interface PropertiesShouldDependencyAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: PropertiesRuleBuilder

    /** Asserts that selected properties only depend on types or usages in packages matching [patterns]. */
    public infix fun onlyDependOnPackages(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { property, _, violations ->
            val deps = property.collectDependencyPackages()
            val offending =
                deps.filterNot { pkg ->
                    pkg == property.packageName || patterns.any { PatternMatchers.matchesPackage(it, pkg) }
                }.sorted()

            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "property.should.onlyDependOnPackages",
                        property.qualifiedName,
                        offending.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    /** Asserts that selected properties only depend on types or usages in packages matching [patterns]. */
    public fun onlyDependOnPackages(vararg patterns: String): PropertiesRuleBuilder =
        onlyDependOnPackages(patterns.toList())

    /** Asserts that selected properties do not depend on types or usages in packages matching [patterns]. */
    public infix fun notDependOnPackages(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { property, _, violations ->
            val deps = property.collectDependencyPackages()
            val offending =
                deps.filter { pkg ->
                    patterns.any { PatternMatchers.matchesPackage(it, pkg) }
                }.sorted()

            if (offending.isNotEmpty()) {
                violations.add(
                    getMessage("property.should.notDependOnPackages", property.qualifiedName, offending.joinToString()),
                )
            }
        }
        return builder
    }

    /** Asserts that selected properties do not depend on types or usages in packages matching [patterns]. */
    public fun notDependOnPackages(vararg patterns: String): PropertiesRuleBuilder =
        notDependOnPackages(patterns.toList())

    /** Asserts that selected properties depend on types or usages in packages matching [patterns]. */
    public infix fun dependOnPackages(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { property, _, violations ->
            val deps = property.collectDependencyPackages()
            val matches =
                deps.any { pkg ->
                    patterns.any { PatternMatchers.matchesPackage(it, pkg) }
                }

            if (!matches) {
                violations.add(
                    getMessage("property.should.dependOnPackages", property.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /** Asserts that selected properties depend on types or usages in packages matching [patterns]. */
    public fun dependOnPackages(vararg patterns: String): PropertiesRuleBuilder = dependOnPackages(patterns.toList())

    /** Asserts that selected properties depend on package containing [classes]. */
    public fun dependOnPackageOf(vararg classes: KClass<*>): PropertiesRuleBuilder =
        dependOnPackages(classes.map { it.konturePackageName() })

    /** Asserts that selected properties only depend on package containing [classes]. */
    public fun onlyDependOnPackageOf(vararg classes: KClass<*>): PropertiesRuleBuilder =
        onlyDependOnPackages(classes.map { it.konturePackageName() })

    /** Asserts that selected properties do not depend on package containing [classes]. */
    public fun notDependOnPackageOf(vararg classes: KClass<*>): PropertiesRuleBuilder =
        notDependOnPackages(classes.map { it.konturePackageName() })
}
