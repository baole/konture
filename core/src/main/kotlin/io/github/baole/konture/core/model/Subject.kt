/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.core.model

import kotlinx.serialization.Serializable

/**
 * Sealed hierarchy representing a target element or subject of an architectural rule.
 */
@Serializable
public sealed interface Subject {
    public val name: String
    public val location: SourceLocation?

    /**
     * Subject representing a Gradle module or project.
     *
     * @property path The Gradle project path (e.g., `:core:model`).
     * @property location Optional source location (e.g. project directory).
     */
    @Serializable
    public data class ModuleSubject(
        val path: String,
        override val location: SourceLocation? = null,
    ) : Subject {
        override val name: String get() = path
    }

    /**
     * Subject representing a Kotlin class, interface, object, or enum.
     *
     * @property fqName Fully qualified class name.
     * @property simpleName Simple class name.
     * @property location Optional source location.
     */
    @Serializable
    public data class ClassSubject(
        val fqName: String,
        val simpleName: String,
        override val location: SourceLocation? = null,
    ) : Subject {
        override val name: String get() = fqName
    }

    /**
     * Subject representing a Kotlin function or method declaration.
     *
     * @property fqName Fully qualified function name.
     * @property location Optional source location.
     */
    @Serializable
    public data class FunctionSubject(
        val fqName: String,
        override val location: SourceLocation? = null,
    ) : Subject {
        override val name: String get() = fqName
    }

    /**
     * Custom or generic subject type for arbitrary rule targets.
     *
     * @property name Subject display name or pattern.
     * @property location Optional source location.
     */
    @Serializable
    public data class CustomSubject(
        override val name: String,
        override val location: SourceLocation? = null,
    ) : Subject
}
