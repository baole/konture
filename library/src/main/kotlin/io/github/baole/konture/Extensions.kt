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
public fun Konture.modules(): ModulesRuleBuilder = ModulesRuleBuilder(projectGraph)

/** Filter or assertion criteria for modules. */
public fun Konture.modules(sourceSets: SourceSetSelector): ModulesRuleBuilder =
    ModulesRuleBuilder(projectGraph, sourceSets)

/**
 * Access the class-level declarative assertion rule builder.
 * Allows filtering and assertion of class structure, modifiers, annotations, visibility, and dependencies.
 */
public fun Konture.classes(): ClassesRuleBuilder = ClassesRuleBuilder(projectGraph)

/** Filter or assertion criteria for classes. */
public fun Konture.classes(sourceSets: SourceSetSelector): ClassesRuleBuilder =
    ClassesRuleBuilder(projectGraph, sourceSets)

/**
 * Access the layered-architecture rule builder.
 * Allows defining high-level layers and declaring directional access constraints between them.
 */
public fun Konture.layeredArchitecture(): LayeredArchitectureBuilder = LayeredArchitectureBuilder(projectGraph)

/**
 * Access the function-level declarative assertion rule builder.
 * Allows filtering and assertion of function declarations (both top-level and nested member functions).
 */
public fun Konture.functions(): FunctionsRuleBuilder = FunctionsRuleBuilder(projectGraph)

/** Filter or assertion criteria for functions. */
public fun Konture.functions(sourceSets: SourceSetSelector): FunctionsRuleBuilder =
    FunctionsRuleBuilder(projectGraph, sourceSets)

/**
 * Access the property-level declarative assertion rule builder.
 * Allows filtering and assertion of property declarations (both top-level and class properties).
 */
public fun Konture.properties(): PropertiesRuleBuilder = PropertiesRuleBuilder(projectGraph)

/** Filter or assertion criteria for properties. */
public fun Konture.properties(sourceSets: SourceSetSelector): PropertiesRuleBuilder =
    PropertiesRuleBuilder(projectGraph, sourceSets)

/**
 * Access the file-level declarative assertion rule builder.
 * Allows filtering and assertion of source files and their imports, package, or wildcard usages.
 */
public fun Konture.files(): FilesRuleBuilder = FilesRuleBuilder(projectGraph)

/** Filter or assertion criteria for files. */
public fun Konture.files(sourceSets: SourceSetSelector): FilesRuleBuilder = FilesRuleBuilder(projectGraph, sourceSets)

/**
 * Access the slice declarative assertion rule builder.
 * Groups packages into slices by a capture-group pattern and asserts relationships between them,
 * such as cycle-freedom.
 */
public fun Konture.slices(): SlicesRuleBuilder = SlicesRuleBuilder(projectGraph)

/** Filter or assertion criteria for slices. */
public fun Konture.slices(sourceSets: SourceSetSelector): SlicesRuleBuilder =
    SlicesRuleBuilder(projectGraph, sourceSets)

/**
 * Verifies that there are no module dependency cycles in the project.
 * Throws an [AssertionError] if a cycle is detected.
 */
public fun Konture.assertNoCycles(): Unit = projectGraph.assertNoCycles(includeTestConfigurations = false)

/**
 * Verifies that there are no module dependency cycles in the project.
 * Throws an [AssertionError] if a cycle is detected.
 *
 * @param includeTestConfigurations if true, test-related dependency configurations will also be analyzed
 * for cycles. If false, they are skipped.
 */
public fun Konture.assertNoCycles(includeTestConfigurations: Boolean): Unit =
    projectGraph.assertNoCycles(includeTestConfigurations)

// Functional scope entry-points

/**
 * Retrieves a class-level functional [KontureScope] representing the entire project.
 */
public val Konture.scope: KontureScope get() = KontureScope.fromProject(projectGraph)

/** Filter or assertion criteria for scope. */
public fun Konture.scope(sourceSets: SourceSetSelector): KontureScope =
    KontureScope.fromProject(projectGraph, sourceSets)

/**
 * Synonym for [scope] representing class-level functional scope for the entire project.
 */
public val Konture.classScope: KontureScope get() = scope

/** Filter or assertion criteria for class scope. */
public fun Konture.classScope(sourceSets: SourceSetSelector): KontureScope = scope(sourceSets)

/**
 * Retrieves a class-level functional [KontureScope] scoped to a specific module path.
 */
public fun Konture.scopeFromModule(path: String): KontureScope = KontureScope.fromModule(path, projectGraph)

/** Filter or assertion criteria for scope from module. */
public fun Konture.scopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
): KontureScope = KontureScope.fromModule(path, projectGraph, sourceSets)

/** Filter or assertion criteria for class scope from module. */
public fun Konture.classScopeFromModule(path: String): KontureScope = scopeFromModule(path)

/** Filter or assertion criteria for class scope from module. */
public fun Konture.classScopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
): KontureScope = scopeFromModule(path, sourceSets)

/** Retrieves a class-level functional [KontureScope] scoped to a specific package name. */
public fun Konture.scopeFromPackage(packageName: String): KontureScope =
    KontureScope.fromPackage(packageName, projectGraph)

/** Retrieves a class-level functional [KontureScope] scoped to a specific package name and source set. */
public fun Konture.scopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
): KontureScope = KontureScope.fromPackage(packageName, projectGraph, sourceSets)

/** Synonym for [scopeFromPackage] retrieving a class-level functional [KontureScope]. */
public fun Konture.classScopeFromPackage(packageName: String): KontureScope = scopeFromPackage(packageName)

/** Synonym for [scopeFromPackage] retrieving a class-level functional [KontureScope] with source set filter. */
public fun Konture.classScopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
): KontureScope = scopeFromPackage(packageName, sourceSets)

/**
 * Retrieves a file-level functional [KontureFileScope] representing all files in the project.
 */
public val Konture.fileScope: KontureFileScope get() = KontureFileScope.fromProject(projectGraph)

/** Filter or assertion criteria for file scope. */
public fun Konture.fileScope(sourceSets: SourceSetSelector): KontureFileScope =
    KontureFileScope.fromProject(projectGraph, sourceSets)

/**
 * Retrieves a file-level functional [KontureFileScope] scoped to a specific module path.
 */
public fun Konture.fileScopeFromModule(path: String): KontureFileScope = KontureFileScope.fromModule(path, projectGraph)

/** Filter or assertion criteria for file scope from module. */
public fun Konture.fileScopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
): KontureFileScope = KontureFileScope.fromModule(path, projectGraph, sourceSets)

/**
 * Retrieves a file-level functional [KontureFileScope] scoped to a specific package name.
 */
public fun Konture.fileScopeFromPackage(packageName: String): KontureFileScope =
    KontureFileScope.fromPackage(packageName, projectGraph)

/** Filter or assertion criteria for file scope from package. */
public fun Konture.fileScopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
): KontureFileScope = KontureFileScope.fromPackage(packageName, projectGraph, sourceSets)

/**
 * Retrieves a function-level functional [KontureFunctionScope] representing all functions in the project.
 */
public val Konture.functionScope: KontureFunctionScope get() = KontureFunctionScope.fromProject(projectGraph)

/** Filter or assertion criteria for function scope. */
public fun Konture.functionScope(sourceSets: SourceSetSelector): KontureFunctionScope =
    KontureFunctionScope.fromProject(projectGraph, sourceSets)

/** Retrieves a function-level functional [KontureFunctionScope] scoped to a specific module path. */
public fun Konture.functionScopeFromModule(path: String): KontureFunctionScope =
    KontureFunctionScope.fromModule(path, projectGraph)

/** Retrieves a function-level functional [KontureFunctionScope] scoped to a specific module path and source set. */
public fun Konture.functionScopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
): KontureFunctionScope = KontureFunctionScope.fromModule(path, projectGraph, sourceSets)

/** Retrieves a function-level functional [KontureFunctionScope] scoped to a specific package name. */
public fun Konture.functionScopeFromPackage(packageName: String): KontureFunctionScope =
    KontureFunctionScope.fromPackage(packageName, projectGraph)

/** Retrieves a function-level functional [KontureFunctionScope] scoped to a specific package name and source set. */
public fun Konture.functionScopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
): KontureFunctionScope = KontureFunctionScope.fromPackage(packageName, projectGraph, sourceSets)

/**
 * Retrieves a property-level functional [KonturePropertyScope] representing all properties in the project.
 */
public val Konture.propertyScope: KonturePropertyScope get() = KonturePropertyScope.fromProject(projectGraph)

/** Filter or assertion criteria for property scope. */
public fun Konture.propertyScope(sourceSets: SourceSetSelector): KonturePropertyScope =
    KonturePropertyScope.fromProject(projectGraph, sourceSets)

/** Retrieves a property-level functional [KonturePropertyScope] scoped to a specific module path. */
public fun Konture.propertyScopeFromModule(path: String): KonturePropertyScope =
    KonturePropertyScope.fromModule(path, projectGraph)

/** Retrieves a property-level functional [KonturePropertyScope] scoped to a specific module path and source set. */
public fun Konture.propertyScopeFromModule(
    path: String,
    sourceSets: SourceSetSelector,
): KonturePropertyScope = KonturePropertyScope.fromModule(path, projectGraph, sourceSets)

/** Retrieves a property-level functional [KonturePropertyScope] scoped to a specific package name. */
public fun Konture.propertyScopeFromPackage(packageName: String): KonturePropertyScope =
    KonturePropertyScope.fromPackage(packageName, projectGraph)

/** Retrieves a property-level functional [KonturePropertyScope] scoped to a specific package name and source set. */
public fun Konture.propertyScopeFromPackage(
    packageName: String,
    sourceSets: SourceSetSelector,
): KonturePropertyScope = KonturePropertyScope.fromPackage(packageName, projectGraph, sourceSets)

/**
 * Retrieves a module-level functional [KontureModuleScope] representing all modules in the project.
 */
public val Konture.moduleScope: KontureModuleScope get() = KontureModuleScope.fromProject(projectGraph)

/** Filter or assertion criteria for module scope. */
public fun Konture.moduleScope(): KontureModuleScope = KontureModuleScope.fromProject(projectGraph)

/** Filter or assertion criteria for module scope. */
public fun Konture.moduleScope(sourceSets: SourceSetSelector): KontureModuleScope =
    KontureModuleScope.fromProject(projectGraph, sourceSets)

/**
 * Retrieves a module-level functional [KontureModuleScope] scoped to a specific module path or pattern.
 */
public fun Konture.moduleScopeFromModule(pattern: String): KontureModuleScope {
    /** Filter or assertion criteria for modules. */
    val modules =
        projectGraph.getAllModules().filter {
            it.path == pattern || io.github.baole.konture.impl.PatternMatchers.matchesModuleGlob(pattern, it.path)
        }
    return KontureModuleScope(modules)
}

/** Filter or assertion criteria for module scope from module. */
public fun Konture.moduleScopeFromModule(
    pattern: String,
    sourceSets: SourceSetSelector,
): KontureModuleScope = KontureModuleScope.fromProject(projectGraph, sourceSets).byPath(pattern)

/**
 * Retrieves a slice-level functional [KontureSliceScope] derived from a package pattern.
 */
public fun Konture.sliceScope(pattern: String): KontureSliceScope = KontureSliceScope.fromProject(pattern, projectGraph)

/** Filter or assertion criteria for slice scope. */
public fun Konture.sliceScope(
    pattern: String,
    sourceSets: SourceSetSelector,
): KontureSliceScope = KontureSliceScope.fromProject(pattern, projectGraph, sourceSets)

/** Retrieves a slice-level functional [KontureSliceScope] derived from a module pattern. */
public fun Konture.sliceScopeFromModule(
    pattern: String,
    modulePath: String,
): KontureSliceScope = KontureSliceScope.fromModule(pattern, modulePath, projectGraph)

/** Retrieves a slice-level functional [KontureSliceScope] derived from a module pattern and source set. */
public fun Konture.sliceScopeFromModule(
    pattern: String,
    modulePath: String,
    sourceSets: SourceSetSelector,
): KontureSliceScope = KontureSliceScope.fromModule(pattern, modulePath, projectGraph, sourceSets)

/** Retrieves a slice-level functional [KontureSliceScope] derived from a package pattern. */
public fun Konture.sliceScopeFromPackage(
    pattern: String,
    packageName: String,
): KontureSliceScope = KontureSliceScope.fromPackage(pattern, packageName, projectGraph)

/** Retrieves a slice-level functional [KontureSliceScope] derived from a package pattern and source set. */
public fun Konture.sliceScopeFromPackage(
    pattern: String,
    packageName: String,
    sourceSets: SourceSetSelector,
): KontureSliceScope = KontureSliceScope.fromPackage(pattern, packageName, projectGraph, sourceSets)

// --- Block-based DSL Entry Points (Auto-Checking) ---

/**
 * Define and run module dependency rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
public fun Konture.modules(block: ModulesRuleBuilder.() -> Unit) {
    ModulesRuleBuilder(projectGraph).apply(block).check()
}

/** Filter or assertion criteria for modules. */
public fun Konture.modules(
    sourceSets: SourceSetSelector,
    block: ModulesRuleBuilder.() -> Unit,
) {
    ModulesRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Define and run class dependency/structural rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
public fun Konture.classes(block: ClassesRuleBuilder.() -> Unit) {
    ClassesRuleBuilder(projectGraph).apply(block).check()
}

/** Filter or assertion criteria for classes. */
public fun Konture.classes(
    sourceSets: SourceSetSelector,
    block: ClassesRuleBuilder.() -> Unit,
) {
    ClassesRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Define and run function dependency/structural rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
public fun Konture.functions(block: FunctionsRuleBuilder.() -> Unit) {
    FunctionsRuleBuilder(projectGraph).apply(block).check()
}

/** Filter or assertion criteria for functions. */
public fun Konture.functions(
    sourceSets: SourceSetSelector,
    block: FunctionsRuleBuilder.() -> Unit,
) {
    FunctionsRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Define and run property dependency/structural rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
public fun Konture.properties(block: PropertiesRuleBuilder.() -> Unit) {
    PropertiesRuleBuilder(projectGraph).apply(block).check()
}

/** Filter or assertion criteria for properties. */
public fun Konture.properties(
    sourceSets: SourceSetSelector,
    block: PropertiesRuleBuilder.() -> Unit,
) {
    PropertiesRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Define and run source file dependency/structural rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
public fun Konture.files(block: FilesRuleBuilder.() -> Unit) {
    FilesRuleBuilder(projectGraph).apply(block).check()
}

/** Filter or assertion criteria for files. */
public fun Konture.files(
    sourceSets: SourceSetSelector,
    block: FilesRuleBuilder.() -> Unit,
) {
    FilesRuleBuilder(projectGraph, sourceSets).apply(block).check()
}

/**
 * Define and run slice rules inside a block-based DSL context.
 * Automatically checks the rules at the end of the block.
 */
public fun Konture.slices(block: SlicesRuleBuilder.() -> Unit) {
    SlicesRuleBuilder(projectGraph).apply(block).check()
}

/** Filter or assertion criteria for slices. */
public fun Konture.slices(
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
public fun Konture.layered(block: LayeredArchitectureDsl.() -> Unit) {
    /** Filter or assertion criteria for dsl. */
    val dsl = LayeredArchitectureDsl(projectGraph).apply(block)
    dsl.verify()
}
