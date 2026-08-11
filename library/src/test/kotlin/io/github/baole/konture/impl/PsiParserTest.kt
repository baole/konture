/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.KontureScopeTestFixture
import io.github.baole.konture.Modifier
import io.github.baole.konture.Visibility
import io.github.baole.konture.impl.psi.MapSymbolLookup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class PsiParserTest : KontureScopeTestFixture() {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test PsiParser parses Kotlin classes correctly`() {
        val file =
            File(tempDir, "SampleClass.kt").apply {
                writeText(
                    """
                    package com.example.domain

                    import com.example.data.User

                    class SampleClass : BaseClass() {
                        fun execute(user: User): String {
                            return user.name
                        }
                    }

                    interface SampleInterface {
                        fun doSomething()
                    }
                    """.trimIndent(),
                )
            }

        val parsedFile = PsiParser.parseFile(file)
        assertNotNull(parsedFile)
        assertEquals("SampleClass.kt", parsedFile!!.name)
        assertEquals("com.example.domain", parsedFile.packageName)

        val classes = parsedFile.classes
        assertEquals(2, classes.size)

        val sampleClass = classes[0]
        assertEquals("SampleClass", sampleClass.name)
        assertEquals("com.example.domain.SampleClass", sampleClass.fqName)
        assertEquals(false, sampleClass.isInterface)
        assertEquals(listOf("BaseClass"), sampleClass.supertypes)

        val sampleInterface = classes[1]
        assertEquals("SampleInterface", sampleInterface.name)
        assertEquals(true, sampleInterface.isInterface)
    }

    @Test
    fun `test PsiParser parses property features correctly`() {
        val file =
            File(tempDir, "PropertyFeatures.kt").apply {
                writeText(
                    """
                    package com.example

                    class PropertyFeatures {
                        val valProp: String = "val"
                        var varProp: Int = 1
                        val delegateProp: String by lazy { "delegate" }
                        val String.extensionProp: Int get() = length
                    }
                    """.trimIndent(),
                )
            }

        val parsedFile = PsiParser.parseFile(file)
        assertNotNull(parsedFile)

        val properties = parsedFile!!.classes.single().properties
        assertEquals(4, properties.size)

        val valProp = properties.single { it.name == "valProp" }
        assertTrue(valProp.isVal)

        val varProp = properties.single { it.name == "varProp" }
        assertFalse(varProp.isVal)

        val delegateProp = properties.single { it.name == "delegateProp" }
        assertEquals("delegateProp", delegateProp.name)

        val extensionProp = properties.single { it.name == "extensionProp" }
        assertTrue(extensionProp.isExtension)
    }

    @Test
    fun `test PsiParser parses import aliases annotations and enums correctly`() {
        val file =
            File(tempDir, "ImportAliasesAndEnums.kt").apply {
                writeText(
                    """
                    package com.example

                    import com.example.data.User as UserAlias
                    import com.example.data.User

                    @Deprecated("Use new API")
                    enum class UserRole {
                        ADMIN, USER
                    }

                    class Service {
                        fun load(user: UserAlias): UserAlias = user
                    }
                    """.trimIndent(),
                )
            }

        val parsedFile = PsiParser.parseFile(file)
        assertNotNull(parsedFile)
        assertEquals(
            listOf("com.example.data.User as UserAlias", "com.example.data.User"),
            parsedFile!!.imports,
        )

        val enumClass = parsedFile.classes.single { it.name == "UserRole" }
        assertTrue(enumClass.isEnum)
        assertEquals(1, enumClass.annotations.size)
        assertEquals("Deprecated", enumClass.annotations.single().name)

        val serviceClass = parsedFile.classes.single { it.name == "Service" }
        val function = serviceClass.functions.single()
        assertEquals("UserAlias", function.returnType)
        assertEquals("UserAlias", function.parameters.single().type)
    }

    @Test
    fun `resolves nested explicit imports default types and same package types`() {
        val outerFile =
            File(tempDir, "Outer.kt").apply {
                writeText(
                    """
                    package domain

                    class Outer {
                        class Inner
                    }
                    class SamePackageType
                    """.trimIndent(),
                )
            }
        val consumerFile =
            File(tempDir, "Consumer.kt").apply {
                writeText(
                    """
                    package app
                    import domain.Outer
                    import domain.SamePackageType

                    class Consumer {
                        fun load(value: Outer.Inner, local: SamePackageType): Result<String> = TODO()
                    }
                    """.trimIndent(),
                )
            }

        val lookup = MapSymbolLookup(setOf("domain.Outer", "domain.Outer.Inner", "domain.SamePackageType"))
        val consumer = PsiParser.parseFile(consumerFile, lookup)!!.classes.single()
        val load = consumer.functions.single()

        assertEquals("domain.Outer.Inner", load.parameters[0].resolvedType)
        assertEquals("domain.SamePackageType", load.parameters[1].resolvedType)
        assertEquals("kotlin.Result", load.resolvedReturnType)
    }

    @Test
    fun `test parse non-existent file returns null`() {
        val nonExistentFile = File(tempDir, "DoesNotExist.kt")
        val parsedFile = PsiParser.parseFile(nonExistentFile)
        assertNull(parsedFile)
    }

    @Test
    fun `extracts resolved calls and class references without import-only false positives`() {
        val file =
            File(tempDir, "UsageExtraction.kt").apply {
                writeText(
                    """
                    package app
                    import io.mockk.spyk
                    import io.mockk.spyk as partialSpy

                    class UsageExtraction {
                        fun direct() = io.mockk.spyk(this)
                        fun alias() = partialSpy(this)
                        fun wildcard() = spyk(this)
                        fun local() { fun spyk(value: Any) = value; spyk(this) }
                    }
                    """.trimIndent(),
                )
            }

        val declaration = PsiParser.parseFile(file)!!
        val usages = declaration.usages

        assertTrue(usages.any { it.targetFqName == "io.mockk.spyk" && it.enclosingFunction == "direct" })
        assertTrue(usages.any { it.targetFqName == "io.mockk.spyk" && it.enclosingFunction == "alias" })
        assertTrue(usages.any { it.targetFqName == "io.mockk.spyk" && it.enclosingFunction == "wildcard" })
        assertFalse(usages.any { it.enclosingFunction == "local" && it.targetFqName == "io.mockk.spyk" })
    }

    @Test
    fun `test top level declarations`() {
        val file =
            File(tempDir, "TopLevel.kt").apply {
                writeText(
                    """
                    package com.example

                    val topProp: Int = 42
                    fun topFun(): String = "hello"
                    """.trimIndent(),
                )
            }

        val parsedFile = PsiParser.parseFile(file)
        assertNotNull(parsedFile)
        assertEquals(1, parsedFile!!.topLevelProperties.size)
        assertEquals("topProp", parsedFile.topLevelProperties.first().name)

        assertEquals(1, parsedFile.topLevelFunctions.size)
        assertEquals("topFun", parsedFile.topLevelFunctions.first().name)
    }

    @Test
    fun `test KDoc parsing`() {
        val file =
            File(tempDir, "KDocClass.kt").apply {
                writeText(
                    """
                    package com.example

                    /**
                     * This is a sample class with KDoc.
                     * @author test
                     */
                    class KDocClass {
                        /**
                         * A function doc.
                         */
                        fun foo() {}
                    }
                    """.trimIndent(),
                )
            }

        val parsedFile = PsiParser.parseFile(file)
        assertNotNull(parsedFile)

        val kdocClass = parsedFile!!.classes.single()
        assertNotNull(kdocClass.kdocText)
        assertTrue(kdocClass.kdocText!!.contains("This is a sample class with KDoc."))

        val fooFunc = kdocClass.functions.single()
        assertNotNull(fooFunc.kdocText)
        assertTrue(fooFunc.kdocText!!.contains("A function doc."))
    }

    @Test
    fun `test modifiers and nested companion structures`() {
        val file =
            File(tempDir, "ModifiersClass.kt").apply {
                writeText(
                    """
                    package com.example

                    abstract class ModifiersClass {
                        protected open fun openFun() {}
                        private suspend fun asyncFun() {}

                        companion object {
                            const val CONST_VAL = "const"
                        }
                    }
                    """.trimIndent(),
                )
            }

        val parsedFile = PsiParser.parseFile(file)
        assertNotNull(parsedFile)

        val cls = parsedFile!!.classes.single { it.name == "ModifiersClass" }
        assertTrue(cls.isAbstract)

        val openFun = cls.functions.single { it.name == "openFun" }
        assertEquals(Visibility.PROTECTED, openFun.visibility)
        assertTrue(openFun.modifiers.contains(Modifier.OPEN))

        val asyncFun = cls.functions.single { it.name == "asyncFun" }
        assertEquals(Visibility.PRIVATE, asyncFun.visibility)
        assertTrue(asyncFun.modifiers.contains(Modifier.SUSPEND))
    }

    @Test
    fun `test constructor parameter defaults and annotations`() {
        val file =
            File(tempDir, "ConstructorParams.kt").apply {
                writeText(
                    """
                    package com.example

                    class ConstructorParams(
                        @Deprecated("old") val name: String = "default",
                        var count: Int
                    )
                    """.trimIndent(),
                )
            }

        val parsedFile = PsiParser.parseFile(file)
        assertNotNull(parsedFile)

        val cls = parsedFile!!.classes.single()
        val primaryCtor = cls.primaryConstructor
        assertNotNull(primaryCtor)
        assertEquals(2, primaryCtor!!.parameters.size)

        val nameParam = primaryCtor.parameters.single { it.name == "name" }
        assertTrue(nameParam.hasDefaultValue)
        assertEquals(1, nameParam.annotations.size)

        val countParam = primaryCtor.parameters.single { it.name == "count" }
        assertFalse(countParam.hasDefaultValue)
    }

    @Test
    fun `test star import and default import resolution with symbol lookup`() {
        val file =
            File(tempDir, "StarImport.kt").apply {
                writeText(
                    """
                    package app
                    import domain.*

                    class Consumer {
                        fun f(result: Result, list: List, ext: ExternalType) {}
                    }
                    """.trimIndent(),
                )
            }

        val lookup = MapSymbolLookup(setOf("domain.ExternalType"))
        val parsed = PsiParser.parseFile(file, lookup)!!
        val consumer = parsed.classes.single()
        val f = consumer.functions.single()

        assertEquals("kotlin.Result", f.parameters[0].resolvedType)
        assertEquals("kotlin.collections.List", f.parameters[1].resolvedType)
        assertEquals("domain.ExternalType", f.parameters[2].resolvedType)
    }

    @Test
    fun `multiple wildcard imports leave ambiguous types unresolved`() {
        val file =
            File(tempDir, "AmbiguousStarImports.kt").apply {
                writeText(
                    """
                    package app
                    import first.*
                    import second.*

                    class Consumer {
                        fun use(value: ExternalType) = value
                    }
                    """.trimIndent(),
                )
            }

        val lookup = MapSymbolLookup(setOf("first.ExternalType", "second.ExternalType"))

        val parameter = PsiParser.parseFile(file, lookup)!!.classes.single().functions.single().parameters.single()

        assertEquals(null, parameter.resolvedType)
    }
}
