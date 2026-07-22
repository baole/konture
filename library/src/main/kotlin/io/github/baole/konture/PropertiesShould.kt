/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class PropertiesShould internal constructor(
    private val builder: PropertiesRuleBuilder,
) {
    infix fun resideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, prop.packageName)) {
                violations.add(
                    getMessage("property.should.resideInPackage", prop.qualifiedName, packagePattern, prop.packageName),
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
                    getMessage("property.should.resideInPackageAny", prop.qualifiedName, packagePatterns.joinToString(), prop.packageName),
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
                    getMessage("property.should.resideInPackageMatching", prop.qualifiedName, prop.packageName),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.name.endsWith(suffix)) {
                violations.add(
                    getMessage("property.should.haveNameEndingWith", prop.qualifiedName, suffix),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = suffixes.any { prop.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameEndingWithAny", prop.qualifiedName, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.name.startsWith(prefix)) {
                violations.add(
                    getMessage("property.should.haveNameStartingWith", prop.qualifiedName, prefix),
                )
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = prefixes.any { prop.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameStartingWithAny", prop.qualifiedName, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, prop.declaration.name)) {
                violations.add(
                    getMessage("property.should.haveNameMatching", prop.qualifiedName, pattern),
                )
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, prop.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("property.should.haveNameMatchingAny", prop.qualifiedName, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): PropertiesRuleBuilder = haveNameMatching(patterns.toList())

    fun bePublic(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PUBLIC) {
                violations.add(
                    getMessage("property.should.bePublic", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    fun beInternal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.INTERNAL) {
                violations.add(
                    getMessage("property.should.beInternal", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    fun bePrivate(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PRIVATE) {
                violations.add(
                    getMessage("property.should.bePrivate", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    fun beProtected(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PROTECTED) {
                violations.add(
                    getMessage("property.should.beProtected", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    fun beVar(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.isVar) {
                violations.add(
                    getMessage("property.should.beVar", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beVal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.isVar) {
                violations.add(
                    getMessage("property.should.beVal", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    infix fun haveType(typeFqName: String): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.type != typeFqName) {
                violations.add(
                    getMessage("property.should.haveType", prop.qualifiedName, typeFqName, prop.declaration.type),
                )
            }
        }
        return builder
    }

    /** Asserts that selected properties have the specified raw type. */
    infix fun haveType(type: kotlin.reflect.KClass<*>): PropertiesRuleBuilder {
        val expectedType = type.toKontureTypeReference()
        builder.setShould { property, _, violations ->
            if (property.declaration.resolvedType?.let { matchesKotlinType(it, expectedType) } != true) {
                violations.add(
                    getMessage("property.should.haveType", property.declaration.name, type.kontureQualifiedName(), property.declaration.type),
                )
            }
        }
        return builder
    }

    /** Asserts that selected properties have the specified raw type. */
    inline fun <reified T : Any> haveTypeOf(): PropertiesRuleBuilder = haveType(T::class)

    infix fun haveType(typeFqNames: List<String>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!typeFqNames.contains(prop.declaration.type)) {
                violations.add(
                    getMessage("property.should.haveTypeAny", prop.qualifiedName, typeFqNames.joinToString(), prop.declaration.type),
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
                violations.add(
                    getMessage("property.should.haveAnnotation", prop.qualifiedName, annotationName),
                )
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
                    getMessage("property.should.haveAnnotationAny", prop.qualifiedName, annotationNames.joinToString()),
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
                violations.add(
                    getMessage("property.should.haveAllAnnotations", prop.qualifiedName, names.joinToString()),
                )
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
                    getMessage("property.should.haveAnyAnnotation", prop.qualifiedName, names.joinToString()),
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
                violations.add(
                    getMessage("property.should.haveModifier", prop.qualifiedName, modifier),
                )
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
                    getMessage("property.should.haveAllModifiers", prop.qualifiedName, modifiers.joinToString(), missing.joinToString()),
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
                    getMessage("property.should.haveAnyModifier", prop.qualifiedName, modifiers.joinToString()),
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
                    getMessage("property.should.haveVisibility", prop.qualifiedName, visibility, prop.declaration.visibility),
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
                    getMessage("property.should.haveAnyVisibility", prop.qualifiedName, visibilities.joinToString(), prop.declaration.visibility),
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
                violations.add(
                    getMessage("property.should.beExtension", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beConst(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.CONST)) {
                violations.add(
                    getMessage("property.should.beConst", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beLateinit(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.LATEINIT)) {
                violations.add(
                    getMessage("property.should.beLateinit", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    fun beDocumentedWithKDoc(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.kdocText.isNullOrBlank()) {
                violations.add(
                    getMessage("property.should.beDocumented", prop.qualifiedName),
                )
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
                violations.add(
                    getMessage("property.should.satisfyCustom", prop.qualifiedName, description),
                )
            }
        }
        return builder
    }

    fun satisfy(assertion: (PropertyDeclarationContext, MutableList<String>) -> Unit): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations -> assertion(prop, violations) }
        return builder
    }
}
