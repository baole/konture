/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

/**
 * Gradle task input data model representing an outgoing project dependency.
 *
 * @property configuration The Gradle dependency configuration name (e.g. `implementation`, `api`).
 * @property targetBuildId Unique identifier of the target build.
 * @property targetPath Path of the target module being depended on.
 */
data class DependencyData(
    val configuration: String,
    val targetBuildId: String,
    val targetPath: String,
) : Serializable
