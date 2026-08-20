/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.allowempty

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import io.github.baole.konture.tests.utils.violationsFound
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class AllowEmptyTest {

    @Test
    fun `classes allowEmpty passes on zero matches and fails without allowEmpty`() {
        Konture.classes {
            that().named("NonexistentClass")
            allowEmpty()
            should().beInterfaces()
        }

        val error = violationsFound {
            Konture.classes {
                that().named("NonexistentClass")
                should().beInterfaces()
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `files allowEmpty passes on zero matches and fails without allowEmpty`() {
        Konture.files {
            that().nameMatches("NonexistentFile.kt")
            allowEmpty()
            should().containClass(AllowEmptyClass::class)
        }

        val error = violationsFound {
            Konture.files {
                that().nameMatches("NonexistentFile.kt")
                should().containClass(AllowEmptyClass::class)
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `functions allowEmpty passes on zero matches and fails without allowEmpty`() {
        Konture.functions {
            that().haveName("nonexistentFunction")
            allowEmpty()
            should().haveName("foo")
        }

        val error = violationsFound {
            Konture.functions {
                that().haveName("nonexistentFunction")
                should().haveName("foo")
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `properties allowEmpty passes on zero matches and fails without allowEmpty`() {
        Konture.properties {
            that().haveName("nonexistentProperty")
            allowEmpty()
            should().haveName("foo")
        }

        val error = violationsFound {
            Konture.properties {
                that().haveName("nonexistentProperty")
                should().haveName("foo")
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `modules allowEmpty passes on zero matches and fails without allowEmpty`() {
        Konture.modules {
            that().haveNamePath(":nonexistentModule")
            allowEmpty()
            should().onlyDependOnModules(":core")
        }

        val error = violationsFound {
            Konture.modules {
                that().haveNamePath(":nonexistentModule")
                should().onlyDependOnModules(":core")
            }
        }
        assertNotNull(error)
    }

    @Test
    fun `slices allowEmpty passes on zero matches and fails without allowEmpty`() {
        Konture.slices {
            matching("nonexistent.package.(*)..")
            allowEmpty()
            should().notContainClasses()
        }

        val error = violationsFound {
            Konture.slices {
                matching("nonexistent.package.(*)..")
                should().notContainClasses()
            }
        }
        assertNotNull(error)
    }
}
