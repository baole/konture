/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.modulefunctional

import java.io.Serializable

annotation class ModuleFunctionalMarker

@ModuleFunctionalMarker
class ModuleFunctionalClass : Serializable {
    @ModuleFunctionalMarker
    val moduleProp: String = "moduleProp"

    @ModuleFunctionalMarker
    fun moduleFunc() {
        println(moduleProp)
    }
}
