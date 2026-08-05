/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class PublicApiGapsRound2VerificationTest : RuleBuildersTestBase() {

    @Test
    fun `test classes reified subtyping and beChildOf aliases`() {
        assertDoesNotThrow {
            ClassesRuleBuilder(projectGraph)
                .that()
                .areAssignableTo<RuleBuildersTestBase>()
                .and().beChildOf<RuleBuildersTestBase>()
                .and().beChildOf("io.github.baole.konture.RuleBuildersTestBase")
        }
    }

    @Test
    fun `test files collection overloads and negative structure filters`() {
        assertDoesNotThrow {
            FilesRuleBuilder(projectGraph)
                .that()
                .haveImportOf("org.junit.jupiter.api.Test", "kotlin.test.*")
                .and().haveImportOf(listOf("org.junit.jupiter.api.Test"))
                .and().containClass("PublicApiGapsRound2VerificationTest", "RuleBuildersTestBase")
                .and().containClass(listOf("PublicApiGapsRound2VerificationTest"))
                .and().notContainTopLevelFunctions()
                .and().notContainTopLevelProperties()
                .and().notContainClasses()
            FilesRuleBuilder(projectGraph)
                .should()
                .haveNoWildcardImports()
        }
    }

    @Test
    fun `test functions haveNoParameters assertion`() {
        assertDoesNotThrow {
            FunctionsRuleBuilder(projectGraph)
                .should()
                .haveNoParameters()
        }
    }

    @Test
    fun `test slices key overloads`() {
        assertDoesNotThrow {
            SlicesRuleBuilder(projectGraph)
                .that()
                .haveKey("core", "library")
                .and().haveKey(listOf("core", "library"))
                .and().haveKey { it.isNotEmpty() }
                .and().haveKeyMatching(listOf("co*", "lib*"))
                .and().haveKeyMatching("co*", "lib*")
        }
    }

    @Test
    fun `test scope plus and minus operators`() {
        val scopeM1 = KontureModuleScope(listOf(moduleA))
        val scopeM2 = KontureModuleScope(listOf(moduleB))
        val scopeMCombined = scopeM1 + scopeM2
        assertEquals(2, scopeMCombined.modules.size)
        val scopeMDiff = scopeMCombined - scopeM2
        assertEquals(1, scopeMDiff.modules.size)

        val slice1 = Slice("core", setOf("com.example.core"), listOf(classA))
        val slice2 = Slice("app", setOf("com.example.app"), listOf(classB))
        val scopeS1 = KontureSliceScope(listOf(slice1))
        val scopeS2 = KontureSliceScope(listOf(slice2))
        val scopeSCombined = scopeS1 + scopeS2
        assertEquals(2, scopeSCombined.slices.size)
        val scopeSDiff = scopeSCombined - scopeS2
        assertEquals(1, scopeSDiff.slices.size)
    }
}
