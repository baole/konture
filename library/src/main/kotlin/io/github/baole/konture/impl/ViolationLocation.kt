/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

/**
 * Builds the uniform location string appended to violation messages for file-backed subjects
 * (classes, files, functions, properties).
 *
 * The format is `<modulePath>, <sourceSet> source set, <filePath>[:<line>[:<column>]]`, so a violation can be
 * traced to its module and source set in multi-module and multiplatform projects, not just its file.
 * Module rules embed the module path in the message itself and do not use this helper.
 */
internal object ViolationLocation {
    fun of(
        modulePath: String,
        sourceSetName: String?,
        filePath: String,
        line: Int = -1,
        column: Int = -1,
    ): String {
        val file =
            when {
                line > 0 && column > 0 -> "$filePath:$line:$column"
                line > 0 -> "$filePath:$line"
                else -> filePath
            }
        return "$modulePath, ${sourceSetName ?: "unknown"} source set) ($file"
    }
}

