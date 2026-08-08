/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.setalgebra

annotation class SetAlgebraMarkerA

annotation class SetAlgebraMarkerB

@SetAlgebraMarkerA
class SetAlgebraClassA {
    @SetAlgebraMarkerA
    val propA: String = "A"

    @SetAlgebraMarkerA
    fun funcA() {}
}

@SetAlgebraMarkerB
class SetAlgebraClassB {
    @SetAlgebraMarkerB
    val propB: String = "B"

    @SetAlgebraMarkerB
    fun funcB() {}
}
