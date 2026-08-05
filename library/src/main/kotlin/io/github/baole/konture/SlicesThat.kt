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

    fun haveKeyStartingWith(vararg prefixes: String): SlicesRuleBuilder =
        haveKeyStartingWith(prefixes.toList())

    infix fun haveKeyEndingWith(suffix: String): SlicesRuleBuilder {
        builder.setThat { it.key.endsWith(suffix) }
        return builder
    }

    infix fun haveKeyEndingWith(suffixes: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> suffixes.any { slice.key.endsWith(it) } }
        return builder
    }

    fun haveKeyEndingWith(vararg suffixes: String): SlicesRuleBuilder =
        haveKeyEndingWith(suffixes.toList())

    infix fun haveKeyMatching(pattern: String): SlicesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.key) }
        return builder
    }

    infix fun haveKeyMatching(patterns: List<String>): SlicesRuleBuilder {
        builder.setThat { slice -> patterns.any { PatternMatchers.matchesSimpleGlob(it, slice.key) } }
        return builder
    }

    fun haveKeyMatching(vararg patterns: String): SlicesRuleBuilder =
        haveKeyMatching(patterns.toList())

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
    infix fun containClass(type: KClass<*>): SlicesRuleBuilder =
        containClass(type.kontureQualifiedName())

    /**
     * Restricts the slice rule to slices that contain a class of the specified type [T].
     */
    inline fun <reified T : Any> containClass(): SlicesRuleBuilder =
        containClass(T::class)

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
}
