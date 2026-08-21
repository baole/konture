/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.layered.data

import io.github.baole.konture.tests.layered.domain.DomainModel

class DataRepository {
    fun fetch(): DomainModel = DomainModel("data")
}
