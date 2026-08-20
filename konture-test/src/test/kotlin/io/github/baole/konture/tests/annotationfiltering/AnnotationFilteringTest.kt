/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.annotationfiltering

import io.github.baole.konture.Konture
import io.github.baole.konture.annotatedWith
import io.github.baole.konture.annotatedWithAllOf
import io.github.baole.konture.annotatedWithAnyOf
import io.github.baole.konture.beAnnotatedWith
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.haveAnnotationOf
import io.github.baole.konture.haveAnnotationOfType
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class AnnotationFilteringTest {

    private val pkg = "io.github.baole.konture.tests.annotationfiltering"

    @Test
    fun `classes annotation filtering and assertions`() {
        Konture.classes {
            that().inPackage(pkg).and().annotatedWith<AnnMarkerA>()
            should().annotatedWith<AnnMarkerB>()
        }

        Konture.classes {
            that().inPackage(pkg).and().annotatedWith<AnnMarkerA>()
            should().annotatedWith<AnnMarkerB>()
        }

        Konture.classes {
            that().inPackage(pkg).and().annotatedWithAllOf(
                "io.github.baole.konture.tests.annotationfiltering.AnnMarkerA",
                "io.github.baole.konture.tests.annotationfiltering.AnnMarkerB",
            )
            should().named("AnnotatedClass")
        }

        Konture.classes {
            that().inPackage(pkg).and().annotatedWithAnyOf("io.github.baole.konture.tests.annotationfiltering.AnnMarkerA")
            should().named("AnnotatedClass")
        }
    }

    @Test
    fun `files annotation filtering and assertions`() {
        Konture.files {
            that().resideInAPackage(pkg).and().containClassesWithAnnotation<AnnMarkerA>()
            should().containClassesWithAnnotation<AnnMarkerA>()
        }
    }

    @Test
    fun `functions annotation filtering and assertions`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().haveAnnotationOf<AnnMarkerA>()
            should().haveAnnotationOfType<AnnMarkerB>().andShould().beAnnotatedWith<AnnMarkerB>()
        }
    }

    @Test
    fun `properties annotation filtering and assertions`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().haveAnnotationOf<AnnMarkerA>()
            should().haveAnnotationOfType<AnnMarkerB>().andShould().beAnnotatedWith<AnnMarkerB>()
        }
    }

    @Test
    fun `modules annotation filtering and assertions`() {
        Konture.modules {
            that().containClassesWithAnnotation(AnnMarkerA::class)
            should().containClassesWithAnnotation(AnnMarkerB::class)
        }
    }

    @Test
    fun `slices annotation filtering and assertions`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().containClassesWithAnnotation(AnnMarkerA::class)
            should().containClassesWithAnnotation(AnnMarkerB::class)
        }
    }
}
