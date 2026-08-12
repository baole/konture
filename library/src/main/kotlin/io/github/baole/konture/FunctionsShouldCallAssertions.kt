/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers
import io.github.baole.konture.impl.ViolationLocation
import kotlin.reflect.KClass

/** Member call assertions for function rules. */
public interface FunctionsShouldCallAssertions {
    /** Filter or assertion criteria for builder. */
    public val builder: FunctionsRuleBuilder

    /** Fails when the selected function invokes [fqName]. */
    public fun notCall(fqName: String): FunctionsRuleBuilder {
        builder.setShould { function, _, violations ->
            function.usages
                .filter { usage -> PatternMatchers.isCallUsageMatch(usage, fqName) }
                .forEach { usage ->
                    /** Filter or assertion criteria for unresolved. */
                    val unresolved = if (usage.unresolvedPossibleUsage) "unresolved possible " else ""
                    violations.add(
                        "${getMessage("usage.notCall", unresolved, fqName, usage.rawExpression, usage.line, usage.column)} " +
                            "(at ${ViolationLocation.format(usage.filePath, usage.line, usage.column, function.modulePath, function.sourceSet?.name, fqName = function.className?.let { "${function.packageName}.$it" } ?: function.packageName, packageName = function.packageName)})",
                    )
                }
        }
        return builder
    }

    /** Fails when the selected function invokes [kClass]. */
    public fun notCall(kClass: KClass<*>): FunctionsRuleBuilder = notCall(kClass.kontureQualifiedName())

    /** Fails for every actual class/type use of [fqName] in the selected function; imports alone do not match. */
    public fun notReferenceClass(fqName: String): FunctionsRuleBuilder {
        builder.setShould { function, _, violations ->
            function.usages
                .filter { usage ->
                    usage.kind == UsageKind.CLASS_REFERENCE &&
                        (usage.targetFqName == fqName || usage.targetFqName.endsWith(".$fqName") || fqName.endsWith("." + usage.targetFqName) || usage.rawExpression == fqName || fqName in usage.possibleTargetFqNames)
                }.forEach { usage ->
                    violations.add(
                        "${getMessage("usage.notReferenceClass", fqName, usage.rawExpression, usage.line, usage.column)} " +
                            "(at ${ViolationLocation.format(usage.filePath, usage.line, usage.column, function.modulePath, function.sourceSet?.name, fqName = function.className?.let { "${function.packageName}.$it" } ?: function.packageName, packageName = function.packageName)})",
                    )
                }
        }
        return builder
    }

    /** Fails for every actual class/type use of [kClass] in the selected function; imports alone do not match. */
    public fun notReferenceClass(kClass: KClass<*>): FunctionsRuleBuilder =
        notReferenceClass(kClass.kontureQualifiedName())
}

/** Fails when the selected function invokes [T]. */
public inline fun <reified T : Any> FunctionsShouldCallAssertions.notCall(): FunctionsRuleBuilder = notCall(T::class)

/** Fails for every actual class/type use of [T] in the selected function. */
public inline fun <reified T : Any> FunctionsShouldCallAssertions.notReferenceClass(): FunctionsRuleBuilder =
    notReferenceClass(
        T::class,
    )
