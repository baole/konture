/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.rule

import io.github.baole.konture.Konture
import io.github.baole.konture.architecture
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.rule
import io.github.baole.konture.tests.utils.violationsFound
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NamedRuleIntegrationTest {

    @Test
    fun `named rule evaluates against real compiled bytecode when valid`() {
        val myRule =
            rule("integration.classes.named.rule") {
                description = "Classes in declarative package must reside in package"
                severity = Severity.WARNING
                tag("integration", "architecture")

                classes {
                    that().haveName("DeclarativeClass")
                    should().resideInAPackage("io.github.baole.konture.tests.declarative..")
                }
            }

        assertEquals("integration.classes.named.rule", myRule.metadata.id)
        assertEquals("Classes in declarative package must reside in package", myRule.metadata.description)
        assertEquals(Severity.WARNING, myRule.metadata.severity)
        assertTrue(myRule.metadata.tags.contains("integration"))

        myRule.check()
    }

    @Test
    fun `named rule reports failure with rule id and description`() {
        val failingRule =
            rule("integration.failing.rule") {
                description = "DeclarativeClass should be an interface"
                severity = Severity.ERROR

                classes {
                    that().haveName("DeclarativeClass")
                    should().beInterfaces()
                }
            }

        val error = violationsFound {
            failingRule.check()
        }

        assertNotNull(error)
        val msg = error!!.message!!
        assertTrue(msg.contains("integration.failing.rule"), "Expected message to contain rule ID, but was: $msg")
        assertTrue(msg.contains("DeclarativeClass should be an interface"), "Expected message to contain rule description, but was: $msg")
        assertTrue(msg.contains("DeclarativeClass"), "Expected message to contain DeclarativeClass, but was: $msg")
    }

    @Test
    fun `architecture context supports named rules on real project graph`() {
        Konture.architecture {
            rule("integration.sub.rule") {
                description = "Sub-rule in architecture suite"

                classes {
                    that().haveName("DeclarativeClass")
                    should().resideInAPackage("io.github.baole.konture.tests.declarative..")
                }
            }
        }
    }
}
