/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import kotlinx.serialization.Serializable

@Serializable
internal data class TestMethodConfig(
    val name: String,
    val violations: List<BaselineViolation>,
)
