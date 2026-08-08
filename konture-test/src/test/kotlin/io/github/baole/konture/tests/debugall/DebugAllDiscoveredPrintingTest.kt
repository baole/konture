/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.debugall

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

class DebugAllDiscoveredPrintingTest {

    @Test
    fun `printAllClasses logs all discovered classes to custom logger`() {
        val discovered = mutableListOf<ClassDeclaration>()
        Konture.classes {
            printAllClasses { discovered.add(it) }
            that().haveName("DebugAllClass")
            should().haveName("DebugAllClass")
        }
        assertTrue(discovered.any { it.name == "DebugAllClass" })
    }

    @Test
    fun `printAllFiles logs all discovered files to custom logger`() {
        val discovered = mutableListOf<FileDeclarationContext>()
        Konture.files {
            printAllFiles { discovered.add(it) }
            that().haveNameMatching("DebugAllTargets.kt")
            should().containClass(DebugAllClass::class)
        }
        assertTrue(discovered.any { it.declaration.name == "DebugAllTargets.kt" })
    }

    @Test
    fun `printAllFunctions logs all discovered functions to custom logger`() {
        val discovered = mutableListOf<FunctionDeclarationContext>()
        Konture.functions {
            printAllFunctions { discovered.add(it) }
            that().haveName("debugAllFunc")
            should().haveName("debugAllFunc")
        }
        assertTrue(discovered.any { it.declaration.name == "debugAllFunc" })
    }

    @Test
    fun `printAllProperties logs all discovered properties to custom logger`() {
        val discovered = mutableListOf<PropertyDeclarationContext>()
        Konture.properties {
            printAllProperties { discovered.add(it) }
            that().haveName("debugAllProp")
            should().haveName("debugAllProp")
        }
        assertTrue(discovered.any { it.declaration.name == "debugAllProp" })
    }

    @Test
    fun `printAllModules logs all discovered modules to custom logger`() {
        val discovered = mutableListOf<Module>()
        Konture.modules {
            printAllModules { discovered.add(it) }
            that().haveNamePath(":konture-test")
            should().onlyDependOnModules(":core")
        }
        assertTrue(discovered.any { it.path == ":konture-test" })
    }

    @Test
    fun `printAllSlices logs all discovered slices to custom logger`() {
        val discovered = mutableListOf<Slice>()
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            printAllSlices { discovered.add(it) }
            that().haveName("debugall")
            should().containClassesWithAnnotation(DebugAllMarker::class)
        }
        assertTrue(discovered.any { it.key == "debugall" })
    }
}
