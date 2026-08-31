/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.cache

import io.github.baole.konture.Konture
import io.github.baole.konture.impl.PsiParser
import io.github.baole.konture.impl.psi.MapSymbolLookup
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PersistentAstCacheTest {
    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        Konture.reset()
        Konture.incremental = true
        Konture.cacheEnabled = true
        Konture.cacheDir = tempDir
        Konture.cacheFingerprint = ""
        IncrementalAstCache.clear()
    }

    @AfterEach
    fun tearDown() {
        Konture.reset()
        IncrementalAstCache.clear()
    }

    @Test
    fun `parsed results are restored from disk after in-memory cache is cleared`() {
        val file =
            File(tempDir, "Service.kt").apply {
                writeText(
                    """
                    package com.example.service
                    class UserService
                    """.trimIndent(),
                )
            }

        val first = PsiParser.parseFile(file)
        assertNotNull(first)
        assertEquals("UserService", first?.classes?.first()?.name)
        assertEquals(1, IncrementalAstCache.diskWrites)

        // Simulate a fresh JVM: memory cleared, disk cache persists.
        IncrementalAstCache.clear()
        assertEquals(0, IncrementalAstCache.diskHits)

        val second = PsiParser.parseFile(file)
        assertNotNull(second)
        assertEquals("UserService", second?.classes?.first()?.name)
        assertEquals(1, IncrementalAstCache.diskHits)
        assertEquals(1, IncrementalAstCache.parseMisses)
    }

    @Test
    fun `class and type alias scans are restored from disk after in-memory cache is cleared`() {
        val file =
            File(tempDir, "Aliases.kt").apply {
                writeText(
                    """
                    package com.example.types
                    typealias UserId = String
                    class Account
                    """.trimIndent(),
                )
            }
        val files = listOf(file)

        val firstClasses = PsiParser.getDeclaredClassFqNames(files)
        val firstAliases = PsiParser.getDeclaredTypeAliases(files)
        assertEquals(setOf("com.example.types.Account"), firstClasses)
        assertEquals(setOf("com.example.types.UserId"), firstAliases.keys)

        IncrementalAstCache.clear()

        val secondClasses = PsiParser.getDeclaredClassFqNames(files)
        val secondAliases = PsiParser.getDeclaredTypeAliases(files)
        assertEquals(firstClasses, secondClasses)
        assertEquals(firstAliases.keys, secondAliases.keys)
        assertEquals(2, IncrementalAstCache.diskHits)
    }

    @Test
    fun `usages survive the disk round-trip`() {
        val file =
            File(tempDir, "UsageService.kt").apply {
                writeText(
                    """
                    package com.example.service
                    import com.example.domain.User

                    class UserService(private val user: User) {
                        fun getUser(): User = user
                    }
                    """.trimIndent(),
                )
            }
        val lookup = MapSymbolLookup(declaredClasses = setOf("com.example.domain.User"))

        val first = PsiParser.parseFile(file, lookup)
        assertNotNull(first)
        assertTrue(first?.usages?.isNotEmpty() == true, "Expected at least one resolved usage")

        IncrementalAstCache.clear()

        val second = PsiParser.parseFile(file, lookup)
        assertNotNull(second)
        assertEquals(first, second)
        assertEquals(1, IncrementalAstCache.diskHits)
    }

    @Test
    fun `changing the cache fingerprint invalidates previously persisted entries`() {
        val file =
            File(tempDir, "Fingerprinted.kt").apply {
                writeText(
                    """
                    package com.example
                    class FingerprintedV1
                    """.trimIndent(),
                )
            }

        Konture.cacheFingerprint = "rules-v1"
        val v1 = PsiParser.parseFile(file)
        assertNotNull(v1)
        assertEquals("FingerprintedV1", v1?.classes?.first()?.name)

        IncrementalAstCache.clear()

        // Rule definition changed: a different fingerprint selects a fresh namespace.
        Konture.cacheFingerprint = "rules-v2"
        val v2 = PsiParser.parseFile(file)
        assertNotNull(v2)
        assertEquals("FingerprintedV1", v2?.classes?.first()?.name)
        assertEquals(1, IncrementalAstCache.diskMisses)
        assertEquals(0, IncrementalAstCache.diskHits)

        val v1Namespace = File(tempDir, "rules-v1")
        val v2Namespace = File(tempDir, "rules-v2")
        assertTrue(v1Namespace.isDirectory, "v1 namespace directory should exist")
        assertTrue(v2Namespace.isDirectory, "v2 namespace directory should exist")

        // And the original fingerprint still hits from disk afterwards.
        IncrementalAstCache.clear()
        Konture.cacheFingerprint = "rules-v1"
        val v1Again = PsiParser.parseFile(file)
        assertNotNull(v1Again)
        assertEquals("FingerprintedV1", v1Again?.classes?.first()?.name)
        assertEquals(1, IncrementalAstCache.diskHits)
    }

    @Test
    fun `modifying source content invalidates the persisted entry`() {
        val file =
            File(tempDir, "Entity.kt").apply {
                writeText(
                    """
                    package com.example
                    class EntityV1
                    """.trimIndent(),
                )
            }

        val first = PsiParser.parseFile(file)
        assertNotNull(first)
        assertEquals("EntityV1", first?.classes?.first()?.name)

        IncrementalAstCache.clear()
        file.writeText(
            """
            package com.example
            class EntityV2
            """.trimIndent(),
        )

        val second = PsiParser.parseFile(file)
        assertNotNull(second)
        assertEquals("EntityV2", second?.classes?.first()?.name)
        assertEquals(1, IncrementalAstCache.diskMisses)
        assertEquals(0, IncrementalAstCache.diskHits)
    }

    @Test
    fun `corrupted cache entries are treated as misses and repaired on re-parse`() {
        val file =
            File(tempDir, "Corruptible.kt").apply {
                writeText(
                    """
                    package com.example
                    class Corruptible
                    """.trimIndent(),
                )
            }

        val first = PsiParser.parseFile(file)
        assertNotNull(first)
        val writesAfterFirst = IncrementalAstCache.diskWrites
        assertTrue(writesAfterFirst > 0)

        val entryFiles =
            tempDir
                .walkTopDown()
                .filter { it.extension == "json" && it.name != "manifest.json" }
                .toList()
        assertTrue(entryFiles.isNotEmpty(), "Cache entry files should exist")
        entryFiles.forEach { it.writeText("{ not valid json !!!") }

        IncrementalAstCache.clear()

        val repaired = PsiParser.parseFile(file)
        assertNotNull(repaired)
        assertEquals("Corruptible", repaired?.classes?.first()?.name)
        assertEquals(entryFiles.size.toLong(), IncrementalAstCache.diskMisses)
        assertTrue(IncrementalAstCache.diskWrites >= writesAfterFirst)
    }

    @Test
    fun `disabled persistent caching writes nothing to disk`() {
        Konture.cacheEnabled = false

        val file =
            File(tempDir, "NoCache.kt").apply {
                writeText(
                    """
                    package com.example
                    class NoCache
                    """.trimIndent(),
                )
            }

        val decl = PsiParser.parseFile(file)
        assertNotNull(decl)
        assertEquals(0, IncrementalAstCache.diskWrites)
        assertEquals(0, IncrementalAstCache.diskHits)
        assertTrue(File(tempDir, "ast").exists().not(), "No cache directory should be created")
    }

    @Test
    fun `persistent cache remains thread-safe under concurrent memory access`() {
        val files =
            (1..20).map { i ->
                File(tempDir, "PersistentConcurrent$i.kt").apply {
                    writeText(
                        """
                        package com.example.concurrent
                        class PersistentConcurrentClass$i {
                            fun doWork$i(): Int = $i
                        }
                        """.trimIndent(),
                    )
                }
            }

        // Cold parse persists all entries to disk.
        files.forEach { file ->
            assertNotNull(PsiParser.parseFile(file))
        }
        assertEquals(20, IncrementalAstCache.diskWrites)
        assertEquals(0, IncrementalAstCache.diskHits)

        val executor = Executors.newFixedThreadPool(8)
        val tasks =
            (1..50).map {
                Runnable {
                    files.forEach { file ->
                        val decl = PsiParser.parseFile(file)
                        assertNotNull(decl)
                        assertTrue(decl?.classes?.isNotEmpty() == true)
                    }
                }
            }
        tasks.forEach { executor.submit(it) }
        executor.shutdown()
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS))

        // All subsequent concurrent passes hit the shared in-memory cache.
        assertEquals(20, IncrementalAstCache.parseMisses)
        assertEquals(50 * 20L, IncrementalAstCache.parseHits)
    }

    @Test
    fun `disk cache is read safely under concurrent cold start`() {
        val files =
            (1..20).map { i ->
                File(tempDir, "PersistentColdStart$i.kt").apply {
                    writeText(
                        """
                        package com.example.concurrent
                        class PersistentColdStartClass$i
                        """.trimIndent(),
                    )
                }
            }

        // Persist all entries from the main thread.
        files.forEach { file ->
            assertNotNull(PsiParser.parseFile(file))
        }
        assertEquals(20, IncrementalAstCache.diskWrites)

        // Simulate a fresh JVM: memory is cleared and each worker thread starts with
        // a fresh thread-local state (Konture is thread-isolated by design).
        IncrementalAstCache.clear()
        val executor = Executors.newFixedThreadPool(8)
        val tasks =
            (1..8).map {
                Runnable {
                    Konture.incremental = true
                    Konture.cacheEnabled = true
                    Konture.cacheDir = tempDir
                    files.forEach { file ->
                        val decl = PsiParser.parseFile(file)
                        assertNotNull(decl)
                        val index = file.nameWithoutExtension.removePrefix("PersistentColdStart")
                        assertEquals("PersistentColdStartClass$index", decl?.classes?.first()?.name)
                    }
                }
            }
        tasks.forEach { executor.submit(it) }
        executor.shutdown()
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS))

        // Every file was restored from disk at least once across the worker threads.
        assertTrue(
            IncrementalAstCache.diskHits >= 20,
            "Expected at least 20 disk hits, got ${IncrementalAstCache.diskHits}",
        )
    }
}
