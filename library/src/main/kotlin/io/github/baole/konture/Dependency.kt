/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
public data class Dependency(
    /** Filter or assertion criteria for configuration. */
    public val configuration: String,
    /** Filter or assertion criteria for target build id. */
    public val targetBuildId: String,
    /** Filter or assertion criteria for target path. */
    public val targetPath: String,
)
