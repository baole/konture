/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.model

import kotlinx.serialization.Serializable

/**
 * Represents the severity level of an architectural rule violation.
 */
@Serializable
public enum class Severity {
    /** Informational message indicating architectural insights or non-critical notices. */
    INFO,

    /** Warning message indicating potential architectural debt or non-fatal deviations. */
    WARNING,

    /** Error level violation indicating a strict architectural rule breach. */
    ERROR,
}
