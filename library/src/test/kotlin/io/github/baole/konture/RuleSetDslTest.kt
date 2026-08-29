/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for the reusable rule set DSL (issue #74).
 *
 * Covers [architectureRules] builder, [KontureContext.apply] of rule sets,
 * custom extension functions on [RuleSetBuilder], and composition/reuse.
 */
class RuleSetDslTest : RuleBuildersTestBase() {
    // ------------------------------------------------------------------
    // AC 1: architectureRules { ... } creates reusable RuleSet objects
    // ------------------------------------------------------------------

    @Test
    fun `architectureRules creates a non-null RuleSet`() {
        val ruleSet =
            architectureRules {
                classes {
                    that().nameStartsWith("ClassA")
                    should().inPackage("com.example")
                }
            }
        assertTrue(ruleSet is RuleSet)
    }

    @Test
    fun `empty architectureRules creates a valid RuleSet`() {
        val ruleSet = architectureRules { }
        assertTrue(ruleSet is RuleSet)
    }

    // ------------------------------------------------------------------
    // AC 2: architecture { apply(ruleSet) } imports and executes rule sets
    // ------------------------------------------------------------------

    @Test
    fun `apply executes a passing rule set without throwing`() {
        val ruleSet =
            architectureRules {
                classes {
                    that().nameStartsWith("ClassA")
                    should().inPackage("com.example")
                }
                modules {
                    that().haveNamePath(":moduleA")
                    should().satisfy { it.appliedPlugins.contains("kotlin") }
                }
            }
        assertDoesNotThrow {
            architecture {
                apply(ruleSet)
            }
        }
    }

    @Test
    fun `apply executes a failing rule set and throws AssertionError`() {
        val ruleSet =
            architectureRules {
                classes {
                    that().nameStartsWith("ClassA")
                    should().inPackage("com.wrong")
                }
            }
        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    apply(ruleSet)
                }
            }
        assertTrue(error.message!!.contains("[classes]"))
    }

    @Test
    fun `apply aggregates violations from multiple suites in the rule set`() {
        val ruleSet =
            architectureRules {
                modules {
                    that().haveNamePath(":moduleA")
                    should().satisfy { it.appliedPlugins.contains("java") }
                }
                classes {
                    that().nameStartsWith("ClassA")
                    should().inPackage("com.wrong")
                }
            }
        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    apply(ruleSet)
                }
            }
        assertTrue(error.message!!.contains("[modules]"))
        assertTrue(error.message!!.contains("[classes]"))
        assertTrue(error.message!!.contains("2 suite(s)"))
    }

    // ------------------------------------------------------------------
    // Rule sets containing layer policies
    // ------------------------------------------------------------------

    @Test
    fun `apply replays layer policies declared in the rule set`() {
        val dependingClassA =
            classA.copy(
                imports = listOf("com.other.ClassC"),
                referencedTypes = setOf("ClassC"),
            )
        val fileA = FileDeclaration("ClassA.kt", "com.example", classes = listOf(dependingClassA))
        ProjectGraph(builds = mapOf(":" to listOf(moduleA.copy(files = listOf(fileA)), moduleB, moduleC)))
            .also { ProjectGraph.setDefault(it) }

        val ruleSet =
            architectureRules {
                layer("presentation") {
                    selector { packages("com.example") }
                    mayDependOn("domain")
                }
                layer("domain") {
                    selector { packages("com.other") }
                }
            }
        assertDoesNotThrow {
            architecture {
                apply(ruleSet)
            }
        }
    }

    @Test
    fun `apply replays layer policies and detects violations`() {
        val dependingClassA =
            classA.copy(
                imports = listOf("com.other.ClassC"),
                referencedTypes = setOf("ClassC"),
            )
        val fileA = FileDeclaration("ClassA.kt", "com.example", classes = listOf(dependingClassA))
        ProjectGraph(builds = mapOf(":" to listOf(moduleA.copy(files = listOf(fileA)), moduleB, moduleC)))
            .also { ProjectGraph.setDefault(it) }

        val ruleSet =
            architectureRules {
                layer("presentation") {
                    selector { packages("com.example") }
                    mayDependOn("core")
                }
                layer("domain") {
                    selector { packages("com.other") }
                }
                layer("core") {
                    selector { packages("com.core") }
                }
            }
        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    apply(ruleSet)
                }
            }
        assertTrue(error.message!!.contains("may only depend on layers [core]"))
    }

    // ------------------------------------------------------------------
    // Support custom extension functions on RuleSetBuilder
    // ------------------------------------------------------------------

    @Test
    fun `custom extension function on RuleSetBuilder is supported`() {
        val ruleSet =
            architectureRules {
                cleanArchitectureLayers()
            }
        // The custom extension declares two layers; with the base fixture graph
        // (no cross-layer deps), it should pass.
        assertDoesNotThrow {
            architecture {
                apply(ruleSet)
            }
        }
    }

    @Test
    fun `custom extension function combining multiple rule types is supported`() {
        val ruleSet =
            architectureRules {
                noAbstractClasses()
            }
        // moduleC has ClassC which is abstract — should fail.
        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    apply(ruleSet)
                }
            }
        assertTrue(error.message!!.contains("[classes]"))
    }

    // ------------------------------------------------------------------
    // Composition & reuse
    // ------------------------------------------------------------------

    @Test
    fun `multiple rule sets can be composed in a single architecture block`() {
        val passingRules =
            architectureRules {
                classes {
                    that().nameStartsWith("ClassA")
                    should().inPackage("com.example")
                }
            }
        val failingRules =
            architectureRules {
                modules {
                    that().haveNamePath(":moduleA")
                    should().satisfy { it.appliedPlugins.contains("java") }
                }
            }
        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    apply(passingRules)
                    apply(failingRules)
                }
            }
        assertTrue(error.message!!.contains("[modules]"))
    }

    @Test
    fun `a rule set can be reused across multiple architecture blocks`() {
        val ruleSet =
            architectureRules {
                classes {
                    that().nameStartsWith("ClassA")
                    should().inPackage("com.example")
                }
            }
        // First use
        assertDoesNotThrow {
            architecture {
                apply(ruleSet)
            }
        }
        // Second use — same rule set object, should still work
        assertDoesNotThrow {
            architecture {
                apply(ruleSet)
            }
        }
    }

    @Test
    fun `rule set can combine inline rules and applied rule sets`() {
        val sharedRules =
            architectureRules {
                modules {
                    that().haveNamePath(":moduleA")
                    should().satisfy { it.appliedPlugins.contains("kotlin") }
                }
            }
        assertDoesNotThrow {
            architecture {
                apply(sharedRules)
                classes {
                    that().nameStartsWith("ClassA")
                    should().inPackage("com.example")
                }
            }
        }
    }

    @Test
    fun `apply with empty rule set does nothing`() {
        val empty = architectureRules { }
        assertDoesNotThrow {
            architecture {
                apply(empty)
                classes {
                    that().nameStartsWith("ClassA")
                    should().inPackage("com.example")
                }
            }
        }
    }

    @Test
    fun `named rules inside a rule set are replayed correctly`() {
        val ruleSet =
            architectureRules {
                rule("custom-no-abstract") {
                    description = "No abstract classes"
                    classes {
                        that().inPackage("com.other")
                        should().satisfy { !it.isAbstract }
                    }
                }
            }
        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    apply(ruleSet)
                }
            }
        assertTrue(error.message!!.contains("custom-no-abstract"))
    }
}

// ------------------------------------------------------------------
// Custom extension functions on RuleSetBuilder
// ------------------------------------------------------------------

/** Example custom rule-set extension: declares clean-architecture-style layers. */
private fun RuleSetBuilder.cleanArchitectureLayers() {
    layer("presentation") {
        selector { packages("com.example") }
    }
    layer("domain") {
        selector { packages("com.other") }
    }
}

/** Example custom rule-set extension: forbids abstract classes. */
private fun RuleSetBuilder.noAbstractClasses() {
    classes {
        that().inPackage("com.other")
        should().satisfy { !it.isAbstract }
    }
}
