/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

internal fun KClass<*>.kontureQualifiedName(): String =
    qualifiedName ?: throw IllegalArgumentException("Konture requires a stable qualified name; local and anonymous classes are unsupported.")

/**
 * Returns the JVM package for an exact package-of-type rule.
 *
 * Konture's library module currently targets the JVM. A future non-JVM target should replace this
 * with platform-specific package discovery rather than depending on [Class.getPackageName].
 */
internal fun KClass<*>.konturePackageName(): String = java.packageName

internal data class KonturePackageReference(
    val packageName: String,
)

internal fun KClass<*>.toKonturePackageReference() = KonturePackageReference(konturePackageName())

internal data class KontureTypeReference(
    val qualifiedName: String,
    val simpleName: String?,
    val nestedName: String,
)

internal fun KClass<*>.toKontureTypeReference(): KontureTypeReference {
    val qualifiedName = kontureQualifiedName()
    return KontureTypeReference(
        qualifiedName = qualifiedName,
        simpleName = simpleName,
        nestedName = qualifiedName.removePrefix("${konturePackageName()}."),
    )
}

internal fun matchesKotlinType(
    declaredType: String,
    expectedType: KClass<*>,
): Boolean = matchesKotlinType(declaredType, expectedType.toKontureTypeReference())

internal fun matchesKotlinType(
    declaredType: String,
    expectedType: KontureTypeReference,
): Boolean {
    val declaredRawType = declaredType.trim().removeSuffix("?").substringBefore('<').trim()
    return declaredRawType == expectedType.qualifiedName
}
