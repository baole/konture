/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.cycledetection

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.modules
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class CycleDetectionTest {

    private val pkg = "io.github.baole.konture.tests.cycledetection"

    @Test
    fun `classes beFreeOfCycles`() {
        Konture.classes {
            that().resideInAPackage(pkg)
            should().beFreeOfCycles()
        }
    }

    @Test
    fun `files beFreeOfCycles`() {
        Konture.files {
            that().resideInAPackage(pkg)
            should().beFreeOfCycles()
        }
    }


    @Test
    fun `modules beFreeOfCycles`() {
        Konture.modules {
            that().resideInAModule(":konture-test")
            should().beFreeOfCycles()
        }
    }

    @Test
    fun `slices beFreeOfCycles`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("cycledetection")
            should().beFreeOfCycles()
        }
    }
}
