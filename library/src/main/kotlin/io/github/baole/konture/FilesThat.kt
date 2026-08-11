/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.impl.PatternMatchers

/** Filter builder for selecting file declarations matching specific conditions. */
@KontureDsl
public class FilesThat internal constructor(
    private val builder: FilesRuleBuilder,
) {
    /**
     * Logical NOT operator for negating the next filter condition.
     */
    public fun not(): FilesThat = builder.not()

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.declaration.packageName) }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.declaration.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    public fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.packageName) }
        return builder
    }

    /** Filter or assertion criteria for reside in package of. */
    public infix fun resideInPackageOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(name: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name == name }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(names: List<String>): FilesRuleBuilder {
        builder.setThat { context -> names.contains(context.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public fun haveName(vararg names: String): FilesRuleBuilder = haveName(names.toList())

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder =
        haveName("custom name predicate", predicate)

    /** Filter or assertion criteria for have name. */
    @Suppress("UnusedParameter")
    public fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = haveNameMatching(patterns.toList())

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePath: String): FilesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
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

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder {
        /** Filter or assertion criteria for normalized paths. */
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

    /** Filter or assertion criteria for reside in a module. */
    public fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for reside in module. */
    public infix fun resideInModule(modulePath: String): FilesRuleBuilder = resideInAModule(modulePath)

    /** Filter or assertion criteria for reside in modules. */
    public infix fun resideInModules(modulePaths: List<String>): FilesRuleBuilder = resideInAModule(modulePaths)

    /** Filter or assertion criteria for reside in modules. */
    public fun resideInModules(vararg modulePaths: String): FilesRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePath: String): FilesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { context ->
            /** Filter or assertion criteria for match. */
            val match =
                context.modulePath == normalized || PatternMatchers.matchesModuleGlob(normalized, context.modulePath)
            !match
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePaths: List<String>): FilesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setThat { context ->
            /** Filter or assertion criteria for match. */
            val match =
                normalized.any { target ->
                    context.modulePath == target || PatternMatchers.matchesModuleGlob(target, context.modulePath)
                }
            !match
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in a module. */
    public fun notResideInAModule(vararg modulePaths: String): FilesRuleBuilder =
        notResideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in module. */
    public infix fun notResideInModule(modulePath: String): FilesRuleBuilder = notResideInAModule(modulePath)

    /** Filter or assertion criteria for not reside in modules. */
    public infix fun notResideInModules(modulePaths: List<String>): FilesRuleBuilder = notResideInAModule(modulePaths)

    /** Filter or assertion criteria for not reside in modules. */
    public fun notResideInModules(vararg modulePaths: String): FilesRuleBuilder =
        notResideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(name: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name != name }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(names: List<String>): FilesRuleBuilder {
        builder.setThat { !names.contains(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public fun notHaveName(vararg names: String): FilesRuleBuilder = notHaveName(names.toList())

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setThat { !predicate(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setThat { !it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public fun notHaveNameStartingWith(vararg prefixes: String): FilesRuleBuilder =
        notHaveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setThat { !it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public fun notHaveNameEndingWith(vararg suffixes: String): FilesRuleBuilder =
        notHaveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public fun notHaveNameMatching(vararg patterns: String): FilesRuleBuilder = notHaveNameMatching(patterns.toList())

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(fqName: String): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.classes.any { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(fqNames: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.classes.any { cls -> fqNames.any { cls.fqName == it || cls.name == it } }
        }
        return builder
    }

    /** Filter or assertion criteria for contain class. */
    public fun containClass(vararg fqNames: String): FilesRuleBuilder = containClass(fqNames.toList())

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        containClass(type.kontureQualifiedName())

    /** Filter or assertion criteria for contain class. */
    public fun containClass(vararg types: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        containClass(types.map { it.kontureQualifiedName() })

    /** Filters files containing class [T]. */
    public inline fun <reified T : Any> containClass(): FilesRuleBuilder = containClass(T::class)

    /** Filter or assertion criteria for contain classes with annotation. */
    public infix fun containClassesWithAnnotation(annotationFqName: String): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.classes.any { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for contain classes with annotation. */
    public infix fun containClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): FilesRuleBuilder =
        containClassesWithAnnotation(annotation.kontureQualifiedName())

    /** Filters files containing classes with annotation [T]. */
    public inline fun <reified T : Annotation> containClassesWithAnnotation(): FilesRuleBuilder =
        containClassesWithAnnotation(T::class)

    /** Filter or assertion criteria for have import of. */
    public infix fun haveImportOf(importPath: String): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.imports.any { PatternMatchers.matchesPackage(importPath, it) || it == importPath }
        }
        return builder
    }

    /** Filter or assertion criteria for have import of. */
    public infix fun haveImportOf(imports: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            context.declaration.imports.any { imp ->
                imports.any { PatternMatchers.matchesPackage(it, imp) || imp == it }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have import of. */
    public fun haveImportOf(vararg imports: String): FilesRuleBuilder = haveImportOf(imports.toList())

    /** Filter or assertion criteria for have import of. */
    public infix fun haveImportOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        haveImportOf(type.kontureQualifiedName())

    /** Filter or assertion criteria for have import of. */
    public fun haveImportOf(vararg types: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        haveImportOf(types.map { it.kontureQualifiedName() })

    /** Filters files having import of [T]. */
    public inline fun <reified T : Any> haveImportOf(): FilesRuleBuilder = haveImportOf(T::class)

    /** Filter or assertion criteria for contain top level functions. */
    public fun containTopLevelFunctions(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelFunctions.isNotEmpty() }
        return builder
    }

    /** Filter or assertion criteria for not contain top level functions. */
    public fun notContainTopLevelFunctions(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelFunctions.isEmpty() }
        return builder
    }

    /** Filter or assertion criteria for contain top level properties. */
    public fun containTopLevelProperties(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelProperties.isNotEmpty() }
        return builder
    }

    /** Filter or assertion criteria for not contain top level properties. */
    public fun notContainTopLevelProperties(): FilesRuleBuilder {
        builder.setThat { it.declaration.topLevelProperties.isEmpty() }
        return builder
    }

    /** Filter or assertion criteria for contain classes. */
    public fun containClasses(): FilesRuleBuilder {
        builder.setThat { it.declaration.classes.isNotEmpty() }
        return builder
    }

    /** Filter or assertion criteria for not contain classes. */
    public fun notContainClasses(): FilesRuleBuilder {
        builder.setThat { it.declaration.classes.isEmpty() }
        return builder
    }

    /** Filter or assertion criteria for satisfy. */
    public infix fun satisfy(predicate: (FileDeclarationContext) -> Boolean): FilesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotationName: String): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.classes.any { cls ->
                cls.annotations.any { it.name == annotationName || it.fqName == annotationName }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotation: kotlin.reflect.KClass<out Annotation>): FilesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for have all annotations of. */
    public infix fun haveAllAnnotationsOf(names: List<String>): FilesRuleBuilder {
        builder.setThat { file ->
            names.all { name ->
                file.declaration.classes.any { cls ->
                    cls.annotations.any { it.name == name || it.fqName == name }
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have all annotations of. */
    public fun haveAllAnnotationsOf(vararg names: String): FilesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /** Filter or assertion criteria for have any annotation of. */
    public infix fun haveAnyAnnotationOf(names: List<String>): FilesRuleBuilder {
        builder.setThat { file ->
            names.any { name ->
                file.declaration.classes.any { cls ->
                    cls.annotations.any { it.name == name || it.fqName == name }
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have any annotation of. */
    public fun haveAnyAnnotationOf(vararg names: String): FilesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /** Filter or assertion criteria for any of. */
    public fun anyOf(vararg blocks: FilesThat.() -> Unit): FilesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = FilesRuleBuilder(builder.graph)
                FilesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    public fun allOf(vararg blocks: FilesThat.() -> Unit): FilesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = FilesRuleBuilder(builder.graph)
                FilesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    public fun noneOf(vararg blocks: FilesThat.() -> Unit): FilesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = FilesRuleBuilder(builder.graph)
                FilesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.declaration.packageName) }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.none { PatternMatchers.matchesPackage(it, context.declaration.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    public fun notResideInAPackage(vararg packagePatterns: String): FilesRuleBuilder =
        notResideInAPackage(
            packagePatterns.toList(),
        )

    /** Filter or assertion criteria for not contain class. */
    public infix fun notContainClass(fqName: String): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.classes.none { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain class. */
    public infix fun notContainClass(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        notContainClass(type.kontureQualifiedName())

    /** Filter or assertion criteria for not contain classes with annotation. */
    public infix fun notContainClassesWithAnnotation(annotationFqName: String): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.classes.none { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain classes with annotation. */
    public infix fun notContainClassesWithAnnotation(
        annotation: kotlin.reflect.KClass<out Annotation>,
    ): FilesRuleBuilder = notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for not have import of. */
    public infix fun notHaveImportOf(importPath: String): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.imports.none { it == importPath || it.endsWith(".$importPath") }
        }
        return builder
    }

    /** Filter or assertion criteria for not have import of. */
    public infix fun notHaveImportOf(imports: List<String>): FilesRuleBuilder {
        builder.setThat { file ->
            file.declaration.imports.none { imp -> imports.any { it == imp || imp.endsWith(".$it") } }
        }
        return builder
    }

    /** Filter or assertion criteria for not have import of. */
    public fun notHaveImportOf(vararg imports: String): FilesRuleBuilder = notHaveImportOf(imports.toList())

    /** Filter or assertion criteria for not have import of. */
    public infix fun notHaveImportOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        notHaveImportOf(type.kontureQualifiedName())
}
