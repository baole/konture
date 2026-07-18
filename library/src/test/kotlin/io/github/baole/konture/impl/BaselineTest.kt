/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Konture
import io.github.baole.konture.RuleBuildersTestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BaselineTest : RuleBuildersTestBase() {
    @TempDir
    lateinit var tempDir: File

    private var originalGenerateBaseline: Boolean = false
    private var originalBaselinePath: String = ""
    private var originalBaselineDirProp: String? = null
    private var originalBaselineGenerateProp: String? = null

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Save current configurations
        originalGenerateBaseline = Konture.generateBaseline
        originalBaselinePath = Konture.baselinePath
        originalBaselineDirProp = System.getProperty(Konture.PROPERTY_BASELINE_DIR)
        originalBaselineGenerateProp = System.getProperty(Konture.PROPERTY_BASELINE_GENERATE)

        // Set up test configurations to write to the temp directory
        System.setProperty(Konture.PROPERTY_BASELINE_DIR, tempDir.absolutePath)
        Konture.baselinePath = "test-baseline.json"

        // Reset BaselineManager state
        BaselineManager.resetForTest()
    }

    @AfterEach
    fun tearDown() {
        // Restore configurations
        Konture.generateBaseline = originalGenerateBaseline
        Konture.baselinePath = originalBaselinePath
        if (originalBaselineDirProp != null) {
            System.setProperty(Konture.PROPERTY_BASELINE_DIR, originalBaselineDirProp!!)
        } else {
            System.clearProperty(Konture.PROPERTY_BASELINE_DIR)
        }
        if (originalBaselineGenerateProp != null) {
            System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, originalBaselineGenerateProp!!)
        } else {
            System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
        }

        // Reset BaselineManager state
        BaselineManager.resetForTest()
    }

    @Test
    fun `test path normalization`() {
        val buildRoot = File("/Users/baole/workspace/konture").canonicalFile
        val violation = "Class com.example.ClassA in file /Users/baole/workspace/konture/library/src/ClassA.kt violates architecture rules"

        val normalized = BaselineManager.normalize(violation, buildRoot)
        assertEquals("Class com.example.ClassA in file <root>/library/src/ClassA.kt violates architecture rules", normalized)
    }

    @Test
    fun `test recording mode captures and writes violations with structured data`() {
        Konture.generateBaseline = true
        val violations =
            listOf(
                "Class com.example.ClassA depends on com.example.ClassB",
                "Class com.other.ClassC depends on com.example.ClassB",
            )

        // Invoke handleViolations
        BaselineManager.handleViolations(violations, "Class architecture violation(s) detected:")

        // Force a save by calling internal writeBaseline directly
        BaselineManager.writeBaseline()

        // Verify file is created and contains serialized JSON
        val baselineFile = File(tempDir, "test-baseline.json")
        assertTrue(baselineFile.exists(), "Baseline file should be created")

        val content = baselineFile.readText()

        // Check structured fields of format including the version
        println("DEBUG_JSON:\n$content")
        assertTrue(content.contains("\"version\": 1"))
        assertTrue(content.contains("\"name\": \"io.github.baole.konture.impl.BaselineTest\""))
        assertTrue(content.contains("\"name\": \"test recording mode captures and writes violations with structured data\""))
        assertTrue(content.contains("\"message\": \"depends on com.example.ClassB\""))
    }

    @Test
    fun `test recording omits null violation locations from baseline JSON`() {
        Konture.generateBaseline = true
        BaselineManager.handleViolations(listOf("A location-less violation"), "detected")

        BaselineManager.writeBaseline()

        val content = File(tempDir, "test-baseline.json").readText()
        assertFalse(content.contains("\"location\": null"))
    }

    @Test
    fun `test loading and parsing of structured baseline data`() {
        val baselineFile = File(tempDir, "test-baseline.json")
        val structuredBaseline =
            """
            {
              "version": 1,
              "testClasses": [
                {
                  "name": "io.github.baole.konture.impl.BaselineTest",
                  "tests": [
                    {
                      "name": "test loading and parsing of structured baseline data",
                      "violations": [
                        {
                          "location": "com.example.ClassA",
                          "message": "depends on com.example.ClassB"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        baselineFile.writeText(structuredBaseline)

        // Reset BaselineManager state to reload the newly written baseline file
        BaselineManager.resetForTest()

        Konture.generateBaseline = false

        // This violation is baselined, so it should not throw
        assertDoesNotThrow {
            BaselineManager.handleViolations(
                listOf("Class com.example.ClassA depends on com.example.ClassB"),
                "Class architecture violation(s) detected:",
            )
        }

        // This violation is NOT baselined (different message/class), so it must throw AssertionError
        val exception =
            assertThrows(AssertionError::class.java) {
                BaselineManager.handleViolations(
                    listOf("Class com.other.ClassC depends on com.example.ClassB"),
                    "Class architecture violation(s) detected:",
                )
            }
        assertTrue(exception.message!!.contains("depends on com.example.ClassB (at com.other.ClassC)"))
    }

    @Test
    fun `test findModuleForViolation maps correctly`() {
        val graph = projectGraph

        // Case 1: Location starts with ":" (gradle module path)
        val v1 = FlatBaselineViolation("TestClass", "testMethod", ":moduleA", "some message")
        val m1 = BaselineManager.findModuleForViolation(v1, graph)
        assertNotNull(m1)
        assertEquals(":moduleA", m1?.path)

        // Case 2: Location is relative path matching module directory prefix
        val v2 = FlatBaselineViolation("TestClass", "testMethod", "moduleB/src/main/kotlin/ClassB.kt", "some message")
        val m2 = BaselineManager.findModuleForViolation(v2, graph)
        assertNotNull(m2)
        assertEquals(":moduleB", m2?.path)

        // Case 3: Location is exact module directory
        val v3 = FlatBaselineViolation("TestClass", "testMethod", "moduleC", "some message")
        val m3 = BaselineManager.findModuleForViolation(v3, graph)
        assertNotNull(m3)
        assertEquals(":moduleC", m3?.path)
    }

    @Test
    fun `test custom baseline path resolved dynamically from system property`() {
        val originalProp = System.getProperty(Konture.PROPERTY_BASELINE_PATH)
        val originalProgrammaticValue = Konture.baselinePath
        try {
            System.setProperty(Konture.PROPERTY_BASELINE_PATH, "sys-prop-baseline.json")
            assertEquals("sys-prop-baseline.json", Konture.baselinePath)
        } finally {
            if (originalProp != null) {
                System.setProperty(Konture.PROPERTY_BASELINE_PATH, originalProp)
            } else {
                System.clearProperty(Konture.PROPERTY_BASELINE_PATH)
            }
            Konture.baselinePath = originalProgrammaticValue
        }
    }

    @Test
    fun `test generate baseline flag resolved dynamically from system property`() {
        val originalProp = System.getProperty(Konture.PROPERTY_BASELINE_GENERATE)
        val originalProgrammaticValue = Konture.generateBaseline
        try {
            System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "true")
            assertTrue(Konture.generateBaseline)

            System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "false")
            assertFalse(Konture.generateBaseline)
        } finally {
            if (originalProp != null) {
                System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, originalProp)
            } else {
                System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
            }
            Konture.generateBaseline = originalProgrammaticValue
        }
    }

    @Test
    fun `test JVM system properties take precedence over programmatic properties`() {
        val origPath = Konture.baselinePath
        val origGen = Konture.generateBaseline

        try {
            System.setProperty(Konture.PROPERTY_BASELINE_PATH, "sys-path.json")
            System.setProperty(Konture.PROPERTY_BASELINE_GENERATE, "true")

            Konture.baselinePath = "programmatic-path.json"
            Konture.generateBaseline = false

            assertEquals("sys-path.json", Konture.baselinePath)
            assertTrue(Konture.generateBaseline)
        } finally {
            System.clearProperty(Konture.PROPERTY_BASELINE_PATH)
            System.clearProperty(Konture.PROPERTY_BASELINE_GENERATE)
            Konture.baselinePath = origPath
            Konture.generateBaseline = origGen
        }
    }

    @Test
    fun `test baseline cache invalidation and reload upon configuration changes`() {
        val file1 = File(tempDir, "baseline-1.json")
        val file2 = File(tempDir, "baseline-2.json")

        file1.writeText(
            """
            {
              "version": 1,
              "testClasses": [
                {
                  "name": "io.github.baole.konture.impl.BaselineTest",
                  "tests": [
                    {
                      "name": "test baseline cache invalidation and reload upon configuration changes",
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

        file2.writeText(
            """
            {
              "version": 1,
              "testClasses": [
                {
                  "name": "io.github.baole.konture.impl.BaselineTest",
                  "tests": [
                    {
                      "name": "test baseline cache invalidation and reload upon configuration changes",
                      "violations": [
                        {
                          "location": "com.example.Class2",
                          "message": "violates rule 2"
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

        // Load 1st configuration
        Konture.baselinePath = "baseline-1.json"

        // This violation in baseline-1 should be ignored (no throw)
        assertDoesNotThrow {
            BaselineManager.handleViolations(
                listOf("Class com.example.Class1 violates rule 1"),
                "detected",
            )
        }

        // Change configuration path dynamically without manual reset
        Konture.baselinePath = "baseline-2.json"

        // The old violation (from baseline-1) is no longer baselined, so it should throw
        assertThrows(AssertionError::class.java) {
            BaselineManager.handleViolations(
                listOf("Class com.example.Class1 violates rule 1"),
                "detected",
            )
        }

        // The new violation (from baseline-2) should be baselined, so it shouldn't throw
        assertDoesNotThrow {
            BaselineManager.handleViolations(
                listOf("Class com.example.Class2 violates rule 2"),
                "detected",
            )
        }
    }

    @Test
    fun `test test-level isolation matching with identical messages`() {
        val baselineFile = File(tempDir, "test-baseline.json")
        val structuredBaseline =
            """
            {
              "version": 1,
              "testClasses": [
                {
                  "name": "io.github.baole.konture.impl.BaselineTest",
                  "tests": [
                    {
                      "name": "matchingMethod",
                      "violations": [
                        {
                          "location": "com.example.ClassA",
                          "message": "depends on com.example.ClassB"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        baselineFile.writeText(structuredBaseline)

        BaselineManager.resetForTest()
        Konture.generateBaseline = false

        // The baseline matches only when class = BaselineTest and method = "matchingMethod".
        // But our current executing test is "test test-level isolation matching with identical messages".
        // So the same violation must NOT be suppressed because of test-level isolation.
        val exception =
            assertThrows(AssertionError::class.java) {
                BaselineManager.handleViolations(
                    listOf("Class com.example.ClassA depends on com.example.ClassB"),
                    "detected",
                )
            }
        assertTrue(exception.message!!.contains("depends on com.example.ClassB (at com.example.ClassA)"))
    }

    class DummyFirstTest {
        fun runFirst() {
            BaselineManager.handleViolations(
                listOf("Class com.example.ClassA depends on com.example.ClassB"),
                "detected",
            )
        }
    }

    class DummySecondTest {
        fun runSecond() {
            BaselineManager.handleViolations(
                listOf("Class com.example.ClassC depends on com.example.ClassD"),
                "detected",
            )
        }
    }

    @Test
    fun `test multi-class baseline writing and reading end-to-end`() {
        BaselineManager.resetForTest()

        val baselineFile = File(tempDir, "test-baseline-multi.json")
        System.setProperty(Konture.PROPERTY_BASELINE_DIR, tempDir.absolutePath)
        Konture.baselinePath = baselineFile.absolutePath
        Konture.generateBaseline = true

        try {
            DummyFirstTest().runFirst()
            DummySecondTest().runSecond()

            BaselineManager.writeBaseline()

            assertTrue(baselineFile.exists())
            val content = baselineFile.readText()
            assertTrue(content.contains("\"testClasses\":"))
            assertTrue(content.contains("DummyFirstTest"))
            assertTrue(content.contains("DummySecondTest"))

            // Reset and reload from file
            BaselineManager.resetForTest()
            Konture.generateBaseline = false

            // These should be successfully suppressed
            DummyFirstTest().runFirst()
            DummySecondTest().runSecond()

            // But running the same violation in a different class should fail (isolation)
            class OtherTest {
                fun runOther() {
                    BaselineManager.handleViolations(
                        listOf("Class com.example.ClassA depends on com.example.ClassB"),
                        "detected",
                    )
                }
            }
            val exception =
                assertThrows(AssertionError::class.java) {
                    OtherTest().runOther()
                }
            assertTrue(exception.message!!.contains("depends on com.example.ClassB"))
        } finally {
            System.clearProperty(Konture.PROPERTY_BASELINE_DIR)
            baselineFile.delete()
        }
    }
}
