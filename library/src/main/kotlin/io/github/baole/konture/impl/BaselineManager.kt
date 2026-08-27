/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Konture
import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.model.Violation
import io.github.baole.konture.core.model.ViolationReport
import io.github.baole.konture.i18n.getMessage
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

internal class BaselineManager {
    private val state: KontureRuntimeState
        get() = KontureRuntimeStateProvider.currentState

    private val evaluator = BaselineRuleEvaluator(this)

    @Volatile
    private var capturedBaselinePath: String? = null

    @Volatile
    private var capturedGenerateBaseline: Boolean? = null

    @Volatile
    private var capturedProjectGraph: ProjectGraph? = null

    @Volatile
    private var capturedBuildRoot: File? = null

    @Volatile
    private var hasCapturedBuildRoot = false

    @Volatile
    internal var isShutdownRunning = false

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    internal fun captureContextSnapshot() {
        if (isShutdownRunning) return
        try {
            val ctx = state
            val gen = ctx.generateBaseline
            if (gen) {
                capturedGenerateBaseline = true
                globalGenerateBaseline = true
            }
            val path = ctx.baselinePath
            if (path != "konture-baseline.json") {
                capturedBaselinePath = path
                globalBaselinePath = path
            }
            val graph = ctx.projectGraph
            if (graph != null) {
                capturedProjectGraph = graph
                globalProjectGraph = graph
            }
            val root =
                try {
                    ctx.projectGraphLoader.findBuildRoot()
                } catch (e: Exception) {
                    null
                }
            if (root != null) {
                capturedBuildRoot = root
                hasCapturedBuildRoot = true
                globalBuildRoot = root
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    @get:Suppress("TooGenericExceptionCaught", "SwallowedException")
    val generateBaseline: Boolean
        get() {
            val sysProp = System.getProperty(Konture.PROPERTY_BASELINE_GENERATE)?.toBoolean()
            if (sysProp != null) return sysProp
            if (isShutdownRunning) return globalGenerateBaseline ?: capturedGenerateBaseline ?: false
            val ctxVal =
                try {
                    state.generateBaseline
                } catch (e: Exception) {
                    null
                }
            if (ctxVal == true) {
                capturedGenerateBaseline = true
                globalGenerateBaseline = true
                return true
            }
            return globalGenerateBaseline ?: capturedGenerateBaseline ?: ctxVal ?: false
        }

    @get:Suppress("TooGenericExceptionCaught", "SwallowedException")
    val baselinePath: String
        get() {
            val sysProp = System.getProperty(Konture.PROPERTY_BASELINE_PATH)
            if (sysProp != null) return sysProp
            if (isShutdownRunning) return globalBaselinePath ?: capturedBaselinePath ?: "konture-baseline.json"
            val ctxVal =
                try {
                    state.baselinePath
                } catch (e: Exception) {
                    null
                }
            if (ctxVal != null && ctxVal != "konture-baseline.json") {
                capturedBaselinePath = ctxVal
                globalBaselinePath = ctxVal
                return ctxVal
            }
            return globalBaselinePath ?: capturedBaselinePath ?: ctxVal ?: "konture-baseline.json"
        }

    @get:Suppress("TooGenericExceptionCaught", "SwallowedException")
    val projectGraph: ProjectGraph?
        get() {
            if (isShutdownRunning) return globalProjectGraph ?: capturedProjectGraph
            val ctxVal =
                try {
                    state.projectGraph
                } catch (e: Exception) {
                    null
                }
            if (ctxVal != null) {
                capturedProjectGraph = ctxVal
                globalProjectGraph = ctxVal
                return ctxVal
            }
            return globalProjectGraph ?: capturedProjectGraph
        }

    val baselineDir: File
        get() {
            val path = System.getProperty(Konture.PROPERTY_BASELINE_DIR) ?: System.getProperty("user.dir")
            return File(path).canonicalFile
        }

    @get:Suppress("TooGenericExceptionCaught", "SwallowedException")
    val buildRoot: File?
        get() {
            if (isShutdownRunning) return globalBuildRoot ?: capturedBuildRoot
            if (hasCapturedBuildRoot) return capturedBuildRoot
            return try {
                state.projectGraphLoader.findBuildRoot().also {
                    capturedBuildRoot = it
                    globalBuildRoot = it
                    hasCapturedBuildRoot = true
                }
            } catch (e: Exception) {
                hasCapturedBuildRoot = true
                globalBuildRoot
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

    private data class BaselineCacheKey(
        val path: String,
        val directoryProperty: String?,
        val projectGraph: ProjectGraph?,
    )

    private var loadedCacheKey: BaselineCacheKey? = null
    private var loadedViolations: Set<FlatBaselineViolation>? = null

    // Existing baseline violations loaded from files (per-module if project graph is available, else fallback)
    @get:Suppress("TooGenericExceptionCaught", "SwallowedException")
    val existingViolations: Set<FlatBaselineViolation>
        get() {
            val currentPath = baselinePath
            val currentDirProp = System.getProperty(Konture.PROPERTY_BASELINE_DIR)
            val currentGraph = projectGraph
            val cacheKey = BaselineCacheKey(currentPath, currentDirProp, currentGraph)

            val loaded = loadedViolations
            if (loaded != null && cacheKey == loadedCacheKey) {
                return loaded
            }

            val violations =
                BaselineStorage.loadExistingViolations(
                    currentPath,
                    currentDirProp,
                    currentGraph,
                    buildRoot,
                )
            loadedCacheKey = cacheKey
            loadedViolations = violations
            return violations
        }

    // Thread-safe set of newly recorded violations (shared across parallel test threads)
    val recordedViolations: MutableSet<FlatBaselineViolation>
        get() = globalRecordedViolations

    // Thread-safe set of all evaluated violations in the current JVM session (shared across parallel test threads)
    val evaluatedViolations: MutableSet<FlatBaselineViolation>
        get() = globalEvaluatedViolations

    fun resetForTest() {
        loadedViolations = null
        loadedCacheKey = null
        capturedBaselinePath = null
        capturedGenerateBaseline = null
        capturedProjectGraph = null
        capturedBuildRoot = null
        hasCapturedBuildRoot = false
        globalRecordedViolations.clear()
        globalEvaluatedViolations.clear()
        globalGenerateBaseline = null
        globalBaselinePath = null
        globalProjectGraph = null
        globalBuildRoot = null
    }

    init {
        ensureShutdownHookRegistered()
    }

    /**
     * Returns the set of violations present in the baseline that were not observed during execution.
     */
    fun getResolvedViolations(): Set<FlatBaselineViolation> {
        val existing = existingViolations
        val evaluated = evaluatedViolations
        return existing.filter { !evaluated.contains(it) }.toSet()
    }

    /**
     * Returns the set of violations present in both the baseline and evaluated rules (active suppressed debt).
     */
    fun getActiveBaselineViolations(): Set<FlatBaselineViolation> {
        val existing = existingViolations
        val evaluated = evaluatedViolations
        return existing.filter { evaluated.contains(it) }.toSet()
    }

    /**
     * Returns the set of violations observed during execution that are not in the baseline.
     */
    fun getNewViolations(): Set<FlatBaselineViolation> {
        val existing = existingViolations
        val evaluated = evaluatedViolations
        return evaluated.filter { !existing.contains(it) }.toSet()
    }

    /**
     * Checks if any baseline violations have been resolved and throws an [AssertionError] if
     * [Konture.failOnResolvedViolations] is enabled.
     */
    fun checkRatchet() {
        val resolved = getResolvedViolations()
        if (resolved.isNotEmpty()) {
            if (Konture.reportResolvedViolations) {
                KontureLogger.log(LogLevel.INFO, getMessage("baseline.resolved.info", resolved.size))
            }
            if (Konture.failOnResolvedViolations) {
                throw AssertionError(getMessage("baseline.resolved.ratchetError", resolved.size))
            }
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
        evaluator.handleViolations(violations, header)
    }

    fun checkRule(
        violationHeader: String,
        runCheck: (MutableList<String>) -> Unit,
    ) {
        evaluator.checkRule(violationHeader, runCheck)
    }

    /**
     * Executes a rule check function producing structured [Violation] instances, performs baseline
     * suppression filtering, handles localized [AssertionError] throwing if un-baselined violations exist,
     * and returns the aggregated [ViolationReport].
     */
    fun checkRuleReport(
        ruleId: String,
        violationHeader: String,
        runCheckReport: (MutableList<Violation>) -> Unit,
    ): ViolationReport = evaluator.checkRuleReport(ruleId, violationHeader, runCheckReport)

    /**
     * Writes the baseline to file. When [generateBaseline] is active, only the live recorded
     * violations are written (pruning resolved technical debt).
     */
    internal fun writeBaseline() {
        val graph = projectGraph
        val root = buildRoot
        val isCustomDir =
            BaselineStorage.isCustomDirectory(
                baselinePath = baselinePath,
                customDirProp = System.getProperty(Konture.PROPERTY_BASELINE_DIR),
                graph = graph,
                root = root,
            )
        val targetViolations = if (generateBaseline) recordedViolations else (existingViolations + recordedViolations)

        BaselineStorage.writeBaseline(
            baselinePath = baselinePath,
            fallbackBaselineFile = baselineFile,
            graph = graph,
            root = root,
            isCustomDir = isCustomDir,
            targetViolations = targetViolations,
        )
    }

    companion object {
        private val globalRecordedViolations = ConcurrentHashMap.newKeySet<FlatBaselineViolation>()
        private val globalEvaluatedViolations = ConcurrentHashMap.newKeySet<FlatBaselineViolation>()
        private val shutdownHookRegistered = AtomicBoolean(false)
        private var globalShutdownHook: Thread? = null

        @Volatile
        private var globalGenerateBaseline: Boolean? = null

        @Volatile
        private var globalBaselinePath: String? = null

        @Volatile
        private var globalProjectGraph: ProjectGraph? = null

        @Volatile
        private var globalBuildRoot: File? = null

        @Suppress("TooGenericExceptionCaught")
        private fun ensureShutdownHookRegistered() {
            if (shutdownHookRegistered.compareAndSet(false, true)) {
                try {
                    val hook =
                        Thread {
                            val activeManager = KontureRuntimeStateProvider.currentState.baselineManager
                            activeManager.isShutdownRunning = true
                            if (activeManager.generateBaseline && globalRecordedViolations.isNotEmpty()) {
                                activeManager.writeBaseline()
                            } else if (!activeManager.generateBaseline) {
                                val resolved = activeManager.getResolvedViolations()
                                if (resolved.isNotEmpty()) {
                                    if (Konture.reportResolvedViolations) {
                                        KontureLogger.log(
                                            LogLevel.INFO,
                                            getMessage("baseline.resolved.info", resolved.size),
                                        )
                                    }
                                    if (Konture.failOnResolvedViolations) {
                                        KontureLogger.log(
                                            LogLevel.ERROR,
                                            getMessage("baseline.resolved.ratchetError", resolved.size),
                                        )
                                    }
                                }
                            }
                        }
                    Runtime.getRuntime().addShutdownHook(hook)
                    globalShutdownHook = hook
                } catch (e: Exception) {
                    KontureLogger.log(LogLevel.WARNING, "Failed to register baseline shutdown hook: ${e.message}")
                }
            }
        }

        fun resetForTest() {
            KontureRuntimeStateProvider.currentState.baselineManager.resetForTest()
        }

        fun normalize(
            violation: String,
            buildRoot: File?,
        ): String {
            return KontureRuntimeStateProvider.currentState.baselineManager.normalize(violation, buildRoot)
        }

        fun findModuleForViolation(
            violation: FlatBaselineViolation,
            graph: ProjectGraph,
        ): Module? {
            return KontureRuntimeStateProvider.currentState.baselineManager.findModuleForViolation(violation, graph)
        }

        fun handleViolations(
            violations: List<String>,
            header: String,
        ) {
            KontureRuntimeStateProvider.currentState.baselineManager.handleViolations(violations, header)
        }

        fun checkRule(
            violationHeader: String,
            runCheck: (MutableList<String>) -> Unit,
        ) {
            KontureRuntimeStateProvider.currentState.baselineManager.checkRule(violationHeader, runCheck)
        }

        /** Delegates [checkRuleReport] to current runtime state's [BaselineManager]. */
        fun checkRuleReport(
            ruleId: String,
            violationHeader: String,
            runCheckReport: (MutableList<Violation>) -> Unit,
        ): ViolationReport {
            return KontureRuntimeStateProvider.currentState.baselineManager.checkRuleReport(
                ruleId,
                violationHeader,
                runCheckReport,
            )
        }

        fun writeBaseline() {
            KontureRuntimeStateProvider.currentState.baselineManager.writeBaseline()
        }

        /** Returns the set of violations present in the baseline that were not observed during execution. */
        fun getResolvedViolations(): Set<FlatBaselineViolation> =
            KontureRuntimeStateProvider.currentState.baselineManager.getResolvedViolations()

        /** Returns the set of violations present in both the baseline and evaluated rules. */
        fun getActiveBaselineViolations(): Set<FlatBaselineViolation> =
            KontureRuntimeStateProvider.currentState.baselineManager.getActiveBaselineViolations()

        /** Returns the set of violations observed during execution that are not in the baseline. */
        fun getNewViolations(): Set<FlatBaselineViolation> =
            KontureRuntimeStateProvider.currentState.baselineManager.getNewViolations()

        /** Checks ratchet status and fails if resolved violations exist with ratchet mode enabled. */
        fun checkRatchet() {
            KontureRuntimeStateProvider.currentState.baselineManager.checkRatchet()
        }
    }
}
