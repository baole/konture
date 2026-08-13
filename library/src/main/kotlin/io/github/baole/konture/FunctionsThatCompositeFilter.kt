/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Trait interface for composite, logical and custom predicate filtering on functions.
 */
public interface FunctionsThatCompositeFilter : FunctionsThatScope {
    /** Inverts the next filter condition using logical NOT. */
    public fun not(): FunctionsThat = builder.not()

    /** Filters functions satisfying custom context [predicate]. */
    public infix fun satisfy(predicate: (FunctionDeclarationContext) -> Boolean): FunctionsRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filters functions satisfying at least one condition specified in [blocks]. */
    public fun anyOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /** Filters functions satisfying all conditions specified in [blocks]. */
    public fun allOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /** Filters functions satisfying none of the conditions specified in [blocks]. */
    public fun noneOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }
}
