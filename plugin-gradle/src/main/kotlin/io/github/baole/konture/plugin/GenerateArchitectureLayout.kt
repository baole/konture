/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.core.BuildModel
import io.github.baole.konture.core.DependencyEdge
import io.github.baole.konture.core.ExclusionsModel
import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LayoutModel
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.core.ModuleModel
import io.github.baole.konture.core.SourceSetKind
import io.github.baole.konture.core.SourceSetModel
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlinx.serialization.json.Json

/**
 * A standard Gradle task that extracts the multi-project module layout, source sets, applied plugins,
 * and project dependencies, and serializes them into the v2 `layout_v2.json` schema.
 *
 * This task acts as the "Producer" phase of Konture's offline static analysis mechanism.
 */
@CacheableTask
abstract class GenerateArchitectureLayout : DefaultTask() {
    /**
     * Track all Kotlin/Java source files in all modules to enable cache and up-to-date checking.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: org.gradle.api.file.ConfigurableFileCollection

    /**
     * The root project directory of the build, used to resolve and serialize relative paths.
     */
    @get:Input
    abstract val rootProjectDir: org.gradle.api.provider.Property<File>

    /**
     * Lazily populated list of module configurations from all projects in the build.
     */
    @get:Input
    abstract val modules: ListProperty<ModuleData>

    @get:Input
    @get:Optional
    abstract val excludeModules: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val excludePackages: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val excludeClasses: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val excludeConfigurations: ListProperty<String>

    @get:Input
    abstract val logLevel: org.gradle.api.provider.Property<String>

    /**
     * The output location of the generated `layout_v2.json` file.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Main task action that executes the extraction and serialization of layout metadata.
     */
    @TaskAction
    @Suppress("SwallowedException")
    fun generate() {
        val levelStr = logLevel.getOrElse("INFO").uppercase()
        val mappedLevel =
            try {
                LogLevel.valueOf(levelStr)
            } catch (e: IllegalArgumentException) {
                throw org.gradle.api.GradleException(
                    "Invalid log level: '$levelStr'. Valid levels are: TRACE, DEBUG, INFO, WARNING, ERROR",
                )
            }

        KontureLogger.minLevel = mappedLevel
        KontureLogger.logger = { level, msg, err ->
            when (level) {
                LogLevel.TRACE -> logger.trace(msg, err)
                LogLevel.DEBUG -> logger.debug(msg, err)
                LogLevel.INFO -> logger.info(msg, err)
                LogLevel.WARNING -> logger.warn(msg, err)
                LogLevel.ERROR -> logger.error(msg, err)
            }
        }

        KontureLogger.log(
            LogLevel.INFO,
            "Generating architecture layout file to ${outputFile.get().asFile.absolutePath} " +
                "with log level $mappedLevel",
        )
        KontureLogger.log(
            LogLevel.DEBUG,
            "Exclusions - Modules: ${excludeModules.orNull}, " +
                "Packages: ${excludePackages.orNull}, " +
                "Classes: ${excludeClasses.orNull}",
        )
        val json =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }

        val buildId = ":"
        val rootDir = rootProjectDir.get().canonicalFile

        val moduleModels =
            modules.get().map { input ->
                val moduleDir = File(input.projectDir).canonicalFile
                val relProjectDir = if (moduleDir == rootDir) "." else moduleDir.relativeTo(rootDir).path

                val sourceSetModels =
                    input.sourceSets.map { ssInput ->
                        val files = mutableListOf<String>()
                        ssInput.srcDirs.forEach { dirPath ->
                            val dirFile = File(dirPath).canonicalFile
                            val isGenerated =
                                try {
                                    val rel = dirFile.relativeTo(moduleDir).path
                                    rel.startsWith("build/") || rel.startsWith("build\\") || rel == "build"
                                } catch (e: IllegalArgumentException) {
                                    false
                                }
                            if (!isGenerated && dirFile.exists() && dirFile.isDirectory) {
                                dirFile
                                    .walkTopDown()
                                    .filter { it.isFile && it.extension == "kt" }
                                    .forEach { file ->
                                        val relPath = file.relativeTo(moduleDir).path
                                        files.add(relPath)
                                    }
                            }
                        }

                        val relSrcDirs =
                            ssInput.srcDirs.map { dirPath ->
                                val dirFile = File(dirPath).canonicalFile
                                if (dirFile.isAbsolute) {
                                    try {
                                        dirFile.relativeTo(moduleDir).path
                                    } catch (e: IllegalArgumentException) {
                                        dirPath
                                    }
                                } else {
                                    dirPath
                                }
                            }

                        SourceSetModel(
                            name = ssInput.name,
                            kind = SourceSetKind.valueOf(ssInput.kind),
                            production = ssInput.production,
                            srcDirs = relSrcDirs,
                            kotlinFiles = files,
                            platforms = ssInput.platforms,
                            compileClasspath = ssInput.compileClasspath,
                            jvmTarget = ssInput.jvmTarget,
                        )
                    }

                val dependencyEdges =
                    input.dependencies.map { depInput ->
                        DependencyEdge(
                            configuration = depInput.configuration,
                            targetBuildId = depInput.targetBuildId,
                            targetPath = depInput.targetPath,
                        )
                    }

                ModuleModel(
                    path = input.path,
                    projectDir = relProjectDir,
                    appliedPlugins = input.appliedPlugins,
                    sourceSets = sourceSetModels,
                    dependencies = dependencyEdges,
                )
            }

        val layoutModel =
            LayoutModel(
                schemaVersion = 2,
                builds =
                    listOf(
                        BuildModel(
                            id = buildId,
                            modules = moduleModels,
                        ),
                    ),
                exclusions =
                    ExclusionsModel(
                        excludeModules = excludeModules.getOrElse(emptyList()),
                        excludePackages = excludePackages.getOrElse(emptyList()),
                        excludeClasses = excludeClasses.getOrElse(emptyList()),
                        excludeConfigurations = excludeConfigurations.getOrElse(emptyList()),
                    ),
                logLevel = levelStr,
            )

        val jsonText = json.encodeToString(LayoutModel.serializer(), layoutModel)
        outputFile.get().asFile.writeText(jsonText)
    }
}
