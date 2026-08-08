/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.debugall

annotation class DebugAllMarker

@DebugAllMarker
class DebugAllClass {
    @DebugAllMarker
    val debugAllProp: String = "debugAll"

    @DebugAllMarker
    fun debugAllFunc() {}
}
