/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Top-level builder function for defining a named architecture rule with metadata and assertions.
 *
 * @param id Unique stable identifier for the rule (e.g. `domain.repositories.must-be-interfaces`).
 * @param block DSL configuration block scoped to [RuleBuilder].
 * @return Executable [RuleDefinition].
 */
public fun rule(
    id: String,
    block: RuleBuilder.() -> Unit,
): RuleDefinition {
    val builder = RuleBuilder(id)
    builder.apply(block)
    return RuleDefinition(
        metadata = builder.buildMetadata(),
        executionSuites = builder.executionSuites,
        programmaticSuppressions = builder.programmaticSuppressions,
    )
}
