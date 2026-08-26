/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import kotlinx.serialization.Serializable

/**
 * Model specifying exclusion rules for modules, packages, classes, and configurations when analyzing builds.
 *
 * @property excludeModules List of module path patterns to exclude from analysis.
 * @property excludePackages List of package name patterns to exclude from analysis.
 * @property excludeClasses List of class FQCN patterns to exclude from analysis.
 * @property excludeConfigurations List of dependency configuration names to exclude from dependency graph collection.
 */
@Serializable
public data class ExclusionsModel(
    val excludeModules: List<String> = emptyList(),
    val excludePackages: List<String> = emptyList(),
    val excludeClasses: List<String> = emptyList(),
    // Patterns use simple glob syntax (* = any chars). "test*" matches "testImplementation",
    // "testRuntimeOnly", etc. The old default "test" used a substring-contains fallback
    // that has been removed; explicit wildcards make the intent unambiguous.
    val excludeConfigurations: List<String> = listOf("test*", "benchmark*", "profile", "testedapks"),
)
