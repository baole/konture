/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class FullParityCompletionTest : RuleBuildersTestBase() {
    @Test
    fun `test full slice parity methods`() {
        assertDoesNotThrow {
            SlicesRuleBuilder(projectGraph)
                .matching("io.github.baole.konture.(*)..")
                .that().haveName("core")
                .and().haveNameMatching("co*")
                .and().haveNameStartingWith("c")
                .and().haveNameEndingWith("e")
                .and().notHaveName("forbidden")
                .and().resideInModule(":library")
                .and().resideInModules(":library", ":core")
                .and().notResideInModule(":forbidden")
                .and().notResideInModules(":forbidden", ":other")
                .and().resideInAPackage("io.github.baole..")
                .and().notResideInAPackage("com.forbidden..")
                .and().notContainClass<FullParityCompletionTest>()
                .and().notContainClassesWithAnnotation<Test>()
            SlicesRuleBuilder(projectGraph)
                .matching("io.github.baole.konture.(*)..")
                .should().containFiles()
                .andShould().notContainFiles()
                .andShould().resideInModule(":library")
                .andShould().resideInModules(":library", ":core")
                .andShould().notResideInModule(":forbidden")
                .andShould().notResideInModules(":forbidden", ":other")
                .andShould().notContainClass<FullParityCompletionTest>()
                .andShould().notContainClassesWithAnnotation<Test>()
        }
    }

    @Test
    fun `test files reified negative methods`() {
        assertDoesNotThrow {
            FilesRuleBuilder(projectGraph)
                .that().haveName("FullParityCompletionTest.kt")
                .and().notHaveImportOf<Test>()
        }
    }

    @Test
    fun `test classes and functions reified negative methods`() {
        assertDoesNotThrow {
            ClassesRuleBuilder(projectGraph)
                .that().haveName("FullParityCompletionTest")
                .and().areNotAssignableTo<String>()
                .and().areNotAssignableFrom<String>()
            FunctionsRuleBuilder(projectGraph)
                .that().haveName("testFullSliceParityMethods")
                .and().haveReturnType<Unit>()
                .and().notHaveReturnType<String>()
                .and().notHaveParameterOf<String>()
        }
    }
}
