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
class ClassDeclarationShouldContext internal constructor(
    /** Filter or assertion criteria for element. */
    val element: ClassDeclaration,
    /** Filter or assertion criteria for all classes. */
    val allClasses: List<ClassDeclaration>,
    /** Filter or assertion criteria for violations. */
    val violations: MutableList<String>,
) {
    /** Filter or assertion criteria for name. */
    val name get() = element.name

    /** Filter or assertion criteria for fq name. */
    val fqName get() = element.fqName

    /** Filter or assertion criteria for package name. */
    val packageName get() = element.packageName

    /** Filter or assertion criteria for is interface. */
    val isInterface get() = element.isInterface

    /** Filter or assertion criteria for is abstract. */
    val isAbstract get() = element.isAbstract

    /** Filter or assertion criteria for annotations. */
    val annotations get() = element.annotations

    /** Filter or assertion criteria for imports. */
    val imports get() = element.imports

    /** Filter or assertion criteria for referenced types. */
    val referencedTypes get() = element.referencedTypes

    /** Filter or assertion criteria for file path. */
    val filePath get() = element.filePath

    /** Filter or assertion criteria for visibility. */
    val visibility get() = element.visibility

    /** Filter or assertion criteria for modifiers. */
    val modifiers get() = element.modifiers

    /** Filter or assertion criteria for supertypes. */
    val supertypes get() = element.supertypes

    /** Filter or assertion criteria for primary constructor. */
    val primaryConstructor get() = element.primaryConstructor

    /** Filter or assertion criteria for secondary constructors. */
    val secondaryConstructors get() = element.secondaryConstructors

    /** Filter or assertion criteria for functions. */
    val functions get() = element.functions

    /** Filter or assertion criteria for properties. */
    val properties get() = element.properties

    /** Filter or assertion criteria for companion object. */
    val companionObject get() = element.companionObject

    /** Filter or assertion criteria for kdoc text. */
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
            addViolation(message ?: io.github.baole.konture.i18n.getMessage("class.should.failedAssertion", fqName))
        }
    }

    /**
     * Asserts that this class is decorated with the specified annotation.
     */
    fun assertAnnotationOf(annotationName: String) {
        if (!hasAnnotation(annotationName)) {
            addViolation(io.github.baole.konture.i18n.getMessage("class.should.haveAnnotation", fqName, annotationName))
        }
    }

    /**
     * Asserts that this class is decorated with all of the specified annotations.
     */
    fun assertAllAnnotationsOf(names: List<String>) {
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
    fun assertAllAnnotationsOf(vararg names: String) = assertAllAnnotationsOf(names.asList())

    /**
     * Asserts that this class is decorated with at least one of the specified annotations.
     */
    fun assertAnyAnnotationOf(names: List<String>) {
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
