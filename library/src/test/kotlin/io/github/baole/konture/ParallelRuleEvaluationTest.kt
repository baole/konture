/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.model.Severity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class ParallelRuleEvaluationTest : RuleBuildersTestBase() {
    @BeforeEach
    override fun setUp() {
        Konture.reset()
        super.setUp()
    }

    @AfterEach
    fun tearDown() {
        Konture.reset()
        System.clearProperty(Konture.PROPERTY_PARALLEL_ENABLED)
        System.clearProperty(Konture.PROPERTY_PARALLEL_MAX_WORKERS)
    }

    @Test
    fun `parallel execution defaults to false and runs sequentially`() {
        assertFalse(Konture.parallel)
        assertEquals(0, Konture.parallelMaxWorkers)

        Konture.architecture {
            classes {
                that().nameStartsWith("ClassA")
                should().inPackage("com.example")
            }
            modules {
                that().haveNamePath(":moduleA")
                should().satisfy { module -> module.appliedPlugins.contains("kotlin") }
            }
        }
    }

    @Test
    fun `parallel execution succeeds when all suites pass`() {
        Konture.parallel = true

        Konture.architecture {
            classes {
                that().nameStartsWith("ClassA")
                should().inPackage("com.example")
            }
            modules {
                that().haveNamePath(":moduleA")
                should().satisfy { module -> module.appliedPlugins.contains("kotlin") }
            }
            files {
                that().nameEndsWith("ClassB.kt")
                should().inPackage("com.example")
            }
        }
    }

    @Test
    fun `parallel execution deterministically aggregates failures in declaration order`() {
        Konture.parallel = true

        val error =
            assertThrows(AssertionError::class.java) {
                Konture.architecture {
                    // Suite 1: classes fails
                    classes {
                        that().nameStartsWith("ClassA")
                        should().inPackage("com.wrong")
                    }
                    // Suite 2: modules fails
                    modules {
                        that().haveNamePath(":moduleA")
                        should().satisfy { module -> module.appliedPlugins.contains("java") }
                    }
                    // Suite 3: files fails
                    files {
                        that().nameEndsWith("ClassB.kt")
                        should().inPackage("com.wrong")
                    }
                }
            }

        val message = requireNotNull(error.message)
        assertTrue(message.contains("Architecture validation failed in 3 suite(s):"))

        val indexClasses = message.indexOf("[classes]")
        val indexModules = message.indexOf("[modules]")
        val indexFiles = message.indexOf("[files]")

        assertTrue(indexClasses != -1)
        assertTrue(indexModules != -1)
        assertTrue(indexFiles != -1)
        assertTrue(indexClasses < indexModules, "Expected [classes] before [modules]")
        assertTrue(indexModules < indexFiles, "Expected [modules] before [files]")
    }

    @Test
    fun `parallel execution propagates runtime state to worker coroutines`() {
        Konture.parallel = true
        Konture.failOnSeverity = Severity.WARNING
        Konture.baselinePath = "custom/path/baseline.json"

        val observedSeverities = ConcurrentHashMap<String, Severity?>()
        val observedBaselinePaths = ConcurrentHashMap<String, String>()

        Konture.architecture {
            classes {
                that().nameStartsWith("ClassA")
                should().satisfy {
                    observedSeverities["classes"] = Konture.failOnSeverity
                    observedBaselinePaths["classes"] = Konture.baselinePath
                    true
                }
            }
            modules {
                that().haveNamePath(":moduleA")
                should().satisfy {
                    observedSeverities["modules"] = Konture.failOnSeverity
                    observedBaselinePaths["modules"] = Konture.baselinePath
                    true
                }
            }
            files {
                that().nameEndsWith("ClassB.kt")
                should().satisfy {
                    observedSeverities["files"] = Konture.failOnSeverity
                    observedBaselinePaths["files"] = Konture.baselinePath
                    true
                }
            }
        }

        assertEquals(Severity.WARNING, observedSeverities["classes"])
        assertEquals(Severity.WARNING, observedSeverities["modules"])
        assertEquals(Severity.WARNING, observedSeverities["files"])

        assertEquals("custom/path/baseline.json", observedBaselinePaths["classes"])
        assertEquals("custom/path/baseline.json", observedBaselinePaths["modules"])
        assertEquals("custom/path/baseline.json", observedBaselinePaths["files"])
    }

    @Test
    fun `parallel execution respects maxWorkers thread limit`() {
        Konture.parallel = true
        Konture.parallelMaxWorkers = 2

        val activeWorkers = AtomicInteger(0)
        val maxSimultaneousWorkers = AtomicInteger(0)

        Konture.architecture {
            repeat(10) { idx ->
                classes {
                    that().nameStartsWith("ClassA")
                    should().satisfy {
                        val current = activeWorkers.incrementAndGet()
                        maxSimultaneousWorkers.updateAndGet { prev -> maxOf(prev, current) }
                        Thread.sleep(15)
                        activeWorkers.decrementAndGet()
                        true
                    }
                }
            }
        }

        assertTrue(
            maxSimultaneousWorkers.get() in 1..2,
            "Simultaneous workers (${maxSimultaneousWorkers.get()}) should be between 1 and maxWorkers limit (2)",
        )
    }

    @Test
    fun `parallel execution configured via system properties`() {
        System.setProperty(Konture.PROPERTY_PARALLEL_ENABLED, "true")
        System.setProperty(Konture.PROPERTY_PARALLEL_MAX_WORKERS, "3")

        assertTrue(Konture.parallel)
        assertEquals(3, Konture.parallelMaxWorkers)

        val executingThreads = ConcurrentHashMap.newKeySet<String>()
        val observedWorkerParallelState = AtomicInteger(0)

        Konture.architecture {
            classes {
                that().nameStartsWith("ClassA")
                should().satisfy {
                    executingThreads.add(Thread.currentThread().name)
                    if (io.github.baole.konture.impl.KontureRuntimeStateProvider.currentState.parallel) {
                        observedWorkerParallelState.incrementAndGet()
                    }
                    true
                }
            }
            modules {
                that().haveNamePath(":moduleA")
                should().satisfy {
                    executingThreads.add(Thread.currentThread().name)
                    if (io.github.baole.konture.impl.KontureRuntimeStateProvider.currentState.parallel) {
                        observedWorkerParallelState.incrementAndGet()
                    }
                    true
                }
            }
        }

        assertEquals(
            2,
            observedWorkerParallelState.get(),
            "Both worker suites should observe parallel = true in runtime state",
        )
        assertTrue(
            executingThreads.any { it.contains("DefaultDispatcher") || it.contains("worker") },
            "Expected execution on dispatcher worker threads, got: $executingThreads",
        )
    }

    @Test
    fun `parallel execution propagates unexpected runtime exceptions without swallowing`() {
        Konture.parallel = true

        val error =
            assertThrows(IllegalStateException::class.java) {
                Konture.architecture {
                    classes {
                        that().nameStartsWith("ClassA")
                        should().satisfy {
                            throw IllegalStateException("Simulated unexpected predicate failure")
                        }
                    }
                    modules {
                        that().haveNamePath(":moduleA")
                        should().satisfy { module -> module.appliedPlugins.contains("kotlin") }
                    }
                }
            }

        assertEquals("Simulated unexpected predicate failure", error.message)
    }

    @Test
    fun `parallel execution rejects negative maxWorkers programmatically and clamps negative system property`() {
        var caughtException: IllegalArgumentException? = null
        try {
            Konture.parallelMaxWorkers = -1
        } catch (e: IllegalArgumentException) {
            caughtException = e
        }
        org.junit.jupiter.api.Assertions.assertNotNull(
            caughtException,
            "Expected IllegalArgumentException when setting negative parallelMaxWorkers",
        )

        System.setProperty(Konture.PROPERTY_PARALLEL_MAX_WORKERS, "-3")
        assertEquals(0, Konture.parallelMaxWorkers)
    }
}
