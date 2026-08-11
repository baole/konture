/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

@KontureDsl
class FunctionAssertionScope internal constructor() {
    val assertions = mutableListOf<(FunctionDeclaration, MutableList<String>) -> Unit>()

    fun haveNameMatching(pattern: String) {
        haveNameMatching(listOf(pattern))
    }

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

    fun haveNameMatching(vararg patterns: String) {
        haveNameMatching(patterns.asList())
    }

    fun haveNameStartingWith(prefix: String) {
        haveNameStartingWith(listOf(prefix))
    }

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

    fun haveNameStartingWith(vararg prefixes: String) {
        haveNameStartingWith(prefixes.asList())
    }

    fun haveNameEndingWith(suffix: String) {
        haveNameEndingWith(listOf(suffix))
    }

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

    fun haveNameEndingWith(vararg suffixes: String) {
        haveNameEndingWith(suffixes.asList())
    }

    fun bePublic() {
        assertions.add { func, violations ->
            if (func.visibility != Visibility.PUBLIC) {
                violations.add(getMessage("function.scope.bePublic", func.visibility.name.lowercase()))
            }
        }
    }

    fun beInternal() {
        assertions.add { func, violations ->
            if (func.visibility != Visibility.INTERNAL) {
                violations.add(getMessage("function.scope.beInternal", func.visibility.name.lowercase()))
            }
        }
    }

    fun bePrivate() {
        assertions.add { func, violations ->
            if (func.visibility != Visibility.PRIVATE) {
                violations.add(getMessage("function.scope.bePrivate", func.visibility.name.lowercase()))
            }
        }
    }

    fun beProtected() {
        assertions.add { func, violations ->
            if (func.visibility != Visibility.PROTECTED) {
                violations.add(getMessage("function.scope.beProtected", func.visibility.name.lowercase()))
            }
        }
    }

    fun beSuspend() {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(Modifier.SUSPEND)) {
                violations.add(getMessage("function.scope.beSuspend"))
            }
        }
    }

    fun beInline() {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(Modifier.INLINE)) {
                violations.add(getMessage("function.scope.beInline"))
            }
        }
    }

    fun beOpen() {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(Modifier.OPEN)) {
                violations.add(getMessage("function.scope.beOpen"))
            }
        }
    }

    fun beAbstract() {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(Modifier.ABSTRACT)) {
                violations.add(getMessage("function.scope.beAbstract"))
            }
        }
    }

    fun haveModifier(modifier: Modifier) {
        assertions.add { func, violations ->
            if (!func.modifiers.contains(modifier)) {
                violations.add(getMessage("function.scope.haveModifier", modifier.name.lowercase()))
            }
        }
    }

    fun haveReturnType(typeFqName: String) {
        haveReturnType(listOf(typeFqName))
    }

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

    fun haveReturnType(vararg typeFqNames: String) {
        haveReturnType(typeFqNames.asList())
    }

    fun haveAnnotationOf(annotationName: String) {
        haveAnnotationOf(listOf(annotationName))
    }

    fun haveAnnotationOf(annotationNames: List<String>) {
        assertions.add { func, violations ->
            val present = func.annotations.map { it.name }.toSet() + func.annotations.map { it.fqName }.toSet()
            if (annotationNames.none { it in present }) {
                violations.add(
                    getMessage("function.scope.beAnnotatedWithAny", annotationNames.joinToString { "@$it" }),
                )
            }
        }
    }

    fun haveAnnotationOf(vararg annotationNames: String) {
        haveAnnotationOf(annotationNames.asList())
    }

    fun beExtension() {
        assertions.add { func, violations ->
            if (!func.isExtension) {
                violations.add(getMessage("function.scope.beExtension"))
            }
        }
    }

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
