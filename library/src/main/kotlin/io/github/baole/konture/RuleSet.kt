/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Encapsulates a reusable collection of architectural rules and policies.
 *
 * Rule sets can be defined using [architectureRules], composed using [plus] or [apply],
 * executed standalone via [check] or [verify], or imported into an [architecture] block.
 *
 * @property name Optional human-readable name or identifier for this rule set.
 * @property description Optional human-readable rationale or description of the rule set.
 */
public class RuleSet internal constructor(
    public val name: String? = null,
    public val description: String? = null,
    internal val actions: List<(KontureContext) -> Unit>,
) {
    /**
     * Executes all rules and assertions in this rule set against the default [ProjectGraph].
     *
     * @throws AssertionError if any architectural violations occur.
     */
    public fun check() {
        Konture.architecture {
            apply(this@RuleSet)
        }
    }

    /**
     * Alias for [check].
     */
    public fun verify() {
        check()
    }

    /**
     * Combines this rule set with [other] into a new composite [RuleSet].
     *
     * @param other Another [RuleSet] to combine with this one.
     * @return A new [RuleSet] containing rules from both sets.
     */
    public operator fun plus(other: RuleSet): RuleSet {
        val combinedName =
            listOfNotNull(name, other.name)
                .joinToString(" + ")
                .ifEmpty { null }
        val combinedDesc =
            listOfNotNull(description, other.description)
                .joinToString("; ")
                .ifEmpty { null }
        return RuleSet(
            name = combinedName,
            description = combinedDesc,
            actions = this.actions + other.actions,
        )
    }

    internal fun applyTo(context: KontureContext) {
        actions.forEach { action -> action(context) }
    }
}
