/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.cache

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SourceHasherTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `hashString produces expected deterministic SHA-256 hex digest`() {
        val input = "package com.example\n\nclass Sample\n"
        val hash1 = SourceHasher.hashString(input)
        val hash2 = SourceHasher.hashString(input)

        assertEquals(64, hash1.length)
        assertEquals(hash1, hash2)
    }

    @Test
    fun `different strings produce different hashes`() {
        val hash1 = SourceHasher.hashString("class A")
        val hash2 = SourceHasher.hashString("class B")

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `hashFile returns null for missing file or directory`() {
        val missing = File(tempDir, "non_existent.kt")
        assertNull(SourceHasher.hashFile(missing))

        val dir = File(tempDir, "subDir").apply { mkdirs() }
        assertNull(SourceHasher.hashFile(dir))
    }

    @Test
    fun `hashFile matches hashString for file content`() {
        val file = File(tempDir, "Sample.kt")
        val content = "package com.example\n\ninterface Greeter { fun greet(): String }\n"
        file.writeText(content)

        val fileHash = SourceHasher.hashFile(file)
        val stringHash = SourceHasher.hashString(content)

        assertNotNull(fileHash)
        assertEquals(stringHash, fileHash)
    }
}
