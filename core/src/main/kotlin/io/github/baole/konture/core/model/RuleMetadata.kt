/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.model

import kotlinx.serialization.Serializable

/**
 * Represents structured metadata associated with an architectural rule definition.
 *
 * @property id Stable identifier for the rule (e.g. `domain.repositories.must-be-interfaces`).
 * @property description Human-readable explanation of the architectural rule.
 * @property severity Severity level of violations produced by this rule (defaults to [Severity.ERROR]).
 * @property tags Arbitrary tag labels associated with this rule for filtering and taxonomy.
 */
@Serializable
public data class RuleMetadata(
    val id: String,
    val description: String? = null,
    val severity: Severity = Severity.ERROR,
    val tags: Set<String> = emptySet(),
)
