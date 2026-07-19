/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.impl.psi.MapSymbolLookup
import io.github.baole.konture.isAssignableTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@Suppress("LargeClass")
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
        assertTrue(serviceClass.supertypes.contains("com.example.domain.BaseService"))

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
        assertEquals("java.util.UUID", property.resolvedType)
        assertTrue(property.isVal)

        assertEquals(1, serviceClass.functions.size)
        val function = serviceClass.functions.first()
        assertEquals("execute", function.name)
        assertEquals("String", function.returnType)
        assertEquals("kotlin.String", function.resolvedReturnType)
        assertEquals(1, function.parameters.size)
        assertEquals("user", function.parameters.first().name)
        assertEquals("User", function.parameters.first().type)
        assertEquals("com.example.models.User", function.parameters.first().resolvedType)

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
                fun load(user: UserAlias): UserAlias = user
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
        assertEquals("java.util.List", controllerClass.properties.single().resolvedType)
        val load = controllerClass.functions.single { it.name == "load" }
        assertEquals("com.example.models.User", load.resolvedReturnType)
        assertEquals("com.example.models.User", load.parameters.single().resolvedType)
    }

    @Test
    fun `resolves nested explicit imports default types and same package types`() {
        val file =
            File(tempDir, "ResolvedTypes.kt").apply {
                writeText(
                    """
                    package com.example.domain

                    import com.example.models.Outer
                    import other.package.*

                    class SamePackageType

                    class Consumer {
                        fun load(value: Outer.Inner, local: SamePackageType): Result<String> = TODO()
                    }
                    """.trimIndent(),
                )
            }

        val consumer = PsiParser.parseFile(file)!!.classes.single { it.name == "Consumer" }
        val load = consumer.functions.single()

        assertEquals("com.example.models.Outer.Inner", load.parameters[0].resolvedType)
        assertEquals("com.example.domain.SamePackageType", load.parameters[1].resolvedType)
        assertEquals("kotlin.Result", load.resolvedReturnType)
    }

    @Test
    fun `test parse non-existent file returns null`() {
        val nonExistent = File(tempDir, "DoesNotExist.kt")
        val fileDecl = PsiParser.parseFile(nonExistent)
        org.junit.jupiter.api.Assertions
            .assertNull(fileDecl)
    }

    @Test
    fun `extracts resolved calls and class references without import-only false positives`() {
        val file =
            File(tempDir, "Usages.kt").apply {
                writeText(
                    """
                    package example
                    import io.mockk.spyk as partialSpy
                    import io.mockk.MockK
                    import io.mockk.*

                    @MockK class Subject : MockK {
                        fun direct() = io.mockk.spyk(this)
                        fun alias() = partialSpy(this)
                        fun wildcard() = spyk(this)
                        fun local() { fun spyk(value: Any) = value; spyk(this) }
                        val type: MockK? = null
                        val literal = MockK::class
                    }
                    """.trimIndent(),
                )
            }

        val declaration = PsiParser.parseFile(file)!!
        val mockkCalls = declaration.usages.filter { it.kind == io.github.baole.konture.UsageKind.CALL && it.targetFqName == "io.mockk.spyk" }
        assertEquals(3, mockkCalls.size)
        assertTrue(mockkCalls.all { it.line > 0 && it.column > 0 })
        assertTrue(mockkCalls.none { it.enclosingFunction == "local" })
        assertTrue(declaration.usages.any { it.kind == io.github.baole.konture.UsageKind.CLASS_REFERENCE && it.targetFqName == "io.mockk.MockK" })
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
    fun `test star import and default import resolution with symbol lookup`() {
        val ktContent =
            """
            package app
            import external.*

            class MyClass {
                fun f(result: Result, list: List, ext: ExternalType) {}
            }
            """.trimIndent()
        val file = File(tempDir, "ResolverPrecedence.kt").apply { writeText(ktContent) }

        // Case 1: No symbol lookup provided, Result and List map to defaults and a unique wildcard import resolves external types.
        val declNoLookup = PsiParser.parseFile(file)
        assertNotNull(declNoLookup)
        val clazzNoLookup = declNoLookup!!.classes.first()
        val paramsNoLookup = clazzNoLookup.functions.first { it.name == "f" }.parameters

        assertEquals("kotlin.Result", paramsNoLookup[0].resolvedType)
        assertEquals("kotlin.collections.List", paramsNoLookup[1].resolvedType)
        assertEquals("external.ExternalType", paramsNoLookup[2].resolvedType)

        // Case 2: With symbol lookup where app.Result and app.List are declared, external.ExternalType is declared
        val lookup = MapSymbolLookup(setOf("app.Result", "app.List", "external.ExternalType"))
        val declWithLookup = PsiParser.parseFile(file, lookup)
        assertNotNull(declWithLookup)
        val clazzWithLookup = declWithLookup!!.classes.first()
        val paramsWithLookup = clazzWithLookup.functions.first { it.name == "f" }.parameters

        assertEquals("app.Result", paramsWithLookup[0].resolvedType) // Local package declaration takes precedence over default import
        assertEquals("app.List", paramsWithLookup[1].resolvedType) // Local declarations take precedence over default imports
        assertEquals("external.ExternalType", paramsWithLookup[2].resolvedType) // Resolved via wildcard because it is declared in the project!
    }

    @Test
    fun `multiple wildcard imports leave ambiguous types unresolved`() {
        val file =
            File(tempDir, "AmbiguousWildcards.kt").apply {
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

    @Test
    fun `symbol pre-scan excludes local classes`() {
        val file =
            File(tempDir, "LocalClass.kt").apply {
                writeText(
                    """
                    package app

                    fun create() {
                        class Local
                        Local()
                    }
                    """.trimIndent(),
                )
            }

        val declarations = PsiParser.getDeclaredClassFqNames(listOf(file))

        assertFalse("app.Local" in declarations)
    }

    @Test
    fun `resolves Kotlin default imported annotations`() {
        val file =
            File(tempDir, "DefaultAnnotations.kt").apply {
                writeText(
                    """
                    package app

                    @Deprecated("Use replacement")
                    @Suppress("unused")
                    class Legacy

                    @Retention(AnnotationRetention.RUNTIME)
                    annotation class RuntimeMarker

                    @OptIn(ExperimentalStdlibApi::class)
                    @JvmInline
                    value class InlineId(val value: String)

                    class JvmApi {
                        @Throws(Exception::class)
                        @Synchronized
                        fun load() = Unit

                        @Volatile
                        @Transient
                        var cache: String = ""
                    }
                    """.trimIndent(),
                )
            }

        val declarations = PsiParser.parseFile(file)!!.classes
        val legacy = declarations.single { it.name == "Legacy" }
        val marker = declarations.single { it.name == "RuntimeMarker" }
        val inlineId = declarations.single { it.name == "InlineId" }
        val jvmApi = declarations.single { it.name == "JvmApi" }

        assertTrue(legacy.annotations.any { it.fqName == "kotlin.Deprecated" })
        assertTrue(legacy.annotations.any { it.fqName == "kotlin.Suppress" })
        assertTrue(marker.annotations.any { it.fqName == "kotlin.annotation.Retention" })
        assertTrue(inlineId.annotations.any { it.fqName == "kotlin.OptIn" })
        assertTrue(inlineId.annotations.any { it.fqName == "kotlin.jvm.JvmInline" })
        assertTrue(jvmApi.functions.single().annotations.any { it.fqName == "kotlin.jvm.Throws" })
        assertTrue(jvmApi.functions.single().annotations.any { it.fqName == "kotlin.jvm.Synchronized" })
        assertTrue(jvmApi.properties.single().annotations.any { it.fqName == "kotlin.jvm.Volatile" })
        assertTrue(jvmApi.properties.single().annotations.any { it.fqName == "kotlin.jvm.Transient" })
    }

    @Test
    fun `resolves local and imported type aliases to their underlying class`() {
        val localAliasFile =
            File(tempDir, "LocalAlias.kt").apply {
                writeText(
                    """
                    package app

                    class User
                    class Result<T>
                    typealias PublicUser = User
                    typealias PublicResult = Result<String>
                    typealias Identity<T> = T
                    typealias ChainedUser = PublicUser
                    typealias LoopA = LoopB
                    typealias LoopB = LoopA

                    class LocalConsumer {
                        fun load(): PublicUser = TODO()
                        fun result(): PublicResult = TODO()
                        fun identity(): Identity<User> = TODO()
                        fun chained(): ChainedUser = TODO()
                        fun cyclic(): LoopA = TODO()
                    }
                    """.trimIndent(),
                )
            }
        val aliasFile =
            File(tempDir, "ImportedAlias.kt").apply {
                writeText(
                    """
                    package api
                    import domain.User

                    typealias PublicUser = User
                    """.trimIndent(),
                )
            }
        val importedAliasConsumer =
            File(tempDir, "ImportedAliasConsumer.kt").apply {
                writeText(
                    """
                    package app
                    import api.PublicUser as ExternalUser

                    class ImportedConsumer {
                        fun load(): ExternalUser = TODO()
                    }
                    """.trimIndent(),
                )
            }

        assertEquals("User", PsiParser.getDeclaredTypeAliases(listOf(localAliasFile))["app.PublicUser"]?.underlyingType)
        val localFunctions = PsiParser.parseFile(localAliasFile)!!.classes.single { it.name == "LocalConsumer" }.functions
        val aliases = PsiParser.getDeclaredTypeAliases(listOf(aliasFile))
        val lookup = MapSymbolLookup(setOf("domain.User"), aliases)
        val importedFunction = PsiParser.parseFile(importedAliasConsumer, lookup)!!.classes.single().functions.single()

        assertEquals("app.User", localFunctions.single { it.name == "load" }.resolvedReturnType)
        assertEquals("app.Result", localFunctions.single { it.name == "result" }.resolvedReturnType)
        assertEquals("app.User", localFunctions.single { it.name == "identity" }.resolvedReturnType)
        assertEquals("app.User", localFunctions.single { it.name == "chained" }.resolvedReturnType)
        assertEquals(null, localFunctions.single { it.name == "cyclic" }.resolvedReturnType)
        assertEquals("domain.User", importedFunction.resolvedReturnType)
    }

    @Test
    fun `resolves multiline generic and function type aliases`() {
        val file =
            File(tempDir, "MultilineAliases.kt").apply {
                writeText(
                    """
                    package app

                    class Request<T>
                    class Response

                    typealias ResponseMap<T> = Map<
                        String,
                        T,
                    >
                    typealias Sorted /* header docs */ <T : Comparable</* outer < /* nested > */ >*/T>> = List<T>
                    typealias Handler = (
                        Request<String>,
                    ) -> Response
                    typealias Callback = suspend
                        (Request<String>) -> Response
                    typealias ExtensionHandler = Request<String>.(
                        Response,
                    ) -> Response

                    class Consumer {
                        fun responses(): ResponseMap<Response> = TODO()
                        fun sorted(): Sorted<Response> = TODO()
                        fun handler(): Handler = TODO()
                        fun callback(): Callback = TODO()
                        fun extensionHandler(): ExtensionHandler = TODO()
                    }
                    """.trimIndent(),
                )
            }

        val aliases = PsiParser.getDeclaredTypeAliases(listOf(file))
        val functions = PsiParser.parseFile(file)!!.classes.single { it.name == "Consumer" }.functions

        assertTrue(aliases.getValue("app.ResponseMap").underlyingType.endsWith(">"))
        assertTrue(aliases.getValue("app.ResponseMap").underlyingType.contains("T"))
        assertEquals(listOf("T"), aliases.getValue("app.Sorted").typeParameters)
        assertTrue(aliases.getValue("app.Handler").underlyingType.endsWith("Response"))
        assertTrue(aliases.getValue("app.Handler").underlyingType.contains("Request<String>"))
        assertTrue(aliases.getValue("app.Callback").underlyingType.endsWith("Response"))
        assertTrue(aliases.getValue("app.Callback").underlyingType.startsWith("suspend"))
        assertTrue(aliases.getValue("app.ExtensionHandler").underlyingType.endsWith("Response"))
        assertEquals("kotlin.collections.Map", functions.single { it.name == "responses" }.resolvedReturnType)
        assertEquals("kotlin.collections.List", functions.single { it.name == "sorted" }.resolvedReturnType)
        assertEquals("kotlin.Function1", functions.single { it.name == "handler" }.resolvedReturnType)
        assertEquals("kotlin.Function2", functions.single { it.name == "callback" }.resolvedReturnType)
        assertEquals("kotlin.Function2", functions.single { it.name == "extensionHandler" }.resolvedReturnType)
    }

    @Test
    fun `resolves aliases with trailing and embedded comments`() {
        val file =
            File(tempDir, "CommentedAliases.kt").apply {
                writeText(
                    """
                    package app

                    class User
                    class Response
                    class Outer {
                        class Inner
                    }

                    typealias PublicUser = User // public API name
                    typealias PublicResponse = /* response docs < > */ Response
                    typealias PublicInner = Outer/* nested type docs */.Inner
                    typealias ResponseList = List<
                        /* outer docs < /* nested delimiter < > */ > */
                        Response,
                    >

                    class Consumer {
                        fun user(): PublicUser = TODO()
                        fun response(): PublicResponse = TODO()
                        fun inner(): PublicInner = TODO()
                        fun responses(): ResponseList = TODO()
                    }
                    """.trimIndent(),
                )
            }

        val aliases = PsiParser.getDeclaredTypeAliases(listOf(file))
        val functions = PsiParser.parseFile(file)!!.classes.single { it.name == "Consumer" }.functions

        assertEquals("User", aliases.getValue("app.PublicUser").underlyingType)
        assertEquals("Response", aliases.getValue("app.PublicResponse").underlyingType)
        assertEquals("Outer.Inner", aliases.getValue("app.PublicInner").underlyingType)
        assertTrue(aliases.getValue("app.ResponseList").underlyingType.none { it == '*' || it == '/' })
        assertEquals("app.User", functions.single { it.name == "user" }.resolvedReturnType)
        assertEquals("app.Response", functions.single { it.name == "response" }.resolvedReturnType)
        assertEquals("app.Outer.Inner", functions.single { it.name == "inner" }.resolvedReturnType)
        assertEquals("kotlin.collections.List", functions.single { it.name == "responses" }.resolvedReturnType)
    }

    @Test
    fun `resolves imported and default supertypes for typed assignability`() {
        val file =
            File(tempDir, "ExternalSupertypes.kt").apply {
                writeText(
                    """
                    package app
                    import java.io.Serializable

                    class ImportedSupertype : Serializable
                    class DefaultSupertype : CharSequence
                    """.trimIndent(),
                )
            }

        val classes = PsiParser.parseFile(file)!!.classes
        val imported = classes.single { it.name == "ImportedSupertype" }
        val default = classes.single { it.name == "DefaultSupertype" }

        assertEquals(listOf("java.io.Serializable"), imported.supertypes)
        assertEquals(listOf("kotlin.CharSequence"), default.supertypes)
        assertTrue(imported.isAssignableTo(java.io.Serializable::class.qualifiedName!!, classes))
        assertTrue(default.isAssignableTo(CharSequence::class.qualifiedName!!, classes))
        assertTrue(imported.isAssignableTo("Serializable", classes))
        assertTrue(default.isAssignableTo("CharSequence", classes))
    }

    @Test
    fun `resolves same file and imported nested type aliases`() {
        val sameFile =
            File(tempDir, "NestedAlias.kt").apply {
                writeText(
                    """
                    package app

                    class User
                    typealias PublicUser = User

                    class Api {
                        class User
                        typealias PublicUser = User

                        fun load(): PublicUser = TODO()
                        fun direct(): User = TODO()
                        fun update(value: PublicUser) = value
                        val current: PublicUser = TODO()
                    }
                    """.trimIndent(),
                )
            }
        val nestedAlias =
            File(tempDir, "ImportedNestedAlias.kt").apply {
                writeText(
                    """
                    package api

                    class Api {
                        class User
                        typealias PublicUser = User
                    }
                    """.trimIndent(),
                )
            }
        val importedConsumer =
            File(tempDir, "ImportedNestedAliasConsumer.kt").apply {
                writeText(
                    """
                    package app
                    import api.Api.PublicUser

                    class Consumer {
                        fun load(): PublicUser = TODO()
                    }
                    """.trimIndent(),
                )
            }

        assertEquals("app.Api.User", PsiParser.getDeclaredTypeAliases(listOf(sameFile))["app.Api.PublicUser"]?.underlyingType)
        val api = PsiParser.parseFile(sameFile)!!.classes.single { it.name == "Api" }
        val aliases = PsiParser.getDeclaredTypeAliases(listOf(nestedAlias))
        val imported =
            PsiParser.parseFile(importedConsumer, MapSymbolLookup(setOf("api.Api.User"), aliases))!!
                .classes
                .single()
                .functions
                .single()

        assertEquals("app.Api.User", api.functions.single { it.name == "load" }.resolvedReturnType)
        assertEquals("app.Api.User", api.functions.single { it.name == "direct" }.resolvedReturnType)
        assertEquals("app.Api.User", api.functions.single { it.name == "update" }.parameters.single().resolvedType)
        assertEquals("app.Api.User", api.properties.single().resolvedType)
        assertEquals("api.Api.User", imported.resolvedReturnType)
    }

    @Test
    fun `resolves top level and nested escaped type aliases`() {
        val file =
            File(tempDir, "EscapedAliases.kt").apply {
                writeText(
                    """
                    package app

                    class User
                    typealias `public user` = User
                    typealias Identity<`type parameter`> = `type parameter`

                    class Consumer {
                        fun load(): `public user` = TODO()
                        fun identity(): Identity<User> = TODO()
                    }

                    class Api {
                        class User
                        typealias `public user` = User

                        fun load(): `public user` = TODO()
                        val current: `public user` = TODO()
                    }
                    """.trimIndent(),
                )
            }

        val aliases = PsiParser.getDeclaredTypeAliases(listOf(file))
        val classes = PsiParser.parseFile(file)!!.classes
        val consumer = classes.single { it.name == "Consumer" }
        val api = classes.single { it.name == "Api" }

        assertEquals("User", aliases.getValue("app.public user").underlyingType)
        assertEquals(listOf("type parameter"), aliases.getValue("app.Identity").typeParameters)
        assertEquals("app.Api.User", aliases.getValue("app.Api.public user").underlyingType)
        assertEquals("app.User", consumer.functions.single { it.name == "load" }.resolvedReturnType)
        assertEquals("app.User", consumer.functions.single { it.name == "identity" }.resolvedReturnType)
        assertEquals("app.Api.User", api.functions.single().resolvedReturnType)
        assertEquals("app.Api.User", api.properties.single().resolvedType)
    }

    @Test
    fun `ignores typealias text in comments and string literals`() {
        val tripleQuote = "\"\"\""
        val file =
            File(tempDir, "AliasText.kt").apply {
                writeText(
                    """
                    package app

                    /*
                    typealias User = MissingCommentAlias
                    */
                    val source = $tripleQuote
                    typealias User = MissingStringAlias
                    $tripleQuote

                    class User
                    class Consumer {
                        fun load(): User = TODO()
                    }
                    """.trimIndent(),
                )
            }

        val aliases = PsiParser.getDeclaredTypeAliases(listOf(file))
        val consumer = PsiParser.parseFile(file)!!.classes.single { it.name == "Consumer" }

        assertTrue(aliases.isEmpty())
        assertEquals("app.User", consumer.functions.single().resolvedReturnType)
    }

    @Test
    fun `test dispose cleanup`() {
        PsiParser.dispose()
        PsiParser.dispose()

        val file =
            File(tempDir, "AfterDispose.kt").apply {
                writeText(
                    """
                    package app

                    class AfterDispose
                    """.trimIndent(),
                )
            }

        assertEquals("AfterDispose", PsiParser.parseFile(file)?.classes?.single()?.name)
    }
}
