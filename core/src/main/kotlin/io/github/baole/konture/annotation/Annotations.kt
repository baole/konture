/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.annotation

/**
 * Marker annotation for Konture DSL builders to isolate receiver scopes.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class KontureDsl

/**
 * Marks internal Konture API that is subject to change or removal without notice.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal Konture API and should not be used outside of Konture.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class InternalKontureApi

/**
 * Marks experimental Konture API that is subject to change in future minor releases.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This Konture API is experimental and subject to change.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class ExperimentalKontureApi
