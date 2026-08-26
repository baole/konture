/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import io.github.baole.konture.plugin.KonturePlugin.Companion.KIND_ANDROID
import io.github.baole.konture.plugin.KonturePlugin.Companion.PLATFORM_ANDROID
import io.github.baole.konture.plugin.KonturePlugin.Companion.SUBSTRING_TEST_LOWERCASE
import org.gradle.api.Project
import java.io.File

internal object AgpInspector {
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

    fun collectAndroidSourceSets(
        proj: Project,
        list: MutableList<SourceSetData>,
    ) {
        val androidExt =
            proj.extensions.findByName("android")
                ?: proj.extensions.findByName("androidLibrary")
                ?: return
        val plugin = KonturePlugin()
        val sourceSets = (androidExt.invokeMethod("getSourceSets") as? Iterable<*>) ?: return
        for (sourceSet in sourceSets) {
            val data = parseAndroidSourceSet(proj, plugin, sourceSet) ?: continue
            list.add(data)
        }
    }

    private fun parseAndroidSourceSet(
        proj: Project,
        plugin: KonturePlugin,
        sourceSet: Any?,
    ): SourceSetData? {
        if (sourceSet == null) return null
        val name = sourceSet.invokeMethod("getName")?.toString() ?: return null
        val javaDirSet = sourceSet.invokeMethod("getJava")
        val kotlinDirSet = sourceSet.invokeMethod("getKotlin")
        val javaDirs =
            (javaDirSet?.invokeMethod("getDirectories") as? Iterable<*>)?.mapNotNull {
                it?.toString()
            } ?: emptyList()
        val kotlinDirs =
            (kotlinDirSet?.invokeMethod("getDirectories") as? Iterable<*>)?.mapNotNull {
                it?.toString()
            } ?: emptyList()
        val srcDirs = (javaDirs + kotlinDirs).distinct()

        return SourceSetData(
            name = name,
            kind = KIND_ANDROID,
            production = !name.lowercase().contains(SUBSTRING_TEST_LOWERCASE),
            srcDirs = srcDirs.map { plugin.toRelPath(proj, File(it)) },
            platforms = listOf(PLATFORM_ANDROID),
            compileClasspath = plugin.compilationClasspath(proj, name),
        )
    }

    fun collectAndroidSourceDirs(
        proj: Project,
        list: MutableList<File>,
    ) {
        val androidExt =
            proj.extensions.findByName("android")
                ?: proj.extensions.findByName("androidLibrary")
                ?: return
        val sourceSets = (androidExt.invokeMethod("getSourceSets") as? Iterable<*>)?.filterNotNull() ?: return
        for (sourceSet in sourceSets) {
            val javaDirSet = sourceSet.invokeMethod("getJava")
            val kotlinDirSet = sourceSet.invokeMethod("getKotlin")
            val javaDirs =
                (javaDirSet?.invokeMethod("getDirectories") as? Iterable<*>)?.mapNotNull {
                    it?.toString()
                } ?: emptyList()
            val kotlinDirs =
                (kotlinDirSet?.invokeMethod("getDirectories") as? Iterable<*>)?.mapNotNull {
                    it?.toString()
                } ?: emptyList()
            val srcDirs = (javaDirs + kotlinDirs).distinct()
            for (dirPath in srcDirs) {
                val f = File(dirPath)
                list.add(if (f.isAbsolute) f else File(proj.projectDir, f.path))
            }
        }
    }
}
