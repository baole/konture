/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.cache

import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.impl.psi.TypeAliasDefinition
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Thread-safe cache storing AST parse snapshots and symbol metadata keyed by file SHA-256 content hashes.
 */
internal object IncrementalAstCache {
    private val classFqNamesCache = ConcurrentHashMap<String, Set<String>>()
    private val typeAliasesCache = ConcurrentHashMap<String, Map<String, TypeAliasDefinition>>()
    private val fileDeclarationCache = ConcurrentHashMap<String, FileDeclaration>()

    private val classScanHitCount = AtomicLong(0)
    private val classScanMissCount = AtomicLong(0)
    private val typeAliasScanHitCount = AtomicLong(0)
    private val typeAliasScanMissCount = AtomicLong(0)
    private val parseHitCount = AtomicLong(0)
    private val parseMissCount = AtomicLong(0)

    val classScanHits: Long get() = classScanHitCount.get()
    val classScanMisses: Long get() = classScanMissCount.get()
    val typeAliasScanHits: Long get() = typeAliasScanHitCount.get()
    val typeAliasScanMisses: Long get() = typeAliasScanMissCount.get()
    val parseHits: Long get() = parseHitCount.get()
    val parseMisses: Long get() = parseMissCount.get()

    val totalHits: Long get() = classScanHits + typeAliasScanHits + parseHits
    val totalMisses: Long get() = classScanMisses + typeAliasScanMisses + parseMisses

    fun getClassFqNames(hash: String): Set<String>? {
        val result = classFqNamesCache[hash]
        if (result != null) {
            classScanHitCount.incrementAndGet()
            return result
        }
        classScanMissCount.incrementAndGet()
        val fromDisk = PersistentAstCacheStore.readClassFqNames(hash)
        if (fromDisk != null) {
            classFqNamesCache[hash] = fromDisk
        }
        return fromDisk
    }

    fun putClassFqNames(
        hash: String,
        names: Set<String>,
    ) {
        classFqNamesCache[hash] = names
        PersistentAstCacheStore.writeClassFqNames(hash, names)
    }

    fun getTypeAliases(hash: String): Map<String, TypeAliasDefinition>? {
        val result = typeAliasesCache[hash]
        if (result != null) {
            typeAliasScanHitCount.incrementAndGet()
            return result
        }
        typeAliasScanMissCount.incrementAndGet()
        val fromDisk = PersistentAstCacheStore.readTypeAliases(hash)
        if (fromDisk != null) {
            typeAliasesCache[hash] = fromDisk
        }
        return fromDisk
    }

    fun putTypeAliases(
        hash: String,
        aliases: Map<String, TypeAliasDefinition>,
    ) {
        typeAliasesCache[hash] = aliases
        PersistentAstCacheStore.writeTypeAliases(hash, aliases)
    }

    fun getFileDeclaration(key: String): FileDeclaration? {
        val result = fileDeclarationCache[key]
        if (result != null) {
            parseHitCount.incrementAndGet()
            return result
        }
        parseMissCount.incrementAndGet()
        val fromDisk = PersistentAstCacheStore.readFileDeclaration(key)
        if (fromDisk != null) {
            fileDeclarationCache[key] = fromDisk
        }
        return fromDisk
    }

    fun putFileDeclaration(
        key: String,
        declaration: FileDeclaration,
    ) {
        fileDeclarationCache[key] = declaration
        PersistentAstCacheStore.writeFileDeclaration(key, declaration)
    }

    fun clear() {
        classFqNamesCache.clear()
        typeAliasesCache.clear()
        fileDeclarationCache.clear()
        resetMetrics()
        PersistentAstCacheStore.resetMetrics()
    }

    fun resetMetrics() {
        classScanHitCount.set(0)
        classScanMissCount.set(0)
        typeAliasScanHitCount.set(0)
        typeAliasScanMissCount.set(0)
        parseHitCount.set(0)
        parseMissCount.set(0)
        PersistentAstCacheStore.resetMetrics()
    }

    /** Number of entries successfully loaded from the persistent disk cache. */
    val diskHits: Long get() = PersistentAstCacheStore.diskHits

    /** Number of disk cache lookups that missed. */
    val diskMisses: Long get() = PersistentAstCacheStore.diskMisses

    /** Number of entries successfully written to the persistent disk cache. */
    val diskWrites: Long get() = PersistentAstCacheStore.diskWrites

    val size: Int get() = classFqNamesCache.size + typeAliasesCache.size + fileDeclarationCache.size
}
