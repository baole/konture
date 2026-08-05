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
        fqName: String? = null,
        packageName: String? = null,
    ): String {
        val fileName = if (filePath.isNotEmpty()) java.io.File(filePath).name else filePath
        val fileLocation =
            when {
                line > 0 -> "$fileName:$line"
                else -> fileName
            }

        val targetClass =
            when {
                !fqName.isNullOrEmpty() && fqName != packageName -> fqName
                !packageName.isNullOrEmpty() && fileName.isNotEmpty() -> {
                    val simpleName = fileName.substringBeforeLast('.')
                    "$packageName.$simpleName"
                }
                !fqName.isNullOrEmpty() -> fqName
                else -> null
            }

        val locationSpec =
            if (targetClass != null) {
                "$targetClass($fileLocation)"
            } else {
                fileLocation
            }

        return "$modulePath, ${sourceSetName ?: "unknown"} source set) ($locationSpec"
    }

    fun format(
        filePath: String,
        line: Int = -1,
        column: Int = -1,
        modulePath: String? = null,
        sourceSetName: String? = null,
        fqName: String? = null,
        packageName: String? = null,
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
            of(modulePath, sourceSetName, filePath, effectiveLine, column, fqName, packageName)
        } else {
            val fileName = if (filePath.isNotEmpty()) java.io.File(filePath).name else filePath
            val fileLocation =
                when {
                    effectiveLine > 0 -> "$fileName:$effectiveLine"
                    else -> fileName
                }
            val targetClass =
                when {
                    !fqName.isNullOrEmpty() && fqName != packageName -> fqName
                    !packageName.isNullOrEmpty() && fileName.isNotEmpty() -> {
                        val simpleName = fileName.substringBeforeLast('.')
                        "$packageName.$simpleName"
                    }
                    !fqName.isNullOrEmpty() -> fqName
                    else -> null
                }
            if (targetClass != null) {
                "$targetClass($fileLocation)"
            } else {
                fileLocation
            }
        }
    }

    fun format(
        cls: ClassDeclaration,
        modulePath: String? = null,
        sourceSetName: String? = null,
    ): String =
        format(
            filePath = cls.filePath,
            line = cls.sourceLine,
            modulePath = modulePath,
            sourceSetName = sourceSetName,
            fqName = cls.fqName,
            packageName = cls.packageName,
        )

    fun format(
        file: FileDeclaration,
        modulePath: String? = null,
        sourceSetName: String? = null,
    ): String =
        format(
            filePath = file.filePath,
            line = 1,
            modulePath = modulePath,
            sourceSetName = sourceSetName,
            packageName = file.packageName,
        )

    fun format(func: FunctionDeclarationContext): String =
        format(
            filePath = func.filePath,
            line = func.declaration.sourceLine,
            modulePath = func.modulePath,
            sourceSetName = func.sourceSet?.name,
            fqName = func.className?.let { "${func.packageName}.$it" } ?: func.packageName,
            packageName = func.packageName,
        )

    fun format(prop: PropertyDeclarationContext): String =
        format(
            filePath = prop.filePath,
            line = prop.declaration.sourceLine,
            modulePath = prop.modulePath,
            sourceSetName = prop.sourceSet?.name,
            fqName = prop.className?.let { "${prop.packageName}.$it" } ?: prop.packageName,
            packageName = prop.packageName,
        )
}
