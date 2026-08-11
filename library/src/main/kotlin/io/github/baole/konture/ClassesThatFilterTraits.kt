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
    public val builder: ClassesRuleBuilder
}

/**
 * Trait interface for package and module residency filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatPackageFilter : ClassesThatScope {
    /** Specifies reside in a package criteria. */
    public infix fun resideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Specifies reside in a package criteria. */
    public infix fun resideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    public fun resideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    /** Specifies reside in a package criteria. */
    public infix fun resideInAPackage(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    /** Specifies reside in package of criteria. */
    public infix fun resideInPackageOf(type: KClass<*>): ClassesRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    /** Specifies not reside in a package criteria. */
    public infix fun notResideInAPackage(packagePattern: String): ClassesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Specifies not reside in a package criteria. */
    public infix fun notResideInAPackage(packagePatterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    public fun notResideInAPackage(vararg packagePatterns: String): ClassesRuleBuilder =
        notResideInAPackage(packagePatterns.toList())

    /** Specifies reside in a module criteria. */
    public infix fun resideInAModule(modulePath: String): ClassesRuleBuilder {
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
    public infix fun resideInAModule(modulePaths: List<String>): ClassesRuleBuilder {
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
    public fun resideInAModule(vararg modulePaths: String): ClassesRuleBuilder = resideInAModule(modulePaths.toList())

    /** Specifies reside in module criteria. */
    public infix fun resideInModule(modulePath: String): ClassesRuleBuilder = resideInAModule(modulePath)

    /** Specifies reside in modules criteria. */
    public infix fun resideInModules(modulePaths: List<String>): ClassesRuleBuilder = resideInAModule(modulePaths)

    /** Filter or assertion criteria for reside in modules. */
    public fun resideInModules(vararg modulePaths: String): ClassesRuleBuilder = resideInAModule(modulePaths.toList())

    /** Specifies not reside in a module criteria. */
    public infix fun notResideInAModule(modulePath: String): ClassesRuleBuilder {
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
    public infix fun notResideInAModule(modulePaths: List<String>): ClassesRuleBuilder {
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
    public fun notResideInAModule(vararg modulePaths: String): ClassesRuleBuilder =
        notResideInAModule(modulePaths.toList())

    /** Specifies not reside in module criteria. */
    public infix fun notResideInModule(modulePath: String): ClassesRuleBuilder = notResideInAModule(modulePath)

    /** Specifies not reside in modules criteria. */
    public infix fun notResideInModules(modulePaths: List<String>): ClassesRuleBuilder = notResideInAModule(modulePaths)

    /** Filter or assertion criteria for not reside in modules. */
    public fun notResideInModules(vararg modulePaths: String): ClassesRuleBuilder =
        notResideInAModule(modulePaths.toList())
}

/**
 * Trait interface for name matching and naming pattern filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatNameFilter : ClassesThatScope {
    /** Specifies have name criteria. */
    public infix fun haveName(name: String): ClassesRuleBuilder {
        builder.setThat { it.fqName == name || it.name == name }
        return builder
    }

    /** Specifies have simple name criteria. */
    public infix fun haveSimpleName(name: String): ClassesRuleBuilder {
        builder.setThat { it.name == name }
        return builder
    }

    /** Specifies have name criteria. */
    public infix fun haveName(names: List<String>): ClassesRuleBuilder {
        builder.setThat { names.contains(it.name) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public fun haveName(vararg names: String): ClassesRuleBuilder = haveName(names.toList())

    /** Specifies not have name criteria. */
    public infix fun notHaveName(name: String): ClassesRuleBuilder {
        builder.setThat { it.name != name }
        return builder
    }

    /** Specifies not have name criteria. */
    public infix fun notHaveName(names: List<String>): ClassesRuleBuilder {
        builder.setThat { !names.contains(it.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public fun notHaveName(vararg names: String): ClassesRuleBuilder = notHaveName(names.toList())

    /** Specifies not have name criteria. */
    public infix fun notHaveName(predicate: (String) -> Boolean): ClassesRuleBuilder {
        builder.setThat { !predicate(it.name) }
        return builder
    }

    /** Specifies have name ending with criteria. */
    public infix fun haveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { it.name.endsWith(suffix) }
        return builder
    }

    /** Specifies have name ending with criteria. */
    public infix fun haveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Specifies not have name ending with criteria. */
    public infix fun notHaveNameEndingWith(suffix: String): ClassesRuleBuilder {
        builder.setThat { !it.name.endsWith(suffix) }
        return builder
    }

    /** Specifies not have name ending with criteria. */
    public infix fun notHaveNameEndingWith(suffixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public fun notHaveNameEndingWith(vararg suffixes: String): ClassesRuleBuilder =
        notHaveNameEndingWith(suffixes.toList())

    /** Specifies have name starting with criteria. */
    public infix fun haveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { it.name.startsWith(prefix) }
        return builder
    }

    /** Specifies have name starting with criteria. */
    public infix fun haveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder =
        haveNameStartingWith(prefixes.toList())

    /** Specifies not have name starting with criteria. */
    public infix fun notHaveNameStartingWith(prefix: String): ClassesRuleBuilder {
        builder.setThat { !it.name.startsWith(prefix) }
        return builder
    }

    /** Specifies not have name starting with criteria. */
    public infix fun notHaveNameStartingWith(prefixes: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public fun notHaveNameStartingWith(vararg prefixes: String): ClassesRuleBuilder =
        notHaveNameStartingWith(prefixes.toList())

    /** Specifies have name criteria. */
    public infix fun haveName(predicate: (String) -> Boolean): ClassesRuleBuilder =
        haveName("custom name predicate", predicate)

    /** Filter or assertion criteria for have name. */
    @Suppress("UnusedParameter")
    public fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setThat { predicate(it.name) }
        return builder
    }

    /** Specifies have name matching criteria. */
    public infix fun haveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    /** Specifies have name matching criteria. */
    public infix fun haveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(vararg patterns: String): ClassesRuleBuilder = haveNameMatching(patterns.toList())

    /** Specifies not have name matching criteria. */
    public infix fun notHaveNameMatching(pattern: String): ClassesRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.name) }
        return builder
    }

    /** Specifies not have name matching criteria. */
    public infix fun notHaveNameMatching(patterns: List<String>): ClassesRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public fun notHaveNameMatching(vararg patterns: String): ClassesRuleBuilder = notHaveNameMatching(patterns.toList())
}

/**
 * Trait interface for structural, hierarchy, member and type assignability filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatStructureFilter : ClassesThatScope {
    /** Specifies are assignable to criteria. */
    public infix fun areAssignableTo(superType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { it.isAssignableTo(superType, allClasses) }
        return builder
    }

    /** Specifies are assignable to criteria. */
    public infix fun areAssignableTo(superType: KClass<*>): ClassesRuleBuilder =
        areAssignableTo(superType.kontureQualifiedName())

    /** Specifies be child of criteria. */
    public infix fun beChildOf(superType: String): ClassesRuleBuilder = areAssignableTo(superType)

    /** Specifies be child of criteria. */
    public infix fun beChildOf(superType: KClass<*>): ClassesRuleBuilder = areAssignableTo(superType)

    /** Specifies are assignable to any of criteria. */
    public infix fun areAssignableToAnyOf(superType: String): ClassesRuleBuilder =
        areAssignableToAnyOf(listOf(superType))

    /** Specifies are assignable to any of criteria. */
    public infix fun areAssignableToAnyOf(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.any { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to any of. */
    public fun areAssignableToAnyOf(vararg superTypes: String): ClassesRuleBuilder =
        areAssignableToAnyOf(superTypes.asList())

    /** Filter or assertion criteria for are assignable to any of. */
    public fun areAssignableToAnyOf(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): ClassesRuleBuilder = areAssignableToAnyOf((arrayOf(first, *additional)).map { it.kontureQualifiedName() })

    /** Specifies are assignable to all of criteria. */
    public infix fun areAssignableToAllOf(superType: String): ClassesRuleBuilder =
        areAssignableToAllOf(listOf(superType))

    /** Specifies are assignable to all of criteria. */
    public infix fun areAssignableToAllOf(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to all of. */
    public fun areAssignableToAllOf(vararg superTypes: String): ClassesRuleBuilder =
        areAssignableToAllOf(superTypes.asList())

    /** Filter or assertion criteria for are assignable to all of. */
    public fun areAssignableToAllOf(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): ClassesRuleBuilder = areAssignableToAllOf((arrayOf(first, *additional)).map { it.kontureQualifiedName() })

    /** Specifies are assignable from criteria. */
    public infix fun areAssignableFrom(subType: String): ClassesRuleBuilder {
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
    public infix fun areAssignableFrom(subType: KClass<*>): ClassesRuleBuilder =
        areAssignableFrom(subType.kontureQualifiedName())

    /** Filter or assertion criteria for have companion object. */
    public fun haveCompanionObject(): ClassesRuleBuilder {
        builder.setThat { it.companionObject != null }
        return builder
    }

    /** Filter or assertion criteria for have no arg constructor. */
    public fun haveNoArgConstructor(): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.primaryConstructor?.parameters?.isEmpty() == true ||
                cls.secondaryConstructors.any { it.parameters.isEmpty() }
        }
        return builder
    }

    /** Filter or assertion criteria for have private primary constructor. */
    public fun havePrivatePrimaryConstructor(): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.primaryConstructor?.visibility == Visibility.PRIVATE
        }
        return builder
    }

    /** Specifies contain property criteria. */
    public infix fun containProperty(propertyName: String): ClassesRuleBuilder {
        builder.setThat { cls -> cls.properties.any { it.name == propertyName } }
        return builder
    }

    /** Specifies contain property criteria. */
    public infix fun containProperty(propertyNames: List<String>): ClassesRuleBuilder {
        builder.setThat { cls -> propertyNames.all { prop -> cls.properties.any { it.name == prop } } }
        return builder
    }

    /** Filter or assertion criteria for contain property. */
    public fun containProperty(vararg propertyNames: String): ClassesRuleBuilder =
        containProperty(propertyNames.toList())

    /** Specifies contain properties criteria. */
    public infix fun containProperties(propertyNames: List<String>): ClassesRuleBuilder = containProperty(propertyNames)

    /** Filter or assertion criteria for contain properties. */
    public fun containProperties(vararg propertyNames: String): ClassesRuleBuilder =
        containProperty(propertyNames.toList())

    /** Specifies contain function criteria. */
    public infix fun containFunction(functionName: String): ClassesRuleBuilder {
        builder.setThat { cls -> cls.functions.any { it.name == functionName } }
        return builder
    }

    /** Specifies contain function criteria. */
    public infix fun containFunction(functionNames: List<String>): ClassesRuleBuilder {
        builder.setThat { cls -> functionNames.all { func -> cls.functions.any { it.name == func } } }
        return builder
    }

    /** Filter or assertion criteria for contain function. */
    public fun containFunction(vararg functionNames: String): ClassesRuleBuilder =
        containFunction(functionNames.toList())

    /** Specifies contain functions criteria. */
    public infix fun containFunctions(functionNames: List<String>): ClassesRuleBuilder = containFunction(functionNames)

    /** Filter or assertion criteria for contain functions. */
    public fun containFunctions(vararg functionNames: String): ClassesRuleBuilder =
        containFunction(functionNames.toList())

    /** Specifies are assignable to criteria. */
    public infix fun areAssignableTo(superTypes: List<String>): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> superTypes.all { cls.isAssignableTo(it, allClasses) } }
        return builder
    }

    /** Filter or assertion criteria for are assignable to. */
    public fun areAssignableTo(vararg superTypes: String): ClassesRuleBuilder = areAssignableTo(superTypes.toList())

    /** Specifies are assignable from criteria. */
    public infix fun areAssignableFrom(subTypes: List<String>): ClassesRuleBuilder {
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
    public fun areAssignableFrom(vararg subTypes: String): ClassesRuleBuilder = areAssignableFrom(subTypes.toList())

    /** Specifies are not assignable to criteria. */
    public infix fun areNotAssignableTo(superType: String): ClassesRuleBuilder {
        /** Filter or assertion criteria for all classes. */
        val allClasses = builder.graph.getAllModules().flatMap { it.classes }
        builder.setThat { cls -> !cls.isAssignableTo(superType, allClasses) }
        return builder
    }

    /** Specifies are not assignable to criteria. */
    public infix fun areNotAssignableTo(type: KClass<*>): ClassesRuleBuilder =
        areNotAssignableTo(type.kontureQualifiedName())

    /** Specifies are not assignable from criteria. */
    public infix fun areNotAssignableFrom(subType: String): ClassesRuleBuilder {
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
    public infix fun areNotAssignableFrom(type: KClass<*>): ClassesRuleBuilder =
        areNotAssignableFrom(type.kontureQualifiedName())
}

/**
 * Trait interface for metadata, annotations, modifiers and visibility filtering on classes.
 */
@Suppress("ComplexInterface")
public interface ClassesThatMetadataFilter : ClassesThatScope {
    /** Specifies have annotation of criteria. */
    public infix fun haveAnnotationOf(annotationFqName: String): ClassesRuleBuilder {
        builder.setThat { cls ->
            cls.annotations.any { it.fqName == annotationFqName || it.name == annotationFqName }
        }
        return builder
    }

    /** Specifies have annotation of criteria. */
    public infix fun haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    /** Specifies are annotated with criteria. */
    public infix fun areAnnotatedWith(annotationFqName: String): ClassesRuleBuilder = haveAnnotationOf(annotationFqName)

    /** Specifies are annotated with criteria. */
    public infix fun areAnnotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder =
        haveAnnotationOf(annotation)

    /** Specifies have all annotations of criteria. */
    public infix fun haveAllAnnotationsOf(name: String): ClassesRuleBuilder = haveAllAnnotationsOf(listOf(name))

    /** Specifies have all annotations of criteria. */
    public infix fun haveAllAnnotationsOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    /** Filter or assertion criteria for have all annotations of. */
    public fun haveAllAnnotationsOf(vararg names: String): ClassesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /** Specifies have any annotation of criteria. */
    public infix fun haveAnyAnnotationOf(name: String): ClassesRuleBuilder = haveAnyAnnotationOf(listOf(name))

    /** Specifies have any annotation of criteria. */
    public infix fun haveAnyAnnotationOf(names: List<String>): ClassesRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    /** Filter or assertion criteria for have any annotation of. */
    public fun haveAnyAnnotationOf(vararg names: String): ClassesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /** Filter or assertion criteria for have annotation with argument. */
    public fun haveAnnotationWithArgument(
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
    public fun areInterfaces(): ClassesRuleBuilder {
        builder.setThat { it.isInterface }
        return builder
    }

    /** Filter or assertion criteria for are enums. */
    public fun areEnums(): ClassesRuleBuilder {
        builder.setThat { it.isEnum }
        return builder
    }

    /** Filter or assertion criteria for are abstract. */
    public fun areAbstract(): ClassesRuleBuilder {
        builder.setThat { it.isAbstract || it.isInterface }
        return builder
    }

    /** Specifies have visibility criteria. */
    public infix fun haveVisibility(visibility: Visibility): ClassesRuleBuilder {
        builder.setThat { it.visibility == visibility }
        return builder
    }

    /** Specifies have any visibility criteria. */
    public infix fun haveAnyVisibility(visibility: Visibility): ClassesRuleBuilder =
        haveAnyVisibility(listOf(visibility))

    /** Specifies have any visibility criteria. */
    public infix fun haveAnyVisibility(visibilities: List<Visibility>): ClassesRuleBuilder {
        builder.setThat { cls -> visibilities.contains(cls.visibility) }
        return builder
    }

    /** Filter or assertion criteria for have any visibility. */
    public fun haveAnyVisibility(vararg visibilities: Visibility): ClassesRuleBuilder =
        haveAnyVisibility(visibilities.asList())

    /** Filter or assertion criteria for be public. */
    public fun bePublic(): ClassesRuleBuilder = haveVisibility(Visibility.PUBLIC)

    /** Filter or assertion criteria for be internal. */
    public fun beInternal(): ClassesRuleBuilder = haveVisibility(Visibility.INTERNAL)

    /** Filter or assertion criteria for be private. */
    public fun bePrivate(): ClassesRuleBuilder = haveVisibility(Visibility.PRIVATE)

    /** Filter or assertion criteria for be protected. */
    public fun beProtected(): ClassesRuleBuilder = haveVisibility(Visibility.PROTECTED)

    /** Specifies have modifier criteria. */
    public infix fun haveModifier(modifier: Modifier): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(modifier) }
        return builder
    }

    /** Specifies have any modifier criteria. */
    public infix fun haveAnyModifier(modifier: Modifier): ClassesRuleBuilder = haveAnyModifier(listOf(modifier))

    /** Specifies have any modifier criteria. */
    public infix fun haveAnyModifier(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.any { cls.modifiers.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have any modifier. */
    public fun haveAnyModifier(vararg modifiers: Modifier): ClassesRuleBuilder = haveAnyModifier(modifiers.asList())

    /** Specifies have all modifiers criteria. */
    public infix fun haveAllModifiers(modifier: Modifier): ClassesRuleBuilder = haveAllModifiers(listOf(modifier))

    /** Specifies have all modifiers criteria. */
    public infix fun haveAllModifiers(modifiers: List<Modifier>): ClassesRuleBuilder {
        builder.setThat { cls -> modifiers.all { cls.modifiers.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have all modifiers. */
    public fun haveAllModifiers(vararg modifiers: Modifier): ClassesRuleBuilder = haveAllModifiers(modifiers.asList())

    /** Filter or assertion criteria for be sealed. */
    public fun beSealed(): ClassesRuleBuilder = haveModifier(Modifier.SEALED)

    /** Filter or assertion criteria for be data. */
    public fun beData(): ClassesRuleBuilder = haveModifier(Modifier.DATA)

    /** Filter or assertion criteria for be inline. */
    public fun beInline(): ClassesRuleBuilder {
        builder.setThat { it.modifiers.contains(Modifier.INLINE) || it.modifiers.contains(Modifier.VALUE) }
        return builder
    }

    /** Filter or assertion criteria for are open. */
    public fun areOpen(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    /** Filter or assertion criteria for are override. */
    public fun areOverride(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

    /** Filter or assertion criteria for are inner. */
    public fun areInner(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.modifiers.contains(Modifier.INNER) }
        return builder
    }

    /** Filter or assertion criteria for are top level. */
    public fun areTopLevel(): ClassesRuleBuilder {
        builder.setThat { cls ->
            !cls.fqName.substringBeforeLast('.').contains('.') || cls.packageName == cls.fqName.substringBeforeLast('.')
        }
        return builder
    }

    /** Filter or assertion criteria for are nested. */
    public fun areNested(): ClassesRuleBuilder {
        builder.setThat { cls -> cls.packageName != cls.fqName.substringBeforeLast('.') }
        return builder
    }

    /** Filter or assertion criteria for be documented with k doc. */
    public fun beDocumentedWithKDoc(): ClassesRuleBuilder {
        builder.setThat { it.kdocText?.isNotBlank() == true }
        return builder
    }
}

/**
 * Trait interface for composite, logical and custom predicate filtering on classes.
 */
public interface ClassesThatCompositeFilter : ClassesThatScope {
    /** Filter or assertion criteria for not. */
    public fun not(): ClassesThat = builder.not()

    /** Specifies matching criteria. */
    public infix fun matching(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for satisfy. */
    public fun satisfy(predicate: (ClassDeclaration) -> Boolean): ClassesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for any of. */
    public fun anyOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
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
    public fun allOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
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
    public fun noneOf(vararg blocks: ClassesThat.() -> Unit): ClassesRuleBuilder {
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
