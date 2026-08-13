/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.dependencyassertions

class DependencyClassA {
    fun funcA() {}
    val sampleDependencyProperty: String = "test"

    fun sampleDependencyFunction(): String {
        return sampleDependencyProperty
    }
}

class DependencyClassB {
    fun funcB() {}
}


