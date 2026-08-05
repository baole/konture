/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Octavio Calleya Garcia (@octaviospain), Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.SliceCycleDetector

/**
 * Assertions that the slices derived by [SlicesRuleBuilder] must satisfy.
 */
@KontureDsl
class SlicesShould(private val builder: SlicesRuleBuilder) {
    /**
     * Asserts that the slices have no cyclic dependencies between them. Each detected cycle is
     * reported as a path, e.g. `payment -> billing -> payment`.
     */
    fun beFreeOfCycles(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (cycle in SliceCycleDetector.findCycles(sliceGraph.adjacency)) {
                val rendered = (cycle + cycle.first()).joinToString(" -> ")
                violations.add(getMessage("slice.should.beFreeOfCycles", rendered))
            }
        }
        return builder
    }

    /**
     * Asserts that no slice depends on any other slice — enforcing complete isolation between them.
     * Every inter-slice dependency is reported.
     */
    fun notDependOnEachOther(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                for (to in targets.sorted()) {
                    violations.add(getMessage("slice.should.notDependOnEachOther", from, to))
                }
            }
        }
        return builder
    }

    /**
     * Asserts that slices depend only on the specified allowed slices.
     */
    fun onlyDependOnSlices(vararg allowedSliceKeys: String): SlicesRuleBuilder {
        val allowed = allowedSliceKeys.toSet()
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                for (to in targets.sorted()) {
                    if (to !in allowed) {
                        violations.add(getMessage("slice.should.onlyDependOnSlices", from, to, allowed.joinToString()))
                    }
                }
            }
        }
        return builder
    }

    /**
     * Asserts that slices do not depend on the specified forbidden slice.
     */
    infix fun notDependOnSlice(forbiddenSliceKey: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                if (forbiddenSliceKey in targets) {
                    violations.add(getMessage("slice.should.notDependOnSlice", from, forbiddenSliceKey))
                }
            }
        }
        return builder
    }

    /**
     * Asserts that slices do not depend on any of the specified forbidden slices.
     */
    infix fun notDependOnSlices(forbiddenSliceKeys: List<String>): SlicesRuleBuilder {
        forbiddenSliceKeys.forEach { notDependOnSlice(it) }
        return builder
    }

    /**
     * Asserts that slices do not depend on any of the specified forbidden slices.
     */
    fun notDependOnSlices(vararg forbiddenSliceKeys: String): SlicesRuleBuilder =
        notDependOnSlices(forbiddenSliceKeys.toList())

    /**
     * Asserts that slices depend on the specified required slice.
     */
    infix fun dependOnSlice(requiredSliceKey: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for ((from, targets) in sliceGraph.adjacency.toSortedMap()) {
                if (from != requiredSliceKey && requiredSliceKey !in targets) {
                    violations.add(getMessage("slice.should.dependOnSlice", from, requiredSliceKey))
                }
            }
        }
        return builder
    }

    /**
     * Asserts that slices depend on all specified required slices.
     */
    fun dependOnSlices(vararg requiredSliceKeys: String): SlicesRuleBuilder {
        requiredSliceKeys.forEach { dependOnSlice(it) }
        return builder
    }

    fun containClasses(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isEmpty()) {
                    violations.add("Slice '${slice.key}' should contain classes")
                }
            }
        }
        return builder
    }

    fun notContainClasses(): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                if (slice.classes.isNotEmpty()) {
                    violations.add("Slice '${slice.key}' should not contain classes")
                }
            }
        }
        return builder
    }

    infix fun containClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            for (slice in sliceGraph.slices) {
                val matches = slice.classes.any { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
                if (!matches) {
                    violations.add("Slice '${slice.key}' should contain classes in package '$packagePattern'")
                }
            }
        }
        return builder
    }

    /**
     * Custom assertion block executed against the derived [SliceGraph].
     */
    internal fun satisfy(assertion: (io.github.baole.konture.impl.SliceGraph, MutableList<String>) -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion(assertion)
        return builder
    }

    /**
     * Asserts that the derived slices satisfy a custom Boolean predicate.
     */
    internal fun satisfy(description: String, predicate: (io.github.baole.konture.impl.SliceGraph) -> Boolean): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            if (!predicate(sliceGraph)) {
                violations.add("Slice graph does not satisfy custom condition: $description")
            }
        }
        return builder
    }

    fun anyOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            val anyPassed = blocks.any { block ->
                val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                SlicesShould(tempBuilder).apply(block)
                val tempViolations = mutableListOf<String>()
                tempBuilder.checkRuleAssertions(sliceGraph, tempViolations)
                tempViolations.isEmpty()
            }
            if (!anyPassed) {
                violations.add("Slice graph does not satisfy any of the specified conditions")
            }
        }
        return builder
    }

    fun allOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            blocks.forEach { block ->
                val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                SlicesShould(tempBuilder).apply(block)
                tempBuilder.checkRuleAssertions(sliceGraph, violations)
            }
        }
        return builder
    }

    fun noneOf(vararg blocks: SlicesShould.() -> Unit): SlicesRuleBuilder {
        builder.addShouldAssertion { sliceGraph, violations ->
            val anyPassed = blocks.any { block ->
                val tempBuilder = SlicesRuleBuilder(builder.graph).allowEmpty()
                SlicesShould(tempBuilder).apply(block)
                val tempViolations = mutableListOf<String>()
                tempBuilder.checkRuleAssertions(sliceGraph, tempViolations)
                tempViolations.isEmpty()
            }
            if (anyPassed) {
                violations.add("Slice graph satisfies one of the forbidden conditions")
            }
        }
        return builder
    }
}


