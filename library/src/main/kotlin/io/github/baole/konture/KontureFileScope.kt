/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ViolationLocation
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/**
 * Represents a scope containing a set of Kotlin files for checking file-level rules.
 *
 * @property files The list of [FileDeclaration] structures included in this scope.
 */
public class KontureFileScope(
    /** Filter or assertion criteria for files. */
    public val files: List<FileDeclaration>,
) {
    /** Factory methods for constructing file scopes. */
    public companion object {
        /**
         * Creates a [KontureFileScope] representing all files in the project.
         *
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         */
        public fun fromProject(
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFileScope {
            /** Filter or assertion criteria for files. */
            val files =
                graph.getAllModules().flatMap {
                        module ->
                    module.files.filter { it.membershipsFor(module.path).any(sourceSets::matches) }
                }
            return KontureFileScope(files)
        }

        /**
         * Creates a [KontureFileScope] for a specific Gradle module.
         *
         * @param path The Gradle module path.
         * @param graph The project graph to use (defaults to [Konture.projectGraph]).
         */
        public fun fromModule(
            path: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFileScope {
            /** Filter or assertion criteria for module. */
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
        public fun fromPackage(
            packageName: String,
            graph: ProjectGraph = Konture.projectGraph,
            sourceSets: SourceSetSelector = SourceSets.production(),
        ): KontureFileScope {
            /** Filter or assertion criteria for files. */
            val files =
                graph
                    .getAllModules()
                    .flatMap {
                            module ->
                        module.files.filter { it.membershipsFor(module.path).any(sourceSets::matches) }
                    }
                    .filter { it.packageName == packageName || it.packageName.startsWith("$packageName.") }
            return KontureFileScope(files)
        }
    }
}

/** Filter or assertion criteria for plus. */
public operator fun KontureFileScope.plus(other: KontureFileScope): KontureFileScope =
    KontureFileScope(
        this.files + other.files,
    )

/** Filter or assertion criteria for minus. */
public operator fun KontureFileScope.minus(other: KontureFileScope): KontureFileScope {
    /** Filter or assertion criteria for other paths. */
    val otherPaths = other.files.map { it.filePath }.toSet()
    return KontureFileScope(this.files.filterNot { it.filePath in otherPaths })
}

// Filtering extensions on List<FileDeclaration>

/**
 * Filters the list of file declarations to include only those whose names end with the specified suffix.
 */
@JvmName("withFileNameEndingWith")
public fun List<FileDeclaration>.withNameEndingWith(suffix: String): List<FileDeclaration> =
    filter { it.name.endsWith(suffix) }

/**
 * Filters the list of file declarations to include only those whose names start with the specified prefix.
 */
@JvmName("withFileNameStartingWith")
public fun List<FileDeclaration>.withNameStartingWith(prefix: String): List<FileDeclaration> =
    filter { it.name.startsWith(prefix) }

/**
 * Filters the list of file declarations to include only those whose names match the specified glob pattern.
 * Supports '*' wildcards.
 */
@JvmName("withFileNameMatching")
public fun List<FileDeclaration>.withNameMatching(pattern: String): List<FileDeclaration> =
    filter { PatternMatchers.matchesSimpleGlob(pattern, it.name) }

/**
 * Filters the list of file declarations to include only those residing in packages matching the specified pattern.
 * Supports '..' wildcards.
 */
@JvmName("withFilePackage")
public fun List<FileDeclaration>.withPackage(packagePattern: String): List<FileDeclaration> =
    filter { PatternMatchers.matchesPackage(packagePattern, it.packageName) }

// Scope-level delegation for KontureFileScope

/** Filter or assertion criteria for with name ending with. */
public fun KontureFileScope.withNameEndingWith(suffix: String): KontureFileScope =
    KontureFileScope(files.withNameEndingWith(suffix))

/** Filter or assertion criteria for with name starting with. */
public fun KontureFileScope.withNameStartingWith(prefix: String): KontureFileScope =
    KontureFileScope(files.withNameStartingWith(prefix))

/** Filter or assertion criteria for with name matching. */
public fun KontureFileScope.withNameMatching(pattern: String): KontureFileScope =
    KontureFileScope(files.withNameMatching(pattern))

/** Filter or assertion criteria for with package. */
public fun KontureFileScope.withPackage(packagePattern: String): KontureFileScope =
    KontureFileScope(files.withPackage(packagePattern))

/** Filter or assertion criteria for with module. */
public fun KontureFileScope.withModule(
    modulePath: String,
    graph: ProjectGraph = Konture.projectGraph,
): KontureFileScope {
    /** Filter or assertion criteria for norm. */
    val norm = if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) ":$modulePath" else modulePath
    return KontureFileScope(
        files.filter { file ->
            /** Filter or assertion criteria for mod. */
            val mod =
                graph.getAllModules().find { m ->
                    m.files.any { f -> f.filePath == file.filePath || f.name == file.name }
                }
            mod?.path == norm
        },
    )
}

/** Filters files in this scope to include only those with import matching [importPath]. */
public fun KontureFileScope.withImportOf(importPath: String): KontureFileScope =
    KontureFileScope(
        files.filter {
                file ->
            file.imports.any { PatternMatchers.matchesPackage(importPath, it) || it == importPath }
        },
    )

/** Filters files in this scope to include only those with import of class [type]. */
public fun KontureFileScope.withImportOf(type: KClass<*>): KontureFileScope = withImportOf(type.kontureQualifiedName())

/** Filters files in this scope to include only those containing class [fqName]. */
public fun KontureFileScope.containingClass(fqName: String): KontureFileScope =
    KontureFileScope(files.filter { file -> file.classes.any { it.fqName == fqName || it.name == fqName } })

/** Filters files in this scope to include only those containing class [type]. */
public fun KontureFileScope.containingClass(type: KClass<*>): KontureFileScope =
    containingClass(type.kontureQualifiedName())

// Assertion extensions on List<FileDeclaration> and KontureFileScope

/** Asserts that all files in this list satisfy [predicate]. */
@JvmName("assertFilesTrue")
public fun List<FileDeclaration>.assertTrue(
    additionalMessage: String? = null,
    predicate: (FileDeclaration) -> Boolean,
) {
    /** Filter or assertion criteria for violations. */
    val violations = filterNot(predicate)
    if (violations.isNotEmpty()) {
        /** Filter or assertion criteria for message. */
        val message =
            buildString {
                appendLine("Assertion failed! The following files do not meet the criteria:")
                if (additionalMessage != null) {
                    appendLine(additionalMessage)
                }
                violations.forEach {
                    appendLine("  - ${it.name} (at ${ViolationLocation.format(it)})")
                }
            }
        throw AssertionError(message)
    }
}

/** Asserts that no file in this list contains wildcard imports. */
public fun List<FileDeclaration>.assertNoWildcardImports(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { file ->
        file.imports.none { it.endsWith(".*") }
    }
}

/** Asserts that files in this list contain at most one class. */
public fun List<FileDeclaration>.assertOnlyOneClassPerFile(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { file ->
        file.classes.size <= 1
    }
}

/** Asserts that file names match their primary class names. */
public fun List<FileDeclaration>.assertFileNameMatchesClassName(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { file ->
        /** Filter or assertion criteria for expected name. */
        val expectedName = file.name.substringBeforeLast(".kt")
        file.classes.isEmpty() || file.classes.any { it.name == expectedName }
    }
}

/** Asserts that files in this list have KDoc documentation. */
@JvmName("assertFilesHasKDoc")
public fun List<FileDeclaration>.assertHasKDoc(additionalMessage: String? = null) {
    assertTrue(additionalMessage) { it.kdocText?.isNotBlank() == true }
}

/** Asserts that all files in this scope satisfy [predicate]. */
public fun KontureFileScope.assertTrue(
    additionalMessage: String? = null,
    predicate: (FileDeclaration) -> Boolean,
) {
    files.assertTrue(additionalMessage, predicate)
}

/** Asserts that no file in this scope contains wildcard imports. */
public fun KontureFileScope.assertNoWildcardImports(additionalMessage: String? = null): Unit =
    files.assertNoWildcardImports(
        additionalMessage,
    )

/** Asserts that files in this scope contain at most one class. */
public fun KontureFileScope.assertOnlyOneClassPerFile(additionalMessage: String? = null): Unit =
    files.assertOnlyOneClassPerFile(
        additionalMessage,
    )

/** Asserts that file names in this scope match their primary class names. */
public fun KontureFileScope.assertFileNameMatchesClassName(additionalMessage: String? = null): Unit =
    files.assertFileNameMatchesClassName(
        additionalMessage,
    )

/** Asserts that files in this scope have KDoc documentation. */
public fun KontureFileScope.assertHasKDoc(additionalMessage: String? = null): Unit =
    files.assertHasKDoc(additionalMessage)

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
public fun List<FileDeclaration>.assertResideInAPackage(vararg packagePatterns: String) {
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
public fun List<FileDeclaration>.assertNameEndingWith(vararg suffixes: String) {
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
public fun List<FileDeclaration>.assertNameStartingWith(vararg prefixes: String) {
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
public fun List<FileDeclaration>.assertNameMatching(vararg patterns: String) {
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
public fun KontureFileScope.assertResideInAPackage(vararg packagePatterns: String): Unit =
    files.assertResideInAPackage(*packagePatterns)

/**
 * Asserts that all file declarations in the scope have names ending with any of the specified suffixes.
 *
 * @param suffixes The allowed suffixes. At least one must match.
 * @throws AssertionError if any file name does not end with any of the specified suffixes.
 */
public fun KontureFileScope.assertNameEndingWith(vararg suffixes: String): Unit = files.assertNameEndingWith(*suffixes)

/**
 * Asserts that all file declarations in the scope have names starting with any of the specified prefixes.
 *
 * @param prefixes The allowed prefixes. At least one must match.
 * @throws AssertionError if any file name does not start with any of the specified prefixes.
 */
public fun KontureFileScope.assertNameStartingWith(vararg prefixes: String): Unit =
    files.assertNameStartingWith(*prefixes)

/**
 * Asserts that all file declarations in the scope have names matching any of the specified glob patterns.
 *
 * @param patterns The glob patterns. At least one must match.
 * @throws AssertionError if any file name does not match any of the specified glob patterns.
 */
public fun KontureFileScope.assertNameMatching(vararg patterns: String): Unit = files.assertNameMatching(*patterns)

/** Filter or assertion criteria for list. */
public fun List<FileDeclaration>.assertResideInAModule(
    modulePath: String,
    graph: ProjectGraph = Konture.projectGraph,
) {
    /** Filter or assertion criteria for norm. */
    val norm = if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) ":$modulePath" else modulePath
    assertTrue("Files must reside in module '$norm'") { file ->
        /** Filter or assertion criteria for mod. */
        val mod =
            graph.getAllModules().find { m ->
                m.files.any { f -> f.filePath == file.filePath || f.name == file.name }
            }
        mod?.path == norm
    }
}

/** Filter or assertion criteria for list. */
public fun List<FileDeclaration>.assertNotResideInAModule(
    modulePath: String,
    graph: ProjectGraph = Konture.projectGraph,
) {
    /** Filter or assertion criteria for norm. */
    val norm = if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) ":$modulePath" else modulePath
    assertTrue("Files must not reside in module '$norm'") { file ->
        /** Filter or assertion criteria for mod. */
        val mod =
            graph.getAllModules().find { m ->
                m.files.any { f -> f.filePath == file.filePath || f.name == file.name }
            }
        mod?.path != norm
    }
}

/** Filter or assertion criteria for assert reside in a module. */
public fun KontureFileScope.assertResideInAModule(
    modulePath: String,
    graph: ProjectGraph = Konture.projectGraph,
): Unit = files.assertResideInAModule(modulePath, graph)

/** Filter or assertion criteria for assert not reside in a module. */
public fun KontureFileScope.assertNotResideInAModule(
    modulePath: String,
    graph: ProjectGraph = Konture.projectGraph,
): Unit = files.assertNotResideInAModule(modulePath, graph)
