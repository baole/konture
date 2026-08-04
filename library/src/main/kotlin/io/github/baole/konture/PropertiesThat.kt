/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/**
 * Fluent API for defining filtering conditions on Kotlin properties.
 */
@KontureDsl
class PropertiesThat internal constructor(
    private val builder: PropertiesRuleBuilder,
) {
    /**
     * Restricts the rules to properties residing in packages matching the specified pattern.
     * Supports `..` segment wildcards.
     *
     * @param packagePattern Package matching pattern.
     */
    infix fun resideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /**
     * Restricts the rules to properties residing in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns List of package matching patterns.
     */
    infix fun resideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    /**
     * Restricts the rules to properties residing in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns Package matching patterns.
     */
    fun resideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    /**
     * Restricts the rules to properties residing in packages matching the specified predicate.
     *
     * @param predicate Predicate checking package name.
     */
    infix fun resideInAPackage(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple names end with the specified suffix.
     *
     * @param suffix The suffix.
     */
    infix fun haveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple names end with any of the specified suffixes.
     *
     * @param suffixes List of suffixes.
     */
    infix fun haveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple names end with any of the specified suffixes.
     *
     * @param suffixes Suffixes.
     */
    fun haveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /**
     * Restricts the rules to properties whose simple names start with the specified prefix.
     *
     * @param prefix The prefix.
     */
    infix fun haveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple names start with any of the specified prefixes.
     *
     * @param prefixes List of prefixes.
     */
    infix fun haveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple names start with any of the specified prefixes.
     *
     * @param prefixes Prefixes.
     */
    fun haveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /**
     * Restricts the rules to properties whose simple names match the specified glob pattern.
     *
     * @param pattern Glob pattern.
     */
    infix fun haveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple names match any of the specified glob patterns.
     *
     * @param patterns List of glob patterns.
     */
    infix fun haveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple names match any of the specified glob patterns.
     *
     * @param patterns Glob patterns.
     */
    fun haveNameMatching(vararg patterns: String): PropertiesRuleBuilder = haveNameMatching(patterns.toList())

    /**
     * Restricts the rules to top-level properties (not enclosed within any class).
     */
    fun beTopLevel(): PropertiesRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    /**
     * Restricts the rules to member properties declared inside a class or interface.
     */
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

    /** Restricts the rules to properties with the specified raw type. */
    infix fun haveType(type: KClass<*>): PropertiesRuleBuilder {
        val expectedType = type.toKontureTypeReference()
        builder.setThat { property -> property.declaration.resolvedType?.let { matchesKotlinType(it, expectedType) } == true }
        return builder
    }

    /** Restricts the rules to properties with the specified raw type. */
    inline fun <reified T : Any> haveTypeOf(): PropertiesRuleBuilder = haveType(T::class)

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

    /**
     * Restricts the rules to properties satisfying an arbitrary custom predicate logic.
     *
     * @param predicate Predicate checking [PropertyDeclarationContext].
     */
    infix fun satisfy(predicate: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Restricts the rules to properties residing in modules matching the specified glob pattern.
     *
     * @param modulePath Module path glob pattern (e.g. `:core`, `:feature-*`).
     */
    infix fun resideInAModule(modulePath: String): PropertiesRuleBuilder {
        builder.setThat { PatternMatchers.matchesModuleGlob(modulePath, it.modulePath) }
        return builder
    }

    /**
     * Restricts the rules to properties residing in modules matching any of the specified glob patterns.
     *
     * @param modulePaths List of module path glob patterns.
     */
    infix fun resideInAModule(modulePaths: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            modulePaths.any { PatternMatchers.matchesModuleGlob(it, context.modulePath) }
        }
        return builder
    }

    /**
     * Restricts the rules to properties residing in modules matching any of the specified glob patterns.
     *
     * @param modulePaths Module path glob patterns.
     */
    fun resideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = resideInAModule(modulePaths.toList())

    /**
     * Restricts the rules to member properties declared inside a class matching the specified pattern.
     *
     * @param classNamePattern Class name wildcard or package pattern.
     */
    infix fun belongToClass(classNamePattern: String): PropertiesRuleBuilder {
        builder.setThat { context ->
            context.className?.let {
                PatternMatchers.matchesPackage(classNamePattern, it) || PatternMatchers.matchesSimpleGlob(classNamePattern, it)
            } == true
        }
        return builder
    }

    /**
     * Restricts the rules to member properties declared inside [kClass].
     *
     * @param kClass The enclosing class.
     */
    infix fun belongToClass(kClass: KClass<*>): PropertiesRuleBuilder = belongToClass(kClass.kontureQualifiedName())

    /**
     * Restricts the rules to member properties declared inside [T].
     */
    inline fun <reified T : Any> belongToClass(): PropertiesRuleBuilder = belongToClass(T::class)

    /**
     * Restricts the rules to member properties declared inside a class matching the predicate.
     *
     * @param predicate Predicate checking enclosing class name.
     */
    infix fun belongToClass(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { context -> context.className?.let(predicate) == true }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple name satisfies the given predicate.
     *
     * @param predicate Predicate checking property simple name.
     */
    infix fun haveName(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    /**
     * Restricts the rules to properties whose simple name satisfies the given predicate.
     *
     * @param description Descriptive string for rule violation messages.
     * @param predicate Predicate checking property simple name.
     */
    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): PropertiesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }
}
