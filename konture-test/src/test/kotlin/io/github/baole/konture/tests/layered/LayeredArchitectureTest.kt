/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.layered

import io.github.baole.konture.Konture
import io.github.baole.konture.layered
import io.github.baole.konture.layeredArchitecture
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class LayeredArchitectureTest {

    @Test
    fun `layered architecture block DSL verifies directional access constraints`() {
        Konture.layered {
            val presentation = layer("presentation") definedBy "io.github.baole.konture.tests.layered.presentation.."
            val domain = layer("domain") definedBy "io.github.baole.konture.tests.layered.domain.."
            val data = layer("data") definedBy "io.github.baole.konture.tests.layered.data.."

            where(presentation) {
                mayOnlyAccessLayers(domain)
            }
            where(data) {
                mayOnlyAccessLayers(domain)
            }
            where(domain) {
                mayOnlyBeAccessedByLayers(presentation, data)
            }
        }
    }

    @Test
    fun `layered architecture builder fluent API enforces layer constraints`() {
        Konture.layeredArchitecture()
            .layer("presentation").definedBy("io.github.baole.konture.tests.layered.presentation..")
            .layer("domain").definedBy("io.github.baole.konture.tests.layered.domain..")
            .layer("data").definedBy("io.github.baole.konture.tests.layered.data..")
            .whereLayer("presentation").mayOnlyAccessLayers("domain")
            .whereLayer("data").mayOnlyAccessLayers("domain")
            .whereLayer("presentation").mayNotBeAccessedByAnyLayer()
            .check()
    }

    @Test
    fun `layered architecture reports violations when constraints are broken`() {
        assertThrows(AssertionError::class.java) {
            Konture.layered {
                val presentation = layer("presentation") definedBy "io.github.baole.konture.tests.layered.presentation.."
                val domain = layer("domain") definedBy "io.github.baole.konture.tests.layered.domain.."

                where(domain) {
                    mayNotBeAccessedByAnyLayer()
                }
                where(presentation) {
                    mayOnlyAccessLayers(domain)
                }
            }
        }
    }

    @Test
    fun `layered architecture builder supports forbidden layer access rules`() {
        Konture.layeredArchitecture()
            .layer("presentation").definedBy("io.github.baole.konture.tests.layered.presentation..")
            .layer("domain").definedBy("io.github.baole.konture.tests.layered.domain..")
            .layer("data").definedBy("io.github.baole.konture.tests.layered.data..")
            .whereLayer("presentation").mayNotAccessLayers("data")
            .whereLayer("data").mayNotBeAccessedByLayers("presentation")
            .check()
    }
}
