/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.buildlogic

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UpdateKotlinContributorsTest {
    @Test
    fun `throws comprehensive error when konture contributor name is missing`(
        @TempDir tempDir: File,
    ) {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val task = project.tasks.register("updateKotlinContributors", UpdateKotlinContributors::class.java).get()
        task.repositoryDirectory.set(tempDir)
        task.contributorPropertiesFile.set(tempDir.resolve("local.properties"))
        task.contributorSourceDirectories.set(listOf(tempDir.resolve("src").absolutePath))

        val exception =
            assertFailsWith<IllegalArgumentException> {
                task.updateHeaders()
            }

        val expectedPath = tempDir.resolve("local.properties").absolutePath
        assertTrue(exception.message!!.contains("Missing required key 'konture.contributor.name' in local.properties!"))
        assertTrue(exception.message!!.contains("Running 'spotlessApply' requires contributor attribution details"))
        assertTrue(exception.message!!.contains(expectedPath))
        assertTrue(exception.message!!.contains("konture.contributor.name=Your Name"))
        assertTrue(exception.message!!.contains("konture.contributor.github=@your-github-username"))
    }

    @Test
    fun `updates headers successfully when konture contributor name is defined`(
        @TempDir tempDir: File,
    ) {
        ProcessBuilder("git", "init").directory(tempDir).start().waitFor()

        val localProperties = tempDir.resolve("local.properties")
        localProperties.writeText(
            """
            konture.contributor.name=Jane Doe
            konture.contributor.github=jane-doe
            """.trimIndent(),
        )

        val srcDir = tempDir.resolve("src").apply { mkdirs() }
        val testKtFile = srcDir.resolve("Foo.kt")
        testKtFile.writeText("class Foo")

        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val task = project.tasks.register("updateKotlinContributors", UpdateKotlinContributors::class.java).get()
        task.repositoryDirectory.set(tempDir)
        task.contributorPropertiesFile.set(localProperties)
        task.contributorSourceDirectories.set(listOf(srcDir.absolutePath))

        task.updateHeaders()

        val updatedContent = testKtFile.readText()
        assertTrue(updatedContent.contains("Contributors: Jane Doe (@jane-doe)"))
        assertTrue(updatedContent.contains("SPDX-License-Identifier: Apache-2.0"))
        assertTrue(updatedContent.endsWith("class Foo"))
    }

    @Test
    fun `updates headers successfully for kts files when konture contributor name is defined`(
        @TempDir tempDir: File,
    ) {
        ProcessBuilder("git", "init").directory(tempDir).start().waitFor()

        val localProperties = tempDir.resolve("local.properties")
        localProperties.writeText(
            """
            konture.contributor.name=Jane Doe
            konture.contributor.github=jane-doe
            """.trimIndent(),
        )

        val srcDir = tempDir.resolve("src").apply { mkdirs() }
        val testKtsFile = srcDir.resolve("build.gradle.kts")
        testKtsFile.writeText("plugins { id(\"kotlin\") }")

        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val task = project.tasks.register("updateKotlinContributors", UpdateKotlinContributors::class.java).get()
        task.repositoryDirectory.set(tempDir)
        task.contributorPropertiesFile.set(localProperties)
        task.contributorSourceDirectories.set(listOf(srcDir.absolutePath))

        task.updateHeaders()

        val updatedContent = testKtsFile.readText()
        assertTrue(updatedContent.contains("Contributors: Jane Doe (@jane-doe)"))
        assertTrue(updatedContent.contains("SPDX-License-Identifier: Apache-2.0"))
        assertTrue(updatedContent.endsWith("plugins { id(\"kotlin\") }"))
    }
}
