/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.signatures

const val TOP_LEVEL_CONST_PROP: String = "const_value"

var topLevelVarProp: Int = 42

fun topLevelCustomAction(input: String): String = "result:$input"

class SignatureContainer {
    val memberValProp: Double = 3.14

    lateinit var memberLateinitProp: String

    fun memberProcessMethod(count: Int, flag: Boolean): String = "$count-$flag"

    infix fun memberInfixAction(param: String): String = "infix:$param"

    operator fun plus(other: SignatureContainer): SignatureContainer = this

    inline fun inlineExecute(block: () -> Unit) {
        block()
    }

    suspend fun suspendFetch(): String = "fetched"
}
