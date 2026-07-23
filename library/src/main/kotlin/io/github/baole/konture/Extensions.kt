/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Access the modules-level declarative assertion rule builder.
 * Allows filtering and assertion of architectural module structures and dependencies.
 */
fun Konture.modules() = ModulesRuleBuilder(projectGraph)

/**
 * Access the class-level declarative assertion rule builder.
 * Allows filtering and assertion of class structure, modifiers, annotations, visibility, and dependencies.
 */
fun Konture.classes() = ClassesRuleBuilder(projectGraph)

fun Konture.classes(sourceSets: SourceSetSelector) = ClassesRuleBuilder(projectGraph, sourceSets)

/**
 * Access the layered-architecture rule builder.
 * Allows defining high-level layers and declaring directional access constraints between them.
 */
fun Konture.layeredArchitecture() = LayeredArchitectureBuilder(projectGraph)

/**
 * Access the function-level declarative assertion rule builder.
 * Allows filtering and assertion of function declarations (both top-level and nested member functions).
 */
fun Konture.functions() = FunctionsRuleBuilder(projectGraph)

fun Konture.functions(sourceSets: SourceSetSelector) = FunctionsRuleBuilder(projectGraph, sourceSets)

/**
 * Access the property-level declarative assertion rule builder.
 * Allows filtering and assertion of property declarations (both top-level and class properties).
 */
fun Konture.properties() = PropertiesRuleBuilder(projectGraph)

fun Konture.properties(sourceSets: SourceSetSelector) = PropertiesRuleBuilder(projectGraph, sourceSets)

/**
 * Access the file-level declarative assertion rule builder.
 * Allows filtering and assertion of source files and their imports, package, or wildcard usages.
 */
fun Konture.files() = FilesRuleBuilder(projectGraph)

fun Konture.files(sourceSets: SourceSetSelector) = FilesRuleBuilder(projectGraph, sourceSets)

/**
 * Verifies that there are no package or module dependency cycles in the project.
 * Throws an [AssertionError] if a cycle is detected.
 */
fun Konture.assertNoCycles() = projectGraph.assertNoCycles(includeTestConfigurations = false)

/**
 * Verifies that there are no package or module dependency cycles in the project.
 * Throws an [AssertionError] if a cycle is detected.
 *
 * @param includeTestConfigurations if true, test-related dependency configurations will also be analyzed
 * for cycles. If false, they are skipped.
 */
fun Konture.assertNoCycles(includeTestConfigurations: Boolean) = projectGraph.assertNoCycles(includeTestConfigurations)

// Functional scope entry-points

/**
 * Retrieves a class-level functional [KontureScope] representing the entire project.
 */
val Konture.scope: KontureScope get() = KontureScope.fromProject(projectGraph)

fun Konture.scope(sourceSets: SourceSetSelector) = KontureScope.fromProject(projectGraph, sourceSets)

/**
 * Retrieves a class-level functional [KontureScope] scoped to a specific module path.
 */
fun Konture.scopeFromModule(path: String) = KontureScope.fromModule(path, projectGraph)

fun Konture.scopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
) = KontureScope.fromModule(path, projectGraph, sourceSets)

/**
 * Retrieves a class-level functional [KontureScope] scoped to a specific package name.
 */
fun Konture.scopeFromPackage(packageName: String) = KontureScope.fromPackage(packageName, projectGraph)

fun Konture.scopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
) = KontureScope.fromPackage(packageName, projectGraph, sourceSets)

/**
 * Retrieves a file-level functional [KontureFileScope] representing all files in the project.
 */
val Konture.fileScope: KontureFileScope get() = KontureFileScope.fromProject(projectGraph)

fun Konture.fileScope(sourceSets: SourceSetSelector) = KontureFileScope.fromProject(projectGraph, sourceSets)

/**
 * Retrieves a file-level functional [KontureFileScope] scoped to a specific module path.
 */
fun Konture.fileScopeFromModule(path: String) = KontureFileScope.fromModule(path, projectGraph)

/**
 * Retrieves a file-level functional [KontureFileScope] scoped to a specific package name.
 */
fun Konture.fileScopeFromPackage(packageName: String) = KontureFileScope.fromPackage(packageName, projectGraph)

// --- Block-based DSL Entry Points (Auto-Checking) ---

/**
 * Define and run module dependency rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
fun Konture.modules(block: ModulesRuleBuilder.() -> Unit) {
    ModulesRuleBuilder(projectGraph).apply(block).check()
}

/**
 * Define and run class dependency/structural rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
fun Konture.classes(block: ClassesRuleBuilder.() -> Unit) {
    ClassesRuleBuilder(projectGraph).apply(block).check()
}

fun Konture.classes(
    sourceSets: SourceSetSelector,
    block: ClassesRuleBuilder.() -> Unit,
) {
    ClassesRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Define and run function dependency/structural rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
fun Konture.functions(block: FunctionsRuleBuilder.() -> Unit) {
    FunctionsRuleBuilder(projectGraph).apply(block).check()
}

fun Konture.functions(
    sourceSets: SourceSetSelector,
    block: FunctionsRuleBuilder.() -> Unit,
) {
    FunctionsRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Define and run property dependency/structural rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
fun Konture.properties(block: PropertiesRuleBuilder.() -> Unit) {
    PropertiesRuleBuilder(projectGraph).apply(block).check()
}

fun Konture.properties(
    sourceSets: SourceSetSelector,
    block: PropertiesRuleBuilder.() -> Unit,
) {
    PropertiesRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Define and run source file dependency/structural rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
fun Konture.files(block: FilesRuleBuilder.() -> Unit) {
    FilesRuleBuilder(projectGraph).apply(block).check()
}

fun Konture.files(
    sourceSets: SourceSetSelector,
    block: FilesRuleBuilder.() -> Unit,
) {
    FilesRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Unified multi-rule block supporting modules, classes, and layered architecture validations.
 * Runs every declared suite and aggregates all violations before failing.
 */
fun Konture.architecture(block: KontureContext.() -> Unit) {
    KontureContext(projectGraph).apply(block).verifyAll()
}

/**
 * Define and run a nested, type-safe layered architecture specification.
 * Automatically checks the layered rules at the end of the block.
 */
fun Konture.layered(block: LayeredArchitectureDsl.() -> Unit) {
    val dsl = LayeredArchitectureDsl(projectGraph).apply(block)
    dsl.verify()
}
