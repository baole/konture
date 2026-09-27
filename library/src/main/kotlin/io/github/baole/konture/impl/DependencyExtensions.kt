/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Dependency

private val NON_PRODUCTION_SUBSTRINGS = listOf("baselineprofile", "testedapks", "swiftpm")

/**
 * Returns true if the Gradle configuration name represents a test-only or non-production
 * configuration (e.g. `testImplementation`, `androidTestImplementation`, `baselineProfile`,
 * `testedApks`, `benchmarkImplementation`).
 *
 * Detection uses word-boundary rules so that names like `userProfileImplementation` or
 * `customerMetadataApi` are not misidentified as test/non-production configurations.
 */
internal fun isTestConfiguration(configuration: String): Boolean {
    val lower = configuration.lowercase()
    if (isSpecificNonProductionConfiguration(lower)) {
        return true
    }
    return hasWordBoundaryToken(configuration, "test") || hasWordBoundaryToken(configuration, "benchmark")
}

private fun isSpecificNonProductionConfiguration(lower: String): Boolean {
    if (NON_PRODUCTION_SUBSTRINGS.any { lower.contains(it) }) return true
    return lower.startsWith("metadata") || lower.endsWith("metadataclasspathdependencies")
}

private fun hasWordBoundaryToken(
    name: String,
    token: String,
): Boolean {
    var start = 0
    while (true) {
        val index = name.indexOf(token, start, ignoreCase = true)
        if (index == -1) break
        val end = index + token.length
        val leftOk = index == 0 || name[index].isUpperCase() || !name[index - 1].isLetterOrDigit()
        val rightOk = end == name.length || name[end].isUpperCase() || !name[end].isLetterOrDigit()
        if (leftOk && rightOk) return true
        start = index + 1
    }
    return false
}

internal fun Dependency.isTestConfiguration(): Boolean = isTestConfiguration(configuration)
