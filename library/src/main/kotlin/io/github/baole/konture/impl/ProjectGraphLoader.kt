/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Dependency
import io.github.baole.konture.Module
import io.github.baole.konture.ProjectGraph
import io.github.baole.konture.SourceSet
import io.github.baole.konture.SourceSetId
import io.github.baole.konture.SourceSetKind
import io.github.baole.konture.SourceSetRole
import io.github.baole.konture.core.DependencyGraphModel
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LayoutModel
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.impl.psi.MapSymbolLookup
import io.github.baole.konture.impl.psi.TypeAliasDefinition
import java.io.File
import java.io.InputStream
import kotlinx.serialization.json.Json

/**
 * Entry point class responsible for loading the layout model from JSON configurations
 * and parsing raw Kotlin sources into class definitions using PSI-tree analysis.
 */
internal class ProjectGraphLoader {
    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    internal fun findBuildRoot(): File {
        val userDir = File(System.getProperty("user.dir")).canonicalFile

        // 1. Search for closest Gradle settings file walking up
        val ancestorDirectories = generateSequence(userDir) { it.parentFile }
        val gradleRoot =
            ancestorDirectories.firstOrNull { directory ->
                File(directory, "settings.gradle").exists() || File(directory, "settings.gradle.kts").exists()
            }

        // 2. Search for closest Maven pom.xml walking up to find the topmost pom.xml
        val topmostPomDir =
            generateSequence(userDir) { it.parentFile }
                .filter { directory -> File(directory, "pom.xml").exists() }
                .lastOrNull()

        // 3. Compare and pick the deepest (most specific) root
        if (gradleRoot != null && topmostPomDir != null) {
            return if (gradleRoot.absolutePath.length >= topmostPomDir.absolutePath.length) {
                gradleRoot
            } else {
                topmostPomDir
            }
        }

        return gradleRoot ?: topmostPomDir ?: userDir
    }

    /**
     * Deserializes the [LayoutModel] from the provided [InputStream], resolves absolute
     * paths, triggers the class parser, and populates the resulting [ProjectGraph].
     *
     * @param inputStream The source stream containing `layout.json` contents.
     * @return The instantiated, fully loaded [ProjectGraph].
     */
    @Suppress("CyclomaticComplexMethod")
    fun loadFromStream(
        inputStream: InputStream,
        depsStreamLoader: () -> InputStream? = { null },
    ): ProjectGraph {
        val layoutContent = inputStream.bufferedReader().use { it.readText() }
        val layoutModel = json.decodeFromString<LayoutModel>(layoutContent)
        require(layoutModel.schemaVersion == LayoutModel.CURRENT_SCHEMA_VERSION) {
            "Konture requires layout schema v2. Regenerate the layout with the matching Konture Gradle plugin."
        }
        val buildRoot = findBuildRoot()

        val levelStr = layoutModel.logLevel.uppercase()
        val mappedLevel = LogLevel.entries.firstOrNull { it.name == levelStr } ?: LogLevel.INFO
        KontureLogger.minLevel = mappedLevel
        KontureLogger.log(LogLevel.DEBUG, "Loaded project layout. Active log level: $mappedLevel")

        val exclusions = layoutModel.exclusions
        val isModuleExcluded = { path: String ->
            exclusions.excludeModules.any { pattern ->
                PatternMatchers.matchesModuleGlob(pattern, path)
            }
        }
        val isPackageExcluded = { packageName: String ->
            exclusions.excludePackages.any { pattern ->
                PatternMatchers.matchesPackage(pattern, packageName)
            }
        }
        val isClassExcluded = { simpleName: String, fqName: String ->
            exclusions.excludeClasses.any { pattern ->
                PatternMatchers.matchesPackage(pattern, fqName) ||
                    PatternMatchers.matchesPackage(pattern, simpleName) ||
                    PatternMatchers.matchesSimpleGlob(pattern, fqName) ||
                    PatternMatchers.matchesSimpleGlob(pattern, simpleName)
            }
        }

        // Build a declared-symbol registry per source set. Production code must not see test
        // sources or test-only dependencies during type resolution.
        val sourceSetModels = mutableMapOf<Triple<String, String, String>, io.github.baole.konture.core.SourceSetModel>()
        val declaredClassesBySourceSet = mutableMapOf<Triple<String, String, String>, Set<String>>()
        val declaredTypeAliasesBySourceSet = mutableMapOf<Triple<String, String, String>, Map<String, TypeAliasDefinition>>()
        layoutModel.builds.forEach { buildModel ->
            buildModel.modules
                .filter { !isModuleExcluded(it.path) }
                .forEach { moduleModel ->
                    val resolvedProjectDir =
                        if (File(moduleModel.projectDir).isAbsolute) {
                            File(moduleModel.projectDir)
                        } else {
                            File(buildRoot, moduleModel.projectDir)
                        }
                    moduleModel.sourceSets.forEach { sourceSetModel ->
                        val key = Triple(buildModel.id, moduleModel.path, sourceSetModel.name)
                        sourceSetModels[key] = sourceSetModel
                        val files =
                            sourceSetModel.kotlinFiles.map { filePath ->
                                val candidate = File(filePath)
                                if (candidate.isAbsolute) candidate else File(resolvedProjectDir, filePath)
                            }
                        declaredClassesBySourceSet[key] = PsiParser.getDeclaredClassFqNames(files)
                        declaredTypeAliasesBySourceSet[key] = PsiParser.getDeclaredTypeAliases(files)
                    }
                }
        }

        fun isTestConfiguration(configuration: String): Boolean {
            var start = 0
            while (true) {
                val index = configuration.indexOf("test", start, ignoreCase = true)
                if (index == -1) return false
                val end = index + 4
                val leftBoundary = index == 0 || configuration[index].isUpperCase() || !configuration[index - 1].isLetterOrDigit()
                val rightBoundary = end == configuration.length || configuration[end].isUpperCase() || !configuration[end].isLetterOrDigit()
                if (leftBoundary && rightBoundary) return true
                start = index + 1
            }
        }

        fun isCompileVisible(
            sourceSet: io.github.baole.konture.core.SourceSetModel,
            configuration: String,
        ): Boolean {
            val normalized = configuration.lowercase()
            if (normalized.contains("runtimeonly") || normalized == "runtime") return false
            if (!isTestConfiguration(configuration)) return true
            if (sourceSet.production) return false

            val sourceSetName = sourceSet.name.lowercase()
            return when {
                sourceSetName.contains("androidtest") -> normalized.contains("androidtest")
                sourceSetName.contains("commontest") -> normalized.contains("commontest")
                else -> !normalized.contains("androidtest") && !normalized.contains("commontest")
            }
        }

        fun hasCompatiblePlatforms(
            consumer: io.github.baole.konture.core.SourceSetModel,
            candidate: io.github.baole.konture.core.SourceSetModel,
        ): Boolean {
            val consumerPlatforms = consumer.platforms.toSet()
            if (consumerPlatforms.isEmpty() || !candidate.platforms.toSet().containsAll(consumerPlatforms)) return false

            // A candidate must support every consumer platform. "native" is only a platform
            // family, so native consumers additionally require every concrete target identity
            // to be covered. Missing metadata deliberately fails closed.
            return if ("native" in consumerPlatforms) {
                consumer.targetNames.isNotEmpty() &&
                    candidate.targetNames.isNotEmpty() &&
                    candidate.targetNames.toSet().containsAll(consumer.targetNames)
            } else {
                true
            }
        }

        data class VisibleSymbols(
            val classes: Set<String>,
            val typeAliases: Map<String, TypeAliasDefinition>,
        )
        val visibleSymbolsCache = mutableMapOf<Triple<String, String, String>, VisibleSymbols>()

        fun visibleSymbolsFor(sourceSetKey: Triple<String, String, String>): VisibleSymbols =
            visibleSymbolsCache.getOrPut(sourceSetKey) {
                val visited = mutableSetOf<Triple<String, String, String>>()

                fun collect(key: Triple<String, String, String>): VisibleSymbols {
                    if (!visited.add(key)) return VisibleSymbols(emptySet(), emptyMap())
                    val sourceSet = sourceSetModels[key] ?: return VisibleSymbols(emptySet(), emptyMap())
                    val model =
                        layoutModel.builds.firstOrNull { it.id == key.first }?.modules?.firstOrNull { it.path == key.second }
                            ?: return VisibleSymbols(emptySet(), emptyMap())

                    fun sourceSetClosure(start: Triple<String, String, String>): Set<Triple<String, String, String>> {
                        val closure = mutableSetOf<Triple<String, String, String>>()

                        fun visit(candidate: Triple<String, String, String>) {
                            if (!closure.add(candidate)) return
                            sourceSetModels.getValue(candidate).dependsOnSourceSets.forEach { parentName ->
                                val parent = Triple(candidate.first, candidate.second, parentName)
                                if (sourceSetModels.containsKey(parent)) visit(parent)
                            }
                        }
                        visit(start)
                        return closure
                    }
                    val ownSourceSets =
                        if (sourceSet.kind == io.github.baole.konture.core.SourceSetKind.KMP) {
                            sourceSetClosure(key)
                        } else {
                            sourceSetModels.keys.filter { candidate ->
                                candidate.first == key.first && candidate.second == key.second &&
                                    (candidate == key || (!sourceSet.production && sourceSetModels.getValue(candidate).production))
                            }.toSet()
                        }
                    val dependencySourceSets =
                        model.dependencies
                            .filter { dependency ->
                                !isModuleExcluded(dependency.targetPath) &&
                                    isCompileVisible(sourceSet, dependency.configuration) &&
                                    (
                                        sourceSet.kind != io.github.baole.konture.core.SourceSetKind.KMP ||
                                            ownSourceSets.any { candidate ->
                                                dependency.configuration in
                                                    sourceSetModels.getValue(candidate).dependencyConfigurations
                                            }
                                    )
                            }
                            .flatMap { dependency ->
                                sourceSetModels.keys.filter { candidate ->
                                    candidate.first == dependency.targetBuildId && candidate.second == dependency.targetPath &&
                                        sourceSetModels.getValue(candidate).production &&
                                        (
                                            sourceSet.kind != io.github.baole.konture.core.SourceSetKind.KMP ||
                                                hasCompatiblePlatforms(sourceSet, sourceSetModels.getValue(candidate))
                                        )
                                }
                            }
                    val dependencySymbols = dependencySourceSets.map(::collect)
                    return VisibleSymbols(
                        classes =
                            (
                                ownSourceSets.flatMap { declaredClassesBySourceSet[it].orEmpty() } +
                                    dependencySymbols.flatMap { it.classes }
                            ).toSet(),
                        typeAliases =
                            (
                                ownSourceSets.flatMap { declaredTypeAliasesBySourceSet[it].orEmpty().entries } +
                                    dependencySymbols.flatMap { it.typeAliases.entries }
                            ).associate { it.toPair() },
                    )
                }
                collect(sourceSetKey)
            }

        val builds =
            layoutModel.builds.associate { buildModel ->
                buildModel.id to
                    buildModel.modules
                        .filter { !isModuleExcluded(it.path) }
                        .map { moduleModel ->
                            val resolvedProjectDir =
                                if (File(moduleModel.projectDir).isAbsolute) {
                                    File(moduleModel.projectDir).canonicalPath
                                } else {
                                    File(buildRoot, moduleModel.projectDir).canonicalPath
                                }

                            KontureLogger.log(
                                LogLevel.DEBUG,
                                "Loading module ${moduleModel.path} from $resolvedProjectDir",
                            )

                            val pathsToSourceSets = linkedMapOf<String, MutableList<SourceSetId>>()
                            moduleModel.sourceSets.forEach { sourceSetModel ->
                                val sourceSetId =
                                    SourceSetId(
                                        modulePath = moduleModel.path,
                                        name = sourceSetModel.name,
                                        kind = sourceSetModel.kind.toPublicKind(),
                                        role = if (sourceSetModel.production) SourceSetRole.PRODUCTION else SourceSetRole.TEST,
                                    )
                                sourceSetModel.kotlinFiles.forEach { filePath ->
                                    val candidate = File(filePath)
                                    val resolved = if (candidate.isAbsolute) candidate else File(resolvedProjectDir, filePath)
                                    pathsToSourceSets.getOrPut(resolved.canonicalPath) { mutableListOf() }.add(sourceSetId)
                                }
                            }
                            val files =
                                pathsToSourceSets.mapNotNull { (path, memberships) ->
                                    val resolvedFile = File(path)
                                    val symbols =
                                        memberships
                                            .map { membership -> visibleSymbolsFor(Triple(buildModel.id, moduleModel.path, membership.name)) }
                                    val symbolLookup =
                                        MapSymbolLookup(
                                            declaredClasses = symbols.flatMap { it.classes }.toSet(),
                                            typeAliases = symbols.flatMap { it.typeAliases.entries }.associate { it.toPair() },
                                        )
                                    val fileDecl = PsiParser.parseFile(resolvedFile, symbolLookup)
                                    if (fileDecl == null) {
                                        KontureLogger.log(LogLevel.WARNING, "Failed to parse AST for Kotlin file: $path")
                                        return@mapNotNull null
                                    }
                                    if (isPackageExcluded(fileDecl.packageName)) return@mapNotNull null
                                    val filteredClasses = fileDecl.classes.filterNot { isClassExcluded(it.name, it.fqName) }
                                    fileDecl.copy(
                                        classes = filteredClasses,
                                        sourceSets = memberships.toList(),
                                        usages = fileDecl.usages.map { usage -> usage.copy(sourceSets = memberships.toList()) },
                                    )
                                }

                            Module(
                                buildId = buildModel.id,
                                path = moduleModel.path,
                                projectDir = resolvedProjectDir,
                                appliedPlugins = moduleModel.appliedPlugins,
                                sourceSets =
                                    moduleModel.sourceSets.map { ss ->
                                        val resolvedSrcDirs =
                                            ss.srcDirs.map { dirPath ->
                                                val file = File(dirPath)
                                                if (file.isAbsolute) {
                                                    file.canonicalPath
                                                } else {
                                                    File(resolvedProjectDir, dirPath).canonicalPath
                                                }
                                            }
                                        SourceSet(
                                            name = ss.name,
                                            kind = ss.kind.name,
                                            production = ss.production,
                                            srcDirs = resolvedSrcDirs,
                                            kotlinFiles = ss.kotlinFiles,
                                            platforms = ss.platforms,
                                        )
                                    },
                                dependencies =
                                    moduleModel.dependencies
                                        .filter { dep ->
                                            val lowerConf = dep.configuration.lowercase()
                                            val isConfExcluded =
                                                exclusions.excludeConfigurations.any { pattern ->
                                                    val lowerPattern = pattern.lowercase()
                                                    lowerConf.contains(lowerPattern) ||
                                                        PatternMatchers.matchesSimpleGlob(lowerPattern, lowerConf)
                                                }
                                            !isModuleExcluded(dep.targetPath) &&
                                                dep.targetPath != moduleModel.path &&
                                                !isConfExcluded
                                        }.map { dep ->
                                            Dependency(
                                                configuration = dep.configuration,
                                                targetBuildId = dep.targetBuildId,
                                                targetPath = dep.targetPath,
                                            )
                                        },
                                files = files,
                            )
                        }
            }

        return ProjectGraph(builds) {
            depsStreamLoader()?.use { stream ->
                try {
                    KontureLogger.log(LogLevel.DEBUG, "Lazy loading external dependencies schema...")
                    val content = stream.bufferedReader().use { it.readText() }
                    json.decodeFromString<DependencyGraphModel>(content)
                } catch (e: kotlinx.serialization.SerializationException) {
                    KontureLogger.log(LogLevel.WARNING, "Failed to parse dependencies.json structure: ${e.message}")
                    DependencyGraphModel()
                } catch (e: java.io.IOException) {
                    KontureLogger.log(LogLevel.WARNING, "Failed to read dependencies.json content: ${e.message}")
                    DependencyGraphModel()
                }
            }
        }
    }

    /**
     * Resolves and loads the layout model from a classpath resource.
     *
     * @param resourcePath The classpath resource path of the generated layout file (defaults to `/konture/layout.json`).
     * @return The fully populated [ProjectGraph].
     * @throws IllegalArgumentException if the resource cannot be found on the classpath.
     */
    fun loadFromResource(resourcePath: String = "/konture/layout_v2.json"): ProjectGraph {
        val stream = javaClass.getResourceAsStream(resourcePath)
        if (stream != null) {
            KontureLogger.log(LogLevel.DEBUG, "Loading layout_v2.json from classpath resources: $resourcePath")
            val depsPath = resourcePath.replace("layout_v2.json", "dependencies.json")
            return loadFromStream(
                inputStream = stream,
                depsStreamLoader = { javaClass.getResourceAsStream(depsPath) },
            )
        }

        // Fallback: search for layout_v2.json in the build directory of the build root
        val buildRoot = findBuildRoot()
        val fallbackFile = File(buildRoot, "build/konture/layout_v2.json")
        KontureLogger.log(
            LogLevel.WARNING,
            "layout_v2.json not found in classpath resources. Searching at fallback location: ${fallbackFile.absolutePath}",
        )
        if (fallbackFile.exists()) {
            KontureLogger.log(LogLevel.INFO, "Loaded layout_v2.json from fallback file: ${fallbackFile.absolutePath}")
            val fallbackDepsFile = File(buildRoot, "build/konture/dependencies.json")
            return loadFromStream(
                inputStream = fallbackFile.inputStream(),
                depsStreamLoader = {
                    if (fallbackDepsFile.exists()) fallbackDepsFile.inputStream() else null
                },
            )
        }

        throw IllegalArgumentException(
            "Konture layout_v2.json was not found at $resourcePath or ${fallbackFile.absolutePath}. " +
                "Run ./gradlew generateArchitectureLayout, then rerun the architecture tests.",
        )
    }

    companion object {
        fun findBuildRoot(): File {
            return KontureContextProvider.currentContext.projectGraphLoader.findBuildRoot()
        }

        fun loadFromStream(
            inputStream: InputStream,
            depsStreamLoader: () -> InputStream? = { null },
        ): ProjectGraph {
            return KontureContextProvider.currentContext.projectGraphLoader.loadFromStream(inputStream, depsStreamLoader)
        }

        fun loadFromResource(resourcePath: String = "/konture/layout_v2.json"): ProjectGraph {
            return KontureContextProvider.currentContext.projectGraphLoader.loadFromResource(resourcePath)
        }
    }
}

private fun io.github.baole.konture.core.SourceSetKind.toPublicKind(): SourceSetKind =
    when (this) {
        io.github.baole.konture.core.SourceSetKind.KOTLIN_JVM -> SourceSetKind.JVM
        io.github.baole.konture.core.SourceSetKind.ANDROID_VARIANT -> SourceSetKind.ANDROID
        io.github.baole.konture.core.SourceSetKind.KMP -> SourceSetKind.KMP
    }

/**
 * Extension on [ProjectGraph.Companion] to load the layout model from a classpath resource
 * and automatically set it as the thread-local default session graph.
 *
 * @param resourcePath Classpath resource path to load from (defaults to `/konture/layout.json`).
 * @return The loaded [ProjectGraph] instance.
 */
fun ProjectGraph.Companion.fromResource(resourcePath: String = "/konture/layout_v2.json"): ProjectGraph {
    val graph = ProjectGraphLoader.loadFromResource(resourcePath)
    setDefault(graph)
    return graph
}
