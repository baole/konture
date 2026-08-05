/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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

    infix fun resideInPackageOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    infix fun haveName(name: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name == name }
        return builder
    }

    infix fun haveName(names: List<String>): FilesRuleBuilder {
        builder.setThat { context -> names.contains(context.declaration.name) }
        return builder
    }

    fun haveName(vararg names: String): FilesRuleBuilder = haveName(names.toList())

    infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder =
        haveName("custom name predicate", predicate)

    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
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

    infix fun containClass(fqName: String): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.classes.any { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    infix fun containClass(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        containClass(type.kontureQualifiedName())

    infix fun containClassesWithAnnotation(annotationFqName: String): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.classes.any { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    infix fun containClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): FilesRuleBuilder =
        containClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun haveImportOf(importPath: String): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.imports.any { PatternMatchers.matchesPackage(importPath, it) || it == importPath }
        }
        return builder
    }

    infix fun haveImportOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        haveImportOf(type.kontureQualifiedName())

    fun containTopLevelFunctions(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelFunctions.isNotEmpty() }
        return builder
    }

    fun containTopLevelProperties(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelProperties.isNotEmpty() }
        return builder
    }

    fun containClasses(): FilesRuleBuilder {
        builder.setThat { it.declaration.classes.isNotEmpty() }
        return builder
    }

    infix fun satisfy(predicate: (FileDeclarationContext) -> Boolean): FilesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }
}



