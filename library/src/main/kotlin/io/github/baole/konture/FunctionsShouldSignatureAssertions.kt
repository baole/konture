/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import kotlin.reflect.KClass

/** Signature and type assertions for function rules. */
public interface FunctionsShouldSignatureAssertions {
    /** Filter or assertion criteria for builder. */
    val builder: FunctionsRuleBuilder

    /** Asserts that selected functions have the specified return type fully qualified name [typeFqName]. */
    public infix fun haveReturnType(typeFqName: String): FunctionsRuleBuilder {
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
        /** Filter or assertion criteria for expected type. */
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

    /** Filter or assertion criteria for have return type. */
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

    /** Filter or assertion criteria for have return type. */
    fun haveReturnType(vararg typeFqNames: String): FunctionsRuleBuilder = haveReturnType(typeFqNames.asList())

    /** Filter or assertion criteria for have annotation of. */
    infix fun haveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for has annotation. */
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

    /** Filter or assertion criteria for have annotation of. */
    infix fun haveAnnotationOf(annotationNames: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for has annotation. */
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

    /** Filter or assertion criteria for have annotation of. */
    fun haveAnnotationOf(vararg annotationNames: String): FunctionsRuleBuilder =
        haveAnnotationOf(
            annotationNames.asList(),
        )

    /** Filter or assertion criteria for have annotation with argument. */
    fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String? = null,
        argValue: String,
    ): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            /** Filter or assertion criteria for matches. */
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
            /** Filter or assertion criteria for match. */
            val match =
                func.declaration.parameters.size == types.size &&
                    func.declaration.parameters.zip(types).all { (param, expectedType) ->
                        param.type == expectedType || param.type.endsWith(".$expectedType")
                    }
            if (!match) {
                /** Filter or assertion criteria for current types. */
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
        /** Filter or assertion criteria for types. */
        val types = arrayOf(first, *additional).map { it.toKontureTypeReference() }
        builder.setShould { function, _, violations ->
            /** Filter or assertion criteria for matches. */
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
            /** Filter or assertion criteria for has any. */
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
        /** Filter or assertion criteria for types. */
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

    /** Filter or assertion criteria for have no parameters. */
    fun haveNoParameters(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.parameters.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "function.should.haveNoParameters",
                        func.qualifiedName,
                        func.declaration.parameters.size,
                    ),
                )
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
