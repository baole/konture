/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.prohibitions

class CalleeClass {
    fun calleeFunc() {}
}

class CleanClass {
    val cleanProp: String = "clean"
    fun cleanFunc() {}
}
