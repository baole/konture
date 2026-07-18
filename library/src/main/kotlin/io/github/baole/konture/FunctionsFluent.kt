/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

// ==========================================
// Functions Rule Builder Fluent DSL
// ==========================================

/**
 * Filters functions in this rule using a concise lambda predicate evaluated on each [FunctionDeclarationContext].
 *
 * @param predicate The filter criteria block executed on the [FunctionDeclarationContext].
 * @return This [FunctionsRuleBuilder] with the filter condition applied.
 */
fun FunctionsRuleBuilder.that(predicate: FunctionDeclarationContext.() -> Boolean): FunctionsRuleBuilder =
    this.apply {
        setThat { it.predicate() }
    }

/**
 * Asserts rules on filtered functions using a lambda block that provides a [FunctionDeclarationShouldContext] receiver.
 * Supports both imperative assertions and Boolean predicate matches.
 *
 * @param assertion The assertion block containing function validation rules or boolean predicate.
 * @return This [FunctionsRuleBuilder] with the assertion block registered.
 */
fun FunctionsRuleBuilder.should(assertion: FunctionDeclarationShouldContext.() -> Any?): FunctionsRuleBuilder =
    this.apply {
        setShould { func, allFuncs, violations ->
            val context = FunctionDeclarationShouldContext(func, allFuncs, violations)
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add(io.github.baole.konture.i18n.getMessage("function.should.failedCustomAssertion", func.declaration.name))
            }
        }
    }

/**
 * Context receiver for writing declarative assertions on a [FunctionDeclarationContext] element.
 * Provides easy access to all element properties and custom helper assertions.
 *
 * @property element The target [FunctionDeclarationContext] being verified.
 * @property allFunctions The complete list of function declaration contexts in this test run scope.
 * @property violations Mutable collection where assertion failure messages are appended.
 */
class FunctionDeclarationShouldContext internal constructor(
    val element: FunctionDeclarationContext,
    val allFunctions: List<FunctionDeclarationContext>,
    val violations: MutableList<String>,
) {
    val declaration get() = element.declaration
    val name get() = element.declaration.name
    val packageName get() = element.packageName
    val className get() = element.className
    val modulePath get() = element.modulePath
    val filePath get() = element.filePath
    val visibility get() = element.declaration.visibility
    val modifiers get() = element.declaration.modifiers
    val returnType get() = element.declaration.returnType
    val parameters get() = element.declaration.parameters
    val annotations get() = element.declaration.annotations
    val kdocText get() = element.declaration.kdocText
    val isExtension get() = element.declaration.isExtension

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
            addViolation(message ?: io.github.baole.konture.i18n.getMessage("function.should.failedAssertion", name))
        }
    }

    /**
     * Checks if this function is decorated with the specified annotation.
     */
    fun hasAnnotation(name: String): Boolean = annotations.any { it.name == name || it.fqName == name }

    /**
     * Checks if this function is decorated with all of the specified annotations.
     */
    fun hasAllAnnotations(names: List<String>): Boolean = element.hasAllAnnotations(names)

    /**
     * Checks if this function is decorated with all of the specified annotations.
     */
    fun hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

    /**
     * Checks if this function is decorated with any of the specified annotations.
     */
    fun hasAnyAnnotation(names: List<String>): Boolean = element.hasAnyAnnotation(names)

    /**
     * Checks if this function is decorated with any of the specified annotations.
     */
    fun hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

    /**
     * Asserts that this function is decorated with the specified annotation.
     */
    fun assertAnnotationOf(annotationName: String) {
        if (!hasAnnotation(annotationName)) {
            addViolation(io.github.baole.konture.i18n.getMessage("function.should.haveAnnotation", name, annotationName))
        }
    }

    /**
     * Asserts that this function is decorated with all of the specified annotations.
     */
    fun assertAllAnnotationsOf(names: List<String>) {
        if (!hasAllAnnotations(names)) {
            addViolation(io.github.baole.konture.i18n.getMessage("function.should.haveAllAnnotations", name, names.joinToString()))
        }
    }

    /**
     * Asserts that this function is decorated with all of the specified annotations.
     */
    fun assertAllAnnotationsOf(vararg names: String) = assertAllAnnotationsOf(names.asList())

    /**
     * Asserts that this function is decorated with at least one of the specified annotations.
     */
    fun assertAnyAnnotationOf(names: List<String>) {
        if (!hasAnyAnnotation(names)) {
            addViolation(io.github.baole.konture.i18n.getMessage("function.should.haveAnyAnnotation", name, names.joinToString()))
        }
    }

    /**
     * Asserts that this function is decorated with at least one of the specified annotations.
     */
    fun assertAnyAnnotationOf(vararg names: String) = assertAnyAnnotationOf(names.asList())

    /**
     * Asserts that none of the function's parameters match the given predicate block.
     * Appends the [message] suffix to the violation report on failure.
     */
    fun noneParameterMatches(
        message: String,
        predicate: (ParameterDeclaration) -> Boolean,
    ) {
        val violated = parameters.any { predicate(it) }
        if (violated) {
            addViolation("Function $name $message")
        }
    }

    /**
     * Asserts that at least one of the function's parameters matches the given predicate block.
     * Appends the [message] suffix to the violation report on failure.
     */
    fun anyParameterMatches(
        message: String,
        predicate: (ParameterDeclaration) -> Boolean,
    ) {
        val matched = parameters.any { predicate(it) }
        if (!matched) {
            addViolation(io.github.baole.konture.i18n.getMessage("function.should.haveAnyParameterMatching", name, message))
        }
    }
}

// ==========================================
// Functions Common Extra Semantic Extensions
// ==========================================

/**
 * Helper extension to check if a function has the specified annotation.
 */
fun FunctionDeclarationContext.hasAnnotation(name: String): Boolean = declaration.annotations.any { it.name == name || it.fqName == name }

/**
 * Helper extension to check if a function has all of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this function, false otherwise.
 */
fun FunctionDeclarationContext.hasAllAnnotations(names: List<String>): Boolean = names.all { hasAnnotation(it) }

/**
 * Helper extension to check if a function has all of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this function, false otherwise.
 */
fun FunctionDeclarationContext.hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

/**
 * Helper extension to check if a function has any of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this function, false otherwise.
 */
fun FunctionDeclarationContext.hasAnyAnnotation(names: List<String>): Boolean = names.any { hasAnnotation(it) }

/**
 * Helper extension to check if a function has any of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this function, false otherwise.
 */
fun FunctionDeclarationContext.hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

// ==========================================
// Functions Context Field Delegation Extensions
// ==========================================

/** Delegates name property to the underlying [FunctionDeclaration]. */
val FunctionDeclarationContext.name: String get() = declaration.name

/** Delegates visibility property to the underlying [FunctionDeclaration]. */
val FunctionDeclarationContext.visibility: Visibility get() = declaration.visibility

/** Delegates modifiers property to the underlying [FunctionDeclaration]. */
val FunctionDeclarationContext.modifiers: Set<Modifier> get() = declaration.modifiers

/** Delegates returnType property to the underlying [FunctionDeclaration]. */
val FunctionDeclarationContext.returnType: String get() = declaration.returnType

/** Delegates parameters property to the underlying [FunctionDeclaration]. */
val FunctionDeclarationContext.parameters: List<ParameterDeclaration> get() = declaration.parameters

/** Delegates annotations property to the underlying [FunctionDeclaration]. */
val FunctionDeclarationContext.annotations: List<AnnotationDeclaration> get() = declaration.annotations

/** Delegates isExtension property to the underlying [FunctionDeclaration]. */
val FunctionDeclarationContext.isExtension: Boolean get() = declaration.isExtension

/** Delegates kdocText property to the underlying [FunctionDeclaration]. */
val FunctionDeclarationContext.kdocText: String? get() = declaration.kdocText
