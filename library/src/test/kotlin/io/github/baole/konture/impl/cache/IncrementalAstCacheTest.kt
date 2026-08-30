/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
import kotlin.system.measureTimeMillis

class IncrementalAstCacheTest {
    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        Konture.reset()
        Konture.incremental = true
        IncrementalAstCache.clear()
    }

    @AfterEach
    fun tearDown() {
        Konture.reset()
        IncrementalAstCache.clear()
    }

    @Test
    fun `getDeclaredClassFqNames hits cache on repeated calls with unchanged files`() {
        val file1 =
            File(tempDir, "User.kt").apply {
                writeText(
                    """
                    package com.example.domain
                    class User
                    class UserProfile
                    """.trimIndent(),
                )
            }
        val file2 =
            File(tempDir, "Order.kt").apply {
                writeText(
                    """
                    package com.example.order
                    class Order
                    """.trimIndent(),
                )
            }
        val files = listOf(file1, file2)

        val firstScan = PsiParser.getDeclaredClassFqNames(files)
        assertEquals(
            setOf("com.example.domain.User", "com.example.domain.UserProfile", "com.example.order.Order"),
            firstScan,
        )
        assertEquals(2, IncrementalAstCache.classScanMisses)
        assertEquals(0, IncrementalAstCache.classScanHits)

        // Second run with unchanged files
        val secondScan = PsiParser.getDeclaredClassFqNames(files)
        assertEquals(firstScan, secondScan)
        assertEquals(2, IncrementalAstCache.classScanHits)
        assertEquals(2, IncrementalAstCache.classScanMisses)
    }

    @Test
    fun `getDeclaredTypeAliases hits cache on repeated calls with unchanged files`() {
        val file =
            File(tempDir, "Aliases.kt").apply {
                writeText(
                    """
                    package com.example.types
                    typealias UserId = String
                    typealias OrderId = Long
                    """.trimIndent(),
                )
            }
        val files = listOf(file)

        val firstScan = PsiParser.getDeclaredTypeAliases(files)
        assertEquals(2, firstScan.size)
        assertEquals(1, IncrementalAstCache.typeAliasScanMisses)
        assertEquals(0, IncrementalAstCache.typeAliasScanHits)

        val secondScan = PsiParser.getDeclaredTypeAliases(files)
        assertEquals(firstScan.keys, secondScan.keys)
        assertEquals(1, IncrementalAstCache.typeAliasScanHits)
    }

    @Test
    fun `parseFile hits cache on repeated calls for unchanged file`() {
        val file =
            File(tempDir, "Service.kt").apply {
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

        val decl1 = PsiParser.parseFile(file, lookup)
        assertNotNull(decl1)
        assertEquals("UserService", decl1?.classes?.first()?.name)
        assertEquals(1, IncrementalAstCache.parseMisses)
        assertEquals(0, IncrementalAstCache.parseHits)

        val decl2 = PsiParser.parseFile(file, lookup)
        assertNotNull(decl2)
        assertEquals(decl1?.name, decl2?.name)
        assertEquals(decl1?.classes?.first()?.fqName, decl2?.classes?.first()?.fqName)
        assertEquals(1, IncrementalAstCache.parseHits)
        assertEquals(1, IncrementalAstCache.parseMisses)
    }

    @Test
    fun `modifying file content causes cache miss and re-parse with updated AST`() {
        val file =
            File(tempDir, "Entity.kt").apply {
                writeText(
                    """
                    package com.example
                    class EntityV1
                    """.trimIndent(),
                )
            }

        val decl1 = PsiParser.parseFile(file)
        assertNotNull(decl1)
        assertEquals("EntityV1", decl1?.classes?.first()?.name)
        assertEquals(1, IncrementalAstCache.parseMisses)

        // Modify file
        file.writeText(
            """
            package com.example
            class EntityV2
            """.trimIndent(),
        )

        val decl2 = PsiParser.parseFile(file)
        assertNotNull(decl2)
        assertEquals("EntityV2", decl2?.classes?.first()?.name)
        assertEquals(2, IncrementalAstCache.parseMisses)
        assertEquals(0, IncrementalAstCache.parseHits)
    }

    @Test
    fun `disabling incremental mode bypasses caching`() {
        Konture.incremental = false

        val file =
            File(tempDir, "Bypass.kt").apply {
                writeText(
                    """
                    package com.example
                    class Bypass
                    """.trimIndent(),
                )
            }

        val decl1 = PsiParser.parseFile(file)
        val decl2 = PsiParser.parseFile(file)
        assertNotNull(decl1)
        assertNotNull(decl2)
        assertEquals(0, IncrementalAstCache.parseHits)
        assertEquals(0, IncrementalAstCache.parseMisses)
    }

    @Test
    fun `cache is thread-safe under concurrent access`() {
        val files =
            (1..20).map { i ->
                File(tempDir, "Concurrent$i.kt").apply {
                    writeText(
                        """
                        package com.example.concurrent
                        class ConcurrentClass$i {
                            fun doWork$i(): Int = $i
                        }
                        """.trimIndent(),
                    )
                }
            }

        // Pre-warm cache with initial parse
        files.forEach { file ->
            val decl = PsiParser.parseFile(file)
            assertNotNull(decl)
        }
        assertEquals(20, IncrementalAstCache.parseMisses)
        assertEquals(0, IncrementalAstCache.parseHits)

        val executor = Executors.newFixedThreadPool(8)
        val tasks =
            (1..50).map {
                Runnable {
                    files.forEach { file ->
                        val decl = PsiParser.parseFile(file)
                        assertNotNull(decl)
                    }
                }
            }

        tasks.forEach { executor.submit(it) }
        executor.shutdown()
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS))

        // All subsequent concurrent passes hit cache without additional misses
        assertEquals(20, IncrementalAstCache.parseMisses)
        assertEquals(50 * 20L, IncrementalAstCache.parseHits)
    }

    @Test
    fun `two files with identical content in different paths retain their respective paths and are cached separately`() {
        val fileA =
            File(tempDir, "ModuleA_Config.kt").apply {
                writeText(
                    """
                    package com.example.config
                    class Config
                    """.trimIndent(),
                )
            }
        val fileB =
            File(tempDir, "ModuleB_Config.kt").apply {
                writeText(
                    """
                    package com.example.config
                    class Config
                    """.trimIndent(),
                )
            }

        val declA = PsiParser.parseFile(fileA)
        val declB = PsiParser.parseFile(fileB)

        assertNotNull(declA)
        assertNotNull(declB)
        assertEquals(fileA.absolutePath, declA?.filePath)
        assertEquals(fileB.absolutePath, declB?.filePath)
        assertEquals("ModuleA_Config.kt", declA?.name)
        assertEquals("ModuleB_Config.kt", declB?.name)

        // Both files caused misses initially
        assertEquals(2, IncrementalAstCache.parseMisses)

        // Repeated parse hits cache for both files and maintains correct paths
        val cachedA = PsiParser.parseFile(fileA)
        val cachedB = PsiParser.parseFile(fileB)
        assertEquals(fileA.absolutePath, cachedA?.filePath)
        assertEquals(fileB.absolutePath, cachedB?.filePath)
        assertEquals(2, IncrementalAstCache.parseHits)
    }

    @Test
    fun `benchmark verifies sub-second incremental execution time`() {
        val files =
            (1..50).map { i ->
                File(tempDir, "Bench$i.kt").apply {
                    writeText(
                        """
                        package com.example.benchmark
                        import java.util.UUID

                        interface Service$i {
                            fun execute(): String
                        }

                        class ServiceImpl$i : Service$i {
                            val id: String = UUID.randomUUID().toString()
                            override fun execute(): String = id
                        }
                        """.trimIndent(),
                    )
                }
            }

        // Initial cold parse
        val coldTime =
            measureTimeMillis {
                PsiParser.getDeclaredClassFqNames(files)
                PsiParser.getDeclaredTypeAliases(files)
                files.forEach { PsiParser.parseFile(it) }
            }

        // Incremental cached run
        val warmTime =
            measureTimeMillis {
                val classes = PsiParser.getDeclaredClassFqNames(files)
                val aliases = PsiParser.getDeclaredTypeAliases(files)
                val decls = files.mapNotNull { PsiParser.parseFile(it) }
                assertEquals(100, classes.size) // 50 interfaces + 50 classes
                assertEquals(50, decls.size)
            }

        // Warm run must be well under 1000ms (sub-second)
        assertTrue(
            warmTime < 1000,
            "Incremental re-analysis must execute in sub-second time, took: ${warmTime}ms (cold was: ${coldTime}ms)",
        )
    }
}
