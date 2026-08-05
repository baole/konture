/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import kotlin.reflect.KClass

interface FunctionsShouldSignatureAssertions {
    val builder: FunctionsRuleBuilder

    infix fun haveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.returnType != typeFqName) {
                violations.add(
                    getMessage(
                        "function.should.haveReturnType",
                        func.qualifiedName,
                        typeFqName,
                        func.declaration.returnType,
                    ),
                )
            }
        }
        return builder
    }

    /** Asserts that selected functions have the specified raw return type. */
    infix fun haveReturnType(type: KClass<*>): FunctionsRuleBuilder {
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

    infix fun haveReturnType(typeFqNames: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!typeFqNames.contains(func.declaration.returnType)) {
                violations.add(
                    getMessage(
                        "function.should.haveReturnTypeAny",
                        func.qualifiedName,
                        typeFqNames.joinToString(),
                        func.declaration.returnType,
                    ),
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
                    getMessage("function.should.haveAnnotation", func.qualifiedName, annotationName),
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
                    getMessage("function.should.haveAnnotationAny", func.qualifiedName, annotationNames.joinToString()),
                )
            }
        }
        return builder
    }

    fun haveAnnotationOf(vararg annotationNames: String): FunctionsRuleBuilder =
        haveAnnotationOf(
            annotationNames.asList(),
        )

    fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String? = null,
        argValue: String,
    ): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches =
                func.declaration.annotations.any { ann ->
                    (ann.name == annotationName || ann.fqName == annotationName) &&
                        ann.arguments.any { arg ->
                            (argName == null || arg.name == argName) && arg.value == argValue
                        }
                }
            if (!matches) {
                violations.add(
                    getMessage(
                        "function.should.haveAnnotationWithArgument",
                        func.qualifiedName,
                        annotationName,
                        argName ?: "any",
                        argValue,
                    ),
                )
            }
        }
        return builder
    }

    infix fun resideInAModule(modulePath: String): FunctionsRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { func, _, violations ->
            if (func.modulePath != normalized) {
                violations.add(getMessage("function.should.resideInModule", func.qualifiedName, normalized, func.modulePath))
            }
        }
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
        builder.setShould { func, _, violations ->
            if (!normalizedPaths.contains(func.modulePath)) {
                violations.add(getMessage("function.should.resideInModule", func.qualifiedName, normalizedPaths.joinToString(), func.modulePath))
            }
        }
        return builder
    }

    fun resideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = resideInAModule(modulePaths.toList())

    infix fun notResideInAModule(modulePath: String): FunctionsRuleBuilder {
        val normalized =
            if (!modulePath.startsWith(":") && !modulePath.startsWith("**") && modulePath.isNotEmpty()) {
                ":$modulePath"
            } else {
                modulePath
            }
        builder.setShould { func, _, violations ->
            if (func.modulePath == normalized) {
                violations.add(getMessage("function.should.notResideInModule", func.qualifiedName, normalized))
            }
        }
        return builder
    }

    infix fun notResideInAModule(modulePaths: List<String>): FunctionsRuleBuilder {
        val normalizedPaths =
            modulePaths.map { path ->
                if (!path.startsWith(":") && !path.startsWith("**") && path.isNotEmpty()) {
                    ":$path"
                } else {
                    path
                }
            }
        builder.setShould { func, _, violations ->
            if (normalizedPaths.contains(func.modulePath)) {
                violations.add(getMessage("function.should.notResideInModuleAny", func.qualifiedName, normalizedPaths.joinToString()))
            }
        }
        return builder
    }

    fun notResideInAModule(vararg modulePaths: String): FunctionsRuleBuilder = notResideInAModule(modulePaths.toList())


    /**
     * Asserts that selected functions are annotated with all of the specified annotations.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.hasAllAnnotations(names)) {
                violations.add(
                    getMessage("function.should.haveAllAnnotations", func.qualifiedName, names.joinToString()),
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
                    getMessage("function.should.haveAnyAnnotation", func.qualifiedName, names.joinToString()),
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
                    getMessage(
                        "function.should.haveParameterTypes",
                        func.qualifiedName,
                        types.joinToString(),
                        currentTypes.joinToString(),
                    ),
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
        first: KClass<*>,
        vararg additional: KClass<*>,
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
                    getMessage("function.should.haveAnyParameterType", func.qualifiedName, types.joinToString()),
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
        first: KClass<*>,
        vararg additional: KClass<*>,
    ): FunctionsRuleBuilder {
        val types = arrayOf(first, *additional).map { it.toKontureTypeReference() }
        builder.setShould { function, _, violations ->
            if (function.declaration.parameters.none {
                        parameter ->
                    parameter.resolvedType?.let {
                            resolvedType ->
                        types.any { matchesKotlinType(resolvedType, it) }
                    } == true
                }
            ) {
                violations.add(
                    getMessage(
                        "function.should.haveAnyParameterType",
                        function.declaration.name,
                        types.joinToString {
                            it.qualifiedName
                        },
                    ),
                )
            }
        }
        return builder
    }

    fun haveNoParameters(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.parameters.isNotEmpty()) {
                violations.add(getMessage("function.should.haveNoParameters", func.qualifiedName, func.declaration.parameters.size))
            }
        }
        return builder
    }
}

/** Asserts that selected functions have the specified raw return type. */
inline fun <reified T : Any> FunctionsShould.haveReturnTypeOf(): FunctionsRuleBuilder = haveReturnType(T::class)

/** Asserts that selected functions have a parameter of raw type [T]. */
inline fun <reified T : Any> FunctionsShould.haveAnyParameterTypeOf(): FunctionsRuleBuilder =
    haveAnyParameterType(T::class)
