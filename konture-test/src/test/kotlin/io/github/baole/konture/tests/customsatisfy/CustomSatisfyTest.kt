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
    fun `files satisfy on that and should`() {
        Konture.files {
            that().resideInAPackage(pkg).and().satisfy { it.declaration.name == "SatisfyTargets.kt" }
            should().satisfy { file, _ -> file.declaration.name == "SatisfyTargets.kt" }
        }
    }

    @Test
    fun `functions satisfy on that and should`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().satisfy { it.declaration.name == "satisfyFunc" }
            should().satisfy { fn, _ -> fn.declaration.name == "satisfyFunc" }
        }
    }

    @Test
    fun `properties satisfy on that and should`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().satisfy { it.declaration.name == "satisfyProp" }
            should().satisfy { prop, _ -> prop.declaration.name == "satisfyProp" }
        }
    }

    @Test
    fun `modules satisfy on that and should`() {
        Konture.modules {
            that().satisfy { it.path == ":konture-test" }
            should().satisfy { module, _ -> module.path == ":konture-test" }
        }
    }

    @Test
    fun `slices satisfy on that and should`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().satisfy { it.key == "customsatisfy" }
            should().satisfy("custom slice assertion")
        }
    }
}
