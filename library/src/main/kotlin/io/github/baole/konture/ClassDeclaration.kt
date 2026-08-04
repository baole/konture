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
 * @property annotations List of annotations declared on this class.
 * @property imports List of exact import directives in the file containing this class.
 * @property referencedTypes Set of simple types referenced/accessed in this class body (used for dependency inference).
 * @property filePath The absolute path of the file containing this class.
 * @property visibility The declared visibility modifier of this class.
 * @property modifiers The set of modifiers declared on this class (e.g. `SEALED`, `OPEN`, `DATA`).
 * @property supertypes List of supertype names extended or implemented by this class.
 * @property primaryConstructor The primary constructor declaration of this class, or null if omitted.
 * @property secondaryConstructors List of secondary constructors declared in this class.
 * @property functions List of member functions declared directly inside this class.
 * @property properties List of properties declared directly inside this class.
 * @property companionObject The companion object declaration of this class, or null if absent.
 * @property kdocText Raw text content of the KDoc block attached to this class, or null if un-documented.
 * @property importAliases Map of local import aliases to their fully-qualified names.
 * @property isEnum True if this declaration represents an enum class.
 * @property sourceLine 1-indexed source line number where this class declaration begins.
 * @property modulePath The module path containing this class declaration.
 * @property usages List of code usages (calls and references) originating within this class body.
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
    val sourceLine: Int = -1,
    val modulePath: String = "",
    val usages: List<SourceUsage> = emptyList(),
)
