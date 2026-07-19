/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("TooGenericExceptionCaught", "NestedBlockDepth", "SwallowedException")

package io.github.baole.konture.plugin

import com.android.build.api.dsl.CommonExtension
import io.github.baole.konture.core.KontureConstants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
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

        // Automatically configure consumer layout sharing on subprojects
        if (project != project.rootProject) {
            setupConsumerLayout(project)
        }

        project.tasks.register(TASK_GENERATE_BASELINE) { task ->
            task.group = TASK_GROUP_VERIFICATION
            task.description = TASK_DESC_BASELINE
            task.dependsOn(project.tasks.withType(org.gradle.api.tasks.testing.Test::class.java))
            if (project == project.rootProject) {
                project.subprojects.forEach { subproject ->
                    task.dependsOn(subproject.tasks.matching { t -> t.name == TASK_GENERATE_BASELINE })
                }
            }
        }

        project.tasks.withType(org.gradle.api.tasks.testing.Test::class.java).configureEach { testTask ->
            val cliBaselinePath = project.providers.systemProperty(KontureConstants.PROPERTY_BASELINE_PATH).orNull
            val cliBaselineDir = project.providers.systemProperty(KontureConstants.PROPERTY_BASELINE_DIR).orNull
            val cliLanguage = project.providers.systemProperty(KontureConstants.PROPERTY_LOCALE).orNull

            testTask.systemProperty(KontureConstants.PROPERTY_BASELINE_DIR, cliBaselineDir ?: project.projectDir.absolutePath)
            if (cliBaselinePath != null) {
                testTask.systemProperty(KontureConstants.PROPERTY_BASELINE_PATH, cliBaselinePath)
            } else {
                testTask.systemProperty(KontureConstants.PROPERTY_BASELINE_PATH, extension.baselinePath)
            }
            if (cliLanguage != null) {
                testTask.systemProperty(KontureConstants.PROPERTY_LOCALE, cliLanguage)
            } else {
                testTask.systemProperty(KontureConstants.PROPERTY_LOCALE, extension.language)
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
        }

        // Root/Producer logic
        if (project == project.rootProject) {
            val generateTask =
                project.tasks.register(TASK_GENERATE_LAYOUT, GenerateArchitectureLayout::class.java) { task ->
                    task.outputFile.convention(project.layout.buildDirectory.file(PATH_LAYOUT_V2))
                    task.rootProjectDir.set(project.rootDir)
                    task.excludeModules.set(extension.excludeModules)
                    task.excludePackages.set(extension.excludePackages)
                    task.excludeClasses.set(extension.excludeClasses)
                    task.excludeConfigurations.set(extension.excludeConfigurations)
                    task.logLevel.set(extension.logLevel)
                }

            val generateDepsTask =
                project.tasks.register(TASK_GENERATE_DEPS, GenerateDependencyGraph::class.java) { task ->
                    task.outputFile.convention(project.layout.buildDirectory.file(PATH_DEPENDENCIES))
                }
            val detectExternalDependencyRules =
                project.tasks.register(TASK_DETECT_RULES, DetectExternalDependencyRules::class.java) { task ->
                    task.resultFile.convention(project.layout.buildDirectory.file(PATH_EXTERNAL_RULES))
                }
            val dependencyGraphRequired = detectExternalDependencyRules.flatMap { it.resultFile }
            generateDepsTask.configure { task ->
                task.dependsOn(detectExternalDependencyRules)
                task.inputs.file(dependencyGraphRequired)
                task.onlyIf { dependencyGraphRequired.get().asFile.readText().trim().toBoolean() }
            }

            // Eagerly evaluate project mapping and source directory listings during configuration phase
            // by using afterEvaluate block. This ensures full Configuration Cache compatibility
            // since the computed list is passed directly as a task property without capturing any live Project references.
            project.afterEvaluate {
                generateTask.configure { task ->
                    val allSourceDirs = collectAllSourceDirs(project)
                    task.sourceFiles.from(allSourceDirs)

                    val modulesList =
                        project.allprojects.map { sub ->
                            val plugins = mutableListOf<String>()
                            if (sub.pluginManager.hasPlugin(ID_KOTLIN_JVM)) plugins.add(PLUGIN_KOTLIN_JVM)
                            if (sub.pluginManager.hasPlugin(ID_ANDROID_APPLICATION)) {
                                plugins.add(PLUGIN_ANDROID_APP)
                            }
                            if (sub.pluginManager.hasPlugin(ID_ANDROID_LIBRARY)) plugins.add(PLUGIN_ANDROID_LIB)
                            // Support specialized Android plugins: dynamic-feature modules and separate test modules
                            if (sub.pluginManager.hasPlugin(ID_ANDROID_DYNAMIC_FEATURE) ||
                                sub.plugins.any { it.javaClass.simpleName.contains("DynamicFeature") }
                            ) {
                                plugins.add(PLUGIN_ANDROID_FEATURE)
                            }
                            if (sub.pluginManager.hasPlugin(ID_ANDROID_TEST) ||
                                sub.plugins.any {
                                    it.javaClass.simpleName.contains("TestPlugin") ||
                                        it.javaClass.simpleName.contains("AndroidTest")
                                }
                            ) {
                                plugins.add(PLUGIN_ANDROID_TEST)
                            }
                            if (sub.pluginManager.hasPlugin(ID_KOTLIN_MULTIPLATFORM)) {
                                plugins.add(PLUGIN_KOTLIN_KMP)
                            }

                            val sourceSets = collectSourceSets(sub)
                            val dependencies = collectDependencies(sub)

                            ModuleData(
                                path = sub.path,
                                projectDir = sub.projectDir.absolutePath,
                                appliedPlugins = plugins,
                                sourceSets = sourceSets,
                                dependencies = dependencies,
                            )
                        }
                    task.modules.set(modulesList)
                }

                generateDepsTask.configure { task ->
                    val buildFilesList = project.allprojects.map { it.buildFile }
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

                    project.allprojects.forEach { sub ->
                        val resolvableConfigs =
                            sub.configurations.filter { config ->
                                config.isCanBeResolved && isKontureDependencyConfiguration(config.name)
                            }
                        resolvableConfigs.forEach { config ->
                            val key = "${sub.path}:${config.name}"
                            val declared =
                                config.dependencies.mapNotNull { dep ->
                                    val g = dep.group
                                    val n = dep.name
                                    if (g != null) "$g:$n" else null
                                }
                            declaredMap[key] = declared

                            // Do not resolve Gradle configurations while writing the configuration cache.
                            // Direct external dependencies are sufficient for Konture's optional dependency graph;
                            // transitive resolution remains deliberately out of the configuration phase.
                            resolvedMap[key] =
                                config.dependencies.mapNotNull { dependency ->
                                    val group = dependency.group ?: return@mapNotNull null
                                    val version = dependency.version ?: return@mapNotNull null
                                    "$group:${dependency.name}:$version"
                                }
                        }
                    }

                    task.declaredDependencies.set(declaredMap)

                    task.resolvedDependencies.set(resolvedMap)
                }
            }

            // Define outgoing configuration 'archLayoutElements'
            project.configurations.create(CONFIG_LAYOUT_ELEMENTS) { config ->
                config.isCanBeConsumed = true
                config.isCanBeResolved = false
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, USAGE_LAYOUT),
                    )
                }
                config.outgoing.artifact(generateTask.flatMap { it.outputFile })
            }

            // Define outgoing configuration 'archDepsElements'
            project.configurations.create(CONFIG_DEPS_ELEMENTS) { config ->
                config.isCanBeConsumed = true
                config.isCanBeResolved = false
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, USAGE_DEPS),
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

    @Suppress("CyclomaticComplexMethod")
    internal fun collectSourceSets(proj: Project): List<SourceSetData> {
        val list = mutableListOf<SourceSetData>()
        // CommonExtension is supplied by AGP, which is intentionally only a compile-time
        // dependency of this plugin. Do not resolve its class in ordinary JVM/KMP builds.
        val androidExtension = if (proj.hasAndroidPlugin()) proj.extensions.findByType(CommonExtension::class.java) else null
        if (androidExtension != null) {
            androidExtension.sourceSets.forEach { sourceSet ->
                val name = sourceSet.name
                val srcDirs = sourceSet.java.directories + sourceSet.kotlin.directories
                list.add(
                    SourceSetData(
                        name = name,
                        kind = KIND_ANDROID,
                        production = !name.lowercase().contains(SUBSTRING_TEST_LOWERCASE),
                        srcDirs = srcDirs.map { File(it).absolutePath },
                        platforms = listOf(PLATFORM_ANDROID),
                        compileClasspath = compilationClasspath(proj, name),
                    ),
                )
            }
        }

        if (list.isEmpty()) {
            val kotlinExt = proj.extensions.findByType(KotlinProjectExtension::class.java)
            if (kotlinExt != null) {
                val kmpExtension = proj.extensions.findByType(KotlinMultiplatformExtension::class.java)
                val isKmp = kmpExtension != null
                val kind = if (isKmp) KIND_KMP else KIND_JVM

                // Capture target metadata and each compilation's actual source-set closure with
                // the public Kotlin Gradle API. The latter includes KGP's implicit commonMain
                // relationship as well as explicit dependsOn edges.
                val sourceSetPlatforms = mutableMapOf<String, MutableSet<String>>()
                val sourceSetTargets = mutableMapOf<String, MutableSet<String>>()
                val sourceSetVisibility = mutableMapOf<String, MutableSet<String>>()
                val sourceSetDependencyConfigurations = mutableMapOf<String, MutableSet<String>>()
                val mainCompilationSourceSets = mutableSetOf<String>()
                kmpExtension?.targets?.forEach { target ->
                    val targetPlatforms = listOf(target.platformType.name)
                    val nativeTargetIdentity = (target as? KotlinNativeTarget)?.konanTarget?.name
                    target.compilations.forEach { compilation ->
                        val compilationSourceSets = compilation.allKotlinSourceSets
                        compilationSourceSets.forEach { sourceSet ->
                            sourceSetPlatforms.getOrPut(sourceSet.name) { mutableSetOf() }.addAll(targetPlatforms)
                            if (nativeTargetIdentity != null) {
                                sourceSetTargets.getOrPut(sourceSet.name) { mutableSetOf() }.add(nativeTargetIdentity)
                            }
                        }
                        val defaultSourceSet = compilation.defaultSourceSet.name
                        sourceSetVisibility
                            .getOrPut(defaultSourceSet) { mutableSetOf() }
                            .addAll(compilationSourceSets.map { it.name }.filterNot { it == defaultSourceSet })
                        sourceSetDependencyConfigurations
                            .getOrPut(defaultSourceSet) { mutableSetOf() }
                            .addAll(
                                listOfNotNull(
                                    compilation.apiConfigurationName,
                                    compilation.implementationConfigurationName,
                                    compilation.compileOnlyConfigurationName,
                                    compilation.runtimeOnlyConfigurationName,
                                    compilation.compileDependencyConfigurationName,
                                    compilation.runtimeDependencyConfigurationName,
                                ),
                            )
                        if (compilation.name.equals(COMPILATION_MAIN, ignoreCase = true)) {
                            mainCompilationSourceSets.addAll(compilationSourceSets.map { it.name })
                        }
                    }
                }
                // Source-set dependency buckets do not necessarily belong to a target's default
                // compilation (for example, an intermediate source set). Associate each actual
                // Gradle configuration with its longest matching source-set name.
                proj.configurations.forEach { configuration ->
                    val owner =
                        kotlinExt.sourceSets
                            .map { it.name }
                            .filter { sourceSetName ->
                                configuration.name == sourceSetName ||
                                    (
                                        configuration.name.startsWith(sourceSetName) &&
                                            configuration.name.getOrNull(sourceSetName.length)?.isUpperCase() == true
                                    )
                            }.maxByOrNull(String::length)
                    if (owner != null) {
                        sourceSetDependencyConfigurations.getOrPut(owner) { mutableSetOf() }.add(configuration.name)
                    }
                }

                for (sourceSet in kotlinExt.sourceSets) {
                    val name = sourceSet.name
                    val isProduction =
                        if (isKmp) {
                            name in mainCompilationSourceSets
                        } else {
                            // Single-platform Java/Kotlin JVM modules use main and test.
                            name == NAME_MAIN_LOWERCASE ||
                                (
                                    name.endsWith(SUFFIX_MAIN, ignoreCase = true) &&
                                        !name.lowercase().contains(SUBSTRING_TEST_LOWERCASE)
                                )
                        }
                    val platforms =
                        if (isKmp) {
                            sourceSetPlatforms[name]?.toList() ?: emptyList()
                        } else {
                            listOf(PLATFORM_JVM)
                        }
                    list.add(
                        SourceSetData(
                            name = name,
                            kind = kind,
                            production = isProduction,
                            srcDirs = sourceSet.kotlin.srcDirs.map { it.absolutePath },
                            platforms = platforms,
                            targetNames = if (isKmp) sourceSetTargets[name]?.toList() ?: emptyList() else emptyList(),
                            dependsOnSourceSets =
                                if (isKmp) {
                                    sourceSetVisibility[name]?.toList() ?: sourceSet.dependsOn.map { it.name }
                                } else {
                                    emptyList()
                                },
                            dependencyConfigurations =
                                if (isKmp) sourceSetDependencyConfigurations[name]?.toList() ?: emptyList() else emptyList(),
                            compileClasspath = compilationClasspath(proj, name),
                        ),
                    )
                }
            }
        }

        if (list.isEmpty()) {
            val javaSourceSets = proj.extensions.findByName(EXTENSION_SOURCE_SETS) as? org.gradle.api.tasks.SourceSetContainer
            if (javaSourceSets != null) {
                for (ss in javaSourceSets) {
                    list.add(
                        SourceSetData(
                            name = ss.name,
                            kind = KIND_JVM,
                            production = ss.name == NAME_MAIN_LOWERCASE,
                            srcDirs = ss.allSource.srcDirs.map { it.absolutePath },
                            platforms = listOf(PLATFORM_JVM),
                            compileClasspath = compilationClasspath(proj, ss.name),
                        ),
                    )
                }
            }
        }
        return list
    }

    /** Returns resolved compile-classpath paths when Gradle exposes a matching configuration. */
    private fun compilationClasspath(
        project: Project,
        sourceSetName: String,
    ): List<String> {
        val candidates = listOf("$sourceSetName$SUFFIX_COMPILE_CLASSPATH_CAMEL", CONFIG_COMPILE_CLASSPATH_LOWER)
        val configuration = candidates.firstNotNullOfOrNull { project.configurations.findByName(it) } ?: return emptyList()
        if (!configuration.isCanBeResolved) return emptyList()
        return try {
            configuration.resolve().map { it.canonicalPath }.sorted()
        } catch (exception: Exception) {
            project.logger.info("Konture could not resolve compiler classpath for $sourceSetName: ${exception.message}")
            emptyList()
        }
    }

    private fun collectDependencies(proj: Project): List<DependencyData> {
        val deps = mutableListOf<DependencyData>()
        val localGroups =
            proj.rootProject.allprojects
                .mapNotNull { it.group as? String }
                .filter { it.isNotEmpty() }
                .toSet()
        val includedBuildGroups =
            proj.gradle.includedBuilds
                .map { it.name }
                .toSet()

        proj.configurations.forEach { config ->
            config.dependencies.forEach { dep ->
                if (dep is ProjectDependency) {
                    deps.add(
                        DependencyData(
                            configuration = config.name,
                            targetBuildId = ":",
                            targetPath = dep.path,
                        ),
                    )
                } else {
                    // Composite build and dynamic group dependency resolution heuristic
                    val depGroup = dep.group
                    val depName = dep.name
                    if (depGroup != null) {
                        if (depGroup in localGroups || depGroup in includedBuildGroups ||
                            depGroup.startsWith(GROUP_PREFIX_KONTURE)
                        ) {
                            deps.add(
                                DependencyData(
                                    configuration = config.name,
                                    targetBuildId = ":",
                                    targetPath = ":$depName",
                                ),
                            )
                        }
                    }
                }
            }
        }
        return deps
    }

    private fun collectAllSourceDirs(proj: Project): List<File> =
        proj.allprojects.flatMap { sub ->
            val list = mutableListOf<File>()
            val androidExtension = if (sub.hasAndroidPlugin()) sub.extensions.findByType(CommonExtension::class.java) else null
            if (androidExtension != null) {
                androidExtension.sourceSets.forEach { sourceSet ->
                    list.addAll(sourceSet.java.directories.map(::File))
                    list.addAll(sourceSet.kotlin.directories.map(::File))
                }
            }

            if (list.isEmpty()) {
                val kotlinExt = sub.extensions.findByType(KotlinProjectExtension::class.java)
                if (kotlinExt != null) {
                    for (sourceSet in kotlinExt.sourceSets) {
                        list.addAll(sourceSet.kotlin.srcDirs)
                    }
                }
            }
            if (list.isEmpty()) {
                val javaSourceSets = sub.extensions.findByName(EXTENSION_SOURCE_SETS) as? org.gradle.api.tasks.SourceSetContainer
                if (javaSourceSets != null) {
                    for (ss in javaSourceSets) {
                        list.addAll(ss.allSource.srcDirs)
                    }
                }
            }
            val buildDir =
                try {
                    sub.layout.buildDirectory
                        .get()
                        .asFile.canonicalFile
                } catch (e: Exception) {
                    null
                }
            if (buildDir != null) {
                list.filter { dir ->
                    try {
                        val canonicalDir = dir.canonicalFile
                        !canonicalDir.startsWith(buildDir)
                    } catch (e: Exception) {
                        true
                    }
                }
            } else {
                list
            }
        }

    private fun Project.hasAndroidPlugin(): Boolean =
        pluginManager.hasPlugin(ID_ANDROID_APPLICATION) ||
            pluginManager.hasPlugin(ID_ANDROID_LIBRARY) ||
            pluginManager.hasPlugin(ID_ANDROID_TEST) ||
            pluginManager.hasPlugin(ID_ANDROID_DYNAMIC_FEATURE)

    private fun setupConsumerLayout(project: Project) {
        if (project.configurations.findByName(CONFIG_LAYOUT_INCOMING) != null) {
            return
        }
        val archLayoutIncoming =
            project.configurations.create(CONFIG_LAYOUT_INCOMING) { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, USAGE_LAYOUT),
                    )
                }
            }

        val archDepsIncoming =
            project.configurations.create(CONFIG_DEPS_INCOMING) { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, USAGE_DEPS),
                    )
                }
            }

        // Add dependencies on the root project
        project.dependencies.add(CONFIG_LAYOUT_INCOMING, project.dependencies.project(mapOf("path" to ":")))
        project.dependencies.add(CONFIG_DEPS_INCOMING, project.dependencies.project(mapOf("path" to ":")))

        // Copy only the matching v2 layout and clear layouts from previously checked-out branches.
        val cleanLayoutResources =
            project.tasks.register(TASK_CLEAN_LAYOUT_RESOURCES, org.gradle.api.tasks.Delete::class.java) { delete ->
                delete.delete(
                    project.layout.buildDirectory.file("$PATH_RESOURCES_TEST_KONTURE/$FILE_LAYOUT"),
                    project.layout.buildDirectory.file("$PATH_RESOURCES_TEST_KONTURE/$FILE_LAYOUT_V2"),
                )
            }
        val copyLayoutTask =
            project.tasks.register(TASK_COPY_LAYOUT, org.gradle.api.tasks.Copy::class.java) { copy ->
                copy.from(archLayoutIncoming)
                copy.into(project.layout.buildDirectory.dir(PATH_RESOURCES_TEST_KONTURE))
                copy.rename { FILE_LAYOUT_V2 }
                copy.dependsOn(cleanLayoutResources)
            }

        val copyDepsTask =
            project.tasks.register(TASK_COPY_DEPS, org.gradle.api.tasks.Copy::class.java) { copy ->
                copy.from(archDepsIncoming)
                copy.into(project.layout.buildDirectory.dir(PATH_RESOURCES_TEST_KONTURE))
                copy.rename { FILE_DEPENDENCIES }
            }

        val cleanDependencyResource =
            project.tasks.register(TASK_CLEAN_DEPS_RESOURCE, org.gradle.api.tasks.Delete::class.java) { delete ->
                delete.delete(project.layout.buildDirectory.file("$PATH_RESOURCES_TEST_KONTURE/$FILE_DEPENDENCIES"))
            }
        val rootDetector =
            project.rootProject.tasks.findByName(TASK_DETECT_RULES) as? DetectExternalDependencyRules
        rootDetector?.testSources?.from(project.fileTree(DIR_SRC) { pattern -> pattern.include(GLOB_KT) })
        copyDepsTask.configure { task ->
            task.dependsOn(cleanDependencyResource)
            if (rootDetector == null) {
                task.onlyIf { false }
            } else {
                val detectorOutput = rootDetector.resultFile
                task.dependsOn(rootDetector)
                task.inputs.file(detectorOutput)
                task.onlyIf { detectorOutput.get().asFile.readText().trim().toBoolean() }
            }
        }
        // Make processTestResources depend on copy tasks
        project.tasks.configureEach { task ->
            if (task.name == TASK_PROCESS_TEST_RESOURCES) {
                task.dependsOn(copyLayoutTask)
                task.dependsOn(cleanDependencyResource)
                task.dependsOn(copyDepsTask)
            }
        }
    }

    companion object {
        private const val EXTENSION_NAME = "konture"
        private const val EXTENSION_SOURCE_SETS = "sourceSets"

        private const val TASK_GENERATE_BASELINE = "generateKontureBaseline"
        private const val TASK_GENERATE_LAYOUT = "generateArchitectureLayout"
        private const val TASK_GENERATE_DEPS = "generateDependencyGraph"
        private const val TASK_DETECT_RULES = "detectKontureExternalDependencyRules"
        private const val TASK_COPY_LAYOUT = "copyArchitectureLayout"
        private const val TASK_COPY_DEPS = "copyArchitectureDeps"
        private const val TASK_CLEAN_LAYOUT_RESOURCES = "cleanArchitectureLayoutResources"
        private const val TASK_CLEAN_DEPS_RESOURCE = "cleanArchitectureDependencyResource"
        private const val TASK_PROCESS_TEST_RESOURCES = "processTestResources"

        private const val TASK_GROUP_VERIFICATION = "verification"
        private const val TASK_DESC_BASELINE = "Generates/records the architecture baseline for this module by running unit tests."

        private const val CONFIG_LAYOUT_ELEMENTS = "archLayoutElements"
        private const val CONFIG_DEPS_ELEMENTS = "archDepsElements"
        private const val CONFIG_LAYOUT_INCOMING = "archLayoutIncoming"
        private const val CONFIG_DEPS_INCOMING = "archDepsIncoming"

        private const val USAGE_LAYOUT = "koarch-layout"
        private const val USAGE_DEPS = "koarch-deps"

        private const val PATH_LAYOUT_V2 = "konture/layout_v2.json"
        private const val PATH_DEPENDENCIES = "konture/dependencies.json"
        private const val PATH_EXTERNAL_RULES = "konture/external-dependency-rules.txt"
        private const val PATH_RESOURCES_TEST_KONTURE = "resources/test/konture"

        private const val FILE_LAYOUT = "layout.json"
        private const val FILE_LAYOUT_V2 = "layout_v2.json"
        private const val FILE_DEPENDENCIES = "dependencies.json"
        private const val FILE_SETTINGS_KTS = "settings.gradle.kts"
        private const val FILE_SETTINGS_GROOVY = "settings.gradle"
        private const val FILE_LIBS_VERSIONS_TOML = "gradle/libs.versions.toml"

        private const val PLUGIN_KOTLIN_JVM = "kotlin-jvm"
        private const val PLUGIN_ANDROID_APP = "android-application"
        private const val PLUGIN_ANDROID_LIB = "android-library"
        private const val PLUGIN_ANDROID_FEATURE = "android-dynamic-feature"
        private const val PLUGIN_ANDROID_TEST = "android-test"
        private const val PLUGIN_KOTLIN_KMP = "kotlin-multiplatform"

        private const val ID_KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
        private const val ID_ANDROID_APPLICATION = "com.android.application"
        private const val ID_ANDROID_LIBRARY = "com.android.library"
        private const val ID_ANDROID_DYNAMIC_FEATURE = "com.android.dynamic-feature"
        private const val ID_ANDROID_TEST = "com.android.test"
        private const val ID_KOTLIN_MULTIPLATFORM = "org.jetbrains.kotlin.multiplatform"

        private const val KIND_ANDROID = "ANDROID_VARIANT"
        private const val KIND_KMP = "KMP"
        private const val KIND_JVM = "KOTLIN_JVM"

        private const val PLATFORM_ANDROID = "android"
        private const val PLATFORM_JVM = "jvm"

        private const val CONFIG_COMPILE_CLASSPATH = "compileclasspath"
        private const val CONFIG_RUNTIME_CLASSPATH = "runtimeclasspath"

        private const val COMPILATION_MAIN = "main"
        private const val NAME_MAIN_LOWERCASE = "main"
        private const val SUFFIX_MAIN = "Main"
        private const val SUBSTRING_TEST_LOWERCASE = "test"

        private const val SUFFIX_COMPILE_CLASSPATH_CAMEL = "CompileClasspath"
        private const val CONFIG_COMPILE_CLASSPATH_LOWER = "compileClasspath"

        private const val GROUP_PREFIX_KONTURE = "io.github.baole"

        private const val DIR_SRC = "src"
        private const val GLOB_KT = "**/*.kt"
    }
}
