/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import java.util.concurrent.ConcurrentHashMap

/**
 * Core utility class containing pattern matching algorithms for module glob patterns and package path patterns.
 */
internal object PatternMatchers {
    /**
     * Converts a module glob pattern (e.g. ":feature:*" or ":*-api") to a Regex.
     * '*' matches exactly one segment (characters except ':').
     * '**' matches zero or more segments (any characters).
     *
     * @param pattern The module glob pattern.
     * @return The compiled [Regex] matching the glob structure.
     */
    fun moduleGlobToRegex(pattern: String): Regex {
        val normalizedPattern =
            if (!pattern.startsWith(":") && !pattern.startsWith("**") && pattern.isNotEmpty()) {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "Module pattern '$pattern' lacks a leading colon (':'). Suggest matching with ':$pattern' instead.",
                )
                ":$pattern"
            } else {
                pattern
            }
        val builder = StringBuilder("^")
        var i = 0
        while (i < normalizedPattern.length) {
            if (normalizedPattern.startsWith("**", i)) {
                builder.append(".*")
                i += 2
            } else if (normalizedPattern.startsWith("*", i)) {
                builder.append("[^:]*")
                i += 1
            } else {
                val char = normalizedPattern[i]
                if (char in ":-_" || char.isLetterOrDigit()) {
                    builder.append(char)
                } else {
                    builder.append(Regex.escape(char.toString()))
                }
                i += 1
            }
        }
        builder.append("$")
        return Regex(builder.toString())
    }

    /**
     * Checks if a package name matches a package pattern.
     * Package patterns use '..' to mean zero-or-more package segments.
     * E.g., "..domain.." matches any package containing "domain" as a segment.
     * "com.acme.domain.." matches any package starting with "com.acme.domain".
     *
     * @param pattern The package pattern containing segments and '..' wildcards.
     * @return The compiled [Regex] representing the package match pattern.
     */
    fun packagePatternToRegex(pattern: String): Regex {
        if (pattern == "..") {
            return Regex("^.*$")
        }
        val parts = pattern.split("..")
        val escapedParts =
            parts.map { part ->
                if (part.isEmpty()) {
                    ""
                } else {
                    part.split(".").joinToString("\\.") { Regex.escape(it) }
                }
            }

        val builder = StringBuilder("^")
        if (pattern.startsWith("..")) {
            builder.append("(.*\\.)?")
        }

        for (i in escapedParts.indices) {
            val part = escapedParts[i]
            if (part.isNotEmpty()) {
                builder.append(part)
            }
            // Add wildcard between parts, except the last one unless pattern ends with ..
            if (i < escapedParts.size - 1) {
                if (escapedParts[i].isNotEmpty() && escapedParts[i + 1].isNotEmpty()) {
                    builder.append("\\.(.*\\.)?")
                } else if (escapedParts[i].isNotEmpty() && escapedParts[i + 1].isEmpty() &&
                    i + 1 < escapedParts.size - 1
                ) {
                    builder.append("\\.(.*\\.)?")
                }
            }
        }

        if (pattern.endsWith("..")) {
            builder.append("(\\..*)?")
        }
        builder.append("$")
        return Regex(builder.toString())
    }

    /**
     * Checks if a Gradle project path matches a module glob pattern.
     *
     * @param pattern The module glob pattern (e.g. `:feature:*`).
     * @param path The actual Gradle path to match.
     * @return True if matches, false otherwise.
     */
    fun matchesModuleGlob(
        pattern: String,
        path: String,
    ): Boolean = moduleGlobToRegex(pattern).matches(path)

    /**
     * Checks if a package name matches a package pattern.
     *
     * @param pattern The package pattern with '..' segment wildcards.
     * @param packageName The package name to verify.
     * @return True if matches, false otherwise.
     */
    fun matchesPackage(
        pattern: String,
        packageName: String,
    ): Boolean = packagePatternToRegex(pattern).matches(packageName)

    /**
     * Checks if a string matches a simple glob pattern where '*' matches zero or more characters.
     *
     * @param pattern The glob pattern (e.g., "*UseCase" or "com.example.*").
     * @param input The string to check.
     * @return True if matches, false otherwise.
     */
    fun matchesSimpleGlob(
        pattern: String,
        input: String,
    ): Boolean {
        val regexString = "^" + pattern.split("*").joinToString(".*") { Regex.escape(it) } + "$"
        return Regex(regexString).matches(input)
    }

    /**
     * Derives the slice key for a package from a slice pattern containing a single capture group.
     *
     * A slice pattern is a package pattern with exactly one capture token: `(*)` captures one package
     * segment, `(**)` captures one or more segments. The captured text is the slice key — packages
     * that produce the same key belong to the same slice. Surrounding `..` behaves as in
     * [packagePatternToRegex] (zero or more segments).
     *
     * For example `"com.acme.(*).."` yields `payment` for `com.acme.payment` and `com.acme.payment.api`,
     * and `null` for `com.other.thing`.
     *
     * @param pattern The slice pattern, which must contain exactly one `(*)` or `(**)` capture token.
     * @param packageName The package name to derive a slice key from.
     * @return The captured slice key, or null if the package does not match the pattern.
     * @throws IllegalArgumentException if the pattern does not contain exactly one capture token.
     */
    fun sliceKeyFor(
        pattern: String,
        packageName: String,
    ): String? = sliceRegexFor(pattern).matchEntire(packageName)?.groupValues?.get(1)

    private val sliceRegexCache = ConcurrentHashMap<String, Regex>()

    private fun sliceRegexFor(pattern: String): Regex =
        sliceRegexCache.getOrPut(pattern) {
            val doubleStar = pattern.indexOf("(**)")
            val singleStar = pattern.indexOf("(*)")
            val (token, captureRegex) =
                when {
                    doubleStar != -1 -> "(**)" to "(.+)"
                    singleStar != -1 -> "(*)" to "([^.]+)"
                    else -> throw IllegalArgumentException(
                        "Slice pattern '$pattern' must contain a capture group: '(*)' for one segment or '(**)' for one or more.",
                    )
                }
            val firstIndex = pattern.indexOf(token)
            require(pattern.indexOf(token, firstIndex + token.length) == -1) {
                "Slice pattern '$pattern' must contain exactly one capture group."
            }
            val left = sliceSideToRegex(pattern.substring(0, firstIndex))
            val right = sliceSideToRegex(pattern.substring(firstIndex + token.length))
            Regex("^$left$captureRegex$right$")
        }

    private fun sliceSideToRegex(side: String): String {
        val builder = StringBuilder()
        var i = 0
        while (i < side.length) {
            if (side.startsWith("..", i)) {
                // Trailing '..' matches any suffix; an interior '..' must land on a segment boundary.
                builder.append(if (i + 2 == side.length) ".*" else "(?:.*\\.)?")
                i += 2
            } else if (side[i] == '.') {
                builder.append("\\.")
                i += 1
            } else {
                builder.append(Regex.escape(side[i].toString()))
                i += 1
            }
        }
        return builder.toString()
    }

    /**
     * Checks if a [io.github.baole.konture.SourceUsage] matches a method call target or class call target pattern [fqName].
     */
    fun isCallUsageMatch(
        usage: io.github.baole.konture.SourceUsage,
        fqName: String,
    ): Boolean {
        if (usage.kind != io.github.baole.konture.UsageKind.CALL && usage.kind != io.github.baole.konture.UsageKind.CLASS_REFERENCE) return false

        val target = usage.targetFqName
        val raw = usage.rawExpression

        if (target == fqName || raw == fqName) return true
        if (target.endsWith(".$fqName") || raw.endsWith(".$fqName")) return true
        if (fqName.endsWith(".$target") || fqName.endsWith(".$raw")) return true

        if (target.startsWith("$fqName.") || raw.startsWith("$fqName.")) return true

        if (fqName in usage.possibleTargetFqNames) return true
        if (usage.possibleTargetFqNames.any { it == fqName || it.endsWith(".$fqName") || it.startsWith("$fqName.") }) return true

        return false
    }
}
