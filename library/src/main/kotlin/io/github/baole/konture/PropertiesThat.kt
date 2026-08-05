/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

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

    infix fun resideInPackageOf(type: KClass<*>): PropertiesRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    infix fun resideInAModule(modulePath: String): PropertiesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
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
                    ":$path"
                } else {
                    path
                }
            }
        builder.setThat { context -> normalizedPaths.contains(context.modulePath) }
        return builder
    }

    fun resideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun resideInModule(modulePath: String): PropertiesRuleBuilder = resideInAModule(modulePath)

    infix fun resideInModules(modulePaths: List<String>): PropertiesRuleBuilder = resideInAModule(modulePaths)

    fun resideInModules(vararg modulePaths: String): PropertiesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): PropertiesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { context ->
            val match =
                context.modulePath == normalized || PatternMatchers.matchesModuleGlob(normalized, context.modulePath)
            !match
        }
        return builder
    }

    infix fun notResideInAModule(modulePaths: List<String>): PropertiesRuleBuilder {
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setThat { context ->
            val match =
                normalized.any { target ->
                    context.modulePath == target || PatternMatchers.matchesModuleGlob(target, context.modulePath)
                }
            !match
        }
        return builder
    }

    fun notResideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun notResideInModule(modulePath: String): PropertiesRuleBuilder = notResideInAModule(modulePath)

    infix fun notResideInModules(modulePaths: List<String>): PropertiesRuleBuilder = notResideInAModule(modulePaths)

    fun notResideInModules(vararg modulePaths: String): PropertiesRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun haveName(name: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name == name }
        return builder
    }

    infix fun haveName(names: List<String>): PropertiesRuleBuilder {
        builder.setThat { names.contains(it.declaration.name) }
        return builder
    }

    fun haveName(vararg names: String): PropertiesRuleBuilder = haveName(names.toList())

    infix fun notHaveName(name: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name != name }
        return builder
    }

    infix fun notHaveName(names: List<String>): PropertiesRuleBuilder {
        builder.setThat { !names.contains(it.declaration.name) }
        return builder
    }

    fun notHaveName(vararg names: String): PropertiesRuleBuilder = notHaveName(names.toList())

    infix fun notHaveName(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { !predicate(it.declaration.name) }
        return builder
    }

    infix fun haveName(predicate: (String) -> Boolean): PropertiesRuleBuilder =
        haveName("custom name predicate", predicate)

    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): PropertiesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
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

    infix fun notHaveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setThat { !it.declaration.name.endsWith(suffix) }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = notHaveNameEndingWith(suffixes.toList())

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

    infix fun notHaveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setThat { !it.declaration.name.startsWith(prefix) }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

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

    infix fun notHaveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): PropertiesRuleBuilder = notHaveNameMatching(patterns.toList())

    fun notBePublic(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PUBLIC }
        return builder
    }

    fun notBeInternal(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.INTERNAL }
        return builder
    }

    fun notBePrivate(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PRIVATE }
        return builder
    }

    fun notBeProtected(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PROTECTED }
        return builder
    }

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
    fun haveAnnotationOf(vararg annotationNames: String): PropertiesRuleBuilder =
        haveAnnotationOf(annotationNames.asList())

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

    fun areOpen(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    fun areAbstract(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.ABSTRACT) }
        return builder
    }

    fun areOverride(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

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
    fun haveAnyVisibility(vararg visibilities: Visibility): PropertiesRuleBuilder =
        haveAnyVisibility(visibilities.asList())

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
        builder.setThat {
                property ->
            property.declaration.resolvedType?.let { matchesKotlinType(it, expectedType) } == true
        }
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

    fun areExtension(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.isExtension }
        return builder
    }

    fun areTopLevel(): PropertiesRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    fun areMember(): PropertiesRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String?,
        argValue: String,
    ): PropertiesRuleBuilder {
        builder.setThat { prop ->
            prop.declaration.annotations.any { ann ->
                (ann.name == annotationName || ann.fqName == annotationName) &&
                    ann.arguments.any { arg ->
                        (argName == null || arg.name == argName) && arg.value == argValue
                    }
            }
        }
        return builder
    }

    infix fun satisfy(predicate: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    fun beVal(): PropertiesRuleBuilder {
        builder.setThat { !it.declaration.isVar }
        return builder
    }

    fun beVar(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.isVar }
        return builder
    }

    fun beConst(): PropertiesRuleBuilder = haveModifier(Modifier.CONST)

    fun beLateinit(): PropertiesRuleBuilder = haveModifier(Modifier.LATEINIT)

    fun anyOf(vararg blocks: PropertiesThat.() -> Unit): PropertiesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = PropertiesRuleBuilder(builder.graph)
                PropertiesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    fun allOf(vararg blocks: PropertiesThat.() -> Unit): PropertiesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = PropertiesRuleBuilder(builder.graph)
                PropertiesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    fun noneOf(vararg blocks: PropertiesThat.() -> Unit): PropertiesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = PropertiesRuleBuilder(builder.graph)
                PropertiesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }

    @JvmName("haveTypesByKClass")
    infix fun haveType(types: List<KClass<*>>): PropertiesRuleBuilder {
        val expectedTypes = types.map { it.toKontureTypeReference() }
        builder.setThat { prop ->
            prop.declaration.resolvedType?.let {
                    resolved ->
                expectedTypes.any { matchesKotlinType(resolved, it) }
            } == true
        }
        return builder
    }

    infix fun notResideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    infix fun notResideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    fun notResideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        notResideInAPackage(
            packagePatterns.toList(),
        )

    infix fun notHaveAnnotationOf(annotationName: String): PropertiesRuleBuilder {
        builder.setThat { !it.hasAnnotation(annotationName) }
        return builder
    }

    infix fun notHaveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
        notHaveAnnotationOf(annotation.kontureQualifiedName())

    infix fun haveImportOf(importFqName: String): PropertiesRuleBuilder {
        builder.setThat { prop ->
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find {
                            file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()
            imports.any { it == importFqName || PatternMatchers.matchesSimpleGlob(importFqName, it) }
        }
        return builder
    }

    infix fun haveImportOf(importFqNames: List<String>): PropertiesRuleBuilder {
        builder.setThat { prop ->
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find {
                            file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()
            imports.any { imp -> importFqNames.any { imp == it || PatternMatchers.matchesSimpleGlob(it, imp) } }
        }
        return builder
    }

    fun haveImportOf(vararg importFqNames: String): PropertiesRuleBuilder = haveImportOf(importFqNames.toList())

    infix fun haveImportOf(type: KClass<*>): PropertiesRuleBuilder = haveImportOf(type.kontureQualifiedName())

    infix fun notHaveImportOf(importFqName: String): PropertiesRuleBuilder {
        builder.setThat { prop ->
            val imports =
                builder.graph.getAllModules().flatMap { it.files }
                    .find {
                            file ->
                        file.filePath == prop.filePath || (prop.className != null && file.classes.any { it.name == prop.className })
                    }
                    ?.imports.orEmpty()
            imports.none { it == importFqName || PatternMatchers.matchesSimpleGlob(importFqName, it) }
        }
        return builder
    }

    infix fun notHaveImportOf(type: KClass<*>): PropertiesRuleBuilder = notHaveImportOf(type.kontureQualifiedName())
}
