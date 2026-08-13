/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Common scope interface providing access to [PropertiesRuleBuilder].
 */
@KontureDsl
public interface PropertiesThatScope {
    /** The underlying [PropertiesRuleBuilder] used to accumulate property filtering rules. */
    public val builder: PropertiesRuleBuilder
}
