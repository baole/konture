/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

data class DependencyData(
    val configuration: String,
    val targetBuildId: String,
    val targetPath: String,
) : Serializable
