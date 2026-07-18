/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Konture
import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.RuleBuildersTestBase
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ThreadIsolationTest : RuleBuildersTestBase() {
    @TempDir
    lateinit var tempDir: File

    private var originalGenerateBaseline: Boolean = false
    private var originalBaselinePath: String = ""
    private var originalBaselineDirProp: String? = null

    @BeforeEach
    override fun setUp() {
        super.setUp()
        originalGenerateBaseline = Konture.generateBaseline
        originalBaselinePath = Konture.baselinePath
        originalBaselineDirProp = System.getProperty(Konture.PROPERTY_BASELINE_DIR)

        System.setProperty(Konture.PROPERTY_BASELINE_DIR, tempDir.absolutePath)
        KontureContextProvider.reset()
    }

    @AfterEach
    fun tearDown() {
        Konture.generateBaseline = originalGenerateBaseline
        Konture.baselinePath = originalBaselinePath
        if (originalBaselineDirProp != null) {
            System.setProperty(Konture.PROPERTY_BASELINE_DIR, originalBaselineDirProp!!)
        } else {
            System.clearProperty(Konture.PROPERTY_BASELINE_DIR)
        }
        KontureContextProvider.reset()
    }

    @Test
    fun testProjectGraphThreadLocalIsolation() {
        assertFalse(ProjectGraph.isDefaultInitialized())

        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)
        var thread2InitializedBeforeSet = true
        var thread2InitializedAfterSet = true

        val t1 =
            Thread {
                ProjectGraph.setDefault(projectGraph)
                latch1.countDown()
            }

        val t2 =
            Thread {
                // Wait for Thread 1 to set its default
                latch1.await(5, TimeUnit.SECONDS)
                thread2InitializedBeforeSet = ProjectGraph.isDefaultInitialized()

                // Now set its own default
                ProjectGraph.setDefault(projectGraph)
                thread2InitializedAfterSet = ProjectGraph.isDefaultInitialized()
                latch2.countDown()
            }

        t1.start()
        t2.start()

        latch2.await(5, TimeUnit.SECONDS)

        // The main thread should still be uninitialized
        assertFalse(ProjectGraph.isDefaultInitialized())
        assertFalse(thread2InitializedBeforeSet, "Thread 2 should not inherit Thread 1's graph setting")
        assertTrue(thread2InitializedAfterSet, "Thread 2 should be able to set its own default graph")
    }

    @Test
    fun testLoggerThreadLocalIsolation() {
        val latch = CountDownLatch(1)
        var t1LogLevel: LogLevel? = null
        var t2LogLevel: LogLevel? = null

        val t1 =
            Thread {
                KontureLogger.minLevel = LogLevel.ERROR
                t1LogLevel = KontureLogger.minLevel
                latch.countDown()
            }

        t1.start()
        latch.await(5, TimeUnit.SECONDS)

        t2LogLevel = KontureLogger.minLevel

        assertEquals(LogLevel.ERROR, t1LogLevel)
        assertEquals(LogLevel.INFO, t2LogLevel, "Thread 2 should have the default log level, isolated from Thread 1")
    }

    @Test
    fun testSettingsChangePreservesBaselineManagerAndViolations() {
        val originalManager = KontureContextProvider.currentContext.baselineManager
        assertNotNull(originalManager)

        // Record a dummy violation
        val violation =
            FlatBaselineViolation(
                testClass = "MyIsolationTest",
                testMethod = "testMethod",
                location = "some/file.kt:10",
                message = "Some test error",
            )
        originalManager.recordedViolations.add(violation)
        assertEquals(1, originalManager.recordedViolations.size)

        // Change public settings
        Konture.baselinePath = "brand-new-path.json"
        Konture.generateBaseline = true

        // Retrieve the current context's baselineManager
        val newManager = KontureContextProvider.currentContext.baselineManager

        // It must be the exact same instance
        assertSame(originalManager, newManager, "BaselineManager instance should be preserved across settings updates")
        assertEquals(1, newManager.recordedViolations.size, "Recorded violations must be preserved")
        assertEquals("brand-new-path.json", KontureContextProvider.currentContext.baselinePath)
        assertTrue(KontureContextProvider.currentContext.generateBaseline)
    }

    @Test
    fun testBaselineCachingAndLazyReadCorrectness() {
        val fileName = "test-caching.json"
        val cacheFile = File(tempDir, fileName)

        // 1. Write initial baseline with one violation for this test
        cacheFile.writeText(
            """
            {
              "version": 1,
              "testClasses": [
                {
                  "name": "io.github.baole.konture.impl.ThreadIsolationTest",
                  "tests": [
                    {
                      "name": "testBaselineCachingAndLazyReadCorrectness",
                      "violations": [
                        {
                          "location": "com.example.Class1",
                          "message": "violates rule 1"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
        )

        BaselineManager.resetForTest()
        Konture.generateBaseline = false
        Konture.baselinePath = fileName

        // 2. Querying the violation should be baselined and NOT throw an exception
        assertDoesNotThrow {
            BaselineManager.handleViolations(
                listOf("Class com.example.Class1 violates rule 1"),
                "detected",
            )
        }

        // 3. Overwrite the file on disk to clear the violations completely.
        cacheFile.writeText(
            """
            {
              "version": 1,
              "testClasses": []
            }
            """.trimIndent(),
        )

        // 4. Checking the violation again MUST STILL NOT throw because the baseline is cached!
        assertDoesNotThrow {
            BaselineManager.handleViolations(
                listOf("Class com.example.Class1 violates rule 1"),
                "detected",
            )
        }

        // 5. Invalidate the cache by changing baselinePath dynamically
        Konture.baselinePath = "./test-caching.json"

        // 6. Checking the violation now MUST throw an AssertionError because the cache was invalidated,
        // and it reloaded the empty baseline from disk!
        assertThrows(AssertionError::class.java) {
            BaselineManager.handleViolations(
                listOf("Class com.example.Class1 violates rule 1"),
                "detected",
            )
        }
    }
}
