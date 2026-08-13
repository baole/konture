/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

/**
 * Trait interface for structural, parameter, return type and declaration location filtering on functions.
 */
@Suppress("ComplexInterface")
public interface FunctionsThatStructureFilter : FunctionsThatScope {
    /** Filters functions that are extension functions. */
    public fun areExtension(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.isExtension }
        return builder
    }

    /** Filters functions that have an extension receiver matching [receiverTypeFqName]. */
    public infix fun haveExtensionReceiver(receiverTypeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.receiverType?.let { receiver ->
                receiver == receiverTypeFqName || receiver.endsWith(".$receiverTypeFqName") || receiverTypeFqName.endsWith(".$receiver")
            } ?: false
        }
        return builder
    }

    /** Filters functions that have an extension receiver of type [kClass]. */
    public infix fun haveExtensionReceiver(kClass: KClass<*>): FunctionsRuleBuilder =
        haveExtensionReceiver(kClass.kontureQualifiedName())

    /** Filters functions that are top-level declarations. */
    public fun areTopLevel(): FunctionsRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    /** Filters functions that are member functions within a class or object. */
    public fun areMember(): FunctionsRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    /** Filters functions that are top-level declarations. */
    public fun beTopLevel(): FunctionsRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    /** Filters functions that are member functions within a class or object. */
    public fun beMember(): FunctionsRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    /** Filters functions having at least one parameter matching type [typeFqName]. */
    public infix fun haveParameterOf(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.any { p ->
                p.type == typeFqName || p.type.endsWith(".$typeFqName") || typeFqName.endsWith(".${p.type}")
            }
        }
        return builder
    }

    /** Filters functions having at least one parameter of type [kClass]. */
    public infix fun haveParameterOf(kClass: KClass<*>): FunctionsRuleBuilder =
        haveParameterOf(kClass.kontureQualifiedName())

    /** Filters functions having at least one parameter matching any type in [types]. */
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

    /** Filters functions having at least one parameter matching any type in [types]. */
    public fun haveParameterOf(vararg types: String): FunctionsRuleBuilder = haveParameterOf(types.toList())

    /** Filters functions not having any parameter matching type [typeFqName]. */
    public infix fun notHaveParameterOf(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.none { p ->
                p.type == typeFqName || p.type.endsWith(".$typeFqName") || typeFqName.endsWith(".${p.type}")
            }
        }
        return builder
    }

    /** Filters functions not having any parameter of type [type]. */
    public infix fun notHaveParameterOf(type: KClass<*>): FunctionsRuleBuilder =
        notHaveParameterOf(type.kontureQualifiedName())

    /** Filters functions having return type matching [typeFqName]. */
    public infix fun haveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.returnType == typeFqName }
        return builder
    }

    /** Filters functions having return type matching [type]. */
    public infix fun haveReturnType(type: KClass<*>): FunctionsRuleBuilder {
        val expectedType = type.toKontureTypeReference()
        builder.setThat { function ->
            function.declaration.resolvedReturnType?.let { matchesKotlinType(it, expectedType) } == true
        }
        return builder
    }

    /** Filters functions having return type matching any in [typeFqNames]. */
    public infix fun haveReturnType(typeFqNames: List<String>): FunctionsRuleBuilder {
        builder.setThat { func -> typeFqNames.contains(func.declaration.returnType) }
        return builder
    }

    /** Filters functions having return type matching any in [typeFqNames]. */
    public fun haveReturnType(vararg typeFqNames: String): FunctionsRuleBuilder = haveReturnType(typeFqNames.asList())

    /** Filters functions not having return type matching [typeFqName]. */
    public infix fun notHaveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.returnType != typeFqName && !func.declaration.returnType.endsWith(".$typeFqName")
        }
        return builder
    }

    /** Filters functions not having return type matching [type]. */
    public infix fun notHaveReturnType(type: KClass<*>): FunctionsRuleBuilder =
        notHaveReturnType(type.kontureQualifiedName())

    /** Filters functions whose parameter types exactly match [types] in order and count. */
    public infix fun haveParameterTypes(types: List<String>): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.size == types.size &&
                func.declaration.parameters.zip(types).all { (param, expectedType) ->
                    param.type == expectedType || param.type.endsWith(".$expectedType")
                }
        }
        return builder
    }

    /** Filters functions whose parameter types exactly match [types] in order and count. */
    public fun haveParameterTypes(vararg types: String): FunctionsRuleBuilder = haveParameterTypes(types.asList())

    /** Filters functions whose parameter types exactly match [first] and [additional] classes in order and count. */
    public fun haveParameterTypes(
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

    /** Filters functions having at least one parameter type matching any in [types]. */
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

    /** Filters functions having at least one parameter type matching any in [types]. */
    public fun haveAnyParameterType(vararg types: String): FunctionsRuleBuilder = haveAnyParameterType(types.asList())

    /** Filters functions having at least one parameter type matching [first] or any in [additional]. */
    public fun haveAnyParameterType(
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

    /** Filters functions that take no parameters. */
    public fun haveNoParameters(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.parameters.isEmpty() }
        return builder
    }

    /** Filters functions taking exactly [count] parameters. */
    public infix fun haveParameterCount(count: Int): FunctionsRuleBuilder {
        builder.setThat { it.declaration.parameters.size == count }
        return builder
    }

    /** Filters functions whose parameter count satisfies [predicate]. */
    public infix fun haveParameterCount(predicate: (Int) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { predicate(it.declaration.parameters.size) }
        return builder
    }

    /** Filters functions declared within a class matching [className]. */
    public infix fun belongToClass(className: String): FunctionsRuleBuilder {
        builder.setThat { it.className == className || (it.className != null && it.qualifiedName.contains(className)) }
        return builder
    }

    /** Filters functions declared within class [type]. */
    public infix fun belongToClass(type: KClass<*>): FunctionsRuleBuilder = belongToClass(type.kontureQualifiedName())
}
