/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.KontureRuntimeStateProvider

/**
 * Encapsulates a named architecture rule with its metadata and evaluation logic.
 *
 * @property metadata Structured metadata attached to this rule.
 * @property executionSuites List of rule check lambdas configured inside the rule block.
 */
public class RuleDefinition internal constructor(
    public val metadata: RuleMetadata,
    private val executionSuites: List<() -> Unit>,
    internal val programmaticSuppressions: List<ProgrammaticSuppression> = emptyList(),
) {
    /**
     * Executes all assertions configured for this rule, propagating rule metadata
     * to all generated violations.
     *
     * @throws AssertionError if any architectural violations occur.
     */
    public fun check() {
        val currentState = KontureRuntimeStateProvider.currentState
        val newState =
            currentState.copy(
                currentRuleMetadata = metadata,
                activeProgrammaticSuppressions = programmaticSuppressions,
            )

        KontureRuntimeStateProvider.runWithState(newState) {
            val failures = mutableListOf<AssertionError>()
            for (suite in executionSuites) {
                try {
                    suite()
                } catch (e: AssertionError) {
                    failures.add(e)
                }
            }
            if (failures.size == 1) {
                throw failures.first()
            } else if (failures.size > 1) {
                throw AssertionError(
                    "Architecture rule '${metadata.id}' failed (${failures.size} violation suites):\n\n" +
                        failures.joinToString("\n\n") { it.message ?: "Architectural violation" },
                )
            }
        }
    }

    /**
     * Alias for [check].
     */
    public fun verify() {
        check()
    }
}
