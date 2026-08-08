/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.logicalchaining

annotation class LogicalMarkerA

annotation class LogicalMarkerB

@LogicalMarkerA
class LogicalClassA {
    @LogicalMarkerA
    val propA: String = "A"

    @LogicalMarkerA
    fun funcA() {}
}

@LogicalMarkerB
class LogicalClassB {
    @LogicalMarkerB
    val propB: String = "B"

    @LogicalMarkerB
    fun funcB() {}
}
