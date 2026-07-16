/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Test

class ArchitectureTest {
    @Test
    fun `no circular dependencies allowed in the module graph`() {
        Konture
            .assertNoCycles()
    }

    @Test
    fun `module dependency rules verified via archunit style`() {
        // :core should not depend on anything other than external libraries
        Konture
            .modules()
            .that()
            .haveNamePath(":core")
            .should()
            .onlyDependOnModules() // no other project modules
            .check()

        // :library should only depend on :core
        Konture
            .modules()
            .that()
            .haveNamePath(":library")
            .should()
            .onlyDependOnModules(":core")
            .check()

        // :plugin-gradle should only depend on :core
        Konture
            .modules()
            .that()
            .haveNamePath(":plugin-gradle")
            .should()
            .onlyDependOnModules(":core")
            .check()
    }
}
