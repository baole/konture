/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/** Modifier and visibility assertions for property rules. */
public interface PropertiesShouldModifierAssertions {
    /** Filter or assertion criteria for builder. */
    val builder: PropertiesRuleBuilder

    /** Filter or assertion criteria for be public. */
    fun bePublic(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PUBLIC) {
                violations.add(
                    getMessage("property.should.bePublic", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be internal. */
    fun beInternal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.INTERNAL) {
                violations.add(
                    getMessage("property.should.beInternal", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be private. */
    fun bePrivate(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PRIVATE) {
                violations.add(
                    getMessage("property.should.bePrivate", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be protected. */
    fun beProtected(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != Visibility.PROTECTED) {
                violations.add(
                    getMessage("property.should.beProtected", prop.qualifiedName, prop.declaration.visibility),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be public. */
    fun notBePublic(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility == Visibility.PUBLIC) {
                violations.add(getMessage("property.should.notBePublic", prop.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be internal. */
    fun notBeInternal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility == Visibility.INTERNAL) {
                violations.add(getMessage("property.should.notBeInternal", prop.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be private. */
    fun notBePrivate(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility == Visibility.PRIVATE) {
                violations.add(getMessage("property.should.notBePrivate", prop.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be protected. */
    fun notBeProtected(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility == Visibility.PROTECTED) {
                violations.add(getMessage("property.should.notBeProtected", prop.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be var. */
    fun beVar(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.isVal) {
                violations.add(
                    getMessage("property.should.beVar", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be val. */
    fun beVal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.isVal) {
                violations.add(
                    getMessage("property.should.beVal", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be open. */
    fun beOpen(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.OPEN)) {
                violations.add(
                    getMessage("property.should.beOpen", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be abstract. */
    fun beAbstract(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.ABSTRACT)) {
                violations.add(
                    getMessage("property.should.beAbstract", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be override. */
    fun beOverride(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.OVERRIDE)) {
                violations.add(
                    getMessage("property.should.beOverride", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be extension. */
    fun beExtension(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.isExtension) {
                violations.add(
                    getMessage("property.should.beExtension", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be extension. */
    fun notBeExtension(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.isExtension) {
                violations.add(getMessage("property.should.notBeExtension", prop.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be const. */
    fun beConst(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.CONST)) {
                violations.add(
                    getMessage("property.should.beConst", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be const. */
    fun notBeConst(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.modifiers.contains(Modifier.CONST)) {
                violations.add(getMessage("property.should.notBeConst", prop.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for be lateinit. */
    fun beLateinit(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(Modifier.LATEINIT)) {
                violations.add(
                    getMessage("property.should.beLateinit", prop.qualifiedName),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for not be lateinit. */
    fun notBeLateinit(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.modifiers.contains(Modifier.LATEINIT)) {
                violations.add(getMessage("property.should.notBeLateinit", prop.qualifiedName))
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have all modifiers. */
    infix fun haveAllModifiers(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            /** Filter or assertion criteria for missing. */
            val missing = modifiers.filter { !prop.declaration.modifiers.contains(it) }
            if (missing.isNotEmpty()) {
                violations.add(
                    getMessage(
                        "property.should.haveAllModifiers",
                        prop.qualifiedName,
                        modifiers.joinToString(),
                        missing.joinToString(),
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have all modifiers. */
    fun haveAllModifiers(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAllModifiers(modifiers.asList())

    /** Filter or assertion criteria for have any modifier. */
    infix fun haveAnyModifier(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!modifiers.any { prop.declaration.modifiers.contains(it) }) {
                violations.add(
                    getMessage("property.should.haveAnyModifier", prop.qualifiedName, modifiers.joinToString()),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have any modifier. */
    fun haveAnyModifier(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAnyModifier(modifiers.asList())

    /** Filter or assertion criteria for have modifier. */
    infix fun haveModifier(modifier: Modifier): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!prop.declaration.modifiers.contains(modifier)) {
                violations.add(
                    getMessage("property.should.haveModifier", prop.qualifiedName, modifier),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have modifier. */
    infix fun haveModifier(modifiers: List<Modifier>): PropertiesRuleBuilder = haveAnyModifier(modifiers)

    /** Filter or assertion criteria for have modifier. */
    fun haveModifier(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAnyModifier(modifiers.toList())

    /** Filter or assertion criteria for have visibility. */
    infix fun haveVisibility(visibility: Visibility): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility != visibility) {
                violations.add(
                    getMessage(
                        "property.should.haveVisibility",
                        prop.qualifiedName,
                        visibility,
                        prop.declaration.visibility,
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have any visibility. */
    infix fun haveAnyVisibility(visibilities: List<Visibility>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (!visibilities.contains(prop.declaration.visibility)) {
                violations.add(
                    getMessage(
                        "property.should.haveAnyVisibility",
                        prop.qualifiedName,
                        visibilities.joinToString(),
                        prop.declaration.visibility,
                    ),
                )
            }
        }
        return builder
    }

    /** Filter or assertion criteria for have any visibility. */
    fun haveAnyVisibility(vararg visibilities: Visibility): PropertiesRuleBuilder =
        haveAnyVisibility(visibilities.asList())
}
