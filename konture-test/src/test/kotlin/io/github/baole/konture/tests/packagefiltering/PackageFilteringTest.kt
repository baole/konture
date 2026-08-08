/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.packagefiltering

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.resideInPackageOf
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class PackageFilteringTest {

    private val pkg = "io.github.baole.konture.tests.packagefiltering"

    @Test
    fun `classes resideInAPackage and resideInPackageOf`() {
        Konture.classes {
            that().resideInAPackage(pkg)
            should().haveName("PackageFilteringClass")
        }

        Konture.classes {
            that().resideInPackageOf(PackageFilteringClass::class)
            should().haveName("PackageFilteringClass")
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().notResideInAPackage("io.github.baole.konture.tests.declarative")
            should().haveName("PackageFilteringClass")
        }
    }

    @Test
    fun `files resideInAPackage and resideInPackageOf`() {
        Konture.files {
            that().resideInAPackage(pkg)
            should().containClass(PackageFilteringClass::class)
        }

        Konture.files {
            that().resideInPackageOf(PackageFilteringClass::class)
            should().containClass(PackageFilteringClass::class)
        }
    }

    @Test
    fun `functions resideInAPackage and resideInPackageOf`() {
        Konture.functions {
            that().resideInAPackage(pkg)
            should().haveName("packageFunc")
        }

        Konture.functions {
            that().resideInPackageOf(PackageFilteringClass::class)
            should().haveName("packageFunc")
        }
    }

    @Test
    fun `properties resideInAPackage and resideInPackageOf`() {
        Konture.properties {
            that().resideInAPackage(pkg)
            should().haveName("packageProp")
        }

        Konture.properties {
            that().resideInPackageOf(PackageFilteringClass::class)
            should().haveName("packageProp")
        }
    }

    @Test
    fun `modules containPackage and resideInAPackage`() {
        Konture.modules {
            that().containPackage(pkg)
            should().onlyDependOnModules(":core")
        }

        Konture.modules {
            that().resideInAPackage(pkg)
            should().onlyDependOnModules(":core")
        }
    }

    @Test
    fun `slices resideInAPackage`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().resideInAPackage(pkg)
            should().containClasses()
        }
    }
}
