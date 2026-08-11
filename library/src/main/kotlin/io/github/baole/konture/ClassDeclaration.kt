/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
    /** Filter or assertion criteria for name. */
    val name: String,
    /** Filter or assertion criteria for fq name. */
    val fqName: String,
    /** Filter or assertion criteria for package name. */
    val packageName: String,
    /** Filter or assertion criteria for is interface. */
    val isInterface: Boolean,
    /** Filter or assertion criteria for is abstract. */
    val isAbstract: Boolean,
    /** Filter or assertion criteria for annotations. */
    val annotations: List<AnnotationDeclaration>,
    /** Filter or assertion criteria for imports. */
    val imports: List<String>,
    /** Filter or assertion criteria for referenced types. */
    val referencedTypes: Set<String>,
    /** Filter or assertion criteria for file path. */
    val filePath: String,
    /** Filter or assertion criteria for visibility. */
    val visibility: Visibility = Visibility.PUBLIC,
    /** Filter or assertion criteria for modifiers. */
    val modifiers: Set<Modifier> = emptySet(),
    /** Filter or assertion criteria for supertypes. */
    val supertypes: List<String> = emptyList(),
    /** Filter or assertion criteria for primary constructor. */
    val primaryConstructor: ConstructorDeclaration? = null,
    /** Filter or assertion criteria for secondary constructors. */
    val secondaryConstructors: List<ConstructorDeclaration> = emptyList(),
    /** Filter or assertion criteria for functions. */
    val functions: List<FunctionDeclaration> = emptyList(),
    /** Filter or assertion criteria for properties. */
    val properties: List<PropertyDeclaration> = emptyList(),
    /** Filter or assertion criteria for companion object. */
    val companionObject: ClassDeclaration? = null,
    /** Filter or assertion criteria for kdoc text. */
    val kdocText: String? = null,
    /** Filter or assertion criteria for import aliases. */
    val importAliases: Map<String, String> = emptyMap(),
    /** Filter or assertion criteria for is enum. */
    val isEnum: Boolean = false,
    /** Filter or assertion criteria for source line. */
    val sourceLine: Int = -1,
)
