/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("TooGenericExceptionCaught", "NestedBlockDepth", "SwallowedException")

package io.github.baole.konture.plugin

import io.github.baole.konture.core.KontureConstants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
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
        val extension = project.extensions.create("konture", KontureExtension::class.java, project)

        // Automatically configure consumer layout sharing on subprojects
        if (project != project.rootProject) {
            setupConsumerLayout(project)
        }

        project.tasks.register("generateKontureBaseline") { task ->
            task.group = "verification"
            task.description = "Generates/records the architecture baseline for this module by running unit tests."
            task.dependsOn(project.tasks.withType(org.gradle.api.tasks.testing.Test::class.java))
            if (project == project.rootProject) {
                project.subprojects.forEach { subproject ->
                    task.dependsOn(subproject.tasks.matching { t -> t.name == "generateKontureBaseline" })
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
                    name == "generateKontureBaseline" ||
                        (
                            name.endsWith(":generateKontureBaseline") &&
                                (
                                    project.path == name.substringBeforeLast(":generateKontureBaseline") ||
                                        project.path.startsWith(name.substringBeforeLast(":generateKontureBaseline") + ":")
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
                project.tasks.register("generateArchitectureLayout", GenerateArchitectureLayout::class.java) { task ->
                    task.outputFile.convention(project.layout.buildDirectory.file("konture/layout_v2.json"))
                    task.rootProjectDir.set(project.rootDir)
                    task.excludeModules.set(extension.excludeModules)
                    task.excludePackages.set(extension.excludePackages)
                    task.excludeClasses.set(extension.excludeClasses)
                    task.excludeConfigurations.set(extension.excludeConfigurations)
                    task.logLevel.set(extension.logLevel)
                }

            val generateDepsTask =
                project.tasks.register("generateDependencyGraph", GenerateDependencyGraph::class.java) { task ->
                    task.outputFile.convention(project.layout.buildDirectory.file("konture/dependencies.json"))
                }
            val detectExternalDependencyRules =
                project.tasks.register("detectKontureExternalDependencyRules", DetectExternalDependencyRules::class.java) { task ->
                    task.resultFile.convention(project.layout.buildDirectory.file("konture/external-dependency-rules.txt"))
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
                            if (sub.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) plugins.add("kotlin-jvm")
                            if (sub.pluginManager.hasPlugin("com.android.application")) {
                                plugins.add("android-application")
                            }
                            if (sub.pluginManager.hasPlugin("com.android.library")) plugins.add("android-library")
                            // Support specialized Android plugins: dynamic-feature modules and separate test modules
                            if (sub.pluginManager.hasPlugin("com.android.dynamic-feature") ||
                                sub.plugins.any { it.javaClass.simpleName.contains("DynamicFeature") }
                            ) {
                                plugins.add("android-dynamic-feature")
                            }
                            if (sub.pluginManager.hasPlugin("com.android.test") ||
                                sub.plugins.any {
                                    it.javaClass.simpleName.contains("TestPlugin") ||
                                        it.javaClass.simpleName.contains("AndroidTest")
                                }
                            ) {
                                plugins.add("android-test")
                            }
                            if (sub.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                                plugins.add("kotlin-multiplatform")
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
                        project.rootProject.file("settings.gradle.kts").takeIf { it.exists() }
                            ?: project.rootProject.file("settings.gradle").takeIf { it.exists() }
                    if (settingsFile != null) {
                        filesCollection.from(settingsFile)
                    }
                    val versionCatalog = project.rootProject.file("gradle/libs.versions.toml").takeIf { it.exists() }
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
            project.configurations.create("archLayoutElements") { config ->
                config.isCanBeConsumed = true
                config.isCanBeResolved = false
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, "koarch-layout"),
                    )
                }
                config.outgoing.artifact(generateTask.flatMap { it.outputFile })
            }

            // Define outgoing configuration 'archDepsElements'
            project.configurations.create("archDepsElements") { config ->
                config.isCanBeConsumed = true
                config.isCanBeResolved = false
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, "koarch-deps"),
                    )
                }
                config.outgoing.artifact(generateDepsTask.flatMap { it.outputFile })
            }
        }
    }

    private fun isKontureDependencyConfiguration(name: String): Boolean {
        val normalized = name.lowercase()
        return normalized == "compileclasspath" ||
            normalized == "runtimeclasspath" ||
            normalized.endsWith("compileclasspath") ||
            normalized.endsWith("runtimeclasspath")
    }

    internal fun collectSourceSets(proj: Project): List<SourceSetData> {
        val list = mutableListOf<SourceSetData>()
        val androidExt = proj.extensions.findByName("android")
        if (androidExt != null) {
            try {
                val sourceSetsContainer =
                    androidExt.callMethod(
                        "getSourceSets",
                    ) as? org.gradle.api.NamedDomainObjectContainer<*>
                if (sourceSetsContainer != null) {
                    for (sourceSetObj in sourceSetsContainer) {
                        val sourceSet = ReflectiveAndroidSourceSet(sourceSetObj)
                        val name = sourceSet.name
                        val srcDirs = sourceSet.javaSrcDirs + sourceSet.kotlinSrcDirs

                        list.add(
                            SourceSetData(
                                name = name,
                                kind = "ANDROID_VARIANT",
                                production = !name.lowercase().contains("test"),
                                srcDirs = srcDirs.map { it.absolutePath },
                                platforms = listOf("android"),
                                compileClasspath = compilationClasspath(proj, name),
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                proj.logger.warn("Failed to extract Android source sets from project '${proj.path}': ${e.message}", e)
                // fall back to default logic
            }
        }

        if (list.isEmpty()) {
            val kotlinExt = proj.extensions.findByType(KotlinProjectExtension::class.java)
            if (kotlinExt != null) {
                val isKmp = proj.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")
                val kind = if (isKmp) "KMP" else "KOTLIN_JVM"

                // Extract compile platform targets for each source set if it is KMP
                val sourceSetPlatforms = mutableMapOf<String, MutableSet<String>>()
                if (isKmp) {
                    try {
                        val targetsContainer =
                            kotlinExt.callMethod(
                                "getTargets",
                            ) as? org.gradle.api.NamedDomainObjectCollection<*>
                        if (targetsContainer != null) {
                            for (targetObj in targetsContainer) {
                                val platformTypeObj = targetObj.callMethod("getPlatformType")
                                val platformName = platformTypeObj?.callMethod("getName") as? String ?: ""
                                val compilationsContainer =
                                    targetObj.callMethod(
                                        "getCompilations",
                                    ) as? org.gradle.api.NamedDomainObjectCollection<*>
                                if (compilationsContainer != null) {
                                    for (compilationObj in compilationsContainer) {
                                        val allKotlinSourceSets =
                                            compilationObj.callMethod(
                                                "getAllKotlinSourceSets",
                                            ) as? Set<*>
                                        if (allKotlinSourceSets != null) {
                                            for (ssObj in allKotlinSourceSets) {
                                                val ssName = ssObj?.callMethod("getName") as? String
                                                if (ssName != null && platformName.isNotEmpty()) {
                                                    sourceSetPlatforms
                                                        .getOrPut(
                                                            ssName,
                                                        ) { mutableSetOf() }
                                                        .add(platformName)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        proj.logger.warn("Failed to dynamically resolve Kotlin targets metadata in project '${proj.path}': ${e.message}", e)
                        // fall back if targets metadata cannot be resolved dynamically
                    }
                }

                for (sourceSet in kotlinExt.sourceSets) {
                    val name = sourceSet.name
                    // KMP source sets typically end in "Main" for production and "Test" for testing,
                    // while single-platform Java/Kotlin JVM modules use "main" and "test".
                    val isProduction =
                        name == "main" ||
                            (name.endsWith("Main", ignoreCase = true) && !name.lowercase().contains("test"))
                    val platforms =
                        if (isKmp) {
                            sourceSetPlatforms[name]?.toList() ?: emptyList()
                        } else {
                            listOf("jvm")
                        }
                    list.add(
                        SourceSetData(
                            name = name,
                            kind = kind,
                            production = isProduction,
                            srcDirs = sourceSet.kotlin.srcDirs.map { it.absolutePath },
                            platforms = platforms,
                            compileClasspath = compilationClasspath(proj, name),
                        ),
                    )
                }
            }
        }

        if (list.isEmpty()) {
            val javaSourceSets = proj.extensions.findByName("sourceSets") as? org.gradle.api.tasks.SourceSetContainer
            if (javaSourceSets != null) {
                for (ss in javaSourceSets) {
                    list.add(
                        SourceSetData(
                            name = ss.name,
                            kind = "KOTLIN_JVM",
                            production = ss.name == "main",
                            srcDirs = ss.allSource.srcDirs.map { it.absolutePath },
                            platforms = listOf("jvm"),
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
        val candidates = listOf("${sourceSetName}CompileClasspath", "compileClasspath")
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
                            depGroup.startsWith("io.github.baole")
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
            val androidExt = sub.extensions.findByName("android")
            if (androidExt != null) {
                try {
                    val sourceSetsContainer =
                        androidExt.callMethod(
                            "getSourceSets",
                        ) as? org.gradle.api.NamedDomainObjectContainer<*>
                    if (sourceSetsContainer != null) {
                        for (sourceSetObj in sourceSetsContainer) {
                            val sourceSet = ReflectiveAndroidSourceSet(sourceSetObj)
                            list.addAll(sourceSet.javaSrcDirs)
                            list.addAll(sourceSet.kotlinSrcDirs)
                        }
                    }
                } catch (e: Exception) {
                    sub.logger.warn("Failed to collect Android source directories for project '${sub.path}': ${e.message}", e)
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
                val javaSourceSets = sub.extensions.findByName("sourceSets") as? org.gradle.api.tasks.SourceSetContainer
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

    private fun setupConsumerLayout(project: Project) {
        if (project.configurations.findByName("archLayoutIncoming") != null) {
            return
        }
        val archLayoutIncoming =
            project.configurations.create("archLayoutIncoming") { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, "koarch-layout"),
                    )
                }
            }

        val archDepsIncoming =
            project.configurations.create("archDepsIncoming") { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
                config.attributes { attrs ->
                    attrs.attribute(
                        org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
                        project.objects.named(org.gradle.api.attributes.Usage::class.java, "koarch-deps"),
                    )
                }
            }

        // Add dependencies on the root project
        project.dependencies.add("archLayoutIncoming", project.dependencies.project(mapOf("path" to ":")))
        project.dependencies.add("archDepsIncoming", project.dependencies.project(mapOf("path" to ":")))

        // Copy only the matching v2 layout and clear layouts from previously checked-out branches.
        val cleanLayoutResources =
            project.tasks.register("cleanArchitectureLayoutResources", org.gradle.api.tasks.Delete::class.java) { delete ->
                delete.delete(
                    project.layout.buildDirectory.file("resources/test/konture/layout.json"),
                    project.layout.buildDirectory.file("resources/test/konture/layout_v2.json"),
                )
            }
        val copyLayoutTask =
            project.tasks.register("copyArchitectureLayout", org.gradle.api.tasks.Copy::class.java) { copy ->
                copy.from(archLayoutIncoming)
                copy.into(project.layout.buildDirectory.dir("resources/test/konture"))
                copy.rename { "layout_v2.json" }
                copy.dependsOn(cleanLayoutResources)
            }

        val copyDepsTask =
            project.tasks.register("copyArchitectureDeps", org.gradle.api.tasks.Copy::class.java) { copy ->
                copy.from(archDepsIncoming)
                copy.into(project.layout.buildDirectory.dir("resources/test/konture"))
                copy.rename { "dependencies.json" }
            }

        val cleanDependencyResource =
            project.tasks.register("cleanArchitectureDependencyResource", org.gradle.api.tasks.Delete::class.java) { delete ->
                delete.delete(project.layout.buildDirectory.file("resources/test/konture/dependencies.json"))
            }
        val rootDetector =
            project.rootProject.tasks.findByName("detectKontureExternalDependencyRules") as? DetectExternalDependencyRules
        rootDetector?.testSources?.from(project.fileTree("src") { pattern -> pattern.include("**/*.kt") })
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
            if (task.name == "processTestResources") {
                task.dependsOn(copyLayoutTask)
                task.dependsOn(cleanDependencyResource)
                task.dependsOn(copyDepsTask)
            }
        }
    }
}
