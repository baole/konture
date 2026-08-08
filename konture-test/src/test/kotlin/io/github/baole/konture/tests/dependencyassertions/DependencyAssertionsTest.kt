/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.dependencyassertions

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.modules
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
    fun `modules dependency assertions`() {
        Konture.modules {
            that().resideInAModule(":konture-test")
            should().onlyDependOnModules(":core").andShould().notDependOnModules(":nonExistentModule")
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
