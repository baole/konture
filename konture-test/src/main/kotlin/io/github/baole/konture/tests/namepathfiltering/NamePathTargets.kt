/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.namepathfiltering

annotation class NamePathMarker

@NamePathMarker
class TargetNameClass {
    @NamePathMarker
    val targetProp: String = "target"

    @NamePathMarker
    fun targetFunc() {}
}
