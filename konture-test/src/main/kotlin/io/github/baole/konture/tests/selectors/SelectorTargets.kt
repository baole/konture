/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.selectors

interface SelectorSampleRepository {
    fun findById(id: String): String
}

class SelectorSampleRepositoryImpl : SelectorSampleRepository {
    override fun findById(id: String): String = id
}

class SelectorSampleService(
    val repo: SelectorSampleRepository,
) {
    val serviceName: String = "SelectorSampleService"

    fun process() {
        repo.findById("1")
    }
}
