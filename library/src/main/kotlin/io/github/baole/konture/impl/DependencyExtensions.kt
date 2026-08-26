/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Dependency

/**
 * Returns true if the [Dependency.configuration] name represents a test-only Gradle configuration
 * (e.g. `testImplementation`, `testRuntimeOnly`, `androidTestImplementation`).
 *
 * Detection uses word-boundary rules so that names like `testedapks` are not misidentified:
 * the token "test" must start at the beginning of the string or follow a non-alphanumeric character,
 * and must end at the end of the string or be followed by an uppercase letter or a non-alphanumeric
 * character.
 */
internal fun Dependency.isTestConfiguration(): Boolean {
    val name = configuration
    var start = 0
    while (true) {
        val index = name.indexOf("test", start, ignoreCase = true)
        if (index == -1) break
        val end = index + 4
        val leftOk = index == 0 || name[index].isUpperCase() || !name[index - 1].isLetterOrDigit()
        val rightOk = end == name.length || name[end].isUpperCase() || !name[end].isLetterOrDigit()
        if (leftOk && rightOk) return true
        start = index + 1
    }
    return false
}
