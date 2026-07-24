/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViolationLocationTest {
    @Test
    fun `formats module, source set and file with a line number`() {
        assertEquals(
            ":app, main source set, src/Foo.kt:12",
            ViolationLocation.of(":app", "main", "src/Foo.kt", 12),
        )
    }

    @Test
    fun `omits the line number when it is unknown`() {
        assertEquals(
            ":app, main source set, src/Foo.kt",
            ViolationLocation.of(":app", "main", "src/Foo.kt"),
        )
    }

    @Test
    fun `falls back to unknown when the source set is null`() {
        assertEquals(
            ":app, unknown source set, src/Foo.kt",
            ViolationLocation.of(":app", null, "src/Foo.kt"),
        )
    }
}
