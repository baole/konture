/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class PropertiesThat internal constructor(
    private val builder: PropertiesRuleBuilder,
) {
    infix fun resideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    infix fun resideInAPackage(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): PropertiesRuleBuilder = haveNameMatching(patterns.toList())

    fun beTopLevel(): PropertiesRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    fun beMember(): PropertiesRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    /**
     * Restricts the rules to properties annotated with the specified annotation.
     * Matches either the annotation's simple name or its FQN.
     *
     * @param annotationName The annotation name or fully qualified name.
     */
    infix fun haveAnnotationOf(annotationName: String): PropertiesRuleBuilder {
        builder.setThat { it.hasAnnotation(annotationName) }
        return builder
    }

    /**
     * Restricts the rules to properties annotated with any of the specified annotations.
     */
    infix fun haveAnnotationOf(annotationNames: List<String>): PropertiesRuleBuilder {
        builder.setThat { prop -> annotationNames.any { prop.hasAnnotation(it) } }
        return builder
    }

    /**
     * Restricts the rules to properties annotated with any of the specified annotations.
     */
    fun haveAnnotationOf(vararg annotationNames: String): PropertiesRuleBuilder = haveAnnotationOf(annotationNames.asList())

    /**
     * Restricts the rules to properties annotated with all of the specified annotations.
     * Matches either simple names or FQNs.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): PropertiesRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    /**
     * Restricts the rules to properties annotated with all of the specified annotations.
     * Matches either simple names or FQNs.
     */
    fun haveAllAnnotationsOf(vararg names: String): PropertiesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /**
     * Restricts the rules to properties annotated with any of the specified annotations.
     * Matches either simple names or FQNs.
     */
    infix fun haveAnyAnnotationOf(names: List<String>): PropertiesRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    /**
     * Restricts the rules to properties annotated with any of the specified annotations.
     * Matches either simple names or FQNs.
     */
    fun haveAnyAnnotationOf(vararg names: String): PropertiesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /**
     * Restricts the rules to properties containing the specified modifier.
     */
    infix fun haveModifier(modifier: Modifier): PropertiesRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(modifier) }
        return builder
    }

    /**
     * Restricts the rules to properties containing all of the specified modifiers.
     *
     * @param modifiers The list of modifiers that must all be present.
     */
    infix fun haveAllModifiers(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setThat { prop -> modifiers.all { prop.declaration.modifiers.contains(it) } }
        return builder
    }

    /**
     * Restricts the rules to properties containing all of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers that must all be present.
     */
    fun haveAllModifiers(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAllModifiers(modifiers.asList())

    /**
     * Restricts the rules to properties containing any of the specified modifiers.
     *
     * @param modifiers The list of modifiers, at least one of which must be present.
     */
    infix fun haveAnyModifier(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setThat { prop -> modifiers.any { prop.declaration.modifiers.contains(it) } }
        return builder
    }

    /**
     * Restricts the rules to properties containing any of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers, at least one of which must be present.
     */
    fun haveAnyModifier(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAnyModifier(modifiers.asList())

    /**
     * Restricts the rules to properties with the specified visibility.
     */
    infix fun haveVisibility(visibility: Visibility): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility == visibility }
        return builder
    }

    /**
     * Restricts the rules to properties with any of the specified visibilities.
     *
     * @param visibilities The list of acceptable visibilities.
     */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): PropertiesRuleBuilder {
        builder.setThat { prop -> visibilities.contains(prop.declaration.visibility) }
        return builder
    }

    /**
     * Restricts the rules to properties with any of the specified visibilities.
     *
     * @param visibilities The vararg list of acceptable visibilities.
     */
    fun haveAnyVisibility(vararg visibilities: Visibility): PropertiesRuleBuilder = haveAnyVisibility(visibilities.asList())

    /**
     * Restricts the rules to properties with the specified type (simple or fully qualified).
     */
    infix fun haveType(typeFqName: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.type == typeFqName }
        return builder
    }

    /**
     * Restricts the rules to properties with any of the specified types.
     */
    infix fun haveType(typeFqNames: List<String>): PropertiesRuleBuilder {
        builder.setThat { prop -> typeFqNames.contains(prop.declaration.type) }
        return builder
    }

    /**
     * Restricts the rules to properties with any of the specified types.
     */
    fun haveType(vararg typeFqNames: String): PropertiesRuleBuilder = haveType(typeFqNames.asList())

    infix fun satisfy(predicate: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }
}
