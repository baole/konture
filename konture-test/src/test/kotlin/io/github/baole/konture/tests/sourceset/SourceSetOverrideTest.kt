/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.sourceset

import io.github.baole.konture.Konture
import io.github.baole.konture.SourceSetKind
import io.github.baole.konture.SourceSetRole
import io.github.baole.konture.SourceSets
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.haveAnnotationOf
import io.github.baole.konture.haveAnnotationOfType
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import io.github.baole.konture.tests.utils.violationsFound
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SourceSetOverrideTest {

    @Test
    fun `classes source set selector overrides correctly`() {
        Konture.classes(SourceSets.named("main"))
            .that().haveName("ProductionTarget")
            .should().haveAnnotationOf<ProductionOnlyMarker>()
            .check()

        val error = violationsFound {
            Konture.classes(SourceSets.named("nonexistent"))
                .that().haveName("ProductionTarget")
                .should().haveAnnotationOf<ProductionOnlyMarker>()
                .check()
        }
        assertNotNull(error)
    }

    @Test
    fun `files source set selector overrides correctly`() {
        Konture.files(SourceSets.named("main"))
            .that().haveNameMatching("SourceSetTargets.kt")
            .should().containClass(ProductionTarget::class)
            .check()

        val error = violationsFound {
            Konture.files(SourceSets.named("nonexistent"))
                .that().haveNameMatching("SourceSetTargets.kt")
                .should().containClass(ProductionTarget::class)
                .check()
        }
        assertNotNull(error)
    }

    @Test
    fun `functions source set selector overrides correctly`() {
        Konture.functions(SourceSets.named("main"))
            .that().haveName("prodFunction")
            .should().haveAnnotationOfType<ProductionOnlyMarker>()
            .check()

        val error = violationsFound {
            Konture.functions(SourceSets.named("nonexistent"))
                .that().haveName("prodFunction")
                .should().haveAnnotationOfType<ProductionOnlyMarker>()
                .check()
        }
        assertNotNull(error)
    }

    @Test
    fun `properties source set selector overrides correctly`() {
        Konture.properties(SourceSets.named("main"))
            .that().haveName("prodProperty")
            .should().haveAnnotationOfType<ProductionOnlyMarker>()
            .check()

        val error = violationsFound {
            Konture.properties(SourceSets.named("nonexistent"))
                .that().haveName("prodProperty")
                .should().haveAnnotationOfType<ProductionOnlyMarker>()
                .check()
        }
        assertNotNull(error)
    }

    @Test
    fun `slices source set selector overrides correctly`() {
        Konture.slices(SourceSets.named("main"))
            .matching("io.github.baole.konture.tests.(*)..")
            .that().haveName("sourceset")
            .should().containClassesWithAnnotation(ProductionOnlyMarker::class)
            .check()

        val error = violationsFound {
            Konture.slices(SourceSets.named("nonexistent"))
                .matching("io.github.baole.konture.tests.(*)..")
                .that().haveName("sourceset")
                .should().containClassesWithAnnotation(ProductionOnlyMarker::class)
                .check()
        }
        assertNotNull(error)
    }

    @Test
    fun `modules source set selector overrides correctly`() {
        Konture.modules(SourceSets.named("main"))
            .that().haveNamePath(":library")
            .should().onlyDependOnModules(":core")
            .check()

        val error = violationsFound {
            Konture.modules(SourceSets.named("main"))
                .that().haveNamePath(":library")
                .should().notDependOnModule(":core")
                .check()
        }
        assertNotNull(error)
    }

    @Test
    fun `SourceSets production selector variation`() {
        Konture.classes(SourceSets.production())
            .that().haveName("ProductionTarget")
            .should().haveAnnotationOf<ProductionOnlyMarker>()
            .check()
    }

    @Test
    fun `SourceSets matchingName selector variation`() {
        Konture.classes(SourceSets.matchingName("mai*"))
            .that().haveName("ProductionTarget")
            .should().haveAnnotationOf<ProductionOnlyMarker>()
            .check()
    }

    @Test
    fun `SourceSets of selector variation`() {
        Konture.classes(SourceSets.of(role = SourceSetRole.PRODUCTION, kind = SourceSetKind.JVM))
            .that().haveName("ProductionTarget")
            .should().haveAnnotationOf<ProductionOnlyMarker>()
            .check()
    }

    @Test
    fun `SourceSets inModule selector variation`() {
        Konture.classes(SourceSets.inModule(":konture-test"))
            .that().haveName("ProductionTarget")
            .should().haveAnnotationOf<ProductionOnlyMarker>()
            .check()
    }

    @Test
    fun `SourceSets combination and variation`() {
        Konture.classes(SourceSets.production() and SourceSets.inModule(":konture-test"))
            .that().haveName("ProductionTarget")
            .should().haveAnnotationOf<ProductionOnlyMarker>()
            .check()
    }

    @Test
    fun `SourceSets combination or variation`() {
        Konture.classes(SourceSets.named("main") or SourceSets.named("test"))
            .that().haveName("ProductionTarget")
            .should().haveAnnotationOf<ProductionOnlyMarker>()
            .check()
    }

    @Test
    fun `SourceSets operator not variation`() {
        Konture.classes(!SourceSets.named("nonexistent"))
            .that().haveName("ProductionTarget")
            .should().haveAnnotationOf<ProductionOnlyMarker>()
            .check()
    }
}
