/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class ModuleExtensionsTest : RuleBuildersTestBase() {
    @Test
    fun `test module configuration scoped dependencies graph roles and metadata`() {
        assertDoesNotThrow {
            Konture.modules(SourceSets.named("main")) {
                allowEmpty()
                that().haveName(":library")
                    .and().haveBuildId("root")
                    .and().haveProjectDir("*library*")
                    .and().notHaveBuildId("otherBuild")
                    .and().notHaveProjectDir("prohibitedDir")
                    .and().containClassesInPackage("io.github.baole..")
                    .and().notContainClassesInPackage("com.forbidden..")
                    .and().containClassesWithAnnotation<Test>()
                    .and().notContainClassesWithAnnotation<Deprecated>()
                    .and().containClass<ModuleExtensionsTest>()
                    .and().notContainClass("ForbiddenClass")
                    .and().dependOnExternalLibrary("org.jetbrains.kotlin:kotlin-stdlib")

                should().dependOnModuleApi(":core")
                    .andShould().dependOnModuleImplementation(":core")
                    .andShould().dependOnModuleViaConfiguration(":core", "implementation")
                    .andShould().notDependOnModuleViaConfiguration(":app", "ksp")
                    .andShould().dependOnModuleTransitively(":core")
                    .andShould().notDependOnModuleTransitively(":forbidden")
                    .andShould().beLeafModule()
                    .andShould().haveBuildId("root")
                    .andShould().notHaveBuildId("forbiddenBuild")
                    .andShould().haveProjectDir("*library*")
                    .andShould().notHaveProjectDir("forbiddenDir")
                    .andShould().containClassesInPackage("io.github.baole..")
                    .andShould().notContainClassesInPackage("com.forbidden..")
                    .andShould().containClassesWithAnnotation<Test>()
                    .andShould().notContainClassesWithAnnotation<Deprecated>()
                    .andShould().containClass<ModuleExtensionsTest>()
                    .andShould().notContainClass("ForbiddenClass")
            }
        }
    }
}
