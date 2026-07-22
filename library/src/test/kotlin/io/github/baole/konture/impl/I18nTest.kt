/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Konture
import io.github.baole.konture.i18n.getMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class I18nTest {
    private lateinit var originalDefaultLocale: Locale
    private var originalLocaleProperty: String? = null

    @BeforeEach
    fun setUp() {
        originalDefaultLocale = Locale.getDefault()
        originalLocaleProperty = System.getProperty(Konture.PROPERTY_LOCALE)
        System.clearProperty(Konture.PROPERTY_LOCALE)
        KontureContextProvider.reset()
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(originalDefaultLocale)
        originalLocaleProperty?.let {
            System.setProperty(Konture.PROPERTY_LOCALE, it)
        } ?: System.clearProperty(Konture.PROPERTY_LOCALE)
        KontureContextProvider.reset()
    }

    @Test
    fun testDefaultLocaleIsEnglish() {
        Konture.locale = Locale.ENGLISH
        val message = getMessage("module.should.notBeDependedOnByModules", "A", "B")
        assertEquals("Module A is depended on by B, which is not allowed.", message)
    }

    @Test
    fun testClassShouldBeAssignableFromTranslations() {
        // English
        Konture.locale = Locale.ENGLISH
        assertEquals(
            "Class A should be assignable from B",
            getMessage("class.should.beAssignableFrom", "A", "B"),
        )

        // Spanish
        Konture.locale = Locale.forLanguageTag("es")
        assertEquals(
            "La clase A debería ser asignable desde B",
            getMessage("class.should.beAssignableFrom", "A", "B"),
        )

        // French
        Konture.locale = Locale.FRENCH
        assertEquals(
            "La classe A devrait être assignable depuis B",
            getMessage("class.should.beAssignableFrom", "A", "B"),
        )
    }

    @Test
    fun testLanguageTranslations() {
        // Spanish
        Konture.locale = Locale.forLanguageTag("es")
        assertEquals(
            "El módulo A depende de B, lo cual está prohibido por el patrón(es): pattern",
            getMessage("module.should.notDependOnModulesExplicit", "A", "B", "pattern"),
        )

        // French
        Konture.locale = Locale.FRENCH
        assertEquals(
            "Le module A dépend du module B, ce qui n'est pas autorisé par les motifs : pattern",
            getMessage("module.should.notDependOnModules", "A", "B", "pattern"),
        )

        // Italian
        Konture.locale = Locale.ITALIAN
        assertEquals(
            "Il modulo A ha dipendenze da B, il che non è consentito.",
            getMessage("module.should.notBeDependedOnByModules", "A", "B"),
        )

        // Vietnamese
        Konture.locale = Locale.forLanguageTag("vi")
        assertEquals(
            "Mô-đun A bị phụ thuộc bởi B, điều này không được phép bởi: desc",
            getMessage("module.should.notBeDependedOnByModulesPredicate", "A", "B", "desc"),
        )

        // Simplified Chinese
        Konture.locale = Locale.SIMPLIFIED_CHINESE
        assertEquals(
            "模块 A 被 B 依赖，这是不被 desc 允许的。",
            getMessage("module.should.notBeDependedOnByModulesPredicate", "A", "B", "desc"),
        )

        // Bare Chinese resolves to Simplified Chinese
        Konture.locale = Locale.forLanguageTag("zh")
        assertEquals(
            "模块 A 被 B 依赖，这是不被 desc 允许的。",
            getMessage("module.should.notBeDependedOnByModulesPredicate", "A", "B", "desc"),
        )

        // Traditional Chinese
        Konture.locale = Locale.TRADITIONAL_CHINESE
        assertEquals(
            "模組 A 被 B 依賴，這是不被 desc 允許的。",
            getMessage("module.should.notBeDependedOnByModulesPredicate", "A", "B", "desc"),
        )

        // Bare Chinese
        Konture.locale = Locale.forLanguageTag("zh")
        assertEquals(
            "模块 A 被 B 依赖，这是不被 desc 允许的。",
            getMessage("module.should.notBeDependedOnByModulesPredicate", "A", "B", "desc"),
        )
    }

    @Test
    fun testFallbackToEnglishForMissingKey() {
        Konture.locale = Locale.forLanguageTag("non_existent_lang")
        val message = getMessage("module.should.notBeDependedOnByModules", "A", "B")
        assertEquals("Module A is depended on by B, which is not allowed.", message)
    }

    @Test
    fun testUnsupportedLocaleFallsBackToEnglishInsteadOfJvmDefaultLocale() {
        Locale.setDefault(Locale.FRENCH)
        Konture.locale = Locale.GERMAN

        assertEquals(
            "Module A is depended on by B, which is not allowed.",
            getMessage("module.should.notBeDependedOnByModules", "A", "B"),
        )
    }

    @Test
    fun testLocaleParentFallback() {
        Konture.locale = Locale.CANADA_FRENCH

        assertEquals(
            "Le module A dépend du module B, ce qui n'est pas autorisé.",
            getMessage("module.should.notBeDependedOnByModules", "A", "B"),
        )
    }

    @Test
    fun testFallbackToPlaceholderWhenKeyNotFoundAnywhere() {
        Konture.locale = Locale.ENGLISH
        val message = getMessage("invalid.key.that.does.not.exist", "arg1", 123)
        assertEquals("[invalid.key.that.does.not.exist: arg1, 123]", message)
    }

    @Test
    fun testZeroArgumentMessagesPreserveMessageFormatEscaping() {
        Konture.locale = Locale.ENGLISH

        assertEquals(
            "Classes rule has no assertion ('should()'). You must specify at least one assertion condition.",
            getMessage("classes.rule.noAssertion"),
        )
        assertEquals("Class architecture violation(s) detected:", getMessage("classes.rule.violationHeader"))
    }

    @Test
    fun testThreadLocalLocaleIsolation() {
        Konture.locale = Locale.ENGLISH

        val latch = CountDownLatch(1)
        var threadLocale: Locale? = null
        var threadMessage: String? = null

        val thread =
            Thread {
                Konture.locale = Locale.FRENCH
                threadLocale = Konture.locale
                threadMessage = getMessage("module.should.notBeDependedOnByModules", "A", "B")
                latch.countDown()
            }

        thread.start()
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        // The thread should have its local locale (French)
        assertEquals(Locale.FRENCH, threadLocale)
        assertEquals("Le module A dépend du module B, ce qui n'est pas autorisé.", threadMessage)

        // The main thread should still have its local locale (English)
        assertEquals(Locale.ENGLISH, Konture.locale)
        assertEquals(
            "Module A is depended on by B, which is not allowed.",
            getMessage("module.should.notBeDependedOnByModules", "A", "B"),
        )
    }

    @Test
    fun testConcurrentMessageFormattingUsesThreadLocalFormatters() {
        Konture.locale = Locale.ENGLISH
        val startBarrier = CyclicBarrier(3)
        val completionLatch = CountDownLatch(2)
        val failure = AtomicReference<Throwable?>(null)
        val workers =
            listOf(
                Locale.ENGLISH to "Module A is depended on by B, which is not allowed.",
                Locale.FRENCH to "Le module A dépend du module B, ce qui n'est pas autorisé.",
            ).map { (locale, expectedMessage) ->
                Thread {
                    try {
                        Konture.locale = locale
                        startBarrier.await(5, TimeUnit.SECONDS)
                        repeat(100) {
                            assertEquals(
                                expectedMessage,
                                getMessage("module.should.notBeDependedOnByModules", "A", "B"),
                            )
                        }
                    } catch (error: Throwable) {
                        failure.compareAndSet(null, error)
                    } finally {
                        completionLatch.countDown()
                    }
                }
            }

        workers.forEach(Thread::start)
        startBarrier.await(5, TimeUnit.SECONDS)
        assertTrue(completionLatch.await(5, TimeUnit.SECONDS))
        failure.get()?.let { throw AssertionError("Concurrent message formatting failed", it) }
        assertEquals(Locale.ENGLISH, Konture.locale)
    }

    @Test
    fun testSystemPropertyLocaleOverride() {
        System.setProperty(Konture.PROPERTY_LOCALE, "fr")
        try {
            // By default, system property is used
            assertEquals(Locale.FRENCH, Konture.locale)
            assertEquals(
                "Le module A dépend du module B, ce qui n'est pas autorisé.",
                getMessage("module.should.notBeDependedOnByModules", "A", "B"),
            )

            // When set programmatically, programmatic setting takes precedence
            Konture.locale = Locale.ITALIAN
            assertEquals(Locale.ITALIAN, Konture.locale)
            assertEquals(
                "Il modulo A ha dipendenze da B, il che non è consentito.",
                getMessage("module.should.notBeDependedOnByModules", "A", "B"),
            )
        } finally {
            System.clearProperty(Konture.PROPERTY_LOCALE)
        }
    }

    @Test
    fun testBaselineNormalizerDynamicPrefixes() {
        // French prefix testing: "La classe {0}..." -> prefix: "La classe "
        val frMessage = "La classe com.foo.MyClass devrait résider dans le package 'com.foo.bar' mais réside dans 'com.foo'"
        val (frLoc, frMsg) = BaselineNormalizer.parseLocationAndMessage(frMessage, null)
        assertEquals("com.foo.MyClass", frLoc)
        assertEquals("devrait résider dans le package 'com.foo.bar' mais réside dans 'com.foo'", frMsg)

        // Vietnamese prefix testing: "Lớp {0}..." -> prefix: "Lớp "
        val viMessage = "Lớp com.foo.MyClass phải nằm trong gói 'com.foo.bar' nhưng lại nằm trong 'com.foo'"
        val (viLoc, viMsg) = BaselineNormalizer.parseLocationAndMessage(viMessage, null)
        assertEquals("com.foo.MyClass", viLoc)
        assertEquals("phải nằm trong gói 'com.foo.bar' nhưng lại nằm trong 'com.foo'", viMsg)

        // French file prefix testing: "Le fichier {0}..." -> prefix: "Le fichier "
        val frFileMessage = "Le fichier foo/bar/MyFile.kt devrait résider dans le package 'com.foo.bar' mais réside dans 'com.foo'"
        val (frFileLoc, frFileMsg) = BaselineNormalizer.parseLocationAndMessage(frFileMessage, null)
        assertEquals("foo/bar/MyFile.kt", frFileLoc)
        assertEquals("devrait résider dans le package 'com.foo.bar' mais réside dans 'com.foo'", frFileMsg)
    }
}
