/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

/**
 * Common scope interface providing access to [FunctionsRuleBuilder].
 */
public interface FunctionsThatScope {
    /** Filter or assertion criteria for builder. */
    public val builder: FunctionsRuleBuilder
}

/**
 * Trait interface for package and module residency filtering on functions.
 */
@Suppress("ComplexInterface")
public interface FunctionsThatPackageFilter : FunctionsThatScope {
    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    /** Filter or assertion criteria for reside in a package. */
    public fun resideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    /** Filter or assertion criteria for reside in a package. */
    public infix fun resideInAPackage(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    /** Filter or assertion criteria for reside in package of. */
    public infix fun resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    public infix fun notResideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    /** Filter or assertion criteria for not reside in a package. */
    public fun notResideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        notResideInAPackage(packagePatterns.toList())

    /** Filter or assertion criteria for reside in a module. */
    public infix fun resideInAModule(modulePath: String): FunctionsRuleBuilder {
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
    public infix fun resideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
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
    public fun resideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for reside in module. */
    public infix fun resideInModule(modulePath: String): FunctionsRuleBuilder = resideInAModule(modulePath)

    /** Filter or assertion criteria for reside in modules. */
    public infix fun resideInModules(modulePaths: List<String>): FunctionsRuleBuilder = resideInAModule(modulePaths)

    /** Filter or assertion criteria for reside in modules. */
    public fun resideInModules(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in a module. */
    public infix fun notResideInAModule(modulePath: String): FunctionsRuleBuilder {
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
    public infix fun notResideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
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
    public fun notResideInAModule(vararg modulePaths: String): FunctionsRuleBuilder =
        notResideInAModule(modulePaths.toList())

    /** Filter or assertion criteria for not reside in module. */
    public infix fun notResideInModule(modulePath: String): FunctionsRuleBuilder = notResideInAModule(modulePath)

    /** Filter or assertion criteria for not reside in modules. */
    public infix fun notResideInModules(modulePaths: List<String>): FunctionsRuleBuilder =
        notResideInAModule(modulePaths)

    /** Filter or assertion criteria for not reside in modules. */
    public fun notResideInModules(vararg modulePaths: String): FunctionsRuleBuilder =
        notResideInAModule(modulePaths.toList())
}

/**
 * Trait interface for name matching and naming pattern filtering on functions.
 */
@Suppress("ComplexInterface")
public interface FunctionsThatNameFilter : FunctionsThatScope {
    /** Filter or assertion criteria for have name. */
    public infix fun haveName(name: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name == name }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { names.contains(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public fun haveName(vararg names: String): FunctionsRuleBuilder = haveName(names.toList())

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(name: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name != name }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { !names.contains(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name. */
    public fun notHaveName(vararg names: String): FunctionsRuleBuilder = notHaveName(names.toList())

    /** Filter or assertion criteria for not have name. */
    public infix fun notHaveName(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { !predicate(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name. */
    public infix fun haveName(predicate: (String) -> Boolean): FunctionsRuleBuilder =
        haveName("custom name predicate", predicate)

    /** Filter or assertion criteria for have name. */
    @Suppress("UnusedParameter")
    public fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): FunctionsRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public infix fun haveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = haveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setThat { !it.declaration.name.endsWith(suffix) }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public infix fun notHaveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name ending with. */
    public fun notHaveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder =
        notHaveNameEndingWith(suffixes.toList())

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public infix fun haveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder =
        haveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setThat { !it.declaration.name.startsWith(prefix) }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public infix fun notHaveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name starting with. */
    public fun notHaveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder =
        notHaveNameStartingWith(prefixes.toList())

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public infix fun haveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(vararg patterns: String): FunctionsRuleBuilder = haveNameMatching(patterns.toList())

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public infix fun notHaveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /** Filter or assertion criteria for not have name matching. */
    public fun notHaveNameMatching(vararg patterns: String): FunctionsRuleBuilder =
        notHaveNameMatching(patterns.toList())
}

/**
 * Trait interface for structural, parameter, return type and declaration location filtering on functions.
 */
@Suppress("ComplexInterface")
public interface FunctionsThatStructureFilter : FunctionsThatScope {
    /** Filter or assertion criteria for are extension. */
    public fun areExtension(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.isExtension }
        return builder
    }

    /** Filter or assertion criteria for have extension receiver. */
    public infix fun haveExtensionReceiver(receiverTypeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.receiverType?.let { receiver ->
                receiver == receiverTypeFqName || receiver.endsWith(".$receiverTypeFqName") || receiverTypeFqName.endsWith(".$receiver")
            } ?: false
        }
        return builder
    }

    /** Filter or assertion criteria for have extension receiver. */
    public infix fun haveExtensionReceiver(kClass: KClass<*>): FunctionsRuleBuilder =
        haveExtensionReceiver(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for are top level. */
    public fun areTopLevel(): FunctionsRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    /** Filter or assertion criteria for are member. */
    public fun areMember(): FunctionsRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    /** Filter or assertion criteria for be top level. */
    public fun beTopLevel(): FunctionsRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    /** Filter or assertion criteria for be member. */
    public fun beMember(): FunctionsRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    /** Filter or assertion criteria for have parameter of. */
    public infix fun haveParameterOf(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.any { p ->
                p.type == typeFqName || p.type.endsWith(".$typeFqName") || typeFqName.endsWith(".${p.type}")
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have parameter of. */
    public infix fun haveParameterOf(kClass: KClass<*>): FunctionsRuleBuilder =
        haveParameterOf(kClass.kontureQualifiedName())

    /** Filter or assertion criteria for have parameter of. */
    public infix fun haveParameterOf(types: List<String>): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.any { p ->
                types.any { typeFqName ->
                    p.type == typeFqName || p.type.endsWith(".$typeFqName") || typeFqName.endsWith(".${p.type}")
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have parameter of. */
    public fun haveParameterOf(vararg types: String): FunctionsRuleBuilder = haveParameterOf(types.toList())

    /** Filter or assertion criteria for not have parameter of. */
    public infix fun notHaveParameterOf(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.none { p ->
                p.type == typeFqName || p.type.endsWith(".$typeFqName") || typeFqName.endsWith(".${p.type}")
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have parameter of. */
    public infix fun notHaveParameterOf(type: KClass<*>): FunctionsRuleBuilder =
        notHaveParameterOf(type.kontureQualifiedName())

    /** Filter or assertion criteria for have return type. */
    public infix fun haveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.returnType == typeFqName }
        return builder
    }

    /** Filter or assertion criteria for have return type. */
    public infix fun haveReturnType(type: KClass<*>): FunctionsRuleBuilder {
        /** Filter or assertion criteria for expected type. */
        val expectedType = type.toKontureTypeReference()
        builder.setThat { function ->
            function.declaration.resolvedReturnType?.let { matchesKotlinType(it, expectedType) } == true
        }
        return builder
    }

    /** Filter or assertion criteria for have return type. */
    public infix fun haveReturnType(typeFqNames: List<String>): FunctionsRuleBuilder {
        builder.setThat { func -> typeFqNames.contains(func.declaration.returnType) }
        return builder
    }

    /** Filter or assertion criteria for have return type. */
    public fun haveReturnType(vararg typeFqNames: String): FunctionsRuleBuilder = haveReturnType(typeFqNames.asList())

    /** Filter or assertion criteria for not have return type. */
    public infix fun notHaveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.returnType != typeFqName && !func.declaration.returnType.endsWith(".$typeFqName")
        }
        return builder
    }

    /** Filter or assertion criteria for not have return type. */
    public infix fun notHaveReturnType(type: KClass<*>): FunctionsRuleBuilder =
        notHaveReturnType(type.kontureQualifiedName())

    /** Filter or assertion criteria for have parameter types. */
    public infix fun haveParameterTypes(types: List<String>): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.size == types.size &&
                func.declaration.parameters.zip(types).all { (param, expectedType) ->
                    param.type == expectedType || param.type.endsWith(".$expectedType")
                }
        }
        return builder
    }

    /** Filter or assertion criteria for have parameter types. */
    public fun haveParameterTypes(vararg types: String): FunctionsRuleBuilder = haveParameterTypes(types.asList())

    /** Filter or assertion criteria for have parameter types. */
    public fun haveParameterTypes(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): FunctionsRuleBuilder {
        /** Filter or assertion criteria for types. */
        val types = arrayOf(first, *additional).map { it.toKontureTypeReference() }
        builder.setThat { function ->
            function.declaration.parameters.size == types.size &&
                function.declaration.parameters.zip(types).all { (parameter, type) ->
                    parameter.resolvedType?.let { matchesKotlinType(it, type) } == true
                }
        }
        return builder
    }

    /** Filter or assertion criteria for have any parameter type. */
    public infix fun haveAnyParameterType(types: List<String>): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.any { param ->
                types.any { expectedType ->
                    param.type == expectedType || param.type.endsWith(".$expectedType")
                }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have any parameter type. */
    public fun haveAnyParameterType(vararg types: String): FunctionsRuleBuilder = haveAnyParameterType(types.asList())

    /** Filter or assertion criteria for have any parameter type. */
    public fun haveAnyParameterType(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): FunctionsRuleBuilder {
        /** Filter or assertion criteria for types. */
        val types = arrayOf(first, *additional).map { it.toKontureTypeReference() }
        builder.setThat { function ->
            function.declaration.parameters.any { parameter ->
                parameter.resolvedType?.let { resolvedType ->
                    types.any { matchesKotlinType(resolvedType, it) }
                } == true
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have no parameters. */
    public fun haveNoParameters(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.parameters.isEmpty() }
        return builder
    }

    /** Filter or assertion criteria for have parameter count. */
    public infix fun haveParameterCount(count: Int): FunctionsRuleBuilder {
        builder.setThat { it.declaration.parameters.size == count }
        return builder
    }

    /** Filter or assertion criteria for have parameter count. */
    public infix fun haveParameterCount(predicate: (Int) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { predicate(it.declaration.parameters.size) }
        return builder
    }

    /** Filter or assertion criteria for belong to class. */
    public infix fun belongToClass(className: String): FunctionsRuleBuilder {
        builder.setThat { it.className == className || (it.className != null && it.qualifiedName.contains(className)) }
        return builder
    }

    /** Filter or assertion criteria for belong to class. */
    public infix fun belongToClass(type: KClass<*>): FunctionsRuleBuilder = belongToClass(type.kontureQualifiedName())
}

/**
 * Trait interface for modifiers, annotations, visibility and modifier filtering on functions.
 */
@Suppress("ComplexInterface")
public interface FunctionsThatModifierFilter : FunctionsThatScope {
    /** Filter or assertion criteria for are public. */
    public fun arePublic(): FunctionsRuleBuilder = haveVisibility(Visibility.PUBLIC)

    /** Filter or assertion criteria for be public. */
    public fun bePublic(): FunctionsRuleBuilder = arePublic()

    /** Filter or assertion criteria for are internal. */
    public fun areInternal(): FunctionsRuleBuilder = haveVisibility(Visibility.INTERNAL)

    /** Filter or assertion criteria for be internal. */
    public fun beInternal(): FunctionsRuleBuilder = areInternal()

    /** Filter or assertion criteria for are private. */
    public fun arePrivate(): FunctionsRuleBuilder = haveVisibility(Visibility.PRIVATE)

    /** Filter or assertion criteria for be private. */
    public fun bePrivate(): FunctionsRuleBuilder = arePrivate()

    /** Filter or assertion criteria for are protected. */
    public fun areProtected(): FunctionsRuleBuilder = haveVisibility(Visibility.PROTECTED)

    /** Filter or assertion criteria for be protected. */
    public fun beProtected(): FunctionsRuleBuilder = areProtected()

    /** Filter or assertion criteria for not be public. */
    public fun notBePublic(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PUBLIC }
        return builder
    }

    /** Filter or assertion criteria for not be internal. */
    public fun notBeInternal(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.INTERNAL }
        return builder
    }

    /** Filter or assertion criteria for not be private. */
    public fun notBePrivate(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PRIVATE }
        return builder
    }

    /** Filter or assertion criteria for not be protected. */
    public fun notBeProtected(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PROTECTED }
        return builder
    }

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setThat { it.hasAnnotation(annotationName) }
        return builder
    }

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for are annotated with. */
    public infix fun areAnnotatedWith(annotationName: String): FunctionsRuleBuilder = haveAnnotationOf(annotationName)

    /** Filter or assertion criteria for are annotated with. */
    public infix fun areAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        haveAnnotationOf(annotation)

    /** Filter or assertion criteria for have annotation of. */
    public infix fun haveAnnotationOf(annotationNames: List<String>): FunctionsRuleBuilder {
        builder.setThat { func -> annotationNames.any { func.hasAnnotation(it) } }
        return builder
    }

    /** Filter or assertion criteria for have annotation of. */
    public fun haveAnnotationOf(vararg annotationNames: String): FunctionsRuleBuilder =
        haveAnnotationOf(annotationNames.asList())

    /** Filter or assertion criteria for not have annotation of. */
    public infix fun notHaveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setThat { !it.hasAnnotation(annotationName) }
        return builder
    }

    /** Filter or assertion criteria for not have annotation of. */
    public infix fun notHaveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        notHaveAnnotationOf(annotation.kontureQualifiedName())

    /** Filter or assertion criteria for not be annotated with. */
    public infix fun notBeAnnotatedWith(annotationName: String): FunctionsRuleBuilder =
        notHaveAnnotationOf(annotationName)

    /** Filter or assertion criteria for not be annotated with. */
    public infix fun notBeAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        notHaveAnnotationOf(annotation)

    /** Filter or assertion criteria for have all annotations of. */
    public infix fun haveAllAnnotationsOf(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    /** Filter or assertion criteria for have all annotations of. */
    public fun haveAllAnnotationsOf(vararg names: String): FunctionsRuleBuilder = haveAllAnnotationsOf(names.asList())

    /** Filter or assertion criteria for have any annotation of. */
    public infix fun haveAnyAnnotationOf(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    /** Filter or assertion criteria for have any annotation of. */
    public fun haveAnyAnnotationOf(vararg names: String): FunctionsRuleBuilder = haveAnyAnnotationOf(names.asList())

    /** Filter or assertion criteria for are open. */
    public fun areOpen(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    /** Filter or assertion criteria for are abstract. */
    public fun areAbstract(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.ABSTRACT) }
        return builder
    }

    /** Filter or assertion criteria for are override. */
    public fun areOverride(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

    /** Filter or assertion criteria for have modifier. */
    public infix fun haveModifier(modifier: Modifier): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(modifier) }
        return builder
    }

    /** Filter or assertion criteria for have all modifiers. */
    public infix fun haveAllModifiers(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setThat { func -> modifiers.all { func.declaration.modifiers.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have all modifiers. */
    public fun haveAllModifiers(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAllModifiers(modifiers.asList())

    /** Filter or assertion criteria for have any modifier. */
    public infix fun haveAnyModifier(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setThat { func -> modifiers.any { func.declaration.modifiers.contains(it) } }
        return builder
    }

    /** Filter or assertion criteria for have any modifier. */
    public fun haveAnyModifier(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAnyModifier(modifiers.asList())

    /** Filter or assertion criteria for have visibility. */
    public infix fun haveVisibility(visibility: Visibility): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility == visibility }
        return builder
    }

    /** Filter or assertion criteria for have any visibility. */
    public infix fun haveAnyVisibility(visibilities: List<Visibility>): FunctionsRuleBuilder {
        builder.setThat { func -> visibilities.contains(func.declaration.visibility) }
        return builder
    }

    /** Filter or assertion criteria for have any visibility. */
    public fun haveAnyVisibility(vararg visibilities: Visibility): FunctionsRuleBuilder =
        haveAnyVisibility(visibilities.asList())

    /** Filter or assertion criteria for have annotation with argument. */
    public fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String?,
        argValue: String,
    ): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.annotations.any { ann ->
                (ann.name == annotationName || ann.fqName == annotationName) &&
                    ann.arguments.any { arg ->
                        (argName == null || arg.name == argName) && arg.value == argValue
                    }
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be suspend. */
    public fun beSuspend(): FunctionsRuleBuilder = haveModifier(Modifier.SUSPEND)

    /** Filter or assertion criteria for be inline. */
    public fun beInline(): FunctionsRuleBuilder = haveModifier(Modifier.INLINE)

    /** Filter or assertion criteria for be infix. */
    public fun beInfix(): FunctionsRuleBuilder = haveModifier(Modifier.INFIX)

    /** Filter or assertion criteria for be operator. */
    public fun beOperator(): FunctionsRuleBuilder = haveModifier(Modifier.OPERATOR)
}

/**
 * Trait interface for composite, logical and custom predicate filtering on functions.
 */
public interface FunctionsThatCompositeFilter : FunctionsThatScope {
    /** Filter or assertion criteria for not. */
    public fun not(): FunctionsThat = builder.not()

    /** Filter or assertion criteria for satisfy. */
    public infix fun satisfy(predicate: (FunctionDeclarationContext) -> Boolean): FunctionsRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /** Filter or assertion criteria for any of. */
    public fun anyOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for all of. */
    public fun allOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    /** Filter or assertion criteria for none of. */
    public fun noneOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        /** Filter or assertion criteria for predicates. */
        val predicates =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }
}
