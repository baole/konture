/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Represents a scope containing a set of function declaration contexts for checking function-level rules in a Konsist-inspired fluent DSL.
 *
 * @property functions The list of [FunctionDeclarationContext] structures included in this scope.
 */
class KontureFunctionScope(
    val functions: List<FunctionDeclarationContext>,
) {
    companion object {
        fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            val funcs = graph.getAllModules().flatMap { module ->
                module.files
                    .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                    .flatMap { file ->
                        val top = file.topLevelFunctions.map {
                            FunctionDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                        }
                        val mem = file.classes.flatMap { cls ->
                            cls.functions.map {
                                FunctionDeclarationContext(it, file.packageName, cls.name, module.path, file.filePath)
                            }
                        }
                        top + mem
                    }
            }
            return KontureFunctionScope(funcs)
        }

        fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            val module = graph.getAllModules().find { it.path == path }
                ?: throw IllegalArgumentException("Module $path not found in project graph")
            val funcs = module.files
                .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                .flatMap { file ->
                    val top = file.topLevelFunctions.map {
                        FunctionDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                    }
                    val mem = file.classes.flatMap { cls ->
                        cls.functions.map {
                            FunctionDeclarationContext(it, file.packageName, cls.name, module.path, file.filePath)
                        }
                    }
                    top + mem
                }
            return KontureFunctionScope(funcs)
        }

        fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFunctionScope {
            val funcs = fromProject(graph, sourceSets).functions.filter {
                it.packageName == packageName || it.packageName.startsWith("$packageName.")
            }
            return KontureFunctionScope(funcs)
        }
    }
}

operator fun KontureFunctionScope.plus(other: KontureFunctionScope): KontureFunctionScope =
    KontureFunctionScope(this.functions + other.functions)

operator fun KontureFunctionScope.minus(other: KontureFunctionScope): KontureFunctionScope {
    val otherNames = other.functions.map { it.qualifiedName }.toSet()
    return KontureFunctionScope(this.functions.filterNot { it.qualifiedName in otherNames })
}

// Filtering extensions
fun List<FunctionDeclarationContext>.withNameEndingWith(suffix: String): List<FunctionDeclarationContext> =
    filter { it.declaration.name.endsWith(suffix) }

fun List<FunctionDeclarationContext>.withNameStartingWith(prefix: String): List<FunctionDeclarationContext> =
    filter { it.declaration.name.startsWith(prefix) }

fun List<FunctionDeclarationContext>.withNameMatching(pattern: String): List<FunctionDeclarationContext> =
    filter { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }


fun List<FunctionDeclarationContext>.withPackage(packagePattern: String): List<FunctionDeclarationContext> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

fun List<FunctionDeclarationContext>.memberFunctions(): List<FunctionDeclarationContext> =
    filter { it.className != null }

fun List<FunctionDeclarationContext>.topLevelFunctions(): List<FunctionDeclarationContext> =
    filter { it.className == null }

// Assertion extensions on KontureFunctionScope
fun KontureFunctionScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (FunctionDeclarationContext) -> Boolean,
) {
    val failing = functions.filterNot(predicate)
    if (failing.isNotEmpty()) {
        val details = failing.joinToString("\n - ") { it.qualifiedName }
        val prefix = additionalMessage?.let { "$it\n" } ?: ""
        throw AssertionError("${prefix}Functions failed assertion:\n - $details")
    }
}
