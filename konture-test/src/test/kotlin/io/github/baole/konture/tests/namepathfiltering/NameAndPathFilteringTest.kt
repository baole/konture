/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.namepathfiltering

import io.github.baole.konture.Konture
import io.github.baole.konture.annotatedWith
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.haveAnnotationOfType
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class NameAndPathFilteringTest {

    @Test
    fun `classes name filtering via haveName, haveSimpleName, haveNameMatching`() {
        Konture.classes {
            that().named("io.github.baole.konture.tests.namepathfiltering.TargetNameClass")
            should().annotatedWith<NamePathMarker>()
        }

        Konture.classes {
            that().simpleNamed("TargetNameClass")
            should().annotatedWith<NamePathMarker>()
        }

        Konture.classes {
            that().nameMatches("TargetName*")
            should().annotatedWith<NamePathMarker>()
        }
    }

    @Test
    fun `files name and path filtering`() {
        Konture.files {
            that().named("NamePathTargets.kt")
            should().containClass(TargetNameClass::class)
        }

        Konture.files {
            that().nameMatches("NamePath*.kt")
            should().containClass(TargetNameClass::class)
        }
    }

    @Test
    fun `functions name filtering`() {
        Konture.functions {
            that().resideInAPackage("io.github.baole.konture.tests.namepathfiltering")
                .and().haveName("targetFunc")
            should().haveAnnotationOfType<NamePathMarker>()
        }

        Konture.functions {
            that().resideInAPackage("io.github.baole.konture.tests.namepathfiltering")
                .and().haveNameMatching("target*")
            should().haveAnnotationOfType<NamePathMarker>()
        }
    }

    @Test
    fun `properties name filtering`() {
        Konture.properties {
            that().resideInAPackage("io.github.baole.konture.tests.namepathfiltering")
                .and().haveName("targetProp")
            should().haveAnnotationOfType<NamePathMarker>()
        }

        Konture.properties {
            that().resideInAPackage("io.github.baole.konture.tests.namepathfiltering")
                .and().haveNameMatching("target*")
            should().haveAnnotationOfType<NamePathMarker>()
        }
    }

    @Test
    fun `modules path and name filtering`() {
        Konture.modules {
            that().haveNamePath(":konture-test")
            should().onlyDependOn(":core")
        }

        Konture.modules {
            that().haveName("konture-test")
            should().onlyDependOn(":core")
        }
    }

    @Test
    fun `slices key and name filtering`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveKey("namepathfiltering")
            should().containClassesWithAnnotation(NamePathMarker::class)
        }

        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("namepathfiltering")
            should().containClassesWithAnnotation(NamePathMarker::class)
        }
    }
}
