/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class FilesShould internal constructor(
    private val builder: FilesRuleBuilder,
) {
    /** Fails for every invocation of [fqName] in the selected source file. */
    fun notCall(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            file.declaration.usages
                .filter { usage ->
                    usage.kind == UsageKind.CALL &&
                        (usage.targetFqName == fqName || fqName in usage.possibleTargetFqNames)
                }.forEach { usage ->
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(getMessage("usage.notCall", unresolved, fqName, usage.rawExpression, usage.line, usage.column))
                }
        }
        return builder
    }

    /** Fails for every invocation of [kClass] in the selected source file. */
    fun notCall(kClass: kotlin.reflect.KClass<*>): FilesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every invocation of [T] in the selected source file. */
    inline fun <reified T : Any> notCall(): FilesRuleBuilder = notCall(T::class)

    /** Fails for every actual class/type use of [fqName]; imports alone do not match. */
    fun notReferenceClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            file.declaration.usages
                .filter { it.kind == UsageKind.CLASS_REFERENCE && it.targetFqName == fqName }
                .forEach { usage ->
                    violations.add(getMessage("usage.notReferenceClass", fqName, usage.rawExpression, usage.line, usage.column))
                }
        }
        return builder
    }

    /** Fails for every actual class/type use of [kClass]; imports alone do not match. */
    fun notReferenceClass(kClass: kotlin.reflect.KClass<*>): FilesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())

    /** Fails for every actual class/type use of [T]; imports alone do not match. */
    inline fun <reified T : Any> notReferenceClass(): FilesRuleBuilder = notReferenceClass(T::class)

    infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, file.declaration.packageName)) {
                violations.add(
                    getMessage("file.should.resideInPackage", file.declaration.name, packagePattern, file.declaration.packageName),
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
                    getMessage("file.should.resideInPackageAny", file.declaration.name, packagePatterns.joinToString(), file.declaration.packageName),
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
                    getMessage("file.should.resideInPackageMatching", file.declaration.name, "predicate", file.declaration.packageName),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.endsWith(suffix)) {
                violations.add(
                    getMessage("file.should.haveNameEndingWith", file.declaration.name, suffix),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = suffixes.any { file.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameEndingWithAny", file.declaration.name, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!file.declaration.name.startsWith(prefix)) {
                violations.add(
                    getMessage("file.should.haveNameStartingWith", file.declaration.name, prefix),
                )
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = prefixes.any { file.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameStartingWithAny", file.declaration.name, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, file.declaration.name)) {
                violations.add(
                    getMessage("file.should.haveNameMatching", file.declaration.name, pattern),
                )
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, file.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("file.should.haveNameMatchingAny", file.declaration.name, patterns.joinToString()),
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
                    getMessage("file.should.notContainWildcardImports", file.declaration.name, wildcards.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveOnlyOneClassPerFile(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.size > 1) {
                violations.add(
                    getMessage(
                        "file.should.containAtMostOneClass",
                        file.declaration.name,
                        file.declaration.classes.size,
                        file.declaration.classes.joinToString {
                            it.name
                        },
                    ),
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
                    getMessage("file.should.matchClassName", "${file.declaration.name} (at ${file.declaration.filePath})"),
                )
            }
        }
        return builder
    }

    fun beDocumentedWithKDoc(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.kdocText.isNullOrBlank()) {
                violations.add(
                    getMessage("file.should.beDocumented", file.declaration.name),
                )
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
                violations.add(
                    getMessage("file.should.satisfyCustom", file.declaration.name, description),
                )
            }
        }
        return builder
    }

    fun satisfy(assertion: (FileDeclarationContext, MutableList<String>) -> Unit): FilesRuleBuilder {
        builder.setShould { file, _, violations -> assertion(file, violations) }
        return builder
    }
}
