/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.selectors

import io.github.baole.konture.Konture
import io.github.baole.konture.architecture
import io.github.baole.konture.classScopeFromPackage
import io.github.baole.konture.fileScopeFromPackage
import io.github.baole.konture.functionScopeFromPackage
import io.github.baole.konture.inPackage
import io.github.baole.konture.minus
import io.github.baole.konture.moduleScopeFromModule
import io.github.baole.konture.plus
import io.github.baole.konture.propertyScopeFromPackage
import io.github.baole.konture.should
import io.github.baole.konture.sliceScope
import io.github.baole.konture.withName
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SelectorsAndCompositionTest {
    private val pkg = "io.github.baole.konture.tests.selectors"

    @Test
    fun `reusable class selectors allow sequential should assertions`() {
        val domainClasses = Konture.classScopeFromPackage(pkg)
        val repositories = domainClasses.withName("*Repository")

        repositories.should().beInterfaces()
        repositories.should().bePublic()
        repositories.should().notDependOnPackages("..data..")
    }

    @Test
    fun `scope selectors support set composition operators plus and minus`() {
        val selectorsScope = Konture.classScopeFromPackage(pkg)
        val declarativeScope = Konture.classScopeFromPackage("io.github.baole.konture.tests.declarative")

        val combined = selectorsScope + declarativeScope
        assertTrue(combined.classes.any { it.name == "SelectorSampleRepository" })
        assertTrue(combined.classes.any { it.name == "DeclarativeClass" })

        val subtracted = combined - declarativeScope
        assertTrue(subtracted.classes.any { it.name == "SelectorSampleRepository" })
        assertTrue(subtracted.classes.none { it.name == "DeclarativeClass" })
    }

    @Test
    fun `architecture context block supports direct selector properties`() {
        Konture.architecture {
            val repos = classes.inPackage("..selectors..").withName("*Repository")
            repos.should().beInterfaces()

            val services = classes.inPackage("..selectors..").withName("*Service")
            services.should().bePublic()
        }
    }

    @Test
    fun `function and property selectors support fluent filtering and assertions`() {
        val fns = Konture.functionScopeFromPackage(pkg)
        val processFn = fns.withName("process")
        processFn.should().bePublic()

        val props = Konture.propertyScopeFromPackage(pkg)
        val nameProp = props.withName("serviceName")
        nameProp.should().bePublic()
        nameProp.should().beVal()
    }

    @Test
    fun `file module and slice selectors support fluent filtering and should assertions`() {
        val files = Konture.fileScopeFromPackage(pkg)
        val targetFile = files.withName("*Targets.kt")
        targetFile.should().haveNoWildcardImports()

        val modules = Konture.moduleScopeFromModule(":konture-test")
        modules.should().notDependOnModules(":data:database")

        val slices = Konture.sliceScope("io.github.baole.konture.tests.(*)..")
        val selectorSlice = slices.withName("selectors")
        selectorSlice.should().beFreeOfCycles()
    }
}
