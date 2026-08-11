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
interface FunctionsThatScope {
    val builder: FunctionsRuleBuilder
}

/**
 * Trait interface for package and module residency filtering on functions.
 */
@Suppress("ComplexInterface")
interface FunctionsThatPackageFilter : FunctionsThatScope {
    infix fun resideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.packageName) }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        resideInAPackage(packagePatterns.toList())

    infix fun resideInAPackage(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { predicate(it.packageName) }
        return builder
    }

    infix fun resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder =
        resideInAPackage(type.toKonturePackageReference().packageName)

    infix fun notResideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setThat { !PatternMatchers.matchesPackage(packagePattern, it.packageName) }
        return builder
    }

    infix fun notResideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context -> packagePatterns.none { PatternMatchers.matchesPackage(it, context.packageName) } }
        return builder
    }

    fun notResideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        notResideInAPackage(packagePatterns.toList())

    infix fun resideInAModule(modulePath: String): FunctionsRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setThat { it.modulePath == normalized }
        return builder
    }

    infix fun resideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
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

    fun resideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun resideInModule(modulePath: String): FunctionsRuleBuilder = resideInAModule(modulePath)

    infix fun resideInModules(modulePaths: List<String>): FunctionsRuleBuilder = resideInAModule(modulePaths)

    fun resideInModules(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): FunctionsRuleBuilder {
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

    infix fun notResideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
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

    fun notResideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = notResideInAModule(modulePaths.toList())

    infix fun notResideInModule(modulePath: String): FunctionsRuleBuilder = notResideInAModule(modulePath)

    infix fun notResideInModules(modulePaths: List<String>): FunctionsRuleBuilder = notResideInAModule(modulePaths)

    fun notResideInModules(vararg modulePaths: String): FunctionsRuleBuilder = notResideInAModule(modulePaths.toList())
}

/**
 * Trait interface for name matching and naming pattern filtering on functions.
 */
@Suppress("ComplexInterface")
interface FunctionsThatNameFilter : FunctionsThatScope {
    infix fun haveName(name: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name == name }
        return builder
    }

    infix fun haveName(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { names.contains(it.declaration.name) }
        return builder
    }

    fun haveName(vararg names: String): FunctionsRuleBuilder = haveName(names.toList())

    infix fun notHaveName(name: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name != name }
        return builder
    }

    infix fun notHaveName(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { !names.contains(it.declaration.name) }
        return builder
    }

    fun notHaveName(vararg names: String): FunctionsRuleBuilder = notHaveName(names.toList())

    infix fun notHaveName(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { !predicate(it.declaration.name) }
        return builder
    }

    infix fun haveName(predicate: (String) -> Boolean): FunctionsRuleBuilder =
        haveName("custom name predicate", predicate)

    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): FunctionsRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun notHaveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setThat { !it.declaration.name.endsWith(suffix) }
        return builder
    }

    infix fun notHaveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    fun notHaveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = notHaveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun notHaveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setThat { !it.declaration.name.startsWith(prefix) }
        return builder
    }

    infix fun notHaveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    fun notHaveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder =
        notHaveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): FunctionsRuleBuilder = haveNameMatching(patterns.toList())

    infix fun notHaveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setThat { !PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    infix fun notHaveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setThat { context ->
            !patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    fun notHaveNameMatching(vararg patterns: String): FunctionsRuleBuilder = notHaveNameMatching(patterns.toList())
}

/**
 * Trait interface for structural, parameter, return type and declaration location filtering on functions.
 */
@Suppress("ComplexInterface")
interface FunctionsThatStructureFilter : FunctionsThatScope {
    fun areExtension(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.isExtension }
        return builder
    }

    infix fun haveExtensionReceiver(receiverTypeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.receiverType?.let { receiver ->
                receiver == receiverTypeFqName || receiver.endsWith(".$receiverTypeFqName") || receiverTypeFqName.endsWith(".$receiver")
            } ?: false
        }
        return builder
    }

    infix fun haveExtensionReceiver(kClass: KClass<*>): FunctionsRuleBuilder =
        haveExtensionReceiver(kClass.kontureQualifiedName())

    fun areTopLevel(): FunctionsRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    fun areMember(): FunctionsRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    fun beTopLevel(): FunctionsRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    fun beMember(): FunctionsRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    infix fun haveParameterOf(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.any { p ->
                p.type == typeFqName || p.type.endsWith(".$typeFqName") || typeFqName.endsWith(".${p.type}")
            }
        }
        return builder
    }

    infix fun haveParameterOf(kClass: KClass<*>): FunctionsRuleBuilder = haveParameterOf(kClass.kontureQualifiedName())

    infix fun haveParameterOf(types: List<String>): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.any { p ->
                types.any { typeFqName ->
                    p.type == typeFqName || p.type.endsWith(".$typeFqName") || typeFqName.endsWith(".${p.type}")
                }
            }
        }
        return builder
    }

    fun haveParameterOf(vararg types: String): FunctionsRuleBuilder = haveParameterOf(types.toList())

    infix fun notHaveParameterOf(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.none { p ->
                p.type == typeFqName || p.type.endsWith(".$typeFqName") || typeFqName.endsWith(".${p.type}")
            }
        }
        return builder
    }

    infix fun notHaveParameterOf(type: KClass<*>): FunctionsRuleBuilder =
        notHaveParameterOf(type.kontureQualifiedName())

    infix fun haveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.returnType == typeFqName }
        return builder
    }

    infix fun haveReturnType(type: KClass<*>): FunctionsRuleBuilder {
        val expectedType = type.toKontureTypeReference()
        builder.setThat { function ->
            function.declaration.resolvedReturnType?.let { matchesKotlinType(it, expectedType) } == true
        }
        return builder
    }

    infix fun haveReturnType(typeFqNames: List<String>): FunctionsRuleBuilder {
        builder.setThat { func -> typeFqNames.contains(func.declaration.returnType) }
        return builder
    }

    fun haveReturnType(vararg typeFqNames: String): FunctionsRuleBuilder = haveReturnType(typeFqNames.asList())

    infix fun notHaveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.returnType != typeFqName && !func.declaration.returnType.endsWith(".$typeFqName")
        }
        return builder
    }

    infix fun notHaveReturnType(type: KClass<*>): FunctionsRuleBuilder = notHaveReturnType(type.kontureQualifiedName())

    infix fun haveParameterTypes(types: List<String>): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.size == types.size &&
                func.declaration.parameters.zip(types).all { (param, expectedType) ->
                    param.type == expectedType || param.type.endsWith(".$expectedType")
                }
        }
        return builder
    }

    fun haveParameterTypes(vararg types: String): FunctionsRuleBuilder = haveParameterTypes(types.asList())

    fun haveParameterTypes(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): FunctionsRuleBuilder {
        val types = arrayOf(first, *additional).map { it.toKontureTypeReference() }
        builder.setThat { function ->
            function.declaration.parameters.size == types.size &&
                function.declaration.parameters.zip(types).all { (parameter, type) ->
                    parameter.resolvedType?.let { matchesKotlinType(it, type) } == true
                }
        }
        return builder
    }

    infix fun haveAnyParameterType(types: List<String>): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.any { param ->
                types.any { expectedType ->
                    param.type == expectedType || param.type.endsWith(".$expectedType")
                }
            }
        }
        return builder
    }

    fun haveAnyParameterType(vararg types: String): FunctionsRuleBuilder = haveAnyParameterType(types.asList())

    fun haveAnyParameterType(
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): FunctionsRuleBuilder {
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

    fun haveNoParameters(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.parameters.isEmpty() }
        return builder
    }

    infix fun haveParameterCount(count: Int): FunctionsRuleBuilder {
        builder.setThat { it.declaration.parameters.size == count }
        return builder
    }

    infix fun haveParameterCount(predicate: (Int) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { predicate(it.declaration.parameters.size) }
        return builder
    }

    infix fun belongToClass(className: String): FunctionsRuleBuilder {
        builder.setThat { it.className == className || (it.className != null && it.qualifiedName.contains(className)) }
        return builder
    }

    infix fun belongToClass(type: KClass<*>): FunctionsRuleBuilder = belongToClass(type.kontureQualifiedName())
}

/**
 * Trait interface for modifiers, annotations, visibility and modifier filtering on functions.
 */
@Suppress("ComplexInterface")
interface FunctionsThatModifierFilter : FunctionsThatScope {
    fun arePublic(): FunctionsRuleBuilder = haveVisibility(Visibility.PUBLIC)

    fun bePublic(): FunctionsRuleBuilder = arePublic()

    fun areInternal(): FunctionsRuleBuilder = haveVisibility(Visibility.INTERNAL)

    fun beInternal(): FunctionsRuleBuilder = areInternal()

    fun arePrivate(): FunctionsRuleBuilder = haveVisibility(Visibility.PRIVATE)

    fun bePrivate(): FunctionsRuleBuilder = arePrivate()

    fun areProtected(): FunctionsRuleBuilder = haveVisibility(Visibility.PROTECTED)

    fun beProtected(): FunctionsRuleBuilder = areProtected()

    fun notBePublic(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PUBLIC }
        return builder
    }

    fun notBeInternal(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.INTERNAL }
        return builder
    }

    fun notBePrivate(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PRIVATE }
        return builder
    }

    fun notBeProtected(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PROTECTED }
        return builder
    }

    infix fun haveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setThat { it.hasAnnotation(annotationName) }
        return builder
    }

    infix fun haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    infix fun areAnnotatedWith(annotationName: String): FunctionsRuleBuilder = haveAnnotationOf(annotationName)

    infix fun areAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder = haveAnnotationOf(annotation)

    infix fun haveAnnotationOf(annotationNames: List<String>): FunctionsRuleBuilder {
        builder.setThat { func -> annotationNames.any { func.hasAnnotation(it) } }
        return builder
    }

    fun haveAnnotationOf(vararg annotationNames: String): FunctionsRuleBuilder =
        haveAnnotationOf(annotationNames.asList())

    infix fun notHaveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setThat { !it.hasAnnotation(annotationName) }
        return builder
    }

    infix fun notHaveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        notHaveAnnotationOf(annotation.kontureQualifiedName())

    infix fun notBeAnnotatedWith(annotationName: String): FunctionsRuleBuilder = notHaveAnnotationOf(annotationName)

    infix fun notBeAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        notHaveAnnotationOf(annotation)

    infix fun haveAllAnnotationsOf(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    fun haveAllAnnotationsOf(vararg names: String): FunctionsRuleBuilder = haveAllAnnotationsOf(names.asList())

    infix fun haveAnyAnnotationOf(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    fun haveAnyAnnotationOf(vararg names: String): FunctionsRuleBuilder = haveAnyAnnotationOf(names.asList())

    fun areOpen(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    fun areAbstract(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.ABSTRACT) }
        return builder
    }

    fun areOverride(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

    infix fun haveModifier(modifier: Modifier): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(modifier) }
        return builder
    }

    infix fun haveAllModifiers(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setThat { func -> modifiers.all { func.declaration.modifiers.contains(it) } }
        return builder
    }

    fun haveAllModifiers(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAllModifiers(modifiers.asList())

    infix fun haveAnyModifier(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setThat { func -> modifiers.any { func.declaration.modifiers.contains(it) } }
        return builder
    }

    fun haveAnyModifier(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAnyModifier(modifiers.asList())

    infix fun haveVisibility(visibility: Visibility): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility == visibility }
        return builder
    }

    infix fun haveAnyVisibility(visibilities: List<Visibility>): FunctionsRuleBuilder {
        builder.setThat { func -> visibilities.contains(func.declaration.visibility) }
        return builder
    }

    fun haveAnyVisibility(vararg visibilities: Visibility): FunctionsRuleBuilder =
        haveAnyVisibility(visibilities.asList())

    fun haveAnnotationWithArgument(
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

    fun beSuspend(): FunctionsRuleBuilder = haveModifier(Modifier.SUSPEND)

    fun beInline(): FunctionsRuleBuilder = haveModifier(Modifier.INLINE)

    fun beInfix(): FunctionsRuleBuilder = haveModifier(Modifier.INFIX)

    fun beOperator(): FunctionsRuleBuilder = haveModifier(Modifier.OPERATOR)
}

/**
 * Trait interface for composite, logical and custom predicate filtering on functions.
 */
interface FunctionsThatCompositeFilter : FunctionsThatScope {
    fun not(): FunctionsThat = builder.not()

    infix fun satisfy(predicate: (FunctionDeclarationContext) -> Boolean): FunctionsRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    fun anyOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.any { it(item) } }
        return builder
    }

    fun allOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.all { it(item) } }
        return builder
    }

    fun noneOf(vararg blocks: FunctionsThat.() -> Unit): FunctionsRuleBuilder {
        val predicates =
            blocks.map { block ->
                val tempBuilder = FunctionsRuleBuilder(builder.graph)
                FunctionsThat(tempBuilder).block()
                tempBuilder.getThatPredicate() ?: { true }
            }
        builder.setThat { item -> predicates.none { it(item) } }
        return builder
    }
}
