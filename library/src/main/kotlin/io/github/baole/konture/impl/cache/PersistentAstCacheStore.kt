/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.cache

import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.Konture
import io.github.baole.konture.impl.psi.TypeAliasDefinition
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Disk-backed persistence tier for [IncrementalAstCache].
 *
 * When persistent caching is enabled, parsed AST snapshots are serialized under
 * `<cacheDir>/<fingerprint>/ast/v<schema>/` and consulted on cache misses so that
 * unchanged Kotlin files skip re-parsing across JVM runs (e.g. repeated Gradle
 * test executions).
 *
 * Invalidation model:
 * - Source changes invalidate entries automatically because cache keys embed the
 *   SHA-256 content hash of the analyzed file.
 * - Rule definition / analysis configuration changes invalidate the whole cache by
 *   switching the fingerprint namespace. The Gradle plugin derives the fingerprint
 *   from the effective `konture { analysis { } }` configuration.
 * - A manifest records the serialization schema; entries written by an incompatible
 *   future schema are wiped instead of being mis-decoded.
 *
 * Cache corruption is never fatal: unreadable or undecodable entries are treated as
 * misses and re-parsed, and write failures are swallowed so caching can never break
 * an analysis run.
 */
internal object PersistentAstCacheStore {
    private const val SCHEMA_VERSION = 1
    private const val FORMAT_NAME = "konture-ast-cache"
    private const val MANIFEST_FILE = "manifest.json"
    private const val CLASS_FQ_NAMES_DIR = "class-fq-names"
    private const val TYPE_ALIASES_DIR = "type-aliases"
    private const val FILE_DECLARATIONS_DIR = "file-declarations"
    private const val DEFAULT_NAMESPACE = "default"
    private const val SANITIZED_NAMESPACE_FALLBACK = "custom"
    private const val MAX_NAMESPACE_LENGTH = 64

    private val json = Json { ignoreUnknownKeys = true }

    private val writeLock = Any()
    private val initializedNamespaces = ConcurrentHashMap.newKeySet<String>()

    private val diskHitCount = AtomicLong(0)
    private val diskMissCount = AtomicLong(0)
    private val diskWriteCount = AtomicLong(0)

    /** Number of disk cache entries that were successfully read from disk. */
    val diskHits: Long get() = diskHitCount.get()

    /** Number of disk cache lookups that missed (missing, unreadable, or undecodable entry). */
    val diskMisses: Long get() = diskMissCount.get()

    /** Number of entries successfully written to disk. */
    val diskWrites: Long get() = diskWriteCount.get()

    fun resetMetrics() {
        diskHitCount.set(0)
        diskMissCount.set(0)
        diskWriteCount.set(0)
    }

    fun readClassFqNames(hash: String): Set<String>? = readEntry(namespaceDir(), CLASS_FQ_NAMES_DIR, hash)

    fun writeClassFqNames(
        hash: String,
        names: Set<String>,
    ) {
        writeEntry(namespaceDir(), CLASS_FQ_NAMES_DIR, hash, names)
    }

    fun readTypeAliases(hash: String): Map<String, TypeAliasDefinition>? {
        return readEntry(namespaceDir(), TYPE_ALIASES_DIR, hash)
    }

    fun writeTypeAliases(
        hash: String,
        aliases: Map<String, TypeAliasDefinition>,
    ) {
        writeEntry(namespaceDir(), TYPE_ALIASES_DIR, hash, aliases)
    }

    fun readFileDeclaration(cacheKey: String): FileDeclaration? {
        return readEntry(namespaceDir(), FILE_DECLARATIONS_DIR, cacheKey)
    }

    fun writeFileDeclaration(
        cacheKey: String,
        declaration: FileDeclaration,
    ) {
        writeEntry(namespaceDir(), FILE_DECLARATIONS_DIR, cacheKey, declaration)
    }

    /**
     * Returns the active namespace directory, or null when persistent caching is disabled.
     * The namespace is derived from the configured cache directory and fingerprint so that
     * rule/config changes automatically select a fresh cache partition.
     */
    private fun namespaceDir(): File? {
        if (!Konture.cacheEnabled) return null
        val base = Konture.cacheDir
        val fingerprint = Konture.cacheFingerprint
        val namespace = fingerprintNamespace(fingerprint)
        return File(File(base, namespace), "ast/v$SCHEMA_VERSION")
    }

    private fun fingerprintNamespace(fingerprint: String): String {
        if (fingerprint.isBlank()) return DEFAULT_NAMESPACE
        val sanitized = fingerprint.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        return sanitized.take(MAX_NAMESPACE_LENGTH)
            .ifEmpty { SANITIZED_NAMESPACE_FALLBACK }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private inline fun <reified T> readEntry(
        dir: File?,
        subDir: String,
        key: String,
    ): T? {
        if (dir == null) return null
        val file = entryFile(dir, subDir, key)
        if (!file.isFile) {
            diskMissCount.incrementAndGet()
            return null
        }
        return try {
            val decoded = json.decodeFromString<T>(file.readText())
            diskHitCount.incrementAndGet()
            decoded
        } catch (_: Exception) {
            diskMissCount.incrementAndGet()
            null
        }
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private inline fun <reified T> writeEntry(
        dir: File?,
        subDir: String,
        key: String,
        value: T,
    ) {
        if (dir == null) return
        try {
            ensureNamespace(dir)
            val file = entryFile(dir, subDir, key)
            val text = json.encodeToString(value)
            synchronized(writeLock) {
                file.parentFile.mkdirs()
                val tmp = File(file.parentFile, "${file.name}.tmp")
                tmp.writeText(text)
                if (!tmp.renameTo(file)) {
                    file.delete()
                    tmp.renameTo(file)
                }
            }
            diskWriteCount.incrementAndGet()
        } catch (_: Exception) {
            // Cache writes must never break analysis.
        }
    }

    private fun entryFile(
        dir: File,
        subDir: String,
        key: String,
    ): File = File(File(dir, subDir), entryFileName(key))

    private fun entryFileName(key: String): String =
        if (isHexKey(key)) {
            "$key.json"
        } else {
            "${SourceHasher.hashString(key)}.json"
        }

    private fun isHexKey(key: String): Boolean = key.length == HEX_KEY_LENGTH && key.all { it in HEX_CHARS }

    private fun ensureNamespace(dir: File) {
        val dirKey = dir.absolutePath
        if (initializedNamespaces.contains(dirKey)) return
        synchronized(writeLock) {
            if (initializedNamespaces.contains(dirKey)) return
            val manifest = File(dir, MANIFEST_FILE)
            if (manifest.isFile) {
                val existing =
                    try {
                        json.decodeFromString<CacheManifest>(manifest.readText())
                    } catch (_: Exception) {
                        null
                    }
                if (existing == null || existing.schemaVersion != SCHEMA_VERSION || existing.format != FORMAT_NAME) {
                    dir.deleteRecursively()
                }
            }
            dir.mkdirs()
            manifest.writeText(json.encodeToString(cacheManifest()))
            initializedNamespaces.add(dirKey)
        }
    }

    private fun cacheManifest(): CacheManifest =
        CacheManifest(
            format = FORMAT_NAME,
            schemaVersion = SCHEMA_VERSION,
            fingerprint = Konture.cacheFingerprint,
        )

    private const val HEX_KEY_LENGTH = 64
    private const val HEX_CHARS = "0123456789abcdef"
}

@Serializable
internal data class CacheManifest(
    val format: String,
    val schemaVersion: Int,
    val fingerprint: String,
)
