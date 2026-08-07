/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.notCall

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.tests.utils.violationsFound
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows

class BannedApiTest {
    @Test
    fun `BannedViewModel no call banned API`() {
        Konture.classes()
            .that().areAssignableTo<BannedViewModel>()
            .should().notCall("io.github.baole.konture.tests.notCall.BannedApi.legacyLogNotExist")
            .check()
    }
    @Test
    fun `BannedViewModel calling banned API violation`() {
        val error = violationsFound {
            Konture.classes()
                .that().areAssignableTo<BannedViewModel>()
                .should().notCall("io.github.baole.konture.tests.notCall.BannedApi.legacyLog")
                .check()
        }

        assertNotNull(error, "expected a violation for calling legacyLog")
        Assertions.assertTrue(error.message!!.contains("legacyLog"))
        Assertions.assertTrue(error.message!!.contains("BannedViewModel"))
    }
}
