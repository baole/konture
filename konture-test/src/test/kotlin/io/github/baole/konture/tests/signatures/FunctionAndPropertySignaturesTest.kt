/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.signatures

import io.github.baole.konture.Konture
import io.github.baole.konture.Modifier
import io.github.baole.konture.Visibility
import io.github.baole.konture.functions
import io.github.baole.konture.haveReturnTypeOf
import io.github.baole.konture.properties
import org.junit.jupiter.api.Test

class FunctionAndPropertySignaturesTest {
    private val pkg = "io.github.baole.konture.tests.signatures"

    @Test
    fun `top-level function signature and modifier assertions`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("topLevelCustomAction")
            should().beTopLevel()
                .andShould().bePublic()
                .andShould().haveReturnTypeOf<String>()
                .andShould().haveParameterTypes("kotlin.String")
                .andShould().haveNameStartingWith("topLevel")
                .andShould().haveNameEndingWith("Action")
                .andShould().notBeSuspend()
        }
    }

    @Test
    fun `member function modifiers assertions`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("memberInfixAction")
            should().beMember()
                .andShould().beInfix()
                .andShould().haveModifier(Modifier.INFIX)
        }

        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("plus")
            should().beMember()
                .andShould().beOperator()
        }

        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("inlineExecute")
            should().beMember()
                .andShould().beInline()
        }

        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("suspendFetch")
            should().beMember()
                .andShould().beSuspend()
        }
    }

    @Test
    fun `member function parameter assertions`() {
        Konture.functions {
            that().resideInAPackage(pkg).and().haveName("memberProcessMethod")
            should().haveParameterTypes("kotlin.Int", "kotlin.Boolean")
                .andShould().haveAnyParameterType("kotlin.Int")
        }
    }

    @Test
    fun `top-level property modifier and type assertions`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("TOP_LEVEL_CONST_PROP")
            should().beTopLevel()
                .andShould().beConst()
                .andShould().beVal()
                .andShould().haveType(String::class)
                .andShould().haveNameStartingWith("TOP_")
                .andShould().notBeLateinit()
        }

        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("topLevelVarProp")
            should().beTopLevel()
                .andShould().beVar()
                .andShould().haveType(Int::class)
                .andShould().notBeConst()
        }
    }

    @Test
    fun `member property modifier and type assertions`() {
        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("memberValProp")
            should().beMember()
                .andShould().beVal()
                .andShould().haveType(Double::class)
                .andShould().haveVisibility(Visibility.PUBLIC)
        }

        Konture.properties {
            that().resideInAPackage(pkg).and().haveName("memberLateinitProp")
            should().beMember()
                .andShould().beLateinit()
                .andShould().beVar()
                .andShould().haveType(String::class)
                .andShould().haveNameEndingWith("Prop")
        }
    }
}
