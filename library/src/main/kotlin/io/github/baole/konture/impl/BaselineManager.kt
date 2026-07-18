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
import kotlinx.serialization.json.Json

internal object BaselineManager {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }

    private val baselineDir: File
        get() {
            val path = System.getProperty(Konture.PROPERTY_BASELINE_DIR) ?: System.getProperty("user.dir")
            return File(path).canonicalFile
        }

    private val buildRoot: File? by lazy {
        try {
            ProjectGraphLoader.findBuildRoot()
        } catch (e: Exception) {
            null
        }
    }

    private val baselineFile: File
        get() {
            val path = Konture.baselinePath
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
    private val existingViolations: Set<FlatBaselineViolation>
        get() {
            val currentPath = Konture.baselinePath
            val currentDirProp = System.getProperty(Konture.PROPERTY_BASELINE_DIR)
            val currentGraph =
                try {
                    Konture.projectGraph
                } catch (e: Exception) {
                    null
                }

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
                                        val moduleDir = getModuleDir(root, module)
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
                    val moduleDir = getModuleDir(root, module)
                    val file = File(moduleDir, currentPath)
                    if (file.exists()) {
                        violations.addAll(loadViolationsFromFile(file))
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
                    violations.addAll(loadViolationsFromFile(file))
                }
            }

            loadedBaselinePath = currentPath
            loadedBaselineDirProp = currentDirProp
            loadedProjectGraph = currentGraph
            loadedViolations = violations
            return violations
        }

    private fun loadViolationsFromFile(file: File): Set<FlatBaselineViolation> {
        if (!file.exists()) return emptySet()
        return try {
            KontureLogger.log(LogLevel.INFO, "Loading architecture baseline from: ${file.absolutePath}")
            val content = file.readText()

            val flatList = mutableListOf<FlatBaselineViolation>()

            val data = json.decodeFromString<BaselineData>(content)

            for (testClassConfig in data.testClasses) {
                val className = testClassConfig.name
                for (method in testClassConfig.tests) {
                    val methodName = method.name
                    for (violation in method.violations) {
                        flatList.add(
                            FlatBaselineViolation(
                                testClass = className,
                                testMethod = methodName,
                                location = violation.location,
                                message = violation.message,
                            ),
                        )
                    }
                }
            }

            flatList.toSet()
        } catch (e: Exception) {
            KontureLogger.log(LogLevel.WARNING, "Failed to parse baseline file: ${e.message}")
            emptySet()
        }
    }

    fun resetForTest() {
        loadedViolations = null
        loadedBaselinePath = null
        loadedBaselineDirProp = null
        loadedProjectGraph = null
        recordedViolations.clear()
    }

    // Thread-safe set of newly recorded violations
    private val recordedViolations = ConcurrentHashMap.newKeySet<FlatBaselineViolation>()

    init {
        // Register shutdown hook to write baseline when JVM exits (if in recording mode)
        try {
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    if (Konture.generateBaseline && recordedViolations.isNotEmpty()) {
                        writeBaseline()
                    }
                },
            )
        } catch (e: Exception) {
            KontureLogger.log(LogLevel.WARNING, "Failed to register baseline shutdown hook: ${e.message}")
        }
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

    internal fun parseLocationAndMessage(
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
            normalized = normalized.substring(7)
        } else if (normalized.startsWith("<root>")) {
            normalized = normalized.substring(6)
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

    private fun findTestLocation(): StackTraceElement? {
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            val className = element.className
            if (className == "java.lang.Thread" || className == "java.lang.Throwable") continue
            if (className.startsWith("org.junit.") ||
                className.startsWith("junit.") ||
                className.startsWith("org.testng.") ||
                className.startsWith("org.gradle.") ||
                className.startsWith("org.apache.maven.") ||
                className.startsWith("sun.reflect.") ||
                className.startsWith("java.lang.reflect.")
            ) {
                continue
            }
            if (element.methodName.contains("$")) continue
            if (className.contains("Test")) return element
            val pkg = BaselineManager::class.java.`package`?.name ?: "io.github.baole.konture.impl"
            val rootPkg = pkg.substringBefore(".impl").substringBefore(".core")
            if (className.startsWith("$rootPkg.") &&
                !className.startsWith("$rootPkg.sample.") &&
                !className.startsWith("$rootPkg.test.")
            ) {
                continue
            }
            return element
        }
        return null
    }

    /**
     * Handles a list of rule violations. If [generateBaseline] is active, the violations
     * are recorded. Otherwise, they are filtered against the existing baseline, and any
     * new violations will throw an [AssertionError].
     */
    fun handleViolations(
        violations: List<String>,
        header: String,
    ) {
        if (violations.isEmpty()) return

        val testLoc = findTestLocation()
        val testClass = testLoc?.className ?: "UnknownTest"
        val testMethod = testLoc?.methodName ?: "unknownMethod"

        val normalizedViolations =
            violations.map {
                val normMsg = normalize(it, buildRoot)
                val (location, cleanMsg) = parseLocationAndMessage(normMsg, buildRoot)
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

        if (Konture.generateBaseline) {
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

    internal fun findModuleForViolation(
        violation: FlatBaselineViolation,
        graph: ProjectGraph,
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

    private fun writeViolationsToFile(
        file: File,
        violations: List<FlatBaselineViolation>,
    ) {
        try {
            val classes = violations
                .groupBy { it.testClass }
                .map { (className, classViolations) ->
                    val tests = classViolations
                        .groupBy { it.testMethod }
                        .map { (methodName, methodViolations) ->
                            val finalViolations = methodViolations.map {
                                BaselineViolation(location = it.location, message = it.message)
                            }.sorted()
                            TestMethodConfig(name = methodName, violations = finalViolations)
                        }.sortedBy { it.name }
                    TestClassConfig(name = className, tests = tests)
                }.sortedBy { it.name }

            val data = BaselineData(
                testClasses = classes,
            )
            val content = json.encodeToString(data)

            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            file.writeText(content)
            KontureLogger.log(LogLevel.INFO, "Successfully wrote baseline file containing ${violations.size} violations to: ${file.absolutePath}")
        } catch (e: Exception) {
            KontureLogger.log(LogLevel.WARNING, "Failed to write baseline file to ${file.absolutePath}: ${e.message}")
        }
    }

    /**
     * Writes the combined baseline (existing + newly recorded) to file.
     */
    private fun getModuleDir(
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

    /**
     * Writes the combined baseline (existing + newly recorded) to file.
     */
    internal fun writeBaseline() {
        val graph =
            try {
                Konture.projectGraph
            } catch (e: Exception) {
                null
            }
        val root = buildRoot
        val isCustomDir =
            File(Konture.baselinePath).isAbsolute ||
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
                                    val moduleDir = getModuleDir(root, module)
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
                val module = findModuleForViolation(v, graph)
                if (module != null) {
                    moduleViolationsMap.getOrPut(module) { mutableListOf() }.add(v)
                } else {
                    orphanedViolations.add(v)
                }
            }

            for (module in graph.getAllModules()) {
                val moduleDir = getModuleDir(root, module)
                val file = File(moduleDir, Konture.baselinePath)
                val mViolations = moduleViolationsMap[module] ?: emptyList()

                if (mViolations.isNotEmpty()) {
                    writeViolationsToFile(file, mViolations)
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
                writeViolationsToFile(baselineFile, orphanedViolations)
            }
        } else {
            // Fallback to single baseline file
            val combined = (existingViolations + recordedViolations)
            writeViolationsToFile(baselineFile, combined.toList())
        }
    }
}
