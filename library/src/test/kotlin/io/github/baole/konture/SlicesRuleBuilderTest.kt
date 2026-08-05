/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Octavio Calleya Garcia (@octaviospain), Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SlicesRuleBuilderTest {
    private fun classIn(
        pkg: String,
        name: String,
        imports: List<String> = emptyList(),
    ) = ClassDeclaration(
        name = name,
        fqName = "$pkg.$name",
        packageName = pkg,
        isInterface = false,
        isAbstract = false,
        annotations = emptyList(),
        imports = imports,
        referencedTypes = emptySet(),
        filePath = "/src/$name.kt",
    )

    private fun graphOf(vararg classes: ClassDeclaration): ProjectGraph {
        val files =
            classes.map {
                FileDeclaration(it.name + ".kt", it.packageName, classes = listOf(it), filePath = it.filePath)
            }
        val module =
            Module(
                buildId = ":",
                path = ":app",
                projectDir = "app",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = files,
            )
        return ProjectGraph(mapOf(":" to listOf(module)))
    }

    @Test
    fun `beFreeOfCycles detects a package cycle between slices`() {
        val graph =
            graphOf(
                classIn("com.app.a", "ServiceA", imports = listOf("com.app.b.ServiceB")),
                classIn("com.app.b", "ServiceB", imports = listOf("com.app.a.ServiceA")),
            )
        val error =
            assertThrows(AssertionError::class.java) {
                SlicesRuleBuilder(graph).matching("com.app.(*)..").should().beFreeOfCycles().check()
            }
        assertTrue(error.message!!.contains("Slice architecture violation(s) detected:"))
        assertTrue(
            error.message!!.contains("a -> b -> a"),
            "Expected the cycle path rendered from its smallest key, got: ${error.message}",
        )
    }

    @Test
    fun `beFreeOfCycles passes for acyclic slices`() {
        val graph =
            graphOf(
                classIn("com.app.a", "ServiceA", imports = listOf("com.app.b.ServiceB")),
                classIn("com.app.b", "ServiceB"),
            )
        assertDoesNotThrow {
            SlicesRuleBuilder(graph).matching("com.app.(*)..").should().beFreeOfCycles().check()
        }
    }

    @Test
    fun `notDependOnEachOther flags an inter-slice dependency`() {
        val graph =
            graphOf(
                classIn("com.app.a", "ServiceA", imports = listOf("com.app.b.ServiceB")),
                classIn("com.app.b", "ServiceB"),
            )
        val error =
            assertThrows(AssertionError::class.java) {
                SlicesRuleBuilder(graph).matching("com.app.(*)..").should().notDependOnEachOther().check()
            }
        assertTrue(error.message!!.contains("Slice 'a' should not depend on slice 'b'"))
    }

    @Test
    fun `notDependOnEachOther passes for isolated slices`() {
        val graph =
            graphOf(
                classIn("com.app.a", "ServiceA"),
                classIn("com.app.b", "ServiceB"),
            )
        assertDoesNotThrow {
            SlicesRuleBuilder(graph).matching("com.app.(*)..").should().notDependOnEachOther().check()
        }
    }

    @Test
    fun `empty selection throws unless allowEmpty is set`() {
        val graph = graphOf(classIn("com.app.a", "ServiceA"))
        assertThrows(AssertionError::class.java) {
            SlicesRuleBuilder(graph).matching("com.nomatch.(*)..").should().beFreeOfCycles().check()
        }
        assertDoesNotThrow {
            SlicesRuleBuilder(graph).matching("com.nomatch.(*)..").allowEmpty().should().beFreeOfCycles().check()
        }
    }

    @Test
    fun `cycle rendering is deterministic across runs`() {
        val graph =
            graphOf(
                classIn("com.app.a", "ServiceA", imports = listOf("com.app.b.ServiceB")),
                classIn("com.app.b", "ServiceB", imports = listOf("com.app.a.ServiceA")),
            )

        fun run(): String =
            assertThrows(AssertionError::class.java) {
                SlicesRuleBuilder(graph).matching("com.app.(*)..").should().beFreeOfCycles().check()
            }.message!!
        assertEquals(run(), run(), "Cycle violation text must be stable across runs for baseline matching")
    }

    @Test
    fun `chained assertions are all evaluated`() {
        // A cyclic, non-isolated graph: both beFreeOfCycles and notDependOnEachOther should fire,
        // proving the second should() does not discard the first.
        val graph =
            graphOf(
                classIn("com.app.a", "ServiceA", imports = listOf("com.app.b.ServiceB")),
                classIn("com.app.b", "ServiceB", imports = listOf("com.app.a.ServiceA")),
            )
        val error =
            assertThrows(AssertionError::class.java) {
                SlicesRuleBuilder(graph)
                    .matching("com.app.(*)..")
                    .should().beFreeOfCycles()
                    .should().notDependOnEachOther()
                    .check()
            }
        assertTrue(error.message!!.contains("a -> b -> a"), "Expected the cycle assertion to run")
        assertTrue(
            error.message!!.contains("should not depend on slice"),
            "Expected the isolation assertion to run as well",
        )
    }

    @Test
    fun `dependency on a package with classes only in subpackages resolves to its slice`() {
        // com.app.a depends on package com.app.b (e.g. a star import) whose classes live only in
        // the subpackage com.app.b.impl. The edge must still resolve to slice 'b'.
        val graph =
            graphOf(
                classIn("com.app.a", "ServiceA", imports = listOf("com.app.b.ServiceB")),
                classIn("com.app.b.impl", "ServiceB", imports = listOf("com.app.a.ServiceA")),
            )
        val error =
            assertThrows(AssertionError::class.java) {
                SlicesRuleBuilder(graph).matching("com.app.(*)..").should().beFreeOfCycles().check()
            }
        assertTrue(
            error.message!!.contains("a -> b -> a"),
            "Expected the cycle through the subpackage-backed slice, got: ${error.message}",
        )
    }

    @Test
    fun `sliceKeyFor captures one segment or many`() {
        assertEquals("payment", PatternMatchers.sliceKeyFor("com.acme.(*)..", "com.acme.payment"))
        assertEquals("payment", PatternMatchers.sliceKeyFor("com.acme.(*)..", "com.acme.payment.api"))
        assertNull(PatternMatchers.sliceKeyFor("com.acme.(*)..", "com.other.thing"))
        assertNull(PatternMatchers.sliceKeyFor("com.acme.(*)..", "com.acme"))
        assertEquals("payment.api", PatternMatchers.sliceKeyFor("com.acme.(**)", "com.acme.payment.api"))
    }

    @Test
    fun `test printMatchedSlices debugging helper`() {
        val printedSlices = mutableListOf<String>()
        val graph =
            graphOf(
                classIn("com.app.a", "ServiceA"),
                classIn("com.app.b", "ServiceB"),
            )

        SlicesRuleBuilder(graph)
            .matching("com.app.(*)..")
            .printMatchedSlices { printedSlices.add(it.key) }
            .should().beFreeOfCycles()
            .check()

        assertEquals(listOf("a", "b"), printedSlices.sorted())
    }
}
