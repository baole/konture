/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
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
}
