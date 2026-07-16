/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LayeredArchitectureBuilderTest : RuleBuildersTestBase() {
    @Test
    fun `test layer definition and verification passing`() {
        Konture.layered {
            val presentation = layer("presentation") definedBy "com.example"
            val domain = layer("domain") definedBy "com.other"

            where(presentation) {
                mayOnlyAccessLayers(domain)
            }
        }
    }

    @Test
    fun `test mayNotBeAccessedByAnyLayer constraint violation`() {
        val dependingClassA =
            classA.copy(
                imports = listOf("com.other.ClassC"),
                referencedTypes = setOf("ClassC"),
            )

        val fileDeclA = FileDeclaration("ClassA.kt", "com.example", classes = listOf(dependingClassA))
        val updatedModuleA = moduleA.copy(files = listOf(fileDeclA))
        val customGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(updatedModuleA, moduleB, moduleC)),
            )

        val builder =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .whereLayer("domain")
                .mayNotBeAccessedByAnyLayer()

        val exception =
            assertThrows(AssertionError::class.java) {
                builder.check()
            }

        assertTrue(exception.message!!.contains("Layer 'domain' may not be accessed by any layer"))
        assertTrue(
            exception.message!!.contains(
                "class com.example.ClassA in layer 'presentation' depends on com.other.ClassC",
            ),
        )
    }

    @Test
    fun `test mayOnlyBeAccessedByLayers constraint success and violation`() {
        val dependingClassC =
            classC.copy(
                imports = listOf("com.example.ClassA"),
                referencedTypes = setOf("ClassA"),
            )

        val fileDeclC = FileDeclaration("ClassC.kt", "com.other", classes = listOf(dependingClassC))
        val updatedModuleC = moduleC.copy(files = listOf(fileDeclC))
        val customGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA, moduleB, updatedModuleC)),
            )

        val builderSuccess =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .whereLayer("presentation")
                .mayOnlyBeAccessedByLayers("domain")

        assertDoesNotThrow {
            builderSuccess.check()
        }

        val builderViolation =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .layer("data")
                .definedBy("com.none")
                .whereLayer("presentation")
                .mayOnlyBeAccessedByLayers("data")

        val exception =
            assertThrows(AssertionError::class.java) {
                builderViolation.check()
            }

        assertTrue(exception.message!!.contains("Layer 'presentation' may only be accessed by layers [data]"))
        assertTrue(
            exception.message!!.contains("class com.other.ClassC in layer 'domain' depends on com.example.ClassA"),
        )
    }

    @Test
    fun `test mayOnlyAccessLayers constraint success and violation`() {
        val dependingClassC =
            classC.copy(
                imports = listOf("com.example.ClassA"),
                referencedTypes = setOf("ClassA"),
            )

        val fileDeclC = FileDeclaration("ClassC.kt", "com.other", classes = listOf(dependingClassC))
        val updatedModuleC = moduleC.copy(files = listOf(fileDeclC))
        val customGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA, moduleB, updatedModuleC)),
            )

        val builderSuccess =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .whereLayer("domain")
                .mayOnlyAccessLayers("presentation")

        assertDoesNotThrow {
            builderSuccess.check()
        }

        val builderViolation =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .layer("data")
                .definedBy("com.none")
                .whereLayer("domain")
                .mayOnlyAccessLayers("data")

        val exception =
            assertThrows(AssertionError::class.java) {
                builderViolation.check()
            }

        assertTrue(exception.message!!.contains("Layer 'domain' may only access layers [data]"))
        assertTrue(
            exception.message!!.contains(
                "class com.other.ClassC depends on com.example.ClassA in layer 'presentation'",
            ),
        )
    }

    @Test
    fun `test block-based DSL extensions and scope builders`() {
        assertDoesNotThrow {
            Konture.modules {
                allowEmpty()
                that().haveNameMatching("*")
                should().satisfy { true }
            }
        }

        assertDoesNotThrow {
            Konture.classes {
                allowEmpty()
                that().haveNameMatching("*")
                should().satisfy { true }
            }
        }

        assertDoesNotThrow {
            Konture.functions {
                allowEmpty()
                that().haveNameMatching("*")
                should().satisfy { true }
            }
        }

        assertDoesNotThrow {
            Konture.properties {
                allowEmpty()
                that().haveNameMatching("*")
                should().satisfy { true }
            }
        }

        assertDoesNotThrow {
            Konture.files {
                allowEmpty()
                that().haveNameMatching("*")
                should().satisfy { true }
            }
        }

        assertDoesNotThrow {
            Konture.architecture {
                modules {
                    allowEmpty()
                    that().haveNameMatching("*")
                    should().satisfy { true }
                }
                classes {
                    allowEmpty()
                    that().haveNameMatching("*")
                    should().satisfy { true }
                }
                functions {
                    allowEmpty()
                    that().haveNameMatching("*")
                    should().satisfy { true }
                }
                properties {
                    allowEmpty()
                    that().haveNameMatching("*")
                    should().satisfy { true }
                }
                files {
                    allowEmpty()
                    that().haveNameMatching("*")
                    should().satisfy { true }
                }
                layeredArchitecture {
                    layer("presentation").definedBy("com.example")
                }
                layered {
                    layer("presentation") definedBy "com.example"
                }
            }
        }

        val scope = Konture.scope
        assertEquals(3, scope.classes.size)

        val moduleScope = Konture.scopeFromModule(":moduleA")
        assertEquals(1, moduleScope.classes.size)

        val packageScope = Konture.scopeFromPackage("com.example")
        assertEquals(2, packageScope.classes.size)

        val fileScope = Konture.fileScope
        assertEquals(3, fileScope.files.size)

        val fileModuleScope = Konture.fileScopeFromModule(":moduleA")
        assertEquals(1, fileModuleScope.files.size)

        val filePackageScope = Konture.fileScopeFromPackage("com.example")
        assertEquals(2, filePackageScope.files.size)

        assertDoesNotThrow {
            Konture.assertNoCycles()
        }
    }

    @Test
    fun `test mayNotAccessLayers constraint success and violation`() {
        val dependingClassC =
            classC.copy(
                imports = listOf("com.example.ClassA"),
                referencedTypes = setOf("ClassA"),
            )

        val fileDeclC = FileDeclaration("ClassC.kt", "com.other", classes = listOf(dependingClassC))
        val updatedModuleC = moduleC.copy(files = listOf(fileDeclC))
        val customGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA, moduleB, updatedModuleC)),
            )

        // Success scenario: domain (com.other) may not access 'data' (com.none) layer, which is true (only accesses com.example)
        val builderSuccess =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .layer("data")
                .definedBy("com.none")
                .whereLayer("domain")
                .mayNotAccessLayers("data")

        assertDoesNotThrow {
            builderSuccess.check()
        }

        // Violation scenario: domain (com.other) may not access 'presentation' (com.example), but ClassC depends on ClassA
        val builderViolation =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .whereLayer("domain")
                .mayNotAccessLayers("presentation")

        val exception =
            assertThrows(AssertionError::class.java) {
                builderViolation.check()
            }

        assertTrue(exception.message!!.contains("Layer 'domain' may not access layers [presentation]"))
        assertTrue(
            exception.message!!.contains(
                "class com.other.ClassC depends on com.example.ClassA in layer 'presentation'",
            ),
        )
    }

    @Test
    fun `test mayNotBeAccessedByLayers constraint success and violation`() {
        val dependingClassC =
            classC.copy(
                imports = listOf("com.example.ClassA"),
                referencedTypes = setOf("ClassA"),
            )

        val fileDeclC = FileDeclaration("ClassC.kt", "com.other", classes = listOf(dependingClassC))
        val updatedModuleC = moduleC.copy(files = listOf(fileDeclC))
        val customGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA, moduleB, updatedModuleC)),
            )

        // Success scenario: presentation (com.example) may not be accessed by 'data' (com.none), which is true
        val builderSuccess =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .layer("data")
                .definedBy("com.none")
                .whereLayer("presentation")
                .mayNotBeAccessedByLayers("data")

        assertDoesNotThrow {
            builderSuccess.check()
        }

        // Violation scenario: presentation (com.example) may not be accessed by 'domain' (com.other), but ClassC depends on ClassA
        val builderViolation =
            LayeredArchitectureBuilder(customGraph)
                .layer("presentation")
                .definedBy("com.example")
                .layer("domain")
                .definedBy("com.other")
                .whereLayer("presentation")
                .mayNotBeAccessedByLayers("domain")

        val exception =
            assertThrows(AssertionError::class.java) {
                builderViolation.check()
            }

        assertTrue(exception.message!!.contains("Layer 'presentation' may not be accessed by layers [domain]"))
        assertTrue(
            exception.message!!.contains("class com.other.ClassC in layer 'domain' depends on com.example.ClassA"),
        )
    }

    @Test
    fun `test layered architecture direct infix and single value overloads`() {
        val dependingClassC =
            classC.copy(
                imports = listOf("com.example.ClassA"),
                referencedTypes = setOf("ClassA"),
            )

        val fileDeclC = FileDeclaration("ClassC.kt", "com.other", classes = listOf(dependingClassC))
        val updatedModuleC = moduleC.copy(files = listOf(fileDeclC))
        val customGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(moduleA, moduleB, updatedModuleC)),
            )

        // Verify we can use 'definedBy' and the constraints with 'infix' operators on single strings and lists
        val builderSuccess = LayeredArchitectureBuilder(customGraph)
        builderSuccess.layer("presentation") definedBy "com.example"
        builderSuccess.layer("domain") definedBy "com.other"
        builderSuccess.layer("data") definedBy listOf("com.none")

        // May only access
        builderSuccess.whereLayer("domain") mayOnlyAccessLayers "presentation"
        builderSuccess.whereLayer("domain") mayOnlyAccessLayers listOf("presentation")

        // May not access
        builderSuccess.whereLayer("domain") mayNotAccessLayers "data"
        builderSuccess.whereLayer("domain") mayNotAccessLayers listOf("data")

        // May only be accessed by
        builderSuccess.whereLayer("presentation") mayOnlyBeAccessedByLayers "domain"
        builderSuccess.whereLayer("presentation") mayOnlyBeAccessedByLayers listOf("domain")

        // May not be accessed by
        builderSuccess.whereLayer("presentation") mayNotBeAccessedByLayers "data"
        builderSuccess.whereLayer("presentation") mayNotBeAccessedByLayers listOf("data")

        assertDoesNotThrow {
            builderSuccess.check()
        }
    }
}
