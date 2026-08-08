/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.autochecking

import java.io.Serializable

annotation class AutoCheckingMarker

@AutoCheckingMarker
class AutoCheckingClass : Serializable {
    @AutoCheckingMarker
    val autoCheckingProperty: String = "autoCheck"

    @AutoCheckingMarker
    fun autoCheckingFunction() {
        println(autoCheckingProperty)
    }
}
