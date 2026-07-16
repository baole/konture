/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.DependencyGraphModel
import io.github.baole.konture.impl.ModuleKey

/**
 * Represents the complete structural graph of the multi-project/composite build.
 * It provides core querying capabilities, circular dependency verification, and access to the parsed
 * class/declaration hierarchy of the entire workspace.
 *
 * @property builds Map of build ID to the list of modules contained inside that build.
 */
data class ProjectGraph(
    val builds: Map<String, List<Module>>,
    private val externalDependenciesLoader: () -> DependencyGraphModel = {
        DependencyGraphModel()
    },
) {
    val externalDependencies: DependencyGraphModel by lazy {
        externalDependenciesLoader()
    }
    private val moduleMap: Map<ModuleKey, Module> =
        builds
            .flatMap { (buildId, modules) ->
                modules.map { ModuleKey(buildId, it.path) to it }
            }.toMap()

    /**
     * Looks up a module within the project graph by its build ID and Gradle project path.
     *
     * @param buildId The build containing the module (e.g., ":" for the root build).
     * @param path The Gradle project path (e.g., ":domain" or ":feature:profile").
     * @return The matching [Module] if found, or null.
     */
    fun findModule(
        buildId: String,
        path: String,
    ): Module? = moduleMap[ModuleKey(buildId, path)]

    /**
     * Returns a flat list of all modules across all builds in this graph.
     */
    fun getAllModules(): List<Module> = moduleMap.values.toList()

    /**
     * Detects dependency cycles in the module graph and throws an [AssertionError] if a cycle is found.
     * The verification is performed using a Depth-First Search (DFS) traversal.
     *
     * @throws AssertionError if any circular dependency is detected.
     */
    fun assertNoCycles() {
        val visited = mutableSetOf<ModuleKey>()
        val recursionStack = mutableSetOf<ModuleKey>()
        val cycle = mutableListOf<ModuleKey>()

        for (key in moduleMap.keys) {
            if (key !in visited) {
                dfs(key, visited, recursionStack, cycle)
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun dfs(
        key: ModuleKey,
        visited: MutableSet<ModuleKey>,
        recursionStack: MutableSet<ModuleKey>,
        cycle: MutableList<ModuleKey>,
    ): Boolean {
        visited.add(key)
        recursionStack.add(key)
        cycle.add(key)

        val module = moduleMap[key]
        if (module != null) {
            for (dep in module.dependencies) {
                val depKey = ModuleKey(dep.targetBuildId, dep.targetPath)
                if (depKey in recursionStack) {
                    cycle.add(depKey)
                    val cycleStartIndex = cycle.indexOf(depKey)
                    val cyclePath =
                        cycle
                            .subList(
                                cycleStartIndex,
                                cycle.size,
                            ).joinToString(" -> ") { "${it.buildId}${it.path}" }
                    throw AssertionError("Circular dependency detected in project graph: $cyclePath")
                }
                if (depKey !in visited) {
                    if (dfs(depKey, visited, recursionStack, cycle)) return true
                }
            }
        }

        recursionStack.remove(key)
        cycle.removeAt(cycle.size - 1)
        return false
    }

    companion object {
        private var defaultGraph: ProjectGraph? = null

        /**
         * Checks if the default ProjectGraph is initialized.
         */
        internal fun isDefaultInitialized(): Boolean = defaultGraph != null

        /**
         * Sets the default ProjectGraph for the current JVM runtime session.
         */
        internal fun setDefault(graph: ProjectGraph) {
            defaultGraph = graph
        }

        /**
         * Retrieves the default ProjectGraph for the JVM session.
         *
         * @throws IllegalStateException if the default graph has not been initialized.
         */
        internal fun getDefault(): ProjectGraph =
            defaultGraph ?: throw IllegalStateException(
                "Default ProjectGraph has not been initialized. " +
                    "Make sure to apply the plugin or load a graph first.",
            )
    }
}

/**
 * Represents a single Gradle module/project and all its structural and source declarations.
 *
 * @property buildId The ID of the build containing this module.
 * @property path The Gradle project path (e.g. `:core:database`).
 * @property projectDir The build-root-relative directory path.
 * @property appliedPlugins List of plugin IDs applied to this module (e.g., `kotlin-jvm`, `android-library`).
 * @property sourceSets The source sets present in this module.
 * @property dependencies Declared project dependencies of this module.
 * @property classes Parsed Kotlin class declarations contained inside this module's production source sets.
 */
data class Module(
    val buildId: String,
    val path: String,
    val projectDir: String,
    val appliedPlugins: List<String>,
    val sourceSets: List<SourceSet>,
    val dependencies: List<Dependency>,
    val files: List<FileDeclaration> = emptyList(),
) {
    val classes: List<ClassDeclaration> get() = files.flatMap { it.classes }
}

/**
 * Represents a source set within a module at test runtime.
 *
 * @property name Name of the source set (e.g., `main`, `test`).
 * @property kind Technological type of the source set, i.e., "KOTLIN_JVM", "ANDROID_VARIANT", or "KMP".
 * @property production True if this represents a production source set, false otherwise.
 * @property srcDirs Source directories mapped to this source set.
 * @property kotlinFiles List of relative Kotlin file paths.
 * @property platforms List of target platforms associated with this source set (e.g., "jvm", "js", "native").
 */
data class SourceSet(
    val name: String,
    // "KOTLIN_JVM", "ANDROID_VARIANT", "KMP"
    val kind: String,
    val production: Boolean,
    val srcDirs: List<String>,
    val kotlinFiles: List<String>,
    val platforms: List<String> = emptyList(),
)

/**
 * Represents a declared project-to-project dependency edge at runtime.
 *
 * @property configuration The Gradle configuration where the dependency was declared (e.g., `api`, `implementation`).
 * @property targetBuildId The target build ID of the dependent project.
 * @property targetPath The Gradle project path of the dependent project (e.g., `:domain`).
 */
data class Dependency(
    val configuration: String,
    val targetBuildId: String,
    val targetPath: String,
)

enum class Visibility {
    PUBLIC,
    INTERNAL,
    PROTECTED,
    PRIVATE,
}

enum class Modifier {
    SEALED,
    OPEN,
    ABSTRACT,
    DATA,
    VALUE,
    INNER,
    INLINE,
    SUSPEND,
    COMPANION,
    OBJECT,
    EXPECT,
    ACTUAL,
    CONST,
    LATEINIT,
}

data class ParameterDeclaration(
    val name: String,
    val type: String,
    val hasDefaultValue: Boolean,
    val annotations: List<AnnotationDeclaration>,
)

data class ConstructorDeclaration(
    val visibility: Visibility,
    val parameters: List<ParameterDeclaration>,
    val annotations: List<AnnotationDeclaration>,
)

data class FunctionDeclaration(
    val name: String,
    val visibility: Visibility,
    val modifiers: Set<Modifier>,
    val returnType: String,
    val parameters: List<ParameterDeclaration>,
    val annotations: List<AnnotationDeclaration>,
    val kdocText: String?,
    val isExtension: Boolean,
)

data class PropertyDeclaration(
    val name: String,
    val visibility: Visibility,
    val modifiers: Set<Modifier>,
    val type: String,
    val isVal: Boolean,
    val annotations: List<AnnotationDeclaration>,
    val kdocText: String?,
    val isExtension: Boolean = false,
) {
    val isVar: Boolean get() = !isVal
}

data class AnnotationArgumentDeclaration(
    val name: String?,
    val value: String,
)

data class FileDeclaration(
    val name: String,
    val packageName: String,
    val imports: List<String> = emptyList(),
    val classes: List<ClassDeclaration> = emptyList(),
    val topLevelFunctions: List<FunctionDeclaration> = emptyList(),
    val topLevelProperties: List<PropertyDeclaration> = emptyList(),
    val kdocText: String? = null,
    val filePath: String = "",
    val importAliases: Map<String, String> = emptyMap(),
)

/**
 * Represents a parsed Kotlin class, interface, or object declaration.
 *
 * @property name The simple name of the class (e.g., `GetUserUseCase`).
 * @property fqName The fully qualified name of the class (e.g., `com.acme.domain.GetUserUseCase`).
 * @property packageName The package name of the class (e.g., `com.acme.domain`).
 * @property isInterface True if this declaration represents an interface.
 * @property isAbstract True if this declaration is marked with the `abstract` modifier.
 * @property isEnum True if this declaration represents an enum class.
 * @property annotations List of annotations declared on this class.
 * @property imports List of exact import directives in the file containing this class.
 * @property importAliases Map of local aliases to their fully-qualified names.
 * @property referencedTypes Set of simple types referenced/accessed in this class body (used for dependency inference).
 * @property filePath The absolute path of the file containing this class.
 */
data class ClassDeclaration(
    val name: String,
    val fqName: String,
    val packageName: String,
    val isInterface: Boolean,
    val isAbstract: Boolean,
    val annotations: List<AnnotationDeclaration>,
    val imports: List<String>,
    val referencedTypes: Set<String>,
    val filePath: String,
    val visibility: Visibility = Visibility.PUBLIC,
    val modifiers: Set<Modifier> = emptySet(),
    val supertypes: List<String> = emptyList(),
    val primaryConstructor: ConstructorDeclaration? = null,
    val secondaryConstructors: List<ConstructorDeclaration> = emptyList(),
    val functions: List<FunctionDeclaration> = emptyList(),
    val properties: List<PropertyDeclaration> = emptyList(),
    val companionObject: ClassDeclaration? = null,
    val kdocText: String? = null,
    val importAliases: Map<String, String> = emptyMap(),
    val isEnum: Boolean = false,
)

/**
 * Represents a parsed Kotlin annotation declared on a class.
 *
 * @property name The simple name of the annotation (e.g., `UseCase`).
 * @property fqName The fully qualified name of the annotation if resolvable, or its simple name (e.g., `com.acme.annotations.UseCase`).
 * @property arguments List of arguments declared on the annotation.
 */
data class AnnotationDeclaration(
    val name: String,
    val fqName: String,
    val arguments: List<AnnotationArgumentDeclaration> = emptyList(),
)
