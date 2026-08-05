/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViolationLocationTest {
    @Test
    fun `formats module, source set and file with a line number`() {
        assertEquals(
            ":app, main source set) (Foo.kt:12",
            ViolationLocation.of(":app", "main", "src/Foo.kt", 12),
        )
    }

    @Test
    fun `omits the line number when it is unknown`() {
        assertEquals(
            ":app, main source set) (Foo.kt",
            ViolationLocation.of(":app", "main", "src/Foo.kt"),
        )
    }

    @Test
    fun `falls back to unknown when the source set is null`() {
        assertEquals(
            ":app, unknown source set) (Foo.kt",
            ViolationLocation.of(":app", null, "src/Foo.kt"),
        )
    }

    @Test
    fun `formats with fully qualified class name for IDE console hyperlinking`() {
        assertEquals(
            ":app, main source set) (com.example.MyViewModel(MyViewModel.kt:154)",
            ViolationLocation.of(":app", "main", "src/MyViewModel.kt", 154, fqName = "com.example.MyViewModel"),
        )
    }

    @Test
    fun `formats with package name when fqName is absent`() {
        assertEquals(
            ":app, main source set) (com.example.MyViewModel(MyViewModel.kt:154)",
            ViolationLocation.of(":app", "main", "src/MyViewModel.kt", 154, packageName = "com.example"),
        )
    }
}
