/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.prohibitions

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.notCall
import io.github.baole.konture.notReferenceClass
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class CallAndReferenceProhibitionsTest {

    private val pkg = "io.github.baole.konture.tests.prohibitions"

    @Test
    fun `classes notCall and notReferenceClass`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveName("CleanClass")
            should().notCall<CalleeClass>().andShould().notReferenceClass(CalleeClass::class)
        }
    }

    @Test
    fun `files notCall and notReferenceClass`() {
        Konture.files {
            that().resideInAPackage(pkg).and().haveName("ProhibitionTargets.kt")
            should().notCall("io.github.baole.konture.tests.prohibitions.CalleeClass.nonExistentFunc")
        }
    }

    @Test
    fun `functions notCall and notReferenceClass`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("cleanFunc")
            should().notCall<CalleeClass>().andShould().notReferenceClass(CalleeClass::class)
        }
    }

    @Test
    fun `properties notCall and notReferenceClass`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("cleanProp")
            should().notCall<CalleeClass>().andShould().notReferenceClass(CalleeClass::class)
        }
    }

    @Test
    fun `modules notCall and notReferenceClass`() {
        Konture.modules {
            that().resideInAModule(":konture-test")
            should().notCall("nonExistentForbiddenPackage.ForbiddenClass.func")
        }
    }

    @Test
    fun `slices notCall and notReferenceClass`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("prohibitions")
            should().notCall("nonExistentForbiddenPackage.ForbiddenClass.func")
        }
    }
}
