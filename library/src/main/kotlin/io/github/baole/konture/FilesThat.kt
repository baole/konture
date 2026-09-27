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
@Suppress("TooManyFunctions", "LargeClass")
public class FilesThat internal constructor(
    private val builder: FilesRuleBuilder,
) {
    /**
     * Logical NOT operator for negating the next filter condition.
     */
    public fun not(): FilesThat = builder.not()

    /** Filters files in a package matching [packagePattern]. */
    public infix fun inPackage(packagePattern: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.declaration.packageName) }
        return builder
    }

    /** Filters files in packages matching [packagePatterns]. */
    public infix fun inPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.declaration.packageName) }
        }
        return builder
    }

    /** Filters files in packages matching [packagePatterns]. */
    public fun inPackage(vararg packagePatterns: String): FilesRuleBuilder = inPackage(packagePatterns.toList())

    /** Filters files in a package matching [predicate]. */
    public infix fun inPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.packageName) }
        return builder
    }

    /** Filters files in the package of type [type]. */
    public infix fun inPackageOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder =
        inPackage(type.toKonturePackageReference().packageName)

    /** Filter or assertion criteria for have name. */
    public infix fun named(name: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name == name }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public infix fun named(names: List<String>): FilesRuleBuilder {
        builder.setThat { context -> names.contains(context.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public fun named(vararg names: String): FilesRuleBuilder = named(names.toList())

    /** Filter or assertion criteria for have name. */
    public infix fun named(predicate: (String) -> Boolean): FilesRuleBuilder = named("custom name predicate", predicate)

    /** Filter or assertion criteria for have name. */
    @Suppress("UnusedParameter")
    public fun named(
        description: String,
        predicate: (String) -> Boolean,
    ): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for name ending with. */
    public infix fun nameEndsWith(suffix: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for name ending with. */
    public infix fun nameEndsWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for name ending with. */
    public fun nameEndsWith(vararg suffixes: String): FilesRuleBuilder = nameEndsWith(suffixes.toList())

    /** Filter or assertion criteria for name starting with. */
    public infix fun nameStartsWith(prefix: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for name starting with. */
    public infix fun nameStartsWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for name starting with. */
    public fun nameStartsWith(vararg prefixes: String): FilesRuleBuilder = nameStartsWith(prefixes.toList())

    /** Filter or assertion criteria for name matching. */
    public infix fun nameMatches(pattern: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for name matching. */
    public infix fun nameMatches(patterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for name matching. */
    public fun nameMatches(vararg patterns: String): FilesRuleBuilder = nameMatches(patterns.toList())

    /** Filters files in a module matching [modulePath]. */
    public infix fun inModule(modulePath: String): FilesRuleBuilder {
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

    /** Filters files in modules matching [modulePaths]. */
    public infix fun inModules(modulePaths: List<String>): FilesRuleBuilder {
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

    /** Filters files in modules matching [modulePaths]. */
    public fun inModules(vararg modulePaths: String): FilesRuleBuilder = inModules(modulePaths.toList())

    /** Filters files not in a module matching [modulePath]. */
    public infix fun notInModule(modulePath: String): FilesRuleBuilder {
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

    /** Filters files not in modules matching [modulePaths]. */
    public infix fun notInModules(modulePaths: List<String>): FilesRuleBuilder {
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

    /** Filters files not in modules matching [modulePaths]. */
    public fun notInModules(vararg modulePaths: String): FilesRuleBuilder = notInModules(modulePaths.toList())

    /** Filter or assertion criteria for not have name. */
    public infix fun notNamed(name: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name != name }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public infix fun notNamed(names: List<String>): FilesRuleBuilder {
        builder.setThat { !names.contains(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public fun notNamed(vararg names: String): FilesRuleBuilder = notNamed(names.toList())

    /** Filter or assertion criteria for not have name. */
    public infix fun notNamed(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setThat { !predicate(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notNameStartsWith(prefix: String): FilesRuleBuilder {
        builder.setThat { !it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notNameStartsWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public fun notNameStartsWith(vararg prefixes: String): FilesRuleBuilder = notNameStartsWith(prefixes.toList())

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notNameEndsWith(suffix: String): FilesRuleBuilder {
        builder.setThat { !it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notNameEndsWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public fun notNameEndsWith(vararg suffixes: String): FilesRuleBuilder = notNameEndsWith(suffixes.toList())

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notNameMatches(pattern: String): FilesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notNameMatches(patterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public fun notNameMatches(vararg patterns: String): FilesRuleBuilder = notNameMatches(patterns.toList())

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

    /** Filter or assertion criteria for annotatedWith. */
    public infix fun annotatedWith(annotationFqName: String): FilesRuleBuilder =
        containClassesWithAnnotation(annotationFqName)

    /** Filter or assertion criteria for annotatedWith. */
    public infix fun annotatedWith(annotation: kotlin.reflect.KClass<out Annotation>): FilesRuleBuilder =
        containClassesWithAnnotation(annotation)

    /** Filters files containing classes with annotation [T]. */
    public inline fun <reified T : Annotation> annotatedWith(): FilesRuleBuilder = containClassesWithAnnotation<T>()

    /** Filter or assertion criteria for annotatedWithAllOf. */
    public infix fun annotatedWithAllOf(names: List<String>): FilesRuleBuilder {
        builder.setThat { file ->
            names.all { name ->
                file.declaration.classes.any { cls ->
                    cls.annotations.any { it.name == name || it.fqName == name }
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for annotatedWithAllOf. */
    public fun annotatedWithAllOf(vararg names: String): FilesRuleBuilder = annotatedWithAllOf(names.asList())

    /** Filter or assertion criteria for annotatedWithAnyOf. */
    public infix fun annotatedWithAnyOf(names: List<String>): FilesRuleBuilder {
        builder.setThat { file ->
            names.any { name ->
                file.declaration.classes.any { cls ->
                    cls.annotations.any { it.name == name || it.fqName == name }
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for annotatedWithAnyOf. */
    public fun annotatedWithAnyOf(vararg names: String): FilesRuleBuilder = annotatedWithAnyOf(names.asList())

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

    /** Filter or assertion criteria for not in package. */
    public infix fun notInPackage(packagePattern: String): FilesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.declaration.packageName) }
        return builder
    }

    /** Filter or assertion criteria for not in package. */
    public infix fun notInPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.none { PatternMatchers.matchesPackage(it, context.declaration.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for not in package. */
    public fun notInPackage(vararg packagePatterns: String): FilesRuleBuilder = notInPackage(packagePatterns.toList())

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
