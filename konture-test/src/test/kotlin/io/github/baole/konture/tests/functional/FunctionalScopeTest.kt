/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.functional

import io.github.baole.konture.Konture
import io.github.baole.konture.SourceSets
import io.github.baole.konture.classScope
import io.github.baole.konture.fileScope
import io.github.baole.konture.functionScope
import io.github.baole.konture.moduleScope
import io.github.baole.konture.propertyScope
import io.github.baole.konture.scope
import io.github.baole.konture.sliceScope
import io.github.baole.konture.withAnnotationOf
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FunctionalScopeTest {

    @Test
    fun `classScope and scope functional entries`() {
        val classScope = Konture.classScope
        val scope = Konture.scope
        assertTrue(classScope.classes.any { it.name == "FunctionalClass" })
        assertTrue(scope.classes.any { it.name == "FunctionalClass" })

        val annotatedScope = scope.withAnnotationOf<FunctionalMarker>()
        assertTrue(annotatedScope.classes.any { it.name == "FunctionalClass" })

        val withSourceSets = Konture.classScope(SourceSets.named("main"))
        assertTrue(withSourceSets.classes.any { it.name == "FunctionalClass" })

        val emptySourceSets = Konture.classScope(SourceSets.named("nonexistent"))
        assertFalse(emptySourceSets.classes.any { it.name == "FunctionalClass" })
    }

    @Test
    fun `fileScope functional entry`() {
        val fileScope = Konture.fileScope
        assertTrue(fileScope.files.any { it.name == "FunctionalTargets.kt" })

        val withSourceSets = Konture.fileScope(SourceSets.named("main"))
        assertTrue(withSourceSets.files.any { it.name == "FunctionalTargets.kt" })

        val emptySourceSets = Konture.fileScope(SourceSets.named("nonexistent"))
        assertFalse(emptySourceSets.files.any { it.name == "FunctionalTargets.kt" })
    }

    @Test
    fun `functionScope functional entry`() {
        val functionScope = Konture.functionScope
        assertTrue(functionScope.functions.any { it.declaration.name == "functionalFunction" })

        val withSourceSets = Konture.functionScope(SourceSets.named("main"))
        assertTrue(withSourceSets.functions.any { it.declaration.name == "functionalFunction" })

        val emptySourceSets = Konture.functionScope(SourceSets.named("nonexistent"))
        assertFalse(emptySourceSets.functions.any { it.declaration.name == "functionalFunction" })
    }

    @Test
    fun `propertyScope functional entry`() {
        val propertyScope = Konture.propertyScope
        assertTrue(propertyScope.properties.any { it.declaration.name == "functionalProperty" })

        val withSourceSets = Konture.propertyScope(SourceSets.named("main"))
        assertTrue(withSourceSets.properties.any { it.declaration.name == "functionalProperty" })

        val emptySourceSets = Konture.propertyScope(SourceSets.named("nonexistent"))
        assertFalse(emptySourceSets.properties.any { it.declaration.name == "functionalProperty" })
    }

    @Test
    fun `moduleScope functional entry`() {
        val moduleScope = Konture.moduleScope
        assertTrue(moduleScope.modules.any { it.path == ":konture-test" })

        val withSourceSets = Konture.moduleScope(SourceSets.named("main"))
        assertTrue(withSourceSets.modules.any { it.path == ":konture-test" })
    }

    @Test
    fun `sliceScope functional entry`() {
        val sliceScope = Konture.sliceScope("io.github.baole.konture.tests.(*)..")
        assertTrue(sliceScope.slices.any { it.key == "functional" })

        val withSourceSets = Konture.sliceScope("io.github.baole.konture.tests.(*)..", SourceSets.named("main"))
        assertTrue(withSourceSets.slices.any { it.key == "functional" })

        val emptySourceSets = Konture.sliceScope("io.github.baole.konture.tests.(*)..", SourceSets.named("nonexistent"))
        assertFalse(emptySourceSets.slices.any { it.key == "functional" })
    }
}
