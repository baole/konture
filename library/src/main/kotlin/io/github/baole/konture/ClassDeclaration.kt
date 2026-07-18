/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

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
