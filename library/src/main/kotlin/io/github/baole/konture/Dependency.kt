/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Represents a declared project-to-project dependency edge at runtime.
 *
 * @property configuration The Gradle configuration where the dependency was declared (e.g., `api`, `implementation`).
 * @property targetBuildId The target build ID of the dependent project.
 * @property targetPath The Gradle project path of the dependent project (e.g., `:domain`).
 */
data class Dependency(
    val configuration: String,
    val targetBuildId: String,
    val targetPath: String,
)
