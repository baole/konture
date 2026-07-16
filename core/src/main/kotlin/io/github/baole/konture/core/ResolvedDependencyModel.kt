/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import kotlinx.serialization.Serializable

/**
 * Represents a resolved external library coordinates edge.
 *
 * @property group Maven group name (e.g. "com.google.gson").
 * @property name Maven artifact name (e.g. "gson").
 * @property version Maven resolved version (e.g. "2.10.1").
 * @property configuration The Gradle configuration that resolved this dependency
 * (e.g. "runtimeClasspath", "compileClasspath").
 * @property isTransitive True if this is a transitive dependency (not directly declared by the module).
 */
@Serializable
data class ResolvedDependencyModel(
    val group: String,
    val name: String,
    val version: String,
    val configuration: String,
    val isTransitive: Boolean,
)
