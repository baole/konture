/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class PropertiesShould internal constructor(
    private val builder: PropertiesRuleBuilder,
) {
    infix fun resideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, prop.packageName)) {
                violations.add(
                    "Property ${prop.declaration.name} should reside in package '$packagePattern' but resides in '${prop.packageName}'",
                )
            }
        }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, prop.packageName) }
            if (!matches) {
                violations.add(
                    "Property ${prop.declaration.name} should reside in package in [${packagePatterns.joinToString()}] but resides in '${prop.packageName}'",
                )
            }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    infix fun resideInAPackage(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!predicate(prop.packageName)) {
                violations.add(
                    "Property ${prop.declaration.name} should reside in package matching predicate, but resides in '${prop.packageName}'",
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.name.endsWith(suffix)) {
                violations.add("Property ${prop.declaration.name} should have name ending with '$suffix'")
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = suffixes.any { prop.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    "Property ${prop.declaration.name} should have name ending with any of [${suffixes.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.name.startsWith(prefix)) {
                violations.add("Property ${prop.declaration.name} should have name starting with '$prefix'")
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = prefixes.any { prop.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    "Property ${prop.declaration.name} should have name starting with any of [${prefixes.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, prop.declaration.name)) {
                violations.add("Property ${prop.declaration.name} should have name matching '$pattern'")
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, prop.declaration.name) }
            if (!matches) {
                violations.add(
                    "Property ${prop.declaration.name} should have name matching any of [${patterns.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): PropertiesRuleBuilder = haveNameMatching(patterns.toList())

    fun bePublic(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PUBLIC) {
                violations.add("Property ${prop.declaration.name} should be public")
            }
        }
        return builder
    }

    fun beInternal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.INTERNAL) {
                violations.add("Property ${prop.declaration.name} should be internal")
            }
        }
        return builder
    }

    fun bePrivate(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PRIVATE) {
                violations.add("Property ${prop.declaration.name} should be private")
            }
        }
        return builder
    }

    fun beProtected(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PROTECTED) {
                violations.add("Property ${prop.declaration.name} should be protected")
            }
        }
        return builder
    }

    fun beVar(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.isVar) {
                violations.add("Property ${prop.declaration.name} should be var (mutable)")
            }
        }
        return builder
    }

    fun beVal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.isVar) {
                violations.add("Property ${prop.declaration.name} should be val (read-only)")
            }
        }
        return builder
    }

    infix fun haveType(typeFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.type != typeFqName) {
                violations.add(
                    "Property ${prop.declaration.name} should have type '$typeFqName' but was '${prop.declaration.type}'",
                )
            }
        }
        return builder
    }

    infix fun haveType(typeFqNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!typeFqNames.contains(prop.declaration.type)) {
                violations.add(
                    "Property ${prop.declaration.name} should have type in [${typeFqNames.joinToString()}] but was '${prop.declaration.type}'",
                )
            }
        }
        return builder
    }

    fun haveType(vararg typeFqNames: String): PropertiesRuleBuilder = haveType(typeFqNames.asList())

    infix fun haveAnnotationOf(annotationName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val hasAnnotation =
                prop.declaration.annotations.any {
                    it.name == annotationName || it.fqName == annotationName
                }
            if (!hasAnnotation) {
                violations.add("Property ${prop.declaration.name} should be annotated with @$annotationName")
            }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotationNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val hasAnnotation =
                prop.declaration.annotations.any { ann ->
                    annotationNames.any { it == ann.name || it == ann.fqName }
                }
            if (!hasAnnotation) {
                violations.add(
                    "Property ${prop.declaration.name} should be annotated with any of [${annotationNames.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveAnnotationOf(vararg annotationNames: String): PropertiesRuleBuilder =
        haveAnnotationOf(
            annotationNames.asList(),
        )

    /**
     * Asserts that selected properties are annotated with all of the specified annotations.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.hasAllAnnotations(names)) {
                violations.add("Property ${prop.declaration.name} should have all annotations: ${names.joinToString()}")
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties are annotated with all of the specified annotations.
     */
    fun haveAllAnnotationsOf(vararg names: String): PropertiesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /**
     * Asserts that selected properties are annotated with any of the specified annotations.
     */
    infix fun haveAnyAnnotationOf(names: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.hasAnyAnnotation(names)) {
                violations.add(
                    "Property ${prop.declaration.name} should have at least one annotation of: ${names.joinToString()}",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties are annotated with any of the specified annotations.
     */
    fun haveAnyAnnotationOf(vararg names: String): PropertiesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /**
     * Asserts that selected properties contain the specified modifier.
     */
    infix fun haveModifier(modifier: Modifier): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(modifier)) {
                violations.add("Property ${prop.declaration.name} should have modifier: $modifier")
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have all of the specified modifiers.
     *
     * @param modifiers The list of modifiers that must all be present.
     */
    infix fun haveAllModifiers(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val missing = modifiers.filter { !prop.declaration.modifiers.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(
                    "Property ${prop.declaration.name} should have all modifiers: ${modifiers.joinToString()}, but is missing: ${missing.joinToString()}",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have all of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers that must all be present.
     */
    fun haveAllModifiers(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAllModifiers(modifiers.asList())

    /**
     * Asserts that selected properties have at least one of the specified modifiers.
     *
     * @param modifiers The list of modifiers, at least one of which must be present.
     */
    infix fun haveAnyModifier(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!modifiers.any { prop.declaration.modifiers.contains(it) }) {
                violations.add(
                    "Property ${prop.declaration.name} should have at least one modifier of: ${modifiers.joinToString()}",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have at least one of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers, at least one of which must be present.
     */
    fun haveAnyModifier(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAnyModifier(modifiers.asList())

    /**
     * Asserts that selected properties have the specified visibility.
     */
    infix fun haveVisibility(visibility: Visibility): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != visibility) {
                violations.add(
                    "Property ${prop.declaration.name} should have visibility: $visibility but was: ${prop.declaration.visibility}",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have any of the specified visibilities.
     *
     * @param visibilities The list of acceptable visibilities.
     */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!visibilities.contains(prop.declaration.visibility)) {
                violations.add(
                    "Property ${prop.declaration.name} should have any visibility of: ${visibilities.joinToString()} but was: ${prop.declaration.visibility}",
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected properties have any of the specified visibilities.
     *
     * @param visibilities The vararg list of acceptable visibilities.
     */
    fun haveAnyVisibility(vararg visibilities: Visibility): PropertiesRuleBuilder = haveAnyVisibility(visibilities.asList())

    fun beExtension(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.isExtension) {
                violations.add("Property ${prop.declaration.name} should be an extension property")
            }
        }
        return builder
    }

    fun beConst(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.CONST)) {
                violations.add("Property ${prop.declaration.name} should be const val")
            }
        }
        return builder
    }

    fun beLateinit(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.LATEINIT)) {
                violations.add("Property ${prop.declaration.name} should be lateinit var")
            }
        }
        return builder
    }

    fun beDocumentedWithKDoc(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.kdocText.isNullOrBlank()) {
                violations.add("Property ${prop.declaration.name} should be documented with KDoc")
            }
        }
        return builder
    }

    infix fun satisfy(assertion: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder =
        satisfy("custom condition") { p, _ -> assertion(p) }

    private fun satisfy(
        description: String,
        assertion: (PropertyDeclarationContext, List<PropertyDeclarationContext>) -> Boolean,
    ): PropertiesRuleBuilder {
        builder.setShould { prop, allProps, violations ->
            if (!assertion(prop, allProps)) {
                violations.add("Property ${prop.declaration.name} should satisfy: $description")
            }
        }
        return builder
    }

    fun satisfy(assertion: (PropertyDeclarationContext, MutableList<String>) -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations -> assertion(prop, violations) }
        return builder
    }
}
