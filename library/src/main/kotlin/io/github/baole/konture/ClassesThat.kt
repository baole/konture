/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Fluent API for defining filtering conditions on Kotlin classes.
 */
@KontureDsl
class ClassesThat internal constructor(
    private val builder: ClassesRuleBuilder,
) {
    /**
     * Restricts the rules to classes residing in packages matching the specified pattern.
     * Supports `..` segment wildcards (e.g., `io.github.baole.konture..`).
     *
     * @param packagePattern Package matching pattern.
     */
    infix fun resideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /**
     * Restricts the rules to classes residing in packages matching any of the specified patterns.
     * Supports `..` segment wildcards (e.g., `io.github.baole.konture..`).
     *
     * @param packagePatterns List of package matching patterns.
     */
    infix fun resideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    /**
     * Restricts the rules to classes residing in packages matching any of the specified patterns.
     * Supports `..` segment wildcards.
     *
     * @param packagePatterns Package matching patterns.
     */
    fun resideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder = resideInAPackage(packagePatterns.toList())

    /**
     * Restricts the rules to classes residing in packages matching the specified predicate.
     *
     * @param predicate Predicate checking package name.
     */
    infix fun resideInAPackage(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    /**
     * Restricts the rules to classes whose simple names end with the specified suffix.
     *
     * @param suffix The suffix (e.g., "Repository", "Service").
     */
    infix fun haveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { it.name.endsWith(suffix) }
        return builder
    }

    /**
     * Restricts the rules to classes whose simple names end with any of the specified suffixes.
     *
     * @param suffixes The suffixes.
     */
    infix fun haveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    /**
     * Restricts the rules to classes whose simple names end with any of the specified suffixes.
     *
     * @param suffixes The suffixes.
     */
    fun haveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /**
     * Restricts the rules to classes whose simple names start with the specified prefix.
     *
     * @param prefix The prefix.
     */
    infix fun haveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { it.name.startsWith(prefix) }
        return builder
    }

    /**
     * Restricts the rules to classes whose simple names start with any of the specified prefixes.
     *
     * @param prefixes The prefixes.
     */
    infix fun haveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    /**
     * Restricts the rules to classes whose simple names start with any of the specified prefixes.
     *
     * @param prefixes The prefixes.
     */
    fun haveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /**
     * Restricts the rules to classes whose simple names match the specified predicate.
     *
     * @param predicate Predicate checking class simple name.
     */
    infix fun haveName(predicate: (String) -> Boolean): ClassesRuleBuilder = haveName("custom name predicate", predicate)

    /**
     * Restricts the rules to classes whose simple names match the specified predicate.
     *
     * @param description A descriptive string for the predicate used in violations.
     * @param predicate Predicate checking class simple name.
     */
    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setThat { predicate(it.name) }
        return builder
    }

    /**
     * Restricts the rules to classes whose simple names match the specified glob pattern.
     * Supports '*' wildcards.
     *
     * @param pattern Glob pattern (e.g. "*UseCase", "*Repository").
     */
    infix fun haveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    /**
     * Restricts the rules to classes whose simple names match any of the specified glob patterns.
     * Supports '*' wildcards.
     *
     * @param patterns Glob patterns.
     */
    infix fun haveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    /**
     * Restricts the rules to classes whose simple names match any of the specified glob patterns.
     * Supports '*' wildcards.
     *
     * @param patterns Glob patterns.
     */
    fun haveNameMatching(vararg patterns: String): ClassesRuleBuilder = haveNameMatching(patterns.toList())

    /**
     * Restricts the rules to classes annotated with the specified annotation.
     * Matches either the annotation's simple name or its FQN.
     *
     * @param annotationFqName The annotation name or fully qualified name.
     */
    infix fun haveAnnotationOf(annotationFqName: String): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.annotations.any { it.fqName == annotationFqName || it.name == annotationFqName }
        }
        return builder
    }

    /**
     * Restricts the rules to classes annotated with the specified annotation.
     * Matches either simple names or FQNs.
     */
    infix fun haveAllAnnotationsOf(name: String): ClassesRuleBuilder = haveAllAnnotationsOf(listOf(name))

    /**
     * Restricts the rules to classes annotated with all of the specified annotations.
     * Matches either simple names or FQNs.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    /**
     * Restricts the rules to classes annotated with all of the specified annotations.
     * Matches either simple names or FQNs.
     */
    fun haveAllAnnotationsOf(vararg names: String): ClassesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /**
     * Restricts the rules to classes annotated with the specified annotation.
     * Matches either simple names or FQNs.
     */
    infix fun haveAnyAnnotationOf(name: String): ClassesRuleBuilder = haveAnyAnnotationOf(listOf(name))

    /**
     * Restricts the rules to classes annotated with any of the specified annotations.
     * Matches either simple names or FQNs.
     */
    infix fun haveAnyAnnotationOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    /**
     * Restricts the rules to classes annotated with any of the specified annotations.
     * Matches either simple names or FQNs.
     */
    fun haveAnyAnnotationOf(vararg names: String): ClassesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /**
     * Restricts the rules to classes with specified annotation having a matching argument name and value.
     */
    fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String?,
        argValue: String,
    ): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.annotations.any { ann ->
                (ann.name == annotationName || ann.fqName == annotationName) &&
                    ann.arguments.any { arg ->
                        (argName == null || arg.name == argName) && arg.value == argValue
                    }
            }
        }
        return builder
    }

    /**
     * Restricts the rules to interface definitions only.
     */
    fun areInterfaces(): ClassesRuleBuilder {
        builder.setThat { it.isInterface }
        return builder
    }

    /**
     * Restricts the rules to enum classes only.
     */
    fun areEnums(): ClassesRuleBuilder {
        builder.setThat { it.isEnum }
        return builder
    }

    /**
     * Restricts the rules to abstract classes only.
     */
    fun areAbstract(): ClassesRuleBuilder {
        builder.setThat { it.isAbstract || it.isInterface }
        return builder
    }

    /**
     * Restricts the rules to classes with specified visibility.
     */
    infix fun haveVisibility(visibility: Visibility): ClassesRuleBuilder {
        builder.setThat { it.visibility == visibility }
        return builder
    }

    /**
     * Restricts the rules to classes with the specified visibility.
     *
     * @param visibility The acceptable visibility.
     */
    infix fun haveAnyVisibility(visibility: Visibility): ClassesRuleBuilder = haveAnyVisibility(listOf(visibility))

    /**
     * Restricts the rules to classes with any of the specified visibilities.
     *
     * @param visibilities The list of acceptable visibilities.
     */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): ClassesRuleBuilder {
        builder.setThat { cls -> visibilities.contains(cls.visibility) }
        return builder
    }

    /**
     * Restricts the rules to classes with any of the specified visibilities.
     *
     * @param visibilities The vararg list of acceptable visibilities.
     */
    fun haveAnyVisibility(vararg visibilities: Visibility): ClassesRuleBuilder = haveAnyVisibility(visibilities.asList())

    fun bePublic(): ClassesRuleBuilder = haveVisibility(Visibility.PUBLIC)

    fun beInternal(): ClassesRuleBuilder = haveVisibility(Visibility.INTERNAL)

    fun bePrivate(): ClassesRuleBuilder = haveVisibility(Visibility.PRIVATE)

    fun beProtected(): ClassesRuleBuilder = haveVisibility(Visibility.PROTECTED)

    /**
     * Restricts the rules to classes containing specified modifier.
     */
    infix fun haveModifier(modifier: Modifier): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(modifier) }
        return builder
    }

    /**
     * Restricts the rules to classes containing specified modifier.
     *
     * @param modifier The modifier that must be present.
     */
    infix fun haveAnyModifier(modifier: Modifier): ClassesRuleBuilder = haveAnyModifier(listOf(modifier))

    /**
     * Restricts the rules to classes containing any of the specified modifiers.
     *
     * @param modifiers The list of modifiers, at least one of which must be present.
     */
    infix fun haveAnyModifier(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.any { cls.modifiers.contains(it) } }
        return builder
    }

    /**
     * Restricts the rules to classes containing any of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers, at least one of which must be present.
     */
    fun haveAnyModifier(vararg modifiers: Modifier): ClassesRuleBuilder = haveAnyModifier(modifiers.asList())

    /**
     * Restricts the rules to classes containing specified modifier.
     *
     * @param modifier The modifier that must be present.
     */
    infix fun haveAllModifiers(modifier: Modifier): ClassesRuleBuilder = haveAllModifiers(listOf(modifier))

    /**
     * Restricts the rules to classes containing all of the specified modifiers.
     *
     * @param modifiers The list of modifiers that must all be present.
     */
    infix fun haveAllModifiers(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.all { cls.modifiers.contains(it) } }
        return builder
    }

    /**
     * Restricts the rules to classes containing all of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers that must all be present.
     */
    fun haveAllModifiers(vararg modifiers: Modifier): ClassesRuleBuilder = haveAllModifiers(modifiers.asList())

    fun beSealed(): ClassesRuleBuilder = haveModifier(Modifier.SEALED)

    fun beData(): ClassesRuleBuilder = haveModifier(Modifier.DATA)

    fun beInline(): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(Modifier.INLINE) || it.modifiers.contains(Modifier.VALUE) }
        return builder
    }

    /**
     * Restricts the rules to classes extending or implementing the specified type.
     */
    infix fun areAssignableTo(superType: String): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { it.isAssignableTo(superType, allClasses) }
        return builder
    }

    /**
     * Restricts the rules to classes extending or implementing the specified supertype.
     *
     * @param superType The supertype that must be matched.
     */
    infix fun areAssignableToAnyOf(superType: String): ClassesRuleBuilder = areAssignableToAnyOf(listOf(superType))

    /**
     * Restricts the rules to classes extending or implementing any of the specified supertypes.
     *
     * @param superTypes The list of supertypes, at least one of which must be matched.
     */
    infix fun areAssignableToAnyOf(superTypes: List<String>): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.any { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /**
     * Restricts the rules to classes extending or implementing any of the specified supertypes.
     *
     * @param superTypes The vararg list of supertypes, at least one of which must be matched.
     */
    fun areAssignableToAnyOf(vararg superTypes: String): ClassesRuleBuilder = areAssignableToAnyOf(superTypes.asList())

    /**
     * Restricts the rules to classes extending or implementing the specified supertype.
     *
     * @param superType The supertype that must be matched.
     */
    infix fun areAssignableToAllOf(superType: String): ClassesRuleBuilder = areAssignableToAllOf(listOf(superType))

    /**
     * Restricts the rules to classes extending or implementing all of the specified supertypes.
     *
     * @param superTypes The list of supertypes that must all be matched.
     */
    infix fun areAssignableToAllOf(superTypes: List<String>): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /**
     * Restricts the rules to classes extending or implementing all of the specified supertypes.
     *
     * @param superTypes The vararg list of supertypes that must all be matched.
     */
    fun areAssignableToAllOf(vararg superTypes: String): ClassesRuleBuilder = areAssignableToAllOf(superTypes.asList())

    /**
     * Restricts the rules to classes that are assignable from the specified subtype.
     *
     * @param subType The subtype that must extend or implement the selected classes.
     */
    infix fun areAssignableFrom(subType: String): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            val subTypeDecl = allClasses.find { it.fqName == subType || it.name == subType }
            if (subTypeDecl != null) {
                subTypeDecl.fqName == cls.fqName ||
                    subTypeDecl.isAssignableTo(cls.fqName, allClasses) ||
                    subTypeDecl.isAssignableTo(cls.name, allClasses)
            } else {
                subType == cls.fqName || subType == cls.name
            }
        }
        return builder
    }

    /**
     * Restricts the rules to classes matching the specified predicate.
     *
     * @param predicate The predicate to filter classes.
     */
    infix fun matching(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Satisfies an arbitrary custom predicate logic.
     */
    fun satisfy(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Restricts the rules to classes that are documented with a KDoc comment.
     */
    fun beDocumentedWithKDoc(): ClassesRuleBuilder {
        builder.setThat { it.kdocText?.isNotBlank() == true }
        return builder
    }

    /**
     * Matches if any of the nested condition blocks are satisfied.
     */
    fun anyOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /**
     * Matches if all of the nested condition blocks are satisfied.
     */
    fun allOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /**
     * Matches if none of the nested condition blocks are satisfied.
     */
    fun noneOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }
}
