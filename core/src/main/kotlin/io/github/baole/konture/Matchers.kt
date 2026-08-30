/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Unified pattern matchers for Gradle module paths.
 */
public object modules {
    /**
     * Converts a module base path into a recursive glob pattern matching all submodules under it.
     *
     * Example:
     * ```kotlin
     * modules.under(":feature") // produces ":feature:**" which matches ":feature:checkout", ":feature:profile"
     * ```
     *
     * @param path The base module path (e.g., `":feature"`, `"feature"`, or `":"`).
     * @return The normalized module glob pattern.
     */
    public fun under(path: String): String {
        val trimmed = path.trim()
        val normalized =
            when {
                trimmed.isEmpty() || trimmed == ":" -> ":**"
                trimmed.endsWith(":**") -> trimmed
                trimmed.endsWith(":") -> "$trimmed**"
                trimmed.startsWith(":") -> "$trimmed:**"
                else -> ":$trimmed:**"
            }
        return normalized
    }

    /**
     * Converts multiple module base paths into recursive glob patterns matching all submodules under them.
     */
    public fun under(vararg paths: String): List<String> = paths.map { under(it) }

    /**
     * Converts a list of module base paths into recursive glob patterns matching all submodules under them.
     */
    public fun under(paths: List<String>): List<String> = paths.map { under(it) }
}

/**
 * Unified pattern matchers for package names.
 */
public object packages {
    /**
     * Converts a base package name into a recursive package pattern matching the package and all its subpackages.
     *
     * Example:
     * ```kotlin
     * packages.under("com.acme.domain") // produces "com.acme.domain.." which matches "com.acme.domain.model", etc.
     * ```
     *
     * @param packageName The base package name (e.g., `"com.acme.domain"`).
     * @return The normalized package pattern.
     */
    public fun under(packageName: String): String {
        val trimmed = packageName.trim()
        val normalized =
            when {
                trimmed.isEmpty() || trimmed == ".." -> ".."
                trimmed.endsWith("..") -> trimmed
                trimmed.endsWith(".") -> "${trimmed}."
                else -> "$trimmed.."
            }
        return normalized
    }

    /**
     * Converts multiple base package names into recursive package patterns matching the packages and all their subpackages.
     */
    public fun under(vararg packageNames: String): List<String> = packageNames.map { under(it) }

    /**
     * Converts a list of base package names into recursive package patterns matching the packages and all their subpackages.
     */
    public fun under(packageNames: List<String>): List<String> = packageNames.map { under(it) }
}

/**
 * Typealias for [modules] matching PascalCase Kotlin naming conventions.
 */
public typealias Modules = modules

/**
 * Typealias for [packages] matching PascalCase Kotlin naming conventions.
 */
public typealias Packages = packages
