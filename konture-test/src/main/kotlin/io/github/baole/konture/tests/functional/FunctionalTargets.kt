/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.functional

import java.io.Serializable

annotation class FunctionalMarker

@FunctionalMarker
class FunctionalClass : Serializable {
    @FunctionalMarker
    val functionalProperty: String = "fn"

    @FunctionalMarker
    fun functionalFunction() {
        println(functionalProperty)
    }
}
