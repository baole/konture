/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.report

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal object ReportFileUtil {
    @Suppress("TooGenericExceptionCaught")
    fun writeAtomically(
        targetFile: File,
        content: String,
    ) {
        try {
            targetFile.parentFile?.mkdirs()
            val parent = targetFile.parentFile ?: File(".")
            val tempFile = File.createTempFile("konture-report-", ".tmp", parent)
            try {
                tempFile.writeText(content)
                try {
                    Files.move(
                        tempFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                } catch (_: Exception) {
                    Files.move(
                        tempFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                }
            } finally {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        } catch (_: Exception) {
            // Ignore file write exceptions in restricted environments
        }
    }
}
