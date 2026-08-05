/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

interface FunctionsShouldNameAssertions {
    val builder: FunctionsRuleBuilder

    infix fun resideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, func.packageName)) {
                violations.add(
                    getMessage("function.should.resideInPackage", func.qualifiedName, packagePattern, func.packageName),
                )
            }
        }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
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

    fun resideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    infix fun resideInAPackage(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!predicate(func.packageName)) {
                violations.add(
                    getMessage("function.should.resideInPackageMatching", func.qualifiedName, func.packageName),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.name.endsWith(suffix)) {
                violations.add(
                    getMessage("function.should.haveNameEndingWith", func.qualifiedName, suffix),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = suffixes.any { func.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameEndingWithAny", func.qualifiedName, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.name.startsWith(prefix)) {
                violations.add(
                    getMessage("function.should.haveNameStartingWith", func.qualifiedName, prefix),
                )
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = prefixes.any { func.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameStartingWithAny", func.qualifiedName, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, func.declaration.name)) {
                violations.add(
                    getMessage("function.should.haveNameMatching", func.qualifiedName, pattern),
                )
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, func.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameMatchingAny", func.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): FunctionsRuleBuilder = haveNameMatching(patterns.toList())

    infix fun resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
        resideInAPackage(
            type.toKonturePackageReference().packageName,
        )

    infix fun resideInAModule(modulePath: String): FunctionsRuleBuilder {
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

    infix fun resideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { func, _, violations ->
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

    fun resideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun resideInModule(modulePath: String): FunctionsRuleBuilder = resideInAModule(modulePath)

    infix fun resideInModules(modulePaths: List<String>): FunctionsRuleBuilder = resideInAModule(modulePaths)

    fun resideInModules(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): FunctionsRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { func, _, violations ->
            val matches =
                func.modulePath == normalized || PatternMatchers.matchesModuleGlob(normalized, func.modulePath)
            if (matches) {
                violations.add(getMessage("function.should.notResideInModule", func.qualifiedName, normalized))
            }
        }
        return builder
    }

    infix fun notResideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setShould { func, _, violations ->
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

    fun notResideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun notResideInModule(modulePath: String): FunctionsRuleBuilder = notResideInAModule(modulePath)

    infix fun notResideInModules(modulePaths: List<String>): FunctionsRuleBuilder = notResideInAModule(modulePaths)

    fun notResideInModules(vararg modulePaths: String): FunctionsRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun haveName(name: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.name != name) {
                violations.add(getMessage("function.should.haveName", func.qualifiedName, name))
            }
        }
        return builder
    }

    infix fun haveName(names: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!names.contains(func.declaration.name)) {
                violations.add(getMessage("function.should.haveNameIn", func.qualifiedName, names.joinToString()))
            }
        }
        return builder
    }

    fun haveName(vararg names: String): FunctionsRuleBuilder = haveName(names.toList())

    infix fun haveName(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!predicate(func.declaration.name)) {
                violations.add(getMessage("function.should.haveNameMatchingPredicate", func.qualifiedName))
            }
        }
        return builder
    }

    infix fun notHaveName(name: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.name == name) {
                violations.add(getMessage("function.should.notHaveName", func.qualifiedName, name))
            }
        }
        return builder
    }

    infix fun notHaveName(names: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (names.contains(func.declaration.name)) {
                violations.add(getMessage("function.should.notHaveNameIn", func.qualifiedName, names.joinToString()))
            }
        }
        return builder
    }

    fun notHaveName(vararg names: String): FunctionsRuleBuilder = notHaveName(names.toList())

    infix fun notHaveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (PatternMatchers.matchesSimpleGlob(pattern, func.declaration.name)) {
                violations.add(getMessage("function.should.notHaveNameMatching", func.qualifiedName, pattern))
            }
        }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matching = patterns.filter { PatternMatchers.matchesSimpleGlob(it, func.declaration.name) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.notHaveNameMatching", func.qualifiedName, matching.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): FunctionsRuleBuilder = notHaveNameMatching(patterns.toList())

    infix fun notHaveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.name.startsWith(prefix)) {
                violations.add(getMessage("function.should.notHaveNameStartingWith", func.qualifiedName, prefix))
            }
        }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matching = prefixes.filter { func.declaration.name.startsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.notHaveNameStartingWith", func.qualifiedName, matching.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

    infix fun notHaveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.name.endsWith(suffix)) {
                violations.add(getMessage("function.should.notHaveNameEndingWith", func.qualifiedName, suffix))
            }
        }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matching = suffixes.filter { func.declaration.name.endsWith(it) }
            if (matching.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.notHaveNameEndingWith", func.qualifiedName, matching.joinToString()),
                )
            }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = notHaveNameEndingWith(suffixes.toList())
}
