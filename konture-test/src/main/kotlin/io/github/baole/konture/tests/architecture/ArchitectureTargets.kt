/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.architecture

import java.io.Serializable

annotation class ArchMarker

@ArchMarker
class ArchClass : Serializable {
    @ArchMarker
    val archProperty: String = "arch"

    @ArchMarker
    fun archFunction() {
        println(archProperty)
    }
}
