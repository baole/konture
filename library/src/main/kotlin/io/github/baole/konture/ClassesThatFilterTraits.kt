/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/**
 * Common scope interface providing access to [ClassesRuleBuilder].
 */
interface ClassesThatScope {
    val builder: ClassesRuleBuilder
}

/**
 * Trait interface for package and module residency filtering on classes.
 */
@Suppress("ComplexInterface")
interface ClassesThatPackageFilter : ClassesThatScope {
    infix fun resideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    infix fun resideInAPackage(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    infix fun resideInPackageOf(type: KClass<*>): ClassesRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    infix fun notResideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    infix fun notResideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    fun notResideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder =
        notResideInAPackage(packagePatterns.toList())

    infix fun resideInAModule(modulePath: String): ClassesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { cls ->
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module?.path == normalized
        }
        return builder
    }

    infix fun resideInAModule(modulePaths: List<String>): ClassesRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setThat { cls ->
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module != null && normalizedPaths.contains(module.path)
        }
        return builder
    }

    fun resideInAModule(vararg modulePaths: String): ClassesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun resideInModule(modulePath: String): ClassesRuleBuilder = resideInAModule(modulePath)

    infix fun resideInModules(modulePaths: List<String>): ClassesRuleBuilder = resideInAModule(modulePaths)

    fun resideInModules(vararg modulePaths: String): ClassesRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): ClassesRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { cls ->
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module == null || (module.path != normalized && !PatternMatchers.matchesModuleGlob(normalized, module.path))
        }
        return builder
    }

    infix fun notResideInAModule(modulePaths: List<String>): ClassesRuleBuilder {
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setThat { cls ->
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module == null ||
                normalized.none { target ->
                    module.path == target || PatternMatchers.matchesModuleGlob(target, module.path)
                }
        }
        return builder
    }

    fun notResideInAModule(vararg modulePaths: String): ClassesRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun notResideInModule(modulePath: String): ClassesRuleBuilder = notResideInAModule(modulePath)

    infix fun notResideInModules(modulePaths: List<String>): ClassesRuleBuilder = notResideInAModule(modulePaths)

    fun notResideInModules(vararg modulePaths: String): ClassesRuleBuilder = notResideInAModule(modulePaths.toList())
}

/**
 * Trait interface for name matching and naming pattern filtering on classes.
 */
@Suppress("ComplexInterface")
interface ClassesThatNameFilter : ClassesThatScope {
    infix fun haveName(name: String): ClassesRuleBuilder {
        builder.setThat { it.fqName == name || it.name == name }
        return builder
    }

    infix fun haveSimpleName(name: String): ClassesRuleBuilder {
        builder.setThat { it.name == name }
        return builder
    }

    infix fun haveName(names: List<String>): ClassesRuleBuilder {
        builder.setThat { names.contains(it.name) }
        return builder
    }

    fun haveName(vararg names: String): ClassesRuleBuilder = haveName(names.toList())

    infix fun notHaveName(name: String): ClassesRuleBuilder {
        builder.setThat { it.name != name }
        return builder
    }

    infix fun notHaveName(names: List<String>): ClassesRuleBuilder {
        builder.setThat { !names.contains(it.name) }
        return builder
    }

    fun notHaveName(vararg names: String): ClassesRuleBuilder = notHaveName(names.toList())

    infix fun notHaveName(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { !predicate(it.name) }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { it.name.endsWith(suffix) }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun notHaveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { !it.name.endsWith(suffix) }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = notHaveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { it.name.startsWith(prefix) }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun notHaveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { !it.name.startsWith(prefix) }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder =
        notHaveNameStartingWith(prefixes.toList())

    infix fun haveName(predicate: (String) -> Boolean): ClassesRuleBuilder =
        haveName("custom name predicate", predicate)

    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setThat { predicate(it.name) }
        return builder
    }

    infix fun haveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): ClassesRuleBuilder = haveNameMatching(patterns.toList())

    infix fun notHaveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): ClassesRuleBuilder = notHaveNameMatching(patterns.toList())
}

/**
 * Trait interface for structural, hierarchy, member and type assignability filtering on classes.
 */
@Suppress("ComplexInterface")
interface ClassesThatStructureFilter : ClassesThatScope {
    infix fun areAssignableTo(superType: String): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { it.isAssignableTo(superType, allClasses) }
        return builder
    }

    infix fun areAssignableTo(superType: KClass<*>): ClassesRuleBuilder =
        areAssignableTo(superType.kontureQualifiedName())

    infix fun beChildOf(superType: String): ClassesRuleBuilder = areAssignableTo(superType)

    infix fun beChildOf(superType: KClass<*>): ClassesRuleBuilder = areAssignableTo(superType)

    infix fun areAssignableToAnyOf(superType: String): ClassesRuleBuilder = areAssignableToAnyOf(listOf(superType))

    infix fun areAssignableToAnyOf(superTypes: List<String>): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.any { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    fun areAssignableToAnyOf(vararg superTypes: String): ClassesRuleBuilder = areAssignableToAnyOf(superTypes.asList())

    fun areAssignableToAnyOf(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): ClassesRuleBuilder = areAssignableToAnyOf((arrayOf(first, *additional)).map { it.kontureQualifiedName() })

    infix fun areAssignableToAllOf(superType: String): ClassesRuleBuilder = areAssignableToAllOf(listOf(superType))

    infix fun areAssignableToAllOf(superTypes: List<String>): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    fun areAssignableToAllOf(vararg superTypes: String): ClassesRuleBuilder = areAssignableToAllOf(superTypes.asList())

    fun areAssignableToAllOf(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): ClassesRuleBuilder = areAssignableToAllOf((arrayOf(first, *additional)).map { it.kontureQualifiedName() })

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

    infix fun areAssignableFrom(subType: KClass<*>): ClassesRuleBuilder =
        areAssignableFrom(subType.kontureQualifiedName())

    fun haveCompanionObject(): ClassesRuleBuilder {
        builder.setThat { it.companionObject != null }
        return builder
    }

    fun haveNoArgConstructor(): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.primaryConstructor?.parameters?.isEmpty() == true ||
                cls.secondaryConstructors.any { it.parameters.isEmpty() }
        }
        return builder
    }

    fun havePrivatePrimaryConstructor(): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.primaryConstructor?.visibility == Visibility.PRIVATE
        }
        return builder
    }

    infix fun containProperty(propertyName: String): ClassesRuleBuilder {
        builder.setThat { cls -> cls.properties.any { it.name == propertyName } }
        return builder
    }

    infix fun containProperty(propertyNames: List<String>): ClassesRuleBuilder {
        builder.setThat { cls -> propertyNames.all { prop -> cls.properties.any { it.name == prop } } }
        return builder
    }

    fun containProperty(vararg propertyNames: String): ClassesRuleBuilder = containProperty(propertyNames.toList())

    infix fun containProperties(propertyNames: List<String>): ClassesRuleBuilder = containProperty(propertyNames)

    fun containProperties(vararg propertyNames: String): ClassesRuleBuilder = containProperty(propertyNames.toList())

    infix fun containFunction(functionName: String): ClassesRuleBuilder {
        builder.setThat { cls -> cls.functions.any { it.name == functionName } }
        return builder
    }

    infix fun containFunction(functionNames: List<String>): ClassesRuleBuilder {
        builder.setThat { cls -> functionNames.all { func -> cls.functions.any { it.name == func } } }
        return builder
    }

    fun containFunction(vararg functionNames: String): ClassesRuleBuilder = containFunction(functionNames.toList())

    infix fun containFunctions(functionNames: List<String>): ClassesRuleBuilder = containFunction(functionNames)

    fun containFunctions(vararg functionNames: String): ClassesRuleBuilder = containFunction(functionNames.toList())

    infix fun areAssignableTo(superTypes: List<String>): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    fun areAssignableTo(vararg superTypes: String): ClassesRuleBuilder = areAssignableTo(superTypes.toList())

    infix fun areAssignableFrom(subTypes: List<String>): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            subTypes.all { subType ->
                val subTypeDecl = allClasses.find { it.fqName == subType || it.name == subType }
                if (subTypeDecl != null) {
                    subTypeDecl.fqName == cls.fqName ||
                        subTypeDecl.isAssignableTo(cls.fqName, allClasses) ||
                        subTypeDecl.isAssignableTo(cls.name, allClasses)
                } else {
                    subType == cls.fqName || subType == cls.name
                }
            }
        }
        return builder
    }

    fun areAssignableFrom(vararg subTypes: String): ClassesRuleBuilder = areAssignableFrom(subTypes.toList())

    infix fun areNotAssignableTo(superType: String): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> !cls.isAssignableTo(superType, allClasses) }
        return builder
    }

    infix fun areNotAssignableTo(type: KClass<*>): ClassesRuleBuilder = areNotAssignableTo(type.kontureQualifiedName())

    infix fun areNotAssignableFrom(subType: String): ClassesRuleBuilder {
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            val subTypeDecl = allClasses.find { it.fqName == subType || it.name == subType }
            if (subTypeDecl != null) {
                subTypeDecl.fqName != cls.fqName &&
                    !subTypeDecl.isAssignableTo(cls.fqName, allClasses) &&
                    !subTypeDecl.isAssignableTo(cls.name, allClasses)
            } else {
                subType != cls.fqName && subType != cls.name
            }
        }
        return builder
    }

    infix fun areNotAssignableFrom(type: KClass<*>): ClassesRuleBuilder =
        areNotAssignableFrom(type.kontureQualifiedName())
}

/**
 * Trait interface for metadata, annotations, modifiers and visibility filtering on classes.
 */
@Suppress("ComplexInterface")
interface ClassesThatMetadataFilter : ClassesThatScope {
    infix fun haveAnnotationOf(annotationFqName: String): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.annotations.any { it.fqName == annotationFqName || it.name == annotationFqName }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    infix fun areAnnotatedWith(annotationFqName: String): ClassesRuleBuilder = haveAnnotationOf(annotationFqName)

    infix fun areAnnotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder = haveAnnotationOf(annotation)

    infix fun haveAllAnnotationsOf(name: String): ClassesRuleBuilder = haveAllAnnotationsOf(listOf(name))

    infix fun haveAllAnnotationsOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    fun haveAllAnnotationsOf(vararg names: String): ClassesRuleBuilder = haveAllAnnotationsOf(names.asList())

    infix fun haveAnyAnnotationOf(name: String): ClassesRuleBuilder = haveAnyAnnotationOf(listOf(name))

    infix fun haveAnyAnnotationOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    fun haveAnyAnnotationOf(vararg names: String): ClassesRuleBuilder = haveAnyAnnotationOf(names.asList())

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

    fun areInterfaces(): ClassesRuleBuilder {
        builder.setThat { it.isInterface }
        return builder
    }

    fun areEnums(): ClassesRuleBuilder {
        builder.setThat { it.isEnum }
        return builder
    }

    fun areAbstract(): ClassesRuleBuilder {
        builder.setThat { it.isAbstract || it.isInterface }
        return builder
    }

    infix fun haveVisibility(visibility: Visibility): ClassesRuleBuilder {
        builder.setThat { it.visibility == visibility }
        return builder
    }

    infix fun haveAnyVisibility(visibility: Visibility): ClassesRuleBuilder = haveAnyVisibility(listOf(visibility))

    infix fun haveAnyVisibility(visibilities: List<Visibility>): ClassesRuleBuilder {
        builder.setThat { cls -> visibilities.contains(cls.visibility) }
        return builder
    }

    fun haveAnyVisibility(vararg visibilities: Visibility): ClassesRuleBuilder =
        haveAnyVisibility(visibilities.asList())

    fun bePublic(): ClassesRuleBuilder = haveVisibility(Visibility.PUBLIC)

    fun beInternal(): ClassesRuleBuilder = haveVisibility(Visibility.INTERNAL)

    fun bePrivate(): ClassesRuleBuilder = haveVisibility(Visibility.PRIVATE)

    fun beProtected(): ClassesRuleBuilder = haveVisibility(Visibility.PROTECTED)

    infix fun haveModifier(modifier: Modifier): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(modifier) }
        return builder
    }

    infix fun haveAnyModifier(modifier: Modifier): ClassesRuleBuilder = haveAnyModifier(listOf(modifier))

    infix fun haveAnyModifier(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.any { cls.modifiers.contains(it) } }
        return builder
    }

    fun haveAnyModifier(vararg modifiers: Modifier): ClassesRuleBuilder = haveAnyModifier(modifiers.asList())

    infix fun haveAllModifiers(modifier: Modifier): ClassesRuleBuilder = haveAllModifiers(listOf(modifier))

    infix fun haveAllModifiers(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.all { cls.modifiers.contains(it) } }
        return builder
    }

    fun haveAllModifiers(vararg modifiers: Modifier): ClassesRuleBuilder = haveAllModifiers(modifiers.asList())

    fun beSealed(): ClassesRuleBuilder = haveModifier(Modifier.SEALED)

    fun beData(): ClassesRuleBuilder = haveModifier(Modifier.DATA)

    fun beInline(): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(Modifier.INLINE) || it.modifiers.contains(Modifier.VALUE) }
        return builder
    }

    fun areOpen(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    fun areOverride(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

    fun areInner(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.INNER) }
        return builder
    }

    fun areTopLevel(): ClassesRuleBuilder {
        builder.setThat { cls ->
            !cls.fqName.substringBeforeLast('.').contains('.') || cls.packageName == cls.fqName.substringBeforeLast('.')
        }
        return builder
    }

    fun areNested(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.packageName != cls.fqName.substringBeforeLast('.') }
        return builder
    }

    fun beDocumentedWithKDoc(): ClassesRuleBuilder {
        builder.setThat { it.kdocText?.isNotBlank() == true }
        return builder
    }
}

/**
 * Trait interface for composite, logical and custom predicate filtering on classes.
 */
interface ClassesThatCompositeFilter : ClassesThatScope {
    fun not(): ClassesThat = builder.not()

    infix fun matching(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    fun satisfy(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

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
