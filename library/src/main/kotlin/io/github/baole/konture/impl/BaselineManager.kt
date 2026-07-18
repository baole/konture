/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Konture
import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal class BaselineManager() {
    private val context: KontureContext
        get() = KontureContextProvider.currentContext

    @Volatile
    private var capturedBaselinePath: String? = null

    @Volatile
    private var capturedGenerateBaseline: Boolean? = null

    @Volatile
    private var capturedProjectGraph: ProjectGraph? = null

    @Volatile
    private var isShutdownRunning = false

    private fun captureContextSnapshot() {
        if (isShutdownRunning) return
        try {
            val ctx = context
            capturedGenerateBaseline = ctx.generateBaseline
            capturedBaselinePath = ctx.baselinePath
            capturedProjectGraph = ctx.projectGraph
        } catch (e: Exception) {
            // Ignore
        }
    }

    val generateBaseline: Boolean
        get() {
            val sysProp = System.getProperty(Konture.PROPERTY_BASELINE_GENERATE)?.toBoolean()
            if (sysProp != null) {
                capturedGenerateBaseline = sysProp
                return sysProp
            }
            val ctxVal =
                try {
                    context.generateBaseline
                } catch (e: Exception) {
                    null
                }
            if (ctxVal == true) {
                capturedGenerateBaseline = true
                return true
            }
            return capturedGenerateBaseline ?: ctxVal ?: false
        }

    val baselinePath: String
        get() {
            val sysProp = System.getProperty(Konture.PROPERTY_BASELINE_PATH)
            if (sysProp != null) {
                capturedBaselinePath = sysProp
                return sysProp
            }
            val ctxVal =
                try {
                    context.baselinePath
                } catch (e: Exception) {
                    null
                }
            if (ctxVal != null && ctxVal != "konture-baseline.json") {
                capturedBaselinePath = ctxVal
                return ctxVal
            }
            return capturedBaselinePath ?: ctxVal ?: "konture-baseline.json"
        }

    val projectGraph: ProjectGraph?
        get() {
            val ctxVal =
                try {
                    context.projectGraph
                } catch (e: Exception) {
                    null
                }
            if (ctxVal != null) {
                capturedProjectGraph = ctxVal
                return ctxVal
            }
            return capturedProjectGraph
        }

    val baselineDir: File
        get() {
            val path = System.getProperty(Konture.PROPERTY_BASELINE_DIR) ?: System.getProperty("user.dir")
            return File(path).canonicalFile
        }

    val buildRoot: File? by lazy {
        try {
            context.projectGraphLoader.findBuildRoot()
        } catch (e: Exception) {
            null
        }
    }

    val baselineFile: File
        get() {
            val path = baselinePath
            val file = File(path)
            return if (file.isAbsolute) {
                file.canonicalFile
            } else {
                File(baselineDir, path).canonicalFile
            }
        }

    private var loadedBaselinePath: String? = null
    private var loadedBaselineDirProp: String? = null
    private var loadedProjectGraph: ProjectGraph? = null
    private var loadedViolations: Set<FlatBaselineViolation>? = null

    // Existing baseline violations loaded from files (per-module if project graph is available, else fallback)
    val existingViolations: Set<FlatBaselineViolation>
        get() {
            val currentPath = baselinePath
            val currentDirProp = System.getProperty(Konture.PROPERTY_BASELINE_DIR)
            val currentGraph = projectGraph

            val loaded = loadedViolations
            if (loaded != null &&
                currentPath == loadedBaselinePath &&
                currentDirProp == loadedBaselineDirProp &&
                currentGraph == loadedProjectGraph
            ) {
                return loaded
            }

            val violations = mutableSetOf<FlatBaselineViolation>()
            val root = buildRoot
            val isCustomDir =
                File(currentPath).isAbsolute ||
                    run {
                        if (currentDirProp == null) {
                            false
                        } else if (currentGraph == null || root == null) {
                            true
                        } else {
                            val customDir = File(currentDirProp).canonicalFile
                            val isProjectModuleDir =
                                currentGraph.getAllModules().any { module ->
                                    try {
                                        val moduleDir = BaselineNormalizer.getModuleDir(root, module)
                                        moduleDir == customDir
                                    } catch (e: Exception) {
                                        false
                                    }
                                }
                            !isProjectModuleDir
                        }
                    }

            if (currentGraph != null && root != null && !isCustomDir) {
                KontureLogger.log(LogLevel.INFO, "Loading per-module architecture baselines from project graph...")
                for (module in currentGraph.getAllModules()) {
                    val moduleDir = BaselineNormalizer.getModuleDir(root, module)
                    val file = File(moduleDir, currentPath)
                    if (file.exists()) {
                        violations.addAll(BaselineSerializer.loadViolationsFromFile(file))
                    }
                }
            } else {
                // Standalone fallback
                val file =
                    if (File(currentPath).isAbsolute) {
                        File(currentPath).canonicalFile
                    } else {
                        val dir = File(currentDirProp ?: System.getProperty("user.dir")).canonicalFile
                        File(dir, currentPath).canonicalFile
                    }
                if (file.exists()) {
                    violations.addAll(BaselineSerializer.loadViolationsFromFile(file))
                }
            }

            loadedBaselinePath = currentPath
            loadedBaselineDirProp = currentDirProp
            loadedProjectGraph = currentGraph
            loadedViolations = violations
            return violations
        }

    private var shutdownHook: Thread? = null

    fun resetForTest() {
        loadedViolations = null
        loadedBaselinePath = null
        loadedBaselineDirProp = null
        loadedProjectGraph = null
        capturedBaselinePath = null
        capturedGenerateBaseline = null
        capturedProjectGraph = null
        recordedViolations.clear()
        shutdownHook?.let {
            try {
                Runtime.getRuntime().removeShutdownHook(it)
            } catch (e: Exception) {
                // Ignore (e.g. if shutdown is already in progress)
            }
            shutdownHook = null
        }
    }

    // Thread-safe set of newly recorded violations
    val recordedViolations = ConcurrentHashMap.newKeySet<FlatBaselineViolation>()

    init {
        // Register shutdown hook to write baseline when JVM exits (if in recording mode)
        try {
            val hook =
                Thread {
                    isShutdownRunning = true
                    if (generateBaseline && recordedViolations.isNotEmpty()) {
                        writeBaseline()
                    }
                }
            Runtime.getRuntime().addShutdownHook(hook)
            shutdownHook = hook
        } catch (e: Exception) {
            KontureLogger.log(LogLevel.WARNING, "Failed to register baseline shutdown hook: ${e.message}")
        }
    }

    /**
     * Delegating to BaselineNormalizer for backward compatibility or direct calls.
     */
    fun normalize(
        violation: String,
        buildRoot: File?,
    ): String = BaselineNormalizer.normalize(violation, buildRoot)

    internal fun findModuleForViolation(
        violation: FlatBaselineViolation,
        graph: ProjectGraph,
    ): Module? = BaselineNormalizer.findModuleForViolation(violation, graph, buildRoot)

    /**
     * Handles a list of rule violations. If [generateBaseline] is active, the violations
     * are recorded. Otherwise, they are filtered against the existing baseline, and any
     * new violations will throw an [AssertionError].
     */
    fun handleViolations(
        violations: List<String>,
        header: String,
    ) {
        captureContextSnapshot()
        if (violations.isEmpty()) return

        val testLoc = TestLocationFinder.findTestLocation()
        val testClass = testLoc?.className ?: "UnknownTest"
        val testMethod = testLoc?.methodName ?: "unknownMethod"

        val normalizedViolations =
            violations.map {
                val normMsg = BaselineNormalizer.normalize(it, buildRoot)
                val (location, cleanMsg) = BaselineNormalizer.parseLocationAndMessage(normMsg, buildRoot)
                Pair(
                    FlatBaselineViolation(
                        testClass = testClass,
                        testMethod = testMethod,
                        location = location,
                        message = cleanMsg,
                    ),
                    normMsg,
                )
            }

        if (generateBaseline) {
            recordedViolations.addAll(normalizedViolations.map { it.first })
            KontureLogger.log(
                LogLevel.INFO,
                "Recorded ${violations.size} violations to baseline (current total recorded in JVM: ${recordedViolations.size})",
            )
            return
        }

        // Filter out violations that are already baselined by matching testClass, testMethod, location, and message
        val newViolations =
            normalizedViolations.filter { (norm, _) ->
                !existingViolations.contains(norm)
            }.map { it.first }

        if (newViolations.isNotEmpty()) {
            val message =
                buildString {
                    appendLine(header)
                    newViolations.forEach {
                        if (it.location != null) {
                            appendLine("  - ${it.message} (at ${it.location})")
                        } else {
                            appendLine("  - ${it.message}")
                        }
                    }
                }
            throw AssertionError(message)
        }
    }

    /**
     * Writes the combined baseline (existing + newly recorded) to file.
     */
    internal fun writeBaseline() {
        val graph = projectGraph
        val root = buildRoot
        val isCustomDir =
            File(baselinePath).isAbsolute ||
                run {
                    val customDirProp = System.getProperty(Konture.PROPERTY_BASELINE_DIR)
                    if (customDirProp == null) {
                        false
                    } else if (graph == null || root == null) {
                        true
                    } else {
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
                        !isProjectModuleDir
                    }
                }

        if (graph != null && root != null && !isCustomDir) {
            KontureLogger.log(LogLevel.INFO, "Distributing recorded violations to per-module baselines...")
            val combined = (existingViolations + recordedViolations)

            val moduleViolationsMap = mutableMapOf<Module, MutableList<FlatBaselineViolation>>()
            val orphanedViolations = mutableListOf<FlatBaselineViolation>()

            for (v in combined) {
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
                            KontureLogger.log(LogLevel.WARNING, "Failed to delete empty baseline file ${file.absolutePath}: ${e.message}")
                        }
                    }
                }
            }

            if (orphanedViolations.isNotEmpty()) {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "Found ${orphanedViolations.size} violations that could not be mapped to any module. Writing to fallback baseline file...",
                )
                BaselineSerializer.writeViolationsToFile(baselineFile, orphanedViolations)
            }
        } else {
            // Fallback to single baseline file
            val combined = (existingViolations + recordedViolations)
            BaselineSerializer.writeViolationsToFile(baselineFile, combined.toList())
        }
    }

    companion object {
        fun resetForTest() {
            KontureContextProvider.currentContext.baselineManager.resetForTest()
        }

        fun normalize(
            violation: String,
            buildRoot: File?,
        ): String {
            return KontureContextProvider.currentContext.baselineManager.normalize(violation, buildRoot)
        }

        fun findModuleForViolation(
            violation: FlatBaselineViolation,
            graph: ProjectGraph,
        ): Module? {
            return KontureContextProvider.currentContext.baselineManager.findModuleForViolation(violation, graph)
        }

        fun handleViolations(
            violations: List<String>,
            header: String,
        ) {
            KontureContextProvider.currentContext.baselineManager.handleViolations(violations, header)
        }

        fun writeBaseline() {
            KontureContextProvider.currentContext.baselineManager.writeBaseline()
        }
    }
}
