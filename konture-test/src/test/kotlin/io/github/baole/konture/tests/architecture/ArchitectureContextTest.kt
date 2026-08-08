/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.architecture

import io.github.baole.konture.Konture
import io.github.baole.konture.architecture
import io.github.baole.konture.beAssignableTo
import io.github.baole.konture.haveAnnotationOf
import io.github.baole.konture.haveAnnotationOfType
import io.github.baole.konture.tests.utils.violationsFound
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.Serializable

class ArchitectureContextTest {

    @Test
    fun `architecture context batch verification non-violation`() {
        Konture.architecture {
            classes {
                that().haveName("ArchClass")
                should().beAssignableTo<Serializable>()
                andShould().haveAnnotationOf<ArchMarker>()
            }

            files {
                that().haveNameMatching("ArchitectureTargets.kt")
                should().containClass(ArchClass::class)
            }

            functions {
                that().haveName("archFunction")
                should().haveAnnotationOfType<ArchMarker>()
            }

            properties {
                that().haveName("archProperty")
                should().haveAnnotationOfType<ArchMarker>()
            }

            modules {
                that().haveNamePath(":library")
                should().onlyDependOnModules(":core")
            }

            slices {
                matching("io.github.baole.konture.tests.(*)..")
                that().haveName("architecture")
                should().containClassesWithAnnotation(ArchMarker::class)
            }
        }
    }

    @Test
    fun `architecture context batch verification aggregates single violation`() {
        val error = violationsFound {
            Konture.architecture {
                classes {
                    that().haveName("ArchClass")
                    should().beInterfaces()
                }
            }
        }
        assertNotNull(error)
        assertTrue(error!!.message!!.contains("ArchClass"))
    }

    @Test
    fun `architecture context batch verification aggregates multiple violations`() {
        val error = violationsFound {
            Konture.architecture {
                classes {
                    that().haveName("ArchClass")
                    should().beInterfaces()
                }

                functions {
                    that().haveName("archFunction")
                    should().haveName("nonExistent")
                }
            }
        }
        assertNotNull(error)
        assertTrue(error!!.message!!.contains("ArchClass"))
        assertTrue(error.message!!.contains("archFunction"))
    }
}
