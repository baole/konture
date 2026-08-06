/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

import io.github.baole.konture.Modifier
import io.github.baole.konture.impl.PsiParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DeclarationParserTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test parseClassOrObjectWithNested discovers nested types data objects data classes and interfaces`() {
        val file =
            File(tempDir, "NestedTest.kt").apply {
                writeText(
                    """
                    package com.example.nested

                    interface NestedType

                    object Namespace {
                        interface NestedInterface : NestedType

                        data object DataObject : NestedType

                        data class DataClass(val x: Int) : NestedType {
                            fun innerFun(): String = "hello"
                        }
                    }
                    """.trimIndent(),
                )
            }

        val fileDecl = PsiParser.parseFile(file)
        assertNotNull(fileDecl)

        val classes = fileDecl!!.classes
        assertEquals(5, classes.size)

        val topLevelInterface = classes.single { it.name == "NestedType" }
        assertEquals("com.example.nested.NestedType", topLevelInterface.fqName)
        assertTrue(topLevelInterface.isInterface)

        val namespaceObj = classes.single { it.name == "Namespace" }
        assertEquals("com.example.nested.Namespace", namespaceObj.fqName)
        assertTrue(namespaceObj.modifiers.contains(Modifier.OBJECT))

        val nestedInterface = classes.single { it.name == "NestedInterface" }
        assertEquals("com.example.nested.Namespace.NestedInterface", nestedInterface.fqName)
        assertTrue(nestedInterface.isInterface)
        assertTrue(nestedInterface.supertypes.any { it == "NestedType" || it == "com.example.nested.NestedType" })

        val dataObj = classes.single { it.name == "DataObject" }
        assertEquals("com.example.nested.Namespace.DataObject", dataObj.fqName)
        assertTrue(dataObj.modifiers.contains(Modifier.DATA))
        assertTrue(dataObj.modifiers.contains(Modifier.OBJECT))
        assertTrue(dataObj.supertypes.any { it == "NestedType" || it == "com.example.nested.NestedType" })

        val dataCls = classes.single { it.name == "DataClass" }
        assertEquals("com.example.nested.Namespace.DataClass", dataCls.fqName)
        assertTrue(dataCls.modifiers.contains(Modifier.DATA))
        assertTrue(dataCls.supertypes.any { it == "NestedType" || it == "com.example.nested.NestedType" })

        val innerFun = dataCls.functions.single { it.name == "innerFun" }
        assertEquals("String", innerFun.returnType)
    }

    @Test
    fun `test parseClassOrObjectWithNested discovers companion objects and their member functions`() {
        val file =
            File(tempDir, "CompanionTest.kt").apply {
                writeText(
                    """
                    package com.example.companion

                    class Host {
                        companion object Factory {
                            fun create(): Host = Host()
                        }
                    }
                    """.trimIndent(),
                )
            }

        val fileDecl = PsiParser.parseFile(file)
        assertNotNull(fileDecl)

        val classes = fileDecl!!.classes
        assertEquals(2, classes.size)

        val host = classes.single { it.name == "Host" }
        assertEquals("com.example.companion.Host", host.fqName)

        val factory = classes.single { it.name == "Factory" }
        assertEquals("com.example.companion.Host.Factory", factory.fqName)
        assertTrue(factory.modifiers.contains(Modifier.COMPANION))
        assertTrue(factory.modifiers.contains(Modifier.OBJECT))

        val createFun = factory.functions.single { it.name == "create" }
        assertEquals("Host", createFun.returnType)
    }

    @Test
    fun `test parseClassOrObjectWithNested discovers nested class in interface inner class enum and fun interface`() {
        val file =
            File(tempDir, "SyntaxVariantsTest.kt").apply {
                writeText(
                    """
                    package com.example.syntax

                    interface HostInterface {
                        class ClassInInterface
                    }

                    class HostClass {
                        interface InterfaceInClass
                        inner class InnerClass
                        enum class EnumInClass { A, B }
                    }

                    object HostObject {
                        fun interface FunInterface
                    }
                    """.trimIndent(),
                )
            }

        val fileDecl = PsiParser.parseFile(file)
        assertNotNull(fileDecl)

        val classes = fileDecl!!.classes
        assertEquals(10, classes.size)

        val classInInterface = classes.single { it.name == "ClassInInterface" }
        assertEquals("com.example.syntax.HostInterface.ClassInInterface", classInInterface.fqName)

        val interfaceInClass = classes.single { it.name == "InterfaceInClass" }
        assertEquals("com.example.syntax.HostClass.InterfaceInClass", interfaceInClass.fqName)
        assertTrue(interfaceInClass.isInterface)

        val innerClass = classes.single { it.name == "InnerClass" }
        assertEquals("com.example.syntax.HostClass.InnerClass", innerClass.fqName)
        assertTrue(innerClass.modifiers.contains(Modifier.INNER))

        val enumInClass = classes.single { it.name == "EnumInClass" }
        assertEquals("com.example.syntax.HostClass.EnumInClass", enumInClass.fqName)
        assertTrue(enumInClass.isEnum)

        val funInterface = classes.single { it.name == "FunInterface" }
        assertEquals("com.example.syntax.HostObject.FunInterface", funInterface.fqName)
        assertTrue(funInterface.isInterface)
    }
}
