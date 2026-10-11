/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.i18n.SUPPORTED_LOCALES
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale
import java.util.ResourceBundle

class I18nBundleParityTest {
    private val bundleBaseName = "io.github.baole.konture.i18n.messages"
    private val placeholderRegex = Regex("""\{(\d+)\}""")

    @Test
    fun `test all supported locales have 100 percent key parity with base bundle`() {
        val baseBundle = ResourceBundle.getBundle(bundleBaseName, Locale.ENGLISH)
        val baseKeys = baseBundle.keySet()
        assertTrue(baseKeys.size >= 400, "Base bundle should contain at least 400 keys, found ${baseKeys.size}")

        for (locale in SUPPORTED_LOCALES) {
            val bundle = ResourceBundle.getBundle(bundleBaseName, locale)
            val localeKeys = bundle.keySet()
            val missingKeys = baseKeys - localeKeys

            assertEquals(
                emptySet<String>(),
                missingKeys,
                "Locale '$locale' is missing ${missingKeys.size} keys from base bundle: ${missingKeys.take(5)}",
            )
        }
    }

    @Test
    fun `test all parameterized messages have properly escaped single quotes for MessageFormat`() {
        for (locale in SUPPORTED_LOCALES) {
            val bundle = ResourceBundle.getBundle(bundleBaseName, locale)
            for (key in bundle.keySet()) {
                val value = bundle.getString(key)
                if (placeholderRegex.containsMatchIn(value)) {
                    val withoutDoubled = value.replace("''", "")
                    assertFalse(
                        withoutDoubled.contains("'"),
                        "Unescaped literal single quote (') in locale '$locale' for key '$key': '$value'. " +
                            "In Java MessageFormat, single quotes must be doubled ('') when placeholders are present.",
                    )
                }
            }
        }
    }
}
