/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RuleSetTest : RuleBuildersTestBase() {
    private fun graphWith(modules: List<Module>): ProjectGraph {
        return ProjectGraph(builds = mapOf(":" to modules)).also { ProjectGraph.setDefault(it) }
    }

    @Test
    fun `architectureRules builder creates RuleSet with metadata`() {
        val ruleSet =
            architectureRules("clean-architecture") {
                description = "Enforces clean architecture boundaries"
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.example")
                }
            }

        assertEquals("clean-architecture", ruleSet.name)
        assertEquals("Enforces clean architecture boundaries", ruleSet.description)
    }

    @Test
    fun `architecture block executes applied RuleSet successfully`() {
        val cleanArchitecture =
            architectureRules {
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.example")
                }
                files {
                    that().haveNameEndingWith("ClassB.kt")
                    should().resideInAPackage("com.example")
                }
                noCycles()
            }

        assertDoesNotThrow {
            architecture {
                apply(cleanArchitecture)
            }
        }
    }

    @Test
    fun `RuleSet check and verify run standalone`() {
        val passingRuleSet =
            architectureRules("passing-suite") {
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.example")
                }
            }

        assertDoesNotThrow {
            passingRuleSet.check()
        }

        assertDoesNotThrow {
            passingRuleSet.verify()
        }
    }

    @Test
    fun `custom extension functions on RuleSetBuilder work seamlessly`() {
        // Define custom extension functions on RuleSetBuilder
        fun RuleSetBuilder.domainIsolation() {
            classes {
                that().haveNameStartingWith("ClassA")
                should().resideInAPackage("com.example")
            }
        }

        fun RuleSetBuilder.repositoryInterfaces() {
            classes {
                allowEmpty().that().haveNameEndingWith("Repository")
                should().beInterfaces()
            }
        }

        val cleanArchitecture =
            architectureRules {
                domainIsolation()
                repositoryInterfaces()
                noCycles()
            }

        assertDoesNotThrow {
            architecture {
                apply(cleanArchitecture)
            }
        }
    }

    @Test
    fun `failing rules in RuleSet trigger aggregated assertion error`() {
        val strictArchitecture =
            architectureRules("strict") {
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.nonexistent")
                }
                modules {
                    that().haveNamePath(":moduleA")
                    should().satisfy { module -> module.appliedPlugins.contains("nonexistent-plugin") }
                }
            }

        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    apply(strictArchitecture)
                }
            }

        assertTrue(error.message!!.contains("[classes]"))
        assertTrue(error.message!!.contains("[modules]"))
        assertTrue(error.message!!.contains("2 suite(s)"))
    }

    @Test
    fun `RuleSet composition with plus operator combines rule suites`() {
        val ruleSetA =
            architectureRules("setA") {
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.example")
                }
            }

        val ruleSetB =
            architectureRules("setB") {
                files {
                    that().haveNameEndingWith("ClassB.kt")
                    should().resideInAPackage("com.example")
                }
            }

        val combined = ruleSetA + ruleSetB

        assertEquals("setA + setB", combined.name)

        assertDoesNotThrow {
            architecture {
                apply(combined)
            }
        }

        assertDoesNotThrow {
            combined.check()
        }
    }

    @Test
    fun `RuleSet composition via apply imports nested rule sets`() {
        val innerRules =
            architectureRules {
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.example")
                }
            }

        val outerRules =
            architectureRules {
                apply(innerRules)
                files {
                    that().haveNameEndingWith("ClassB.kt")
                    should().resideInAPackage("com.example")
                }
            }

        assertDoesNotThrow {
            outerRules.check()
        }
    }

    @Test
    fun `apply overloads for vararg, collection, and RuleDefinition`() {
        val ruleSet1 =
            architectureRules {
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.example")
                }
            }

        val ruleSet2 =
            architectureRules {
                files {
                    that().haveNameEndingWith("ClassB.kt")
                    should().resideInAPackage("com.example")
                }
            }

        val standaloneRule =
            rule("standalone-class-check") {
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.example")
                }
            }

        // Test vararg apply in architecture block
        assertDoesNotThrow {
            architecture {
                apply(ruleSet1, ruleSet2)
                apply(standaloneRule)
            }
        }

        // Test collection apply in architecture block
        assertDoesNotThrow {
            architecture {
                apply(listOf(ruleSet1, ruleSet2))
            }
        }

        // Test apply in RuleSetBuilder
        val composite =
            architectureRules {
                apply(ruleSet1, ruleSet2)
                apply(listOf(ruleSet1))
                apply(standaloneRule)
            }

        assertDoesNotThrow {
            composite.check()
        }
    }

    @Test
    fun `architectureRules supports layers, slices, and source sets`() {
        graphWith(listOf(moduleA, moduleB, moduleC))

        val layeredPolicy =
            architectureRules("layered-policy") {
                layer("presentation") {
                    selector { packages("com.example") }
                }
                layer("domain") {
                    selector { packages("com.other") }
                }
                slices(SourceSets.production()) {
                    allowEmpty().matching("com.(*)..").should().beFreeOfCycles()
                }
                sourceSet("commonMain") {
                    mayDependOn("commonMain")
                }
            }

        assertDoesNotThrow {
            architecture {
                apply(layeredPolicy)
            }
        }
    }

    @Test
    fun `Konture architectureRules extension methods work identically`() {
        val ruleSet =
            Konture.architectureRules("konture-rules") {
                classes {
                    that().haveNameStartingWith("ClassA")
                    should().resideInAPackage("com.example")
                }
            }

        assertEquals("konture-rules", ruleSet.name)
        assertDoesNotThrow {
            ruleSet.check()
        }
    }
}
