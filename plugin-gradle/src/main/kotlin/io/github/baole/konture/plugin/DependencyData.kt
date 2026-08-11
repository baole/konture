/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SerialVersionUIDInSerializableClass")

package io.github.baole.konture.plugin

import java.io.Serializable

/** Serialized representation of a Gradle project dependency relationship. */
public data class DependencyData(
    val configuration: String,
    val targetBuildId: String,
    val targetPath: String,
) : Serializable
