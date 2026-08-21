/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for the first-class architectural layer policy DSL declared directly
 * inside an `architecture { ... }` block (see issue #69).
 */
class ArchitectureLayerPolicyTest : RuleBuildersTestBase() {
    /** Returns [source] configured to depend on the class identified by [targetFqName]. */
    private fun dependingOn(
        source: ClassDeclaration,
        targetFqName: String,
        targetSimpleName: String,
    ): ClassDeclaration =
        source.copy(
            imports = listOf(targetFqName),
            referencedTypes = setOf(targetSimpleName),
        )

    private fun graphWith(modules: List<Module>): ProjectGraph {
        return ProjectGraph(builds = mapOf(":" to modules)).also { ProjectGraph.setDefault(it) }
    }

    @Test
    fun `mayDependOn permits dependency to an allowed layer`() {
        val graph =
            graphWith(
                listOf(
                    moduleA.copy(
                        files =
                            listOf(
                                FileDeclaration(
                                    "ClassA.kt",
                                    "com.example",
                                    classes = listOf(dependingOn(classA, "com.other.ClassC", "ClassC")),
                                ),
                            ),
                    ),
                    moduleB,
                    moduleC,
                ),
            )
        assertDoesNotThrow {
            architecture {
                layer("presentation") {
                    selector { packages("com.example") }
                    mayDependOn("domain")
                }
                layer("domain") {
                    selector { packages("com.other") }
                }
            }
        }
        assertTrue(graph.getAllModules().isNotEmpty())
    }

    @Test
    fun `mayDependOn rejects dependency to a layer that is not allowed`() {
        graphWith(
            listOf(
                moduleA.copy(
                    files =
                        listOf(
                            FileDeclaration(
                                "ClassA.kt",
                                "com.example",
                                classes = listOf(dependingOn(classA, "com.other.ClassC", "ClassC")),
                            ),
                        ),
                ),
                moduleB,
                moduleC,
            ),
        )
        val exception =
            assertThrows(AssertionError::class.java) {
                architecture {
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
            }
        assertTrue(exception.message!!.contains("may only depend on layers [core]"))
        assertTrue(exception.message!!.contains("depends on com.other.ClassC"))
    }

    @Test
    fun `mayDependOn allows intra-layer and uncategorized dependencies`() {
        // ClassA depends on ClassB (same presentation layer) and ClassC (uncategorized module).
        val dependingClassA =
            dependingOn(classA, "com.example.ClassB", "ClassB")
                .copy(
                    imports = listOf("com.example.ClassB", "com.other.ClassC"),
                    referencedTypes = setOf("ClassB", "ClassC"),
                )
        val fileA = FileDeclaration("ClassA.kt", "com.example", classes = listOf(dependingClassA))
        graphWith(
            listOf(
                moduleA.copy(files = listOf(fileA)),
                moduleB,
                moduleC,
            ),
        )
        // domain is declared but empty (no classes match); ClassC is uncategorized.
        assertDoesNotThrow {
            architecture {
                layer("presentation") {
                    selector { packages("com.example") }
                    mayDependOn("domain")
                }
                layer("domain") {
                    selector { packages("com.acme.unused") }
                }
            }
        }
    }

    @Test
    fun `referencing an undefined layer is rejected`() {
        graphWith(listOf(moduleA, moduleB, moduleC))
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                architecture {
                    layer("presentation") {
                        selector { packages("com.example") }
                        mayDependOn("typoLayer")
                    }
                }
            }
        assertTrue(exception.message!!.contains("undefined layer"))
        assertTrue(exception.message!!.contains("typoLayer"))
    }

    @Test
    fun `mustNotDependOn rejects dependency to a forbidden layer`() {
        graphWith(
            listOf(
                moduleA.copy(
                    files =
                        listOf(
                            FileDeclaration(
                                "ClassA.kt",
                                "com.example",
                                classes = listOf(dependingOn(classA, "com.other.ClassC", "ClassC")),
                            ),
                        ),
                ),
                moduleB,
                moduleC,
            ),
        )
        val exception =
            assertThrows(AssertionError::class.java) {
                architecture {
                    layer("application") {
                        selector { modules(":moduleA") }
                        mustNotDependOn("data")
                    }
                    layer("data") {
                        selector { modules(":moduleC") }
                    }
                }
            }
        assertTrue(exception.message!!.contains("must not depend on layers [data]"))
        assertTrue(exception.message!!.contains("com.other.ClassC"))
    }

    @Test
    fun `module selector drives layer boundary enforcement`() {
        graphWith(
            listOf(
                moduleA.copy(
                    files =
                        listOf(
                            FileDeclaration(
                                "ClassA.kt",
                                "com.example",
                                classes = listOf(dependingOn(classA, "com.other.ClassC", "ClassC")),
                            ),
                        ),
                ),
                moduleB,
                moduleC,
            ),
        )
        // app(:moduleA) mayDependOn data(:moduleC); the ClassA -> ClassC edge is allowed.
        assertDoesNotThrow {
            architecture {
                layer("app") {
                    selector { modules(":moduleA") }
                    mayDependOn("data")
                }
                layer("data") {
                    selector { modules(":moduleC") }
                }
            }
        }
        // Same setup with mustNotDependOn data yields a violation.
        val exception =
            assertThrows(AssertionError::class.java) {
                architecture {
                    layer("app") {
                        selector { modules(":moduleA") }
                        mustNotDependOn("data")
                    }
                    layer("data") {
                        selector { modules(":moduleC") }
                    }
                }
            }
        assertTrue(exception.message!!.contains("must not depend on layers [data]"))
    }

    @Test
    fun `mayBeAccessedBy permits and rejects callers`() {
        // ClassA (com.example) depends on ClassC (com.other).
        val appClassA = dependingOn(classA, "com.other.ClassC", "ClassC")
        graphWith(
            listOf(
                moduleA.copy(files = listOf(FileDeclaration("ClassA.kt", "com.example", classes = listOf(appClassA)))),
                moduleB,
                moduleC,
            ),
        )

        assertDoesNotThrow {
            architecture {
                layer("presentation") {
                    selector { packages("com.example") }
                }
                layer("domain") {
                    selector { packages("com.other") }
                    mayBeAccessedBy("presentation")
                }
            }
        }

        val exception =
            assertThrows(AssertionError::class.java) {
                architecture {
                    layer("presentation") {
                        selector { packages("com.example") }
                    }
                    layer("domain") {
                        selector { packages("com.other") }
                        mayBeAccessedBy("data")
                    }
                    layer("data") {
                        selector { packages("com.data") }
                    }
                }
            }
        assertTrue(exception.message!!.contains("may only be accessed by layers [data]"))
        assertTrue(exception.message!!.contains("depends on com.other.ClassC"))
    }

    @Test
    fun `mustNotBeAccessedBy rejects a forbidden caller`() {
        graphWith(
            listOf(
                moduleA.copy(
                    files =
                        listOf(
                            FileDeclaration(
                                "ClassA.kt",
                                "com.example",
                                classes = listOf(dependingOn(classA, "com.other.ClassC", "ClassC")),
                            ),
                        ),
                ),
                moduleB,
                moduleC,
            ),
        )
        val exception =
            assertThrows(AssertionError::class.java) {
                architecture {
                    layer("presentation") {
                        selector { packages("com.example") }
                    }
                    layer("domain") {
                        selector { packages("com.other") }
                        mustNotBeAccessedBy("presentation")
                    }
                }
            }
        assertTrue(exception.message!!.contains("must not be accessed by layers [presentation]"))
        assertTrue(exception.message!!.contains("depends on com.other.ClassC"))
    }

    @Test
    fun `layer without a selector fails with IllegalArgumentException`() {
        graphWith(listOf(moduleA, moduleB, moduleC))
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                architecture {
                    layer("orphan") {
                        mayDependOn("domain")
                    }
                }
            }
        assertTrue(exception.message!!.contains("declares no selector"))
    }

    @Test
    fun `layer DSL works inside Konture architecture block with mixed suites`() {
        graphWith(listOf(moduleA, moduleB, moduleC))
        assertDoesNotThrow {
            Konture.architecture {
                layer("domain") {
                    selector { packages("com.other") }
                    mayDependOn("core")
                }
                layer("core") {
                    selector { packages("com.core") }
                }
            }
        }
    }
}
