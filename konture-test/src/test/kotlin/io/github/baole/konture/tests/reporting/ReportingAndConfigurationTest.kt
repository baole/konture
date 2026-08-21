/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.reporting

import io.github.baole.konture.Konture
import io.github.baole.konture.classes
import io.github.baole.konture.core.model.Severity
import io.github.baole.konture.functions
import io.github.baole.konture.properties
import io.github.baole.konture.rule
import org.junit.jupiter.api.Test

class ReportingAndConfigurationTest {
    private val classStructurePkg = "io.github.baole.konture.tests.classstructure"
    private val signaturePkg = "io.github.baole.konture.tests.signatures"

    @Test
    fun `named rule DSL with metadata, severity, and tags executes successfully`() {
        val archRule =
            rule("domain.model.integrity") {
                description = "Ensure domain classes and functions adhere to architectural standards"
                severity = Severity.ERROR
                tag("architecture", "domain")

                classes {
                    that().resideInAPackage(classStructurePkg).and().haveName("SampleInterface")
                    should().beInterfaces()
                }

                functions {
                    that().resideInAPackage(signaturePkg).and().haveName("topLevelCustomAction")
                    should().beTopLevel()
                }

                properties {
                    that().resideInAPackage(signaturePkg).and().haveName("TOP_LEVEL_CONST_PROP")
                    should().beConst()
                }
            }

        archRule.check()
    }

    @Test
    fun `custom predicate satisfy assertion on classes`() {
        Konture.classes {
            that().resideInAPackage(classStructurePkg).and().haveName("SampleOpenClass")
            should().satisfy { cls: io.github.baole.konture.ClassDeclaration ->
                cls.name.startsWith("Sample") && cls.functions.isNotEmpty()
            }
        }
    }

    @Test
    fun `custom predicate satisfy assertion on functions`() {
        Konture.functions {
            that().resideInAPackage(signaturePkg).and().haveName("memberProcessMethod")
            should().satisfy { fn ->
                fn.declaration.parameters.size == 2
            }
        }
    }

    @Test
    fun `custom predicate satisfy assertion on properties`() {
        Konture.properties {
            that().resideInAPackage(signaturePkg).and().haveName("memberValProp")
            should().satisfy { prop ->
                prop.declaration.isVal && prop.declaration.visibility == io.github.baole.konture.Visibility.PUBLIC
            }
        }
    }
}
