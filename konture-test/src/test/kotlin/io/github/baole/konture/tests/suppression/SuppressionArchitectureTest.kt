/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.suppression

import io.github.baole.konture.Konture
import io.github.baole.konture.architecture
import io.github.baole.konture.classes
import io.github.baole.konture.files
import io.github.baole.konture.functions
import io.github.baole.konture.modules
import io.github.baole.konture.properties
import io.github.baole.konture.slices
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SuppressionArchitectureTest {

    @Test
    fun `in-source class suppression with explicit rule id succeeds`() {
        Konture.architecture {
            rule("classes.rule") {
                classes {
                    that().haveName("InSourceSuppressedClass")
                    should().beInterfaces()
                }
            }
        }
    }

    @Test
    fun `in-source class suppression with wildcard token succeeds for any rule`() {
        Konture.architecture {
            rule("custom.domain.rule") {
                classes {
                    that().haveName("WildcardSuppressedClass")
                    should().beInterfaces()
                }
            }
        }
    }

    @Test
    fun `in-source java SuppressWarnings annotation is respected`() {
        Konture.architecture {
            rule("classes.rule") {
                classes {
                    that().haveName("JavaAnnotationSuppressedClass")
                    should().beInterfaces()
                }
            }
        }
    }

    @Test
    fun `file-level suppression cascades to classes and functions in file`() {
        Konture.architecture {
            rule("classes.rule") {
                classes {
                    that().haveName("FileLevelSuppressedClass")
                    should().beInterfaces()
                }
            }
            rule("functions.rule") {
                functions {
                    that().haveName("fileLevelSuppressedFunc")
                    should().haveNameEndingWith("Allowed")
                }
            }
        }
    }

    @Test
    fun `in-source member level function and property suppressions succeed`() {
        Konture.architecture {
            rule("functions.rule") {
                functions {
                    that().haveName("suppressedMemberFunc")
                    should().haveNameEndingWith("NonExistentSuffix")
                }
            }
            rule("properties.rule") {
                properties {
                    that().haveName("suppressedMemberProp")
                    should().beConst()
                }
            }
        }
    }

    @Test
    fun `programmatic class suppression with mandatory reason succeeds`() {
        Konture.classes {
            that().haveName("ProgrammaticTargetClass")
            suppress {
                classFqName(
                    "io.github.baole.konture.tests.suppression.ProgrammaticTargetClass",
                    reason = "Exempted for legacy migration KT-123",
                )
            }
            should().beInterfaces()
        }
    }

    @Test
    fun `programmatic function suppression with reason succeeds`() {
        Konture.functions {
            that().haveName("failingFunction")
            suppress {
                function("failingFunction", reason = "Deprecated function refactor planned in KT-456")
            }
            should().haveNameEndingWith("MandatorySuffix")
        }
    }

    @Test
    fun `programmatic property suppression with reason succeeds`() {
        Konture.properties {
            that().haveName("failingProperty")
            suppress {
                property("failingProperty", reason = "Non-const property tolerated until v2.0")
            }
            should().beConst()
        }
    }

    @Test
    fun `programmatic file suppression with reason succeeds`() {
        Konture.files {
            that().haveName("ProgrammaticSuppressionTargets.kt")
            suppress {
                file("ProgrammaticSuppressionTargets.kt", reason = "Special test fixture file")
            }
            should().notHaveImportOf("io.github.baole.konture.tests.suppression")
        }
    }

    @Test
    fun `programmatic module suppression with reason succeeds`() {
        Konture.modules {
            that().haveNamePath(":konture-test")
            suppress {
                module(":konture-test", reason = "Allow konture-test module to depend on core")
            }
            should().notDependOnModule(":core")
        }
    }

    @Test
    fun `programmatic slice suppression with reason succeeds`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.(*)..")
            that().haveName("suppression")
            suppress {
                slice("suppression", reason = "Suppression test fixtures slice")
            }
            should().notContainClasses()
        }
    }

    @Test
    fun `architecture batch block with mixed suppressions passes`() {
        Konture.architecture {
            classes {
                that().haveName("ProgrammaticTargetClass")
                suppress {
                    classes(reason = "Batch arch test exemption") { it.name == "ProgrammaticTargetClass" }
                }
                should().beInterfaces()
            }

            functions {
                that().haveName("failingFunction")
                suppress {
                    functions(reason = "Batch arch test exemption") { it.declaration.name == "failingFunction" }
                }
                should().haveNameEndingWith("Allowed")
            }

            properties {
                that().haveName("failingProperty")
                suppress {
                    properties(reason = "Batch arch test exemption") { it.declaration.name == "failingProperty" }
                }
                should().beConst()
            }
        }
    }

    @Test
    fun `unsuppressed target fails check as expected`() {
        assertThrows(AssertionError::class.java) {
            Konture.classes {
                that().haveName("UnsuppressedTargetClass")
                should().beInterfaces()
            }
        }
    }
}
