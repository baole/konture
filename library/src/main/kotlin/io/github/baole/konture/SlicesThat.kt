/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers
import kotlin.reflect.KClass

@KontureDsl
class SlicesThat internal constructor(
    private val builder: SlicesRuleBuilder,
) {
    /**
     * Restricts the slice rule to slices whose key matches the specified key pattern.
     */
    infix fun haveKey(keyPattern: String): SlicesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(keyPattern, it.key) }
        return builder
    }

    /**
     * Restricts the slice rule to slices that contain a class with the specified FQN or simple name.
     */
    infix fun containClass(fqName: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.classes.any { it.fqName == fqName || it.name == fqName }
        }
        return builder
    }

    /**
     * Restricts the slice rule to slices that contain a class of the specified [KClass].
     */
    infix fun containClass(type: KClass<*>): SlicesRuleBuilder =
        containClass(type.kontureQualifiedName())

    /**
     * Restricts the slice rule to slices that contain packages matching the specified pattern.
     */
    infix fun containClassesInPackage(packagePattern: String): SlicesRuleBuilder {
        builder.setThat { slice ->
            slice.packages.any { PatternMatchers.matchesPackage(packagePattern, it) }
        }
        return builder
    }

    /**
     * Restricts the slice rule using a custom predicate on [Slice].
     */
    infix fun satisfy(predicate: (Slice) -> Boolean): SlicesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }
}
