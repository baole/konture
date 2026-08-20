/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.i18n.getMessage

/**
 * Programmatic suppression descriptor.
 */
public sealed class ProgrammaticSuppression(
    public val reason: String,
) {
    /** Programmatic suppression by class fully qualified name or glob pattern. */
    public class ClassFqName(
        public val pattern: String,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by class predicate. */
    public class ClassPredicate(
        public val predicate: (ClassDeclaration) -> Boolean,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by file path or glob pattern. */
    public class FilePath(
        public val pattern: String,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by file predicate. */
    public class FilePredicate(
        public val predicate: (FileDeclaration) -> Boolean,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by function name or pattern. */
    public class FunctionName(
        public val nameOrFqName: String,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by function predicate. */
    public class FunctionPredicate(
        public val predicate: (FunctionDeclarationContext) -> Boolean,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by property name or pattern. */
    public class PropertyName(
        public val nameOrFqName: String,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by property predicate. */
    public class PropertyPredicate(
        public val predicate: (PropertyDeclarationContext) -> Boolean,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by Gradle module path. */
    public class ModulePath(
        public val modulePath: String,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by module predicate. */
    public class ModulePredicate(
        public val predicate: (Module) -> Boolean,
        reason: String,
    ) : ProgrammaticSuppression(reason)

    /** Programmatic suppression by slice key pattern. */
    public class SliceKey(
        public val sliceKey: String,
        reason: String,
    ) : ProgrammaticSuppression(reason)
}

/**
 * DSL builder for configuring programmatic violation suppressions with mandatory audit reasons.
 */
@KontureDsl
public class RuleSuppressionBuilder {
    internal val suppressions = mutableListOf<ProgrammaticSuppression>()

    /**
     * Suppress violations for classes matching the fully-qualified name or glob pattern.
     *
     * @param patternOrFqName Fully qualified class name or glob pattern (e.g. `com.example.LegacyClass` or `com.example.legacy.*`).
     * @param reason Mandatory human-readable explanation for the suppression.
     */
    public fun classFqName(
        patternOrFqName: String,
        reason: String,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.ClassFqName(patternOrFqName, reason))
    }

    /**
     * Suppress violations for classes matching a custom predicate.
     *
     * @param reason Mandatory human-readable explanation for the suppression.
     * @param predicate Predicate filtering matching class declarations.
     */
    public fun classes(
        reason: String,
        predicate: (ClassDeclaration) -> Boolean,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.ClassPredicate(predicate, reason))
    }

    /**
     * Suppress violations for classes matching a custom predicate.
     */
    public fun classes(
        predicate: (ClassDeclaration) -> Boolean,
        reason: String,
    ) {
        classes(reason, predicate)
    }

    /**
     * Suppress violations for files matching the given file path or glob pattern.
     *
     * @param pathPattern File path, filename, or glob pattern.
     * @param reason Mandatory human-readable explanation for the suppression.
     */
    public fun file(
        pathPattern: String,
        reason: String,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.FilePath(pathPattern, reason))
    }

    /**
     * Suppress violations for files matching a custom predicate.
     */
    public fun files(
        reason: String,
        predicate: (FileDeclaration) -> Boolean,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.FilePredicate(predicate, reason))
    }

    /**
     * Suppress violations for files matching a custom predicate.
     */
    public fun files(
        predicate: (FileDeclaration) -> Boolean,
        reason: String,
    ) {
        files(reason, predicate)
    }

    /**
     * Suppress violations for functions matching the given function name or qualified name.
     */
    public fun function(
        nameOrFqName: String,
        reason: String,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.FunctionName(nameOrFqName, reason))
    }

    /**
     * Suppress violations for functions matching a custom predicate.
     */
    public fun functions(
        reason: String,
        predicate: (FunctionDeclarationContext) -> Boolean,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.FunctionPredicate(predicate, reason))
    }

    /**
     * Suppress violations for functions matching a custom predicate.
     */
    public fun functions(
        predicate: (FunctionDeclarationContext) -> Boolean,
        reason: String,
    ) {
        functions(reason, predicate)
    }

    /**
     * Suppress violations for properties matching the given property name or qualified name.
     */
    public fun property(
        nameOrFqName: String,
        reason: String,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.PropertyName(nameOrFqName, reason))
    }

    /**
     * Suppress violations for properties matching a custom predicate.
     */
    public fun properties(
        reason: String,
        predicate: (PropertyDeclarationContext) -> Boolean,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.PropertyPredicate(predicate, reason))
    }

    /**
     * Suppress violations for properties matching a custom predicate.
     */
    public fun properties(
        predicate: (PropertyDeclarationContext) -> Boolean,
        reason: String,
    ) {
        properties(reason, predicate)
    }

    /**
     * Suppress violations for modules matching the given module path pattern (e.g. `:features:*`).
     */
    public fun module(
        modulePath: String,
        reason: String,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.ModulePath(modulePath, reason))
    }

    /**
     * Suppress violations for modules matching a custom predicate.
     */
    public fun modules(
        reason: String,
        predicate: (Module) -> Boolean,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.ModulePredicate(predicate, reason))
    }

    /**
     * Suppress violations for modules matching a custom predicate.
     */
    public fun modules(
        predicate: (Module) -> Boolean,
        reason: String,
    ) {
        modules(reason, predicate)
    }

    /**
     * Suppress violations for a specific architecture slice.
     */
    public fun slice(
        sliceKey: String,
        reason: String,
    ) {
        require(reason.isNotBlank()) { getMessage("suppression.reason.blank") }
        suppressions.add(ProgrammaticSuppression.SliceKey(sliceKey, reason))
    }
}
