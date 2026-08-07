/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.defaultImports

import io.github.baole.konture.architecture
import io.github.baole.konture.tests.utils.violationsFound
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

class DefaultImportsTest {

    companion object {
        private const val PACKAGE = "io.github.baole.konture.tests.defaultImports"
    }

    /** Baseline: an explicitly imported JDK type IS detected. */
    @Test
    fun `detects java_io_File which is explicitly imported`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("java.io.File")
                }
            }
        }
        assertNotNull(detected, "expected a violation for java.io.File")
    }

    /** java.lang package: ProcessBuilder, System, IllegalArgumentException */
    @Test
    fun `detects java_lang_ProcessBuilder which is implicitly imported`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("java.lang.ProcessBuilder")
                }
            }
        }
        assertNotNull(detected, "expected a violation for java.lang.ProcessBuilder")
    }

    @Test
    fun `detects java_lang_System which is implicitly imported`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("java.lang.System")
                }
            }
        }
        assertNotNull(detected, "expected a violation for java.lang.System")
    }

    @Test
    fun `detects java_lang_IllegalArgumentException which is implicitly imported`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("java.lang.IllegalArgumentException")
                }
            }
        }
        assertNotNull(detected, "expected a violation for java.lang.IllegalArgumentException")
    }

    /** kotlin.collections package: Collection */
    @Test
    fun `detects kotlin_collections_Collection default import`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("kotlin.collections.Collection")
                }
            }
        }
        assertNotNull(detected, "expected a violation for kotlin.collections.Collection")
    }

    /** kotlin.ranges package: IntRange */
    @Test
    fun `detects kotlin_ranges_IntRange default import`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("kotlin.ranges.IntRange")
                }
            }
        }
        assertNotNull(detected, "expected a violation for kotlin.ranges.IntRange")
    }

    /** kotlin.sequences package: Sequence */
    @Test
    fun `detects kotlin_sequences_Sequence default import`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("kotlin.sequences.Sequence")
                }
            }
        }
        assertNotNull(detected, "expected a violation for kotlin.sequences.Sequence")
    }

    /** kotlin.text package: StringBuilder and Regex */
    @Test
    fun `detects kotlin_text_StringBuilder default import`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("kotlin.text.StringBuilder")
                }
            }
        }
        assertNotNull(detected, "expected a violation for kotlin.text.StringBuilder")
    }

    @Test
    fun `detects kotlin_text_Regex default import`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("kotlin.text.Regex")
                }
            }
        }
        assertNotNull(detected, "expected a violation for kotlin.text.Regex")
    }

    /** kotlin.comparisons package: Comparator */
    @Test
    fun `detects kotlin_comparisons_Comparator default import`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("kotlin.comparisons.Comparator")
                }
            }
        }
        assertNotNull(detected, "expected a violation for kotlin.comparisons.Comparator")
    }

    /** kotlin.annotation package: AnnotationRetention */
    @Test
    fun `detects kotlin_annotation_AnnotationRetention default import`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("kotlin.annotation.AnnotationRetention")
                }
            }
        }
        assertNotNull(detected, "expected a violation for kotlin.annotation.AnnotationRetention")
    }

    /** Explicitly imported alias java.util.Collection is distinguished from default kotlin.collections.Collection. */
    @Test
    fun `distinguishes java_util_Collection from kotlin_collections_Collection`() {
        val detectedJava = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("java.util.Collection")
                }
            }
        }
        assertNotNull(detectedJava, "expected a violation for java.util.Collection")

        val detectedOther = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("java.util.List")
                }
            }
        }
        assertNull(detectedOther, "unexpected violation for java.util.List")
    }

    /** Same package CustomCollection is distinguished from default imports. */
    @Test
    fun `detects same package CustomCollection`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("io.github.baole.konture.tests.defaultImports.CustomCollection")
                }
            }
        }
        assertNotNull(detected, "expected a violation for CustomCollection")
    }

    /** References used via import alias JavaCollection (for java.util.Collection) are detected. */
    @Test
    fun `detects class reference when accessed via import alias`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("JavaCollection")
                }
            }
        }
        assertNotNull(detected, "expected a violation when referencing class via import alias 'JavaCollection'")
    }

    /** References to typealias AliasArrayList are detected by alias name. */
    @Test
    fun `detects typealias reference for AliasArrayList`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("AliasArrayList")
                }
            }
        }
        assertNotNull(detected, "expected a violation for AliasArrayList")
    }

    /** References using AliasArrayList resolve to underlying kotlin.collections.ArrayList default import. */
    @Test
    fun `detects underlying kotlin_collections_ArrayList via typealias AliasArrayList`() {
        val detected = violationsFound {
            architecture {
                classes {
                    that().resideInAPackage(PACKAGE)
                    should().notReferenceClass("kotlin.collections.ArrayList")
                }
            }
        }
        assertNotNull(detected, "expected a violation for underlying kotlin.collections.ArrayList")
    }
}
