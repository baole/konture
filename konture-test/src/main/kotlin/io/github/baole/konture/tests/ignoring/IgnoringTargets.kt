/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.ignoring

class IgnoringClassA {
    val failingProp: String = "fail"
    fun failingFunc() {}
}

class IgnoringClassB {
    val okProp: String = "ok"
    fun okFunc() {}
}
