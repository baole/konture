/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Fluent API for defining assertion rules on Kotlin classes.
 */
@KontureDsl
class ClassesShould internal constructor(
    /** Filter or assertion criteria for builder. */
    override val builder: ClassesRuleBuilder,
) :
    ClassesShouldPackageAssertions,
        ClassesShouldMetadataAssertions,
        ClassesShouldDependencyAssertions,
        ClassesShouldCompositeAssertions
