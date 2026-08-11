/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

@KontureDsl
class FilesShould internal constructor(
    override val builder: FilesRuleBuilder,
) : FilesShouldPathAssertions,
    FilesShouldContentAssertions,
    FilesShouldCompositeAssertions

inline fun <reified T : Any> FilesShould.notCall(): FilesRuleBuilder = notCall(T::class)

inline fun <reified T : Any> FilesShould.notReferenceClass(): FilesRuleBuilder = notReferenceClass(T::class)

inline fun <reified T : Annotation> FilesShould.haveAnnotationOf(): FilesRuleBuilder = haveAnnotationOf(T::class)
