/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.annotationfiltering

annotation class AnnMarkerA

annotation class AnnMarkerB

@AnnMarkerA
@AnnMarkerB
class AnnotatedClass {
    @AnnMarkerA
    @AnnMarkerB
    val annotatedProp: String = "ann"

    @AnnMarkerA
    @AnnMarkerB
    fun annotatedFunc() {}
}
