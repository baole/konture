/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Collects all fully-qualified package names that this function depends on
 * through its signature types (return type, parameters, receiver type, annotations)
 * and body/expression usages.
 */
internal fun FunctionDeclarationContext.collectDependencyPackages(): Set<String> {
    val packages = mutableSetOf<String>()

    fun extractPkg(typeText: String) {
        val clean = typeText.substringBefore("<").removeSuffix("?").trim()
        if (clean.contains(".")) {
            val pkg = PatternMatchers.extractPackage(clean) ?: clean.substringBeforeLast(".", "")
            if (pkg.isNotEmpty()) {
                packages.add(pkg)
            }
        }
    }

    extractPkg(declaration.returnType)
    declaration.resolvedReturnType?.let { extractPkg(it) }
    for (param in declaration.parameters) {
        extractPkg(param.type)
    }
    declaration.receiverType?.let { extractPkg(it) }

    for (ann in declaration.annotations) {
        val fq = ann.fqName
        val pkg = PatternMatchers.extractPackage(fq) ?: fq.substringBeforeLast(".", "")
        if (pkg.isNotEmpty()) {
            packages.add(pkg)
        }
    }

    for (usage in usages) {
        val target = usage.targetFqName
        val pkg = PatternMatchers.extractPackage(target) ?: target.substringBeforeLast(".", "")
        if (pkg.isNotEmpty()) {
            packages.add(pkg)
        }
        for (possible in usage.possibleTargetFqNames) {
            val pPkg = PatternMatchers.extractPackage(possible) ?: possible.substringBeforeLast(".", "")
            if (pPkg.isNotEmpty()) {
                packages.add(pPkg)
            }
        }
    }

    return packages
}
