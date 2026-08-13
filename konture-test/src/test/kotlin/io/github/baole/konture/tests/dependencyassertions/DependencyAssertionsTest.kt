/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.dependencyassertions

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class DependencyAssertionsTest {

    private val pkg = "io.github.baole.konture.tests.dependencyassertions"

    @Test
    fun `classes dependency assertions`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("DependencyClassA")
            should().notDependOnPackages("nonExistentPackage").andShould().notDependOnClasses(DependencyClassB::class)
        }
    }

    @Test
    fun `files dependency assertions`() {
        Konture.files {
            that().resideInAPackage(pkg).and().haveName("DependencyTargets.kt")
            should().notDependOnPackages("nonExistentPackage")
        }
    }

    @Test
    fun `functions dependency assertions and filters`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("sampleDependencyFunction")
            should().onlyDependOnPackages(pkg, "kotlin", "kotlin.jvm.internal").andShould().notDependOnPackages("nonExistentPackage")
        }

        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("sampleDependencyFunction").and().dependOnPackages("kotlin")
            should().haveName("sampleDependencyFunction")
        }

        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("sampleDependencyFunction")
            should().notDependOnPackages("nonExistentPackage")
        }
    }

    @Test
    fun `properties dependency assertions and filters`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("sampleDependencyProperty")
            should().onlyDependOnPackages(pkg, "kotlin").andShould().notDependOnPackages("nonExistentPackage")
        }

        Konture.properties {
            that().resideInAPackage(pkg).and().dependOnPackages("kotlin")
            should().haveName("sampleDependencyProperty")
        }

        Konture.properties {
            that().resideInAPackage(pkg).and().notDependOnPackages("nonExistentPackage")
            should().haveName("sampleDependencyProperty")
        }
    }

    @Test
    fun `modules dependency assertions`() {
        Konture.modules {
            that().resideInAModule(":konture-test")
            should().onlyDependOnModules(":core").andShould().notDependOnModule(":nonExistentModule")
        }
    }

    @Test
    fun `slices dependency assertions`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("dependencyassertions")
            should().notDependOnSlice("nonExistentSlice")
        }
    }
}

