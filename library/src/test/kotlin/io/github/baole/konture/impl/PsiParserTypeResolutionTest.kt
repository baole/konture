/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.KontureScopeTestFixture
import io.github.baole.konture.impl.psi.MapSymbolLookup
import io.github.baole.konture.isAssignableTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.Serializable

internal class PsiParserTypeResolutionTest : KontureScopeTestFixture() {
    @TempDir
    lateinit var tempDir: File

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
    fun `resolves implicit default java lang and kotlin types`() {
        val file =
            File(tempDir, "ImplicitDefaultImports.kt").apply {
                writeText(
                    """
                    package app

                    class ProcessRunner {
                        fun createProcess(): ProcessBuilder = ProcessBuilder(listOf("echo"))
                        fun runThread(): Thread = Thread()
                        fun formatText(): StringBuilder = StringBuilder()
                    }
                    """.trimIndent(),
                )
            }

        val runner = PsiParser.parseFile(file)!!.classes.single { it.name == "ProcessRunner" }
        val createProcess = runner.functions.single { it.name == "createProcess" }
        val runThread = runner.functions.single { it.name == "runThread" }
        val formatText = runner.functions.single { it.name == "formatText" }

        assertEquals("java.lang.ProcessBuilder", createProcess.resolvedReturnType)
        assertEquals("java.lang.Thread", runThread.resolvedReturnType)
        assertEquals("kotlin.text.StringBuilder", formatText.resolvedReturnType)
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
        val localFunctions =
            PsiParser.parseFile(
                localAliasFile,
            )!!.classes.single { it.name == "LocalConsumer" }.functions
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
        assertTrue(imported.isAssignableTo(Serializable::class.qualifiedName!!, classes))
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

        assertEquals(
            "app.Api.User",
            PsiParser.getDeclaredTypeAliases(listOf(sameFile))["app.Api.PublicUser"]?.underlyingType,
        )
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
}
