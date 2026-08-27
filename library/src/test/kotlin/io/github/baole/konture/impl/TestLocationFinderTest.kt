/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class TestLocationFinderTest {
    @Test
    fun `finds test location from current execution stack`() {
        val location = TestLocationFinder.findTestLocation()
        assertNotNull(location)
        assertEquals(TestLocationFinderTest::class.java.name, location?.className)
        assertEquals("finds test location from current execution stack", location?.methodName)
    }
}
