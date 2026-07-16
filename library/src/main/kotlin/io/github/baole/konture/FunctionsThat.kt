/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class FunctionsThat internal constructor(
    private val builder: FunctionsRuleBuilder,
) {
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
        resideInAPackage(
            packagePatterns.toList(),
        )

    infix fun resideInAPackage(predicate: (String) -> Boolean): FunctionsRuleBuilder {
        builder.setThat { predicate(it.packageName) }
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

    fun beTopLevel(): FunctionsRuleBuilder {
        builder.setThat { it.className == null }
        return builder
    }

    fun beMember(): FunctionsRuleBuilder {
        builder.setThat { it.className != null }
        return builder
    }

    /**
     * Restricts the rules to functions annotated with the specified annotation.
     * Matches either the annotation's simple name or its FQN.
     *
     * @param annotationName The annotation name or fully qualified name.
     */
    infix fun haveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setThat { it.hasAnnotation(annotationName) }
        return builder
    }

    /**
     * Restricts the rules to functions annotated with any of the specified annotations.
     */
    infix fun haveAnnotationOf(annotationNames: List<String>): FunctionsRuleBuilder {
        builder.setThat { func -> annotationNames.any { func.hasAnnotation(it) } }
        return builder
    }

    /**
     * Restricts the rules to functions annotated with any of the specified annotations.
     */
    fun haveAnnotationOf(vararg annotationNames: String): FunctionsRuleBuilder = haveAnnotationOf(annotationNames.asList())

    /**
     * Restricts the rules to functions annotated with all of the specified annotations.
     * Matches either simple names or FQNs.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    /**
     * Restricts the rules to functions annotated with all of the specified annotations.
     * Matches either simple names or FQNs.
     */
    fun haveAllAnnotationsOf(vararg names: String): FunctionsRuleBuilder = haveAllAnnotationsOf(names.asList())

    /**
     * Restricts the rules to functions annotated with any of the specified annotations.
     * Matches either simple names or FQNs.
     */
    infix fun haveAnyAnnotationOf(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    /**
     * Restricts the rules to functions annotated with any of the specified annotations.
     * Matches either simple names or FQNs.
     */
    fun haveAnyAnnotationOf(vararg names: String): FunctionsRuleBuilder = haveAnyAnnotationOf(names.asList())

    /**
     * Restricts the rules to functions containing the specified modifier.
     */
    infix fun haveModifier(modifier: Modifier): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(modifier) }
        return builder
    }

    /**
     * Restricts the rules to functions containing all of the specified modifiers.
     *
     * @param modifiers The list of modifiers that must all be present.
     */
    infix fun haveAllModifiers(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setThat { func -> modifiers.all { func.declaration.modifiers.contains(it) } }
        return builder
    }

    /**
     * Restricts the rules to functions containing all of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers that must all be present.
     */
    fun haveAllModifiers(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAllModifiers(modifiers.asList())

    /**
     * Restricts the rules to functions containing any of the specified modifiers.
     *
     * @param modifiers The list of modifiers, at least one of which must be present.
     */
    infix fun haveAnyModifier(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setThat { func -> modifiers.any { func.declaration.modifiers.contains(it) } }
        return builder
    }

    /**
     * Restricts the rules to functions containing any of the specified modifiers.
     *
     * @param modifiers The vararg list of modifiers, at least one of which must be present.
     */
    fun haveAnyModifier(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAnyModifier(modifiers.asList())

    /**
     * Restricts the rules to functions with the specified visibility.
     */
    infix fun haveVisibility(visibility: Visibility): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility == visibility }
        return builder
    }

    /**
     * Restricts the rules to functions with any of the specified visibilities.
     *
     * @param visibilities The list of acceptable visibilities.
     */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): FunctionsRuleBuilder {
        builder.setThat { func -> visibilities.contains(func.declaration.visibility) }
        return builder
    }

    /**
     * Restricts the rules to functions with any of the specified visibilities.
     *
     * @param visibilities The vararg list of acceptable visibilities.
     */
    fun haveAnyVisibility(vararg visibilities: Visibility): FunctionsRuleBuilder = haveAnyVisibility(visibilities.asList())

    /**
     * Restricts the rules to functions with the specified return type (simple or fully qualified).
     */
    infix fun haveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setThat { it.declaration.returnType == typeFqName }
        return builder
    }

    /**
     * Restricts the rules to functions with any of the specified return types.
     */
    infix fun haveReturnType(typeFqNames: List<String>): FunctionsRuleBuilder {
        builder.setThat { func -> typeFqNames.contains(func.declaration.returnType) }
        return builder
    }

    /**
     * Restricts the rules to functions with any of the specified return types.
     */
    fun haveReturnType(vararg typeFqNames: String): FunctionsRuleBuilder = haveReturnType(typeFqNames.asList())

    /**
     * Restricts the rules to functions taking exactly these parameter types in order (simple or fully qualified).
     *
     * @param types The list of parameter types.
     */
    infix fun haveParameterTypes(types: List<String>): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.parameters.size == types.size &&
                func.declaration.parameters.zip(types).all { (param, expectedType) ->
                    param.type == expectedType || param.type.endsWith(".$expectedType")
                }
        }
        return builder
    }

    /**
     * Restricts the rules to functions taking exactly these parameter types in order (simple or fully qualified).
     *
     * @param types The vararg list of parameter types.
     */
    fun haveParameterTypes(vararg types: String): FunctionsRuleBuilder = haveParameterTypes(types.asList())

    /**
     * Restricts the rules to functions where at least one parameter is of one of the specified types.
     *
     * @param types The list of possible parameter types.
     */
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

    /**
     * Restricts the rules to functions where at least one parameter is of one of the specified types.
     *
     * @param types The vararg list of possible parameter types.
     */
    fun haveAnyParameterType(vararg types: String): FunctionsRuleBuilder = haveAnyParameterType(types.asList())

    infix fun satisfy(predicate: (FunctionDeclarationContext) -> Boolean): FunctionsRuleBuilder {
        builder.setThat(predicate)
        return builder
    }
}
