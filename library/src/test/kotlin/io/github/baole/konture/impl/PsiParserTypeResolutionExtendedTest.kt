/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.KontureScopeTestFixture
import io.github.baole.konture.UsageKind
import io.github.baole.konture.impl.psi.MapSymbolLookup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class PsiParserTypeResolutionExtendedTest : KontureScopeTestFixture() {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test qualified method call extraction for instance and static receivers`() {
        val file =
            File(tempDir, "QualifiedCalls.kt").apply {
                writeText(
                    """
                    package app
                    import com.example.analytics.Analytics
                    import com.example.logging.LogEvent

                    class UserViewModel(private val analytics: Analytics) {
                        fun onClick(logEvent: LogEvent) {
                            analytics.trackEvent("click")
                            Analytics.trackEvent("click")
                            logEvent.trackEvent("click")
                            LogEvent.trackEvent("click")
                        }
                    }
                    """.trimIndent(),
                )
            }

        val declaration = PsiParser.parseFile(file)!!
        val usages = declaration.usages.filter { it.kind == UsageKind.CALL }

        val analyticsMatches =
            usages.filter {
                PatternMatchers.isCallUsageMatch(it, "com.example.analytics.Analytics.trackEvent")
            }
        assertEquals(2, analyticsMatches.size)

        val classFqnMatches = usages.filter { PatternMatchers.isCallUsageMatch(it, "com.example.analytics.Analytics") }
        assertEquals(2, classFqnMatches.size)

        val logEventMatches =
            usages.filter {
                PatternMatchers.isCallUsageMatch(
                    it,
                    "com.example.logging.LogEvent.trackEvent",
                )
            }
        assertEquals(2, logEventMatches.size)
    }

    @Test
    fun `test viewModel calls context detection`() {
        val file =
            File(tempDir, "UserViewModel.kt").apply {
                writeText(
                    """
                    package com.example.ui

                    import android.content.Context
                    import androidx.lifecycle.ViewModel

                    class UserViewModel(private val context: Context) : ViewModel() {
                        fun loadData() {
                            val msg = context.getString(101)
                            val title = context.resources.getString(102)
                        }
                    }
                    """.trimIndent(),
                )
            }

        val declaration = PsiParser.parseFile(file)!!
        val usages = declaration.usages.filter { it.kind == UsageKind.CALL }

        val contextMatches = usages.filter { PatternMatchers.isCallUsageMatch(it, "android.content.Context") }
        assertEquals(2, contextMatches.size)
        assertTrue(contextMatches.all { it.enclosingClass == "com.example.ui.UserViewModel" })
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

    @Test
    fun `test unknown capitalized types do not fallback to java lang`() {
        val file =
            File(tempDir, "UnknownFallback.kt").apply {
                writeText(
                    """
                    package app

                    class Consumer {
                        fun load(user: UserRepository, payment: PaymentDTO, s: ThirdPartyService): String = TODO()
                    }
                    """.trimIndent(),
                )
            }
        val consumer = PsiParser.parseFile(file)!!.classes.single()
        val load = consumer.functions.single()

        assertNull(load.parameters[0].resolvedType)
        assertNull(load.parameters[1].resolvedType)
        assertNull(load.parameters[2].resolvedType)
    }

    @Test
    fun `test resolves explicitly imported type aliases from other packages`() {
        val aliasFile =
            File(tempDir, "ExplicitAliasDefinition.kt").apply {
                writeText(
                    """
                    package com.other
                    class RealTarget
                    typealias MyAlias = RealTarget
                    """.trimIndent(),
                )
            }
        val consumerFile =
            File(tempDir, "ExplicitAliasConsumer.kt").apply {
                writeText(
                    """
                    package app
                    import com.other.MyAlias

                    class Consumer {
                        fun load(): MyAlias = TODO()
                    }
                    """.trimIndent(),
                )
            }

        val aliases = PsiParser.getDeclaredTypeAliases(listOf(aliasFile))
        val lookup = MapSymbolLookup(setOf("com.other.RealTarget"), aliases)
        val consumer = PsiParser.parseFile(consumerFile, lookup)!!.classes.single()
        val load = consumer.functions.single()

        assertEquals("com.other.RealTarget", load.resolvedReturnType)
    }

    @Test
    fun `test resolves newly added primitive arrays and standard exceptions`() {
        val file =
            File(tempDir, "NewDefaults.kt").apply {
                writeText(
                    """
                    package app

                    class Consumer {
                        fun arrays(
                            i: IntArray,
                            l: LongArray,
                            s: ShortArray,
                            b: ByteArray,
                            d: DoubleArray,
                            f: FloatArray,
                            c: CharArray,
                            bool: BooleanArray,
                            ub: UByteArray,
                            us: UShortArray,
                            ui: UIntArray,
                            ul: ULongArray
                        ) {}

                        fun exceptions(
                            re: RuntimeException,
                            e: Error,
                            ae: AssertionError,
                            nse: NoSuchElementException,
                            cme: ConcurrentModificationException,
                            nfe: NumberFormatException,
                            ar: ArithmeticException,
                            cce: ClassCastException
                        ) {}
                    }
                    """.trimIndent(),
                )
            }
        val consumer = PsiParser.parseFile(file)!!.classes.single()
        val arrays = consumer.functions.first { it.name == "arrays" }
        val exceptions = consumer.functions.first { it.name == "exceptions" }

        assertEquals("kotlin.IntArray", arrays.parameters[0].resolvedType)
        assertEquals("kotlin.LongArray", arrays.parameters[1].resolvedType)
        assertEquals("kotlin.ShortArray", arrays.parameters[2].resolvedType)
        assertEquals("kotlin.ByteArray", arrays.parameters[3].resolvedType)
        assertEquals("kotlin.DoubleArray", arrays.parameters[4].resolvedType)
        assertEquals("kotlin.FloatArray", arrays.parameters[5].resolvedType)
        assertEquals("kotlin.CharArray", arrays.parameters[6].resolvedType)
        assertEquals("kotlin.BooleanArray", arrays.parameters[7].resolvedType)
        assertEquals("kotlin.UByteArray", arrays.parameters[8].resolvedType)
        assertEquals("kotlin.UShortArray", arrays.parameters[9].resolvedType)
        assertEquals("kotlin.UIntArray", arrays.parameters[10].resolvedType)
        assertEquals("kotlin.ULongArray", arrays.parameters[11].resolvedType)

        assertEquals("kotlin.RuntimeException", exceptions.parameters[0].resolvedType)
        assertEquals("kotlin.Error", exceptions.parameters[1].resolvedType)
        assertEquals("kotlin.AssertionError", exceptions.parameters[2].resolvedType)
        assertEquals("kotlin.NoSuchElementException", exceptions.parameters[3].resolvedType)
        assertEquals("kotlin.ConcurrentModificationException", exceptions.parameters[4].resolvedType)
        assertEquals("kotlin.NumberFormatException", exceptions.parameters[5].resolvedType)
        assertEquals("kotlin.ArithmeticException", exceptions.parameters[6].resolvedType)
        assertEquals("kotlin.ClassCastException", exceptions.parameters[7].resolvedType)
    }

    @Test
    fun `test precedence of explicit wildcard imports over default imports`() {
        val file =
            File(tempDir, "PrecedenceOverDefaults.kt").apply {
                writeText(
                    """
                    package app
                    import custom.*

                    class Consumer {
                        fun f(list: List, res: Result) {}
                    }
                    """.trimIndent(),
                )
            }
        val lookup = MapSymbolLookup(setOf("custom.List", "custom.Result"))
        val consumer = PsiParser.parseFile(file, lookup)!!.classes.single()
        val f = consumer.functions.single()

        assertEquals("custom.List", f.parameters[0].resolvedType)
        assertEquals("custom.Result", f.parameters[1].resolvedType)
    }
}
