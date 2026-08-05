/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.FunctionDeclarationContext
import io.github.baole.konture.PropertyDeclarationContext

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
        val fileName = if (filePath.isNotEmpty()) java.io.File(filePath).name else filePath
        val file =
            when {
                line > 0 -> "$fileName:$line"
                else -> fileName
            }
        return "$modulePath, ${sourceSetName ?: "unknown"} source set) ($file"
    }

    fun format(
        filePath: String,
        line: Int = -1,
        column: Int = -1,
        modulePath: String? = null,
        sourceSetName: String? = null,
    ): String {
        val effectiveLine =
            if (line > 0) {
                line
            } else if (filePath.isNotEmpty()) {
                1
            } else {
                -1
            }
        return if (modulePath != null) {
            of(modulePath, sourceSetName, filePath, effectiveLine, column)
        } else {
            val fileName = if (filePath.isNotEmpty()) java.io.File(filePath).name else filePath
            when {
                effectiveLine > 0 -> "$fileName:$effectiveLine"
                else -> fileName
            }
        }
    }

    fun format(
        cls: ClassDeclaration,
        modulePath: String? = null,
        sourceSetName: String? = null,
    ): String = format(cls.filePath, cls.sourceLine, modulePath = modulePath, sourceSetName = sourceSetName)

    fun format(
        file: FileDeclaration,
        modulePath: String? = null,
        sourceSetName: String? = null,
    ): String = format(file.filePath, line = 1, modulePath = modulePath, sourceSetName = sourceSetName)

    fun format(func: FunctionDeclarationContext): String =
        format(
            func.filePath,
            func.declaration.sourceLine,
            modulePath = func.modulePath,
            sourceSetName = func.sourceSet?.name,
        )

    fun format(prop: PropertyDeclarationContext): String =
        format(
            prop.filePath,
            prop.declaration.sourceLine,
            modulePath = prop.modulePath,
            sourceSetName = prop.sourceSet?.name,
        )
}
