/*
 * Copyright 2026 Bao Le Duc
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
fun ClassesRuleBuilder.that(predicate: ClassDeclaration.() -> Boolean): ClassesRuleBuilder =
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
fun ClassesRuleBuilder.should(assertion: ClassDeclarationShouldContext.() -> Any?): ClassesRuleBuilder =
    this.apply {
        setShould { cls, allClasses, violations ->
            val context = ClassDeclarationShouldContext(cls, allClasses, violations)
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add("Class ${cls.fqName} failed custom assertion")
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
class ClassDeclarationShouldContext internal constructor(
    val element: ClassDeclaration,
    val allClasses: List<ClassDeclaration>,
    val violations: MutableList<String>,
) {
    val name get() = element.name
    val fqName get() = element.fqName
    val packageName get() = element.packageName
    val isInterface get() = element.isInterface
    val isAbstract get() = element.isAbstract
    val annotations get() = element.annotations
    val imports get() = element.imports
    val referencedTypes get() = element.referencedTypes
    val filePath get() = element.filePath
    val visibility get() = element.visibility
    val modifiers get() = element.modifiers
    val supertypes get() = element.supertypes
    val primaryConstructor get() = element.primaryConstructor
    val secondaryConstructors get() = element.secondaryConstructors
    val functions get() = element.functions
    val properties get() = element.properties
    val companionObject get() = element.companionObject
    val kdocText get() = element.kdocText

    /**
     * Checks if this class is annotated with the given annotation name or fully qualified name.
     */
    fun hasAnnotation(name: String): Boolean = element.annotations.any { it.name == name || it.fqName == name }

    /**
     * Checks if this class is annotated with all of the given annotation names.
     */
    fun hasAllAnnotations(names: List<String>): Boolean = element.hasAllAnnotations(names)

    /**
     * Checks if this class is annotated with all of the given annotation names.
     */
    fun hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

    /**
     * Checks if this class is annotated with any of the given annotation names.
     */
    fun hasAnyAnnotation(names: List<String>): Boolean = element.hasAnyAnnotation(names)

    /**
     * Checks if this class is annotated with any of the given annotation names.
     */
    fun hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

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
            addViolation(message ?: "Class $fqName failed assertion")
        }
    }

    /**
     * Asserts that this class is decorated with the specified annotation.
     */
    fun assertAnnotationOf(annotationName: String) {
        if (!hasAnnotation(annotationName)) {
            addViolation("Class $fqName should be annotated with @$annotationName")
        }
    }

    /**
     * Asserts that this class is decorated with all of the specified annotations.
     */
    fun assertAllAnnotationsOf(names: List<String>) {
        if (!hasAllAnnotations(names)) {
            addViolation("Class $fqName should have all annotations: ${names.joinToString()}")
        }
    }

    /**
     * Asserts that this class is decorated with all of the specified annotations.
     */
    fun assertAllAnnotationsOf(vararg names: String) = assertAllAnnotationsOf(names.asList())

    /**
     * Asserts that this class is decorated with at least one of the specified annotations.
     */
    fun assertAnyAnnotationOf(names: List<String>) {
        if (!hasAnyAnnotation(names)) {
            addViolation("Class $fqName should have at least one annotation of: ${names.joinToString()}")
        }
    }

    /**
     * Asserts that this class is decorated with at least one of the specified annotations.
     */
    fun assertAnyAnnotationOf(vararg names: String) = assertAnyAnnotationOf(names.asList())
}

// ==========================================
// Classes Common Extra Semantic Extensions
// ==========================================

/**
 * Helper extension to check if a class is annotated with the given annotation.
 */
fun ClassDeclaration.hasAnnotation(name: String): Boolean = annotations.any { it.name == name || it.fqName == name }

/**
 * Helper extension to check if a class is annotated with all of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this class, false otherwise.
 */
fun ClassDeclaration.hasAllAnnotations(names: List<String>): Boolean = names.all { hasAnnotation(it) }

/**
 * Helper extension to check if a class is annotated with all of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this class, false otherwise.
 */
fun ClassDeclaration.hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

/**
 * Helper extension to check if a class is annotated with any of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this class, false otherwise.
 */
fun ClassDeclaration.hasAnyAnnotation(names: List<String>): Boolean = names.any { hasAnnotation(it) }

/**
 * Helper extension to check if a class is annotated with any of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this class, false otherwise.
 */
fun ClassDeclaration.hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

internal fun validateAssertionResult(result: Any?) {
    if (result !is Boolean && result !is Unit) {
        throw IllegalArgumentException(
            "A should { } block must return either a Boolean (as a predicate) or Unit (imperative assertion). " +
                "Returned type ${result?.javaClass?.name ?: "null"} is not supported.",
        )
    }
}
