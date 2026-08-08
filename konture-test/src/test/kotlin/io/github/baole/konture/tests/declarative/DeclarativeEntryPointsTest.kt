/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.declarative

import io.github.baole.konture.Konture
import io.github.baole.konture.beAssignableTo
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.haveAnnotationOfType
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import io.github.baole.konture.tests.utils.violationsFound
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.Serializable

class DeclarativeEntryPointsTest {

    @Test
    fun `classes entry point non-violation`() {
        Konture.classes()
            .that().haveName("DeclarativeClass")
            .should().beAssignableTo<Serializable>()
            .check()
    }

    @Test
    fun `classes entry point violation`() {
        val error = violationsFound {
            Konture.classes()
                .that().haveName("DeclarativeClass")
                .should().beInterfaces()
                .check()
        }
        assertNotNull(error)
        assertTrue(error!!.message!!.contains("DeclarativeClass"))
    }

    @Test
    fun `files entry point non-violation`() {
        Konture.files()
            .that().haveNameMatching("DeclarativeTargets.kt")
            .should().containClass(DeclarativeClass::class)
            .check()
    }

    @Test
    fun `files entry point violation`() {
        val error = violationsFound {
            Konture.files()
                .that().haveNameMatching("DeclarativeTargets.kt")
                .should().notHaveImportOf("java.io.Serializable")
                .check()
        }
        assertNotNull(error)
        assertTrue(error!!.message!!.contains("DeclarativeTargets.kt"))
    }

    @Test
    fun `functions entry point non-violation`() {
        Konture.functions()
            .that().haveName("declarativeFunction")
            .should().haveAnnotationOfType<DeclarativeMarker>()
            .check()
    }

    @Test
    fun `functions entry point violation`() {
        val error = violationsFound {
            Konture.functions()
                .that().haveName("declarativeFunction")
                .should().haveName("nonExistentFunction")
                .check()
        }
        assertNotNull(error)
        assertTrue(error!!.message!!.contains("declarativeFunction"))
    }

    @Test
    fun `properties entry point non-violation`() {
        Konture.properties()
            .that().haveName("declarativeProperty")
            .should().haveAnnotationOfType<DeclarativeMarker>()
            .check()
    }

    @Test
    fun `properties entry point violation`() {
        val error = violationsFound {
            Konture.properties()
                .that().haveName("declarativeProperty")
                .should().haveName("nonExistentProperty")
                .check()
        }
        assertNotNull(error)
        assertTrue(error!!.message!!.contains("declarativeProperty"))
    }

    @Test
    fun `modules entry point non-violation`() {
        Konture.modules()
            .that().haveNamePath(":library")
            .should().onlyDependOnModules(":core")
            .check()
    }

    @Test
    fun `modules entry point violation`() {
        val error = violationsFound {
            Konture.modules()
                .that().haveNamePath(":library")
                .should().notDependOnModule(":core")
                .check()
        }
        assertNotNull(error)
        assertTrue(error!!.message!!.contains(":library"))
    }

    @Test
    fun `slices entry point non-violation`() {
        Konture.slices()
            .matching("io.github.baole.konture.tests.(*)..")
            .should().beFreeOfCycles()
            .check()
    }

    @Test
    fun `slices entry point violation`() {
        val error = violationsFound {
            Konture.slices()
                .matching("io.github.baole.konture.tests.(*)..")
                .should().notContainClasses()
                .check()
        }
        assertNotNull(error)
    }
}
