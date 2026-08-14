/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.model

import kotlinx.serialization.Serializable

/**
 * Represents an individual architectural rule violation.
 *
 * @property ruleId The unique identifier of the rule being violated.
 * @property subject The subject (e.g. class, function, module) committing the violation.
 * @property target Optional target subject involved in the violation (e.g., dependency target).
 * @property sourceLocation Optional source location for the violation. Defaults to subject location.
 * @property dependencyPath Sequence of subjects representing the dependency path leading to the violation.
 * @property message Descriptive violation explanation message.
 * @property severity Severity level of the violation (defaults to [Severity.ERROR]).
 * @property metadata Optional structured rule metadata associated with this violation.
 */
@Serializable
public data class Violation(
    val ruleId: String,
    val subject: Subject,
    val target: Subject? = null,
    val sourceLocation: SourceLocation? = subject.location,
    val dependencyPath: List<Subject> = emptyList(),
    val message: String,
    val severity: Severity = Severity.ERROR,
    val metadata: RuleMetadata? = null,
)
