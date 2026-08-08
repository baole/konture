/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.notCall

object BannedApi {
    fun legacyLog(message: String) {
        // Legacy logging mechanism
    }
}

abstract class ViewModel

class BannedViewModel : ViewModel() {
    fun process() {
        BannedApi.legacyLog("direct log")
    }
}

class CleanViewModel : ViewModel() {
    fun processClean() {
        // Clean logic
    }
}
