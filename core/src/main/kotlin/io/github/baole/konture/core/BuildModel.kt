/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import kotlinx.serialization.Serializable

/**
 * Represents a single Gradle build (either the root build or an included/composite build).
 *
 * @property id The unique identifier of the build (e.g., ":" for the root build, or the inclusion name for
 * composite builds).
 * @property modules The list of modules/projects contained within this build.
 */
@Serializable
data class BuildModel(
    val id: String,
    val modules: List<ModuleModel>,
)
