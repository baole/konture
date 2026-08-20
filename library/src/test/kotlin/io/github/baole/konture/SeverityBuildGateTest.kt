/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.impl.KontureRuntimeStateProvider
import io.github.baole.konture.impl.report.ReportAccumulator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SeverityBuildGateTest : RuleBuildersTestBase() {
    private val loggedMessages = mutableListOf<Pair<LogLevel, String>>()
    private val originalLogger = KontureLogger.logger
    private val originalMinLevel = KontureLogger.minLevel

    @BeforeEach
    fun setUpGate() {
        loggedMessages.clear()
        KontureLogger.minLevel = LogLevel.TRACE
        KontureLogger.logger = { level, message, _ ->
            loggedMessages.add(level to message)
        }
        System.clearProperty(Konture.PROPERTY_FAIL_ON_SEVERITY)
        KontureRuntimeStateProvider.reset()
        ReportAccumulator.clear()
    }

    @AfterEach
    fun tearDownGate() {
        KontureLogger.logger = originalLogger
        KontureLogger.minLevel = originalMinLevel
        System.clearProperty(Konture.PROPERTY_FAIL_ON_SEVERITY)
        KontureRuntimeStateProvider.reset()
        ReportAccumulator.clear()
    }

    @Test
    fun `test default threshold is ERROR where WARNING and INFO do not fail`() {
        assertEquals(Severity.ERROR, Konture.failOnSeverity)

        // Rule with Severity.ERROR should throw AssertionError
        val errorRule =
            rule("classes.error.rule") {
                severity = Severity.ERROR
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Error violation occurred")
                    }
                }
            }

        assertThrows<AssertionError> {
            errorRule.check()
        }

        loggedMessages.clear()

        // Rule with Severity.WARNING should not throw, but logs diagnostic
        val warningRule =
            rule("classes.warning.rule") {
                severity = Severity.WARNING
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Warning violation occurred")
                    }
                }
            }

        assertDoesNotThrow {
            warningRule.check()
        }

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.WARNING &&
                    msg.contains("[Konture] WARNING violation (non-blocking) in rule 'classes.warning.rule'")
            },
            "Expected WARNING diagnostic log for sub-threshold violation, got: $loggedMessages",
        )

        loggedMessages.clear()

        // Rule with Severity.INFO should not throw, but logs diagnostic
        val infoRule =
            rule("classes.info.rule") {
                severity = Severity.INFO
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Info violation occurred")
                    }
                }
            }

        assertDoesNotThrow {
            infoRule.check()
        }

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.INFO &&
                    msg.contains("[Konture] INFO violation (non-blocking) in rule 'classes.info.rule'")
            },
            "Expected INFO diagnostic log for sub-threshold violation, got: $loggedMessages",
        )
    }

    @Test
    fun `test threshold WARNING fails on ERROR and WARNING but allows INFO`() {
        Konture.failOnSeverity = Severity.WARNING

        val errorRule =
            rule("classes.error.rule") {
                severity = Severity.ERROR
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Error violation occurred")
                    }
                }
            }

        assertThrows<AssertionError> {
            errorRule.check()
        }

        val warningRule =
            rule("classes.warning.rule") {
                severity = Severity.WARNING
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Warning violation occurred")
                    }
                }
            }

        assertThrows<AssertionError> {
            warningRule.check()
        }

        loggedMessages.clear()

        val infoRule =
            rule("classes.info.rule") {
                severity = Severity.INFO
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Info violation occurred")
                    }
                }
            }

        assertDoesNotThrow {
            infoRule.check()
        }

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.INFO && msg.contains("[Konture] INFO violation (non-blocking)")
            },
        )
    }

    @Test
    fun `test threshold INFO fails on ERROR, WARNING, and INFO`() {
        Konture.failOnSeverity = Severity.INFO

        val errorRule =
            rule("classes.error.rule") {
                severity = Severity.ERROR
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Error violation")
                    }
                }
            }

        assertThrows<AssertionError> { errorRule.check() }

        val warningRule =
            rule("classes.warning.rule") {
                severity = Severity.WARNING
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Warning violation")
                    }
                }
            }

        assertThrows<AssertionError> { warningRule.check() }

        val infoRule =
            rule("classes.info.rule") {
                severity = Severity.INFO
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Info violation")
                    }
                }
            }

        assertThrows<AssertionError> { infoRule.check() }
    }

    @Test
    fun `test audit mode with null threshold does not fail on any severity`() {
        Konture.failOnSeverity = null

        val errorRule =
            rule("classes.error.rule") {
                severity = Severity.ERROR
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Error violation")
                    }
                }
            }

        assertDoesNotThrow { errorRule.check() }

        val warningRule =
            rule("classes.warning.rule") {
                severity = Severity.WARNING
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Warning violation")
                    }
                }
            }

        assertDoesNotThrow { warningRule.check() }

        val infoRule =
            rule("classes.info.rule") {
                severity = Severity.INFO
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Info violation")
                    }
                }
            }

        assertDoesNotThrow { infoRule.check() }

        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.ERROR && msg.contains("classes.error.rule")
            },
        )
        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.WARNING && msg.contains("classes.warning.rule")
            },
        )
        assertTrue(
            loggedMessages.any { (level, msg) ->
                level == LogLevel.INFO && msg.contains("classes.info.rule")
            },
        )
    }

    @Test
    fun `test system property configuration and overrides`() {
        System.setProperty(Konture.PROPERTY_FAIL_ON_SEVERITY, "none")
        assertNull(Konture.failOnSeverity)

        System.setProperty(Konture.PROPERTY_FAIL_ON_SEVERITY, "info")
        assertEquals(Severity.INFO, Konture.failOnSeverity)

        System.setProperty(Konture.PROPERTY_FAIL_ON_SEVERITY, "warning")
        assertEquals(Severity.WARNING, Konture.failOnSeverity)

        System.setProperty(Konture.PROPERTY_FAIL_ON_SEVERITY, "error")
        assertEquals(Severity.ERROR, Konture.failOnSeverity)

        System.setProperty(Konture.PROPERTY_FAIL_ON_SEVERITY, "invalid-value")
        assertEquals(Severity.ERROR, Konture.failOnSeverity)

        // Programmatic override takes precedence
        Konture.failOnSeverity = Severity.WARNING
        assertEquals(Severity.WARNING, Konture.failOnSeverity)
    }

    @Test
    fun `test thread local isolation of failOnSeverity`() {
        Konture.failOnSeverity = Severity.ERROR

        val latch = CountDownLatch(1)
        var threadThreshold: Severity? = Severity.ERROR

        val thread =
            Thread {
                Konture.failOnSeverity = Severity.WARNING
                threadThreshold = Konture.failOnSeverity
                latch.countDown()
            }

        thread.start()
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        assertEquals(Severity.WARNING, threadThreshold)
        assertEquals(Severity.ERROR, Konture.failOnSeverity)
    }

    @Test
    fun `test report accumulator records sub-threshold violations in JSON report`(
        @TempDir tempDir: Path,
    ) {
        val jsonReportFile = File(tempDir.toFile(), "konture-report.json")
        Konture.jsonReportPath = jsonReportFile.absolutePath
        Konture.failOnSeverity = Severity.ERROR

        val warningRule =
            rule("classes.warning.test") {
                severity = Severity.WARNING
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Warning violation in module")
                    }
                }
            }

        val infoRule =
            rule("classes.info.test") {
                severity = Severity.INFO
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Info violation in module")
                    }
                }
            }

        assertDoesNotThrow { warningRule.check() }
        assertDoesNotThrow { infoRule.check() }

        assertTrue(jsonReportFile.exists(), "JSON report file should be generated")
        val jsonContent = jsonReportFile.readText()
        assertTrue(jsonContent.contains("\"warningCount\": 3"))
        assertTrue(jsonContent.contains("\"infoCount\": 3"))
        assertTrue(jsonContent.contains("\"errorCount\": 0"))
        assertTrue(jsonContent.contains("\"classes.warning.test\""))
        assertTrue(jsonContent.contains("\"classes.info.test\""))
    }

    @Test
    fun `test baseline generation records violations of all severities without failing`(
        @TempDir tempDir: Path,
    ) {
        val baselineFile = File(tempDir.toFile(), "konture-baseline.json")
        Konture.baselinePath = baselineFile.absolutePath
        Konture.generateBaseline = true
        Konture.failOnSeverity = Severity.ERROR

        val errorRule =
            rule("classes.error.baseline") {
                severity = Severity.ERROR
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Error violation for baseline")
                    }
                }
            }

        val warningRule =
            rule("classes.warning.baseline") {
                severity = Severity.WARNING
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Warning violation for baseline")
                    }
                }
            }

        val infoRule =
            rule("classes.info.baseline") {
                severity = Severity.INFO
                classes {
                    should().satisfy { _, violations ->
                        violations.add("Info violation for baseline")
                    }
                }
            }

        assertDoesNotThrow { errorRule.check() }
        assertDoesNotThrow { warningRule.check() }
        assertDoesNotThrow { infoRule.check() }

        io.github.baole.konture.impl.BaselineManager.writeBaseline()

        assertTrue(baselineFile.exists(), "Baseline file should be generated")
        val baselineContent = baselineFile.readText()
        assertTrue(baselineContent.contains("Error violation for baseline"))
        assertTrue(baselineContent.contains("Warning violation for baseline"))
        assertTrue(baselineContent.contains("Info violation for baseline"))
    }
}
