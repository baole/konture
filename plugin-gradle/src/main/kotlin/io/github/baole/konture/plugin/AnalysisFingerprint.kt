/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import java.security.MessageDigest

/**
 * Computes a stable short SHA-256 fingerprint over the effective Konture analysis
 * configuration. The fingerprint namespaces the persistent analysis cache so that
 * rule definition / configuration changes automatically invalidate previously
 * stored cache entries.
 */
internal object AnalysisFingerprint {
    private const val HASH_ALGORITHM = "SHA-256"
    private const val FINGERPRINT_LENGTH = 16
    private const val BYTE_MASK = 0xFF
    private const val NIBBLE_SHIFT = 4
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    fun compute(vararg parts: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(parts.joinToString("\n").toByteArray(Charsets.UTF_8))
        return bytesToHex(hashBytes).take(FINGERPRINT_LENGTH)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and BYTE_MASK
            hexChars[i * 2] = HEX_CHARS[v ushr NIBBLE_SHIFT]
            hexChars[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(hexChars)
    }
}
