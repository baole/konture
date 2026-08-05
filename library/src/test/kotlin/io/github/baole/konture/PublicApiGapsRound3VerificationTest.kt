/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class PublicApiGapsRound3VerificationTest : RuleBuildersTestBase() {

    @Test
    fun `test functions negative visibility and modifier assertions`() {
        assertDoesNotThrow {
            FunctionsRuleBuilder(projectGraph)
                .should()
                .notBePrivate()
                .andShould().notBeSuspend()
                .andShould().notBeInline()
                .andShould().notHaveModifier(Modifier.SUSPEND)
        }
    }

    @Test
    fun `test classes negative modifier assertions`() {
        assertDoesNotThrow {
            ClassesRuleBuilder(projectGraph)
                .should()
                .notBeAbstract()
                .andShould().notBeSealed()
                .andShould().notBeData()
                .andShould().notBeInline()
                .andShould().notBeOpen()
                .andShould().notBeInner()
                .andShould().notBeInterface()
                .andShould().notHaveModifier(Modifier.INNER)
        }
    }

    @Test
    fun `test files top level structural assertions`() {
        assertDoesNotThrow {
            FilesRuleBuilder(projectGraph)
                .should()
                .haveTopLevelFunctions()
                .andShould().notHaveTopLevelFunctions()
                .andShould().haveTopLevelProperties()
                .andShould().notHaveTopLevelProperties()
                .andShould().haveClasses()
                .andShould().notHaveClasses()
        }
    }

    @Test
    fun `test modules source set and class assertions`() {
        assertDoesNotThrow {
            ModulesRuleBuilder(projectGraph)
                .should()
                .haveSourceSet("main")
                .andShould().notContainClasses()
        }
    }

    @Test
    fun `test slices content and package assertions`() {
        assertDoesNotThrow {
            SlicesRuleBuilder(projectGraph)
                .should()
                .containClasses()
                .andShould().notContainClasses()
                .andShould().containClassesInPackage("io.github.baole.konture..")
        }
    }
}
