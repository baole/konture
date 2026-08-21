/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.slices.featureb

import io.github.baole.konture.tests.slices.common.CommonSliceUtility

class FeatureBService {
    private val utility = CommonSliceUtility()

    fun runB(): String = utility.format("FeatureB")
}
