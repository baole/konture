package io.github.baole.konture.impl

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel

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
}
