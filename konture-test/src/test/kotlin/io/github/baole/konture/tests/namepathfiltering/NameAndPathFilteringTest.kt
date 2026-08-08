/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.namepathfiltering

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.haveAnnotationOf
import io.github.baole.konture.haveAnnotationOfType
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class NameAndPathFilteringTest {

    @Test
    fun `classes name filtering via haveName, haveSimpleName, haveNameMatching`() {
        Konture.classes {
            that().haveName("io.github.baole.konture.tests.namepathfiltering.TargetNameClass")
            should().haveAnnotationOf<NamePathMarker>()
        }

        Konture.classes {
            that().haveSimpleName("TargetNameClass")
            should().haveAnnotationOf<NamePathMarker>()
        }

        Konture.classes {
            that().haveNameMatching("TargetName*")
            should().haveAnnotationOf<NamePathMarker>()
        }
    }

    @Test
    fun `files name and path filtering`() {
        Konture.files {
            that().haveName("NamePathTargets.kt")
            should().containClass(TargetNameClass::class)
        }

        Konture.files {
            that().haveNameMatching("NamePath*.kt")
            should().containClass(TargetNameClass::class)
        }
    }

    @Test
    fun `functions name filtering`() {
        Konture.functions {
            that().haveName("targetFunc")
            should().haveAnnotationOfType<NamePathMarker>()
        }

        Konture.functions {
            that().haveNameMatching("target*")
            should().haveAnnotationOfType<NamePathMarker>()
        }
    }

    @Test
    fun `properties name filtering`() {
        Konture.properties {
            that().haveName("targetProp")
            should().haveAnnotationOfType<NamePathMarker>()
        }

        Konture.properties {
            that().haveNameMatching("target*")
            should().haveAnnotationOfType<NamePathMarker>()
        }
    }

    @Test
    fun `modules path and name filtering`() {
        Konture.modules {
            that().haveNamePath(":konture-test")
            should().onlyDependOnModules(":core")
        }

        Konture.modules {
            that().haveName("konture-test")
            should().onlyDependOnModules(":core")
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
