/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.modulefiltering

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class ModuleFilteringTest {

    private val pkg = "io.github.baole.konture.tests.modulefiltering"

    @Test
    fun `classes resideInAModule and notResideInAModule`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().resideInAModule(":konture-test")
            should().haveName("ModuleFilteringClass")
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().notResideInAModule(":library")
            should().haveName("ModuleFilteringClass")
        }
    }

    @Test
    fun `files resideInAModule and notResideInAModule`() {
        Konture.files {
            that().resideInAPackage(pkg).and().resideInAModule(":konture-test")
            should().containClass(ModuleFilteringClass::class)
        }

        Konture.files {
            that().resideInAPackage(pkg).and().notResideInAModule(":library")
            should().containClass(ModuleFilteringClass::class)
        }
    }

    @Test
    fun `functions resideInAModule and notResideInAModule`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().resideInAModule(":konture-test")
            should().haveName("moduleFunc")
        }

        Konture.functions {
            that().resideInAPackage(pkg).and().notResideInAModule(":library")
            should().haveName("moduleFunc")
        }
    }

    @Test
    fun `properties resideInAModule and notResideInAModule`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().resideInAModule(":konture-test")
            should().haveName("moduleProp")
        }

        Konture.properties {
            that().resideInAPackage(pkg).and().notResideInAModule(":library")
            should().haveName("moduleProp")
        }
    }

    @Test
    fun `modules resideInAModule and resideInModules`() {
        Konture.modules {
            that().resideInAModule(":konture-test")
            should().onlyDependOnModules(":core")
        }

        Konture.modules {
            that().resideInModules(listOf(":konture-test", ":library"))
            should().onlyDependOnModules(":core")
        }
    }

    @Test
    fun `slices resideInModule`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("modulefiltering").and().resideInModule(":konture-test")
            should().containClasses()
        }
    }
}
