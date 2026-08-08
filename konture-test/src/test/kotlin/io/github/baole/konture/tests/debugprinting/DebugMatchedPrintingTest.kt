/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.debugprinting

import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.FileDeclarationContext
import io.github.baole.konture.FunctionDeclarationContext
import io.github.baole.konture.Konture
import io.github.baole.konture.Module
import io.github.baole.konture.PropertyDeclarationContext
import io.github.baole.konture.Slice
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DebugMatchedPrintingTest {

    @Test
    fun `printMatchedClasses logs matched classes to custom logger`() {
        val matched = mutableListOf<ClassDeclaration>()
        Konture.classes {
            that().haveName("DebugClass")
            printMatchedClasses { matched.add(it) }
            should().haveName("DebugClass")
        }
        assertTrue(matched.any { it.name == "DebugClass" })
    }

    @Test
    fun `printMatchedFiles logs matched files to custom logger`() {
        val matched = mutableListOf<FileDeclarationContext>()
        Konture.files {
            that().haveNameMatching("DebugPrintingTargets.kt")
            printMatchedFiles { matched.add(it) }
            should().containClass(DebugClass::class)
        }
        assertTrue(matched.any { it.declaration.name == "DebugPrintingTargets.kt" })
    }

    @Test
    fun `printMatchedFunctions logs matched functions to custom logger`() {
        val matched = mutableListOf<FunctionDeclarationContext>()
        Konture.functions {
            that().haveName("debugFunc")
            printMatchedFunctions { matched.add(it) }
            should().haveName("debugFunc")
        }
        assertTrue(matched.any { it.declaration.name == "debugFunc" })
    }

    @Test
    fun `printMatchedProperties logs matched properties to custom logger`() {
        val matched = mutableListOf<PropertyDeclarationContext>()
        Konture.properties {
            that().haveName("debugProp")
            printMatchedProperties { matched.add(it) }
            should().haveName("debugProp")
        }
        assertTrue(matched.any { it.declaration.name == "debugProp" })
    }

    @Test
    fun `printMatchedModules logs matched modules to custom logger`() {
        val matched = mutableListOf<Module>()
        Konture.modules {
            that().haveNamePath(":konture-test")
            printMatchedModules { matched.add(it) }
            should().onlyDependOnModules(":core")
        }
        assertTrue(matched.any { it.path == ":konture-test" })
    }

    @Test
    fun `printMatchedSlices logs matched slices to custom logger`() {
        val matched = mutableListOf<Slice>()
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("debugprinting")
            printMatchedSlices { matched.add(it) }
            should().containClassesWithAnnotation(DebugMarker::class)
        }
        assertTrue(matched.any { it.key == "debugprinting" })
    }
}
