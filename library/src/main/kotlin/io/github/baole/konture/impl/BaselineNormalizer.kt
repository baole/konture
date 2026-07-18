/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import java.io.File

internal object BaselineNormalizer {
    private const val ROOT_PREFIX_LENGTH = 6
    private const val ROOT_PREFIX_WITH_SEPARATOR_LENGTH = 7

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
        val atIndex = fullMessage.lastIndexOf(" (at ")
        if (atIndex != -1 && fullMessage.endsWith(")")) {
            val rawPath = fullMessage.substring(atIndex + 5, fullMessage.length - 1)
            val cleanPath = normalizePath(rawPath, buildRoot)
            val messageWithoutAt = fullMessage.substring(0, atIndex)
            return Pair(cleanPath, messageWithoutAt)
        }

        if (fullMessage.startsWith("Module ")) {
            val remaining = fullMessage.substring(7)
            val firstSpace = remaining.indexOf(' ')
            if (firstSpace != -1) {
                val modulePath = remaining.substring(0, firstSpace)
                val msg = remaining.substring(firstSpace + 1)
                return Pair(modulePath, msg)
            }
        }

        if (fullMessage.startsWith("Class ")) {
            val remaining = fullMessage.substring(6)
            val firstSpace = remaining.indexOf(' ')
            if (firstSpace != -1) {
                val fqName = remaining.substring(0, firstSpace)
                val msg = remaining.substring(firstSpace + 1)
                return Pair(fqName, msg)
            }
        }

        if (fullMessage.startsWith("File ")) {
            val remaining = fullMessage.substring(5)
            val firstSpace = remaining.indexOf(' ')
            if (firstSpace != -1) {
                val rawPath = remaining.substring(0, firstSpace)
                val msg = remaining.substring(firstSpace + 1)
                val cleanPath = normalizePath(rawPath, buildRoot)
                return Pair(cleanPath, msg)
            }
        }

        return Pair(null, fullMessage)
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
        val location = violation.location ?: return null
        if (location.startsWith(":")) {
            return graph.getAllModules().firstOrNull { it.path == location }
        }
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
            graph.getAllModules()
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
