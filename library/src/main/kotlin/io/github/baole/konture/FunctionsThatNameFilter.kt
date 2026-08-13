/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Trait interface for name matching and naming pattern filtering on functions.
 */
@Suppress("ComplexInterface")
public interface FunctionsThatNameFilter : FunctionsThatScope {
    /** Filters functions whose simple name equals [name]. */
    public infix fun haveName(name: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name == name }
        return builder
    }

    /** Filters functions whose simple name is contained in [names]. */
    public infix fun haveName(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { names.contains(it.declaration.name) }
        return builder
    }

    /** Filters functions whose simple name is contained in [names]. */
    public fun haveName(vararg names: String): FunctionsRuleBuilder = haveName(names.toList())

    /** Filters functions whose simple name does not equal [name]. */
    public infix fun notHaveName(name: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name != name }
        return builder
    }

    /** Filters functions whose simple name is not contained in [names]. */
    public infix fun notHaveName(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { !names.contains(it.declaration.name) }
        return builder
    }

    /** Filters functions whose simple name is not contained in [names]. */
    public fun notHaveName(vararg names: String): FunctionsRuleBuilder = notHaveName(names.toList())

    /** Filters functions whose simple name does not satisfy [predicate]. */
    public infix fun notHaveName(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { !predicate(it.declaration.name) }
        return builder
    }

    /** Filters functions whose simple name satisfies [predicate]. */
    public infix fun haveName(predicate: (String) -> Boolean): FunctionsRuleBuilder =
        haveName("custom name predicate", predicate)

    /** Filters functions whose simple name satisfies [predicate] with a custom [description]. */
    @Suppress("UnusedParameter")
    public fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): FunctionsRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    /** Filters functions whose simple name ends with [suffix]. */
    public infix fun haveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filters functions whose simple name ends with any of [suffixes]. */
    public infix fun haveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filters functions whose simple name ends with any of [suffixes]. */
    public fun haveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Filters functions whose simple name does not end with [suffix]. */
    public infix fun notHaveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setThat { !it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filters functions whose simple name does not end with any of [suffixes]. */
    public infix fun notHaveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filters functions whose simple name does not end with any of [suffixes]. */
    public fun notHaveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder =
        notHaveNameEndingWith(suffixes.toList())

    /** Filters functions whose simple name starts with [prefix]. */
    public infix fun haveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filters functions whose simple name starts with any of [prefixes]. */
    public infix fun haveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filters functions whose simple name starts with any of [prefixes]. */
    public fun haveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder =
        haveNameStartingWith(prefixes.toList())

    /** Filters functions whose simple name does not start with [prefix]. */
    public infix fun notHaveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setThat { !it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filters functions whose simple name does not start with any of [prefixes]. */
    public infix fun notHaveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filters functions whose simple name does not start with any of [prefixes]. */
    public fun notHaveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder =
        notHaveNameStartingWith(prefixes.toList())

    /** Filters functions whose simple name matches glob [pattern]. */
    public infix fun haveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filters functions whose simple name matches any glob pattern in [patterns]. */
    public infix fun haveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filters functions whose simple name matches any glob pattern in [patterns]. */
    public fun haveNameMatching(vararg patterns: String): FunctionsRuleBuilder = haveNameMatching(patterns.toList())

    /** Filters functions whose simple name does not match glob [pattern]. */
    public infix fun notHaveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filters functions whose simple name does not match any glob pattern in [patterns]. */
    public infix fun notHaveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filters functions whose simple name does not match any glob pattern in [patterns]. */
    public fun notHaveNameMatching(vararg patterns: String): FunctionsRuleBuilder =
        notHaveNameMatching(patterns.toList())
}
