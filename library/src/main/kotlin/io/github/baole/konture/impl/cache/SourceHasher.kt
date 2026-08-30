/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.cache

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * Utility for computing cryptographic SHA-256 content hashes for Kotlin source files.
 */
internal object SourceHasher {
    private const val HASH_ALGORITHM = "SHA-256"
    private const val BUFFER_SIZE = 8192
    private const val HEX_RADIX_MASK = 0x0F
    private const val BYTE_MASK = 0xFF
    private const val NIBBLE_SHIFT = 4
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    /**
     * Computes the SHA-256 hash of the given [content] string encoded in UTF-8.
     *
     * @param content The string content to hash.
     * @return The lowercase hex representation of the SHA-256 digest.
     */
    fun hashString(content: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(content.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashBytes)
    }

    /**
     * Computes the SHA-256 hash of the given [file] content by streaming bytes directly.
     *
     * @param file The file to hash.
     * @return The lowercase hex representation of the SHA-256 digest, or null if the file does not exist.
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun hashFile(file: File): String? {
        if (!file.exists() || !file.isFile) return null
        return try {
            val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            val buffer = ByteArray(BUFFER_SIZE)
            FileInputStream(file).use { input ->
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    digest.update(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
            }
            bytesToHex(digest.digest())
        } catch (e: Exception) {
            null
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and BYTE_MASK
            hexChars[i * 2] = HEX_CHARS[v ushr NIBBLE_SHIFT]
            hexChars[i * 2 + 1] = HEX_CHARS[v and HEX_RADIX_MASK]
        }
        return String(hexChars)
    }
}
