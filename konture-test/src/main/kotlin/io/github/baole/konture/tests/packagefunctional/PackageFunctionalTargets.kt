/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.packagefunctional

import java.io.Serializable

annotation class PackageFunctionalMarker

@PackageFunctionalMarker
class PackageFunctionalClass : Serializable {
    @PackageFunctionalMarker
    val packageProp: String = "packageProp"

    @PackageFunctionalMarker
    fun packageFunc() {
        println(packageProp)
    }
}
