/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.time.Year
import java.util.Properties

@DisableCachingByDefault(because = "The task updates source files from the current Git worktree.")
abstract class UpdateKotlinContributors : DefaultTask() {
    @get:Internal
    abstract val repositoryDirectory: DirectoryProperty

    @get:Internal
    abstract val contributorPropertiesFile: RegularFileProperty

    @get:Internal
    abstract val contributorSourceDirectories: ListProperty<String>

    @TaskAction
    fun updateHeaders() {
        val rootDirectory = repositoryDirectory.get().asFile
        val propertiesFile = contributorPropertiesFile.get().asFile
        val properties =
            Properties().apply {
                if (propertiesFile.isFile) {
                    propertiesFile.inputStream().use(::load)
                }
            }
        val contributorName =
            properties
                .getProperty("konture.contributor.name")
                ?.trim()
                ?.takeIf(String::isNotEmpty)
        val contributorGitHub =
            properties
                .getProperty("konture.contributor.github")
                ?.trim()
                ?.removePrefix("@")
                ?.takeIf(String::isNotEmpty)

        if (contributorName == null) {
            val localPropsPath = propertiesFile.absolutePath
            val message =
                """
                |Missing required key 'konture.contributor.name' in local.properties!
                |
                |Running 'spotlessApply' requires contributor attribution details to update source file copyright headers.
                |
                |Please create or edit 'local.properties' in the root project directory:
                |  File: $localPropsPath
                |
                |Add the following properties:
                |  konture.contributor.name=Your Name
                |  konture.contributor.github=@your-github-username
                |
                |Note: 'local.properties' is gitignored and will stay local to your environment.
                """.trimMargin()
            throw IllegalArgumentException(message)
        }

        fun gitPaths(vararg arguments: String): Set<File> {
            val process =
                ProcessBuilder(listOf("git") + arguments)
                    .directory(rootDirectory)
                    .redirectErrorStream(true)
                    .start()
            val output = process.inputStream.readBytes().toString(Charsets.UTF_8)
            if (process.waitFor() != 0) return emptySet()
            return output
                .split('\u0000')
                .filter(String::isNotBlank)
                .map(rootDirectory::resolve)
                .toSet()
        }

        val changedFiles =
            gitPaths("diff", "--name-only", "-z", "--diff-filter=ACMR", "HEAD", "--", "*.kt", "*.kts") +
                gitPaths("ls-files", "-z", "--others", "--exclude-standard", "--", "*.kt", "*.kts")
        val sourceDirectories = contributorSourceDirectories.get().map(::File)
        val changedSourceFiles =
            changedFiles.filter { file ->
                sourceDirectories.any { sourceDirectory ->
                    file.toPath().startsWith(sourceDirectory.toPath())
                }
            }
        val headerPattern =
            Regex(
                """\A/\*\R \* Copyright (?<year>\d{4}(?:-\d{4})?) (?<owner>.+)\R(?: \* Contributors: (?<contributors>.+)\R)? \* SPDX-License-Identifier: Apache-2\.0\R \*/\R\R""",
            )
        val sourceFilesWithoutHeaders =
            sourceDirectories.flatMap { sourceDirectory ->
                sourceDirectory
                    .walkTopDown()
                    .filter { file -> file.isFile && (file.extension == "kt" || file.extension == "kts") }
                    .filter { file -> !headerPattern.containsMatchIn(file.readText()) }
                    .toList()
            }
        val filesToUpdate = (changedSourceFiles + sourceFilesWithoutHeaders).distinct()
        if (filesToUpdate.isEmpty()) {
            return
        }
        val contributor =
            listOfNotNull(
                contributorName,
                contributorGitHub?.let { "(@$it)" },
            ).joinToString(" ")
        filesToUpdate.filter(File::isFile).forEach { file ->
            val original = file.readText()
            val match = headerPattern.find(original)
            val year = match?.groups?.get("year")?.value ?: Year.now().value.toString()
            val owner = match?.groups?.get("owner")?.value
            val existingContributors =
                match
                    ?.groups
                    ?.get("contributors")
                    ?.value
                    ?.split(", ")
                    .orEmpty()
            val contributorsByName = linkedMapOf<String, String>()
            (listOfNotNull(owner?.takeUnless { it == "The Konture Contributors" }) + existingContributors + contributor)
                .forEach { entry ->
                    contributorsByName[entry.substringBefore(" (@").trim().lowercase()] = entry
                }
            val contributors = contributorsByName.values
            val header =
                buildString {
                    appendLine("/*")
                    appendLine(" * Copyright $year The Konture Contributors")
                    appendLine(" * Contributors: ${contributors.joinToString(", ")}")
                    appendLine(" * SPDX-License-Identifier: Apache-2.0")
                    appendLine(" */")
                    appendLine()
                }
            val updated = if (match == null) header + original else original.replaceRange(match.range, header)
            if (updated != original) {
                file.writeText(updated)
            }
        }
    }
}
