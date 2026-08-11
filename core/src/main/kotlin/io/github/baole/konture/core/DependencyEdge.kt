/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import kotlinx.serialization.Serializable

/**
 * Represents a declared project-to-project dependency edge.
 *
 * @property configuration The Gradle configuration where the dependency is declared
 * (e.g., `api`, `implementation`, `testImplementation`).
 * @property targetBuildId The target Gradle build ID (supports cross-build composite dependencies).
 * @property targetPath The Gradle path of the target project dependency (e.g., `:domain`).
 */
@Serializable
public data class DependencyEdge(
    val configuration: String,
    val targetBuildId: String,
    val targetPath: String,
)
