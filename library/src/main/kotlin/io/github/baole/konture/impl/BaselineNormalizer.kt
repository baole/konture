/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import java.io.File
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

internal object BaselineNormalizer {
    private const val ROOT_PREFIX_LENGTH = 6
    private const val ROOT_PREFIX_WITH_SEPARATOR_LENGTH = 7

    private val prefixesSortedByLength: List<Pair<String, String>> by lazy {
        val categories =
            mapOf(
                "Module" to "module.should.resideInDirectory",
                "Class" to "class.should.resideInPackage",
                "File" to "file.should.resideInPackage",
                "Function" to "function.should.resideInPackage",
                "Property" to "property.should.resideInPackage",
            )
        val locales =
            listOf(
                Locale.ENGLISH,
                Locale.forLanguageTag("es"),
                Locale.FRENCH,
                Locale.ITALIAN,
                Locale.forLanguageTag("vi"),
                Locale.forLanguageTag("zh"),
                Locale.SIMPLIFIED_CHINESE,
                Locale.TRADITIONAL_CHINESE,
            )
        val tempPrefixes = mutableListOf<Pair<String, String>>()

        // Add hardcoded English fallbacks
        tempPrefixes.add("Module " to "Module")
        tempPrefixes.add("Class " to "Class")
        tempPrefixes.add("File " to "File")
        tempPrefixes.add("Function " to "Function")
        tempPrefixes.add("Property " to "Property")

        val bundleName = "io.github.baole.konture.i18n.messages"
        for (locale in locales) {
            val bundle = loadBundle(bundleName, locale) ?: continue

            for ((category, key) in categories) {
                val template = loadTemplate(bundle, key) ?: continue

                if (template.contains("{0}")) {
                    val prefix = template.substringBefore("{0}")
                    if (prefix.isNotEmpty()) {
                        tempPrefixes.add(prefix to category)
                    }
                }
            }
        }

        tempPrefixes.distinctBy { it.first }.sortedByDescending { it.first.length }
    }

    /**
     * Normalizes absolute file paths in violation strings to make them portable.
     */
    fun normalize(
        violation: String,
        buildRoot: File?,
    ): String {
        var normalized = violation
        if (buildRoot != null) {
            val rootPath = buildRoot.canonicalPath
            val normalizedRoot = rootPath.replace("\\", "/")
            normalized = normalized.replace("\\", "/").replace(rootPath, "<root>")
            if (normalizedRoot != rootPath) {
                normalized = normalized.replace(normalizedRoot, "<root>")
            }
        }
        return normalized.replace("//", "/")
    }

    fun parseLocationAndMessage(
        fullMessage: String,
        buildRoot: File?,
    ): Pair<String?, String> {
        parseFileLocation(fullMessage, buildRoot)?.let { return it }

        return parsePrefixedLocation(fullMessage, buildRoot) ?: Pair(null, fullMessage)
    }

    private fun parseFileLocation(
        fullMessage: String,
        buildRoot: File?,
    ): Pair<String?, String>? {
        val atIndex = fullMessage.lastIndexOf(" (at ")
        if (atIndex != -1 && fullMessage.endsWith(")")) {
            val rawPath = fullMessage.substring(atIndex + 5, fullMessage.length - 1)
            val cleanPath = normalizePath(rawPath, buildRoot)
            val messageWithoutAt = fullMessage.substring(0, atIndex)
            return Pair(cleanPath, messageWithoutAt)
        }
        return null
    }

    private fun parsePrefixedLocation(
        fullMessage: String,
        buildRoot: File?,
    ): Pair<String?, String>? {
        for ((prefix, category) in prefixesSortedByLength) {
            if (fullMessage.startsWith(prefix)) {
                val remaining = fullMessage.substring(prefix.length)
                val firstSpace = remaining.indexOf(' ')
                if (firstSpace != -1) {
                    val rawLocation = remaining.substring(0, firstSpace)
                    val msg = remaining.substring(firstSpace + 1)
                    val cleanLocation = normalizeLocation(category, rawLocation, buildRoot)
                    return Pair(cleanLocation, msg)
                }
            }
        }
        return null
    }

    private fun normalizeLocation(
        category: String,
        location: String,
        buildRoot: File?,
    ): String = if (category == "File") normalizePath(location, buildRoot) else location

    @Suppress("SwallowedException")
    private fun loadBundle(
        bundleName: String,
        locale: Locale,
    ): ResourceBundle? =
        try {
            ResourceBundle.getBundle(bundleName, locale)
        } catch (_: MissingResourceException) {
            null
        }

    private fun loadTemplate(
        bundle: ResourceBundle,
        key: String,
    ): String? =
        if (bundle.containsKey(key)) {
            bundle.getString(key)
        } else {
            null
        }

    private fun normalizePath(
        path: String,
        buildRoot: File?,
    ): String {
        var normalized = path.replace("\\", "/")
        if (normalized.startsWith("<root>/")) {
            normalized = normalized.substring(ROOT_PREFIX_WITH_SEPARATOR_LENGTH)
        } else if (normalized.startsWith("<root>")) {
            normalized = normalized.substring(ROOT_PREFIX_LENGTH)
        }
        if (buildRoot != null) {
            val rootPath = buildRoot.canonicalPath.replace("\\", "/")
            if (normalized.startsWith(rootPath)) {
                normalized = normalized.substring(rootPath.length)
            }
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1)
        }
        return normalized
    }

    @Suppress("NestedBlockDepth", "TooGenericExceptionCaught", "SwallowedException")
    fun findModuleForViolation(
        violation: FlatBaselineViolation,
        graph: ProjectGraph,
        buildRoot: File?,
    ): Module? {
        val rawLocation = violation.location ?: return null
        // Structured locations are formatted as ":module, <sourceSet> source set, <file>".
        // A bare module violation is just ":module"; a class/file violation is just the file path.
        val hasStructure = rawLocation.contains(", ")
        val modulePathToken = rawLocation.substringBefore(",").trim()
        if (modulePathToken.startsWith(":")) {
            graph.getAllModules().firstOrNull { it.path == modulePathToken }?.let { return it }
            if (!hasStructure) return null
        }
        val location = if (hasStructure) rawLocation.substringAfterLast(", ").trim() else rawLocation
        val root = buildRoot
        val resolvedLoc =
            if (root != null) {
                try {
                    val file = File(location)
                    if (file.isAbsolute) {
                        file.canonicalPath.replace("\\", "/")
                    } else {
                        File(root, location).canonicalPath.replace("\\", "/")
                    }
                } catch (e: Exception) {
                    location.replace("\\", "/")
                }
            } else {
                location.replace("\\", "/")
            }

        val sortedModules =
            graph
                .getAllModules()
                .filter { it.projectDir.isNotEmpty() }
                .sortedByDescending { it.projectDir.length }

        for (module in sortedModules) {
            val moduleDir =
                if (root != null) {
                    getModuleDir(root, module)
                } else {
                    File(module.projectDir)
                }
            val dirPrefix =
                try {
                    moduleDir.canonicalPath.replace("\\", "/")
                } catch (e: Exception) {
                    module.projectDir.replace("\\", "/")
                }
            if (resolvedLoc.startsWith(dirPrefix + "/") || resolvedLoc == dirPrefix) {
                return module
            }
        }

        // Fallback relative path matching
        for (module in sortedModules) {
            val dirPrefix = module.projectDir.replace("\\", "/")
            if (location.startsWith(dirPrefix + "/") || location == dirPrefix) {
                return module
            }
            if (root != null) {
                try {
                    val relPath = File(module.projectDir).relativeToOrNull(root)?.path?.replace("\\", "/")
                    if (relPath != null && (location.startsWith(relPath + "/") || location == relPath)) {
                        return module
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        return graph.getAllModules().firstOrNull { it.projectDir.isEmpty() }
    }

    fun getModuleDir(
        root: File,
        module: Module,
    ): File {
        val dirFile = File(module.projectDir)
        return if (dirFile.isAbsolute) {
            dirFile.canonicalFile
        } else {
            File(root, module.projectDir).canonicalFile
        }
    }
}
