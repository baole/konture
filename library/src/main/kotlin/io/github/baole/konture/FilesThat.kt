/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class FilesThat internal constructor(
    private val builder: FilesRuleBuilder,
) {
    infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.declaration.packageName) }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.declaration.packageName) }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInAPackage(packagePatterns.toList())

    infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.packageName) }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = haveNameMatching(patterns.toList())

    infix fun resideInAModule(modulePath: String): FilesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "Module path '$modulePath' lacks a leading colon (':'). Suggest matching with ':$modulePath' instead.",
                )
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { it.modulePath == normalized }
        return builder
    }

    infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    KontureLogger.log(
                        LogLevel.WARNING,
                        "Module path '$path' lacks a leading colon (':'). Suggest matching with ':$path' instead.",
                    )
                    ":$path"
                } else {
                    path
                }
            }
        builder.setThat { context ->
            normalizedPaths.any { context.modulePath == it }
        }
        return builder
    }

    fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun satisfy(predicate: (FileDeclarationContext) -> Boolean): FilesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }
}
