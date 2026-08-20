/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.customsatisfy

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CustomSatisfyTest {

    private val pkg = "io.github.baole.konture.tests.customsatisfy"

    @Test
    fun `classes satisfy on that and should`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().satisfy { it.name == "CustomSatisfyClass" }
            should().satisfy { cls, _ -> cls.name == "CustomSatisfyClass" }
        }
    }

    @Test
    fun `classes satisfy with structured context and rule metadata`() {
        var evaluatedCount = 0
        Konture.classes {
            that().resideInAPackage(pkg)
            should().satisfy(
                id = "rule.class.structured",
                description = "Classes must pass structured validation",
            ) { cls ->
                assertEquals("rule.class.structured", id)
                assertEquals("Classes must pass structured validation", description)
                evaluatedCount++
                cls.name == "CustomSatisfyClass"
            }
        }
        assertTrue(evaluatedCount > 0)
    }

    @Test
    fun `files satisfy on that and should`() {
        Konture.files {
            that().resideInAPackage(pkg).and().satisfy { it.declaration.name == "SatisfyTargets.kt" }
            should().satisfy { file, _ -> file.declaration.name == "SatisfyTargets.kt" }
        }
    }

    @Test
    fun `files satisfy with structured context`() {
        var evaluated = false
        Konture.files {
            that().resideInAPackage(pkg)
            should().satisfy(
                id = "rule.file.structured",
                description = "Files structured context check",
            ) { f ->
                assertEquals("rule.file.structured", id)
                evaluated = true
                f.declaration.name.endsWith(".kt")
            }
        }
        assertTrue(evaluated)
    }

    @Test
    fun `functions satisfy on that and should`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().satisfy { it.declaration.name == "satisfyFunc" }
            should().satisfy { fn, _ -> fn.declaration.name == "satisfyFunc" }
        }
    }

    @Test
    fun `functions satisfy with structured context and custom violation`() {
        val error = assertThrows(AssertionError::class.java) {
            Konture.functions {
                that().resideInAPackage(pkg)
                should().satisfy(
                    id = "rule.fn.structured",
                    description = "Functions rule check",
                ) {
                    addViolation("Custom violation for function ${subject.declaration.name}")
                    false
                }
            }
        }

        assertTrue(error.message!!.contains("Custom violation for function"))
    }

    @Test
    fun `properties satisfy on that and should`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().satisfy { it.declaration.name == "satisfyProp" }
            should().satisfy { prop, _ -> prop.declaration.name == "satisfyProp" }
        }
    }

    @Test
    fun `properties satisfy with structured context`() {
        var evaluated = false
        Konture.properties {
            that().resideInAPackage(pkg)
            should().satisfy(
                id = "rule.prop.structured",
                description = "Properties structured check",
            ) { prop ->
                assertEquals("rule.prop.structured", id)
                evaluated = true
                prop.declaration.name == "satisfyProp"
            }
        }
        assertTrue(evaluated)
    }

    @Test
    fun `modules satisfy on that and should`() {
        Konture.modules {
            that().satisfy { it.path == ":konture-test" }
            should().satisfy { module, _ -> module.path == ":konture-test" }
        }
    }

    @Test
    fun `modules satisfy with structured context`() {
        var evaluated = false
        Konture.modules {
            that().satisfy { it.path == ":konture-test" }
            should().satisfy(
                id = "rule.mod.structured",
                description = "Modules structured check",
            ) { mod ->
                assertEquals("rule.mod.structured", id)
                evaluated = true
                mod.path == ":konture-test"
            }
        }
        assertTrue(evaluated)
    }

    @Test
    fun `slices satisfy on that and should`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().satisfy { it.key == "customsatisfy" }
            should().satisfy(
                id = "rule.slice.custom",
                description = "Custom slice assertion",
            ) { slices -> slices.isNotEmpty() }
        }
    }

    @Test
    fun `slices satisfy with structured context`() {
        var evaluated = false
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().satisfy { it.key == "customsatisfy" }
            should().satisfy(
                id = "rule.slice.structured",
                description = "Slice structured check",
            ) { slices ->
                assertEquals("rule.slice.structured", id)
                evaluated = true
                slices.any { it.key == "customsatisfy" }
            }
        }
        assertTrue(evaluated)
    }
}
