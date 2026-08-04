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
    override val builder: ClassesRuleBuilder,
) :
    ClassesShouldPackageAssertions,
        ClassesShouldMetadataAssertions,
        ClassesShouldDependencyAssertions,
        ClassesShouldCompositeAssertions {
        /** Fails for every invocation of [T] in the selected class body. */
        inline fun <reified T : Any> notCall(): ClassesRuleBuilder = notCall(T::class)

        /** Fails for every actual class/type use of [T] in the selected class body. */
        inline fun <reified T : Any> notReferenceClass(): ClassesRuleBuilder = notReferenceClass(T::class)
    }
