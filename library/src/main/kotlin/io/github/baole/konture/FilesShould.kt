/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ViolationLocation
import kotlin.reflect.KClass

@KontureDsl
class FilesShould internal constructor(
    private val builder: FilesRuleBuilder,
) {
    /** Fails for every invocation of [fqName] in the selected source file. */
    fun notCall(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            file.declaration.usages
                .filter { usage -> PatternMatchers.isCallUsageMatch(usage, fqName) }
                .forEach { usage ->
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(
                        "${getMessage(
                            "usage.notCall",
                            unresolved,
                            fqName,
                            usage.rawExpression,
                            usage.line,
                            usage.column,
                        )} (at ${ViolationLocation.format(
                            usage.filePath,
                            usage.line,
                            usage.column,
                            file.modulePath,
                            file.sourceSet?.name,
                        )})",
                    )
                }
        }
        return builder
    }

    /** Fails for every invocation of [kClass] in the selected source file. */
    fun notCall(kClass: KClass<*>): FilesRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every invocation of [T] in the selected source file. */
    inline fun <reified T : Any> notCall(): FilesRuleBuilder = notCall(T::class)

    /** Fails for every actual class/type use of [fqName]; imports alone do not match. */
    fun notReferenceClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            file.declaration.usages
                .filter { it.kind == UsageKind.CLASS_REFERENCE && it.targetFqName == fqName }
                .forEach { usage ->
                    violations.add(
                        "${getMessage(
                            "usage.notReferenceClass",
                            fqName,
                            usage.rawExpression,
                            usage.line,
                            usage.column,
                        )} (at ${ViolationLocation.format(
                            usage.filePath,
                            usage.line,
                            usage.column,
                            file.modulePath,
                            file.sourceSet?.name,
                        )})",
                    )
                }
        }
        return builder
    }

    /** Fails for every actual class/type use of [kClass]; imports alone do not match. */
    fun notReferenceClass(kClass: KClass<*>): FilesRuleBuilder = notReferenceClass(kClass.kontureQualifiedName())

    /** Fails for every actual class/type use of [T]; imports alone do not match. */
    inline fun <reified T : Any> notReferenceClass(): FilesRuleBuilder = notReferenceClass(T::class)

    infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, file.declaration.packageName)) {
                violations.add(
                    getMessage(
                        "file.should.resideInPackage",
                        file.declaration.name,
                        packagePattern,
                        file.declaration.packageName,
                    ),
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
                    getMessage(
                        "file.should.resideInPackageAny",
                        file.declaration.name,
                        packagePatterns.joinToString(),
                        file.declaration.packageName,
                    ),
                )
            }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInAPackage(packagePatterns.toList())

    infix fun notResideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (PatternMatchers.matchesPackage(packagePattern, file.declaration.packageName)) {
                violations.add(
                    getMessage(
                        "file.should.notResideInPackage",
                        file.declaration.name,
                        packagePattern,
                    ),
                )
            }
        }
        return builder
    }

    infix fun notResideInAPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, file.declaration.packageName) }
            if (matches) {
                violations.add(
                    getMessage(
                        "file.should.notResideInPackageAny",
                        file.declaration.name,
                        packagePatterns.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    fun notResideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = notResideInAPackage(packagePatterns.toList())

    infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (!predicate(file.declaration.packageName)) {
                violations.add(
                    getMessage(
                        "file.should.resideInPackageMatching",
                        file.declaration.name,
                        "predicate",
                        file.declaration.packageName,
                    ),
                )
            }
        }
        return builder
    }

    infix fun resideInAModule(modulePath: String): FilesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { file, _, violations ->
            if (file.modulePath != normalized) {
                violations.add(getMessage("file.should.resideInModule", file.declaration.name, normalized, file.modulePath))
            }
        }
        return builder
    }

    infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { file, _, violations ->
            if (!normalizedPaths.contains(file.modulePath)) {
                violations.add(getMessage("file.should.resideInModule", file.declaration.name, normalizedPaths.joinToString(), file.modulePath))
            }
        }
        return builder
    }

    fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): FilesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { file, _, violations ->
            if (file.modulePath == normalized) {
                violations.add(getMessage("file.should.notResideInModule", file.declaration.name, normalized))
            }
        }
        return builder
    }

    infix fun notResideInAModule(modulePaths: List<String>): FilesRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { file, _, violations ->
            if (normalizedPaths.contains(file.modulePath)) {
                violations.add(getMessage("file.should.notResideInModuleAny", file.declaration.name, normalizedPaths.joinToString()))
            }
        }
        return builder
    }

    fun notResideInAModule(vararg modulePaths: String): FilesRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun containClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val contains = file.declaration.classes.any { it.fqName == fqName || it.name == fqName }
            if (!contains) {
                violations.add(getMessage("file.should.containClass", file.declaration.name, fqName))
            }
        }
        return builder
    }

    infix fun containClass(type: KClass<*>): FilesRuleBuilder = containClass(type.kontureQualifiedName())

    infix fun notContainClass(fqName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val contains = file.declaration.classes.any { it.fqName == fqName || it.name == fqName }
            if (contains) {
                violations.add("File ${file.declaration.name} should not contain class $fqName")
            }
        }
        return builder
    }

    infix fun notContainClass(type: KClass<*>): FilesRuleBuilder = notContainClass(type.kontureQualifiedName())

    infix fun haveImportOf(importPath: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val hasImport = file.declaration.imports.any { PatternMatchers.matchesPackage(importPath, it) || it == importPath }
            if (!hasImport) {
                violations.add(getMessage("file.should.haveImportOf", file.declaration.name, importPath))
            }
        }
        return builder
    }

    infix fun haveImportOf(type: KClass<*>): FilesRuleBuilder = haveImportOf(type.kontureQualifiedName())

    infix fun notHaveImportOf(importPath: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val hasImport = file.declaration.imports.any { PatternMatchers.matchesPackage(importPath, it) || it == importPath }
            if (hasImport) {
                violations.add("File ${file.declaration.name} should not have import $importPath")
            }
        }
        return builder
    }

    infix fun notHaveImportOf(type: KClass<*>): FilesRuleBuilder = notHaveImportOf(type.kontureQualifiedName())

    fun containTopLevelFunctions(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelFunctions.isEmpty()) {
                violations.add("File ${file.declaration.name} should contain top-level functions")
            }
        }
        return builder
    }

    fun notContainTopLevelFunctions(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelFunctions.isNotEmpty()) {
                violations.add("File ${file.declaration.name} should not contain top-level functions")
            }
        }
        return builder
    }

    fun containTopLevelProperties(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelProperties.isEmpty()) {
                violations.add("File ${file.declaration.name} should contain top-level properties")
            }
        }
        return builder
    }

    fun notContainTopLevelProperties(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelProperties.isNotEmpty()) {
                violations.add("File ${file.declaration.name} should not contain top-level properties")
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
                    getMessage(
                        "file.should.notContainWildcardImports",
                        file.declaration.name,
                        wildcards.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    fun haveNoWildcardImports(): FilesRuleBuilder = notHaveWildcardImports()

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
                    getMessage(
                        "file.should.matchClassName",
                        "${file.declaration.name} (at ${ViolationLocation.format(
                            file.declaration,
                            file.modulePath,
                            file.sourceSet?.name,
                        )})",
                    ),
                )
            }
        }
        return builder
    }

    fun haveTopLevelFunctions(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelFunctions.isEmpty()) {
                violations.add(getMessage("file.should.haveTopLevelFunctions", file.declaration.name))
            }
        }
        return builder
    }

    fun notHaveTopLevelFunctions(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelFunctions.isNotEmpty()) {
                violations.add(getMessage("file.should.notHaveTopLevelFunctions", file.declaration.name))
            }
        }
        return builder
    }

    fun haveTopLevelProperties(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelProperties.isEmpty()) {
                violations.add(getMessage("file.should.haveTopLevelProperties", file.declaration.name))
            }
        }
        return builder
    }

    fun notHaveTopLevelProperties(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.topLevelProperties.isNotEmpty()) {
                violations.add(getMessage("file.should.notHaveTopLevelProperties", file.declaration.name))
            }
        }
        return builder
    }

    fun haveClasses(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.isEmpty()) {
                violations.add(getMessage("file.should.haveClasses", file.declaration.name))
            }
        }
        return builder
    }

    fun notHaveClasses(): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            if (file.declaration.classes.isNotEmpty()) {
                violations.add(getMessage("file.should.notHaveClasses", file.declaration.name))
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

    infix fun haveAnnotationOf(annotationName: String): FilesRuleBuilder {
        builder.setShould { file, _, violations ->
            val hasAnnotation = file.declaration.classes.any { cls ->
                cls.annotations.any { it.name == annotationName || it.fqName == annotationName }
            }
            if (!hasAnnotation) {
                violations.add(getMessage("file.should.haveAnnotationOf", file.declaration.name, annotationName))
            }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotation: KClass<out Annotation>): FilesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    inline fun <reified T : Annotation> haveAnnotationOf(): FilesRuleBuilder =
        haveAnnotationOf(T::class)

    infix fun satisfy(assertion: (FileDeclarationContext) -> Boolean): FilesRuleBuilder =
        satisfy(
            "custom condition",
        ) { f, _ -> assertion(f) }

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

    fun anyOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                    FilesShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(file, allFiles, subViolations)
                    subViolations.isEmpty()
                }
            if (!anyPassed) {
                violations.add("File ${file.declaration.name} does not satisfy any of the specified conditions")
            }
        }
        return builder
    }

    fun allOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            blocks.forEach { block ->
                val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                FilesShould(subBuilder).apply(block)
                val subAssertion = subBuilder.getShouldAssertion()
                subAssertion?.invoke(file, allFiles, violations)
            }
        }
        return builder
    }

    fun noneOf(vararg blocks: FilesShould.() -> Unit): FilesRuleBuilder {
        builder.setShould { file, allFiles, violations ->
            val anyPassed =
                blocks.any { block ->
                    val subBuilder = FilesRuleBuilder(builder.graph).allowEmpty()
                    FilesShould(subBuilder).apply(block)
                    val subAssertion = subBuilder.getShouldAssertion()
                    val subViolations = mutableListOf<String>()
                    subAssertion?.invoke(file, allFiles, subViolations)
                    subViolations.isEmpty()
                }
            if (anyPassed) {
                violations.add("File ${file.declaration.name} satisfies one of the forbidden conditions")
            }
        }
        return builder
    }
}
