/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Access the modules-level declarative assertion rule builder.
 * Allows filtering and assertion of architectural module structures and dependencies.
 */
fun Konture.modules() = ModulesRuleBuilder(projectGraph)

fun Konture.modules(sourceSets: SourceSetSelector) = ModulesRuleBuilder(projectGraph, sourceSets)

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
 * Access the slice declarative assertion rule builder.
 * Groups packages into slices by a capture-group pattern and asserts relationships between them,
 * such as cycle-freedom.
 */
fun Konture.slices() = SlicesRuleBuilder(projectGraph)

fun Konture.slices(sourceSets: SourceSetSelector) = SlicesRuleBuilder(projectGraph, sourceSets)

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
 * Synonym for [scope] representing class-level functional scope for the entire project.
 */
val Konture.classScope: KontureScope get() = scope

fun Konture.classScope(sourceSets: SourceSetSelector) = scope(sourceSets)

/**
 * Retrieves a class-level functional [KontureScope] scoped to a specific module path.
 */
fun Konture.scopeFromModule(path: String) = KontureScope.fromModule(path, projectGraph)

fun Konture.scopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
) = KontureScope.fromModule(path, projectGraph, sourceSets)

fun Konture.classScopeFromModule(path: String) = scopeFromModule(path)

fun Konture.classScopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
) = scopeFromModule(path, sourceSets)

/**
 * Retrieves a class-level functional [KontureScope] scoped to a specific package name.
 */
fun Konture.scopeFromPackage(packageName: String) = KontureScope.fromPackage(packageName, projectGraph)

fun Konture.scopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
) = KontureScope.fromPackage(packageName, projectGraph, sourceSets)

fun Konture.classScopeFromPackage(packageName: String) = scopeFromPackage(packageName)

fun Konture.classScopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
) = scopeFromPackage(packageName, sourceSets)

/**
 * Retrieves a file-level functional [KontureFileScope] representing all files in the project.
 */
val Konture.fileScope: KontureFileScope get() = KontureFileScope.fromProject(projectGraph)

fun Konture.fileScope(sourceSets: SourceSetSelector) = KontureFileScope.fromProject(projectGraph, sourceSets)

/**
 * Retrieves a file-level functional [KontureFileScope] scoped to a specific module path.
 */
fun Konture.fileScopeFromModule(path: String) = KontureFileScope.fromModule(path, projectGraph)

fun Konture.fileScopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
) = KontureFileScope.fromModule(path, projectGraph, sourceSets)

/**
 * Retrieves a file-level functional [KontureFileScope] scoped to a specific package name.
 */
fun Konture.fileScopeFromPackage(packageName: String) = KontureFileScope.fromPackage(packageName, projectGraph)

fun Konture.fileScopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
) = KontureFileScope.fromPackage(packageName, projectGraph, sourceSets)

/**
 * Retrieves a function-level functional [KontureFunctionScope] representing all functions in the project.
 */
val Konture.functionScope: KontureFunctionScope get() = KontureFunctionScope.fromProject(projectGraph)

fun Konture.functionScope(sourceSets: SourceSetSelector) = KontureFunctionScope.fromProject(projectGraph, sourceSets)

fun Konture.functionScopeFromModule(path: String) = KontureFunctionScope.fromModule(path, projectGraph)

fun Konture.functionScopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
) = KontureFunctionScope.fromModule(path, projectGraph, sourceSets)

fun Konture.functionScopeFromPackage(packageName: String) = KontureFunctionScope.fromPackage(packageName, projectGraph)

fun Konture.functionScopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
) = KontureFunctionScope.fromPackage(packageName, projectGraph, sourceSets)

/**
 * Retrieves a property-level functional [KonturePropertyScope] representing all properties in the project.
 */
val Konture.propertyScope: KonturePropertyScope get() = KonturePropertyScope.fromProject(projectGraph)

fun Konture.propertyScope(sourceSets: SourceSetSelector) = KonturePropertyScope.fromProject(projectGraph, sourceSets)

fun Konture.propertyScopeFromModule(path: String) = KonturePropertyScope.fromModule(path, projectGraph)

fun Konture.propertyScopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
) = KonturePropertyScope.fromModule(path, projectGraph, sourceSets)

fun Konture.propertyScopeFromPackage(packageName: String) = KonturePropertyScope.fromPackage(packageName, projectGraph)

fun Konture.propertyScopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
) = KonturePropertyScope.fromPackage(packageName, projectGraph, sourceSets)

/**
 * Retrieves a module-level functional [KontureModuleScope] representing all modules in the project.
 */
val Konture.moduleScope: KontureModuleScope get() = KontureModuleScope.fromProject(projectGraph)

fun Konture.moduleScope(): KontureModuleScope = KontureModuleScope.fromProject(projectGraph)

fun Konture.moduleScope(sourceSets: SourceSetSelector): KontureModuleScope =
    KontureModuleScope.fromProject(projectGraph, sourceSets)

/**
 * Retrieves a module-level functional [KontureModuleScope] scoped to a specific module path or pattern.
 */
fun Konture.moduleScopeFromModule(pattern: String): KontureModuleScope {
    val modules =
        projectGraph.getAllModules().filter {
            it.path == pattern || io.github.baole.konture.impl.PatternMatchers.matchesModuleGlob(pattern, it.path)
        }
    return KontureModuleScope(modules)
}

fun Konture.moduleScopeFromModule(
    pattern: String,
    sourceSets: SourceSetSelector,
): KontureModuleScope = KontureModuleScope.fromProject(projectGraph, sourceSets).byPath(pattern)

/**
 * Retrieves a slice-level functional [KontureSliceScope] derived from a package pattern.
 */
fun Konture.sliceScope(pattern: String): KontureSliceScope = KontureSliceScope.fromProject(pattern, projectGraph)

fun Konture.sliceScope(
    pattern: String,
    sourceSets: SourceSetSelector,
): KontureSliceScope = KontureSliceScope.fromProject(pattern, projectGraph, sourceSets)

fun Konture.sliceScopeFromModule(
    pattern: String,
    modulePath: String,
): KontureSliceScope = KontureSliceScope.fromModule(pattern, modulePath, projectGraph)

fun Konture.sliceScopeFromModule(
    pattern: String,
    modulePath: String,
    sourceSets: SourceSetSelector,
): KontureSliceScope = KontureSliceScope.fromModule(pattern, modulePath, projectGraph, sourceSets)

fun Konture.sliceScopeFromPackage(
    pattern: String,
    packageName: String,
): KontureSliceScope = KontureSliceScope.fromPackage(pattern, packageName, projectGraph)

fun Konture.sliceScopeFromPackage(
    pattern: String,
    packageName: String,
    sourceSets: SourceSetSelector,
): KontureSliceScope = KontureSliceScope.fromPackage(pattern, packageName, projectGraph, sourceSets)

// --- Block-based DSL Entry Points (Auto-Checking) ---

/**
 * Define and run module dependency rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
fun Konture.modules(block: ModulesRuleBuilder.() -> Unit) {
    ModulesRuleBuilder(projectGraph).apply(block).check()
}

fun Konture.modules(
    sourceSets: SourceSetSelector,
    block: ModulesRuleBuilder.() -> Unit,
) {
    ModulesRuleBuilder(projectGraph, sourceSets).apply(block).check()
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
 * Define and run slice rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
fun Konture.slices(block: SlicesRuleBuilder.() -> Unit) {
    SlicesRuleBuilder(projectGraph).apply(block).check()
}

fun Konture.slices(
    sourceSets: SourceSetSelector,
    block: SlicesRuleBuilder.() -> Unit,
) {
    SlicesRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Unified multi-rule block supporting modules, classes, and layered architecture validations.
 * Runs every declared suite and aggregates all violations before failing.
 *
 * Can also be called directly via the top-level [architecture] shorthand function.
 */
public fun Konture.architecture(block: KontureContext.() -> Unit) {
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
