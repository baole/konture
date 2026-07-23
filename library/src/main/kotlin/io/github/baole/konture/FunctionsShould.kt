/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class FunctionsShould internal constructor(
    private val builder: FunctionsRuleBuilder,
) {
    /** Fails when the selected function invokes [fqName]. */
    fun notCall(fqName: String): FunctionsRuleBuilder {
        builder.setShould { function, _, violations ->
            function.usages
                .filter { usage ->
                    usage.kind == UsageKind.CALL &&
                        (usage.targetFqName == fqName || fqName in usage.possibleTargetFqNames)
                }.forEach { usage ->
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(getMessage("usage.notCall", unresolved, fqName, usage.rawExpression, usage.line, usage.column))
                }
        }
        return builder
    }

    /** Fails when the selected function invokes [kClass]. */
    fun notCall(kClass: kotlin.reflect.KClass<*>): FunctionsRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails when the selected function invokes [T]. */
    inline fun <reified T : Any> notCall(): FunctionsRuleBuilder = notCall(T::class)

    infix fun resideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, func.packageName)) {
                violations.add(
                    getMessage("function.should.resideInPackage", func.declaration.name, packagePattern, func.packageName),
                )
            }
        }
        return builder
    }

    infix fun resideInAPackage(packagePatterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = packagePatterns.any { PatternMatchers.matchesPackage(it, func.packageName) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.resideInPackageAny", func.declaration.name, packagePatterns.joinToString(), func.packageName),
                )
            }
        }
        return builder
    }

    fun resideInAPackage(vararg packagePatterns: String): FunctionsRuleBuilder =
        resideInAPackage(
            packagePatterns.toList(),
        )

    infix fun resideInAPackage(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!predicate(func.packageName)) {
                violations.add(
                    getMessage("function.should.resideInPackageMatching", func.declaration.name, func.packageName),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.name.endsWith(suffix)) {
                violations.add(
                    getMessage("function.should.haveNameEndingWith", func.declaration.name, suffix),
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = suffixes.any { func.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameEndingWithAny", func.declaration.name, suffixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.name.startsWith(prefix)) {
                violations.add(
                    getMessage("function.should.haveNameStartingWith", func.declaration.name, prefix),
                )
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = prefixes.any { func.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameStartingWithAny", func.declaration.name, prefixes.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, func.declaration.name)) {
                violations.add(
                    getMessage("function.should.haveNameMatching", func.declaration.name, pattern),
                )
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, func.declaration.name) }
            if (!matches) {
                violations.add(
                    getMessage("function.should.haveNameMatchingAny", func.declaration.name, patterns.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): FunctionsRuleBuilder = haveNameMatching(patterns.toList())

    fun bePublic(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PUBLIC) {
                violations.add(
                    getMessage("function.should.bePublic", func.declaration.name),
                )
            }
        }
        return builder
    }

    fun beInternal(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.INTERNAL) {
                violations.add(
                    getMessage("function.should.beInternal", func.declaration.name),
                )
            }
        }
        return builder
    }

    fun bePrivate(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PRIVATE) {
                violations.add(
                    getMessage("function.should.bePrivate", func.declaration.name),
                )
            }
        }
        return builder
    }

    fun beProtected(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PROTECTED) {
                violations.add(
                    getMessage("function.should.beProtected", func.declaration.name),
                )
            }
        }
        return builder
    }

    fun beSuspend(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.SUSPEND)) {
                violations.add(
                    getMessage("function.should.beSuspend", func.declaration.name),
                )
            }
        }
        return builder
    }

    fun beInline(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.INLINE)) {
                violations.add(
                    getMessage("function.should.beInline", func.declaration.name),
                )
            }
        }
        return builder
    }

    fun beOpen(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.OPEN)) {
                violations.add(
                    getMessage("function.should.beOpen", func.declaration.name),
                )
            }
        }
        return builder
    }

    fun beAbstract(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.ABSTRACT)) {
                violations.add(
                    getMessage("function.should.beAbstract", func.declaration.name),
                )
            }
        }
        return builder
    }

    infix fun haveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.returnType != typeFqName) {
                violations.add(
                    getMessage("function.should.haveReturnType", func.declaration.name, typeFqName, func.declaration.returnType),
                )
            }
        }
        return builder
    }

    /** Asserts that selected functions have the specified raw return type. */
    infix fun haveReturnType(type: kotlin.reflect.KClass<*>): FunctionsRuleBuilder {
        val expectedType = type.toKontureTypeReference()
        builder.setShould { function, _, violations ->
            if (function.declaration.resolvedReturnType?.let { matchesKotlinType(it, expectedType) } != true) {
                violations.add(
                    getMessage(
                        "function.should.haveReturnType",
                        function.declaration.name,
                        type.kontureQualifiedName(),
                        function.declaration.returnType,
                    ),
                )
            }
        }
        return builder
    }

    /** Asserts that selected functions have the specified raw return type. */
    inline fun <reified T : Any> haveReturnTypeOf(): FunctionsRuleBuilder = haveReturnType(T::class)

    infix fun haveReturnType(typeFqNames: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!typeFqNames.contains(func.declaration.returnType)) {
                violations.add(
                    getMessage("function.should.haveReturnTypeAny", func.declaration.name, typeFqNames.joinToString(), func.declaration.returnType),
                )
            }
        }
        return builder
    }

    fun haveReturnType(vararg typeFqNames: String): FunctionsRuleBuilder = haveReturnType(typeFqNames.asList())

    infix fun haveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val hasAnnotation =
                func.declaration.annotations.any {
                    it.name == annotationName || it.fqName == annotationName
                }
            if (!hasAnnotation) {
                violations.add(
                    getMessage("function.should.haveAnnotation", func.declaration.name, annotationName),
                )
            }
        }
        return builder
    }

    infix fun haveAnnotationOf(annotationNames: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val hasAnnotation =
                func.declaration.annotations.any { ann ->
                    annotationNames.any { it == ann.name || it == ann.fqName }
                }
            if (!hasAnnotation) {
                violations.add(
                    getMessage("function.should.haveAnnotationAny", func.declaration.name, annotationNames.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveAnnotationOf(vararg annotationNames: String): FunctionsRuleBuilder =
        haveAnnotationOf(
            annotationNames.asList(),
        )

    /**
     * Asserts that selected functions are annotated with all of the specified annotations.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.hasAllAnnotations(names)) {
                violations.add(
                    getMessage("function.should.haveAllAnnotations", func.declaration.name, names.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions are annotated with all of the specified annotations.
     */
    fun haveAllAnnotationsOf(vararg names: String): FunctionsRuleBuilder = haveAllAnnotationsOf(names.asList())

    /**
     * Asserts that selected functions are annotated with any of the specified annotations.
     */
    infix fun haveAnyAnnotationOf(names: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.hasAnyAnnotation(names)) {
                violations.add(
                    getMessage("function.should.haveAnyAnnotation", func.declaration.name, names.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions are annotated with any of the specified annotations.
     */
    fun haveAnyAnnotationOf(vararg names: String): FunctionsRuleBuilder = haveAnyAnnotationOf(names.asList())

    /**
     * Asserts that selected functions contain the specified modifier.
     */
    infix fun haveModifier(modifier: Modifier): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(modifier)) {
                violations.add(
                    getMessage("function.should.haveModifier", func.declaration.name, modifier),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions have all of the specified modifiers.
     *
     * @param modifiers The list of modifiers that must all be present.
     */
    infix fun haveAllModifiers(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val missing = modifiers.filter { !func.declaration.modifiers.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(
                    getMessage("function.should.haveAllModifiers", func.declaration.name, modifiers.joinToString(), missing.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions have all of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers that must all be present.
     */
    fun haveAllModifiers(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAllModifiers(modifiers.asList())

    /**
     * Asserts that selected functions have at least one of the specified modifiers.
     *
     * @param modifiers The list of modifiers, at least one of which must be present.
     */
    infix fun haveAnyModifier(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!modifiers.any { func.declaration.modifiers.contains(it) }) {
                violations.add(
                    getMessage("function.should.haveAnyModifier", func.declaration.name, modifiers.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions have at least one of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers, at least one of which must be present.
     */
    fun haveAnyModifier(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAnyModifier(modifiers.asList())

    /**
     * Asserts that selected functions have the specified visibility.
     */
    infix fun haveVisibility(visibility: Visibility): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != visibility) {
                violations.add(
                    getMessage("function.should.haveVisibility", func.declaration.name, visibility, func.declaration.visibility),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions have any of the specified visibilities.
     *
     * @param visibilities The list of acceptable visibilities.
     */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!visibilities.contains(func.declaration.visibility)) {
                violations.add(
                    getMessage("function.should.haveAnyVisibility", func.declaration.name, visibilities.joinToString(), func.declaration.visibility),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions have any of the specified visibilities.
     *
     * @param visibilities The vararg list of acceptable visibilities.
     */
    fun haveAnyVisibility(vararg visibilities: Visibility): FunctionsRuleBuilder = haveAnyVisibility(visibilities.asList())

    /**
     * Asserts that selected functions take exactly these parameter types in order (simple or fully qualified).
     *
     * @param types The list of expected parameter types.
     */
    infix fun haveParameterTypes(types: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val match =
                func.declaration.parameters.size == types.size &&
                    func.declaration.parameters.zip(types).all { (param, expectedType) ->
                        param.type == expectedType || param.type.endsWith(".$expectedType")
                    }
            if (!match) {
                val currentTypes = func.declaration.parameters.map { it.type }
                violations.add(
                    getMessage("function.should.haveParameterTypes", func.declaration.name, types.joinToString(), currentTypes.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions take exactly these parameter types in order (simple or fully qualified).
     *
     * @param types The vararg list of expected parameter types.
     */
    fun haveParameterTypes(vararg types: String): FunctionsRuleBuilder = haveParameterTypes(types.asList())

    /** Asserts that selected functions take exactly these raw parameter types in order. */
    fun haveParameterTypes(
        first: kotlin.reflect.KClass<*>,
        vararg additional: kotlin.reflect.KClass<*>,
    ): FunctionsRuleBuilder {
        val types = arrayOf(first, *additional).map { it.toKontureTypeReference() }
        builder.setShould { function, _, violations ->
            val matches =
                function.declaration.parameters.size == types.size &&
                    function.declaration.parameters.zip(types).all {
                            (parameter, type) ->
                        parameter.resolvedType?.let { matchesKotlinType(it, type) } == true
                    }
            if (!matches) {
                violations.add(
                    getMessage(
                        "function.should.haveParameterTypes",
                        function.declaration.name,
                        types.joinToString {
                            it.qualifiedName
                        },
                        function.declaration.parameters.joinToString { it.type },
                    ),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions have at least one parameter of one of the specified types.
     *
     * @param types The list of possible parameter types.
     */
    infix fun haveAnyParameterType(types: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val hasAny =
                func.declaration.parameters.any { param ->
                    types.any { expectedType ->
                        param.type == expectedType || param.type.endsWith(".$expectedType")
                    }
                }
            if (!hasAny) {
                violations.add(
                    getMessage("function.should.haveAnyParameterType", func.declaration.name, types.joinToString()),
                )
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions have at least one parameter of one of the specified types.
     *
     * @param types The vararg list of possible parameter types.
     */
    fun haveAnyParameterType(vararg types: String): FunctionsRuleBuilder = haveAnyParameterType(types.asList())

    /** Asserts that selected functions have a parameter of any specified raw type. */
    fun haveAnyParameterType(
        first: kotlin.reflect.KClass<*>,
        vararg additional: kotlin.reflect.KClass<*>,
    ): FunctionsRuleBuilder {
        val types = arrayOf(first, *additional).map { it.toKontureTypeReference() }
        builder.setShould { function, _, violations ->
            if (function.declaration.parameters.none {
                        parameter ->
                    parameter.resolvedType?.let { resolvedType -> types.any { matchesKotlinType(resolvedType, it) } } == true
                }
            ) {
                violations.add(getMessage("function.should.haveAnyParameterType", function.declaration.name, types.joinToString { it.qualifiedName }))
            }
        }
        return builder
    }

    /** Asserts that selected functions have a parameter of raw type [T]. */
    inline fun <reified T : Any> haveAnyParameterTypeOf(): FunctionsRuleBuilder = haveAnyParameterType(T::class)

    fun beExtension(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.isExtension) {
                violations.add(
                    getMessage("function.should.beExtension", func.declaration.name),
                )
            }
        }
        return builder
    }

    fun beDocumentedWithKDoc(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.kdocText.isNullOrBlank()) {
                violations.add(
                    getMessage("function.should.beDocumented", func.declaration.name),
                )
            }
        }
        return builder
    }

    infix fun satisfy(assertion: (FunctionDeclarationContext) -> Boolean): FunctionsRuleBuilder = satisfy("custom condition") { f, _ -> assertion(f) }

    private fun satisfy(
        description: String,
        assertion: (FunctionDeclarationContext, List<FunctionDeclarationContext>) -> Boolean,
    ): FunctionsRuleBuilder {
        builder.setShould { func, allFuncs, violations ->
            if (!assertion(func, allFuncs)) {
                violations.add(
                    getMessage("function.should.satisfyCustom", func.declaration.name, description),
                )
            }
        }
        return builder
    }

    fun satisfy(assertion: (FunctionDeclarationContext, MutableList<String>) -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, _, violations -> assertion(func, violations) }
        return builder
    }
}
