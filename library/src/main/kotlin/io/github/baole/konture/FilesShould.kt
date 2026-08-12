/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

/** Assertion builder for checking rule expectations on Kotlin file declarations. */
@KontureDsl
public class FilesShould internal constructor(
    /** Filter or assertion criteria for builder. */
    public override val builder: FilesRuleBuilder,
) : FilesShouldPathAssertions,
    FilesShouldContentAssertions,
    FilesShouldCompositeAssertions

/** Asserts that files do not call members of type parameter [T]. */
public inline fun <reified T : Any> FilesShould.notCall(): FilesRuleBuilder = notCall(T::class)

/** Asserts that files do not reference class type parameter [T]. */
public inline fun <reified T : Any> FilesShould.notReferenceClass(): FilesRuleBuilder = notReferenceClass(T::class)

/** Asserts that files are annotated with annotation type parameter [T]. */
public inline fun <reified T : Annotation> FilesShould.haveAnnotationOf(): FilesRuleBuilder = haveAnnotationOf(T::class)
