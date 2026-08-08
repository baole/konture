/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.composite

annotation class CompMarkerA

annotation class CompMarkerB

@CompMarkerA
class CompositeClassA {
    @CompMarkerA
    val compPropA: String = "a"

    @CompMarkerA
    fun compFuncA() {}
}

@CompMarkerB
class CompositeClassB {
    @CompMarkerB
    val compPropB: String = "b"

    @CompMarkerB
    fun compFuncB() {}
}
