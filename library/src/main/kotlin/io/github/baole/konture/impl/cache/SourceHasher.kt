/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.cache

import java.io.File
import java.security.MessageDigest

/**
 * Utility for computing cryptographic SHA-256 content hashes for Kotlin source files.
 */
internal object SourceHasher {
    private const val HASH_ALGORITHM = "SHA-256"

    /**
     * Computes the SHA-256 hash of the given [content] string encoded in UTF-8.
     *
     * @param content The string content to hash.
     * @return The lowercase hex representation of the SHA-256 digest.
     */
    fun hashString(content: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(content.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Computes the SHA-256 hash of the given [file] content.
     *
     * @param file The file to hash.
     * @return The lowercase hex representation of the SHA-256 digest, or null if the file does not exist.
     */
    fun hashFile(file: File): String? {
        if (!file.exists() || !file.isFile) return null
        return hashString(file.readText())
    }
}
