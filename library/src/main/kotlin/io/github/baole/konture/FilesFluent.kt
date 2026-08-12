/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

// ==========================================
// Files Rule Builder Fluent DSL
// ==========================================

/**
 * Filters files in this rule using a concise lambda predicate evaluated on each [FileDeclarationContext].
 *
 * @param predicate The filter criteria block executed on the [FileDeclarationContext].
 * @return This [FilesRuleBuilder] with the filter condition applied.
 */
public fun FilesRuleBuilder.that(predicate: FileDeclarationContext.() -> Boolean): FilesRuleBuilder =
    this.apply {
        setThat { it.predicate() }
    }

/**
 * Asserts rules on filtered files using a lambda block that provides a [FileDeclarationShouldContext] receiver.
 * Supports both imperative assertions and Boolean predicate matches.
 *
 * @param assertion The assertion block containing file validation rules or boolean predicate.
 * @return This [FilesRuleBuilder] with the assertion block registered.
 */
public fun FilesRuleBuilder.should(assertion: FileDeclarationShouldContext.() -> Any?): FilesRuleBuilder =
    this.apply {
        setShould { file, allFiles, violations ->
            /** Filter or assertion criteria for context. */
            val context = FileDeclarationShouldContext(file, allFiles, violations)

            /** Filter or assertion criteria for result. */
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add(
                    io.github.baole.konture.i18n.getMessage("file.should.failedCustomAssertion", file.declaration.name),
                )
            }
        }
    }

/**
 * Context receiver for writing declarative assertions on a [FileDeclarationContext] element.
 * Provides easy access to all element properties and custom helper assertions.
 *
 * @property element The target [FileDeclarationContext] being verified.
 * @property allFiles The complete list of file declaration contexts in this test run scope.
 * @property violations Mutable collection where assertion failure messages are appended.
 */
public class FileDeclarationShouldContext internal constructor(
    /** Filter or assertion criteria for element. */
    public val element: FileDeclarationContext,
    /** Filter or assertion criteria for all files. */
    public val allFiles: List<FileDeclarationContext>,
    /** Filter or assertion criteria for violations. */
    public val violations: MutableList<String>,
) {
    /** Filter or assertion criteria for declaration. */
    public val declaration: FileDeclaration get() = element.declaration

    /** Filter or assertion criteria for name. */
    public val name: String get() = element.declaration.name

    /** Filter or assertion criteria for package name. */
    public val packageName: String get() = element.declaration.packageName

    /** Filter or assertion criteria for imports. */
    public val imports: List<String> get() = element.declaration.imports

    /** Filter or assertion criteria for classes. */
    public val classes: List<ClassDeclaration> get() = element.declaration.classes

    /** Filter or assertion criteria for top level functions. */
    public val topLevelFunctions: List<FunctionDeclaration> get() = element.declaration.topLevelFunctions

    /** Filter or assertion criteria for top level properties. */
    public val topLevelProperties: List<PropertyDeclaration> get() = element.declaration.topLevelProperties

    /** Filter or assertion criteria for kdoc text. */
    public val kdocText: String? get() = element.declaration.kdocText

    /** Filter or assertion criteria for file path. */
    public val filePath: String get() = element.declaration.filePath

    /** Filter or assertion criteria for module path. */
    public val modulePath: String get() = element.modulePath

    /**
     * Appends a custom violation failure message to the assertion run.
     */
    public fun addViolation(message: String) {
        violations.add(message)
    }

    /**
     * Asserts [condition] is true, recording a violation with [message] when false.
     * When [message] is omitted, a default message referencing [element] is used.
     */
    public fun check(
        condition: Boolean,
        message: String? = null,
    ) {
        if (!condition) {
            addViolation(message ?: io.github.baole.konture.i18n.getMessage("file.should.failedAssertion", name))
        }
    }

    /**
     * Checks if this file contains an import matching the given predicate.
     */
    public fun hasImport(predicate: (String) -> Boolean): Boolean = imports.any(predicate)

    /**
     * Checks if this file contains any import with matching package path segment strings.
     */
    public fun hasImportContaining(vararg segments: String): Boolean =
        imports.any { importPath ->
            segments.any { segment -> importPath.contains(segment) }
        }

    /**
     * Checks if this file contains any classes matching the given predicate.
     */
    public fun containsClassWith(predicate: (ClassDeclaration) -> Boolean): Boolean = classes.any(predicate)

    /**
     * Asserts that this file does not use any wildcard star imports.
     */
    public fun assertNoWildcardImports() {
        /** Filter or assertion criteria for wildcards. */
        val wildcards = imports.filter { it.endsWith(".*") }
        if (wildcards.isNotEmpty()) {
            addViolation(
                io.github.baole.konture.i18n.getMessage(
                    "file.should.notContainWildcardImports",
                    name,
                    wildcards.joinToString(),
                ),
            )
        }
    }

    /**
     * Asserts that this file contains at most one class declaration.
     */
    public fun assertOnlyOneClassPerFile() {
        if (classes.size > 1) {
            addViolation(
                io.github.baole.konture.i18n.getMessage(
                    "file.should.containAtMostOneClass",
                    name,
                    classes.size,
                    classes.joinToString { it.name },
                ),
            )
        }
    }
}

// ==========================================
// Files Common Extra Semantic Extensions
// ==========================================

/**
 * Helper extension to check if a file imports the specified match.
 */
public fun FileDeclarationContext.hasImport(predicate: (String) -> Boolean): Boolean =
    declaration.imports.any(predicate)

/**
 * Helper extension to check if a file imports any match containing target segments.
 */
public fun FileDeclarationContext.hasImportContaining(vararg segments: String): Boolean =
    declaration.imports.any { importPath ->
        segments.any { segment -> importPath.contains(segment) }
    }

/**
 * Helper extension to check if a file contains a class matching the predicate.
 */
public fun FileDeclarationContext.containsClassWith(predicate: (ClassDeclaration) -> Boolean): Boolean =
    declaration.classes.any(predicate)

// ==========================================
// Files Context Field Delegation Extensions
// ==========================================

/** Delegates name property to the underlying [FileDeclaration]. */
public val FileDeclarationContext.name: String get() = declaration.name

/** Delegates packageName property to the underlying [FileDeclaration]. */
public val FileDeclarationContext.packageName: String get() = declaration.packageName

/** Delegates imports property to the underlying [FileDeclaration]. */
public val FileDeclarationContext.imports: List<String> get() = declaration.imports

/** Delegates classes property to the underlying [FileDeclaration]. */
public val FileDeclarationContext.classes: List<ClassDeclaration> get() = declaration.classes

/** Delegates topLevelFunctions property to the underlying [FileDeclaration]. */
public val FileDeclarationContext.topLevelFunctions: List<FunctionDeclaration> get() = declaration.topLevelFunctions

/** Delegates topLevelProperties property to the underlying [FileDeclaration]. */
public val FileDeclarationContext.topLevelProperties: List<PropertyDeclaration> get() = declaration.topLevelProperties

/** Filters files residing in a package matching [packagePattern]. */
public fun List<FileDeclarationContext>.residingInPackage(packagePattern: String): List<FileDeclarationContext> =
    filter { io.github.baole.konture.impl.PatternMatchers.matchesPackage(packagePattern, it.packageName) }

/** Filters files residing in a module matching [modulePath]. */
public fun List<FileDeclarationContext>.residingInModule(modulePath: String): List<FileDeclarationContext> =
    filter {
        it.modulePath == modulePath || io.github.baole.konture.impl.PatternMatchers.matchesModuleGlob(modulePath, it.modulePath)
    }
