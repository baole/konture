/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core

import io.github.baole.konture.Modules
import io.github.baole.konture.Packages
import io.github.baole.konture.modules
import io.github.baole.konture.packages
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MatchersTest {
    @Test
    fun `modules under generates correct recursive glob patterns`() {
        assertEquals(":feature:**", modules.under(":feature"))
        assertEquals(":feature:**", modules.under("feature"))
        assertEquals(":feature:**", modules.under(":feature:"))
        assertEquals(":feature:**", modules.under(":feature:**"))
        assertEquals(":feature:checkout:**", modules.under(":feature:checkout"))
        assertEquals(":feature:checkout:**", modules.under("feature:checkout"))
        assertEquals(":**", modules.under(":"))
        assertEquals(":**", modules.under(""))
        assertEquals(":**", modules.under("   "))
    }

    @Test
    fun `modules under supports vararg and list overloads`() {
        val expected = listOf(":feature:**", ":core:**")
        assertEquals(expected, modules.under(":feature", ":core"))
        assertEquals(expected, modules.under(listOf(":feature", ":core")))
    }

    @Test
    fun `packages under generates correct recursive package patterns`() {
        assertEquals("com.acme.domain..", packages.under("com.acme.domain"))
        assertEquals("com.acme.domain..", packages.under("com.acme.domain."))
        assertEquals("com.acme.domain..", packages.under("com.acme.domain.."))
        assertEquals("com.acme..", packages.under("com.acme"))
        assertEquals("domain..", packages.under("domain"))
        assertEquals("..", packages.under(""))
        assertEquals("..", packages.under("   "))
        assertEquals("..", packages.under(".."))
    }

    @Test
    fun `packages under supports vararg and list overloads`() {
        val expected = listOf("com.acme.domain..", "com.acme.feature..")
        assertEquals(expected, packages.under("com.acme.domain", "com.acme.feature"))
        assertEquals(expected, packages.under(listOf("com.acme.domain", "com.acme.feature")))
    }

    @Test
    fun `PascalCase aliases work identically to lowercase objects`() {
        assertEquals(modules.under(":feature"), Modules.under(":feature"))
        assertEquals(packages.under("com.acme"), Packages.under("com.acme"))
    }
}
