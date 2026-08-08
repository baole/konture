/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.autochecking

import io.github.baole.konture.Konture
import io.github.baole.konture.SourceSets
import io.github.baole.konture.beAssignableTo
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
import java.io.Serializable

class AutoCheckingDslTest {

    @Test
    fun `classes block DSL non-violation and violation`() {
        Konture.classes {
            that().haveName("AutoCheckingClass")
            should().beAssignableTo<Serializable>()
        }

        val error = violationsFound {
            Konture.classes {
                that().haveName("AutoCheckingClass")
                should().beInterfaces()
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `classes block DSL with sourceSets overload`() {
        Konture.classes(SourceSets.named("main")) {
            that().haveName("AutoCheckingClass")
            should().haveAnnotationOf<AutoCheckingMarker>()
        }

        val error = violationsFound {
            Konture.classes(SourceSets.named("nonexistent")) {
                that().haveName("AutoCheckingClass")
                should().haveAnnotationOf<AutoCheckingMarker>()
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `files block DSL non-violation and violation`() {
        Konture.files {
            that().haveNameMatching("AutoCheckingTargets.kt")
            should().containClass(AutoCheckingClass::class)
        }

        val error = violationsFound {
            Konture.files {
                that().haveNameMatching("AutoCheckingTargets.kt")
                should().notHaveImportOf("java.io.Serializable")
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `files block DSL with sourceSets overload`() {
        Konture.files(SourceSets.named("main")) {
            that().haveNameMatching("AutoCheckingTargets.kt")
            should().containClass(AutoCheckingClass::class)
        }

        val error = violationsFound {
            Konture.files(SourceSets.named("nonexistent")) {
                that().haveNameMatching("AutoCheckingTargets.kt")
                should().containClass(AutoCheckingClass::class)
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `functions block DSL non-violation and violation`() {
        Konture.functions {
            that().haveName("autoCheckingFunction")
            should().haveAnnotationOfType<AutoCheckingMarker>()
        }

        val error = violationsFound {
            Konture.functions {
                that().haveName("autoCheckingFunction")
                should().haveName("nonExistent")
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `functions block DSL with sourceSets overload`() {
        Konture.functions(SourceSets.named("main")) {
            that().haveName("autoCheckingFunction")
            should().haveAnnotationOfType<AutoCheckingMarker>()
        }

        val error = violationsFound {
            Konture.functions(SourceSets.named("nonexistent")) {
                that().haveName("autoCheckingFunction")
                should().haveAnnotationOfType<AutoCheckingMarker>()
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `properties block DSL non-violation and violation`() {
        Konture.properties {
            that().haveName("autoCheckingProperty")
            should().haveAnnotationOfType<AutoCheckingMarker>()
        }

        val error = violationsFound {
            Konture.properties {
                that().haveName("autoCheckingProperty")
                should().haveName("nonExistent")
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `properties block DSL with sourceSets overload`() {
        Konture.properties(SourceSets.named("main")) {
            that().haveName("autoCheckingProperty")
            should().haveAnnotationOfType<AutoCheckingMarker>()
        }

        val error = violationsFound {
            Konture.properties(SourceSets.named("nonexistent")) {
                that().haveName("autoCheckingProperty")
                should().haveAnnotationOfType<AutoCheckingMarker>()
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `modules block DSL non-violation and violation`() {
        Konture.modules {
            that().haveNamePath(":library")
            should().onlyDependOnModules(":core")
        }

        val error = violationsFound {
            Konture.modules {
                that().haveNamePath(":library")
                should().notDependOnModule(":core")
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `modules block DSL with sourceSets overload`() {
        Konture.modules(SourceSets.named("main")) {
            that().haveNamePath(":library")
            should().onlyDependOnModules(":core")
        }

        val error = violationsFound {
            Konture.modules(SourceSets.named("main")) {
                that().haveNamePath(":library")
                should().notDependOnModule(":core")
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `slices block DSL non-violation and violation`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("autochecking")
            should().containClassesWithAnnotation(AutoCheckingMarker::class)
        }

        val error = violationsFound {
            Konture.slices {
                matching("io.github.baole.konture.tests.(*)..")
                that().haveName("autochecking")
                should().notContainClasses()
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `slices block DSL with sourceSets overload`() {
        Konture.slices(SourceSets.named("main")) {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("autochecking")
            should().containClassesWithAnnotation(AutoCheckingMarker::class)
        }

        val error = violationsFound {
            Konture.slices(SourceSets.named("nonexistent")) {
                matching("io.github.baole.konture.tests.(*)..")
                that().haveName("autochecking")
                should().containClassesWithAnnotation(AutoCheckingMarker::class)
            }
        }
        assertNotNull(error)
    }
}
