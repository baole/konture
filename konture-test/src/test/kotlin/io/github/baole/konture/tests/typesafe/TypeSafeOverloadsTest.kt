/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.typesafe

import io.github.baole.konture.Konture
import io.github.baole.konture.beAnnotatedWith
import io.github.baole.konture.classes
import io.github.baole.konture.containClassesWithAnnotation
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.haveAnnotationOf
import io.github.baole.konture.modules
import io.github.baole.konture.notCall
import io.github.baole.konture.notReferenceClass
import io.github.baole.konture.properties
import io.github.baole.konture.resideInPackageOf
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class TypeSafeOverloadsTest {

    @Test
    fun `classes type safe overloads`() {
        Konture.classes {
            that().resideInPackageOf(TypeSafeClass::class).and().haveAnnotationOf<TypeSafeMarker>()
            should().beAnnotatedWith<TypeSafeMarker>().andShould().notCall<TypeSafeDep>()
        }
    }

    @Test
    fun `files type safe overloads`() {
        Konture.files {
            that().resideInPackageOf(TypeSafeClass::class).and().haveName("TypeSafeTargets.kt")
            should().notReferenceClass(TypeSafeDep::class)
        }
    }

    @Test
    fun `functions type safe overloads`() {
        Konture.functions {
            that().resideInPackageOf(TypeSafeClass::class).and().haveAnnotationOf<TypeSafeMarker>()
            should().beAnnotatedWith<TypeSafeMarker>()
        }
    }

    @Test
    fun `properties type safe overloads`() {
        Konture.properties {
            that().resideInPackageOf(TypeSafeClass::class).and().haveAnnotationOf<TypeSafeMarker>()
            should().beAnnotatedWith<TypeSafeMarker>()
        }
    }

    @Test
    fun `modules type safe overloads`() {
        Konture.modules {
            that().containClassesWithAnnotation<TypeSafeMarker>()
            should().containClassesWithAnnotation<TypeSafeMarker>()
        }
    }

    @Test
    fun `slices type safe overloads`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveKey("typesafe")
            should().containClassesWithAnnotation<TypeSafeMarker>()
        }
    }
}
