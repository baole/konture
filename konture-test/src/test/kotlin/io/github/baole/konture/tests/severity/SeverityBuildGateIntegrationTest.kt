/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.severity

import io.github.baole.konture.Konture
import io.github.baole.konture.architecture
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.rule
import io.github.baole.konture.tests.utils.violationsFound
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SeverityBuildGateIntegrationTest {

    @BeforeEach
    @AfterEach
    fun resetState() {
        System.clearProperty(Konture.PROPERTY_FAIL_ON_SEVERITY)
        Konture.reset()
    }

    @Test
    fun `default threshold ERROR allows WARNING and INFO violations on real bytecode without failing`() {
        val warningRule =
            rule("integration.warning.threshold.rule") {
                description = "DeclarativeClass should be an interface (warning)"
                severity = Severity.WARNING

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        val infoRule =
            rule("integration.info.threshold.rule") {
                description = "DeclarativeClass should be an interface (info)"
                severity = Severity.INFO

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        assertDoesNotThrow { warningRule.check() }
        assertDoesNotThrow { infoRule.check() }
    }

    @Test
    fun `default threshold ERROR fails on ERROR violations on real bytecode`() {
        val errorRule =
            rule("integration.error.threshold.rule") {
                description = "DeclarativeClass should be an interface (error)"
                severity = Severity.ERROR

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        val error = violationsFound {
            errorRule.check()
        }

        assertNotNull(error, "Expected AssertionError for ERROR rule violation")
        val msg = error!!.message!!
        assertTrue(msg.contains("DeclarativeClass should be an interface (error)"), "Message: $msg")
    }

    @Test
    fun `threshold WARNING fails on WARNING and ERROR but passes on INFO`() {
        Konture.failOnSeverity = Severity.WARNING

        val infoRule =
            rule("integration.warning.threshold.info") {
                description = "DeclarativeClass info rule"
                severity = Severity.INFO

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        val warningRule =
            rule("integration.warning.threshold.warning") {
                description = "DeclarativeClass warning rule"
                severity = Severity.WARNING

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        assertDoesNotThrow { infoRule.check() }

        val error = violationsFound {
            warningRule.check()
        }
        assertNotNull(error, "Expected AssertionError when failOnSeverity is WARNING and rule is WARNING")
    }

    @Test
    fun `threshold INFO fails on INFO violations`() {
        Konture.failOnSeverity = Severity.INFO

        val infoRule =
            rule("integration.info.threshold.strict") {
                description = "DeclarativeClass strict info rule"
                severity = Severity.INFO

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        val error = violationsFound {
            infoRule.check()
        }
        assertNotNull(error, "Expected AssertionError when failOnSeverity is INFO and rule is INFO")
    }

    @Test
    fun `audit mode null allows ERROR violations to pass without throwing`() {
        Konture.failOnSeverity = null

        val errorRule =
            rule("integration.audit.mode.error") {
                description = "DeclarativeClass error rule in audit mode"
                severity = Severity.ERROR

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        assertDoesNotThrow { errorRule.check() }
        assertDoesNotThrow { errorRule.verify() }
    }

    @Test
    fun `architecture block respects severity threshold on real bytecode`() {
        Konture.failOnSeverity = Severity.ERROR

        // Architecture block with only WARNING violations passes
        assertDoesNotThrow {
            Konture.architecture {
                rule("integration.arch.warning.only") {
                    description = "Architecture block warning rule"
                    severity = Severity.WARNING

                    classes {
                        that().named("DeclarativeClass")
                        should().beInterfaces()
                    }
                }
            }
        }

        // Architecture block with ERROR violation fails
        val error = violationsFound {
            Konture.architecture {
                rule("integration.arch.error") {
                    description = "Architecture block error rule"
                    severity = Severity.ERROR

                    classes {
                        that().named("DeclarativeClass")
                        should().beInterfaces()
                    }
                }
            }
        }
        assertNotNull(error, "Expected AssertionError for architecture block containing ERROR violation")

        // In audit mode, architecture block with ERROR violation passes
        Konture.failOnSeverity = null
        assertDoesNotThrow {
            Konture.architecture {
                rule("integration.arch.audit.error") {
                    description = "Architecture block error rule in audit mode"
                    severity = Severity.ERROR

                    classes {
                        that().named("DeclarativeClass")
                        should().beInterfaces()
                    }
                }
            }
        }
    }

    @Test
    fun `system property konture_fail_on_severity configures threshold on real bytecode`() {
        val warningRule =
            rule("integration.sysprop.warning") {
                description = "DeclarativeClass warning rule for sysprop test"
                severity = Severity.WARNING

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        val errorRule =
            rule("integration.sysprop.error") {
                description = "DeclarativeClass error rule for sysprop test"
                severity = Severity.ERROR

                classes {
                    that().named("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        // 1. none / audit mode via system property
        System.setProperty(Konture.PROPERTY_FAIL_ON_SEVERITY, "none")
        assertDoesNotThrow { errorRule.check() }

        // 2. warning mode via system property
        System.setProperty(Konture.PROPERTY_FAIL_ON_SEVERITY, "warning")
        val warningError = violationsFound {
            warningRule.check()
        }
        assertNotNull(warningError, "Expected failure when sysprop is warning")

        // 3. Programmatic override takes precedence over system property
        Konture.failOnSeverity = Severity.ERROR
        assertDoesNotThrow { warningRule.check() }
    }
}
