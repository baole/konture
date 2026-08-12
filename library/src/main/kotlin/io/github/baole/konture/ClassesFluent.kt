/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

// ==========================================
// Classes Rule Builder Fluent DSL
// ==========================================

/**
 * Filters classes in this rule using a concise lambda predicate evaluated on each [ClassDeclaration].
 *
 * @param predicate The filter criteria block executed on the [ClassDeclaration].
 * @return This [ClassesRuleBuilder] with the filter condition applied.
 */
public fun ClassesRuleBuilder.that(predicate: ClassDeclaration.() -> Boolean): ClassesRuleBuilder =
    this.apply {
        setThat { it.predicate() }
    }

/**
 * Asserts rules on filtered classes using a lambda block that provides a [ClassDeclarationShouldContext] receiver.
 *
 * This function supports both:
 * 1. Imperative assertions: blocks containing explicit checks or assertions (e.g. `assertAnnotationOf("MyAnnotation")`).
 * 2. Declarative matching: blocks returning a [Boolean] predicate (e.g. `isInterface`). If the block returns false,
 *    a violation is recorded automatically.
 *
 * @param assertion The assertion block containing class validation rules or boolean predicate.
 * @return This [ClassesRuleBuilder] with the assertion registered.
 */
public fun ClassesRuleBuilder.should(assertion: ClassDeclarationShouldContext.() -> Any?): ClassesRuleBuilder =
    this.apply {
        setShould { cls, allClasses, violations ->
            /** Filter or assertion criteria for context. */
            val context = ClassDeclarationShouldContext(cls, allClasses, violations)

            /** Filter or assertion criteria for result. */
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add(
                    io.github.baole.konture.i18n.getMessage("class.should.failedCustomAssertion", cls.fqName),
                )
            }
        }
    }

/**
 * Context receiver for writing declarative assertions on a [ClassDeclaration] element.
 * Provides easy access to all element properties and custom helper assertions.
 *
 * @property element The target [ClassDeclaration] being verified.
 * @property allClasses The complete list of class declarations in this test run scope.
 * @property violations Mutable collection where assertion failure messages are appended.
 */
public class ClassDeclarationShouldContext internal constructor(
    /** Filter or assertion criteria for element. */
    public val element: ClassDeclaration,
    /** Filter or assertion criteria for all classes. */
    public val allClasses: List<ClassDeclaration>,
    /** Filter or assertion criteria for violations. */
    public val violations: MutableList<String>,
) {
    /** Filter or assertion criteria for name. */
    public val name: String get() = element.name

    /** Filter or assertion criteria for fq name. */
    public val fqName: String get() = element.fqName

    /** Filter or assertion criteria for package name. */
    public val packageName: String get() = element.packageName

    /** Filter or assertion criteria for is interface. */
    public val isInterface: Boolean get() = element.isInterface

    /** Filter or assertion criteria for is abstract. */
    public val isAbstract: Boolean get() = element.isAbstract

    /** Filter or assertion criteria for annotations. */
    public val annotations: List<AnnotationDeclaration> get() = element.annotations

    /** Filter or assertion criteria for imports. */
    public val imports: List<String> get() = element.imports

    /** Filter or assertion criteria for referenced types. */
    public val referencedTypes: Set<String> get() = element.referencedTypes

    /** Filter or assertion criteria for file path. */
    public val filePath: String get() = element.filePath

    /** Filter or assertion criteria for visibility. */
    public val visibility: Visibility get() = element.visibility

    /** Filter or assertion criteria for modifiers. */
    public val modifiers: Set<Modifier> get() = element.modifiers

    /** Filter or assertion criteria for supertypes. */
    public val supertypes: List<String> get() = element.supertypes

    /** Filter or assertion criteria for primary constructor. */
    public val primaryConstructor: ConstructorDeclaration? get() = element.primaryConstructor

    /** Filter or assertion criteria for secondary constructors. */
    public val secondaryConstructors: List<ConstructorDeclaration> get() = element.secondaryConstructors

    /** Filter or assertion criteria for functions. */
    public val functions: List<FunctionDeclaration> get() = element.functions

    /** Filter or assertion criteria for properties. */
    public val properties: List<PropertyDeclaration> get() = element.properties

    /** Filter or assertion criteria for companion object. */
    public val companionObject: ClassDeclaration? get() = element.companionObject

    /** Filter or assertion criteria for kdoc text. */
    public val kdocText: String? get() = element.kdocText

    /**
     * Checks if this class is annotated with the given annotation name or fully qualified name.
     */
    public fun hasAnnotation(name: String): Boolean = element.annotations.any { it.name == name || it.fqName == name }

    /**
     * Checks if this class is annotated with all of the given annotation names.
     */
    public fun hasAllAnnotations(names: List<String>): Boolean = element.hasAllAnnotations(names)

    /**
     * Checks if this class is annotated with all of the given annotation names.
     */
    public fun hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

    /**
     * Checks if this class is annotated with any of the given annotation names.
     */
    public fun hasAnyAnnotation(names: List<String>): Boolean = element.hasAnyAnnotation(names)

    /**
     * Checks if this class is annotated with any of the given annotation names.
     */
    public fun hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

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
            addViolation(message ?: io.github.baole.konture.i18n.getMessage("class.should.failedAssertion", fqName))
        }
    }

    /**
     * Asserts that this class is decorated with the specified annotation.
     */
    public fun assertAnnotationOf(annotationName: String) {
        if (!hasAnnotation(annotationName)) {
            addViolation(io.github.baole.konture.i18n.getMessage("class.should.haveAnnotation", fqName, annotationName))
        }
    }

    /**
     * Asserts that this class is decorated with all of the specified annotations.
     */
    public fun assertAllAnnotationsOf(names: List<String>) {
        if (!hasAllAnnotations(names)) {
            addViolation(
                io.github.baole.konture.i18n.getMessage(
                    "class.should.haveAllAnnotations",
                    fqName,
                    names.joinToString(),
                ),
            )
        }
    }

    /**
     * Asserts that this class is decorated with all of the specified annotations.
     */
    public fun assertAllAnnotationsOf(vararg names: String): Unit = assertAllAnnotationsOf(names.asList())

    /**
     * Asserts that this class is decorated with at least one of the specified annotations.
     */
    public fun assertAnyAnnotationOf(names: List<String>) {
        if (!hasAnyAnnotation(names)) {
            addViolation(
                io.github.baole.konture.i18n.getMessage(
                    "class.should.haveAtLeastOneAnnotationOf",
                    fqName,
                    names.joinToString(),
                ),
            )
        }
    }

    /**
     * Asserts that this class is decorated with at least one of the specified annotations.
     */
    public fun assertAnyAnnotationOf(vararg names: String): Unit = assertAnyAnnotationOf(names.asList())
}

// ==========================================
// Classes Common Extra Semantic Extensions
// ==========================================

/**
 * Helper extension to check if a class is annotated with the given annotation.
 */
public fun ClassDeclaration.hasAnnotation(name: String): Boolean =
    annotations.any { it.name == name || it.fqName == name }

/**
 * Helper extension to check if a class is annotated with all of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this class, false otherwise.
 */
public fun ClassDeclaration.hasAllAnnotations(names: List<String>): Boolean = names.all { hasAnnotation(it) }

/**
 * Helper extension to check if a class is annotated with all of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this class, false otherwise.
 */
public fun ClassDeclaration.hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

/**
 * Helper extension to check if a class is annotated with any of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this class, false otherwise.
 */
public fun ClassDeclaration.hasAnyAnnotation(names: List<String>): Boolean = names.any { hasAnnotation(it) }

/**
 * Helper extension to check if a class is annotated with any of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this class, false otherwise.
 */
public fun ClassDeclaration.hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

internal fun validateAssertionResult(result: Any?) {
    if (result !is Boolean && result !is Unit) {
        throw IllegalArgumentException(
            "A should { } block must return either a Boolean (as a predicate) or Unit (imperative assertion). " +
                "Returned type ${result?.javaClass?.name ?: "null"} is not supported.",
        )
    }
}
