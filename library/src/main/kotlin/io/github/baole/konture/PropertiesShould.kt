/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

@KontureDsl
class PropertiesShould internal constructor(
    override val builder: PropertiesRuleBuilder,
) : PropertiesShouldModifierAssertions,
    PropertiesShouldTypeAssertions,
    PropertiesShouldCompositeAssertions

inline fun <reified T : Any> PropertiesShould.haveTypeOf(): PropertiesRuleBuilder = haveType(T::class)
