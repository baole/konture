/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Represents a scope containing a set of Kotlin files for checking file-level rules.
 *
 * @property files The list of [FileDeclaration] structures included in this scope.
 */
class KontureFileScope(
    val files: List<FileDeclaration>,
) {
    companion object {
        /**
         * Creates a [KontureFileScope] representing all files in the project.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         */
        fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFileScope {
            val files = graph.getAllModules().flatMap { module -> module.files.filter { it.membershipsFor(module.path).any(sourceSets::matches) } }
            return KontureFileScope(files)
        }

        /**
         * Creates a [KontureFileScope] for a specific Gradle module.
         *
         * @param path The Gradle module path.
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         */
        fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFileScope {
            val module =
                graph.getAllModules().find { it.path == path }
                    ?: throw IllegalArgumentException("Module $path not found in project graph")
            return KontureFileScope(module.files.filter { it.membershipsFor(module.path).any(sourceSets::matches) })
        }

        /**
         * Creates a [KontureFileScope] containing files in a specific package or its subpackages.
         *
         * @param packageName The package FQN prefix.
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         */
        fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFileScope {
            val files =
                graph
                    .getAllModules()
                    .flatMap { module -> module.files.filter { it.membershipsFor(module.path).any(sourceSets::matches) } }
                    .filter { it.packageName == packageName || it.packageName.startsWith("$packageName.") }
            return KontureFileScope(files)
        }
    }
}

operator fun KontureFileScope.plus(other: KontureFileScope): KontureFileScope =
    KontureFileScope(
        this.files + other.files,
    )

operator fun KontureFileScope.minus(other: KontureFileScope): KontureFileScope {
    val otherPaths = other.files.map { it.filePath }.toSet()
    return KontureFileScope(this.files.filterNot { it.filePath in otherPaths })
}

// Filtering extensions on List<FileDeclaration>

/**
 * Filters the list of file declarations to include only those whose names end with the specified suffix.
 */
@kotlin.jvm.JvmName("withFileNameEndingWith")
fun List<FileDeclaration>.withNameEndingWith(suffix: String): List<FileDeclaration> = filter { it.name.endsWith(suffix) }

/**
 * Filters the list of file declarations to include only those whose names start with the specified prefix.
 */
@kotlin.jvm.JvmName("withFileNameStartingWith")
fun List<FileDeclaration>.withNameStartingWith(prefix: String): List<FileDeclaration> = filter { it.name.startsWith(prefix) }

/**
 * Filters the list of file declarations to include only those whose names match the specified glob pattern.
 * Supports '*' wildcards.
 */
@kotlin.jvm.JvmName("withFileNameMatching")
fun List<FileDeclaration>.withNameMatching(pattern: String): List<FileDeclaration> = filter { PatternMatchers.matchesSimpleGlob(pattern, it.name) }

/**
 * Filters the list of file declarations to include only those residing in packages matching the specified pattern.
 * Supports '..' wildcards.
 */
@kotlin.jvm.JvmName("withFilePackage")
fun List<FileDeclaration>.withPackage(packagePattern: String): List<FileDeclaration> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

// Scope-level delegation for KontureFileScope

fun KontureFileScope.withNameEndingWith(suffix: String) = KontureFileScope(files.withNameEndingWith(suffix))

fun KontureFileScope.withNameStartingWith(prefix: String) = KontureFileScope(files.withNameStartingWith(prefix))

fun KontureFileScope.withNameMatching(pattern: String) = KontureFileScope(files.withNameMatching(pattern))

fun KontureFileScope.withPackage(packagePattern: String) = KontureFileScope(files.withPackage(packagePattern))

// Assertion extensions on List<FileDeclaration> and KontureFileScope

@kotlin.jvm.JvmName("assertFilesTrue")
fun List<FileDeclaration>.assertTrue(
    additionalMessage: String? = null,
    predicate: (FileDeclaration) -> Boolean,
) {
    val violations = filterNot(predicate)
    if (violations.isNotEmpty()) {
        val message =
            buildString {
                appendLine("Assertion failed! The following files do not meet the criteria:")
                if (additionalMessage != null) {
                    appendLine(additionalMessage)
                }
                violations.forEach {
                    appendLine("  - ${it.name} (at ${it.filePath})")
                }
            }
        throw AssertionError(message)
    }
}

fun List<FileDeclaration>.assertNoWildcardImports(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { file ->
        file.imports.none { it.endsWith(".*") }
    }
}

fun List<FileDeclaration>.assertOnlyOneClassPerFile(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { file ->
        file.classes.size <= 1
    }
}

fun List<FileDeclaration>.assertFileNameMatchesClassName(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { file ->
        val expectedName = file.name.substringBeforeLast(".kt")
        file.classes.isEmpty() || file.classes.any { it.name == expectedName }
    }
}

@kotlin.jvm.JvmName("assertFilesHasKDoc")
fun List<FileDeclaration>.assertHasKDoc(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { it.kdocText?.isNotBlank() == true }
}

fun KontureFileScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (FileDeclaration) -> Boolean,
) {
    files.assertTrue(additionalMessage, predicate)
}

fun KontureFileScope.assertNoWildcardImports(additionalMessage: String? = null) =
    files.assertNoWildcardImports(
        additionalMessage,
    )

fun KontureFileScope.assertOnlyOneClassPerFile(additionalMessage: String? = null) =
    files.assertOnlyOneClassPerFile(
        additionalMessage,
    )

fun KontureFileScope.assertFileNameMatchesClassName(additionalMessage: String? = null) =
    files.assertFileNameMatchesClassName(
        additionalMessage,
    )

fun KontureFileScope.assertHasKDoc(additionalMessage: String? = null) = files.assertHasKDoc(additionalMessage)

/**
 * Asserts that all file declarations reside in packages matching any of the specified patterns.
 * Matches using standard Kotlin package wildcard matching (e.g. "..domain..").
 *
 * ### Example:
 * ```kotlin
 * files.assertResideInAPackage("..domain..", "..data..")
 * ```
 *
 * @param packagePatterns The package wildcard patterns. At least one must match.
 * @throws AssertionError if any file does not reside in a matching package.
 */
fun List<FileDeclaration>.assertResideInAPackage(vararg packagePatterns: String) {
    assertTrue("Files must reside in any of these packages: ${packagePatterns.joinToString()}") { file ->
        packagePatterns.any { PatternMatchers.matchesPackage(it, file.packageName) }
    }
}

/**
 * Asserts that all file declarations have names ending with any of the specified suffixes.
 *
 * ### Example:
 * ```kotlin
 * files.assertNameEndingWith("Test", "Spec")
 * ```
 *
 * @param suffixes The allowed suffixes. At least one must match.
 * @throws AssertionError if any file name does not end with any of the specified suffixes.
 */
fun List<FileDeclaration>.assertNameEndingWith(vararg suffixes: String) {
    assertTrue("Files must have names ending with any of: ${suffixes.joinToString()}") { file ->
        suffixes.any { file.name.endsWith(it) }
    }
}

/**
 * Asserts that all file declarations have names starting with any of the specified prefixes.
 *
 * ### Example:
 * ```kotlin
 * files.assertNameStartingWith("Get", "Fetch")
 * ```
 *
 * @param prefixes The allowed prefixes. At least one must match.
 * @throws AssertionError if any file name does not start with any of the specified prefixes.
 */
fun List<FileDeclaration>.assertNameStartingWith(vararg prefixes: String) {
    assertTrue("Files must have names starting with any of: ${prefixes.joinToString()}") { file ->
        prefixes.any { file.name.startsWith(it) }
    }
}

/**
 * Asserts that all file declarations have names matching any of the specified glob patterns.
 *
 * ### Example:
 * ```kotlin
 * files.assertNameMatching("*Test.kt", "*Spec.kt")
 * ```
 *
 * @param patterns The glob patterns. At least one must match.
 * @throws AssertionError if any file name does not match any of the specified glob patterns.
 */
fun List<FileDeclaration>.assertNameMatching(vararg patterns: String) {
    assertTrue("Files must have names matching any of the glob patterns: ${patterns.joinToString()}") { file ->
        patterns.any { PatternMatchers.matchesSimpleGlob(it, file.name) }
    }
}

/**
 * Asserts that all file declarations in the scope reside in packages matching any of the specified patterns.
 * Matches using standard Kotlin package wildcard matching (e.g. "..domain..").
 *
 * @param packagePatterns The package wildcard patterns. At least one must match.
 * @throws AssertionError if any file does not reside in a matching package.
 */
fun KontureFileScope.assertResideInAPackage(vararg packagePatterns: String) = files.assertResideInAPackage(*packagePatterns)

/**
 * Asserts that all file declarations in the scope have names ending with any of the specified suffixes.
 *
 * @param suffixes The allowed suffixes. At least one must match.
 * @throws AssertionError if any file name does not end with any of the specified suffixes.
 */
fun KontureFileScope.assertNameEndingWith(vararg suffixes: String) = files.assertNameEndingWith(*suffixes)

/**
 * Asserts that all file declarations in the scope have names starting with any of the specified prefixes.
 *
 * @param prefixes The allowed prefixes. At least one must match.
 * @throws AssertionError if any file name does not start with any of the specified prefixes.
 */
fun KontureFileScope.assertNameStartingWith(vararg prefixes: String) = files.assertNameStartingWith(*prefixes)

/**
 * Asserts that all file declarations in the scope have names matching any of the specified glob patterns.
 *
 * @param patterns The glob patterns. At least one must match.
 * @throws AssertionError if any file name does not match any of the specified glob patterns.
 */
fun KontureFileScope.assertNameMatching(vararg patterns: String) = files.assertNameMatching(*patterns)
