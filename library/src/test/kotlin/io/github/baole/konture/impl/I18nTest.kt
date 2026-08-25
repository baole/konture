/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Konture
import io.github.baole.konture.i18n.SUPPORTED_LOCALES
import io.github.baole.konture.i18n.getMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.Properties
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
        KontureRuntimeStateProvider.reset()
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(originalDefaultLocale)
        originalLocaleProperty?.let {
            System.setProperty(Konture.PROPERTY_LOCALE, it)
        } ?: System.clearProperty(Konture.PROPERTY_LOCALE)
        KontureRuntimeStateProvider.reset()
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
    fun testAllKeysExistInAllSupportedLocales() {
        val resourcePathPrefix = "io/github/baole/konture/i18n"
        val englishResourceName = "$resourcePathPrefix/messages.properties"
        val englishStream =
            javaClass.classLoader.getResourceAsStream(englishResourceName)
                ?: fail("Could not find default English properties file: $englishResourceName")

        val englishProps = Properties().apply { englishStream.use { load(it) } }
        val englishKeys = englishProps.stringPropertyNames()
        assertTrue(englishKeys.isNotEmpty(), "English properties file should contain string keys")

        // Discover all supported language resource files on the classpath
        val languageFiles = mutableSetOf<String>()
        val englishUrl = javaClass.classLoader.getResource(englishResourceName)
        if (englishUrl != null && englishUrl.protocol == "file") {
            val parentDir = java.io.File(englishUrl.toURI()).parentFile
            parentDir.listFiles()
                ?.filter { it.isFile && it.name.startsWith("messages_") && it.name.endsWith(".properties") }
                ?.mapTo(languageFiles) { it.name }
        }

        // Also ensure all non-English locales in SUPPORTED_LOCALES have their corresponding files checked
        for (locale in SUPPORTED_LOCALES) {
            if (locale == Locale.ENGLISH) continue
            val localeFileName = "messages_$locale.properties"
            languageFiles.add(localeFileName)
        }

        assertTrue(languageFiles.isNotEmpty(), "Should discover at least one supported language properties file")

        val missingByFile = mutableMapOf<String, List<String>>()
        val emptyByFile = mutableMapOf<String, List<String>>()

        for (fileName in languageFiles.sorted()) {
            val resourceName = "$resourcePathPrefix/$fileName"
            val stream = javaClass.classLoader.getResourceAsStream(resourceName)
            if (stream == null) {
                missingByFile[fileName] = englishKeys.toList()
                continue
            }

            val langProps = Properties().apply { stream.use { load(it) } }

            val missing = englishKeys.filterNot { langProps.containsKey(it) }
            if (missing.isNotEmpty()) {
                missingByFile[fileName] = missing
            }

            val empty =
                englishKeys.filter { key ->
                    langProps.containsKey(key) && langProps.getProperty(key).isNullOrBlank()
                }
            if (empty.isNotEmpty()) {
                emptyByFile[fileName] = empty
            }
        }

        assertTrue(
            missingByFile.isEmpty(),
            "Missing English translation keys in supported language files:\n" +
                missingByFile.entries.joinToString("\n") { (file, keys) ->
                    "  $file (${keys.size} missing): ${keys.take(10)}${if (keys.size > 10) "..." else ""}"
                },
        )

        assertTrue(
            emptyByFile.isEmpty(),
            "Empty translation values in supported language files:\n" +
                emptyByFile.entries.joinToString("\n") { (file, keys) ->
                    "  $file (${keys.size} empty): ${keys.take(10)}${if (keys.size > 10) "..." else ""}"
                },
        )
    }

    @Test
    fun testArchitectureLayerPolicyTranslationsInAllLocales() {
        val spyLayer = "feature"
        val allowed = "core, domain"
        val sourceClass = "com.example.Presenter"
        val targetClass = "com.domain.Widget"
        val targetLayer = "domain"
        val at = "Presenter.kt:10"

        // Arg lists per key must match getMessage(...) call signatures in ArchitectureLayerPolicy.
        val argsByKey: Map<String, Array<Any?>> =
            mapOf(
                "architecture.policy.violationHeader" to arrayOf(),
                "architecture.policy.mayDependOn" to arrayOf(spyLayer, allowed, sourceClass, targetClass, targetLayer, at),
                "architecture.policy.mustNotDependOn" to arrayOf(spyLayer, allowed, sourceClass, targetClass, targetLayer, at),
                "architecture.policy.mayBeAccessedBy" to arrayOf(spyLayer, allowed, sourceClass, targetLayer, targetClass, at),
                "architecture.policy.mustNotBeAccessedBy" to arrayOf(spyLayer, allowed, sourceClass, targetLayer, targetClass, at),
                "architecture.policy.noSelector" to arrayOf(spyLayer),
                "architecture.policy.undefinedLayer" to arrayOf("typoLayer"),
            )

        for (locale in SUPPORTED_LOCALES) {
            Konture.locale = locale
            for ((key, args) in argsByKey) {
                val rendered = getMessage(key, *args)
                assertFalse(
                    rendered.startsWith("[$key"),
                    "$key resolved to fallback placeholder in ${locale.language}: '$rendered'",
                )
                if (args.isNotEmpty()) {
                    assertTrue(
                        rendered.contains(args.first().toString()),
                        "$key lost its arguments in ${locale.language}: '$rendered'",
                    )
                }
            }
        }
    }

    @Test
    fun testArchitectureLayerPolicyTranslationsWording() {
        val args =
            arrayOf(
                "feature",
                "core, domain",
                "com.example.Presenter",
                "com.domain.Widget",
                "domain",
                "Presenter.kt:10",
            )

        // Spanish
        Konture.locale = Locale.forLanguageTag("es")
        assertEquals(
            "La capa feature solo puede depender de las capas [core, domain], pero la clase " +
                "com.example.Presenter depende de com.domain.Widget en la(s) capa(s) [domain] (en Presenter.kt:10)",
            getMessage("architecture.policy.mayDependOn", *args),
        )

        // French
        Konture.locale = Locale.FRENCH
        assertEquals(
            "La couche feature ne peut dépendre que des couches [core, domain], mais la classe " +
                "com.example.Presenter dépend de com.domain.Widget dans la/les couche(s) [domain] (à Presenter.kt:10)",
            getMessage("architecture.policy.mayDependOn", *args),
        )

        // Italian
        Konture.locale = Locale.ITALIAN
        assertEquals(
            "Lo strato feature può dipendere solo dagli strati [core, domain], ma la classe " +
                "com.example.Presenter dipende da com.domain.Widget nello/gli strato/i [domain] (a Presenter.kt:10)",
            getMessage("architecture.policy.mayDependOn", *args),
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
