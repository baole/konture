/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.fileassertions

import java.io.Serializable

annotation class FileTargetMarker

@FileTargetMarker
class FileAssertionSampleClass : Serializable {
    fun sample() = "sample"
}

val sampleFileTopLevelProp: String = "file_prop"

fun sampleFileTopLevelFunc(): String = "file_func"
