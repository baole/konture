/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.classstructure

annotation class CustomConfig(val key: String, val value: String)

interface SampleInterface {
    fun interfaceMethod()
}

abstract class SampleAbstractBase : SampleInterface {
    abstract val abstractProp: String
}

open class SampleOpenClass : SampleAbstractBase() {
    override val abstractProp: String = "open"
    override fun interfaceMethod() {}
    open fun openFunc() {}
}

enum class SampleEnum {
    FIRST,
    SECOND,
}

@JvmInline
value class SampleValueClass(val raw: String)

class OuterContainer {
    inner class InnerMember
    class NestedStatic
}

@CustomConfig(key = "feature", value = "enabled")
data class ConfiguredDataClass(
    val title: String,
    val count: Int,
) : SampleInterface {
    override fun interfaceMethod() {}
    fun customMethod() {}
}
