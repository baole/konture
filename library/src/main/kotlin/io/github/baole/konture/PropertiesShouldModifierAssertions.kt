/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

interface PropertiesShouldModifierAssertions {
    val builder: PropertiesRuleBuilder

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

    fun notBePublic(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility == Visibility.PUBLIC) {
                violations.add(getMessage("property.should.notBePublic", prop.qualifiedName))
            }
        }
        return builder
    }

    fun notBeInternal(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility == Visibility.INTERNAL) {
                violations.add(getMessage("property.should.notBeInternal", prop.qualifiedName))
            }
        }
        return builder
    }

    fun notBePrivate(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility == Visibility.PRIVATE) {
                violations.add(getMessage("property.should.notBePrivate", prop.qualifiedName))
            }
        }
        return builder
    }

    fun notBeProtected(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.visibility == Visibility.PROTECTED) {
                violations.add(getMessage("property.should.notBeProtected", prop.qualifiedName))
            }
        }
        return builder
    }

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

    fun notBeExtension(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.isExtension) {
                violations.add(getMessage("property.should.notBeExtension", prop.qualifiedName))
            }
        }
        return builder
    }

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

    fun notBeConst(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.modifiers.contains(Modifier.CONST)) {
                violations.add(getMessage("property.should.notBeConst", prop.qualifiedName))
            }
        }
        return builder
    }

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

    fun notBeLateinit(): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
            if (prop.declaration.modifiers.contains(Modifier.LATEINIT)) {
                violations.add(getMessage("property.should.notBeLateinit", prop.qualifiedName))
            }
        }
        return builder
    }

    infix fun haveAllModifiers(modifiers: List<Modifier>): PropertiesRuleBuilder {
        builder.setShould { prop, _, violations ->
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

    fun haveAllModifiers(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAllModifiers(modifiers.asList())

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

    fun haveAnyModifier(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAnyModifier(modifiers.asList())

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

    infix fun haveModifier(modifiers: List<Modifier>): PropertiesRuleBuilder = haveAnyModifier(modifiers)

    fun haveModifier(vararg modifiers: Modifier): PropertiesRuleBuilder = haveAnyModifier(modifiers.toList())

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

    fun haveAnyVisibility(vararg visibilities: Visibility): PropertiesRuleBuilder =
        haveAnyVisibility(visibilities.asList())
}
