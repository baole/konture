/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class FunctionsShould internal constructor(
    private val builder: FunctionsRuleBuilder,
) {
    infix fun resideInAPackage(packagePattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!PatternMatchers.matchesPackage(packagePattern, func.packageName)) {
                violations.add(
                    "Function ${func.declaration.name} should reside in package '$packagePattern' but resides in '${func.packageName}'",
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
                    "Function ${func.declaration.name} should reside in package in [${packagePatterns.joinToString()}] but resides in '${func.packageName}'",
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
                    "Function ${func.declaration.name} should reside in package matching predicate, but resides in '${func.packageName}'",
                )
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.name.endsWith(suffix)) {
                violations.add("Function ${func.declaration.name} should have name ending with '$suffix'")
            }
        }
        return builder
    }

    infix fun haveNameEndingWith(suffixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = suffixes.any { func.declaration.name.endsWith(it) }
            if (!matches) {
                violations.add(
                    "Function ${func.declaration.name} should have name ending with any of [${suffixes.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameEndingWith(vararg suffixes: String): FunctionsRuleBuilder = haveNameEndingWith(suffixes.toList())

    infix fun haveNameStartingWith(prefix: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.name.startsWith(prefix)) {
                violations.add("Function ${func.declaration.name} should have name starting with '$prefix'")
            }
        }
        return builder
    }

    infix fun haveNameStartingWith(prefixes: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = prefixes.any { func.declaration.name.startsWith(it) }
            if (!matches) {
                violations.add(
                    "Function ${func.declaration.name} should have name starting with any of [${prefixes.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameStartingWith(vararg prefixes: String): FunctionsRuleBuilder = haveNameStartingWith(prefixes.toList())

    infix fun haveNameMatching(pattern: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!PatternMatchers.matchesSimpleGlob(pattern, func.declaration.name)) {
                violations.add("Function ${func.declaration.name} should have name matching '$pattern'")
            }
        }
        return builder
    }

    infix fun haveNameMatching(patterns: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            val matches = patterns.any { PatternMatchers.matchesSimpleGlob(it, func.declaration.name) }
            if (!matches) {
                violations.add(
                    "Function ${func.declaration.name} should have name matching any of [${patterns.joinToString()}]",
                )
            }
        }
        return builder
    }

    fun haveNameMatching(vararg patterns: String): FunctionsRuleBuilder = haveNameMatching(patterns.toList())

    fun bePublic(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PUBLIC) {
                violations.add("Function ${func.declaration.name} should be public")
            }
        }
        return builder
    }

    fun beInternal(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.INTERNAL) {
                violations.add("Function ${func.declaration.name} should be internal")
            }
        }
        return builder
    }

    fun bePrivate(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PRIVATE) {
                violations.add("Function ${func.declaration.name} should be private")
            }
        }
        return builder
    }

    fun beProtected(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PROTECTED) {
                violations.add("Function ${func.declaration.name} should be protected")
            }
        }
        return builder
    }

    fun beSuspend(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.SUSPEND)) {
                violations.add("Function ${func.declaration.name} should be suspend")
            }
        }
        return builder
    }

    fun beInline(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.INLINE)) {
                violations.add("Function ${func.declaration.name} should be inline")
            }
        }
        return builder
    }

    fun beOpen(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.OPEN)) {
                violations.add("Function ${func.declaration.name} should be open")
            }
        }
        return builder
    }

    fun beAbstract(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.ABSTRACT)) {
                violations.add("Function ${func.declaration.name} should be abstract")
            }
        }
        return builder
    }

    infix fun haveReturnType(typeFqName: String): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.returnType != typeFqName) {
                violations.add(
                    "Function ${func.declaration.name} should have return type '$typeFqName' but was '${func.declaration.returnType}'",
                )
            }
        }
        return builder
    }

    infix fun haveReturnType(typeFqNames: List<String>): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!typeFqNames.contains(func.declaration.returnType)) {
                violations.add(
                    "Function ${func.declaration.name} should have return type in [${typeFqNames.joinToString()}] but was '${func.declaration.returnType}'",
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
                violations.add("Function ${func.declaration.name} should be annotated with @$annotationName")
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
                    "Function ${func.declaration.name} should be annotated with any of [${annotationNames.joinToString()}]",
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
                violations.add("Function ${func.declaration.name} should have all annotations: ${names.joinToString()}")
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
                    "Function ${func.declaration.name} should have at least one annotation of: ${names.joinToString()}",
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
                violations.add("Function ${func.declaration.name} should have modifier: $modifier")
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
                    "Function ${func.declaration.name} should have all modifiers: ${modifiers.joinToString()}, but is missing: ${missing.joinToString()}",
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
                    "Function ${func.declaration.name} should have at least one modifier of: ${modifiers.joinToString()}",
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
                    "Function ${func.declaration.name} should have visibility: $visibility but was: ${func.declaration.visibility}",
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
                    "Function ${func.declaration.name} should have any visibility of: ${visibilities.joinToString()} but was: ${func.declaration.visibility}",
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
                    "Function ${func.declaration.name} should take exactly parameter types: [${types.joinToString()}] but took: [${currentTypes.joinToString()}]",
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
                    "Function ${func.declaration.name} should have at least one parameter of any type of: ${types.joinToString()}",
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

    fun beExtension(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.isExtension) {
                violations.add("Function ${func.declaration.name} should be an extension function")
            }
        }
        return builder
    }

    fun beDocumentedWithKDoc(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.kdocText.isNullOrBlank()) {
                violations.add("Function ${func.declaration.name} should be documented with KDoc")
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
                violations.add("Function ${func.declaration.name} should satisfy: $description")
            }
        }
        return builder
    }

    fun satisfy(assertion: (FunctionDeclarationContext, MutableList<String>) -> Unit): FunctionsRuleBuilder {
        builder.setShould { func, _, violations -> assertion(func, violations) }
        return builder
    }
}
