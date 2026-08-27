/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import java.io.File

internal object BaselineStorage {
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun isCustomDirectory(
        baselinePath: String,
        customDirProp: String?,
        graph: ProjectGraph?,
        root: File?,
    ): Boolean {
        if (File(baselinePath).isAbsolute) return true
        if (customDirProp == null) return false
        if (graph == null || root == null) return true

        val customDir = File(customDirProp).canonicalFile
        val isProjectModuleDir =
            graph.getAllModules().any { module ->
                try {
                    val moduleDir = BaselineNormalizer.getModuleDir(root, module)
                    moduleDir == customDir
                } catch (e: Exception) {
                    false
                }
            }
        return !isProjectModuleDir
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun loadExistingViolations(
        baselinePath: String,
        currentDirProp: String?,
        currentGraph: ProjectGraph?,
        root: File?,
    ): Set<FlatBaselineViolation> {
        val violations = mutableSetOf<FlatBaselineViolation>()
        val isCustomDir = isCustomDirectory(baselinePath, currentDirProp, currentGraph, root)

        if (currentGraph != null && root != null && !isCustomDir) {
            KontureLogger.log(LogLevel.INFO, "Loading per-module architecture baselines from project graph...")
            for (module in currentGraph.getAllModules()) {
                val moduleDir = BaselineNormalizer.getModuleDir(root, module)
                val file = File(moduleDir, baselinePath)
                if (file.exists()) {
                    violations.addAll(BaselineSerializer.loadViolationsFromFile(file))
                }
            }
        } else {
            val file =
                if (File(baselinePath).isAbsolute) {
                    File(baselinePath).canonicalFile
                } else {
                    val dir = File(currentDirProp ?: System.getProperty("user.dir")).canonicalFile
                    File(dir, baselinePath).canonicalFile
                }
            if (file.exists()) {
                violations.addAll(BaselineSerializer.loadViolationsFromFile(file))
            }
        }
        return violations
    }

    @Suppress("NestedBlockDepth", "TooGenericExceptionCaught", "SwallowedException")
    fun writeBaseline(
        baselinePath: String,
        fallbackBaselineFile: File,
        graph: ProjectGraph?,
        root: File?,
        isCustomDir: Boolean,
        targetViolations: Collection<FlatBaselineViolation>,
    ) {
        if (graph != null && root != null && !isCustomDir) {
            KontureLogger.log(LogLevel.INFO, "Distributing recorded violations to per-module baselines...")

            val moduleViolationsMap = mutableMapOf<Module, MutableList<FlatBaselineViolation>>()
            val orphanedViolations = mutableListOf<FlatBaselineViolation>()

            for (v in targetViolations) {
                val module = BaselineNormalizer.findModuleForViolation(v, graph, root)
                if (module != null) {
                    moduleViolationsMap.getOrPut(module) { mutableListOf() }.add(v)
                } else {
                    orphanedViolations.add(v)
                }
            }

            for (module in graph.getAllModules()) {
                val moduleDir = BaselineNormalizer.getModuleDir(root, module)
                val file = File(moduleDir, baselinePath)
                val mViolations = moduleViolationsMap[module] ?: emptyList()

                if (mViolations.isNotEmpty()) {
                    BaselineSerializer.writeViolationsToFile(file, mViolations)
                } else {
                    if (file.exists()) {
                        try {
                            file.delete()
                            KontureLogger.log(LogLevel.INFO, "Deleted empty baseline file: ${file.absolutePath}")
                        } catch (e: Exception) {
                            KontureLogger.log(
                                LogLevel.WARNING,
                                "Failed to delete empty baseline file ${file.absolutePath}: ${e.message}",
                            )
                        }
                    }
                }
            }

            if (orphanedViolations.isNotEmpty()) {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "Found ${orphanedViolations.size} violations that could not be mapped to any module. Writing to fallback baseline file...",
                )
                BaselineSerializer.writeViolationsToFile(fallbackBaselineFile, orphanedViolations)
            }
        } else {
            // Fallback to single baseline file
            if (targetViolations.isNotEmpty()) {
                BaselineSerializer.writeViolationsToFile(fallbackBaselineFile, targetViolations.toList())
            } else if (fallbackBaselineFile.exists()) {
                try {
                    fallbackBaselineFile.delete()
                    KontureLogger.log(
                        LogLevel.INFO,
                        "Deleted empty baseline file: ${fallbackBaselineFile.absolutePath}",
                    )
                } catch (e: Exception) {
                    KontureLogger.log(
                        LogLevel.WARNING,
                        "Failed to delete empty baseline file ${fallbackBaselineFile.absolutePath}: ${e.message}",
                    )
                }
            }
        }
    }
}
