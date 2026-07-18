/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/**
 * Fluent API for defining assertion rules on Kotlin classes.
 */
@KontureDsl
class ClassesShould internal constructor(
    override val builder: ClassesRuleBuilder,
) :
    ClassesShouldPackageAssertions,
        ClassesShouldMetadataAssertions,
        ClassesShouldDependencyAssertions,
        ClassesShouldCompositeAssertions
