/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PsiParserTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test PsiParser parses Kotlin classes correctly`() {
        val ktContent =
            """
            package com.example.domain

            import com.example.annotations.UseCase
            import com.example.models.User
            import java.util.UUID

            @UseCase
            abstract class MyService : BaseService {
                val id: UUID = UUID.randomUUID()

                fun execute(user: User): String {
                    return user.name
                }
            }

            interface BaseService {
                fun doSomething()
            }
            """.trimIndent()

        val tempFile =
            File(tempDir, "MyService.kt").apply {
                writeText(ktContent)
            }

        val fileDecl = PsiParser.parseFile(tempFile)
        assertNotNull(fileDecl)
        assertEquals("MyService.kt", fileDecl!!.name)
        assertEquals("com.example.domain", fileDecl.packageName)

        val classes = fileDecl.classes
        assertEquals(2, classes.size)

        // Verify MyService
        val serviceClass = classes.first { it.name == "MyService" }
        assertEquals("com.example.domain.MyService", serviceClass.fqName)
        assertEquals("com.example.domain", serviceClass.packageName)
        assertTrue(serviceClass.isAbstract)
        assertFalse(serviceClass.isInterface)
        assertEquals(io.github.baole.konture.Visibility.PUBLIC, serviceClass.visibility)
        assertTrue(serviceClass.modifiers.contains(io.github.baole.konture.Modifier.ABSTRACT))
        assertTrue(serviceClass.supertypes.contains("BaseService"))

        // Verify annotations
        assertEquals(1, serviceClass.annotations.size)
        val annotation = serviceClass.annotations.first()
        assertEquals("UseCase", annotation.name)
        assertEquals("com.example.annotations.UseCase", annotation.fqName)

        // Verify imports
        assertTrue(serviceClass.imports.contains("com.example.annotations.UseCase"))
        assertTrue(serviceClass.imports.contains("com.example.models.User"))
        assertTrue(serviceClass.imports.contains("java.util.UUID"))

        // Verify referenced types
        assertTrue(serviceClass.referencedTypes.contains("UUID"))
        assertTrue(serviceClass.referencedTypes.contains("User"))
        assertTrue(serviceClass.referencedTypes.contains("BaseService"))

        // Verify functions & properties
        assertEquals(1, serviceClass.properties.size)
        val property = serviceClass.properties.first()
        assertEquals("id", property.name)
        assertEquals("UUID", property.type)
        assertTrue(property.isVal)

        assertEquals(1, serviceClass.functions.size)
        val function = serviceClass.functions.first()
        assertEquals("execute", function.name)
        assertEquals("String", function.returnType)
        assertEquals(1, function.parameters.size)
        assertEquals("user", function.parameters.first().name)
        assertEquals("User", function.parameters.first().type)

        // Verify BaseService interface
        val interfaceClass = classes.first { it.name == "BaseService" }
        assertTrue(interfaceClass.isInterface)
        assertFalse(interfaceClass.isAbstract)
        assertEquals("com.example.domain.BaseService", interfaceClass.fqName)
    }

    @Test
    fun `test PsiParser parses property features correctly`() {
        val ktContent =
            """
            package com.example.domain

            class PropertyHolder {
                var mutableProp: String = ""
                lateinit var lateinitProp: String

                companion object {
                    const val CONST_PROP: String = "const"
                }
            }

            val String.extensionProp: Int
                get() = this.length
            """.trimIndent()

        val tempFile =
            File(tempDir, "Properties.kt").apply {
                writeText(ktContent)
            }

        val fileDecl = PsiParser.parseFile(tempFile)
        assertNotNull(fileDecl)

        val holderClass = fileDecl!!.classes.first { it.name == "PropertyHolder" }

        // Verify mutableProp is var (isVal == false, isVar == true)
        val mutableProp = holderClass.properties.first { it.name == "mutableProp" }
        assertFalse(mutableProp.isVal)
        assertTrue(mutableProp.isVar)
        assertFalse(mutableProp.isExtension)

        // Verify lateinitProp has LATEINIT modifier
        val lateinitProp = holderClass.properties.first { it.name == "lateinitProp" }
        assertTrue(lateinitProp.isVar)
        assertTrue(lateinitProp.modifiers.contains(io.github.baole.konture.Modifier.LATEINIT))

        // Verify companion object const val
        val companion = holderClass.companionObject
        assertNotNull(companion)
        val constProp = companion!!.properties.first { it.name == "CONST_PROP" }
        assertTrue(constProp.isVal)
        assertFalse(constProp.isVar)
        assertTrue(constProp.modifiers.contains(io.github.baole.konture.Modifier.CONST))

        // Verify top-level extension property
        assertEquals(1, fileDecl.topLevelProperties.size)
        val extProp = fileDecl.topLevelProperties.first()
        assertEquals("extensionProp", extProp.name)
        assertTrue(extProp.isExtension)
    }

    @Test
    fun `test PsiParser parses import aliases annotations and enums correctly`() {
        val ktContent =
            """
            package com.example.domain

            import com.example.annotations.UseCase as UseCaseAlias
            import com.example.models.User as UserAlias
            import java.util.List

            @UseCaseAlias(name = "test", priority = 1)
            enum class Status {
                ACTIVE, INACTIVE
            }

            class Controller {
                val users: List<UserAlias> = emptyList()
            }
            """.trimIndent()

        val tempFile =
            File(tempDir, "EnumAndAlias.kt").apply {
                writeText(ktContent)
            }

        val fileDecl = PsiParser.parseFile(tempFile)
        assertNotNull(fileDecl)

        // Verify import aliases map
        val aliases = fileDecl!!.importAliases
        assertEquals("com.example.annotations.UseCase", aliases["UseCaseAlias"])
        assertEquals("com.example.models.User", aliases["UserAlias"])

        // Verify Status enum
        val statusClass = fileDecl.classes.first { it.name == "Status" }
        assertTrue(statusClass.isEnum)
        assertFalse(statusClass.isInterface)

        // Verify annotation FQN was resolved using import alias
        val ann = statusClass.annotations.first()
        assertEquals("UseCaseAlias", ann.name)
        assertEquals("com.example.annotations.UseCase", ann.fqName)

        // Verify annotation arguments
        assertEquals(2, ann.arguments.size)
        val nameArg = ann.arguments.first { it.name == "name" }
        assertEquals("\"test\"", nameArg.value)
        val priorityArg = ann.arguments.first { it.name == "priority" }
        assertEquals("1", priorityArg.value)

        // Verify Controller generic type reference extraction (UserAlias)
        val controllerClass = fileDecl.classes.first { it.name == "Controller" }
        assertTrue(controllerClass.referencedTypes.contains("List"))
        assertTrue(controllerClass.referencedTypes.contains("UserAlias"))
    }

    @Test
    fun `test parse non-existent file returns null`() {
        val nonExistent = File(tempDir, "DoesNotExist.kt")
        val fileDecl = PsiParser.parseFile(nonExistent)
        org.junit.jupiter.api.Assertions
            .assertNull(fileDecl)
    }

    @Test
    fun `test top level declarations`() {
        val ktContent =
            """
            package com.example.toplevel

            val topProp = 42

            fun topFun(): String = "hello"
            """.trimIndent()
        val file = File(tempDir, "TopLevel.kt").apply { writeText(ktContent) }
        val fileDecl = PsiParser.parseFile(file)
        assertNotNull(fileDecl)
        assertEquals(1, fileDecl!!.topLevelFunctions.size)
        assertEquals("topFun", fileDecl.topLevelFunctions.first().name)
        assertEquals("String", fileDecl.topLevelFunctions.first().returnType)

        assertEquals(1, fileDecl.topLevelProperties.size)
        assertEquals("topProp", fileDecl.topLevelProperties.first().name)
        assertTrue(fileDecl.topLevelProperties.first().isVal)
    }

    @Test
    fun `test KDoc parsing`() {
        val ktContent =
            """
            /**
             * File-level KDoc
             */
            package com.example.kdoc

            /**
             * Class KDoc
             */
            class KDocClass {
                /**
                 * Prop KDoc
                 */
                val prop: Int = 0

                /**
                 * Fun KDoc
                 */
                fun foo() {}
            }
            """.trimIndent()
        val file = File(tempDir, "KDoc.kt").apply { writeText(ktContent) }
        val fileDecl = PsiParser.parseFile(file)
        assertNotNull(fileDecl)
        assertTrue(fileDecl!!.kdocText!!.contains("File-level KDoc"))

        val clazz = fileDecl.classes.first()
        assertTrue(clazz.kdocText!!.contains("Class KDoc"))
        assertTrue(
            clazz.properties
                .first()
                .kdocText!!
                .contains("Prop KDoc"),
        )
        assertTrue(
            clazz.functions
                .first()
                .kdocText!!
                .contains("Fun KDoc"),
        )
    }

    @Test
    fun `test modifiers and nested companion structures`() {
        val ktContent =
            """
            package com.example.mods

            sealed class SealedClass

            inner class InnerClass

            @JvmInline
            value class ValueClass(val x: Int)

            class HostClass {
                companion object {
                    const val CONSTANT = "const"
                }

                lateinit var lateinitProp: String

                suspend fun asyncFun() {}
            }
            """.trimIndent()
        val file = File(tempDir, "Modifiers.kt").apply { writeText(ktContent) }
        val fileDecl = PsiParser.parseFile(file)
        assertNotNull(fileDecl)

        val sealed = fileDecl!!.classes.first { it.name == "SealedClass" }
        assertTrue(sealed.modifiers.contains(io.github.baole.konture.Modifier.SEALED))

        val inner = fileDecl.classes.first { it.name == "InnerClass" }
        assertTrue(inner.modifiers.contains(io.github.baole.konture.Modifier.INNER))

        val valueClass = fileDecl.classes.first { it.name == "ValueClass" }
        assertTrue(valueClass.modifiers.contains(io.github.baole.konture.Modifier.VALUE))

        val hostClass = fileDecl.classes.first { it.name == "HostClass" }
        assertNotNull(hostClass.companionObject)
        assertTrue(hostClass.companionObject!!.modifiers.contains(io.github.baole.konture.Modifier.COMPANION))
        assertTrue(hostClass.companionObject.modifiers.contains(io.github.baole.konture.Modifier.OBJECT))

        val prop = hostClass.properties.first { it.name == "lateinitProp" }
        assertTrue(prop.modifiers.contains(io.github.baole.konture.Modifier.LATEINIT))
        assertFalse(prop.isVal)

        val funDecl = hostClass.functions.first { it.name == "asyncFun" }
        assertTrue(funDecl.modifiers.contains(io.github.baole.konture.Modifier.SUSPEND))
    }

    @Test
    fun `test constructor parameter defaults and annotations`() {
        val ktContent =
            """
            package com.example.constructor

            class ConstructorTest @Inject constructor(
                @ParamAnn val param1: String = "default",
                val param2: Int
            )
            """.trimIndent()
        val file = File(tempDir, "ConstructorTest.kt").apply { writeText(ktContent) }
        val fileDecl = PsiParser.parseFile(file)
        assertNotNull(fileDecl)
        val clazz = fileDecl!!.classes.first()
        val primary = clazz.primaryConstructor
        assertNotNull(primary)

        // constructor annotations
        assertTrue(primary!!.annotations.any { it.name == "Inject" })

        val p1 = primary.parameters.first { it.name == "param1" }
        assertTrue(p1.hasDefaultValue)
        assertTrue(p1.annotations.any { it.name == "ParamAnn" })

        val p2 = primary.parameters.first { it.name == "param2" }
        assertFalse(p2.hasDefaultValue)
        assertTrue(p2.annotations.isEmpty())
    }

    @Test
    fun `test dispose cleanup`() {
        // Verify call doesn't crash and disposes correctly
        PsiParser.dispose()
    }
}
