/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/** Modifier and visibility assertions for function rules. */
public interface FunctionsShouldModifierAssertions {
    /** Filter or assertion criteria for builder. */
    val builder: FunctionsRuleBuilder

    /** Filter or assertion criteria for be public. */
    fun bePublic(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PUBLIC) {
                violations.add(
                    getMessage("function.should.bePublic", func.qualifiedName, func.declaration.visibility),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be internal. */
    fun beInternal(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.INTERNAL) {
                violations.add(
                    getMessage("function.should.beInternal", func.qualifiedName, func.declaration.visibility),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be private. */
    fun bePrivate(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PRIVATE) {
                violations.add(
                    getMessage("function.should.bePrivate", func.qualifiedName, func.declaration.visibility),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be protected. */
    fun beProtected(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility != Visibility.PROTECTED) {
                violations.add(
                    getMessage("function.should.beProtected", func.qualifiedName, func.declaration.visibility),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be suspend. */
    fun beSuspend(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.SUSPEND)) {
                violations.add(
                    getMessage("function.should.beSuspend", func.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be inline. */
    fun beInline(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.INLINE)) {
                violations.add(
                    getMessage("function.should.beInline", func.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be open. */
    fun beOpen(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.OPEN)) {
                violations.add(
                    getMessage("function.should.beOpen", func.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be abstract. */
    fun beAbstract(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.ABSTRACT)) {
                violations.add(
                    getMessage("function.should.beAbstract", func.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be override. */
    fun beOverride(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.OVERRIDE)) {
                violations.add(
                    getMessage("function.should.beOverride", func.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be operator. */
    fun beOperator(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.OPERATOR)) {
                violations.add(
                    getMessage("function.should.haveModifier", func.qualifiedName, Modifier.OPERATOR),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be infix. */
    fun beInfix(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(Modifier.INFIX)) {
                violations.add(
                    getMessage("function.should.haveModifier", func.qualifiedName, Modifier.INFIX),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be top level. */
    fun beTopLevel(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.className != null) {
                violations.add(getMessage("function.should.beTopLevel", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be member. */
    fun beMember(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.className == null) {
                violations.add(getMessage("function.should.beMember", func.qualifiedName))
            }
        }
        return builder
    }

    /**
     * Asserts that selected functions contain the specified modifier.
     */
    infix fun haveModifier(modifier: Modifier): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.modifiers.contains(modifier)) {
                violations.add(
                    getMessage("function.should.haveModifier", func.qualifiedName, modifier),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not have modifier. */
    infix fun notHaveModifier(modifier: Modifier): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.modifiers.contains(modifier)) {
                violations.add(
                    getMessage("function.should.notHaveModifier", func.qualifiedName, modifier),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be public. */
    fun notBePublic(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility == Visibility.PUBLIC) {
                violations.add(getMessage("function.should.notBePublic", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be internal. */
    fun notBeInternal(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility == Visibility.INTERNAL) {
                violations.add(getMessage("function.should.notBeInternal", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be private. */
    fun notBePrivate(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility == Visibility.PRIVATE) {
                violations.add(getMessage("function.should.notBePrivate", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be protected. */
    fun notBeProtected(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.visibility == Visibility.PROTECTED) {
                violations.add(getMessage("function.should.notBeProtected", func.qualifiedName))
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
            /** Filter or assertion criteria for missing. */
            val missing = modifiers.filter { !func.declaration.modifiers.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "function.should.haveAllModifiers",
                        func.qualifiedName,
                        modifiers.joinToString(),
                        missing.joinToString(),
                    ),
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
                    getMessage("function.should.haveAnyModifier", func.qualifiedName, modifiers.joinToString()),
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
                    getMessage(
                        "function.should.haveVisibility",
                        func.qualifiedName,
                        visibility,
                        func.declaration.visibility,
                    ),
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
                    getMessage(
                        "function.should.haveAnyVisibility",
                        func.qualifiedName,
                        visibilities.joinToString(),
                        func.declaration.visibility,
                    ),
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
    fun haveAnyVisibility(vararg visibilities: Visibility): FunctionsRuleBuilder =
        haveAnyVisibility(visibilities.asList())

    /** Filter or assertion criteria for be extension. */
    fun beExtension(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (!func.declaration.isExtension) {
                violations.add(
                    getMessage("function.should.beExtension", func.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be extension. */
    fun notBeExtension(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.isExtension) {
                violations.add(getMessage("function.should.notBeExtension", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be documented with k doc. */
    fun beDocumentedWithKDoc(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.kdocText.isNullOrBlank()) {
                violations.add(
                    getMessage("function.should.beDocumented", func.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be suspend. */
    fun notBeSuspend(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.modifiers.contains(Modifier.SUSPEND)) {
                violations.add(getMessage("function.should.notBeSuspend", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be inline. */
    fun notBeInline(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.modifiers.contains(Modifier.INLINE)) {
                violations.add(getMessage("function.should.notBeInline", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be infix. */
    fun notBeInfix(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.modifiers.contains(Modifier.INFIX)) {
                violations.add(getMessage("function.should.notBeInfix", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be operator. */
    fun notBeOperator(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.modifiers.contains(Modifier.OPERATOR)) {
                violations.add(getMessage("function.should.notBeOperator", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be open. */
    fun notBeOpen(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.modifiers.contains(Modifier.OPEN)) {
                violations.add(getMessage("function.should.notBeOpen", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be abstract. */
    fun notBeAbstract(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.modifiers.contains(Modifier.ABSTRACT)) {
                violations.add(getMessage("function.should.notBeAbstract", func.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be override. */
    fun notBeOverride(): FunctionsRuleBuilder {
        builder.setShould { func, _, violations ->
            if (func.declaration.modifiers.contains(Modifier.OVERRIDE)) {
                violations.add(getMessage("function.should.notBeOverride", func.qualifiedName))
            }
        }
        return builder
    }
}
