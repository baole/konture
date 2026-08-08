/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

@KontureDsl
class SlicesThat internal constructor(
    private val builder: SlicesRuleBuilder,
) {
    /** Logical NOT operator for negating the next filter condition. */
    fun not(): SlicesThat = builder.not()

    /**
     * Restricts the slice rule to slices whose key matches the specified key pattern.
     */
    infix fun haveKey(keyPattern: String): SlicesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(keyPattern, it.key) }
        return builder
    }

    infix fun haveKey(keys: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> keys.any { PatternMatchers.matchesSimpleGlob(it, slice.key) || slice.key == it } }
        return builder
    }

    fun haveKey(vararg keys: String): SlicesRuleBuilder = haveKey(keys.toList())

    infix fun haveKey(predicate: (String) -> Boolean): SlicesRuleBuilder {
        builder.setThat { slice -> predicate(slice.key) }
        return builder
    }

    infix fun haveKeyStartingWith(prefix: String): SlicesRuleBuilder {
        builder.setThat { it.key.startsWith(prefix) }
        return builder
    }

    infix fun haveKeyStartingWith(prefixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> prefixes.any { slice.key.startsWith(it) } }
        return builder
    }

    fun haveKeyStartingWith(vararg prefixes: String): SlicesRuleBuilder = haveKeyStartingWith(prefixes.toList())

    infix fun haveKeyEndingWith(suffix: String): SlicesRuleBuilder {
        builder.setThat { it.key.endsWith(suffix) }
        return builder
    }

    infix fun haveKeyEndingWith(suffixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> suffixes.any { slice.key.endsWith(it) } }
        return builder
    }

    fun haveKeyEndingWith(vararg suffixes: String): SlicesRuleBuilder = haveKeyEndingWith(suffixes.toList())

    infix fun haveKeyMatching(pattern: String): SlicesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.key) }
        return builder
    }

    infix fun haveKeyMatching(patterns: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> patterns.any { PatternMatchers.matchesSimpleGlob(it, slice.key) } }
        return builder
    }

    fun haveKeyMatching(vararg patterns: String): SlicesRuleBuilder = haveKeyMatching(patterns.toList())

    infix fun notHaveKey(keyPattern: String): SlicesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(keyPattern, it.key) && it.key != keyPattern }
        return builder
    }

    infix fun notHaveKey(keys: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> !keys.any { PatternMatchers.matchesSimpleGlob(it, slice.key) || slice.key == it } }
        return builder
    }

    fun notHaveKey(vararg keys: String): SlicesRuleBuilder = notHaveKey(keys.toList())

    infix fun notHaveKeyStartingWith(prefix: String): SlicesRuleBuilder {
        builder.setThat { !it.key.startsWith(prefix) }
        return builder
    }

    infix fun notHaveKeyStartingWith(prefixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> !prefixes.any { slice.key.startsWith(it) } }
        return builder
    }

    fun notHaveKeyStartingWith(vararg prefixes: String): SlicesRuleBuilder = notHaveKeyStartingWith(prefixes.toList())

    infix fun notHaveKeyEndingWith(suffix: String): SlicesRuleBuilder {
        builder.setThat { !it.key.endsWith(suffix) }
        return builder
    }

    infix fun notHaveKeyEndingWith(suffixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> !suffixes.any { slice.key.endsWith(it) } }
        return builder
    }

    fun notHaveKeyEndingWith(vararg suffixes: String): SlicesRuleBuilder = notHaveKeyEndingWith(suffixes.toList())

    infix fun notHaveKeyMatching(pattern: String): SlicesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.key) }
        return builder
    }

    infix fun notHaveKeyMatching(patterns: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> !patterns.any { PatternMatchers.matchesSimpleGlob(it, slice.key) } }
        return builder
    }

    fun notHaveKeyMatching(vararg patterns: String): SlicesRuleBuilder = notHaveKeyMatching(patterns.toList())

    /**
     * Restricts the slice rule to slices that contain a class with the specified FQN or simple name.
     */
    infix fun containClass(fqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    /**
     * Restricts the slice rule to slices that contain a class of the specified [KClass].
     */
    infix fun containClass(type: KClass<*>): SlicesRuleBuilder = containClass(type.kontureQualifiedName())

    /**
     * Restricts the slice rule to slices that contain a class of the specified type [T].
     */
    inline fun <reified T : Any> containClass(): SlicesRuleBuilder = containClass(T::class)

    /**
     * Restricts the slice rule to slices that contain packages matching the specified pattern.
     */
    infix fun containClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.packages.any { PatternMatchers.matchesPackage(packagePattern, it) }
        }
        return builder
    }

    infix fun containClassesInPackage(packagePatterns: List<String>): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.packages.any { pkg -> packagePatterns.any { PatternMatchers.matchesPackage(it, pkg) } }
        }
        return builder
    }

    fun containClassesInPackage(vararg packagePatterns: String): SlicesRuleBuilder =
        containClassesInPackage(packagePatterns.toList())

    infix fun containClassesInPackage(predicate: (String) -> Boolean): SlicesRuleBuilder {
        builder.setThat { slice -> slice.packages.any(predicate) }
        return builder
    }

    /**
     * Restricts the slice rule using a custom predicate on [Slice].
     */
    infix fun satisfy(predicate: (Slice) -> Boolean): SlicesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    infix fun containClassesWithAnnotation(annotationFqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    infix fun containClassesWithAnnotation(annotation: kotlin.reflect.KClass<out Annotation>): SlicesRuleBuilder =
        containClassesWithAnnotation(annotation.kontureQualifiedName())

    inline fun <reified T : Annotation> containClassesWithAnnotation(): SlicesRuleBuilder =
        containClassesWithAnnotation(T::class)

    fun anyOf(vararg blocks: SlicesThat.() -> Unit): SlicesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = SlicesRuleBuilder(builder.graph)
                SlicesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    fun allOf(vararg blocks: SlicesThat.() -> Unit): SlicesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = SlicesRuleBuilder(builder.graph)
                SlicesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    fun noneOf(vararg blocks: SlicesThat.() -> Unit): SlicesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = SlicesRuleBuilder(builder.graph)
                SlicesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }

    infix fun containClass(fqNames: List<String>): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { cls -> fqNames.any { cls.fqName == it || cls.name == it } }
        }
        return builder
    }

    fun containClass(vararg fqNames: String): SlicesRuleBuilder = containClass(fqNames.toList())

    infix fun containClassesWithAnnotation(annotationFqNames: List<String>): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { cls ->
                cls.annotations.any { ann -> annotationFqNames.any { ann.name == it || ann.fqName == it } }
            }
        }
        return builder
    }

    fun containClassesWithAnnotation(vararg annotationFqNames: String): SlicesRuleBuilder =
        containClassesWithAnnotation(annotationFqNames.toList())

    infix fun notContainClass(fqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.none { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    infix fun notContainClass(type: KClass<*>): SlicesRuleBuilder = notContainClass(type.kontureQualifiedName())

    infix fun notContainClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.packages.none { PatternMatchers.matchesPackage(packagePattern, it) }
        }
        return builder
    }

    infix fun notContainClassesWithAnnotation(annotationFqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.none { cls ->
                cls.annotations.any { it.name == annotationFqName || it.fqName == annotationFqName }
            }
        }
        return builder
    }

    infix fun notContainClassesWithAnnotation(annotation: KClass<out Annotation>): SlicesRuleBuilder =
        notContainClassesWithAnnotation(annotation.kontureQualifiedName())

    inline fun <reified T : Any> notContainClass(): SlicesRuleBuilder = notContainClass(T::class)

    inline fun <reified T : Annotation> notContainClassesWithAnnotation(): SlicesRuleBuilder =
        notContainClassesWithAnnotation(T::class)

    // Name aliases mirroring haveKey / notHaveKey
    infix fun haveName(namePattern: String): SlicesRuleBuilder = haveKey(namePattern)

    infix fun haveName(names: List<String>): SlicesRuleBuilder = haveKey(names)

    fun haveName(vararg names: String): SlicesRuleBuilder = haveKey(*names)

    infix fun haveNameMatching(pattern: String): SlicesRuleBuilder = haveKeyMatching(pattern)

    infix fun haveNameMatching(patterns: List<String>): SlicesRuleBuilder = haveKeyMatching(patterns)

    fun haveNameMatching(vararg patterns: String): SlicesRuleBuilder = haveKeyMatching(*patterns)

    infix fun haveNameStartingWith(prefix: String): SlicesRuleBuilder = haveKeyStartingWith(prefix)

    infix fun haveNameStartingWith(prefixes: List<String>): SlicesRuleBuilder = haveKeyStartingWith(prefixes)

    fun haveNameStartingWith(vararg prefixes: String): SlicesRuleBuilder = haveKeyStartingWith(*prefixes)

    infix fun haveNameEndingWith(suffix: String): SlicesRuleBuilder = haveKeyEndingWith(suffix)

    infix fun haveNameEndingWith(suffixes: List<String>): SlicesRuleBuilder = haveKeyEndingWith(suffixes)

    fun haveNameEndingWith(vararg suffixes: String): SlicesRuleBuilder = haveKeyEndingWith(*suffixes)

    infix fun notHaveName(namePattern: String): SlicesRuleBuilder = notHaveKey(namePattern)

    infix fun notHaveNameMatching(pattern: String): SlicesRuleBuilder = notHaveKeyMatching(pattern)

    infix fun notHaveNameStartingWith(prefix: String): SlicesRuleBuilder = notHaveKeyStartingWith(prefix)

    infix fun notHaveNameEndingWith(suffix: String): SlicesRuleBuilder = notHaveKeyEndingWith(suffix)

    // Module location filters
    infix fun resideInModule(modulePath: String): SlicesRuleBuilder {
        builder.setThat { slice -> slice.classes.any { it.filePath.contains(modulePath) } }
        return builder
    }

    infix fun resideInModules(modulePaths: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> slice.classes.any { cls -> modulePaths.any { cls.filePath.contains(it) } } }
        return builder
    }

    fun resideInModules(vararg modulePaths: String): SlicesRuleBuilder = resideInModules(modulePaths.toList())

    infix fun notResideInModule(modulePath: String): SlicesRuleBuilder {
        builder.setThat { slice -> slice.classes.none { it.filePath.contains(modulePath) } }
        return builder
    }

    infix fun notResideInModules(modulePaths: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> slice.classes.none { cls -> modulePaths.any { cls.filePath.contains(it) } } }
        return builder
    }

    fun notResideInModules(vararg modulePaths: String): SlicesRuleBuilder = notResideInModules(modulePaths.toList())

    // Package location aliases
    infix fun resideInAPackage(packagePattern: String): SlicesRuleBuilder = containClassesInPackage(packagePattern)

    infix fun resideInAPackage(packagePatterns: List<String>): SlicesRuleBuilder =
        containClassesInPackage(
            packagePatterns,
        )

    fun resideInAPackage(vararg packagePatterns: String): SlicesRuleBuilder = containClassesInPackage(*packagePatterns)

    infix fun notResideInAPackage(packagePattern: String): SlicesRuleBuilder =
        notContainClassesInPackage(
            packagePattern,
        )
}
