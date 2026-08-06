/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BannedApiTest {
    @Test
    fun `BannedViewModel calling banned API fails architecture check`() {
        val error =
            assertThrows<AssertionError> {
                Konture.classes()
                    .that().areAssignableTo<BannedViewModel>()
                    .should().notCall("io.github.baole.konture.sample.BannedApi.legacyLog")
                    .check()
            }
        error.printStackTrace()

        assertTrue(error.message!!.contains("legacyLog"))
        assertTrue(error.message!!.contains("BannedViewModel"))
    }
}
