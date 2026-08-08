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
    /**
     * Logical NOT operator for negating the next filter condition.
     */
    fun not(): FilesThat = builder.not()

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

    infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder = haveName("custom name predicate", predicate)

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

    infix fun resideInModule(modulePath: String): FilesRuleBuilder = resideInAModule(modulePath)

    infix fun resideInModules(modulePaths: List<String>): FilesRuleBuilder = resideInAModule(modulePaths)

    fun resideInModules(vararg modulePaths: String): FilesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): FilesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { context ->
            val match =
                context.modulePath == normalized || PatternMatchers.matchesModuleGlob(normalized, context.modulePath)
            !match
        }
        return builder
    }

    infix fun notResideInAModule(modulePaths: List<String>): FilesRuleBuilder {
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setThat { context ->
            val match =
                normalized.any { target ->
                    context.modulePath == target || PatternMatchers.matchesModuleGlob(target, context.modulePath)
                }
            !match
        }
        return builder
    }

    fun notResideInAModule(vararg modulePaths: String): FilesRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun notResideInModule(modulePath: String): FilesRuleBuilder = notResideInAModule(modulePath)

    infix fun notResideInModules(modulePaths: List<String>): FilesRuleBuilder = notResideInAModule(modulePaths)

    fun notResideInModules(vararg modulePaths: String): FilesRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun notHaveName(name: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name != name }
        return builder
    }

    infix fun notHaveName(names: List<String>): FilesRuleBuilder {
        builder.setThat { !names.contains(it.declaration.name) }
        return builder
    }

    fun notHaveName(vararg names: String): FilesRuleBuilder = notHaveName(names.toList())

    infix fun notHaveName(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setThat { !predicate(it.declaration.name) }
        return builder
    }

    infix fun notHaveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setThat { !it.declaration.name.startsWith(prefix) }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = notHaveNameStartingWith(prefixes.toList())

    infix fun notHaveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setThat { !it.declaration.name.endsWith(suffix) }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = notHaveNameEndingWith(suffixes.toList())

    infix fun notHaveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): FilesRuleBuilder = notHaveNameMatching(patterns.toList())

    infix fun containClass(fqName: String): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.classes.any { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    infix fun containClass(fqNames: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.classes.any { cls -> fqNames.any { cls.fqName == it || cls.name == it } }
        }
        return builder
    }

    fun containClass(vararg fqNames: String): FilesRuleBuilder = containClass(fqNames.toList())

    infix fun containClass(type: kotlin.reflect.KClass<*>): FilesRuleBuilder = containClass(type.kontureQualifiedName())

    fun containClass(vararg types: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        containClass(types.map { it.kontureQualifiedName() })

    inline fun <reified T : Any> containClass(): FilesRuleBuilder = containClass(T::class)

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

    inline fun <reified T : Annotation> containClassesWithAnnotation(): FilesRuleBuilder =
        containClassesWithAnnotation(T::class)

    infix fun haveImportOf(importPath: String): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.imports.any { PatternMatchers.matchesPackage(importPath, it) || it == importPath }
        }
        return builder
    }

    infix fun haveImportOf(imports: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.imports.any { imp ->
                imports.any { PatternMatchers.matchesPackage(it, imp) || imp == it }
            }
        }
        return builder
    }

    fun haveImportOf(vararg imports: String): FilesRuleBuilder = haveImportOf(imports.toList())

    infix fun haveImportOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder = haveImportOf(type.kontureQualifiedName())

    fun haveImportOf(vararg types: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        haveImportOf(types.map { it.kontureQualifiedName() })

    inline fun <reified T : Any> haveImportOf(): FilesRuleBuilder = haveImportOf(T::class)

    fun containTopLevelFunctions(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelFunctions.isNotEmpty() }
        return builder
    }

    fun notContainTopLevelFunctions(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelFunctions.isEmpty() }
        return builder
    }

    fun containTopLevelProperties(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelProperties.isNotEmpty() }
        return builder
    }

    fun notContainTopLevelProperties(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelProperties.isEmpty() }
        return builder
    }

    fun containClasses(): FilesRuleBuilder {
        builder.setThat { it.declaration.classes.isNotEmpty() }
        return builder
    }

    fun notContainClasses(): FilesRuleBuilder {
        builder.setThat { it.declaration.classes.isEmpty() }
        return builder
    }

    infix fun satisfy(predicate: (FileDeclarationContext) -> Boolean): FilesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    infix fun haveAnnotationOf(annotationName: String): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.classes.any { cls ->
                cls.annotations.any { it.name == annotationName || it.fqName == annotationName }
            }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotation: kotlin.reflect.KClass<out Annotation>): FilesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    infix fun haveAllAnnotationsOf(names: List<String>): FilesRuleBuilder {
        builder.setThat { file ->
            names.all { name ->
                file.declaration.classes.any { cls ->
                    cls.annotations.any { it.name == name || it.fqName == name }
                }
            }
        }
        return builder
    }

    fun haveAllAnnotationsOf(vararg names: String): FilesRuleBuilder = haveAllAnnotationsOf(names.asList())

    infix fun haveAnyAnnotationOf(names: List<String>): FilesRuleBuilder {
        builder.setThat { file ->
            names.any { name ->
                file.declaration.classes.any { cls ->
                    cls.annotations.any { it.name == name || it.fqName == name }
                }
            }
        }
        return builder
    }

    fun haveAnyAnnotationOf(vararg names: String): FilesRuleBuilder = haveAnyAnnotationOf(names.asList())

    fun anyOf(vararg blocks: FilesThat.() -> Unit): FilesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FilesRuleBuilder(builder.graph)
                FilesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    fun allOf(vararg blocks: FilesThat.() -> Unit): FilesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FilesRuleBuilder(builder.graph)
                FilesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    fun noneOf(vararg blocks: FilesThat.() -> Unit): FilesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FilesRuleBuilder(builder.graph)
                FilesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }

    infix fun notResideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.declaration.packageName) }
        return builder
    }

    infix fun notResideInAPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.none { PatternMatchers.matchesPackage(it, context.declaration.packageName) }
        }
        return builder
    }

    fun notResideInAPackage(vararg packagePatterns: String): FilesRuleBuilder =
        notResideInAPackage(
            packagePatterns.toList(),
        )

    infix fun notContainClass(fqName: String): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.classes.none { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    infix fun notContainClass(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        notContainClass(type.kontureQualifiedName())

    infix fun notContainClassesWithAnnotation(annotationFqName: String): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.classes.none { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    infix fun notContainClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): FilesRuleBuilder =
        notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    infix fun notHaveImportOf(importPath: String): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.imports.none { it == importPath || it.endsWith(".$importPath") }
        }
        return builder
    }

    infix fun notHaveImportOf(imports: List<String>): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.imports.none { imp -> imports.any { it == imp || imp.endsWith(".$it") } }
        }
        return builder
    }

    fun notHaveImportOf(vararg imports: String): FilesRuleBuilder = notHaveImportOf(imports.toList())

    infix fun notHaveImportOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        notHaveImportOf(type.kontureQualifiedName())
}
