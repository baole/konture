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
fun PropertiesRuleBuilder.that(predicate: PropertyDeclarationContext.() -> Boolean): PropertiesRuleBuilder =
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
fun PropertiesRuleBuilder.should(assertion: PropertyDeclarationShouldContext.() -> Any?): PropertiesRuleBuilder =
    this.apply {
        setShould { prop, allProperties, violations ->
            val context = PropertyDeclarationShouldContext(prop, allProperties, violations)
            val result = context.assertion()
            validateAssertionResult(result)
            if (result is Boolean && !result) {
                violations.add("Property ${prop.declaration.name} failed custom assertion")
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
class PropertyDeclarationShouldContext internal constructor(
    val element: PropertyDeclarationContext,
    val allProperties: List<PropertyDeclarationContext>,
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
    val type get() = element.declaration.type
    val isVal get() = element.declaration.isVal
    val isVar get() = element.declaration.isVar
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
            addViolation(message ?: "Property $name failed assertion")
        }
    }

    /**
     * Checks if this property is decorated with the specified annotation.
     */
    fun hasAnnotation(name: String): Boolean = annotations.any { it.name == name || it.fqName == name }

    /**
     * Checks if this property is decorated with all of the specified annotations.
     */
    fun hasAllAnnotations(names: List<String>): Boolean = element.hasAllAnnotations(names)

    /**
     * Checks if this property is decorated with all of the specified annotations.
     */
    fun hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

    /**
     * Checks if this property is decorated with any of the specified annotations.
     */
    fun hasAnyAnnotation(names: List<String>): Boolean = element.hasAnyAnnotation(names)

    /**
     * Checks if this property is decorated with any of the specified annotations.
     */
    fun hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

    /**
     * Asserts that this property is decorated with the specified annotation.
     */
    fun assertAnnotationOf(annotationName: String) {
        if (!hasAnnotation(annotationName)) {
            addViolation("Property $name should be annotated with @$annotationName")
        }
    }

    /**
     * Asserts that this property is decorated with all of the specified annotations.
     */
    fun assertAllAnnotationsOf(names: List<String>) {
        if (!hasAllAnnotations(names)) {
            addViolation("Property $name should have all annotations: ${names.joinToString()}")
        }
    }

    /**
     * Asserts that this property is decorated with all of the specified annotations.
     */
    fun assertAllAnnotationsOf(vararg names: String) = assertAllAnnotationsOf(names.asList())

    /**
     * Asserts that this property is decorated with at least one of the specified annotations.
     */
    fun assertAnyAnnotationOf(names: List<String>) {
        if (!hasAnyAnnotation(names)) {
            addViolation("Property $name should have at least one annotation of: ${names.joinToString()}")
        }
    }

    /**
     * Asserts that this property is decorated with at least one of the specified annotations.
     */
    fun assertAnyAnnotationOf(vararg names: String) = assertAnyAnnotationOf(names.asList())
}

// ==========================================
// Properties Common Extra Semantic Extensions
// ==========================================

/**
 * Helper extension to check if a property has the specified annotation.
 */
fun PropertyDeclarationContext.hasAnnotation(name: String): Boolean = declaration.annotations.any { it.name == name || it.fqName == name }

/**
 * Helper extension to check if a property has all of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this property, false otherwise.
 */
fun PropertyDeclarationContext.hasAllAnnotations(names: List<String>): Boolean = names.all { hasAnnotation(it) }

/**
 * Helper extension to check if a property has all of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if all annotations are present on this property, false otherwise.
 */
fun PropertyDeclarationContext.hasAllAnnotations(vararg names: String): Boolean = hasAllAnnotations(names.asList())

/**
 * Helper extension to check if a property has any of the specified annotations.
 *
 * @param names The list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this property, false otherwise.
 */
fun PropertyDeclarationContext.hasAnyAnnotation(names: List<String>): Boolean = names.any { hasAnnotation(it) }

/**
 * Helper extension to check if a property has any of the specified annotations.
 *
 * @param names The vararg list of annotation names or fully qualified names to check.
 * @return True if any annotation is present on this property, false otherwise.
 */
fun PropertyDeclarationContext.hasAnyAnnotation(vararg names: String): Boolean = hasAnyAnnotation(names.asList())

// ==========================================
// Properties Context Field Delegation Extensions
// ==========================================

/** Delegates name property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.name: String get() = declaration.name

/** Delegates visibility property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.visibility: Visibility get() = declaration.visibility

/** Delegates modifiers property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.modifiers: Set<Modifier> get() = declaration.modifiers

/** Delegates type property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.type: String get() = declaration.type

/** Delegates isVal property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.isVal: Boolean get() = declaration.isVal

/** Delegates isVar property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.isVar: Boolean get() = declaration.isVar

/** Delegates annotations property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.annotations: List<AnnotationDeclaration> get() = declaration.annotations

/** Delegates isExtension property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.isExtension: Boolean get() = declaration.isExtension

/** Delegates kdocText property to the underlying [PropertyDeclaration]. */
val PropertyDeclarationContext.kdocText: String? get() = declaration.kdocText
