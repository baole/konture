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
    fun loadFromStream(
        inputStream: InputStream,
        depsStreamLoader: () -> InputStream? = { null },
    ): ProjectGraph {
        val layoutContent = inputStream.bufferedReader().use { it.readText() }
        val layoutModel = json.decodeFromString<LayoutModel>(layoutContent)
        require(layoutModel.schemaVersion == 2) {
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
                                    val fileDecl = PsiParser.parseFile(resolvedFile)
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
            } ?: DependencyGraphModel()
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
