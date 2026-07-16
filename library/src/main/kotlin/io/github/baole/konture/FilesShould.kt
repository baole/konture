/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class FilesShould internal constructor(
    private val builder: FilesRuleBuilder,
) {
    infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, file.declaration.packageName)) {
                violations.add(
                    "File ${file.declaration.name} should reside in package '$packagePattern' but resides in '${file.declaration.packageName}'",
                )
            }
        }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, file.declaration.packageName) }
            if (!matches) {
                violations.add(
                    "File ${file.declaration.name} should reside in package in [${packagePatterns.joinToString()}] but resides in '${file.declaration.packageName}'",
                )
            }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInAPackage(packagePatterns.toList())

    infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!predicate(file.declaration.packageName)) {
                violations.add(
                    "File ${file.declaration.name} should reside in package matching predicate, but resides in '${file.declaration.packageName}'",
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.endsWith(suffix)) {
                violations.add("File ${file.declaration.name} should have name ending with '$suffix'")
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = suffixes.any { file.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    "File ${file.declaration.name} should have name ending with any of [${suffixes.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.startsWith(prefix)) {
                violations.add("File ${file.declaration.name} should have name starting with '$prefix'")
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = prefixes.any { file.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    "File ${file.declaration.name} should have name starting with any of [${prefixes.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add("File ${file.declaration.name} should have name matching '$pattern'")
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, file.declaration.name) }
            if (!matches) {
                violations.add(
                    "File ${file.declaration.name} should have name matching any of [${patterns.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = haveNameMatching(patterns.toList())

    fun notHaveWildcardImports(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val wildcards = file.declaration.imports.filter { it.endsWith(".*") }
            if (wildcards.isNotEmpty()) {
                violations.add(
                    "File ${file.declaration.name} should not contain wildcard imports but contains: ${wildcards.joinToString()}",
                )
            }
        }
        return builder
    }

    fun haveOnlyOneClassPerFile(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.size > 1) {
                violations.add(
                    "File ${file.declaration.name} should contain at most one class, but contains ${file.declaration.classes.size}: ${file.declaration.classes.joinToString {
                        it.name
                    }}",
                )
            }
        }
        return builder
    }

    fun haveNameMatchingClassName(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val expectedName = file.declaration.name.substringBeforeLast(".kt")
            val matched = file.declaration.classes.isEmpty() || file.declaration.classes.any { it.name == expectedName }
            if (!matched) {
                violations.add(
                    "File ${file.declaration.name} (at ${file.declaration.filePath}) does not match any of its class names",
                )
            }
        }
        return builder
    }

    fun beDocumentedWithKDoc(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.kdocText.isNullOrBlank()) {
                violations.add("File ${file.declaration.name} should be documented with KDoc")
            }
        }
        return builder
    }

    infix fun satisfy(assertion: (FileDeclarationContext) -> Boolean): FilesRuleBuilder = satisfy("custom condition") { f, _ -> assertion(f) }

    private fun satisfy(
        description: String,
        assertion: (FileDeclarationContext, List<FileDeclarationContext>) -> Boolean,
    ): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            if (!assertion(file, allFiles)) {
                violations.add("File ${file.declaration.name} should satisfy: $description")
            }
        }
        return builder
    }

    fun satisfy(assertion: (FileDeclarationContext, MutableList<String>) -> Unit): FilesRuleBuilder {
        builder.setShould { file, _, violations -> assertion(file, violations) }
        return builder
    }
}
