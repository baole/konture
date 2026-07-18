/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.i18n

import io.github.baole.konture.Konture
import java.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap

private const val BUNDLE_NAME = "io.github.baole.konture.i18n.messages"

private val supportedLocales =
    setOf(
        Locale.ENGLISH,
        Locale.FRENCH,
        Locale.forLanguageTag("es"),
        Locale.ITALIAN,
        Locale.forLanguageTag("vi"),
        Locale.forLanguageTag("zh"),
        Locale.SIMPLIFIED_CHINESE,
        Locale.TRADITIONAL_CHINESE,
    )

private val noFallbackControl = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT)
private val cachedPatterns = ConcurrentHashMap<Locale, ConcurrentHashMap<String, ResolvedPattern>>()
private val cachedZeroArgumentMessages = ConcurrentHashMap<Locale, ConcurrentHashMap<String, String>>()
private val cachedFormatters =
    ThreadLocal.withInitial {
        mutableMapOf<Locale, MutableMap<String, MessageFormat>>()
    }

private sealed interface ResolvedPattern {
    data class Found(
        val value: String,
    ) : ResolvedPattern

    data object Missing : ResolvedPattern
}

/**
 * Retrieves and formats a localized message string for the given key and arguments.
 * Falls back to English if the key is missing in the target locale, and returns
 * a fallback placeholder string if the resource bundle or keys are unavailable entirely.
 */
@Suppress("SwallowedException")
fun getMessage(
    key: String,
    vararg args: Any?,
): String {
    val locale = Konture.locale
    val resolvedPattern = resolvePattern(locale, key)
    if (resolvedPattern is ResolvedPattern.Missing) {
        return fallbackMessage(key, args)
    }

    val pattern = (resolvedPattern as ResolvedPattern.Found).value
    if (args.isEmpty()) {
        return if (locale in supportedLocales) {
            cachedZeroArgumentMessages
                .getOrPut(locale) { ConcurrentHashMap() }
                .computeIfAbsent(key) { formatWithoutArguments(pattern, locale) }
        } else {
            formatWithoutArguments(pattern, locale)
        }
    }

    val formatter =
        try {
            if (locale in supportedLocales) {
                cachedFormatters.get().getOrPut(locale) { mutableMapOf() }.getOrPut(key) {
                    MessageFormat(pattern, locale)
                }
            } else {
                MessageFormat(pattern, locale)
            }
        } catch (e: IllegalArgumentException) {
            return pattern
        }
    return formatter.format(args)
}

@Suppress("SwallowedException")
private fun formatWithoutArguments(
    pattern: String,
    locale: Locale,
): String =
    try {
        MessageFormat(pattern, locale).format(emptyArray<Any?>())
    } catch (e: IllegalArgumentException) {
        pattern
    }

private fun resolvePattern(
    locale: Locale,
    key: String,
): ResolvedPattern {
    if (locale !in supportedLocales) {
        return loadPattern(locale, key)
    }
    return cachedPatterns
        .getOrPut(locale) { ConcurrentHashMap() }
        .computeIfAbsent(key) { loadPattern(locale, it) }
}

@Suppress("SwallowedException")
private fun loadPattern(
    locale: Locale,
    key: String,
): ResolvedPattern {
    val bundle =
        try {
            ResourceBundle.getBundle(BUNDLE_NAME, locale, noFallbackControl)
        } catch (e: MissingResourceException) {
            return ResolvedPattern.Missing
        }
    return if (bundle.containsKey(key)) {
        ResolvedPattern.Found(bundle.getString(key))
    } else {
        ResolvedPattern.Missing
    }
}

private fun fallbackMessage(
    key: String,
    args: Array<out Any?>,
): String = "[$key: ${args.joinToString()}]"
