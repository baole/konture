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

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(packagePattern)"))
    public infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder = inPackage(packagePattern)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(packagePatterns)"))
    public infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder = inPackage(packagePatterns)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(*packagePatterns)"))
    public fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = inPackage(*packagePatterns)

    /** Legacy resideInAPackage method. */
    @Deprecated("Use inPackage instead.", ReplaceWith("inPackage(predicate)"))
    public infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder = inPackage(predicate)

    /** Legacy resideInPackageOf method. */
    @Deprecated("Use inPackageOf instead.", ReplaceWith("inPackageOf(type)"))
    public infix fun resideInPackageOf(type: kotlin.reflect.KClass<*>): FilesRuleBuilder = inPackageOf(type)

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

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(name)"))
    public infix fun haveName(name: String): FilesRuleBuilder = named(name)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(names)"))
    public infix fun haveName(names: List<String>): FilesRuleBuilder = named(names)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(*names)"))
    public fun haveName(vararg names: String): FilesRuleBuilder = named(*names)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(predicate)"))
    public infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder = named(predicate)

    /** Legacy haveName method. */
    @Deprecated("Use named instead.", ReplaceWith("named(description, predicate)"))
    public fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): FilesRuleBuilder = named(description, predicate)

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

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(suffix)"))
    public infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder = nameEndsWith(suffix)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(suffixes)"))
    public infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder = nameEndsWith(suffixes)

    /** Legacy haveNameEndingWith method. */
    @Deprecated("Use nameEndsWith instead.", ReplaceWith("nameEndsWith(*suffixes)"))
    public fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = nameEndsWith(*suffixes)

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

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(prefix)"))
    public infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder = nameStartsWith(prefix)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(prefixes)"))
    public infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder = nameStartsWith(prefixes)

    /** Legacy haveNameStartingWith method. */
    @Deprecated("Use nameStartsWith instead.", ReplaceWith("nameStartsWith(*prefixes)"))
    public fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = nameStartsWith(*prefixes)

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

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(pattern)"))
    public infix fun haveNameMatching(pattern: String): FilesRuleBuilder = nameMatches(pattern)

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(patterns)"))
    public infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder = nameMatches(patterns)

    /** Legacy haveNameMatching method. */
    @Deprecated("Use nameMatches instead.", ReplaceWith("nameMatches(*patterns)"))
    public fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = nameMatches(*patterns)

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

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModule instead.", ReplaceWith("inModule(modulePath)"))
    public infix fun resideInAModule(modulePath: String): FilesRuleBuilder = inModule(modulePath)

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(modulePaths)"))
    public infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder = inModules(modulePaths)

    /** Legacy resideInAModule method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(*modulePaths)"))
    public fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = inModules(*modulePaths)

    /** Legacy resideInModule method. */
    @Deprecated("Use inModule instead.", ReplaceWith("inModule(modulePath)"))
    public infix fun resideInModule(modulePath: String): FilesRuleBuilder = inModule(modulePath)

    /** Legacy resideInModules method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(modulePaths)"))
    public infix fun resideInModules(modulePaths: List<String>): FilesRuleBuilder = inModules(modulePaths)

    /** Legacy resideInModules method. */
    @Deprecated("Use inModules instead.", ReplaceWith("inModules(*modulePaths)"))
    public fun resideInModules(vararg modulePaths: String): FilesRuleBuilder = inModules(*modulePaths)

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

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModule instead.", ReplaceWith("notInModule(modulePath)"))
    public infix fun notResideInAModule(modulePath: String): FilesRuleBuilder = notInModule(modulePath)

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(modulePaths)"))
    public infix fun notResideInAModule(modulePaths: List<String>): FilesRuleBuilder = notInModules(modulePaths)

    /** Legacy notResideInAModule method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(*modulePaths)"))
    public fun notResideInAModule(vararg modulePaths: String): FilesRuleBuilder = notInModules(*modulePaths)

    /** Legacy notResideInModule method. */
    @Deprecated("Use notInModule instead.", ReplaceWith("notInModule(modulePath)"))
    public infix fun notResideInModule(modulePath: String): FilesRuleBuilder = notInModule(modulePath)

    /** Legacy notResideInModules method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(modulePaths)"))
    public infix fun notResideInModules(modulePaths: List<String>): FilesRuleBuilder = notInModules(modulePaths)

    /** Legacy notResideInModules method. */
    @Deprecated("Use notInModules instead.", ReplaceWith("notInModules(*modulePaths)"))
    public fun notResideInModules(vararg modulePaths: String): FilesRuleBuilder = notInModules(*modulePaths)

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

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(name)"))
    public infix fun notHaveName(name: String): FilesRuleBuilder = notNamed(name)

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(names)"))
    public infix fun notHaveName(names: List<String>): FilesRuleBuilder = notNamed(names)

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(*names)"))
    public fun notHaveName(vararg names: String): FilesRuleBuilder = notNamed(*names)

    /** Legacy notHaveName method. */
    @Deprecated("Use notNamed instead.", ReplaceWith("notNamed(predicate)"))
    public infix fun notHaveName(predicate: (String) -> Boolean): FilesRuleBuilder = notNamed(predicate)

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

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(prefix)"))
    public infix fun notHaveNameStartingWith(prefix: String): FilesRuleBuilder = notNameStartsWith(prefix)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(prefixes)"))
    public infix fun notHaveNameStartingWith(prefixes: List<String>): FilesRuleBuilder = notNameStartsWith(prefixes)

    /** Legacy notHaveNameStartingWith method. */
    @Deprecated("Use notNameStartsWith instead.", ReplaceWith("notNameStartsWith(*prefixes)"))
    public fun notHaveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = notNameStartsWith(*prefixes)

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

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(suffix)"))
    public infix fun notHaveNameEndingWith(suffix: String): FilesRuleBuilder = notNameEndsWith(suffix)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(suffixes)"))
    public infix fun notHaveNameEndingWith(suffixes: List<String>): FilesRuleBuilder = notNameEndsWith(suffixes)

    /** Legacy notHaveNameEndingWith method. */
    @Deprecated("Use notNameEndsWith instead.", ReplaceWith("notNameEndsWith(*suffixes)"))
    public fun notHaveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = notNameEndsWith(*suffixes)

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

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(pattern)"))
    public infix fun notHaveNameMatching(pattern: String): FilesRuleBuilder = notNameMatches(pattern)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(patterns)"))
    public infix fun notHaveNameMatching(patterns: List<String>): FilesRuleBuilder = notNameMatches(patterns)

    /** Legacy notHaveNameMatching method. */
    @Deprecated("Use notNameMatches instead.", ReplaceWith("notNameMatches(*patterns)"))
    public fun notHaveNameMatching(vararg patterns: String): FilesRuleBuilder = notNameMatches(*patterns)

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

    /** Legacy areAnnotatedWith method. */
    @Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotationFqName)"))
    public infix fun areAnnotatedWith(annotationFqName: String): FilesRuleBuilder = annotatedWith(annotationFqName)

    /** Legacy areAnnotatedWith method. */
    @Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotation)"))
    public infix fun areAnnotatedWith(annotation: kotlin.reflect.KClass<out Annotation>): FilesRuleBuilder =
        annotatedWith(annotation)

    /** Legacy containClassesWithAnnotation method. */
    @Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotationFqName)"))
    public infix fun haveAnnotationOf(annotationName: String): FilesRuleBuilder =
        containClassesWithAnnotation(annotationName)

    /** Legacy containClassesWithAnnotation method. */
    @Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotation)"))
    public infix fun haveAnnotationOf(annotation: kotlin.reflect.KClass<out Annotation>): FilesRuleBuilder =
        containClassesWithAnnotation(annotation)

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

    /** Legacy haveAllAnnotationsOf method. */
    @Deprecated("Use annotatedWithAllOf instead.", ReplaceWith("annotatedWithAllOf(names)"))
    public infix fun haveAllAnnotationsOf(names: List<String>): FilesRuleBuilder = annotatedWithAllOf(names)

    /** Legacy haveAllAnnotationsOf method. */
    @Deprecated("Use annotatedWithAllOf instead.", ReplaceWith("annotatedWithAllOf(*names)"))
    public fun haveAllAnnotationsOf(vararg names: String): FilesRuleBuilder = annotatedWithAllOf(*names)

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

    /** Legacy haveAnyAnnotationOf method. */
    @Deprecated("Use annotatedWithAnyOf instead.", ReplaceWith("annotatedWithAnyOf(names)"))
    public infix fun haveAnyAnnotationOf(names: List<String>): FilesRuleBuilder = annotatedWithAnyOf(names)

    /** Legacy haveAnyAnnotationOf method. */
    @Deprecated("Use annotatedWithAnyOf instead.", ReplaceWith("annotatedWithAnyOf(*names)"))
    public fun haveAnyAnnotationOf(vararg names: String): FilesRuleBuilder = annotatedWithAnyOf(*names)

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

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(packagePattern)"))
    public infix fun notResideInAPackage(packagePattern: String): FilesRuleBuilder = notInPackage(packagePattern)

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(packagePatterns)"))
    public infix fun notResideInAPackage(packagePatterns: List<String>): FilesRuleBuilder =
        notInPackage(packagePatterns)

    /** Legacy notResideInAPackage method. */
    @Deprecated("Use notInPackage instead.", ReplaceWith("notInPackage(*packagePatterns)"))
    public fun notResideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = notInPackage(*packagePatterns)

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
