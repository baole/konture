/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.modifiers

import io.github.baole.konture.Konture
import io.github.baole.konture.Modifier
import io.github.baole.konture.Visibility
import io.github.baole.konture.classes
import io.github.baole.konture.functions
import io.github.baole.konture.properties
import org.junit.jupiter.api.Test

class VisibilityAndModifierTest {

    private val pkg = "io.github.baole.konture.tests.modifiers"

    @Test
    fun `classes visibility and modifier controls`() {
        Konture.classes {
            that().resideInAPackage(pkg).and().haveVisibility(Visibility.PUBLIC)
            should().bePublic().andShould().beData()
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().haveVisibility(Visibility.INTERNAL)
            should().beInternal().andShould().beSealed()
        }

        Konture.classes {
            that().resideInAPackage(pkg).and().haveModifier(Modifier.DATA)
            should().beData()
        }
    }

    @Test
    fun `functions visibility and modifier controls`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().haveVisibility(Visibility.PUBLIC)
            should().bePublic()
        }

        Konture.functions {
            that().resideInAPackage(pkg).and().haveModifier(Modifier.SUSPEND)
            should().beSuspend().andShould().beInternal()
        }
    }

    @Test
    fun `properties visibility and modifier controls`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("publicVal")
            should().bePublic().andShould().beVal()
        }

        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("publicVar")
            should().bePublic().andShould().beVar()
        }

        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("internalVal")
            should().beInternal().andShould().beVal()
        }
    }
}
