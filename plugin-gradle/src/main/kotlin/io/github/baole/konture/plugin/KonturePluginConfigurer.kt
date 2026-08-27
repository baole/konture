/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("TooGenericExceptionCaught", "NestedBlockDepth", "SwallowedException")

package io.github.baole.konture.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import java.io.File

internal object KonturePluginConfigurer {
    fun setupConsumerLayout(project: Project) {
        if (project.configurations.findByName(CONFIG_LAYOUT_INCOMING) != null) {
            return
        }
        val archLayoutIncoming =
            project.configurations.create(CONFIG_LAYOUT_INCOMING) { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
                config.attributes { attrs ->
                    attrs.attribute(
                        Usage.USAGE_ATTRIBUTE,
                        project.objects.named(Usage::class.java, USAGE_LAYOUT),
                    )
                }
            }

        val archDepsIncoming =
            project.configurations.create(CONFIG_DEPS_INCOMING) { config ->
                config.isCanBeConsumed = false
                config.isCanBeResolved = true
                config.attributes { attrs ->
                    attrs.attribute(
                        Usage.USAGE_ATTRIBUTE,
                        project.objects.named(Usage::class.java, USAGE_DEPS),
                    )
                }
            }

        val layoutProj = project.dependencies.project(mapOf("path" to ":"))
        val depsProj = project.dependencies.project(mapOf("path" to ":"))
        project.dependencies.add(CONFIG_LAYOUT_INCOMING, layoutProj)
        project.dependencies.add(CONFIG_DEPS_INCOMING, depsProj)

        val cleanLayoutResources =
            project.tasks.register(TASK_CLEAN_LAYOUT_RESOURCES, Delete::class.java) { delete ->
                delete.delete(
                    project.layout.buildDirectory.file("$PATH_RESOURCES_TEST_KONTURE/$FILE_LAYOUT"),
                    project.layout.buildDirectory.file("$PATH_RESOURCES_TEST_KONTURE/$FILE_LAYOUT_V2"),
                )
            }
        val copyLayoutTask =
            project.tasks.register(TASK_COPY_LAYOUT, Copy::class.java) { copy ->
                copy.from(archLayoutIncoming)
                copy.into(project.layout.buildDirectory.dir(PATH_RESOURCES_TEST_KONTURE))
                copy.rename { FILE_LAYOUT_V2 }
                copy.dependsOn(cleanLayoutResources)
            }

        val copyDepsTask =
            project.tasks.register(TASK_COPY_DEPS, Copy::class.java) { copy ->
                copy.from(archDepsIncoming)
                copy.into(project.layout.buildDirectory.dir(PATH_RESOURCES_TEST_KONTURE))
                copy.rename { FILE_DEPENDENCIES }
            }

        val cleanDependencyResource =
            project.tasks.register(TASK_CLEAN_DEPS_RESOURCE, Delete::class.java) { delete ->
                delete.delete(project.layout.buildDirectory.file("$PATH_RESOURCES_TEST_KONTURE/$FILE_DEPENDENCIES"))
            }
        copyDepsTask.configure { task ->
            task.dependsOn(cleanDependencyResource)
            task.dependsOn(":$TASK_DETECT_RULES")
            val detectorOutputFile = project.rootProject.layout.buildDirectory.file(PATH_EXTERNAL_RULES)
            task.inputs.file(detectorOutputFile)
            task.onlyIf {
                val f = detectorOutputFile.get().asFile
                f.exists() && f.readText().trim().toBoolean()
            }
        }

        project.tasks.configureEach { task ->
            if (isTestTask(task)) {
                task.dependsOn(copyLayoutTask)
                task.dependsOn(cleanDependencyResource)
                task.dependsOn(copyDepsTask)
            }
        }
    }

    private fun isTestTask(task: org.gradle.api.Task): Boolean {
        if (task is Test) return true
        val testTaskNames =
            setOf(
                TASK_PROCESS_TEST_RESOURCES,
                "compileTestKotlin",
                "compileTestJava",
                "testClasses",
            )
        return task.name in testTaskNames
    }

    fun collectModuleDataForProject(proj: Project): ModuleData {
        val plugins = mutableListOf<String>()
        if (proj.pluginManager.hasPlugin(ID_KOTLIN_JVM)) plugins.add(PLUGIN_KOTLIN_JVM)
        if (proj.pluginManager.hasPlugin(ID_ANDROID_APPLICATION)) plugins.add(PLUGIN_ANDROID_APP)
        if (proj.pluginManager.hasPlugin(ID_ANDROID_LIBRARY)) plugins.add(PLUGIN_ANDROID_LIB)
        if (proj.pluginManager.hasPlugin(ID_ANDROID_DYNAMIC_FEATURE) ||
            proj.plugins.any { it.javaClass.simpleName.contains("DynamicFeature") }
        ) {
            plugins.add(PLUGIN_ANDROID_FEATURE)
        }
        if (proj.pluginManager.hasPlugin(ID_ANDROID_TEST) ||
            proj.plugins.any {
                it.javaClass.simpleName.contains("TestPlugin") ||
                    it.javaClass.simpleName.contains("AndroidTest")
            }
        ) {
            plugins.add(PLUGIN_ANDROID_TEST)
        }
        if (proj.pluginManager.hasPlugin(ID_ANDROID_KOTLIN_MULTIPLATFORM_LIBRARY) ||
            proj.plugins.any { it.javaClass.simpleName.contains("AndroidKotlinMultiplatformLibrary") } ||
            proj.extensions.findByName("androidLibrary") != null
        ) {
            plugins.add(PLUGIN_ANDROID_KMP_LIB)
        }
        if (proj.pluginManager.hasPlugin(ID_KOTLIN_MULTIPLATFORM)) plugins.add(PLUGIN_KOTLIN_KMP)

        val sourceSets = collectSourceSets(proj)
        val dependencies = collectDependencies(proj)

        return ModuleData(
            path = proj.path,
            projectDir =
                if (proj.projectDir == proj.rootDir) {
                    "."
                } else {
                    proj.projectDir.relativeTo(proj.rootDir).path
                },
            appliedPlugins = plugins,
            sourceSets = sourceSets,
            dependencies = dependencies,
        )
    }

    fun collectSourceSets(proj: Project): List<SourceSetData> {
        val list = mutableListOf<SourceSetData>()
        val isKmp =
            proj.pluginManager.hasPlugin(ID_KOTLIN_MULTIPLATFORM) ||
                proj.extensions.findByName("kotlin")?.javaClass?.name?.contains("Multiplatform") == true

        if (isKmp) {
            KgpInspector.collectKotlinSourceSets(proj, list)
            if (proj.hasAndroidPlugin()) {
                val androidList = mutableListOf<SourceSetData>()
                AgpInspector.collectAndroidSourceSets(proj, androidList)
                val existingNames = list.map { it.name }.toSet()
                list.addAll(androidList.filter { it.name !in existingNames })
            }
        } else {
            if (proj.hasAndroidPlugin()) {
                AgpInspector.collectAndroidSourceSets(proj, list)
            }
            if (list.isEmpty() && proj.hasKotlinPlugin()) {
                KgpInspector.collectKotlinSourceSets(proj, list)
            }
        }

        if (list.isEmpty()) {
            val javaSourceSets = proj.extensions.findByName(EXTENSION_SOURCE_SETS) as? SourceSetContainer
            if (javaSourceSets != null) {
                for (ss in javaSourceSets) {
                    list.add(
                        SourceSetData(
                            name = ss.name,
                            kind = KIND_JVM,
                            production = ss.name == NAME_MAIN_LOWERCASE,
                            srcDirs = ss.allSource.srcDirs.map { toRelPath(proj, it) },
                            platforms = listOf(PLATFORM_JVM),
                            compileClasspath = compilationClasspath(proj, ss.name),
                        ),
                    )
                }
            }
        }
        return list
    }

    fun toRelPath(
        proj: Project,
        file: File,
    ): String {
        val rootDir = proj.rootDir.canonicalFile
        val absFile = if (file.isAbsolute) file else File(proj.projectDir, file.path)
        val canonical = absFile.canonicalFile
        return if (canonical.startsWith(rootDir)) {
            canonical.relativeTo(rootDir).path
        } else {
            canonical.path
        }
    }

    fun compilationClasspath(
        project: Project,
        sourceSetName: String,
    ): List<String> {
        val candidates = listOf("$sourceSetName$SUFFIX_COMPILE_CLASSPATH_CAMEL", CONFIG_COMPILE_CLASSPATH_LOWER)
        val configuration = candidates.firstNotNullOfOrNull { project.configurations.findByName(it) } ?: return emptyList()
        if (!configuration.isCanBeResolved) return emptyList()
        return try {
            configuration.resolve().map { toRelPath(project, it) }.sorted()
        } catch (exception: Exception) {
            project.logger.info("Konture could not resolve compiler classpath for $sourceSetName: ${exception.message}")
            emptyList()
        }
    }

    fun collectDependencies(proj: Project): List<DependencyData> {
        val deps = mutableListOf<DependencyData>()
        proj.configurations.forEach { config ->
            if (config.name == CONFIG_LAYOUT_INCOMING || config.name == CONFIG_DEPS_INCOMING) return@forEach
            config.dependencies.forEach { dep ->
                if (dep is ProjectDependency) {
                    deps.add(
                        DependencyData(
                            configuration = config.name,
                            targetBuildId = ":",
                            targetPath = dep.path,
                        ),
                    )
                }
            }
        }
        return deps
    }

    fun collectAllSourceDirs(proj: Project): List<File> {
        val list = mutableListOf<File>()
        if (proj.hasAndroidPlugin()) {
            AgpInspector.collectAndroidSourceDirs(proj, list)
        }
        if (proj.hasKotlinPlugin()) {
            KgpInspector.collectKotlinSourceDirs(proj, list)
        }
        if (list.isEmpty()) {
            val javaSourceSets = proj.extensions.findByName(EXTENSION_SOURCE_SETS) as? SourceSetContainer
            if (javaSourceSets != null) {
                for (ss in javaSourceSets) {
                    for (dir in ss.allSource.srcDirs) {
                        list.add(if (dir.isAbsolute) dir else File(proj.projectDir, dir.path))
                    }
                }
            }
        }
        proj.rootProject.subprojects.forEach { sub ->
            val srcDir = File(sub.projectDir, DIR_SRC)
            if (srcDir.exists()) {
                srcDir.walkTopDown().filter { it.isDirectory && (it.name == "kotlin" || it.name == "java") }.forEach {
                    list.add(it)
                }
            }
        }
        val buildDir =
            try {
                proj.layout.buildDirectory
                    .get()
                    .asFile.canonicalFile
            } catch (_: Exception) {
                null
            }
        return if (buildDir != null) {
            list.filter { dir ->
                try {
                    val canonicalDir = dir.canonicalFile
                    !canonicalDir.startsWith(buildDir)
                } catch (_: Exception) {
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
            pluginManager.hasPlugin(ID_ANDROID_DYNAMIC_FEATURE) ||
            pluginManager.hasPlugin(ID_ANDROID_KOTLIN_MULTIPLATFORM_LIBRARY) ||
            extensions.findByName("android") != null ||
            extensions.findByName("androidLibrary") != null

    private fun Project.hasKotlinPlugin(): Boolean =
        pluginManager.hasPlugin(ID_KOTLIN_JVM) ||
            pluginManager.hasPlugin(ID_KOTLIN_MULTIPLATFORM) ||
            pluginManager.hasPlugin("org.jetbrains.kotlin.android") ||
            pluginManager.hasPlugin("kotlin") ||
            extensions.findByName("kotlin") != null

    private const val EXTENSION_SOURCE_SETS = "sourceSets"

    private const val TASK_DETECT_RULES = "detectKontureExternalDependencyRules"
    private const val TASK_COPY_LAYOUT = "copyArchitectureLayout"
    private const val TASK_COPY_DEPS = "copyArchitectureDeps"
    private const val TASK_CLEAN_LAYOUT_RESOURCES = "cleanArchitectureLayoutResources"
    private const val TASK_CLEAN_DEPS_RESOURCE = "cleanArchitectureDependencyResource"
    private const val TASK_PROCESS_TEST_RESOURCES = "processTestResources"

    private const val CONFIG_LAYOUT_INCOMING = "archLayoutIncoming"
    private const val CONFIG_DEPS_INCOMING = "archDepsIncoming"

    private const val USAGE_LAYOUT = "koarch-layout"
    private const val USAGE_DEPS = "koarch-deps"

    private const val PATH_EXTERNAL_RULES = "konture/external-dependency-rules.txt"
    private const val PATH_RESOURCES_TEST_KONTURE = "resources/test/konture"

    private const val FILE_LAYOUT = "layout.json"
    private const val FILE_LAYOUT_V2 = "layout_v2.json"
    private const val FILE_DEPENDENCIES = "dependencies.json"

    private const val PLUGIN_KOTLIN_JVM = "kotlin-jvm"
    private const val PLUGIN_ANDROID_APP = "android-application"
    private const val PLUGIN_ANDROID_LIB = "android-library"
    private const val PLUGIN_ANDROID_FEATURE = "android-dynamic-feature"
    private const val PLUGIN_ANDROID_TEST = "android-test"
    private const val PLUGIN_ANDROID_KMP_LIB = "android-kmp-library"
    private const val PLUGIN_KOTLIN_KMP = "kotlin-multiplatform"

    private const val ID_KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
    private const val ID_ANDROID_APPLICATION = "com.android.application"
    private const val ID_ANDROID_LIBRARY = "com.android.library"
    private const val ID_ANDROID_DYNAMIC_FEATURE = "com.android.dynamic-feature"
    private const val ID_ANDROID_TEST = "com.android.test"
    private const val ID_ANDROID_KOTLIN_MULTIPLATFORM_LIBRARY = "com.android.kotlin.multiplatform.library"
    private const val ID_KOTLIN_MULTIPLATFORM = "org.jetbrains.kotlin.multiplatform"

    internal const val KIND_JVM = "KOTLIN_JVM"
    internal const val PLATFORM_JVM = "jvm"

    internal const val NAME_MAIN_LOWERCASE = "main"

    private const val SUFFIX_COMPILE_CLASSPATH_CAMEL = "CompileClasspath"
    private const val CONFIG_COMPILE_CLASSPATH_LOWER = "compileClasspath"

    private const val DIR_SRC = "src"
}
