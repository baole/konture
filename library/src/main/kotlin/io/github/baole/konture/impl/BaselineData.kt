/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import kotlinx.serialization.Serializable

@Serializable
internal data class BaselineData(
    val version: Int = 1,
    val testClasses: List<TestClassConfig> = emptyList(),
)
