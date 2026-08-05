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
}
