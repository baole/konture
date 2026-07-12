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
fun FilesRuleBuilder.that(predicate: FileDeclarationContext.() -> Boolean): FilesRuleBuilder =
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
fun FilesRuleBuilder.should(assertion: FileDeclarationShouldContext.() -> Any?): FilesRuleBuilder =
    this.apply {
        setShould { file, allFiles, violations ->
            val context = FileDeclarationShouldContext(file, allFiles, violations)
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add("File ${file.declaration.name} failed custom assertion")
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
class FileDeclarationShouldContext internal constructor(
    val element: FileDeclarationContext,
    val allFiles: List<FileDeclarationContext>,
    val violations: MutableList<String>,
) {
    val declaration get() = element.declaration
    val name get() = element.declaration.name
    val packageName get() = element.declaration.packageName
    val imports get() = element.declaration.imports
    val classes get() = element.declaration.classes
    val topLevelFunctions get() = element.declaration.topLevelFunctions
    val topLevelProperties get() = element.declaration.topLevelProperties
    val kdocText get() = element.declaration.kdocText
    val filePath get() = element.declaration.filePath
    val modulePath get() = element.modulePath

    /**
     * Appends a custom violation failure message to the assertion run.
     */
    fun addViolation(message: String) {
        violations.add(message)
    }

    /**
     * Asserts [condition] is true, recording a violation with [message] when false.
     * When [message] is omitted, a default message referencing [element] is used.
     */
    fun check(
        condition: Boolean,
        message: String? = null,
    ) {
        if (!condition) {
            addViolation(message ?: "File $name failed assertion")
        }
    }

    /**
     * Checks if this file contains an import matching the given predicate.
     */
    fun hasImport(predicate: (String) -> Boolean): Boolean = imports.any(predicate)

    /**
     * Checks if this file contains any import with matching package path segment strings.
     */
    fun hasImportContaining(vararg segments: String): Boolean =
        imports.any { importPath ->
            segments.any { segment -> importPath.contains(segment) }
        }

    /**
     * Checks if this file contains any classes matching the given predicate.
     */
    fun containsClassWith(predicate: (ClassDeclaration) -> Boolean): Boolean = classes.any(predicate)

    /**
     * Asserts that this file does not use any wildcard star imports.
     */
    fun assertNoWildcardImports() {
        if (imports.any { it.endsWith(".*") }) {
            addViolation("File $name should not contain wildcard imports")
        }
    }

    /**
     * Asserts that this file contains at most one class declaration.
     */
    fun assertOnlyOneClassPerFile() {
        if (classes.size > 1) {
            addViolation("File $name should contain at most one class, but contains ${classes.size}")
        }
    }
}

// ==========================================
// Files Common Extra Semantic Extensions
// ==========================================

/**
 * Helper extension to check if a file imports the specified match.
 */
fun FileDeclarationContext.hasImport(predicate: (String) -> Boolean): Boolean = declaration.imports.any(predicate)

/**
 * Helper extension to check if a file imports any match containing target segments.
 */
fun FileDeclarationContext.hasImportContaining(vararg segments: String): Boolean =
    declaration.imports.any { importPath ->
        segments.any { segment -> importPath.contains(segment) }
    }

/**
 * Helper extension to check if a file contains a class matching the predicate.
 */
fun FileDeclarationContext.containsClassWith(predicate: (ClassDeclaration) -> Boolean): Boolean = declaration.classes.any(predicate)

// ==========================================
// Files Context Field Delegation Extensions
// ==========================================

/** Delegates name property to the underlying [FileDeclaration]. */
val FileDeclarationContext.name: String get() = declaration.name

/** Delegates packageName property to the underlying [FileDeclaration]. */
val FileDeclarationContext.packageName: String get() = declaration.packageName

/** Delegates imports property to the underlying [FileDeclaration]. */
val FileDeclarationContext.imports: List<String> get() = declaration.imports

/** Delegates classes property to the underlying [FileDeclaration]. */
val FileDeclarationContext.classes: List<ClassDeclaration> get() = declaration.classes

/** Delegates topLevelFunctions property to the underlying [FileDeclaration]. */
val FileDeclarationContext.topLevelFunctions: List<FunctionDeclaration> get() = declaration.topLevelFunctions

/** Delegates topLevelProperties property to the underlying [FileDeclaration]. */
val FileDeclarationContext.topLevelProperties: List<PropertyDeclaration> get() = declaration.topLevelProperties
