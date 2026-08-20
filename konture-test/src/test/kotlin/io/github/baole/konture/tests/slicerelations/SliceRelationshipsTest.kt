/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.tests.slicerelations

import io.github.baole.konture.Konture
import io.github.baole.konture.Visibility
import io.github.baole.konture.slices
import org.junit.jupiter.api.Test

class SliceRelationshipsTest {
    @Test
    fun `feature slices must be free of cyclic dependencies`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.slices.(*)..")
            should().beFreeOfCycles()
        }
    }

    @Test
    fun `feature slices must not depend on forbidden slices`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.slices.(*)..")
            should().notDependOnSlice("forbidden")
                .andShould().containClasses()
        }
    }

    @Test
    fun `slice classes visibility assertions`() {
        Konture.slices {
            matching("io.github.baole.konture.tests.slices.(*)..")
            should().containOnlyClassesWithVisibility(Visibility.PUBLIC)
        }
    }
}
