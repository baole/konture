/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.packagefunctional

import io.github.baole.konture.Konture
import io.github.baole.konture.SourceSets
import io.github.baole.konture.classScopeFromPackage
import io.github.baole.konture.fileScopeFromPackage
import io.github.baole.konture.functionScopeFromPackage
import io.github.baole.konture.propertyScopeFromPackage
import io.github.baole.konture.scopeFromPackage
import io.github.baole.konture.sliceScopeFromPackage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PackageScopedFunctionalTest {

    private val targetPackage = "io.github.baole.konture.tests.packagefunctional"
    private val emptyPackage = "io.github.baole.konture.tests.nonexistent"

    @Test
    fun `classScopeFromPackage and scopeFromPackage entries`() {
        val scope = Konture.classScopeFromPackage(targetPackage)
        val scopeSynonym = Konture.scopeFromPackage(targetPackage)
        assertTrue(scope.classes.any { it.name == "PackageFunctionalClass" })
        assertTrue(scopeSynonym.classes.any { it.name == "PackageFunctionalClass" })

        val withSourceSets = Konture.classScopeFromPackage(targetPackage, SourceSets.named("main"))
        assertTrue(withSourceSets.classes.any { it.name == "PackageFunctionalClass" })

        val emptyScope = Konture.classScopeFromPackage(emptyPackage)
        assertFalse(emptyScope.classes.any { it.name == "PackageFunctionalClass" })
    }

    @Test
    fun `fileScopeFromPackage entry`() {
        val fileScope = Konture.fileScopeFromPackage(targetPackage)
        assertTrue(fileScope.files.any { it.name == "PackageFunctionalTargets.kt" })

        val withSourceSets = Konture.fileScopeFromPackage(targetPackage, SourceSets.named("main"))
        assertTrue(withSourceSets.files.any { it.name == "PackageFunctionalTargets.kt" })

        val emptyFileScope = Konture.fileScopeFromPackage(emptyPackage)
        assertFalse(emptyFileScope.files.any { it.name == "PackageFunctionalTargets.kt" })
    }

    @Test
    fun `functionScopeFromPackage entry`() {
        val functionScope = Konture.functionScopeFromPackage(targetPackage)
        assertTrue(functionScope.functions.any { it.declaration.name == "packageFunc" })

        val withSourceSets = Konture.functionScopeFromPackage(targetPackage, SourceSets.named("main"))
        assertTrue(withSourceSets.functions.any { it.declaration.name == "packageFunc" })

        val emptyFunctionScope = Konture.functionScopeFromPackage(emptyPackage)
        assertFalse(emptyFunctionScope.functions.any { it.declaration.name == "packageFunc" })
    }

    @Test
    fun `propertyScopeFromPackage entry`() {
        val propertyScope = Konture.propertyScopeFromPackage(targetPackage)
        assertTrue(propertyScope.properties.any { it.declaration.name == "packageProp" })

        val withSourceSets = Konture.propertyScopeFromPackage(targetPackage, SourceSets.named("main"))
        assertTrue(withSourceSets.properties.any { it.declaration.name == "packageProp" })

        val emptyPropertyScope = Konture.propertyScopeFromPackage(emptyPackage)
        assertFalse(emptyPropertyScope.properties.any { it.declaration.name == "packageProp" })
    }

    @Test
    fun `sliceScopeFromPackage entry`() {
        val sliceScope = Konture.sliceScopeFromPackage("io.github.baole.konture.tests.(*)..", targetPackage)
        assertTrue(sliceScope.slices.any { it.key == "packagefunctional" })

        val withSourceSets = Konture.sliceScopeFromPackage("io.github.baole.konture.tests.(*)..", targetPackage, SourceSets.named("main"))
        assertTrue(withSourceSets.slices.any { it.key == "packagefunctional" })

        val emptySliceScope = Konture.sliceScopeFromPackage("io.github.baole.konture.tests.(*)..", emptyPackage)
        assertFalse(emptySliceScope.slices.any { it.key == "packagefunctional" })
    }
}
