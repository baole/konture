/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.model

import kotlinx.serialization.Serializable

/**
 * Categorization of the origin/mechanism used to suppress an architectural violation.
 */
@Serializable
public enum class SuppressionKind {
    /** Suppressed via an in-source code annotation (e.g. `@Suppress("konture:<ruleId>")` or `@SuppressWarnings`). */
    IN_SOURCE,

    /** Suppressed via programmatic rule configuration (`suppress { ... }`). */
    PROGRAMMATIC,

    /** Suppressed via an architecture baseline file. */
    BASELINE,
}

/**
 * Structured metadata describing why and where an architectural violation was suppressed.
 *
 * @property kind The mechanism used to suppress the violation.
 * @property reason Human-readable justification or explanation for the suppression.
 * @property location Optional source location associated with the suppression annotation or definition.
 */
@Serializable
public data class SuppressionMetadata(
    public val kind: SuppressionKind,
    public val reason: String? = null,
    public val location: SourceLocation? = null,
)
