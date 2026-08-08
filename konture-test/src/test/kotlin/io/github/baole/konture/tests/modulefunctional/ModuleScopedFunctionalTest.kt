/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.modulefunctional

import io.github.baole.konture.Konture
import io.github.baole.konture.SourceSets
import io.github.baole.konture.classScopeFromModule
import io.github.baole.konture.fileScopeFromModule
import io.github.baole.konture.functionScopeFromModule
import io.github.baole.konture.moduleScopeFromModule
import io.github.baole.konture.propertyScopeFromModule
import io.github.baole.konture.scopeFromModule
import io.github.baole.konture.sliceScopeFromModule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ModuleScopedFunctionalTest {

    @Test
    fun `classScopeFromModule and scopeFromModule entries`() {
        val scope = Konture.classScopeFromModule(":konture-test")
        val scopeSynonym = Konture.scopeFromModule(":konture-test")
        assertTrue(scope.classes.any { it.name == "ModuleFunctionalClass" })
        assertTrue(scopeSynonym.classes.any { it.name == "ModuleFunctionalClass" })

        val withSourceSets = Konture.classScopeFromModule(":konture-test", SourceSets.named("main"))
        assertTrue(withSourceSets.classes.any { it.name == "ModuleFunctionalClass" })

        assertThrows<IllegalArgumentException> {
            Konture.classScopeFromModule(":nonexistent")
        }
    }

    @Test
    fun `fileScopeFromModule entry`() {
        val fileScope = Konture.fileScopeFromModule(":konture-test")
        assertTrue(fileScope.files.any { it.name == "ModuleFunctionalTargets.kt" })

        assertThrows<IllegalArgumentException> {
            Konture.fileScopeFromModule(":nonexistent")
        }
    }

    @Test
    fun `functionScopeFromModule entry`() {
        val functionScope = Konture.functionScopeFromModule(":konture-test")
        assertTrue(functionScope.functions.any { it.declaration.name == "moduleFunc" })

        assertThrows<IllegalArgumentException> {
            Konture.functionScopeFromModule(":nonexistent")
        }
    }

    @Test
    fun `propertyScopeFromModule entry`() {
        val propertyScope = Konture.propertyScopeFromModule(":konture-test")
        assertTrue(propertyScope.properties.any { it.declaration.name == "moduleProp" })

        assertThrows<IllegalArgumentException> {
            Konture.propertyScopeFromModule(":nonexistent")
        }
    }

    @Test
    fun `moduleScopeFromModule entry`() {
        val moduleScope = Konture.moduleScopeFromModule(":konture-test")
        assertTrue(moduleScope.modules.any { it.path == ":konture-test" })

        val moduleScopeWithSourceSets = Konture.moduleScopeFromModule(":konture-test", SourceSets.named("main"))
        assertTrue(moduleScopeWithSourceSets.modules.any { it.path == ":konture-test" })

        val patternScope = Konture.moduleScopeFromModule(":*")
        assertTrue(patternScope.modules.size >= 2)
    }

    @Test
    fun `sliceScopeFromModule entry`() {
        val sliceScope = Konture.sliceScopeFromModule("io.github.baole.konture.tests.(*)..", ":konture-test")
        assertTrue(sliceScope.slices.any { it.key == "modulefunctional" })

        assertThrows<IllegalArgumentException> {
            Konture.sliceScopeFromModule("io.github.baole.konture.tests.(*)..", ":nonexistent")
        }
    }
}
