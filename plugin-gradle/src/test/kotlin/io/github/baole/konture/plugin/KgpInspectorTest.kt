/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class KgpInspectorTest {
    @Test
    fun `test collectKotlinSourceDirs with kotlin jvm plugin`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        val srcDir = File(project.projectDir, "src/main/kotlin")
        srcDir.mkdirs()

        val sourceDirs = mutableListOf<File>()
        KgpInspector.collectKotlinSourceDirs(project, sourceDirs)

        assertTrue(sourceDirs.contains(srcDir.canonicalFile) || sourceDirs.contains(srcDir))
    }

    @Test
    fun `test collectKotlinSourceSets with kotlin jvm plugin`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        val list = mutableListOf<SourceSetData>()
        KgpInspector.collectKotlinSourceSets(project, list)

        assertTrue(list.isNotEmpty())
        val mainSs = list.find { it.name == "main" }
        assertTrue(mainSs != null)
        assertTrue(mainSs?.production == true)
    }
}
