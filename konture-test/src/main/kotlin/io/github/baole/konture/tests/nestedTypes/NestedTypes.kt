/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.nestedTypes

interface NestedType

object Namespace {
    data object DataObject : NestedType

    data class DataClass(val x: Int) : NestedType
}

interface OuterInterface {
    class NestedClassInInterface : NestedType

    data object DataObjInInterface : NestedType

    data class DataClsInInterface(val name: String) : NestedType
}

class OuterClass {
    interface NestedInterfaceInClass : NestedType

    object NestedObjectInClass : NestedType

    inner class InnerClassInClass : NestedType

    enum class NestedEnumInClass : NestedType {
        FIRST,
        SECOND,
    }

    companion object {
        class ClassInCompanion : NestedType
    }
}

object OuterObject {
    fun interface FunctionalInterfaceInObject : NestedType {
        fun execute()
    }

    object Level2Object {
        interface Level3Interface : NestedType
    }
}
