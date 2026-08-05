/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class PropertyExtensionsTest : RuleBuildersTestBase() {
    @Test
    fun `test property import assertions and call restrictions`() {
        assertDoesNotThrow {
            Konture.properties(SourceSets.tests()) {
                allowEmpty()
                that().haveImportOf("org.junit.jupiter.api.Test")
                    .and().notHaveImportOf("com.forbidden.ForbiddenClass")
                    .and().haveImportOf<Test>()
                    .and().notHaveImportOf<Deprecated>()

                should().haveImportOf("org.junit.jupiter.api.Test")
                    .andShould().notHaveImportOf("com.forbidden.ForbiddenClass")
                    .andShould().haveImportOf<Test>()
                    .andShould().notHaveImportOf<Deprecated>()
                    .andShould().haveNoWildcardImports()
                    .andShould().notCall("java.lang.System.exit")
                    .andShould().notReferenceClass("com.forbidden.ForbiddenClass")
            }
        }
    }
}
