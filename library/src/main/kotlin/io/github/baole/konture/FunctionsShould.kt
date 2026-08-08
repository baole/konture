/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

@KontureDsl
class FunctionsShould internal constructor(
    override val builder: FunctionsRuleBuilder,
) : FunctionsShouldCallAssertions,
    FunctionsShouldNameAssertions,
    FunctionsShouldModifierAssertions,
    FunctionsShouldSignatureAssertions,
    FunctionsShouldCompositeAssertions


