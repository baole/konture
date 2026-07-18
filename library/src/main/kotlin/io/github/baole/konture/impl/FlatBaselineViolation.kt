/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

internal data class FlatBaselineViolation(
    val testClass: String,
    val testMethod: String,
    val location: String?,
    val message: String,
)
