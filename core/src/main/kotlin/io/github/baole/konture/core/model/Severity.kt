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
    INFO,
    WARNING,
    ERROR,
}
