/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.logicalchaining

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

class LogicalChainingOperatorsTest {

    private val pkg = "io.github.baole.konture.tests.logicalchaining"

    @Test
    fun `classes logical chaining and, or, not`() {
        Konture.classes {
            that().inPackage(pkg).and().named("LogicalClassA").or().named("LogicalClassB")
            should().nameMatches("LogicalClass*")
        }

        Konture.classes {
            that().inPackage(pkg).and().nameMatches("LogicalClass*").and().annotatedWith<LogicalMarkerA>()
            should().named("LogicalClassA")
        }

        Konture.classes {
            that().inPackage(pkg).and().nameMatches("LogicalClass*").and().not().named("LogicalClassB")
            should().named("LogicalClassA")
        }
    }

    @Test
    fun `files logical chaining and, or, not`() {
        Konture.files {
            that().inPackage(pkg).and().nameMatches("LogicalChainingTargets.kt").or().nameMatches("IgnoringTargets.kt")
            should().containClasses()
        }

        Konture.files {
            that().inPackage(pkg).and().nameMatches("LogicalChainingTargets.kt").and().not().nameMatches("IgnoringTargets.kt")
            should().containClass(LogicalClassA::class)
        }
    }

    @Test
    fun `functions logical chaining and, or, not`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("funcA")
            should().haveAnnotationOfType<LogicalMarkerA>()
        }

        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("funcA").and().not().haveName("funcB")
            should().haveAnnotationOfType<LogicalMarkerA>()
        }
    }

    @Test
    fun `properties logical chaining and, or, not`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("propA")
            should().haveAnnotationOfType<LogicalMarkerA>()
        }

        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("propA").and().not().haveName("propB")
            should().haveAnnotationOfType<LogicalMarkerA>()
        }
    }

    @Test
    fun `modules logical chaining and, or, not`() {
        Konture.modules {
            that().haveNamePath(":konture-test").or().haveNamePath(":library")
            should().onlyDependOn(":core")
        }

        Konture.modules {
            that().haveNamePath(":konture-test").and().not().haveNamePath(":library")
            should().onlyDependOn(":core")
        }
    }

    @Test
    fun `slices logical chaining and, or, not`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("logicalchaining").or().haveName("ignoring")
            should().containClasses()
        }

        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("logicalchaining").and().not().haveName("ignoring")
            should().containClassesWithAnnotation(LogicalMarkerA::class)
        }
    }
}
