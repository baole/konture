/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.benchmark

import io.github.baole.konture.Konture
import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.architecture
import io.github.baole.konture.impl.cache.IncrementalAstCache
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.system.measureTimeMillis

@Tag("benchmark")
class LargeRepositoryPerformanceBenchmarkTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var graph: ProjectGraph

    @BeforeEach
    fun setUp() {
        Konture.reset()
        IncrementalAstCache.clear()
        // Generate synthetic repository with 100 modules
        val repoDir = File(tempDir, "repo")
        graph = LargeRepositoryBenchmarkGenerator.generateRepository(repoDir, moduleCount = 100)
    }

    @AfterEach
    fun tearDown() {
        Konture.reset()
        IncrementalAstCache.clear()
    }

    private fun executeComprehensiveRuleSuite() {
        Konture.architecture(graph) {
            // Rule 1: No cyclic dependencies
            noCycles()

            // Rule 2: Module naming convention
            modules {
                that().haveNameStartingWith("core")
                should().satisfy { it.appliedPlugins.contains("kotlin") }
            }

            // Rule 3: Domain layer dependencies
            modules {
                that().haveNameStartingWith("domain")
                should().satisfy { it.appliedPlugins.contains("java") }
            }

            // Rule 4: Feature layer plugin check
            modules {
                that().haveNameStartingWith("feature")
                should().satisfy { it.appliedPlugins.contains("kotlin") }
            }

            // Rule 5: App layer plugin check
            modules {
                that().haveNameStartingWith("app")
                should().satisfy { it.appliedPlugins.contains("kotlin") }
            }

            // Rule 6: Class naming convention for Services
            classes {
                that().haveNameEndingWith("Service")
                should().resideInAPackage("com.example..")
            }

            // Rule 7: Class naming convention for Models
            classes {
                that().haveNameEndingWith("Model")
                should().resideInAPackage("com.example..")
            }

            // Rule 8: File naming convention
            files {
                that().haveNameEndingWith("Service.kt")
                should().resideInAPackage("com.example..")
            }

            // Rule 9: File naming convention for models
            files {
                that().haveNameEndingWith("Model.kt")
                should().resideInAPackage("com.example..")
            }

            // Rule 10: General package structure assertion
            classes {
                that().resideInAPackage("com.example..")
                should().satisfy { true }
            }
        }
    }

    @Test
    fun `cold run benchmark completes under 5000ms for 100-module repository`() {
        Konture.parallel = true
        Konture.incremental = false
        Konture.cacheEnabled = false

        val coldDurationMs =
            measureTimeMillis {
                executeComprehensiveRuleSuite()
            }

        println("Cold analysis duration for 100 modules: ${coldDurationMs}ms")
        assertTrue(
            coldDurationMs < 5000,
            "Cold run benchmark target is < 5000ms, but was ${coldDurationMs}ms",
        )
    }

    @Test
    fun `incremental run benchmark completes under 1000ms for 100-module repository`() {
        Konture.parallel = true
        Konture.incremental = true
        Konture.cacheEnabled = true
        Konture.cacheDir = File(tempDir, "cache")

        // Initial warm-up run to populate the AST cache
        executeComprehensiveRuleSuite()

        // Incremental re-execution with warm cache
        val incrementalDurationMs =
            measureTimeMillis {
                executeComprehensiveRuleSuite()
            }

        println("Incremental analysis duration for 100 modules: ${incrementalDurationMs}ms")
        assertTrue(
            incrementalDurationMs < 1000,
            "Incremental run benchmark target is < 1000ms, but was ${incrementalDurationMs}ms",
        )
    }
}
