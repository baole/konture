/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import java.io.File
import java.net.URI

internal object HtmlReportProjectSignatureResolver {
    private const val ORIGIN_REMOTE = "origin"
    private val remoteSectionRegex = Regex("""\[remote\s+\"([^\"]+)\"\]""")
    private val remoteUrlRegex = Regex("""url\s*=\s*(.+)""")

    @Suppress("TooGenericExceptionCaught")
    fun resolve(
        projectRoot: File?,
        targetFile: File,
    ): String? {
        val candidateRoots =
            listOfNotNull(projectRoot, targetFile.absoluteFile.parentFile, File(System.getProperty("user.dir")))

        for (candidateRoot in candidateRoots) {
            val gitConfig = findGitConfig(candidateRoot) ?: continue
            val remoteUrl = readPreferredRemoteUrl(gitConfig) ?: continue
            return normalizeRemoteUrl(remoteUrl)
        }

        return null
    }

    private fun findGitConfig(startDir: File): File? {
        return generateSequence(startDir.absoluteFile) { it.parentFile }
            .mapNotNull { directory -> resolveGitConfig(File(directory, ".git")) }
            .firstOrNull()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun resolveGitConfig(gitMarker: File): File? {
        return try {
            when {
                gitMarker.isDirectory -> File(gitMarker, "config").takeIf(File::isFile)
                gitMarker.isFile -> resolveGitFileConfig(gitMarker)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveGitFileConfig(gitFile: File): File? {
        val gitDirLine =
            gitFile
                .readLines()
                .asSequence()
                .map(String::trim)
                .firstOrNull { it.startsWith("gitdir:", ignoreCase = true) }
                ?: return null
        val gitDirPath = gitDirLine.substringAfter(':').trim().takeIf { it.isNotEmpty() } ?: return null
        val gitDir = File(gitDirPath).let { if (it.isAbsolute) it else File(gitFile.parentFile, gitDirPath) }
        return File(gitDir, "config").takeIf(File::isFile)
    }

    private fun readPreferredRemoteUrl(configFile: File): String? {
        var currentRemoteName: String? = null
        var originRemoteUrl: String? = null
        var fallbackRemoteUrl: String? = null

        configFile.forEachLine { line ->
            val trimmedLine = line.trim()
            currentRemoteName = remoteSectionRegex.matchEntire(trimmedLine)?.groupValues?.getOrNull(1) ?: currentRemoteName

            val remoteUrl = remoteUrlRegex.matchEntire(trimmedLine)?.groupValues?.getOrNull(1)
            if (remoteUrl != null && currentRemoteName != null) {
                val normalizedRemoteUrl = normalizeRemoteUrl(remoteUrl)
                if (currentRemoteName == ORIGIN_REMOTE) {
                    originRemoteUrl = normalizedRemoteUrl
                }
                if (fallbackRemoteUrl == null) {
                    fallbackRemoteUrl = normalizedRemoteUrl
                }
            }
            if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]") && remoteSectionRegex.matchEntire(trimmedLine) == null) {
                currentRemoteName = null
            }
        }

        return originRemoteUrl ?: fallbackRemoteUrl
    }

    private fun normalizeRemoteUrl(remoteUrl: String): String {
        val trimmedRemoteUrl = remoteUrl.trim()
        return when {
            trimmedRemoteUrl.startsWith("git@") -> normalizeScpStyleRemote(trimmedRemoteUrl)
            trimmedRemoteUrl.startsWith("ssh://") ||
                trimmedRemoteUrl.startsWith("git://") ||
                trimmedRemoteUrl.startsWith("http://") ||
                trimmedRemoteUrl.startsWith("https://") -> normalizeUriStyleRemote(trimmedRemoteUrl)
            else -> trimmedRemoteUrl.removeSuffix(".git").trimEnd('/')
        }
    }

    private fun normalizeScpStyleRemote(remoteUrl: String): String {
        val withoutUser = remoteUrl.substringAfter('@')
        val host = withoutUser.substringBefore(':').ifBlank { return remoteUrl.removeSuffix(".git") }
        val path = withoutUser.substringAfter(':', missingDelimiterValue = "").removePrefix("/").removeSuffix(".git").trimEnd('/')
        return if (path.isBlank()) {
            "https://$host"
        } else {
            "https://$host/$path"
        }
    }

    private fun normalizeUriStyleRemote(remoteUrl: String): String {
        return try {
            val uri = URI(remoteUrl)
            val host = uri.host ?: return remoteUrl.removeSuffix(".git").trimEnd('/')
            val path = uri.path.orEmpty().removePrefix("/").removeSuffix(".git").trimEnd('/')
            if (path.isBlank()) {
                "https://$host"
            } else {
                "https://$host/$path"
            }
        } catch (_: IllegalArgumentException) {
            remoteUrl.removeSuffix(".git").trimEnd('/')
        }
    }
}


