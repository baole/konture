/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.debugprinting

annotation class DebugMarker

@DebugMarker
class DebugClass {
    @DebugMarker
    val debugProp: String = "debug"

    @DebugMarker
    fun debugFunc() {}
}
