/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Represents a scope containing a set of property declaration contexts for checking property-level rules in a Konsist-inspired fluent DSL.
 *
 * @property properties The list of [PropertyDeclarationContext] structures included in this scope.
 */
class KonturePropertyScope(
    val properties: List<PropertyDeclarationContext>,
) {
    companion object {
        fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            val props = graph.getAllModules().flatMap { module ->
                module.files
                    .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                    .flatMap { file ->
                        val top = file.topLevelProperties.map {
                            PropertyDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                        }
                        val mem = file.classes.flatMap { cls ->
                            cls.properties.map {
                                PropertyDeclarationContext(it, file.packageName, cls.name, module.path, file.filePath)
                            }
                        }
                        top + mem
                    }
            }
            return KonturePropertyScope(props)
        }

        fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            val module = graph.getAllModules().find { it.path == path }
                ?: throw IllegalArgumentException("Module $path not found in project graph")
            val props = module.files
                .filter { file -> file.membershipsFor(module.path).any(sourceSets::matches) }
                .flatMap { file ->
                    val top = file.topLevelProperties.map {
                        PropertyDeclarationContext(it, file.packageName, null, module.path, file.filePath)
                    }
                    val mem = file.classes.flatMap { cls ->
                        cls.properties.map {
                            PropertyDeclarationContext(it, file.packageName, cls.name, module.path, file.filePath)
                        }
                    }
                    top + mem
                }
            return KonturePropertyScope(props)
        }

        fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KonturePropertyScope {
            val props = fromProject(graph, sourceSets).properties.filter {
                it.packageName == packageName || it.packageName.startsWith("$packageName.")
            }
            return KonturePropertyScope(props)
        }
    }
}

operator fun KonturePropertyScope.plus(other: KonturePropertyScope): KonturePropertyScope =
    KonturePropertyScope(this.properties + other.properties)

operator fun KonturePropertyScope.minus(other: KonturePropertyScope): KonturePropertyScope {
    val otherNames = other.properties.map { it.qualifiedName }.toSet()
    return KonturePropertyScope(this.properties.filterNot { it.qualifiedName in otherNames })
}

// Filtering extensions
fun List<PropertyDeclarationContext>.withNameEndingWith(suffix: String): List<PropertyDeclarationContext> =
    filter { it.declaration.name.endsWith(suffix) }

fun List<PropertyDeclarationContext>.withNameStartingWith(prefix: String): List<PropertyDeclarationContext> =
    filter { it.declaration.name.startsWith(prefix) }

fun List<PropertyDeclarationContext>.withNameMatching(pattern: String): List<PropertyDeclarationContext> =
    filter { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }


fun List<PropertyDeclarationContext>.withPackage(packagePattern: String): List<PropertyDeclarationContext> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

fun List<PropertyDeclarationContext>.valProperties(): List<PropertyDeclarationContext> =
    filter { it.declaration.isVal }

fun List<PropertyDeclarationContext>.varProperties(): List<PropertyDeclarationContext> =
    filter { !it.declaration.isVal }

fun List<PropertyDeclarationContext>.memberProperties(): List<PropertyDeclarationContext> =
    filter { it.className != null }

fun List<PropertyDeclarationContext>.topLevelProperties(): List<PropertyDeclarationContext> =
    filter { it.className == null }

// Assertion extensions on KonturePropertyScope
fun KonturePropertyScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (PropertyDeclarationContext) -> Boolean,
) {
    val failing = properties.filterNot(predicate)
    if (failing.isNotEmpty()) {
        val details = failing.joinToString("\n - ") { it.qualifiedName }
        val prefix = additionalMessage?.let { "$it\n" } ?: ""
        throw AssertionError("${prefix}Properties failed assertion:\n - $details")
    }
}
