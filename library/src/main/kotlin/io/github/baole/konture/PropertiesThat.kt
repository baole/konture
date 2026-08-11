/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/** Filter builder for selecting property declarations matching specific conditions. */
@KontureDsl
public class PropertiesThat internal constructor(
    private val builder: PropertiesRuleBuilder,
) {
    /** Logical NOT operator for negating the next filter condition. */
    fun not(): PropertiesThat = builder.not()

    /** Filter or assertion criteria for reside in a package. */
    infix fun resideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    infix fun resideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    fun resideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    /** Filter or assertion criteria for reside in a package. */
    infix fun resideInAPackage(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    /** Filter or assertion criteria for reside in package of. */
    infix fun resideInPackageOf(type: KClass<*>): PropertiesRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    /** Filter or assertion criteria for reside in a module. */
    infix fun resideInAModule(modulePath: String): PropertiesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { it.modulePath == normalized }
        return builder
    }

    /** Filter or assertion criteria for reside in a module. */
    infix fun resideInAModule(modulePaths: List<String>): PropertiesRuleBuilder {
        /** Filter or assertion criteria for normalized paths. */
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

    /** Filter or assertion criteria for reside in a module. */
    fun resideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for reside in module. */
    infix fun resideInModule(modulePath: String): PropertiesRuleBuilder = resideInAModule(modulePath)

    /** Filter or assertion criteria for reside in modules. */
    infix fun resideInModules(modulePaths: List<String>): PropertiesRuleBuilder = resideInAModule(modulePaths)

    /** Filter or assertion criteria for reside in modules. */
    fun resideInModules(vararg modulePaths: String): PropertiesRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in a module. */
    infix fun notResideInAModule(modulePath: String): PropertiesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { context ->
            /** Filter or assertion criteria for match. */
            val match =
                context.modulePath == normalized || PatternMatchers.matchesModuleGlob(normalized, context.modulePath)
            !match
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in a module. */
    infix fun notResideInAModule(modulePaths: List<String>): PropertiesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setThat { context ->
            /** Filter or assertion criteria for match. */
            val match =
                normalized.any { target ->
                    context.modulePath == target || PatternMatchers.matchesModuleGlob(target, context.modulePath)
                }
            !match
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in a module. */
    fun notResideInAModule(vararg modulePaths: String): PropertiesRuleBuilder = notResideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in module. */
    infix fun notResideInModule(modulePath: String): PropertiesRuleBuilder = notResideInAModule(modulePath)

    /** Filter or assertion criteria for not reside in modules. */
    infix fun notResideInModules(modulePaths: List<String>): PropertiesRuleBuilder = notResideInAModule(modulePaths)

    /** Filter or assertion criteria for not reside in modules. */
    fun notResideInModules(vararg modulePaths: String): PropertiesRuleBuilder = notResideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for have name. */
    infix fun haveName(name: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name == name }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    infix fun haveName(names: List<String>): PropertiesRuleBuilder {
        builder.setThat { names.contains(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    fun haveName(vararg names: String): PropertiesRuleBuilder = haveName(names.toList())

    /** Filter or assertion criteria for not have name. */
    infix fun notHaveName(name: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name != name }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    infix fun notHaveName(names: List<String>): PropertiesRuleBuilder {
        builder.setThat { !names.contains(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    fun notHaveName(vararg names: String): PropertiesRuleBuilder = notHaveName(names.toList())

    /** Filter or assertion criteria for not have name. */
    infix fun notHaveName(predicate: (String) -> Boolean): PropertiesRuleBuilder {
        builder.setThat { !predicate(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    infix fun haveName(predicate: (String) -> Boolean): PropertiesRuleBuilder =
        haveName("custom name predicate", predicate)

    /** Filter or assertion criteria for have name. */
    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): PropertiesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    infix fun haveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    infix fun haveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    fun haveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for not have name ending with. */
    infix fun notHaveNameEndingWith(suffix: String): PropertiesRuleBuilder {
        builder.setThat { !it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    infix fun notHaveNameEndingWith(suffixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    fun notHaveNameEndingWith(vararg suffixes: String): PropertiesRuleBuilder = notHaveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have name starting with. */
    infix fun haveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    infix fun haveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    fun haveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for not have name starting with. */
    infix fun notHaveNameStartingWith(prefix: String): PropertiesRuleBuilder {
        builder.setThat { !it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    infix fun notHaveNameStartingWith(prefixes: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    fun notHaveNameStartingWith(vararg prefixes: String): PropertiesRuleBuilder =
        notHaveNameStartingWith(
            prefixes.toList(),
        )

    /** Filter or assertion criteria for have name matching. */
    infix fun haveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    infix fun haveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    fun haveNameMatching(vararg patterns: String): PropertiesRuleBuilder = haveNameMatching(patterns.toList())

    /** Filter or assertion criteria for not have name matching. */
    infix fun notHaveNameMatching(pattern: String): PropertiesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    infix fun notHaveNameMatching(patterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    fun notHaveNameMatching(vararg patterns: String): PropertiesRuleBuilder = notHaveNameMatching(patterns.toList())

    /** Filter or assertion criteria for not be public. */
    fun notBePublic(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PUBLIC }
        return builder
    }

    /** Filter or assertion criteria for not be internal. */
    fun notBeInternal(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.INTERNAL }
        return builder
    }

    /** Filter or assertion criteria for not be private. */
    fun notBePrivate(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PRIVATE }
        return builder
    }

    /** Filter or assertion criteria for not be protected. */
    fun notBeProtected(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PROTECTED }
        return builder
    }

    /** Filter or assertion criteria for be top level. */
    fun beTopLevel(): PropertiesRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    /** Filter or assertion criteria for be member. */
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

    /** Filter or assertion criteria for are open. */
    fun areOpen(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    /** Filter or assertion criteria for are abstract. */
    fun areAbstract(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.ABSTRACT) }
        return builder
    }

    /** Filter or assertion criteria for are override. */
    fun areOverride(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

    /** Filter or assertion criteria for have modifier. */
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
        /** Filter or assertion criteria for expected type. */
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

    /** Filter or assertion criteria for are extension. */
    fun areExtension(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.isExtension }
        return builder
    }

    /** Filter or assertion criteria for are top level. */
    fun areTopLevel(): PropertiesRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    /** Filter or assertion criteria for are member. */
    fun areMember(): PropertiesRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    /** Filter or assertion criteria for have annotation with argument. */
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

    /** Filter or assertion criteria for satisfy. */
    infix fun satisfy(predicate: (PropertyDeclarationContext) -> Boolean): PropertiesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for be val. */
    fun beVal(): PropertiesRuleBuilder {
        builder.setThat { !it.declaration.isVar }
        return builder
    }

    /** Filter or assertion criteria for be var. */
    fun beVar(): PropertiesRuleBuilder {
        builder.setThat { it.declaration.isVar }
        return builder
    }

    /** Filter or assertion criteria for be const. */
    fun beConst(): PropertiesRuleBuilder = haveModifier(Modifier.CONST)

    /** Filter or assertion criteria for be lateinit. */
    fun beLateinit(): PropertiesRuleBuilder = haveModifier(Modifier.LATEINIT)

    /** Filter or assertion criteria for any of. */
    fun anyOf(vararg blocks: PropertiesThat.() -> Unit): PropertiesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = PropertiesRuleBuilder(builder.graph)
                PropertiesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    fun allOf(vararg blocks: PropertiesThat.() -> Unit): PropertiesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = PropertiesRuleBuilder(builder.graph)
                PropertiesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    fun noneOf(vararg blocks: PropertiesThat.() -> Unit): PropertiesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = PropertiesRuleBuilder(builder.graph)
                PropertiesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }

    @JvmName("haveTypesByKClass")
    /** Filter or assertion criteria for have type. */
    infix fun haveType(types: List<KClass<*>>): PropertiesRuleBuilder {
        /** Filter or assertion criteria for expected types. */
        val expectedTypes = types.map { it.toKontureTypeReference() }
        builder.setThat { prop ->
            prop.declaration.resolvedType?.let {
                    resolved ->
                expectedTypes.any { matchesKotlinType(resolved, it) }
            } == true
        }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    infix fun notResideInAPackage(packagePattern: String): PropertiesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    infix fun notResideInAPackage(packagePatterns: List<String>): PropertiesRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    fun notResideInAPackage(vararg packagePatterns: String): PropertiesRuleBuilder =
        notResideInAPackage(
            packagePatterns.toList(),
        )

    /** Filter or assertion criteria for not have annotation of. */
    infix fun notHaveAnnotationOf(annotationName: String): PropertiesRuleBuilder {
        builder.setThat { !it.hasAnnotation(annotationName) }
        return builder
    }

    /** Filter or assertion criteria for not have annotation of. */
    infix fun notHaveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
        notHaveAnnotationOf(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for have import of. */
    infix fun haveImportOf(importFqName: String): PropertiesRuleBuilder {
        builder.setThat { prop ->
            /** Filter or assertion criteria for imports. */
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

    /** Filter or assertion criteria for have import of. */
    infix fun haveImportOf(importFqNames: List<String>): PropertiesRuleBuilder {
        builder.setThat { prop ->
            /** Filter or assertion criteria for imports. */
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

    /** Filter or assertion criteria for have import of. */
    fun haveImportOf(vararg importFqNames: String): PropertiesRuleBuilder = haveImportOf(importFqNames.toList())

    /** Filter or assertion criteria for have import of. */
    infix fun haveImportOf(type: KClass<*>): PropertiesRuleBuilder = haveImportOf(type.kontureQualifiedName())

    /** Filter or assertion criteria for not have import of. */
    infix fun notHaveImportOf(importFqName: String): PropertiesRuleBuilder {
        builder.setThat { prop ->
            /** Filter or assertion criteria for imports. */
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

    /** Filter or assertion criteria for not have import of. */
    infix fun notHaveImportOf(type: KClass<*>): PropertiesRuleBuilder = notHaveImportOf(type.kontureQualifiedName())
}
