/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.ignoring

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class BaselineAndIgnoringTest {

    @Test
    fun `classes ignoreFailuresIn suppresses violations for ignored class`() {
        Konture.classes {
            that().haveName("IgnoringClassA")
            ignoreFailuresIn("IgnoringClassA")
            should().beInterfaces()
        }
    }

    @Test
    fun `files ignoreFailuresIn suppresses violations for ignored file`() {
        Konture.files {
            that().haveName("IgnoringTargets.kt")
            ignoreFailuresIn("IgnoringTargets.kt")
            should().notHaveImportOf("java.io.Serializable")
        }
    }

    @Test
    fun `functions ignoreFailuresIn suppresses violations for ignored function`() {
        Konture.functions {
            that().resideInAPackage("io.github.baole.konture.tests.ignoring").and().haveName("failingFunc")
            ignoreFailuresIn("failingFunc")
            should().haveName("nonExistentName")
        }
    }

    @Test
    fun `properties ignoreFailuresIn suppresses violations for ignored property`() {
        Konture.properties {
            that().resideInAPackage("io.github.baole.konture.tests.ignoring").and().haveName("failingProp")
            ignoreFailuresIn("failingProp")
            should().haveName("nonExistentName")
        }
    }

    @Test
    fun `modules ignoreFailuresIn suppresses violations for ignored module`() {
        Konture.modules {
            that().haveNamePath(":konture-test")
            ignoreFailuresIn(":konture-test")
            should().notDependOnModule(":core")
        }
    }

    @Test
    fun `slices ignoreFailuresIn suppresses violations for ignored slice`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("ignoring")
            ignoreFailuresIn("ignoring")
            should().notContainClasses()
        }
    }
}
