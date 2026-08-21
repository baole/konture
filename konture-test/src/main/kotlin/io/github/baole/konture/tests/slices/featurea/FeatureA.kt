/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.slices.featurea

import io.github.baole.konture.tests.slices.common.CommonSliceUtility

class FeatureAService {
    private val utility = CommonSliceUtility()

    fun runA(): String = utility.format("FeatureA")
}
