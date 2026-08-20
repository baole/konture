/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.suppression

class ProgrammaticTargetClass {
    val failingProperty: String = "NotConst"

    fun failingFunction() {
    }
}

class UnsuppressedTargetClass {
    fun unsuppressedFailingFunc() {
    }
}
