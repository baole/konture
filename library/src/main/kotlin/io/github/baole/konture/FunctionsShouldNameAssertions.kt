/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/** Naming and package assertions for function rules. */
public interface FunctionsShouldNameAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: FunctionsRuleBuilder

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, func.packageName)) {
                violations.add(
                    getMessage("function.should.resideInPackage", func.qualifiedName, packagePattern, func.packageName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, func.packageName) }
            if (!matches) {
                violations.add(
                    getMessage(
                        "function.should.resideInPackageAny",
                        func.qualifiedName,
                        packagePatterns.joinToString(),
                        func.packageName,
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    public fun resideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!predicate(func.packageName)) {
                violations.add(
                    getMessage("function.should.resideInPackageMatching", func.qualifiedName, func.packageName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.name.endsWith(suffix)) {
                violations.add(
                    getMessage("function.should.haveNameEndingWith", func.qualifiedName, suffix),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = suffixes.any { func.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameEndingWithAny", func.qualifiedName, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.name.startsWith(prefix)) {
                violations.add(
                    getMessage("function.should.haveNameStartingWith", func.qualifiedName, prefix),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = prefixes.any { func.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameStartingWithAny", func.qualifiedName, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder =
        haveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, func.declaration.name)) {
                violations.add(
                    getMessage("function.should.haveNameMatching", func.qualifiedName, pattern),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, func.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameMatchingAny", func.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(vararg patterns: String): FunctionsRuleBuilder = haveNameMatching(patterns.toList())

    /** Filter or assertion criteria for reside in package of. */
    public infix fun resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
        resideInAPackage(
            type.toKonturePackageReference().packageName,
        )

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePath: String): FunctionsRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { func, _, violations ->
            if (func.modulePath != normalized && !PatternMatchers.matchesModuleGlob(normalized, func.modulePath)) {
                violations.add(
                    getMessage("function.should.resideInModule", func.qualifiedName, normalized, func.modulePath),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
        /** Filter or assertion criteria for normalized paths. */
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches =
                normalizedPaths.any {
                        target ->
                    func.modulePath == target || PatternMatchers.matchesModuleGlob(target, func.modulePath)
                }
            if (!matches) {
                violations.add(
                    getMessage(
                        "function.should.resideInModule",
                        func.qualifiedName,
                        normalizedPaths.joinToString(),
                        func.modulePath,
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a module. */
    public fun resideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for reside in module. */
    public infix fun resideInModule(modulePath: String): FunctionsRuleBuilder = resideInAModule(modulePath)

    /** Filter or assertion criteria for reside in modules. */
    public infix fun resideInModules(modulePaths: List<String>): FunctionsRuleBuilder = resideInAModule(modulePaths)

    /** Filter or assertion criteria for reside in modules. */
    public fun resideInModules(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePath: String): FunctionsRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matches. */
            val matches =
                func.modulePath == normalized || PatternMatchers.matchesModuleGlob(normalized, func.modulePath)
            if (matches) {
                violations.add(getMessage("function.should.notResideInModule", func.qualifiedName, normalized))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matching. */
            val matching =
                normalized.filter { target ->
                    func.modulePath == target || PatternMatchers.matchesModuleGlob(target, func.modulePath)
                }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.notResideInModuleAny", func.qualifiedName, matching.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in a module. */
    public fun notResideInAModule(vararg modulePaths: String): FunctionsRuleBuilder =
        notResideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in module. */
    public infix fun notResideInModule(modulePath: String): FunctionsRuleBuilder = notResideInAModule(modulePath)

    /** Filter or assertion criteria for not reside in modules. */
    public infix fun notResideInModules(modulePaths: List<String>): FunctionsRuleBuilder =
        notResideInAModule(modulePaths)

    /** Filter or assertion criteria for not reside in modules. */
    public fun notResideInModules(vararg modulePaths: String): FunctionsRuleBuilder =
        notResideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(name: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.name != name) {
                violations.add(getMessage("function.should.haveName", func.qualifiedName, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(names: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!names.contains(func.declaration.name)) {
                violations.add(getMessage("function.should.haveNameIn", func.qualifiedName, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public fun haveName(vararg names: String): FunctionsRuleBuilder = haveName(names.toList())

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!predicate(func.declaration.name)) {
                violations.add(getMessage("function.should.haveNameMatchingPredicate", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(name: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.name == name) {
                violations.add(getMessage("function.should.notHaveName", func.qualifiedName, name))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(names: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (names.contains(func.declaration.name)) {
                violations.add(getMessage("function.should.notHaveNameIn", func.qualifiedName, names.joinToString()))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public fun notHaveName(vararg names: String): FunctionsRuleBuilder = notHaveName(names.toList())

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, func.declaration.name)) {
                violations.add(getMessage("function.should.notHaveNameMatching", func.qualifiedName, pattern))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matching. */
            val matching = patterns.filter { PatternMatchers.matchesSimpleGlob(it, func.declaration.name) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.notHaveNameMatching", func.qualifiedName, matching.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public fun notHaveNameMatching(vararg patterns: String): FunctionsRuleBuilder =
        notHaveNameMatching(patterns.toList())

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("function.should.notHaveNameStartingWith", func.qualifiedName, prefix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matching. */
            val matching = prefixes.filter { func.declaration.name.startsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.notHaveNameStartingWith", func.qualifiedName, matching.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public fun notHaveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("function.should.notHaveNameEndingWith", func.qualifiedName, suffix))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matching. */
            val matching = suffixes.filter { func.declaration.name.endsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.notHaveNameEndingWith", func.qualifiedName, matching.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public fun notHaveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder =
        notHaveNameEndingWith(suffixes.toList())
}
