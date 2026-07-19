/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/**
 * Finds class and object declarations and derives their lexical scopes from Kotlin PSI.
 */
internal object DeclaredClassScanner {
    fun collectFqNames(ktFile: KtFile): Set<String> {
        val fqNames = mutableSetOf<String>()
        ktFile.accept(
            object : KtTreeVisitorVoid() {
                override fun visitClassOrObject(classOrObject: KtClassOrObject) {
                    super.visitClassOrObject(classOrObject)
                    classOrObject.fqName?.asString()?.let(fqNames::add)
                }
            },
        )
        return fqNames
    }

    fun enclosingScopes(
        ktFile: KtFile,
        offset: Int,
    ): List<String> {
        val scopes = mutableListOf<String>()
        var element = ktFile.findElementAt(offset)
        while (element != null) {
            if (element is KtClassOrObject) {
                element.fqName?.asString()?.let(scopes::add)
            }
            element = element.parent
        }
        return scopes
    }

    fun scopedCandidates(
        fqName: String,
        packageName: String,
        classScopes: List<String>,
    ): List<String> {
        if (fqName.substringBeforeLast('.', missingDelimiterValue = "") != packageName) return emptyList()
        val simpleName = fqName.substringAfterLast('.')
        return classScopes.map { "$it.$simpleName" }
    }

    fun enclosingScopes(
        classFqName: String,
        packageName: String,
    ): List<String> {
        val prefix = if (packageName.isEmpty()) "" else "$packageName."
        val classNames = classFqName.removePrefix(prefix).split('.').filter(String::isNotEmpty)
        return classNames.indices
            .reversed()
            .map { index ->
                (listOf(packageName).filter(String::isNotEmpty) + classNames.take(index + 1)).joinToString(".")
            }
    }
}
