/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.core.KontureLogger
import io.github.baole.konture.core.LogLevel
import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

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

    infix fun satisfy(predicate: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    infix fun resideInAModule(modulePath: String): PropertiesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                KontureLogger.log(
                    LogLevel.WARNING,
                    "Module path '$modulePath' lacks a leading colon (':'). Suggest matching with ':$modulePath' instead.",
                )
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { it.modulePath == normalized }
        return builder
    }

    infix fun resideInAModule(modulePaths: List<String>): PropertiesRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    KontureLogger.log(
                        LogLevel.WARNING,
                        "Module path '$path' lacks a leading colon (':'). Suggest matching with ':$path' instead.",
                    )
                    ":$path"
                } else {
                    path
                }
            }
        builder.setThat { context ->
            normalizedPaths.any { context.modulePath == it }
        }
        return builder
    }

    fun resideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun belongToClass(classNamePattern: String): PropertiesRuleBuilder {
        builder.setThat { context ->
            context.className?.let {
                PatternMatchers.matchesPackage(classNamePattern, it) || PatternMatchers.matchesSimpleGlob(classNamePattern, it)
            } == true
        }
        return builder
    }

    infix fun belongToClass(kClass: KClass<*>): PropertiesRuleBuilder = belongToClass(kClass.kontureQualifiedName())

    inline fun <reified T : Any> belongToClass(): PropertiesRuleBuilder = belongToClass(T::class)

    infix fun belongToClass(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { context -> context.className?.let(predicate) == true }
        return builder
    }

    infix fun haveName(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): PropertiesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }
}
