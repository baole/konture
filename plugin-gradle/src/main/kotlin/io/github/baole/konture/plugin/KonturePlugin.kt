/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("TooGenericExceptionCaught", "NestedBlockDepth", "SwallowedException")

package io.github.baole.konture.plugin

import io.github.baole.konture.core.KontureConstants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.testing.Test
import java.io.File

/**
 * The main Gradle plugin for Konture.
 *
 * This plugin performs a dual role:
 * 1. **Producer Role (Root Project)**: When applied to the root project, it registers the
 *    [GenerateArchitectureLayout] task, which extracts the full multi-project structure and
 *    dependencies of the build and serializes it into a `layout.json` file. It also registers the
 *    `archLayoutElements` outgoing configuration to share this artifact with consumer projects safely.
 * 2. **Consumer Role (Subprojects)**: Exposes the `konture` DSL block via [KontureExtension] to
 *    allow dedicated test modules to consume the generated layout schema safely in isolated projects.
 */
class KonturePlugin : Plugin<Project> {
    @Suppress("CyclomaticComplexMethod")
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, KontureExtension::class.java, project)

        val isConsumer = project != project.rootProject
        val isProducer = project == project.rootProject

        if (isConsumer) {
            KonturePluginConfigurer.setupConsumerLayout(project)
        }

        project.tasks.register(TASK_GENERATE_BASELINE) { task ->
            task.group = TASK_GROUP_VERIFICATION
            task.description = TASK_DESC_BASELINE
            task.dependsOn(project.tasks.withType(Test::class.java))
            if (isProducer) {
                project.rootProject.subprojects.forEach { subproject ->
                    task.dependsOn("${subproject.path}:$TASK_GENERATE_BASELINE")
                }
            }
        }

        project.tasks.withType(Test::class.java).configureEach { testTask ->
            val rootExtension =
                if (project == project.rootProject) {
                    extension
                } else {
                    try {
                        project.rootProject.extensions.findByType(KontureExtension::class.java)
                    } catch (_: Exception) {
                        null
                    }
                }
            val effectiveBaselinePath =
                if (rootExtension != null) {
                    extension.baselinePath.orElse(rootExtension.baselinePath)
                } else {
                    extension.baselinePath
                }
            val effectiveLanguage =
                if (rootExtension != null) extension.language.orElse(rootExtension.language) else extension.language

            val cliBaselinePath = project.providers.systemProperty(KontureConstants.PROPERTY_BASELINE_PATH).orNull
            val cliBaselineDir = project.providers.systemProperty(KontureConstants.PROPERTY_BASELINE_DIR).orNull
            val cliLanguage = project.providers.systemProperty(KontureConstants.PROPERTY_LOCALE).orNull

            if (cliBaselineDir != null) {
                testTask.systemProperty(KontureConstants.PROPERTY_BASELINE_DIR, cliBaselineDir)
            }
            if (cliBaselinePath != null) {
                testTask.systemProperty(KontureConstants.PROPERTY_BASELINE_PATH, cliBaselinePath)
            } else {
                testTask.systemProperty(
                    KontureConstants.PROPERTY_BASELINE_PATH,
                    effectiveBaselinePath.getOrElse(KontureConstants.DEFAULT_BASELINE_FILENAME),
                )
            }
            if (cliLanguage != null) {
                testTask.systemProperty(KontureConstants.PROPERTY_LOCALE, cliLanguage)
            } else {
                testTask.systemProperty(
                    KontureConstants.PROPERTY_LOCALE,
                    effectiveLanguage.getOrElse(""),
                )
            }
            val isRecordProperty =
                project.providers.systemProperty(KontureConstants.PROPERTY_BASELINE_GENERATE).orNull?.toBoolean() ?: false
            val isRunningGenerateBaseline =
                project.gradle.startParameter.taskNames.any { name ->
                    name == TASK_GENERATE_BASELINE ||
                        (
                            name.endsWith(":$TASK_GENERATE_BASELINE") &&
                                (
                                    project.path == name.substringBeforeLast(":$TASK_GENERATE_BASELINE") ||
                                        project.path.startsWith(name.substringBeforeLast(":$TASK_GENERATE_BASELINE") + ":")
                                )
                        )
                }
            testTask.systemProperty(
                KontureConstants.PROPERTY_BASELINE_GENERATE,
                (isRecordProperty || isRunningGenerateBaseline).toString(),
            )

            val baselineFileProvider =
                if (cliBaselinePath != null) {
                    project.layout.projectDirectory.file(cliBaselinePath)
                } else {
                    project.layout.projectDirectory.file(extension.baselinePath)
                }
            testTask.inputs.files(baselineFileProvider)
                .withPropertyName("kontureBaseline")
                .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)

            if (isRecordProperty || isRunningGenerateBaseline) {
                testTask.outputs.file(baselineFileProvider)
                testTask.outputs.upToDateWhen { false }
            }
        }

        if (isProducer) {
            val generateTask =
                project.tasks.register(TASK_GENERATE_LAYOUT, GenerateArchitectureLayout::class.java) { task ->
                    task.outputFile.convention(project.layout.buildDirectory.file(PATH_LAYOUT_V2))
                    task.rootProjectDir.set(project.rootDir)
                    task.excludeModules.convention(extension.excludeModules)
                    task.excludePackages.convention(extension.excludePackages)
                    task.excludeClasses.convention(extension.excludeClasses)
                    task.excludeConfigurations.convention(extension.excludeConfigurations)
                    task.logLevel.convention(extension.logLevel)
                }

            val generateDepsTask =
                project.tasks.register(TASK_GENERATE_DEPS, GenerateDependencyGraph::class.java) { task ->
                    task.outputFile.convention(project.layout.buildDirectory.file(PATH_DEPENDENCIES))
                }
            val detectExternalDependencyRules =
                project.tasks.register(TASK_DETECT_RULES, DetectExternalDependencyRules::class.java) { task ->
                    task.resultFile.convention(project.layout.buildDirectory.file(PATH_EXTERNAL_RULES))
                    val testFiles =
                        project.rootProject.allprojects.flatMap { sub ->
                            val srcDir = File(sub.projectDir, DIR_SRC)
                            if (srcDir.exists()) {
                                project.fileTree(srcDir) { pattern -> pattern.include(GLOB_KT) }.files
                            } else {
                                emptyList()
                            }
                        }
                    task.testSources.from(testFiles)
                }
            val dependencyGraphRequired = detectExternalDependencyRules.flatMap { it.resultFile }
            generateDepsTask.configure { task ->
                task.dependsOn(detectExternalDependencyRules)
                task.inputs.file(dependencyGraphRequired)
                task.onlyIf { dependencyGraphRequired.get().asFile.readText().trim().toBoolean() }
            }

            val configureTasksAction =
                Runnable {
                    generateTask.configure { task ->
                        val allSourceDirs = KonturePluginConfigurer.collectAllSourceDirs(project)
                        task.sourceFiles.from(allSourceDirs)

                        val modulesList =
                            project.rootProject.allprojects.map { sub ->
                                KonturePluginConfigurer.collectModuleDataForProject(sub)
                            }
                        task.modules.set(modulesList)
                    }

                    generateDepsTask.configure { task ->
                        val buildFilesList =
                            project.rootProject.allprojects.mapNotNull { sub ->
                                val dir = sub.projectDir
                                File(dir, "build.gradle.kts").takeIf { it.exists() }
                                    ?: File(dir, "build.gradle").takeIf { it.exists() }
                            }
                        val filesCollection = project.files(buildFilesList)
                        val settingsFile =
                            project.rootProject.file(FILE_SETTINGS_KTS).takeIf { it.exists() }
                                ?: project.rootProject.file(FILE_SETTINGS_GROOVY).takeIf { it.exists() }
                        if (settingsFile != null) {
                            filesCollection.from(settingsFile)
                        }
                        val versionCatalog = project.rootProject.file(FILE_LIBS_VERSIONS_TOML).takeIf { it.exists() }
                        if (versionCatalog != null) {
                            filesCollection.from(versionCatalog)
                        }

                        task.buildFiles.from(filesCollection)

                        val declaredMap = mutableMapOf<String, List<String>>()
                        val resolvedMap = mutableMapOf<String, List<String>>()

                        val resolvableConfigs =
                            project.configurations.filter { config ->
                                config.isCanBeResolved && isKontureDependencyConfiguration(config.name)
                            }
                        resolvableConfigs.forEach { config ->
                            val key = "${project.path}:${config.name}"
                            val declared =
                                config.dependencies.mapNotNull { dep ->
                                    val g = dep.group
                                    val n = dep.name
                                    if (g != null) "$g:$n" else null
                                }
                            declaredMap[key] = declared

                            resolvedMap[key] =
                                config.dependencies.mapNotNull { dependency ->
                                    val group = dependency.group ?: return@mapNotNull null
                                    val version = dependency.version ?: return@mapNotNull null
                                    "$group:${dependency.name}:$version"
                                }
                        }

                        task.declaredDependencies.set(declaredMap)
                        task.resolvedDependencies.set(resolvedMap)
                    }
                }

            if (project.state.executed) {
                configureTasksAction.run()
            } else {
                project.afterEvaluate {
                    configureTasksAction.run()
                }
                project.gradle.projectsEvaluated {
                    configureTasksAction.run()
                }
            }

            project.configurations.create(CONFIG_LAYOUT_ELEMENTS) { config ->
                config.isCanBeConsumed = true
                config.isCanBeResolved = false
                config.attributes { attrs ->
                    attrs.attribute(
                        Usage.USAGE_ATTRIBUTE,
                        project.objects.named(Usage::class.java, USAGE_LAYOUT),
                    )
                }
                config.outgoing.artifact(generateTask.flatMap { it.outputFile })
            }

            project.configurations.create(CONFIG_DEPS_ELEMENTS) { config ->
                config.isCanBeConsumed = true
                config.isCanBeResolved = false
                config.attributes { attrs ->
                    attrs.attribute(
                        Usage.USAGE_ATTRIBUTE,
                        project.objects.named(Usage::class.java, USAGE_DEPS),
                    )
                }
                config.outgoing.artifact(generateDepsTask.flatMap { it.outputFile })
            }
        }
    }

    private fun isKontureDependencyConfiguration(name: String): Boolean {
        val normalized = name.lowercase()
        return normalized == CONFIG_COMPILE_CLASSPATH ||
            normalized == CONFIG_RUNTIME_CLASSPATH ||
            normalized.endsWith(CONFIG_COMPILE_CLASSPATH) ||
            normalized.endsWith(CONFIG_RUNTIME_CLASSPATH)
    }

    internal fun collectSourceSets(proj: Project): List<SourceSetData> = KonturePluginConfigurer.collectSourceSets(proj)

    @Suppress("UnusedPrivateMember")
    private fun collectDependencies(proj: Project): List<DependencyData> =
        KonturePluginConfigurer.collectDependencies(proj)

    internal fun toRelPath(
        proj: Project,
        file: File,
    ): String = KonturePluginConfigurer.toRelPath(proj, file)

    internal fun compilationClasspath(
        project: Project,
        sourceSetName: String,
    ): List<String> = KonturePluginConfigurer.compilationClasspath(project, sourceSetName)

    companion object {
        private const val EXTENSION_NAME = "konture"

        private const val TASK_GENERATE_BASELINE = "generateKontureBaseline"
        private const val TASK_GENERATE_LAYOUT = "generateArchitectureLayout"
        private const val TASK_GENERATE_DEPS = "generateDependencyGraph"
        private const val TASK_DETECT_RULES = "detectKontureExternalDependencyRules"

        private const val TASK_GROUP_VERIFICATION = "verification"
        private const val TASK_DESC_BASELINE = "Generates/records the architecture baseline for this module by running unit tests."

        private const val CONFIG_LAYOUT_ELEMENTS = "archLayoutElements"
        private const val CONFIG_DEPS_ELEMENTS = "archDepsElements"

        private const val USAGE_LAYOUT = "koarch-layout"
        private const val USAGE_DEPS = "koarch-deps"

        private const val PATH_LAYOUT_V2 = "konture/layout_v2.json"
        private const val PATH_DEPENDENCIES = "konture/dependencies.json"
        private const val PATH_EXTERNAL_RULES = "konture/external-dependency-rules.txt"

        private const val FILE_SETTINGS_KTS = "settings.gradle.kts"
        private const val FILE_SETTINGS_GROOVY = "settings.gradle"
        private const val FILE_LIBS_VERSIONS_TOML = "gradle/libs.versions.toml"

        private const val CONFIG_COMPILE_CLASSPATH = "compileclasspath"
        private const val CONFIG_RUNTIME_CLASSPATH = "runtimeclasspath"

        internal const val KIND_ANDROID = "ANDROID_VARIANT"
        internal const val KIND_KMP = "KMP"
        internal const val KIND_JVM = "KOTLIN_JVM"

        internal const val PLATFORM_ANDROID = "android"
        internal const val PLATFORM_JVM = "jvm"

        internal const val COMPILATION_MAIN = "main"
        internal const val NAME_MAIN_LOWERCASE = "main"
        internal const val SUFFIX_MAIN = "Main"
        internal const val SUBSTRING_TEST_LOWERCASE = "test"

        private const val DIR_SRC = "src"
        private const val GLOB_KT = "**/*.kt"
    }
}
