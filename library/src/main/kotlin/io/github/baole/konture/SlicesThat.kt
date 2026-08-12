/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/** Filter builder for selecting architectural slices matching specific conditions. */
@KontureDsl
public class SlicesThat internal constructor(
    private val builder: SlicesRuleBuilder,
) {
    /** Logical NOT operator for negating the next filter condition. */
    public fun not(): SlicesThat = builder.not()

    /**
     * Restricts the slice rule to slices whose key matches the specified key pattern.
     */
    public infix fun haveKey(keyPattern: String): SlicesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(keyPattern, it.key) }
        return builder
    }

    /** Filter or assertion criteria for have key. */
    public infix fun haveKey(keys: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> keys.any { PatternMatchers.matchesSimpleGlob(it, slice.key) || slice.key == it } }
        return builder
    }

    /** Filter or assertion criteria for have key. */
    public fun haveKey(vararg keys: String): SlicesRuleBuilder = haveKey(keys.toList())

    /** Filter or assertion criteria for have key. */
    public infix fun haveKey(predicate: (String) -> Boolean): SlicesRuleBuilder {
        builder.setThat { slice -> predicate(slice.key) }
        return builder
    }

    /** Filter or assertion criteria for have key starting with. */
    public infix fun haveKeyStartingWith(prefix: String): SlicesRuleBuilder {
        builder.setThat { it.key.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for have key starting with. */
    public infix fun haveKeyStartingWith(prefixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> prefixes.any { slice.key.startsWith(it) } }
        return builder
    }

    /** Filter or assertion criteria for have key starting with. */
    public fun haveKeyStartingWith(vararg prefixes: String): SlicesRuleBuilder = haveKeyStartingWith(prefixes.toList())

    /** Filter or assertion criteria for have key ending with. */
    public infix fun haveKeyEndingWith(suffix: String): SlicesRuleBuilder {
        builder.setThat { it.key.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for have key ending with. */
    public infix fun haveKeyEndingWith(suffixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> suffixes.any { slice.key.endsWith(it) } }
        return builder
    }

    /** Filter or assertion criteria for have key ending with. */
    public fun haveKeyEndingWith(vararg suffixes: String): SlicesRuleBuilder = haveKeyEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have key matching. */
    public infix fun haveKeyMatching(pattern: String): SlicesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.key) }
        return builder
    }

    /** Filter or assertion criteria for have key matching. */
    public infix fun haveKeyMatching(patterns: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> patterns.any { PatternMatchers.matchesSimpleGlob(it, slice.key) } }
        return builder
    }

    /** Filter or assertion criteria for have key matching. */
    public fun haveKeyMatching(vararg patterns: String): SlicesRuleBuilder = haveKeyMatching(patterns.toList())

    /** Filter or assertion criteria for not have key. */
    public infix fun notHaveKey(keyPattern: String): SlicesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(keyPattern, it.key) && it.key != keyPattern }
        return builder
    }

    /** Filter or assertion criteria for not have key. */
    public infix fun notHaveKey(keys: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> !keys.any { PatternMatchers.matchesSimpleGlob(it, slice.key) || slice.key == it } }
        return builder
    }

    /** Filter or assertion criteria for not have key. */
    public fun notHaveKey(vararg keys: String): SlicesRuleBuilder = notHaveKey(keys.toList())

    /** Filter or assertion criteria for not have key starting with. */
    public infix fun notHaveKeyStartingWith(prefix: String): SlicesRuleBuilder {
        builder.setThat { !it.key.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for not have key starting with. */
    public infix fun notHaveKeyStartingWith(prefixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> !prefixes.any { slice.key.startsWith(it) } }
        return builder
    }

    /** Filter or assertion criteria for not have key starting with. */
    public fun notHaveKeyStartingWith(vararg prefixes: String): SlicesRuleBuilder =
        notHaveKeyStartingWith(prefixes.toList())

    /** Filter or assertion criteria for not have key ending with. */
    public infix fun notHaveKeyEndingWith(suffix: String): SlicesRuleBuilder {
        builder.setThat { !it.key.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for not have key ending with. */
    public infix fun notHaveKeyEndingWith(suffixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> !suffixes.any { slice.key.endsWith(it) } }
        return builder
    }

    /** Filter or assertion criteria for not have key ending with. */
    public fun notHaveKeyEndingWith(vararg suffixes: String): SlicesRuleBuilder =
        notHaveKeyEndingWith(suffixes.toList())

    /** Filter or assertion criteria for not have key matching. */
    public infix fun notHaveKeyMatching(pattern: String): SlicesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.key) }
        return builder
    }

    /** Filter or assertion criteria for not have key matching. */
    public infix fun notHaveKeyMatching(patterns: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> !patterns.any { PatternMatchers.matchesSimpleGlob(it, slice.key) } }
        return builder
    }

    /** Filter or assertion criteria for not have key matching. */
    public fun notHaveKeyMatching(vararg patterns: String): SlicesRuleBuilder = notHaveKeyMatching(patterns.toList())

    /**
     * Restricts the slice rule to slices that contain a class with the specified FQN or simple name.
     */
    public infix fun containClass(fqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    /**
     * Restricts the slice rule to slices that contain a class of the specified [KClass].
     */
    public infix fun containClass(type: KClass<*>): SlicesRuleBuilder = containClass(type.kontureQualifiedName())

    /**
     * Restricts the slice rule to slices that contain a class of the specified type [T].
     */
    public inline fun <reified T : Any> containClass(): SlicesRuleBuilder = containClass(T::class)

    /**
     * Restricts the slice rule to slices that contain packages matching the specified pattern.
     */
    public infix fun containClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.packages.any { PatternMatchers.matchesPackage(packagePattern, it) }
        }
        return builder
    }

    /** Filter or assertion criteria for contain classes in package. */
    public infix fun containClassesInPackage(packagePatterns: List<String>): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.packages.any { pkg -> packagePatterns.any { PatternMatchers.matchesPackage(it, pkg) } }
        }
        return builder
    }

    /** Filter or assertion criteria for contain classes in package. */
    public fun containClassesInPackage(vararg packagePatterns: String): SlicesRuleBuilder =
        containClassesInPackage(packagePatterns.toList())

    /** Filter or assertion criteria for contain classes in package. */
    public infix fun containClassesInPackage(predicate: (String) -> Boolean): SlicesRuleBuilder {
        builder.setThat { slice -> slice.packages.any(predicate) }
        return builder
    }

    /**
     * Restricts the slice rule using a custom predicate on [Slice].
     */
    public infix fun satisfy(predicate: (Slice) -> Boolean): SlicesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for contain classes with annotation. */
    public infix fun containClassesWithAnnotation(annotationFqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for contain classes with annotation. */
    public infix fun containClassesWithAnnotation(
        annotation: kotlin.reflect.KClass<out Annotation>,
    ): SlicesRuleBuilder = containClassesWithAnnotation(annotation.kontureQualifiedName())

    /** Filters slices containing classes with annotation [T]. */
    public inline fun <reified T : Annotation> containClassesWithAnnotation(): SlicesRuleBuilder =
        containClassesWithAnnotation(T::class)

    /** Filter or assertion criteria for any of. */
    public fun anyOf(vararg blocks: SlicesThat.() -> Unit): SlicesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = SlicesRuleBuilder(builder.graph)
                SlicesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    public fun allOf(vararg blocks: SlicesThat.() -> Unit): SlicesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = SlicesRuleBuilder(builder.graph)
                SlicesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    public fun noneOf(vararg blocks: SlicesThat.() -> Unit): SlicesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = SlicesRuleBuilder(builder.graph)
                SlicesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for contain class. */
    public infix fun containClass(fqNames: List<String>): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { cls -> fqNames.any { cls.fqName == it || cls.name == it } }
        }
        return builder
    }

    /** Filter or assertion criteria for contain class. */
    public fun containClass(vararg fqNames: String): SlicesRuleBuilder = containClass(fqNames.toList())

    /** Filter or assertion criteria for contain classes with annotation. */
    public infix fun containClassesWithAnnotation(annotationFqNames: List<String>): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { cls ->
                cls.annotations.any { ann -> annotationFqNames.any { ann.name == it || ann.fqName == it } }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for contain classes with annotation. */
    public fun containClassesWithAnnotation(vararg annotationFqNames: String): SlicesRuleBuilder =
        containClassesWithAnnotation(annotationFqNames.toList())

    /** Filter or assertion criteria for not contain class. */
    public infix fun notContainClass(fqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.none { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain class. */
    public infix fun notContainClass(type: KClass<*>): SlicesRuleBuilder = notContainClass(type.kontureQualifiedName())

    /** Filter or assertion criteria for not contain classes in package. */
    public infix fun notContainClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.packages.none { PatternMatchers.matchesPackage(packagePattern, it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain classes with annotation. */
    public infix fun notContainClassesWithAnnotation(annotationFqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.none { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not contain classes with annotation. */
    public infix fun notContainClassesWithAnnotation(annotation: KClass<out Annotation>): SlicesRuleBuilder =
        notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    /** Filters slices not containing class [T]. */
    public inline fun <reified T : Any> notContainClass(): SlicesRuleBuilder = notContainClass(T::class)

    /** Filters slices not containing classes with annotation [T]. */
    public inline fun <reified T : Annotation> notContainClassesWithAnnotation(): SlicesRuleBuilder =
        notContainClassesWithAnnotation(T::class)

    // Name aliases mirroring haveKey / notHaveKey

    /** Filters slices having name matching [namePattern]. */
    public infix fun haveName(namePattern: String): SlicesRuleBuilder = haveKey(namePattern)

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(names: List<String>): SlicesRuleBuilder = haveKey(names)

    /** Filter or assertion criteria for have name. */
    public fun haveName(vararg names: String): SlicesRuleBuilder = haveKey(*names)

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(pattern: String): SlicesRuleBuilder = haveKeyMatching(pattern)

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(patterns: List<String>): SlicesRuleBuilder = haveKeyMatching(patterns)

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(vararg patterns: String): SlicesRuleBuilder = haveKeyMatching(*patterns)

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefix: String): SlicesRuleBuilder = haveKeyStartingWith(prefix)

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefixes: List<String>): SlicesRuleBuilder = haveKeyStartingWith(prefixes)

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(vararg prefixes: String): SlicesRuleBuilder = haveKeyStartingWith(*prefixes)

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffix: String): SlicesRuleBuilder = haveKeyEndingWith(suffix)

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffixes: List<String>): SlicesRuleBuilder = haveKeyEndingWith(suffixes)

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(vararg suffixes: String): SlicesRuleBuilder = haveKeyEndingWith(*suffixes)

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(namePattern: String): SlicesRuleBuilder = notHaveKey(namePattern)

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(pattern: String): SlicesRuleBuilder = notHaveKeyMatching(pattern)

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefix: String): SlicesRuleBuilder = notHaveKeyStartingWith(prefix)

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffix: String): SlicesRuleBuilder = notHaveKeyEndingWith(suffix)

    // Module location filters

    /** Filters slices residing in module [modulePath]. */
    public infix fun resideInAModule(modulePath: String): SlicesRuleBuilder = resideInModule(modulePath)

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePaths: List<String>): SlicesRuleBuilder = resideInModules(modulePaths)

    /** Filter or assertion criteria for reside in a module. */
    public fun resideInAModule(vararg modulePaths: String): SlicesRuleBuilder = resideInModules(modulePaths.toList())

    /** Filter or assertion criteria for reside in module. */
    public infix fun resideInModule(modulePath: String): SlicesRuleBuilder {
        /** Filter or assertion criteria for clean name. */
        val cleanName = modulePath.removePrefix(":").removePrefix("/")
        builder.setThat { slice ->
            slice.classes.any { cls ->
                val normPath = cls.filePath.replace('\\', '/')
                normPath.contains("/$cleanName/") || normPath.contains("$cleanName/")
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in modules. */
    public infix fun resideInModules(modulePaths: List<String>): SlicesRuleBuilder {
        /** Filter or assertion criteria for clean names. */
        val cleanNames = modulePaths.map { it.removePrefix(":").removePrefix("/") }
        builder.setThat { slice ->
            slice.classes.any { cls ->
                val normPath = cls.filePath.replace('\\', '/')
                cleanNames.any { cleanName ->
                    normPath.contains("/$cleanName/") || normPath.contains("$cleanName/")
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in modules. */
    public fun resideInModules(vararg modulePaths: String): SlicesRuleBuilder = resideInModules(modulePaths.toList())

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePath: String): SlicesRuleBuilder = notResideInModule(modulePath)

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePaths: List<String>): SlicesRuleBuilder = notResideInModules(modulePaths)

    /** Filter or assertion criteria for not reside in a module. */
    public fun notResideInAModule(vararg modulePaths: String): SlicesRuleBuilder =
        notResideInModules(modulePaths.toList())

    /** Filter or assertion criteria for not reside in module. */
    public infix fun notResideInModule(modulePath: String): SlicesRuleBuilder {
        /** Filter or assertion criteria for clean name. */
        val cleanName = modulePath.removePrefix(":").removePrefix("/")
        builder.setThat { slice ->
            slice.classes.none { cls ->
                val normPath = cls.filePath.replace('\\', '/')
                normPath.contains("/$cleanName/") || normPath.contains("$cleanName/")
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in modules. */
    public infix fun notResideInModules(modulePaths: List<String>): SlicesRuleBuilder {
        /** Filter or assertion criteria for clean names. */
        val cleanNames = modulePaths.map { it.removePrefix(":").removePrefix("/") }
        builder.setThat { slice ->
            slice.classes.none { cls ->
                val normPath = cls.filePath.replace('\\', '/')
                cleanNames.any { cleanName ->
                    normPath.contains("/$cleanName/") || normPath.contains("$cleanName/")
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in modules. */
    public fun notResideInModules(vararg modulePaths: String): SlicesRuleBuilder =
        notResideInModules(modulePaths.toList())

    // Package location aliases

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePattern: String): SlicesRuleBuilder =
        containClassesInPackage(packagePattern)

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePatterns: List<String>): SlicesRuleBuilder =
        containClassesInPackage(
            packagePatterns,
        )

    /** Filter or assertion criteria for reside in a package. */
    public fun resideInAPackage(vararg packagePatterns: String): SlicesRuleBuilder =
        containClassesInPackage(*packagePatterns)

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePattern: String): SlicesRuleBuilder =
        notContainClassesInPackage(
            packagePattern,
        )
}
