/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.layered.domain

class DomainModel(val id: String)

class DomainService {
    fun execute(): DomainModel = DomainModel("test")
}
