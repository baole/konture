/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Top-level builder function for defining a reusable, named [RuleSet].
 *
 * @param name Optional identifier or name for the rule set.
 * @param block DSL configuration block scoped to [RuleSetBuilder].
 * @return Reusable [RuleSet].
 */
public fun architectureRules(
    name: String? = null,
    block: RuleSetBuilder.() -> Unit,
): RuleSet {
    val builder = RuleSetBuilder(name).apply(block)
    return RuleSet(
        name = builder.name,
        description = builder.description,
        actions = builder.actions.toList(),
    )
}

/**
 * Top-level builder function for defining a reusable [RuleSet].
 *
 * @param block DSL configuration block scoped to [RuleSetBuilder].
 * @return Reusable [RuleSet].
 */
public fun architectureRules(block: RuleSetBuilder.() -> Unit): RuleSet = architectureRules(name = null, block = block)

/**
 * Builder function on [Konture] for defining a reusable, named [RuleSet].
 *
 * @param name Optional identifier or name for the rule set.
 * @param block DSL configuration block scoped to [RuleSetBuilder].
 * @return Reusable [RuleSet].
 */
public fun Konture.architectureRules(
    name: String? = null,
    block: RuleSetBuilder.() -> Unit,
): RuleSet = io.github.baole.konture.architectureRules(name, block)

/**
 * Builder function on [Konture] for defining a reusable [RuleSet].
 *
 * @param block DSL configuration block scoped to [RuleSetBuilder].
 * @return Reusable [RuleSet].
 */
public fun Konture.architectureRules(block: RuleSetBuilder.() -> Unit): RuleSet =
    io.github.baole.konture.architectureRules(name = null, block = block)
