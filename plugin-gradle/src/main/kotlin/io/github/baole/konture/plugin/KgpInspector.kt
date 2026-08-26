/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.plugin.KonturePlugin.Companion.COMPILATION_MAIN
import io.github.baole.konture.plugin.KonturePlugin.Companion.KIND_JVM
import io.github.baole.konture.plugin.KonturePlugin.Companion.KIND_KMP
import io.github.baole.konture.plugin.KonturePlugin.Companion.NAME_MAIN_LOWERCASE
import io.github.baole.konture.plugin.KonturePlugin.Companion.PLATFORM_JVM
import io.github.baole.konture.plugin.KonturePlugin.Companion.SUBSTRING_TEST_LOWERCASE
import io.github.baole.konture.plugin.KonturePlugin.Companion.SUFFIX_MAIN
import org.gradle.api.Project
import java.io.File

internal object KgpInspector {
    private val CONFIG_METHOD_NAMES =
        listOf(
            "getApiConfigurationName",
            "getImplementationConfigurationName",
            "getCompileOnlyConfigurationName",
            "getRuntimeOnlyConfigurationName",
            "getCompileDependencyConfigurationName",
            "getRuntimeDependencyConfigurationName",
        )

    private fun Any.invokeMethod(
        methodName: String,
        vararg args: Any?,
    ): Any? {
        val method = javaClass.methods.find { it.name == methodName && it.parameterCount == args.size } ?: return null
        return try {
            method.invoke(this, *args)
        } catch (_: Throwable) {
            null
        }
    }

    private data class KmpMetadata(
        val sourceSetPlatforms: MutableMap<String, MutableSet<String>> = mutableMapOf(),
        val sourceSetTargets: MutableMap<String, MutableSet<String>> = mutableMapOf(),
        val sourceSetVisibility: MutableMap<String, MutableSet<String>> = mutableMapOf(),
        val sourceSetDependencyConfigurations: MutableMap<String, MutableSet<String>> = mutableMapOf(),
        val mainCompilationSourceSets: MutableSet<String> = mutableSetOf(),
    )

    fun collectKotlinSourceSets(
        proj: Project,
        list: MutableList<SourceSetData>,
    ) {
        val kotlinExt = proj.extensions.findByName("kotlin") ?: return
        val kmpExtension = if (kotlinExt.javaClass.name.contains("Multiplatform")) kotlinExt else null
        val isKmp = kmpExtension != null
        val kind = if (isKmp) KIND_KMP else KIND_JVM

        val metadata = extractKmpMetadata(kmpExtension)

        val sourceSets = (kotlinExt.invokeMethod("getSourceSets") as? Iterable<*>)?.filterNotNull() ?: return
        val sourceSetNames = sourceSets.mapNotNull { it.invokeMethod("getName")?.toString() }

        associateConfigurationsToSourceSets(proj, sourceSetNames, metadata.sourceSetDependencyConfigurations)

        for (sourceSet in sourceSets) {
            val data = createSourceSetData(proj, sourceSet, isKmp, kind, metadata) ?: continue
            list.add(data)
        }
    }

    private fun extractKmpMetadata(kmpExtension: Any?): KmpMetadata {
        val metadata = KmpMetadata()
        if (kmpExtension == null) return metadata
        try {
            val targets = (kmpExtension.invokeMethod("getTargets") as? Iterable<*>)?.filterNotNull() ?: return metadata
            for (target in targets) {
                processKmpTarget(target, metadata)
            }
        } catch (_: Throwable) {
        }
        return metadata
    }

    private fun processKmpTarget(
        target: Any,
        metadata: KmpMetadata,
    ) {
        val platformType = target.invokeMethod("getPlatformType")
        val targetPlatforms = listOfNotNull(platformType?.invokeMethod("getName")?.toString())

        val nativeTargetIdentity =
            if (target.javaClass.name.contains("Native")) {
                try {
                    val konanTarget = target.invokeMethod("getKonanTarget")
                    konanTarget?.invokeMethod("getName")?.toString()
                } catch (_: Throwable) {
                    null
                }
            } else {
                null
            }

        val compilations = (target.invokeMethod("getCompilations") as? Iterable<*>)?.filterNotNull() ?: return
        for (compilation in compilations) {
            processKmpCompilation(compilation, targetPlatforms, nativeTargetIdentity, metadata)
        }
    }

    private fun processKmpCompilation(
        compilation: Any,
        targetPlatforms: List<String>,
        nativeTargetIdentity: String?,
        metadata: KmpMetadata,
    ) {
        val compilationSourceSets = compilation.invokeMethod("getAllKotlinSourceSets") as? Iterable<*>
        val ssNames = compilationSourceSets?.mapNotNull { it?.invokeMethod("getName")?.toString() } ?: emptyList()

        compilationSourceSets?.filterNotNull()?.forEach { sourceSet ->
            val ssName = sourceSet.invokeMethod("getName")?.toString() ?: return@forEach
            metadata.sourceSetPlatforms.getOrPut(ssName) { mutableSetOf() }.addAll(targetPlatforms)
            if (nativeTargetIdentity != null) {
                metadata.sourceSetTargets.getOrPut(ssName) { mutableSetOf() }.add(nativeTargetIdentity)
            }
        }

        val defaultSourceSet = compilation.invokeMethod("getDefaultSourceSet")
        val defaultSourceSetName = defaultSourceSet?.invokeMethod("getName")?.toString() ?: ""

        if (defaultSourceSetName.isNotEmpty()) {
            metadata.sourceSetVisibility
                .getOrPut(defaultSourceSetName) { mutableSetOf() }
                .addAll(ssNames.filterNot { it == defaultSourceSetName })

            val configNames =
                CONFIG_METHOD_NAMES.mapNotNull { methodName ->
                    compilation.invokeMethod(methodName)?.toString()
                }
            metadata.sourceSetDependencyConfigurations
                .getOrPut(defaultSourceSetName) { mutableSetOf() }
                .addAll(configNames)
        }

        val compName = compilation.invokeMethod("getName")?.toString() ?: ""
        if (compName.equals(COMPILATION_MAIN, ignoreCase = true)) {
            metadata.mainCompilationSourceSets.addAll(ssNames)
        }
    }

    private fun associateConfigurationsToSourceSets(
        proj: Project,
        sourceSetNames: List<String>,
        sourceSetDependencyConfigurations: MutableMap<String, MutableSet<String>>,
    ) {
        proj.configurations.forEach { configuration ->
            val owner =
                sourceSetNames
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
    }

    private fun createSourceSetData(
        proj: Project,
        sourceSet: Any,
        isKmp: Boolean,
        kind: String,
        metadata: KmpMetadata,
    ): SourceSetData? {
        val name = sourceSet.invokeMethod("getName")?.toString() ?: return null
        val isProduction =
            if (isKmp) {
                name in metadata.mainCompilationSourceSets
            } else {
                name == NAME_MAIN_LOWERCASE ||
                    (
                        name.endsWith(SUFFIX_MAIN, ignoreCase = true) &&
                            !name.lowercase().contains(SUBSTRING_TEST_LOWERCASE)
                    )
            }
        val platforms =
            if (isKmp) {
                metadata.sourceSetPlatforms[name]?.toList() ?: emptyList()
            } else {
                listOf(PLATFORM_JVM)
            }

        val srcDirs =
            try {
                val kotlinDirSet = sourceSet.invokeMethod("getKotlin")
                (kotlinDirSet?.invokeMethod("getSrcDirs") as? Iterable<*>)?.mapNotNull { it as? File } ?: emptyList()
            } catch (_: Throwable) {
                emptyList()
            }

        val dependsOnNames =
            try {
                val dependsOnSet = sourceSet.invokeMethod("getDependsOn") as? Iterable<*>
                dependsOnSet?.mapNotNull { it?.invokeMethod("getName")?.toString() } ?: emptyList()
            } catch (_: Throwable) {
                emptyList()
            }

        return SourceSetData(
            name = name,
            kind = kind,
            production = isProduction,
            srcDirs = srcDirs.map { KonturePluginConfigurer.toRelPath(proj, it) },
            platforms = platforms,
            targetNames = if (isKmp) metadata.sourceSetTargets[name]?.toList() ?: emptyList() else emptyList(),
            dependsOnSourceSets =
                if (isKmp) {
                    metadata.sourceSetVisibility[name]?.toList() ?: dependsOnNames
                } else {
                    emptyList()
                },
            dependencyConfigurations =
                if (isKmp) metadata.sourceSetDependencyConfigurations[name]?.toList() ?: emptyList() else emptyList(),
            compileClasspath = KonturePluginConfigurer.compilationClasspath(proj, name),
        )
    }

    fun collectKotlinSourceDirs(
        proj: Project,
        list: MutableList<File>,
    ) {
        val kotlinExt = proj.extensions.findByName("kotlin") ?: return
        try {
            val sourceSets = (kotlinExt.invokeMethod("getSourceSets") as? Iterable<*>)?.filterNotNull() ?: return
            for (sourceSet in sourceSets) {
                val kotlinDirSet = sourceSet.invokeMethod("getKotlin")
                val srcDirs = (kotlinDirSet?.invokeMethod("getSrcDirs") as? Iterable<*>) ?: continue
                for (dir in srcDirs) {
                    if (dir is File) {
                        list.add(if (dir.isAbsolute) dir else File(proj.projectDir, dir.path))
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }
}
