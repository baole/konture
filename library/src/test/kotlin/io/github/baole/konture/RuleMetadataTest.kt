/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.Severity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class RuleMetadataTest : RuleBuildersTestBase() {
    @Test
    fun testRuleDslAndMetadataConstruction() {
        val ruleDef =
            rule("domain.repositories.must-be-interfaces") {
                description = "Domain repositories must be interfaces to enforce DIP"
                severity = Severity.WARNING
                tag("architecture", "domain", "dip")
            }

        val metadata = ruleDef.metadata
        assertEquals("domain.repositories.must-be-interfaces", metadata.id)
        assertEquals("Domain repositories must be interfaces to enforce DIP", metadata.description)
        assertEquals(Severity.WARNING, metadata.severity)
        assertEquals(setOf("architecture", "domain", "dip"), metadata.tags)
    }

    @Test
    fun testNamedRuleExecutionAndViolationMetadata() {
        val ruleDef =
            rule("core.services.naming") {
                description = "Core services must end with Service suffix"
                severity = Severity.ERROR
                tag("naming", "core")

                classes {
                    that().resideInAPackage("com.example")
                    should().haveNameEndingWith("Service")
                }
            }

        try {
            ruleDef.check()
            fail("Expected AssertionError due to naming violation")
        } catch (e: AssertionError) {
            val msg = e.message ?: ""
            assertTrue(msg.contains("core.services.naming") || msg.contains("ClassA") || msg.contains("ClassB"))
        }
    }

    @Test
    fun testArchitectureContextWithNamedRule() {
        try {
            architecture {
                rule("app.classes.must-have-suffix") {
                    description = "All classes in com.other must be interfaces"
                    severity = Severity.WARNING
                    tag("documentation")

                    classes {
                        that().resideInAPackage("com.other")
                        should().beInterfaces()
                    }
                }
            }
            fail("Expected AssertionError due to violation in architecture rule")
        } catch (e: AssertionError) {
            val msg = e.message ?: ""
            assertTrue(msg.contains("app.classes.must-have-suffix") || msg.contains("ClassC"))
        }
    }

    @Test
    fun testRuleVerifyAlias() {
        val ruleDef =
            rule("verify.test.rule") {
                description = "Rule testing verify alias"
                classes {
                    that().resideInAPackage("com.example").and().areInterfaces()
                    should().beInterfaces()
                }
            }

        // Passes since classB is interface
        ruleDef.verify()
    }

    @Test
    fun testAllSubRuleBuildersInRuleDsl() {
        var filesExecuted = false
        var functionsExecuted = false
        var modulesExecuted = false
        var propertiesExecuted = false
        var slicesExecuted = false
        var layeredExecuted = false
        var layeredArchExecuted = false

        val ruleDef =
            rule("all.builders.rule") {
                description = "Testing all sub-rule builder registration"

                files {
                    that().resideInAPackage("non.existent")
                    allowEmpty()
                    should().beAnnotatedWith(Deprecated::class)
                    filesExecuted = true
                }

                functions {
                    that().resideInAPackage("non.existent")
                    allowEmpty()
                    should().beAnnotatedWith(Deprecated::class)
                    functionsExecuted = true
                }

                modules {
                    that().haveName("non.existent")
                    allowEmpty()
                    should().beFreeOfCycles()
                    modulesExecuted = true
                }

                properties {
                    that().resideInAPackage("non.existent")
                    allowEmpty()
                    should().beAnnotatedWith(Deprecated::class)
                    propertiesExecuted = true
                }

                slices {
                    matching("com.(*)..")
                    allowEmpty()
                    should().beFreeOfCycles()
                    slicesExecuted = true
                }

                layered {
                    val foo = layer("Foo") definedBy "com.example"
                    val bar = layer("Bar") definedBy "com.other"
                    where(foo) { mayOnlyAccessLayers(foo, bar) }
                    layeredExecuted = true
                }

                layeredArchitecture {
                    layer("Foo").definedBy("com.example")
                    layer("Bar").definedBy("com.other")
                    whereLayer("Foo").mayOnlyAccessLayers("Bar")
                    layeredArchExecuted = true
                }
            }

        ruleDef.check()

        assertTrue(filesExecuted)
        assertTrue(functionsExecuted)
        assertTrue(modulesExecuted)
        assertTrue(propertiesExecuted)
        assertTrue(slicesExecuted)
        assertTrue(layeredExecuted)
        assertTrue(layeredArchExecuted)
    }

    @Test
    fun testSourceSetOverloadsInRuleBuilder() {
        var classesSsExecuted = false
        var filesSsExecuted = false
        var functionsSsExecuted = false
        var modulesSsExecuted = false
        var propertiesSsExecuted = false
        var slicesSsExecuted = false

        val prodSelector = SourceSets.production()

        val ruleDef =
            rule("sourceset.overloads.rule") {
                classes(prodSelector) {
                    that().resideInAPackage("non.existent")
                    allowEmpty()
                    should().beInterfaces()
                    classesSsExecuted = true
                }
                files(prodSelector) {
                    that().resideInAPackage("non.existent")
                    allowEmpty()
                    should().beAnnotatedWith(Deprecated::class)
                    filesSsExecuted = true
                }
                functions(prodSelector) {
                    that().resideInAPackage("non.existent")
                    allowEmpty()
                    should().beAnnotatedWith(Deprecated::class)
                    functionsSsExecuted = true
                }
                modules(prodSelector) {
                    that().haveName("non.existent")
                    allowEmpty()
                    should().beFreeOfCycles()
                    modulesSsExecuted = true
                }
                properties(prodSelector) {
                    that().resideInAPackage("non.existent")
                    allowEmpty()
                    should().beAnnotatedWith(Deprecated::class)
                    propertiesSsExecuted = true
                }
                slices(prodSelector) {
                    matching("com.(*)..")
                    allowEmpty()
                    should().beFreeOfCycles()
                    slicesSsExecuted = true
                }
            }

        ruleDef.check()

        assertTrue(classesSsExecuted)
        assertTrue(filesSsExecuted)
        assertTrue(functionsSsExecuted)
        assertTrue(modulesSsExecuted)
        assertTrue(propertiesSsExecuted)
        assertTrue(slicesSsExecuted)
    }
}
