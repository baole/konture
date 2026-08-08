/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.sourceset

annotation class TestOnlyMarker

@TestOnlyMarker
class TestTarget {
    @TestOnlyMarker
    val testProperty: String = "test"

    @TestOnlyMarker
    fun testFunction() {}
}
