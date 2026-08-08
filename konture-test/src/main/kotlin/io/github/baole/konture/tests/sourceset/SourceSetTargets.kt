/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.sourceset

annotation class ProductionOnlyMarker

@ProductionOnlyMarker
class ProductionTarget {
    @ProductionOnlyMarker
    val prodProperty: String = "prod"

    @ProductionOnlyMarker
    fun prodFunction() {}
}
