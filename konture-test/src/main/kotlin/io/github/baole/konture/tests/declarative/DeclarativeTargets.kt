/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.declarative

import java.io.Serializable

annotation class DeclarativeMarker

class DeclarativeClass : Serializable {
    @DeclarativeMarker
    val declarativeProperty: String = "someProperty"

    @DeclarativeMarker
    fun declarativeFunction() {
        println(declarativeProperty)
    }
}
