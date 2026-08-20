/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/**
 * Fluent API for defining assertion rules on Kotlin classes.
 */
@Suppress("ComplexInterface")
public interface ClassesShouldCompositeAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: ClassesRuleBuilder

    /**
     * Asserts that selected classes do not expose types annotated with the specified annotations
     * in their property, function return, or parameter signatures.
     *
     * @param annotationNames Annotation simple or fully qualified names that must not appear on signature types.
     */
    public fun notHaveSignaturesWithTypesAnnotatedWith(vararg annotationNames: String): ClassesRuleBuilder {
        builder.setShould { cls, allClasses, violations ->
            /** Filter or assertion criteria for signature types. */
            val signatureTypes = cls.collectSignatureTypeNames()
            for (typeName in signatureTypes) {
                /** Filter or assertion criteria for resolved. */
                val resolved = cls.resolveTypeReference(typeName, allClasses) ?: continue

                /** Filter or assertion criteria for forbidden annotation. */
                val forbiddenAnnotation =
                    resolved.annotations.find { annotation ->
                        annotationNames.any { target -> annotation.matchesName(target) }
                    }
                if (forbiddenAnnotation != null) {
                    violations.add(
                        getMessage(
                            "class.should.notExposeForbiddenSignature",
                            cls.fqName,
                            resolved.fqName,
                            forbiddenAnnotation.name,
                        ),
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
    public infix fun notHaveSignaturesWithTypesAnnotatedWith(annotationName: String): ClassesRuleBuilder =
        notHaveSignaturesWithTypesAnnotatedWith(listOf(annotationName))

    /**
     * Asserts that selected classes do not expose types annotated with any of the specified annotations
     * in their property, function return, or parameter signatures.
     */
    public infix fun notHaveSignaturesWithTypesAnnotatedWith(annotationNames: List<String>): ClassesRuleBuilder =
        notHaveSignaturesWithTypesAnnotatedWith(*annotationNames.toTypedArray())

    /** Asserts that selected classes satisfy custom assertion [assertion]. */
    public infix fun satisfy(assertion: (ClassDeclaration) -> Boolean): ClassesRuleBuilder =
        satisfy(id = "custom condition", description = "custom condition") { cls -> assertion(cls) }

    /** Asserts that selected classes satisfy custom description [description]. */
    public fun satisfy(description: String): ClassesRuleBuilder =
        satisfy(id = description, description = description) { false }

    /** Asserts that selected classes satisfy custom predicate [predicate] with [description]. */
    public fun satisfy(
        description: String,
        predicate: (ClassDeclaration) -> Boolean,
    ): ClassesRuleBuilder = satisfy(id = description, description = description) { cls -> predicate(cls) }

    /**
     * Asserts that selected classes satisfy a custom predicate within a [SatisfyContext] block identified by [id] and optional [description].
     */
    public fun satisfy(
        id: String,
        description: String? = null,
        predicate: SatisfyContext<ClassDeclaration>.(ClassDeclaration) -> Boolean,
    ): ClassesRuleBuilder {
        builder.setShould { cls, _, violations ->
            val currentState = io.github.baole.konture.impl.KontureRuntimeStateProvider.currentState
            val activeSeverity = currentState.currentRuleMetadata?.severity ?: io.github.baole.konture.core.model.Severity.ERROR
            val activeTags = currentState.currentRuleMetadata?.tags ?: emptySet()
            val overrideMeta =
                io.github.baole.konture.core.model.RuleMetadata(
                    id = id,
                    description = description,
                    severity = activeSeverity,
                    tags = activeTags,
                )

            io.github.baole.konture.impl.KontureRuntimeStateProvider.runWithState(
                currentState.copy(currentRuleMetadata = overrideMeta),
            ) {
                val context =
                    SatisfyContextImpl(
                        subject = cls,
                        id = id,
                        description = description,
                        graph = builder.graph,
                        rawMessages = violations,
                    )
                val initialCount = violations.size
                val passed = context.predicate(cls)
                if (!passed && violations.size == initialCount) {
                    val msg = description ?: getMessage("class.should.satisfyCustom", cls.fqName, id)
                    violations.add(msg)
                }
            }
        }
        return builder
    }

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
    public fun satisfy(assertion: (ClassDeclaration, MutableList<String>) -> Unit): ClassesRuleBuilder {
        builder.setShould { cls, _, violations -> assertion(cls, violations) }
        return builder
    }

    /**
     * Asserts that at least one of the nested assertion blocks is satisfied.
     */
    public fun anyOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for assertions. */
        val assertions =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { cls, allCls, violations ->
            /** Filter or assertion criteria for temp violations list. */
            val tempViolationsList =
                assertions.map { assertion ->
                    /** Filter or assertion criteria for temp. */
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
    public fun allOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for assertions. */
        val assertions =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
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
    public fun noneOf(vararg blocks: ClassesShould.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for assertions. */
        val assertions =
            blocks.map { block ->
                /** Filter or assertion criteria for temp builder. */
                val tempBuilder = ClassesRuleBuilder(builder.graph)
                ClassesShould(tempBuilder).block()
                tempBuilder.getShouldAssertion() ?: { _, _, _ -> }
            }
        builder.setShould { cls, allCls, violations ->
            assertions.forEach { assertion ->
                /** Filter or assertion criteria for temp. */
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
    public fun allFunctions(block: FunctionAssertionScope.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for scope. */
        val scope = FunctionAssertionScope().apply(block)
        builder.setShould { cls, _, violations ->
            for (func in cls.functions) {
                /** Filter or assertion criteria for func violations. */
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
    public fun allProperties(block: PropertyAssertionScope.() -> Unit): ClassesRuleBuilder {
        /** Filter or assertion criteria for scope. */
        val scope = PropertyAssertionScope().apply(block)
        builder.setShould { cls, _, violations ->
            for (prop in cls.properties) {
                /** Filter or assertion criteria for prop violations. */
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
