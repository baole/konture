/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.typesafe

annotation class TypeSafeMarker

@TypeSafeMarker
class TypeSafeClass {
    @TypeSafeMarker
    val typeSafeProp: String = "typeSafe"

    @TypeSafeMarker
    fun typeSafeFunc() {}
}

class TypeSafeDep
