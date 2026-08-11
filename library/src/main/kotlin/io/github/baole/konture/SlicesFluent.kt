/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.SliceGraph

/**
 * Filters slices in this rule using a concise lambda predicate evaluated on each [Slice].
 */
fun SlicesRuleBuilder.that(predicate: Slice.() -> Boolean): SlicesRuleBuilder =
    this.apply {
        setThat { it.predicate() }
    }

/**
 * Asserts rules on slices using a lambda block that provides a [SliceShouldContext] receiver.
 */
fun SlicesRuleBuilder.should(assertion: SliceShouldContext.() -> Any?): SlicesRuleBuilder =
    this.apply {
        setShould { graph, violations ->
            /** Filter or assertion criteria for context. */
            val context = SliceShouldContext(graph, violations)

            /** Filter or assertion criteria for result. */
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add("Slice rule assertion block evaluated to false")
            }
        }
    }

/**
 * Context receiver for writing declarative assertions on a [SliceGraph].
 */
class SliceShouldContext internal constructor(
    internal val graph: SliceGraph,
    /** Filter or assertion criteria for violations. */
    val violations: MutableList<String>,
) {
    /** Filter or assertion criteria for slices. */
    val slices get() = graph.slices

    /** Filter or assertion criteria for adjacency. */
    val adjacency get() = graph.adjacency

    /**
     * Appends a custom violation failure message to the assertion run.
     */
    fun addViolation(message: String) {
        violations.add(message)
    }

    /**
     * Asserts [condition] is true, recording a violation with [message] when false.
     */
    fun check(
        condition: Boolean,
        message: String = "Slice rule assertion condition failed",
    ) {
        if (!condition) {
            addViolation(message)
        }
    }
}
