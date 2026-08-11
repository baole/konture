/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
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
public fun FunctionsRuleBuilder.that(predicate: FunctionDeclarationContext.() -> Boolean): FunctionsRuleBuilder =
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
public fun FunctionsRuleBuilder.should(assertion: FunctionDeclarationShouldContext.() -> Any?): FunctionsRuleBuilder =
    this.apply {
        setShould { func, allFuncs, violations ->
            /** Filter or assertion criteria for context. */
            val context = FunctionDeclarationShouldContext(func, allFuncs, violations)

            /** Filter or assertion criteria for result. */
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add(
                    io.github.baole.konture.i18n.getMessage(
                        "function.should.failedCustomAssertion",
                        func.declaration.name,
                    ),
                )
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
public class FunctionDeclarationShouldContext internal constructor(
    /** Filter or assertion criteria for element. */
    public val element: FunctionDeclarationContext,
    /** Filter or assertion criteria for all functions. */
    public val allFunctions: List<FunctionDeclarationContext>,
    /** Filter or assertion criteria for violations. */
    public val violations: MutableList<String>,
) {
    /** Filter or assertion criteria for declaration. */
    public val declaration: FunctionDeclaration get() = element.declaration

    /** Filter or assertion criteria for name. */
    public val name: String get() = element.declaration.name

    /** Filter or assertion criteria for package name. */
    public val packageName: String get() = element.packageName

    /** Filter or assertion criteria for class name. */
    public val className: String? get() = element.className

    /** Filter or assertion criteria for module path. */
    public val modulePath: String get() = element.modulePath

    /** Filter or assertion criteria for file path. */
    public val filePath: String get() = element.filePath

    /** Filter or assertion criteria for visibility. */
    public val visibility: Visibility get() = element.declaration.visibility

    /** Filter or assertion criteria for modifiers. */
    public val modifiers: Set<Modifier> get() = element.declaration.modifiers

    /** Filter or assertion criteria for return type. */
    public val returnType: String get() = element.declaration.returnType

    /** Filter or assertion criteria for parameters. */
    public val parameters: List<ParameterDeclaration> get() = element.declaration.parameters

    /** Filter or assertion criteria for annotations. */
    public val annotations: List<AnnotationDeclaration> get() = element.declaration.annotations

    /** Filter or assertion criteria for kdoc text. */
    public val kdocText: String? get() = element.declaration.kdocText

    /** Filter or assertion criteria for is extension. */
    public val isExtension: Boolean get() = element.declaration.isExtension

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
            addViolation(message ?: io.github.baole.konture.i18n.getMessage("function.should.failedAssertion", name))
        }
    }

    /**
     * Checks if this function is decorated with the specified annotation.
     */
    public fun hasAnnotation(name: String): Boolean = annotations.any { it.name == name || it.fqName == name }

    /**
     * Checks if this function is decorated with all of the specified annotations.
     */
    public fun hasAllAnnotations(names: List<String>): Boolean = element.hasAllAnnotations(names)

    /**
     * Checks if this function is decorated with all of the specified annotations.
     */
    public fun hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

    /**
     * Checks if this function is decorated with any of the specified annotations.
     */
    public fun hasAnyAnnotation(names: List<String>): Boolean = element.hasAnyAnnotation(names)

    /**
     * Checks if this function is decorated with any of the specified annotations.
     */
    public fun hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

    /**
     * Asserts that this function is decorated with the specified annotation.
     */
    public fun assertAnnotationOf(annotationName: String) {
        if (!hasAnnotation(annotationName)) {
            addViolation(
                io.github.baole.konture.i18n.getMessage("function.should.haveAnnotation", name, annotationName),
            )
        }
    }

    /**
     * Asserts that this function is decorated with all of the specified annotations.
     */
    public fun assertAllAnnotationsOf(names: List<String>) {
        if (!hasAllAnnotations(names)) {
            addViolation(
                io.github.baole.konture.i18n.getMessage(
                    "function.should.haveAllAnnotations",
                    name,
                    names.joinToString(),
                ),
            )
        }
    }

    /**
     * Asserts that this function is decorated with all of the specified annotations.
     */
    public fun assertAllAnnotationsOf(vararg names: String): Unit = assertAllAnnotationsOf(names.asList())

    /**
     * Asserts that this function is decorated with at least one of the specified annotations.
     */
    public fun assertAnyAnnotationOf(names: List<String>) {
        if (!hasAnyAnnotation(names)) {
            addViolation(
                io.github.baole.konture.i18n.getMessage(
                    "function.should.haveAnyAnnotation",
                    name,
                    names.joinToString(),
                ),
            )
        }
    }

    /**
     * Asserts that this function is decorated with at least one of the specified annotations.
     */
    public fun assertAnyAnnotationOf(vararg names: String): Unit = assertAnyAnnotationOf(names.asList())

    /**
     * Asserts that none of the function's parameters match the given predicate block.
     * Appends the [message] suffix to the violation report on failure.
     */
    public fun noneParameterMatches(
        message: String,
        predicate: (ParameterDeclaration) -> Boolean,
    ) {
        /** Filter or assertion criteria for violated. */
        val violated = parameters.any { predicate(it) }
        if (violated) {
            addViolation("Function $name $message")
        }
    }

    /**
     * Asserts that at least one of the function's parameters matches the given predicate block.
     * Appends the [message] suffix to the violation report on failure.
     */
    public fun anyParameterMatches(
        message: String,
        predicate: (ParameterDeclaration) -> Boolean,
    ) {
        /** Filter or assertion criteria for matched. */
        val matched = parameters.any { predicate(it) }
        if (!matched) {
            addViolation(
                io.github.baole.konture.i18n.getMessage("function.should.haveAnyParameterMatching", name, message),
            )
        }
    }
}

// ==========================================
// Functions Common Extra Semantic Extensions
// ==========================================

/**
 * Helper extension to check if a function has the specified annotation.
 */
public fun FunctionDeclarationContext.hasAnnotation(name: String): Boolean =
    declaration.annotations.any { it.name == name || it.fqName == name }

/**
 * Helper extension to check if a function has all of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this function, false otherwise.
 */
public fun FunctionDeclarationContext.hasAllAnnotations(names: List<String>): Boolean = names.all { hasAnnotation(it) }

/**
 * Helper extension to check if a function has all of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this function, false otherwise.
 */
public fun FunctionDeclarationContext.hasAllAnnotations(vararg names: String): Boolean =
    hasAllAnnotations(names.asList())

/**
 * Helper extension to check if a function has any of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this function, false otherwise.
 */
public fun FunctionDeclarationContext.hasAnyAnnotation(names: List<String>): Boolean = names.any { hasAnnotation(it) }

/**
 * Helper extension to check if a function has any of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this function, false otherwise.
 */
public fun FunctionDeclarationContext.hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

// ==========================================
// Functions Context Field Delegation Extensions
// ==========================================

/** Delegates name property to the underlying [FunctionDeclaration]. */
public val FunctionDeclarationContext.name: String get() = declaration.name

/** Delegates visibility property to the underlying [FunctionDeclaration]. */
public val FunctionDeclarationContext.visibility: Visibility get() = declaration.visibility

/** Delegates modifiers property to the underlying [FunctionDeclaration]. */
public val FunctionDeclarationContext.modifiers: Set<Modifier> get() = declaration.modifiers

/** Delegates returnType property to the underlying [FunctionDeclaration]. */
public val FunctionDeclarationContext.returnType: String get() = declaration.returnType

/** Delegates parameters property to the underlying [FunctionDeclaration]. */
public val FunctionDeclarationContext.parameters: List<ParameterDeclaration> get() = declaration.parameters

/** Delegates annotations property to the underlying [FunctionDeclaration]. */
public val FunctionDeclarationContext.annotations: List<AnnotationDeclaration> get() = declaration.annotations

/** Delegates isExtension property to the underlying [FunctionDeclaration]. */
public val FunctionDeclarationContext.isExtension: Boolean get() = declaration.isExtension

/** Delegates kdocText property to the underlying [FunctionDeclaration]. */
public val FunctionDeclarationContext.kdocText: String? get() = declaration.kdocText

/** Filters functions residing in a package matching [packagePattern]. */
public fun List<FunctionDeclarationContext>.residingInPackage(
    packagePattern: String,
): List<FunctionDeclarationContext> =
    filter { io.github.baole.konture.impl.PatternMatchers.matchesPackage(packagePattern, it.packageName) }

/** Filters functions residing in a module matching [modulePath]. */
public fun List<FunctionDeclarationContext>.residingInModule(modulePath: String): List<FunctionDeclarationContext> =
    filter {
        it.modulePath == modulePath || io.github.baole.konture.impl.PatternMatchers.matchesModuleGlob(modulePath, it.modulePath)
    }

/** Filters functions annotated with [annotationName]. */
public fun List<FunctionDeclarationContext>.annotatedWith(annotationName: String): List<FunctionDeclarationContext> =
    filter { it.hasAnnotation(annotationName) }
