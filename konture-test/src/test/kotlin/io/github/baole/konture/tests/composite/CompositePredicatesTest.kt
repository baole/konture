/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.composite

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class CompositePredicatesTest {

    private val pkg = "io.github.baole.konture.tests.composite"

    @Test
    fun `classes composite predicates anyOf, allOf, noneOf`() {
        Konture.classes {
            that().anyOf({ resideInAPackage(pkg).and().haveName("CompositeClassA") }, { resideInAPackage(pkg).and().haveName("CompositeClassB") })
            should().allOf({ haveNameMatching("CompositeClass*") })
        }

        Konture.classes {
            that().resideInAPackage(pkg)
            should().noneOf({ haveName("NonExistentClass") })
        }
    }

    @Test
    fun `files composite predicates anyOf, allOf, noneOf`() {
        Konture.files {
            that().allOf({ resideInAPackage(pkg) }, { haveName("CompositeTargets.kt") })
            should().noneOf({ containClass(CompositePredicatesTest::class) })
        }
    }

    @Test
    fun `functions composite predicates anyOf, allOf, noneOf`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().anyOf({ haveName("compFuncA") }, { haveName("compFuncB") })
            should().allOf({ resideInAPackage(pkg) })
        }
    }

    @Test
    fun `properties composite predicates anyOf, allOf, noneOf`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().anyOf({ haveName("compPropA") }, { haveName("compPropB") })
            should().allOf({ resideInAPackage(pkg) })
        }
    }

    @Test
    fun `modules composite predicates anyOf, allOf, noneOf`() {
        Konture.modules {
            that().anyOf({ resideInAModule(":konture-test") })
            should().allOf({ notHavePlugin("nonExistentPlugin") })
        }
    }

    @Test
    fun `slices composite predicates anyOf, allOf, noneOf`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().anyOf({ haveName("composite") })
            should().allOf({ containClasses() })
        }
    }
}
