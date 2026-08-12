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
public data class ClassDeclaration(
    /** Filter or assertion criteria for name. */
    public val name: String,
    /** Filter or assertion criteria for fq name. */
    public val fqName: String,
    /** Filter or assertion criteria for package name. */
    public val packageName: String,
    /** Filter or assertion criteria for is interface. */
    public val isInterface: Boolean,
    /** Filter or assertion criteria for is abstract. */
    public val isAbstract: Boolean,
    /** Filter or assertion criteria for annotations. */
    public val annotations: List<AnnotationDeclaration>,
    /** Filter or assertion criteria for imports. */
    public val imports: List<String>,
    /** Filter or assertion criteria for referenced types. */
    public val referencedTypes: Set<String>,
    /** Filter or assertion criteria for file path. */
    public val filePath: String,
    /** Filter or assertion criteria for visibility. */
    public val visibility: Visibility = Visibility.PUBLIC,
    /** Filter or assertion criteria for modifiers. */
    public val modifiers: Set<Modifier> = emptySet(),
    /** Filter or assertion criteria for supertypes. */
    public val supertypes: List<String> = emptyList(),
    /** Filter or assertion criteria for primary constructor. */
    public val primaryConstructor: ConstructorDeclaration? = null,
    /** Filter or assertion criteria for secondary constructors. */
    public val secondaryConstructors: List<ConstructorDeclaration> = emptyList(),
    /** Filter or assertion criteria for functions. */
    public val functions: List<FunctionDeclaration> = emptyList(),
    /** Filter or assertion criteria for properties. */
    public val properties: List<PropertyDeclaration> = emptyList(),
    /** Filter or assertion criteria for companion object. */
    public val companionObject: ClassDeclaration? = null,
    /** Filter or assertion criteria for kdoc text. */
    public val kdocText: String? = null,
    /** Filter or assertion criteria for import aliases. */
    public val importAliases: Map<String, String> = emptyMap(),
    /** Filter or assertion criteria for is enum. */
    public val isEnum: Boolean = false,
    /** Filter or assertion criteria for source line. */
    public val sourceLine: Int = -1,
)
