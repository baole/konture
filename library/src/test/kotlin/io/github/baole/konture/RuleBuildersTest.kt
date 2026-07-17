/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RuleBuildersTest : RuleBuildersTestBase() {
    @Test
    fun `test matchesSimpleGlob`() {
        val matches = { pattern: String, input: String ->
            PatternMatchers
                .matchesSimpleGlob(pattern, input)
        }
        assertTrue(matches("*UseCase", "GetUserUseCase"))
        assertTrue(matches("*UseCase*", "GetUserUseCaseImpl"))
        assertTrue(matches("GetUser*", "GetUserUseCase"))
        assertTrue(matches("GetUserUseCase", "GetUserUseCase"))
        assertFalse(matches("*UseCase", "GetUserUseCaseImpl"))
        assertFalse(matches("GetUser*", "GetProjectUseCase"))
    }

    @Test
    fun `test unified architecture block with new rule builders`() {
        // Assert that the architecture block executes and successfully detects violations
        assertThrows(AssertionError::class.java) {
            Konture.architecture {
                files {
                    that().resideInAPackage("com.example")
                    should().beDocumentedWithKDoc()
                }
            }
        }
    }

    @Test
    fun `test complex architecture block with multiple builders passing`() {
        // Since we are running in the context of our setUp graph,
        // let's set up rules that actually pass so we can verify success.
        Konture.architecture {
            modules {
                that().haveNamePath(":moduleA")
                should().satisfy { module -> module.appliedPlugins.contains("kotlin") }
            }
            classes {
                that().haveNameStartingWith("ClassA")
                should().resideInAPackage("com.example")
            }
            files {
                that().haveNameEndingWith("ClassB.kt")
                should().resideInAPackage("com.example")
            }
        }
    }

    @Test
    fun `test complex architecture block with one failing rule`() {
        val error =
            assertThrows(AssertionError::class.java) {
                Konture.architecture {
                    modules {
                        that().haveNamePath(":moduleA")
                        should().satisfy { module -> module.appliedPlugins.contains("java") } // moduleA only has kotlin
                    }
                    classes {
                        that().haveNameStartingWith("ClassA")
                        should().resideInAPackage("com.wrong") // also fails
                    }
                }
            }
        assertTrue(error.message!!.contains("[modules]"))
        assertTrue(error.message!!.contains("[classes]"))
        assertTrue(error.message!!.contains("2 suite(s)"))
    }
}
