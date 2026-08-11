/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

/** Assertion scope DSL for configuring rule expectations on Kotlin functions. */
@KontureDsl
public class FunctionAssertionScope internal constructor() {
    /** Filter or assertion criteria for assertions. */
    val assertions = mutableListOf<(FunctionDeclaration, MutableList<String>) -> Unit>()

    /** Filter or assertion criteria for have name matching. */
    fun haveNameMatching(pattern: String) {
        haveNameMatching(listOf(pattern))
    }

    /** Filter or assertion criteria for have name matching. */
    fun haveNameMatching(patterns: List<String>) {
        assertions.add { func, violations ->
            if (patterns.none { PatternMatchers.matchesSimpleGlob(it, func.name) }) {
                violations.add(
                    getMessage(
                        "function.scope.haveNameMatching",
                        patterns.joinToString { "'$it'" },
                        func.name,
                    ),
                )
            }
        }
    }

    /** Filter or assertion criteria for have name matching. */
    fun haveNameMatching(vararg patterns: String) {
        haveNameMatching(patterns.asList())
    }

    /** Filter or assertion criteria for have name starting with. */
    fun haveNameStartingWith(prefix: String) {
        haveNameStartingWith(listOf(prefix))
    }

    /** Filter or assertion criteria for have name starting with. */
    fun haveNameStartingWith(prefixes: List<String>) {
        assertions.add { func, violations ->
            if (prefixes.none { func.name.startsWith(it) }) {
                violations.add(
                    getMessage(
                        "function.scope.haveNameStartingWith",
                        prefixes.joinToString { "'$it'" },
                        func.name,
                    ),
                )
            }
        }
    }

    /** Filter or assertion criteria for have name starting with. */
    fun haveNameStartingWith(vararg prefixes: String) {
        haveNameStartingWith(prefixes.asList())
    }

    /** Filter or assertion criteria for have name ending with. */
    fun haveNameEndingWith(suffix: String) {
        haveNameEndingWith(listOf(suffix))
    }

    /** Filter or assertion criteria for have name ending with. */
    fun haveNameEndingWith(suffixes: List<String>) {
        assertions.add { func, violations ->
            if (suffixes.none { func.name.endsWith(it) }) {
                violations.add(
                    getMessage(
                        "function.scope.haveNameEndingWith",
                        suffixes.joinToString { "'$it'" },
                        func.name,
                    ),
                )
            }
        }
    }

    /** Filter or assertion criteria for have name ending with. */
    fun haveNameEndingWith(vararg suffixes: String) {
        haveNameEndingWith(suffixes.asList())
    }

    /** Filter or assertion criteria for be public. */
    fun bePublic() {
        assertions.add { func, violations ->
            if (func.visibility != Visibility.PUBLIC) {
                violations.add(getMessage("function.scope.bePublic", func.visibility.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for be internal. */
    fun beInternal() {
        assertions.add { func, violations ->
            if (func.visibility != Visibility.INTERNAL) {
                violations.add(getMessage("function.scope.beInternal", func.visibility.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for be private. */
    fun bePrivate() {
        assertions.add { func, violations ->
            if (func.visibility != Visibility.PRIVATE) {
                violations.add(getMessage("function.scope.bePrivate", func.visibility.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for be protected. */
    fun beProtected() {
        assertions.add { func, violations ->
            if (func.visibility != Visibility.PROTECTED) {
                violations.add(getMessage("function.scope.beProtected", func.visibility.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for be suspend. */
    fun beSuspend() {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(Modifier.SUSPEND)) {
                violations.add(getMessage("function.scope.beSuspend"))
            }
        }
    }

    /** Filter or assertion criteria for be inline. */
    fun beInline() {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(Modifier.INLINE)) {
                violations.add(getMessage("function.scope.beInline"))
            }
        }
    }

    /** Filter or assertion criteria for be open. */
    fun beOpen() {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(Modifier.OPEN)) {
                violations.add(getMessage("function.scope.beOpen"))
            }
        }
    }

    /** Filter or assertion criteria for be abstract. */
    fun beAbstract() {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(Modifier.ABSTRACT)) {
                violations.add(getMessage("function.scope.beAbstract"))
            }
        }
    }

    /** Filter or assertion criteria for have modifier. */
    fun haveModifier(modifier: Modifier) {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(modifier)) {
                violations.add(getMessage("function.scope.haveModifier", modifier.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for have return type. */
    fun haveReturnType(typeFqName: String) {
        haveReturnType(listOf(typeFqName))
    }

    /** Filter or assertion criteria for have return type. */
    fun haveReturnType(typeFqNames: List<String>) {
        assertions.add { func, violations ->
            if (typeFqNames.none { func.returnType == it }) {
                violations.add(
                    getMessage(
                        "function.scope.haveReturnType",
                        typeFqNames.joinToString { "'$it'" },
                        func.returnType,
                    ),
                )
            }
        }
    }

    /** Filter or assertion criteria for have return type. */
    fun haveReturnType(vararg typeFqNames: String) {
        haveReturnType(typeFqNames.asList())
    }

    /** Filter or assertion criteria for have annotation of. */
    fun haveAnnotationOf(annotationName: String) {
        haveAnnotationOf(listOf(annotationName))
    }

    /** Filter or assertion criteria for have annotation of. */
    fun haveAnnotationOf(annotationNames: List<String>) {
        assertions.add { func, violations ->
            /** Filter or assertion criteria for present. */
            val present = func.annotations.map { it.name }.toSet() + func.annotations.map { it.fqName }.toSet()
            if (annotationNames.none { it in present }) {
                violations.add(
                    getMessage("function.scope.beAnnotatedWithAny", annotationNames.joinToString { "@$it" }),
                )
            }
        }
    }

    /** Filter or assertion criteria for have annotation of. */
    fun haveAnnotationOf(vararg annotationNames: String) {
        haveAnnotationOf(annotationNames.asList())
    }

    /** Filter or assertion criteria for be extension. */
    fun beExtension() {
        assertions.add { func, violations ->
            if (!func.isExtension) {
                violations.add(getMessage("function.scope.beExtension"))
            }
        }
    }

    /** Filter or assertion criteria for be documented with k doc. */
    fun beDocumentedWithKDoc() {
        assertions.add { func, violations ->
            if (func.kdocText.isNullOrBlank()) {
                violations.add(getMessage("function.scope.beDocumented"))
            }
        }
    }

    /**
     * Asserts that member functions are annotated with all of the specified annotations.
     */
    fun haveAllAnnotationsOf(names: List<String>) {
        assertions.add { func, violations ->
            /** Filter or assertion criteria for present. */
            val present = func.annotations.map { it.name }.toSet() + func.annotations.map { it.fqName }.toSet()
            if (!names.all { it in present }) {
                violations.add(getMessage("function.scope.haveAllAnnotations", names.joinToString()))
            }
        }
    }

    /**
     * Asserts that member functions are annotated with all of the specified annotations.
     */
    fun haveAllAnnotationsOf(vararg names: String) {
        haveAllAnnotationsOf(names.asList())
    }

    /**
     * Asserts that member functions are annotated with any of the specified annotations.
     */
    fun haveAnyAnnotationOf(names: List<String>) {
        assertions.add { func, violations ->
            /** Filter or assertion criteria for present. */
            val present = func.annotations.map { it.name }.toSet() + func.annotations.map { it.fqName }.toSet()
            if (names.none { it in present }) {
                violations.add(getMessage("function.scope.haveAtLeastOneAnnotationOf", names.joinToString()))
            }
        }
    }

    /**
     * Asserts that member functions are annotated with any of the specified annotations.
     */
    fun haveAnyAnnotationOf(vararg names: String) {
        haveAnyAnnotationOf(names.asList())
    }

    /**
     * Asserts that member functions have all of the specified modifiers.
     */
    fun haveAllModifiers(modifiers: List<Modifier>) {
        assertions.add { func, violations ->
            if (!modifiers.all { func.modifiers.contains(it) }) {
                violations.add(
                    getMessage(
                        "function.scope.haveAllModifiers",
                        modifiers.joinToString { it.name.lowercase() },
                    ),
                )
            }
        }
    }

    /**
     * Asserts that member functions have all of the specified modifiers.
     */
    fun haveAllModifiers(vararg modifiers: Modifier) {
        haveAllModifiers(modifiers.asList())
    }

    /**
     * Asserts that member functions have any of the specified modifiers.
     */
    fun haveAnyModifier(modifiers: List<Modifier>) {
        assertions.add { func, violations ->
            if (modifiers.none { func.modifiers.contains(it) }) {
                violations.add(
                    getMessage(
                        "function.scope.haveAnyModifier",
                        modifiers.joinToString { it.name.lowercase() },
                    ),
                )
            }
        }
    }

    /**
     * Asserts that member functions have any of the specified modifiers.
     */
    fun haveAnyModifier(vararg modifiers: Modifier) {
        haveAnyModifier(modifiers.asList())
    }

    /**
     * Asserts that member functions have any of the specified visibilities.
     */
    fun haveAnyVisibility(visibilities: List<Visibility>) {
        assertions.add { func, violations ->
            if (!visibilities.contains(func.visibility)) {
                violations.add(
                    getMessage(
                        "function.scope.haveVisibility",
                        visibilities.joinToString { it.name.lowercase() },
                    ),
                )
            }
        }
    }

    /**
     * Asserts that member functions have any of the specified visibilities.
     */
    fun haveAnyVisibility(vararg visibilities: Visibility) {
        haveAnyVisibility(visibilities.asList())
    }
}
