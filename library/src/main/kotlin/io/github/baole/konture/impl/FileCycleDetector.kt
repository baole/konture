/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.FileDeclarationContext
import io.github.baole.konture.collectDependencyPackages

internal object FileCycleDetector {
    fun buildAdjacency(files: List<FileDeclarationContext>): Map<String, Set<String>> {
        val symbolToFile = mutableMapOf<String, String>()
        val packageToFiles = mutableMapOf<String, MutableSet<String>>()

        for (f in files) {
            val fileName = f.declaration.name
            packageToFiles.getOrPut(f.declaration.packageName) { mutableSetOf() }.add(fileName)
            for (cls in f.declaration.classes) {
                symbolToFile[cls.fqName] = fileName
            }
        }

        val adjacency = sortedMapOf<String, MutableSet<String>>()
        for (f in files) {
            adjacency[f.declaration.name] = sortedSetOf()
        }

        val allClasses = files.flatMap { it.declaration.classes }

        for (f in files) {
            val srcFile = f.declaration.name
            val targets = adjacency.getValue(srcFile)
            processClassDependencies(srcFile, f.declaration.classes, allClasses, packageToFiles, targets)
            processImportDependencies(srcFile, f.declaration.imports, symbolToFile, targets)
        }

        return adjacency
    }

    private fun processClassDependencies(
        srcFile: String,
        classes: List<ClassDeclaration>,
        allClasses: List<ClassDeclaration>,
        packageToFiles: Map<String, Set<String>>,
        targets: MutableSet<String>,
    ) {
        for (cls in classes) {
            for (depPkg in cls.collectDependencyPackages(allClasses)) {
                packageToFiles[depPkg]?.forEach { targetFile ->
                    if (targetFile != srcFile) {
                        targets.add(targetFile)
                    }
                }
            }
        }
    }

    private fun processImportDependencies(
        srcFile: String,
        imports: List<String>,
        symbolToFile: Map<String, String>,
        targets: MutableSet<String>,
    ) {
        for (imp in imports) {
            val targetFile = symbolToFile[imp]
            if (targetFile != null && targetFile != srcFile) {
                targets.add(targetFile)
            }
        }
    }

    fun findCycles(files: List<FileDeclarationContext>): List<List<String>> {
        val adjacency = buildAdjacency(files)
        return SliceCycleDetector.findCycles(adjacency)
    }
}
