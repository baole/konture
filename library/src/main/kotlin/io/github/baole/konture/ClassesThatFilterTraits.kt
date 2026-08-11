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
public interface ClassesThatScope {
    /** Filter or assertion criteria for builder. */
    val builder: ClassesRuleBuilder
}

/**
 * Trait interface for package and module residency filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatPackageFilter : ClassesThatScope {
    /** Specifies reside in a package criteria. */
    infix fun resideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Specifies reside in a package criteria. */
    infix fun resideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    fun resideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    /** Specifies reside in a package criteria. */
    infix fun resideInAPackage(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    /** Specifies reside in package of criteria. */
    infix fun resideInPackageOf(type: KClass<*>): ClassesRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    /** Specifies not reside in a package criteria. */
    infix fun notResideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Specifies not reside in a package criteria. */
    infix fun notResideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    fun notResideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder =
        notResideInAPackage(packagePatterns.toList())

    /** Specifies reside in a module criteria. */
    infix fun resideInAModule(modulePath: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { cls ->
            /** Filter or assertion criteria for module. */
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module?.path == normalized
        }
        return builder
    }

    /** Specifies reside in a module criteria. */
    infix fun resideInAModule(modulePaths: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized paths. */
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setThat { cls ->
            /** Filter or assertion criteria for module. */
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module != null && normalizedPaths.contains(module.path)
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a module. */
    fun resideInAModule(vararg modulePaths: String): ClassesRuleBuilder = resideInAModule(modulePaths.toList())

    /** Specifies reside in module criteria. */
    infix fun resideInModule(modulePath: String): ClassesRuleBuilder = resideInAModule(modulePath)

    /** Specifies reside in modules criteria. */
    infix fun resideInModules(modulePaths: List<String>): ClassesRuleBuilder = resideInAModule(modulePaths)

    /** Filter or assertion criteria for reside in modules. */
    fun resideInModules(vararg modulePaths: String): ClassesRuleBuilder = resideInAModule(modulePaths.toList())

    /** Specifies not reside in a module criteria. */
    infix fun notResideInAModule(modulePath: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { cls ->
            /** Filter or assertion criteria for module. */
            val module =
                builder.graph.getAllModules().find { mod ->
                    mod.files.any { f -> f.classes.any { c -> c.fqName == cls.fqName } || f.filePath == cls.filePath }
                }
            module == null || (module.path != normalized && !PatternMatchers.matchesModuleGlob(normalized, module.path))
        }
        return builder
    }

    /** Specifies not reside in a module criteria. */
    infix fun notResideInAModule(modulePaths: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for normalized. */
        val normalized =
            modulePaths.map {
                if (!it.startsWith(":") && !it.startsWith("**") && it.isNotEmpty()) ":$it" else it
            }
        builder.setThat { cls ->
            /** Filter or assertion criteria for module. */
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

    /** Filter or assertion criteria for not reside in a module. */
    fun notResideInAModule(vararg modulePaths: String): ClassesRuleBuilder = notResideInAModule(modulePaths.toList())

    /** Specifies not reside in module criteria. */
    infix fun notResideInModule(modulePath: String): ClassesRuleBuilder = notResideInAModule(modulePath)

    /** Specifies not reside in modules criteria. */
    infix fun notResideInModules(modulePaths: List<String>): ClassesRuleBuilder = notResideInAModule(modulePaths)

    /** Filter or assertion criteria for not reside in modules. */
    fun notResideInModules(vararg modulePaths: String): ClassesRuleBuilder = notResideInAModule(modulePaths.toList())
}

/**
 * Trait interface for name matching and naming pattern filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatNameFilter : ClassesThatScope {
    /** Specifies have name criteria. */
    infix fun haveName(name: String): ClassesRuleBuilder {
        builder.setThat { it.fqName == name || it.name == name }
        return builder
    }

    /** Specifies have simple name criteria. */
    infix fun haveSimpleName(name: String): ClassesRuleBuilder {
        builder.setThat { it.name == name }
        return builder
    }

    /** Specifies have name criteria. */
    infix fun haveName(names: List<String>): ClassesRuleBuilder {
        builder.setThat { names.contains(it.name) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    fun haveName(vararg names: String): ClassesRuleBuilder = haveName(names.toList())

    /** Specifies not have name criteria. */
    infix fun notHaveName(name: String): ClassesRuleBuilder {
        builder.setThat { it.name != name }
        return builder
    }

    /** Specifies not have name criteria. */
    infix fun notHaveName(names: List<String>): ClassesRuleBuilder {
        builder.setThat { !names.contains(it.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    fun notHaveName(vararg names: String): ClassesRuleBuilder = notHaveName(names.toList())

    /** Specifies not have name criteria. */
    infix fun notHaveName(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { !predicate(it.name) }
        return builder
    }

    /** Specifies have name ending with criteria. */
    infix fun haveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { it.name.endsWith(suffix) }
        return builder
    }

    /** Specifies have name ending with criteria. */
    infix fun haveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    fun haveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Specifies not have name ending with criteria. */
    infix fun notHaveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { !it.name.endsWith(suffix) }
        return builder
    }

    /** Specifies not have name ending with criteria. */
    infix fun notHaveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    fun notHaveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = notHaveNameEndingWith(suffixes.toList())

    /** Specifies have name starting with criteria. */
    infix fun haveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { it.name.startsWith(prefix) }
        return builder
    }

    /** Specifies have name starting with criteria. */
    infix fun haveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    fun haveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /** Specifies not have name starting with criteria. */
    infix fun notHaveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { !it.name.startsWith(prefix) }
        return builder
    }

    /** Specifies not have name starting with criteria. */
    infix fun notHaveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    fun notHaveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder =
        notHaveNameStartingWith(prefixes.toList())

    /** Specifies have name criteria. */
    infix fun haveName(predicate: (String) -> Boolean): ClassesRuleBuilder =
        haveName("custom name predicate", predicate)

    /** Filter or assertion criteria for have name. */
    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setThat { predicate(it.name) }
        return builder
    }

    /** Specifies have name matching criteria. */
    infix fun haveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    /** Specifies have name matching criteria. */
    infix fun haveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    fun haveNameMatching(vararg patterns: String): ClassesRuleBuilder = haveNameMatching(patterns.toList())

    /** Specifies not have name matching criteria. */
    infix fun notHaveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    /** Specifies not have name matching criteria. */
    infix fun notHaveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    fun notHaveNameMatching(vararg patterns: String): ClassesRuleBuilder = notHaveNameMatching(patterns.toList())
}

/**
 * Trait interface for structural, hierarchy, member and type assignability filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatStructureFilter : ClassesThatScope {
    /** Specifies are assignable to criteria. */
    infix fun areAssignableTo(superType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { it.isAssignableTo(superType, allClasses) }
        return builder
    }

    /** Specifies are assignable to criteria. */
    infix fun areAssignableTo(superType: KClass<*>): ClassesRuleBuilder =
        areAssignableTo(superType.kontureQualifiedName())

    /** Specifies be child of criteria. */
    infix fun beChildOf(superType: String): ClassesRuleBuilder = areAssignableTo(superType)

    /** Specifies be child of criteria. */
    infix fun beChildOf(superType: KClass<*>): ClassesRuleBuilder = areAssignableTo(superType)

    /** Specifies are assignable to any of criteria. */
    infix fun areAssignableToAnyOf(superType: String): ClassesRuleBuilder = areAssignableToAnyOf(listOf(superType))

    /** Specifies are assignable to any of criteria. */
    infix fun areAssignableToAnyOf(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.any { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to any of. */
    fun areAssignableToAnyOf(vararg superTypes: String): ClassesRuleBuilder = areAssignableToAnyOf(superTypes.asList())

    /** Filter or assertion criteria for are assignable to any of. */
    fun areAssignableToAnyOf(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): ClassesRuleBuilder = areAssignableToAnyOf((arrayOf(first, *additional)).map { it.kontureQualifiedName() })

    /** Specifies are assignable to all of criteria. */
    infix fun areAssignableToAllOf(superType: String): ClassesRuleBuilder = areAssignableToAllOf(listOf(superType))

    /** Specifies are assignable to all of criteria. */
    infix fun areAssignableToAllOf(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to all of. */
    fun areAssignableToAllOf(vararg superTypes: String): ClassesRuleBuilder = areAssignableToAllOf(superTypes.asList())

    /** Filter or assertion criteria for are assignable to all of. */
    fun areAssignableToAllOf(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): ClassesRuleBuilder = areAssignableToAllOf((arrayOf(first, *additional)).map { it.kontureQualifiedName() })

    /** Specifies are assignable from criteria. */
    infix fun areAssignableFrom(subType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            /** Filter or assertion criteria for sub type decl. */
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

    /** Specifies are assignable from criteria. */
    infix fun areAssignableFrom(subType: KClass<*>): ClassesRuleBuilder =
        areAssignableFrom(subType.kontureQualifiedName())

    /** Filter or assertion criteria for have companion object. */
    fun haveCompanionObject(): ClassesRuleBuilder {
        builder.setThat { it.companionObject != null }
        return builder
    }

    /** Filter or assertion criteria for have no arg constructor. */
    fun haveNoArgConstructor(): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.primaryConstructor?.parameters?.isEmpty() == true ||
                cls.secondaryConstructors.any { it.parameters.isEmpty() }
        }
        return builder
    }

    /** Filter or assertion criteria for have private primary constructor. */
    fun havePrivatePrimaryConstructor(): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.primaryConstructor?.visibility == Visibility.PRIVATE
        }
        return builder
    }

    /** Specifies contain property criteria. */
    infix fun containProperty(propertyName: String): ClassesRuleBuilder {
        builder.setThat { cls -> cls.properties.any { it.name == propertyName } }
        return builder
    }

    /** Specifies contain property criteria. */
    infix fun containProperty(propertyNames: List<String>): ClassesRuleBuilder {
        builder.setThat { cls -> propertyNames.all { prop -> cls.properties.any { it.name == prop } } }
        return builder
    }

    /** Filter or assertion criteria for contain property. */
    fun containProperty(vararg propertyNames: String): ClassesRuleBuilder = containProperty(propertyNames.toList())

    /** Specifies contain properties criteria. */
    infix fun containProperties(propertyNames: List<String>): ClassesRuleBuilder = containProperty(propertyNames)

    /** Filter or assertion criteria for contain properties. */
    fun containProperties(vararg propertyNames: String): ClassesRuleBuilder = containProperty(propertyNames.toList())

    /** Specifies contain function criteria. */
    infix fun containFunction(functionName: String): ClassesRuleBuilder {
        builder.setThat { cls -> cls.functions.any { it.name == functionName } }
        return builder
    }

    /** Specifies contain function criteria. */
    infix fun containFunction(functionNames: List<String>): ClassesRuleBuilder {
        builder.setThat { cls -> functionNames.all { func -> cls.functions.any { it.name == func } } }
        return builder
    }

    /** Filter or assertion criteria for contain function. */
    fun containFunction(vararg functionNames: String): ClassesRuleBuilder = containFunction(functionNames.toList())

    /** Specifies contain functions criteria. */
    infix fun containFunctions(functionNames: List<String>): ClassesRuleBuilder = containFunction(functionNames)

    /** Filter or assertion criteria for contain functions. */
    fun containFunctions(vararg functionNames: String): ClassesRuleBuilder = containFunction(functionNames.toList())

    /** Specifies are assignable to criteria. */
    infix fun areAssignableTo(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to. */
    fun areAssignableTo(vararg superTypes: String): ClassesRuleBuilder = areAssignableTo(superTypes.toList())

    /** Specifies are assignable from criteria. */
    infix fun areAssignableFrom(subTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            subTypes.all { subType ->
                /** Filter or assertion criteria for sub type decl. */
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

    /** Filter or assertion criteria for are assignable from. */
    fun areAssignableFrom(vararg subTypes: String): ClassesRuleBuilder = areAssignableFrom(subTypes.toList())

    /** Specifies are not assignable to criteria. */
    infix fun areNotAssignableTo(superType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> !cls.isAssignableTo(superType, allClasses) }
        return builder
    }

    /** Specifies are not assignable to criteria. */
    infix fun areNotAssignableTo(type: KClass<*>): ClassesRuleBuilder = areNotAssignableTo(type.kontureQualifiedName())

    /** Specifies are not assignable from criteria. */
    infix fun areNotAssignableFrom(subType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls ->
            /** Filter or assertion criteria for sub type decl. */
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

    /** Specifies are not assignable from criteria. */
    infix fun areNotAssignableFrom(type: KClass<*>): ClassesRuleBuilder =
        areNotAssignableFrom(type.kontureQualifiedName())
}

/**
 * Trait interface for metadata, annotations, modifiers and visibility filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatMetadataFilter : ClassesThatScope {
    /** Specifies have annotation of criteria. */
    infix fun haveAnnotationOf(annotationFqName: String): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.annotations.any { it.fqName == annotationFqName || it.name == annotationFqName }
        }
        return builder
    }

    /** Specifies have annotation of criteria. */
    infix fun haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    /** Specifies are annotated with criteria. */
    infix fun areAnnotatedWith(annotationFqName: String): ClassesRuleBuilder = haveAnnotationOf(annotationFqName)

    /** Specifies are annotated with criteria. */
    infix fun areAnnotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder = haveAnnotationOf(annotation)

    /** Specifies have all annotations of criteria. */
    infix fun haveAllAnnotationsOf(name: String): ClassesRuleBuilder = haveAllAnnotationsOf(listOf(name))

    /** Specifies have all annotations of criteria. */
    infix fun haveAllAnnotationsOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    /** Filter or assertion criteria for have all annotations of. */
    fun haveAllAnnotationsOf(vararg names: String): ClassesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /** Specifies have any annotation of criteria. */
    infix fun haveAnyAnnotationOf(name: String): ClassesRuleBuilder = haveAnyAnnotationOf(listOf(name))

    /** Specifies have any annotation of criteria. */
    infix fun haveAnyAnnotationOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    /** Filter or assertion criteria for have any annotation of. */
    fun haveAnyAnnotationOf(vararg names: String): ClassesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /** Filter or assertion criteria for have annotation with argument. */
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

    /** Filter or assertion criteria for are interfaces. */
    fun areInterfaces(): ClassesRuleBuilder {
        builder.setThat { it.isInterface }
        return builder
    }

    /** Filter or assertion criteria for are enums. */
    fun areEnums(): ClassesRuleBuilder {
        builder.setThat { it.isEnum }
        return builder
    }

    /** Filter or assertion criteria for are abstract. */
    fun areAbstract(): ClassesRuleBuilder {
        builder.setThat { it.isAbstract || it.isInterface }
        return builder
    }

    /** Specifies have visibility criteria. */
    infix fun haveVisibility(visibility: Visibility): ClassesRuleBuilder {
        builder.setThat { it.visibility == visibility }
        return builder
    }

    /** Specifies have any visibility criteria. */
    infix fun haveAnyVisibility(visibility: Visibility): ClassesRuleBuilder = haveAnyVisibility(listOf(visibility))

    /** Specifies have any visibility criteria. */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): ClassesRuleBuilder {
        builder.setThat { cls -> visibilities.contains(cls.visibility) }
        return builder
    }

    /** Filter or assertion criteria for have any visibility. */
    fun haveAnyVisibility(vararg visibilities: Visibility): ClassesRuleBuilder =
        haveAnyVisibility(visibilities.asList())

    /** Filter or assertion criteria for be public. */
    fun bePublic(): ClassesRuleBuilder = haveVisibility(Visibility.PUBLIC)

    /** Filter or assertion criteria for be internal. */
    fun beInternal(): ClassesRuleBuilder = haveVisibility(Visibility.INTERNAL)

    /** Filter or assertion criteria for be private. */
    fun bePrivate(): ClassesRuleBuilder = haveVisibility(Visibility.PRIVATE)

    /** Filter or assertion criteria for be protected. */
    fun beProtected(): ClassesRuleBuilder = haveVisibility(Visibility.PROTECTED)

    /** Specifies have modifier criteria. */
    infix fun haveModifier(modifier: Modifier): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(modifier) }
        return builder
    }

    /** Specifies have any modifier criteria. */
    infix fun haveAnyModifier(modifier: Modifier): ClassesRuleBuilder = haveAnyModifier(listOf(modifier))

    /** Specifies have any modifier criteria. */
    infix fun haveAnyModifier(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.any { cls.modifiers.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have any modifier. */
    fun haveAnyModifier(vararg modifiers: Modifier): ClassesRuleBuilder = haveAnyModifier(modifiers.asList())

    /** Specifies have all modifiers criteria. */
    infix fun haveAllModifiers(modifier: Modifier): ClassesRuleBuilder = haveAllModifiers(listOf(modifier))

    /** Specifies have all modifiers criteria. */
    infix fun haveAllModifiers(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.all { cls.modifiers.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have all modifiers. */
    fun haveAllModifiers(vararg modifiers: Modifier): ClassesRuleBuilder = haveAllModifiers(modifiers.asList())

    /** Filter or assertion criteria for be sealed. */
    fun beSealed(): ClassesRuleBuilder = haveModifier(Modifier.SEALED)

    /** Filter or assertion criteria for be data. */
    fun beData(): ClassesRuleBuilder = haveModifier(Modifier.DATA)

    /** Filter or assertion criteria for be inline. */
    fun beInline(): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(Modifier.INLINE) || it.modifiers.contains(Modifier.VALUE) }
        return builder
    }

    /** Filter or assertion criteria for are open. */
    fun areOpen(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    /** Filter or assertion criteria for are override. */
    fun areOverride(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

    /** Filter or assertion criteria for are inner. */
    fun areInner(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.INNER) }
        return builder
    }

    /** Filter or assertion criteria for are top level. */
    fun areTopLevel(): ClassesRuleBuilder {
        builder.setThat { cls ->
            !cls.fqName.substringBeforeLast('.').contains('.') || cls.packageName == cls.fqName.substringBeforeLast('.')
        }
        return builder
    }

    /** Filter or assertion criteria for are nested. */
    fun areNested(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.packageName != cls.fqName.substringBeforeLast('.') }
        return builder
    }

    /** Filter or assertion criteria for be documented with k doc. */
    fun beDocumentedWithKDoc(): ClassesRuleBuilder {
        builder.setThat { it.kdocText?.isNotBlank() == true }
        return builder
    }
}

/**
 * Trait interface for composite, logical and custom predicate filtering on classes.
 */
public interface ClassesThatCompositeFilter : ClassesThatScope {
    /** Filter or assertion criteria for not. */
    fun not(): ClassesThat = builder.not()

    /** Specifies matching criteria. */
    infix fun matching(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for satisfy. */
    fun satisfy(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for any of. */
    fun anyOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    fun allOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    fun noneOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }
}
