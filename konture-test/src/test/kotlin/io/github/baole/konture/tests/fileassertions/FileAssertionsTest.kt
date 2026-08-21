/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.fileassertions

import io.github.baole.konture.Konture
import io.github.baole.konture.files
import org.junit.jupiter.api.Test

class FileAssertionsTest {
    private val pkg = "io.github.baole.konture.tests.fileassertions"

    @Test
    fun `file name and package assertions`() {
        Konture.files {
            that().inPackage(pkg).and().named("FileAssertionTargets.kt")
            should().inPackage(pkg)
                .andShould().nameStartsWith("FileAssertion")
                .andShould().nameEndsWith("Targets.kt")
                .andShould().named("FileAssertionTargets.kt")
                .andShould().notNamed("NonExistentFile.kt")
                .andShould().notNameStartsWith("Unknown")
                .andShould().notNameEndsWith(".java")
        }
    }

    @Test
    fun `file content structure and top-level assertions`() {
        Konture.files {
            that().inPackage(pkg).and().named("FileAssertionTargets.kt")
            should().containClasses()
                .andShould().haveTopLevelProperties()
                .andShould().haveTopLevelFunctions()
                .andShould().containClass("FileAssertionSampleClass")
                .andShould().notContainClass("NonExistentClass")
                .andShould().containClassesWithAnnotation(FileTargetMarker::class)
        }
    }

    @Test
    fun `file import assertions`() {
        Konture.files {
            that().inPackage(pkg).and().named("FileAssertionTargets.kt")
            should().haveNoWildcardImports()
                .andShould().notHaveWildcardImports()
                .andShould().haveImportOf("java.io.Serializable")
                .andShould().notHaveImportOf("java.util.concurrent.atomic.AtomicBoolean")
        }
    }
}
