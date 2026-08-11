/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage
import io.github.baole.konture.impl.PatternMatchers

/** Assertion scope DSL for configuring rule expectations on Kotlin properties. */
@KontureDsl
public class PropertyAssertionScope internal constructor() {
    /** Filter or assertion criteria for assertions. */
    public val assertions: MutableList<(PropertyDeclaration, MutableList<String>) -> Unit> =
        mutableListOf()

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(pattern: String) {
        haveNameMatching(listOf(pattern))
    }

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(patterns: List<String>) {
        assertions.add { prop, violations ->
            if (patterns.none { PatternMatchers.matchesSimpleGlob(it, prop.name) }) {
                violations.add(
                    getMessage(
                        "property.scope.haveNameMatching",
                        patterns.joinToString { "'$it'" },
                        prop.name,
                    ),
                )
            }
        }
    }

    /** Filter or assertion criteria for have name matching. */
    public fun haveNameMatching(vararg patterns: String) {
        haveNameMatching(patterns.asList())
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(prefix: String) {
        haveNameStartingWith(listOf(prefix))
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(prefixes: List<String>) {
        assertions.add { prop, violations ->
            if (prefixes.none { prop.name.startsWith(it) }) {
                violations.add(
                    getMessage(
                        "property.scope.haveNameStartingWith",
                        prefixes.joinToString { "'$it'" },
                        prop.name,
                    ),
                )
            }
        }
    }

    /** Filter or assertion criteria for have name starting with. */
    public fun haveNameStartingWith(vararg prefixes: String) {
        haveNameStartingWith(prefixes.asList())
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(suffix: String) {
        haveNameEndingWith(listOf(suffix))
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(suffixes: List<String>) {
        assertions.add { prop, violations ->
            if (suffixes.none { prop.name.endsWith(it) }) {
                violations.add(
                    getMessage(
                        "property.scope.haveNameEndingWith",
                        suffixes.joinToString { "'$it'" },
                        prop.name,
                    ),
                )
            }
        }
    }

    /** Filter or assertion criteria for have name ending with. */
    public fun haveNameEndingWith(vararg suffixes: String) {
        haveNameEndingWith(suffixes.asList())
    }

    /** Filter or assertion criteria for be public. */
    public fun bePublic() {
        assertions.add { prop, violations ->
            if (prop.visibility != Visibility.PUBLIC) {
                violations.add(getMessage("property.scope.bePublic", prop.visibility.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for be internal. */
    public fun beInternal() {
        assertions.add { prop, violations ->
            if (prop.visibility != Visibility.INTERNAL) {
                violations.add(getMessage("property.scope.beInternal", prop.visibility.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for be private. */
    public fun bePrivate() {
        assertions.add { prop, violations ->
            if (prop.visibility != Visibility.PRIVATE) {
                violations.add(getMessage("property.scope.bePrivate", prop.visibility.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for be protected. */
    public fun beProtected() {
        assertions.add { prop, violations ->
            if (prop.visibility != Visibility.PROTECTED) {
                violations.add(getMessage("property.scope.beProtected", prop.visibility.name.lowercase()))
            }
        }
    }

    /** Filter or assertion criteria for be val. */
    public fun beVal() {
        assertions.add { prop, violations ->
            if (!prop.isVal) {
                violations.add(getMessage("property.scope.beVal"))
            }
        }
    }

    /** Filter or assertion criteria for be var. */
    public fun beVar() {
        assertions.add { prop, violations ->
            if (prop.isVal) {
                violations.add(getMessage("property.scope.beVar"))
            }
        }
    }

    /** Filter or assertion criteria for have type. */
    public fun haveType(typeFqName: String) {
        haveType(listOf(typeFqName))
    }

    /** Filter or assertion criteria for have type. */
    public fun haveType(typeFqNames: List<String>) {
        assertions.add { prop, violations ->
            if (typeFqNames.none { prop.type == it }) {
                violations.add(
                    getMessage(
                        "property.scope.haveType",
                        typeFqNames.joinToString { "'$it'" },
                        prop.type,
                    ),
                )
            }
        }
    }

    /** Filter or assertion criteria for have type. */
    public fun haveType(vararg typeFqNames: String) {
        haveType(typeFqNames.asList())
    }

    /** Filter or assertion criteria for have annotation of. */
    public fun haveAnnotationOf(annotationName: String) {
        haveAnnotationOf(listOf(annotationName))
    }

    /** Filter or assertion criteria for have annotation of. */
    public fun haveAnnotationOf(annotationNames: List<String>) {
        assertions.add { prop, violations ->
            /** Filter or assertion criteria for present. */
            val present = prop.annotations.map { it.name }.toSet() + prop.annotations.map { it.fqName }.toSet()
            if (annotationNames.none { it in present }) {
                violations.add(
                    getMessage("property.scope.beAnnotatedWithAny", annotationNames.joinToString { "@$it" }),
                )
            }
        }
    }

    /** Filter or assertion criteria for have annotation of. */
    public fun haveAnnotationOf(vararg annotationNames: String) {
        haveAnnotationOf(annotationNames.asList())
    }

    /** Filter or assertion criteria for be documented with k doc. */
    public fun beDocumentedWithKDoc() {
        assertions.add { prop, violations ->
            if (prop.kdocText.isNullOrBlank()) {
                violations.add(getMessage("property.scope.beDocumented"))
            }
        }
    }

    /**
     * Asserts that member properties are annotated with all of the specified annotations.
     */
    public fun haveAllAnnotationsOf(names: List<String>) {
        assertions.add { prop, violations ->
            /** Filter or assertion criteria for present. */
            val present = prop.annotations.map { it.name }.toSet() + prop.annotations.map { it.fqName }.toSet()
            if (!names.all { it in present }) {
                violations.add(getMessage("property.scope.haveAllAnnotations", names.joinToString()))
            }
        }
    }

    /**
     * Asserts that member properties are annotated with all of the specified annotations.
     */
    public fun haveAllAnnotationsOf(vararg names: String) {
        haveAllAnnotationsOf(names.asList())
    }

    /**
     * Asserts that member properties are annotated with any of the specified annotations.
     */
    public fun haveAnyAnnotationOf(names: List<String>) {
        assertions.add { prop, violations ->
            /** Filter or assertion criteria for present. */
            val present = prop.annotations.map { it.name }.toSet() + prop.annotations.map { it.fqName }.toSet()
            if (names.none { it in present }) {
                violations.add(getMessage("property.scope.haveAtLeastOneAnnotationOf", names.joinToString()))
            }
        }
    }

    /**
     * Asserts that member properties are annotated with any of the specified annotations.
     */
    public fun haveAnyAnnotationOf(vararg names: String) {
        haveAnyAnnotationOf(names.asList())
    }

    /**
     * Asserts that member properties have all of the specified modifiers.
     */
    public fun haveAllModifiers(modifiers: List<Modifier>) {
        assertions.add { prop, violations ->
            if (!modifiers.all { prop.modifiers.contains(it) }) {
                violations.add(
                    getMessage(
                        "property.scope.haveAllModifiers",
                        modifiers.joinToString { it.name.lowercase() },
                    ),
                )
            }
        }
    }

    /**
     * Asserts that member properties have all of the specified modifiers.
     */
    public fun haveAllModifiers(vararg modifiers: Modifier) {
        haveAllModifiers(modifiers.asList())
    }

    /**
     * Asserts that member properties have any of the specified modifiers.
     */
    public fun haveAnyModifier(modifiers: List<Modifier>) {
        assertions.add { prop, violations ->
            if (modifiers.none { prop.modifiers.contains(it) }) {
                violations.add(
                    getMessage(
                        "property.scope.haveAnyModifier",
                        modifiers.joinToString { it.name.lowercase() },
                    ),
                )
            }
        }
    }

    /**
     * Asserts that member properties have any of the specified modifiers.
     */
    public fun haveAnyModifier(vararg modifiers: Modifier) {
        haveAnyModifier(modifiers.asList())
    }

    /**
     * Asserts that member properties have any of the specified visibilities.
     */
    public fun haveAnyVisibility(visibilities: List<Visibility>) {
        assertions.add { prop, violations ->
            if (!visibilities.contains(prop.visibility)) {
                violations.add(
                    getMessage(
                        "property.scope.haveVisibility",
                        visibilities.joinToString { it.name.lowercase() },
                    ),
                )
            }
        }
    }

    /**
     * Asserts that member properties have any of the specified visibilities.
     */
    public fun haveAnyVisibility(vararg visibilities: Visibility) {
        haveAnyVisibility(visibilities.asList())
    }
}
