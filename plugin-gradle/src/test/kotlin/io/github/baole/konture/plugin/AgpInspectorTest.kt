/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class AgpInspectorTest {
    class DummyDirSet(private val directories: List<File>) {
        fun getDirectories(): List<File> = directories
    }

    class DummySourceSet(
        private val name: String,
        private val javaDirs: List<File>,
        private val kotlinDirs: List<File>,
    ) {
        fun getName(): String = name

        fun getJava(): DummyDirSet = DummyDirSet(javaDirs)

        fun getKotlin(): DummyDirSet = DummyDirSet(kotlinDirs)
    }

    class DummyAndroidExtension(private val sourceSets: List<DummySourceSet>) {
        fun getSourceSets(): List<DummySourceSet> = sourceSets
    }

    @Test
    fun `test collectAndroidSourceSets and collectAndroidSourceDirs`() {
        val project = ProjectBuilder.builder().build()
        val javaDir = File(project.projectDir, "src/main/java")
        val kotlinDir = File(project.projectDir, "src/main/kotlin")
        javaDir.mkdirs()
        kotlinDir.mkdirs()

        val mainSourceSet = DummySourceSet("main", listOf(javaDir), listOf(kotlinDir))
        val testSourceSet = DummySourceSet("test", listOf(javaDir), listOf())
        val dummyAndroid = DummyAndroidExtension(listOf(mainSourceSet, testSourceSet))

        project.extensions.add("android", dummyAndroid)

        val sourceSetDataList = mutableListOf<SourceSetData>()
        AgpInspector.collectAndroidSourceSets(project, sourceSetDataList)

        assertEquals(2, sourceSetDataList.size)
        val mainData = sourceSetDataList.first { it.name == "main" }
        assertTrue(mainData.production)
        val testData = sourceSetDataList.first { it.name == "test" }
        assertTrue(!testData.production)

        val sourceDirs = mutableListOf<File>()
        AgpInspector.collectAndroidSourceDirs(project, sourceDirs)
        assertTrue(sourceDirs.contains(javaDir))
        assertTrue(sourceDirs.contains(kotlinDir))
    }
}
