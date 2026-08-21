/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.layered.presentation

import io.github.baole.konture.tests.layered.domain.DomainService

class PresentationComponent(private val service: DomainService) {
    fun render(): String = service.execute().id
}
