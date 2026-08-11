/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import kotlinx.serialization.Serializable

@Serializable
public data class ExclusionsModel(
    val excludeModules: List<String> = emptyList(),
    val excludePackages: List<String> = emptyList(),
    val excludeClasses: List<String> = emptyList(),
    val excludeConfigurations: List<String> = listOf("test", "benchmark", "profile", "testedapks"),
)
