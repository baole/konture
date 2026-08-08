/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.modifiers

data class PublicDataClass(val id: Int) {
    fun publicFunc() {}
    val publicVal: String = "val"
    var publicVar: String = "var"
}

internal sealed class InternalSealedClass {
    internal suspend fun internalSuspendFunc() {}
    internal val internalVal: String = "internal"
}
