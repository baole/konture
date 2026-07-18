/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/**
 * Fluent API for defining assertion rules on Kotlin classes.
 */
internal interface ClassesShouldCompositeAssertions {
    val builder: ClassesRuleBuilder

    /**
     * Asserts that selected classes do not expose types annotated with the specified annotations
     * in their property, function return, or parameter signatures.
     *
     * @param annotationNames Annotation simple or fully qualified names that must not appear on signature types.
     */
    fun notHaveSignaturesWithTypesAnnotatedWith(vararg annotationNames: String): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            val signatureTypes = cls.collectSignatureTypeNames()
            for (typeName in signatureTypes) {
                val resolved = cls.resolveTypeReference(typeName, allClasses) ?: continue
                val forbiddenAnnotation =
                    resolved.annotations.find { annotation ->
                        annotationNames.any { target -> annotation.matchesName(target) }
                    }
                if (forbiddenAnnotation != null) {
                    violations.add(
                        getMessage("class.should.notExposeForbiddenSignature", cls.fqName, resolved.fqName, forbiddenAnnotation.name),
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that selected classes do not expose types annotated with the specified annotation
     * in their property, function return, or parameter signatures.
     */
    infix fun notHaveSignaturesWithTypesAnnotatedWith(annotationName: String): ClassesRuleBuilder =
        notHaveSignaturesWithTypesAnnotatedWith(listOf(annotationName))

    /**
     * Asserts that selected classes do not expose types annotated with any of the specified annotations
     * in their property, function return, or parameter signatures.
     */
    infix fun notHaveSignaturesWithTypesAnnotatedWith(annotationNames: List<String>): ClassesRuleBuilder =
        notHaveSignaturesWithTypesAnnotatedWith(*annotationNames.toTypedArray())

    /**
     * Asserts that selected classes satisfy a custom condition.
     *
     * @param assertion Custom assertion checking the class.
     */
    infix fun satisfy(assertion: (ClassDeclaration) -> Boolean): ClassesRuleBuilder = satisfy("custom condition") { cls, _ -> assertion(cls) }

    /**
     * Asserts that selected classes satisfy a custom condition.
     *
     * @param description A descriptive string for the custom condition used in violations.
     * @param assertion Custom assertion checking the class.
     */
    infix fun satisfy(description: String): ClassesRuleBuilder = satisfy(description) { cls, _ -> false }

    private fun satisfy(
        description: String,
        assertion: (ClassDeclaration, List<ClassDeclaration>) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            if (!assertion(cls, allClasses)) {
                violations.add(getMessage("class.should.satisfyCustom", cls.fqName, description))
            }
        }
        return builder
    }

    /**
     * Satisfies an arbitrary custom assertion logic with custom violations builder.
     */
    fun satisfy(assertion: (ClassDeclaration, MutableList<String>) -> Unit): ClassesRuleBuilder {
        builder.setShould { cls, _, violations -> assertion(cls, violations) }
        return builder
    }

    /**
     * Asserts that at least one of the nested assertion blocks is satisfied.
     */
    fun anyOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { cls, allCls, violations ->
            val tempViolationsList =
                assertions.map { assertion ->
                    val temp = mutableListOf<String>()
                    assertion(cls, allCls, temp)
                    temp
                }
            if (tempViolationsList.all { it.isNotEmpty() }) {
                violations.add(getMessage("class.should.satisfyAtLeastOneNested", cls.fqName))
            }
        }
        return builder
    }

    /**
     * Asserts that all of the nested assertion blocks are satisfied.
     */
    fun allOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { cls, allCls, violations ->
            assertions.forEach { assertion ->
                assertion(cls, allCls, violations)
            }
        }
        return builder
    }

    /**
     * Asserts that none of the nested assertion blocks are satisfied.
     */
    fun noneOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        val assertions =
            blocks.map { block ->
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { cls, allCls, violations ->
            assertions.forEach { assertion ->
                val temp = mutableListOf<String>()
                assertion(cls, allCls, temp)
                if (temp.isEmpty()) {
                    violations.add(getMessage("class.should.notSatisfyNested", cls.fqName))
                }
            }
        }
        return builder
    }

    /**
     * Asserts that all member functions in selected classes satisfy the assertions specified in the [block].
     */
    fun allFunctions(block: FunctionAssertionScope.() -> Unit): ClassesRuleBuilder {
        val scope = FunctionAssertionScope().apply(block)
        builder.setShould { cls, _, violations ->
            for (func in cls.functions) {
                val funcViolations = mutableListOf<String>()
                for (assertion in scope.assertions) {
                    assertion(func, funcViolations)
                }
                if (funcViolations.isNotEmpty()) {
                    violations.add(
                        "Function ${func.name} in class ${cls.fqName} has violations:\n" +
                            funcViolations.joinToString("\n") {
                                "  - $it"
                            },
                    )
                }
            }
        }
        return builder
    }

    /**
     * Asserts that all member properties in selected classes satisfy the assertions specified in the [block].
     */
    fun allProperties(block: PropertyAssertionScope.() -> Unit): ClassesRuleBuilder {
        val scope = PropertyAssertionScope().apply(block)
        builder.setShould { cls, _, violations ->
            for (prop in cls.properties) {
                val propViolations = mutableListOf<String>()
                for (assertion in scope.assertions) {
                    assertion(prop, propViolations)
                }
                if (propViolations.isNotEmpty()) {
                    violations.add(
                        "Property ${prop.name} in class ${cls.fqName} has violations:\n" +
                            propViolations.joinToString("\n") {
                                "  - $it"
                            },
                    )
                }
            }
        }
        return builder
    }
}
