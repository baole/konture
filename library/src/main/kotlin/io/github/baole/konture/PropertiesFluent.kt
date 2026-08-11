/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

// ==========================================
// Properties Rule Builder Fluent DSL
// ==========================================

/**
 * Filters properties in this rule using a concise lambda predicate evaluated on each [PropertyDeclarationContext].
 *
 * @param predicate The filter criteria block executed on the [PropertyDeclarationContext].
 * @return This [PropertiesRuleBuilder] with the filter condition applied.
 */
public fun PropertiesRuleBuilder.that(predicate: PropertyDeclarationContext.() -> Boolean): PropertiesRuleBuilder =
    this.apply {
        setThat { it.predicate() }
    }

/**
 * Asserts rules on filtered properties using a lambda block that provides a [PropertyDeclarationShouldContext] receiver.
 * Supports both imperative assertions and Boolean predicate matches.
 *
 * @param assertion The assertion block containing property validation rules or boolean predicate.
 * @return This [PropertiesRuleBuilder] with the assertion block registered.
 */
public fun PropertiesRuleBuilder.should(assertion: PropertyDeclarationShouldContext.() -> Any?): PropertiesRuleBuilder =
    this.apply {
        setShould { prop, allProperties, violations ->
            /** Filter or assertion criteria for context. */
            val context = PropertyDeclarationShouldContext(prop, allProperties, violations)

            /** Filter or assertion criteria for result. */
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add(
                    io.github.baole.konture.i18n.getMessage(
                        "property.should.failedCustomAssertion",
                        prop.declaration.name,
                    ),
                )
            }
        }
    }

/**
 * Context receiver for writing declarative assertions on a [PropertyDeclarationContext] element.
 * Provides easy access to all element properties and custom helper assertions.
 *
 * @property element The target [PropertyDeclarationContext] being verified.
 * @property allProperties The complete list of property declaration contexts in this test run scope.
 * @property violations Mutable collection where assertion failure messages are appended.
 */
public class PropertyDeclarationShouldContext internal constructor(
    /** Filter or assertion criteria for element. */
    public val element: PropertyDeclarationContext,
    /** Filter or assertion criteria for all properties. */
    public val allProperties: List<PropertyDeclarationContext>,
    /** Filter or assertion criteria for violations. */
    public val violations: MutableList<String>,
) {
    /** Filter or assertion criteria for declaration. */
    public val declaration: PropertyDeclaration get() = element.declaration

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

    /** Filter or assertion criteria for type. */
    public val type: String get() = element.declaration.type

    /** Filter or assertion criteria for is val. */
    public val isVal: Boolean get() = element.declaration.isVal

    /** Filter or assertion criteria for is var. */
    public val isVar: Boolean get() = element.declaration.isVar

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
            addViolation(message ?: io.github.baole.konture.i18n.getMessage("property.should.failedAssertion", name))
        }
    }

    /**
     * Checks if this property is decorated with the specified annotation.
     */
    public fun hasAnnotation(name: String): Boolean = annotations.any { it.name == name || it.fqName == name }

    /**
     * Checks if this property is decorated with all of the specified annotations.
     */
    public fun hasAllAnnotations(names: List<String>): Boolean = element.hasAllAnnotations(names)

    /**
     * Checks if this property is decorated with all of the specified annotations.
     */
    public fun hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

    /**
     * Checks if this property is decorated with any of the specified annotations.
     */
    public fun hasAnyAnnotation(names: List<String>): Boolean = element.hasAnyAnnotation(names)

    /**
     * Checks if this property is decorated with any of the specified annotations.
     */
    public fun hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

    /**
     * Asserts that this property is decorated with the specified annotation.
     */
    public fun assertAnnotationOf(annotationName: String) {
        if (!hasAnnotation(annotationName)) {
            addViolation(
                io.github.baole.konture.i18n.getMessage("property.should.haveAnnotation", name, annotationName),
            )
        }
    }

    /**
     * Asserts that this property is decorated with all of the specified annotations.
     */
    public fun assertAllAnnotationsOf(names: List<String>) {
        if (!hasAllAnnotations(names)) {
            addViolation(
                io.github.baole.konture.i18n.getMessage(
                    "property.should.haveAllAnnotations",
                    name,
                    names.joinToString(),
                ),
            )
        }
    }

    /**
     * Asserts that this property is decorated with all of the specified annotations.
     */
    public fun assertAllAnnotationsOf(vararg names: String): Unit = assertAllAnnotationsOf(names.asList())

    /**
     * Asserts that this property is decorated with at least one of the specified annotations.
     */
    public fun assertAnyAnnotationOf(names: List<String>) {
        if (!hasAnyAnnotation(names)) {
            addViolation(
                io.github.baole.konture.i18n.getMessage(
                    "property.should.haveAnyAnnotation",
                    name,
                    names.joinToString(),
                ),
            )
        }
    }

    /**
     * Asserts that this property is decorated with at least one of the specified annotations.
     */
    public fun assertAnyAnnotationOf(vararg names: String): Unit = assertAnyAnnotationOf(names.asList())
}

// ==========================================
// Properties Common Extra Semantic Extensions
// ==========================================

/**
 * Helper extension to check if a property has the specified annotation.
 */
public fun PropertyDeclarationContext.hasAnnotation(name: String): Boolean =
    declaration.annotations.any { it.name == name || it.fqName == name }

/**
 * Helper extension to check if a property has all of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this property, false otherwise.
 */
public fun PropertyDeclarationContext.hasAllAnnotations(names: List<String>): Boolean = names.all { hasAnnotation(it) }

/**
 * Helper extension to check if a property has all of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this property, false otherwise.
 */
public fun PropertyDeclarationContext.hasAllAnnotations(vararg names: String): Boolean =
    hasAllAnnotations(names.asList())

/**
 * Helper extension to check if a property has any of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this property, false otherwise.
 */
public fun PropertyDeclarationContext.hasAnyAnnotation(names: List<String>): Boolean = names.any { hasAnnotation(it) }

/**
 * Helper extension to check if a property has any of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this property, false otherwise.
 */
public fun PropertyDeclarationContext.hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

// ==========================================
// Properties Context Field Delegation Extensions
// ==========================================

/** Delegates name property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.name: String get() = declaration.name

/** Delegates visibility property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.visibility: Visibility get() = declaration.visibility

/** Delegates modifiers property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.modifiers: Set<Modifier> get() = declaration.modifiers

/** Delegates type property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.type: String get() = declaration.type

/** Delegates isVal property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.isVal: Boolean get() = declaration.isVal

/** Delegates isVar property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.isVar: Boolean get() = declaration.isVar

/** Delegates annotations property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.annotations: List<AnnotationDeclaration> get() = declaration.annotations

/** Delegates isExtension property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.isExtension: Boolean get() = declaration.isExtension

/** Delegates kdocText property to the underlying [PropertyDeclaration]. */
public val PropertyDeclarationContext.kdocText: String? get() = declaration.kdocText

/** Filters properties residing in a package matching [packagePattern]. */
public fun List<PropertyDeclarationContext>.residingInPackage(
    packagePattern: String,
): List<PropertyDeclarationContext> =
    filter { io.github.baole.konture.impl.PatternMatchers.matchesPackage(packagePattern, it.packageName) }

/** Filters properties residing in a module matching [modulePath]. */
public fun List<PropertyDeclarationContext>.residingInModule(modulePath: String): List<PropertyDeclarationContext> =
    filter {
        it.modulePath == modulePath || io.github.baole.konture.impl.PatternMatchers.matchesModuleGlob(modulePath, it.modulePath)
    }

/** Filters properties annotated with [annotationName]. */
public fun List<PropertyDeclarationContext>.annotatedWith(annotationName: String): List<PropertyDeclarationContext> =
    filter { it.hasAnnotation(annotationName) }
