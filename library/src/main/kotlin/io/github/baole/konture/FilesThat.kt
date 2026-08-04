/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import io.github.baole.konture.impl.PatternMatchers

/**
 * Fluent API for defining filtering conditions on Kotlin source files.
 */
@KontureDsl
class FilesThat internal constructor(
    private val builder: FilesRuleBuilder,
) {
    /**
     * Restricts the rules to files declared in packages matching the specified pattern.
     * Supports `..` segment wildcards.
     *
     * @param packagePattern Package matching pattern.
     */
    infix fun resideInAPackage(packagePattern: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesPackage(packagePattern, it.declaration.packageName) }
        return builder
    }

    /**
     * Restricts the rules to files declared in packages matching any of the specified patterns.
     *
     * @param packagePatterns List of package matching patterns.
     */
    infix fun resideInAPackage(packagePatterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            packagePatterns.any { PatternMatchers.matchesPackage(it, context.declaration.packageName) }
        }
        return builder
    }

    /**
     * Restricts the rules to files declared in packages matching any of the specified patterns.
     *
     * @param packagePatterns Package matching patterns.
     */
    fun resideInAPackage(vararg packagePatterns: String): FilesRuleBuilder = resideInAPackage(packagePatterns.toList())

    /**
     * Restricts the rules to files declared in packages satisfying [predicate].
     *
     * @param predicate Predicate checking package name.
     */
    infix fun resideInAPackage(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.packageName) }
        return builder
    }

    /**
     * Restricts the rules to files whose filename ends with the specified suffix.
     *
     * @param suffix Filename suffix.
     */
    infix fun haveNameEndingWith(suffix: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name.endsWith(suffix) }
        return builder
    }

    /**
     * Restricts the rules to files whose filename ends with any of the specified suffixes.
     *
     * @param suffixes List of filename suffixes.
     */
    infix fun haveNameEndingWith(suffixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            suffixes.any { context.declaration.name.endsWith(it) }
        }
        return builder
    }

    /**
     * Restricts the rules to files whose filename ends with any of the specified suffixes.
     *
     * @param suffixes Filename suffixes.
     */
    fun haveNameEndingWith(vararg suffixes: String): FilesRuleBuilder = haveNameEndingWith(suffixes.toList())

    /**
     * Restricts the rules to files whose filename starts with the specified prefix.
     *
     * @param prefix Filename prefix.
     */
    infix fun haveNameStartingWith(prefix: String): FilesRuleBuilder {
        builder.setThat { it.declaration.name.startsWith(prefix) }
        return builder
    }

    /**
     * Restricts the rules to files whose filename starts with any of the specified prefixes.
     *
     * @param prefixes List of filename prefixes.
     */
    infix fun haveNameStartingWith(prefixes: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            prefixes.any { context.declaration.name.startsWith(it) }
        }
        return builder
    }

    /**
     * Restricts the rules to files whose filename starts with any of the specified prefixes.
     *
     * @param prefixes Filename prefixes.
     */
    fun haveNameStartingWith(vararg prefixes: String): FilesRuleBuilder = haveNameStartingWith(prefixes.toList())

    /**
     * Restricts the rules to files whose filename matches the specified glob pattern.
     *
     * @param pattern Glob pattern.
     */
    infix fun haveNameMatching(pattern: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesSimpleGlob(pattern, it.declaration.name) }
        return builder
    }

    /**
     * Restricts the rules to files whose filename matches any of the specified glob patterns.
     *
     * @param patterns List of glob patterns.
     */
    infix fun haveNameMatching(patterns: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            patterns.any { PatternMatchers.matchesSimpleGlob(it, context.declaration.name) }
        }
        return builder
    }

    /**
     * Restricts the rules to files whose filename matches any of the specified glob patterns.
     *
     * @param patterns Glob patterns.
     */
    fun haveNameMatching(vararg patterns: String): FilesRuleBuilder = haveNameMatching(patterns.toList())

    /**
     * Restricts the rules to files residing in modules matching the specified glob pattern.
     *
     * @param modulePath Module path glob pattern (e.g. `:core`, `:feature-*`).
     */
    infix fun resideInAModule(modulePath: String): FilesRuleBuilder {
        builder.setThat { PatternMatchers.matchesModuleGlob(modulePath, it.modulePath) }
        return builder
    }

    /**
     * Restricts the rules to files residing in modules matching any of the specified glob patterns.
     *
     * @param modulePaths List of module path glob patterns.
     */
    infix fun resideInAModule(modulePaths: List<String>): FilesRuleBuilder {
        builder.setThat { context ->
            modulePaths.any { PatternMatchers.matchesModuleGlob(it, context.modulePath) }
        }
        return builder
    }

    /**
     * Restricts the rules to files residing in modules matching any of the specified glob patterns.
     *
     * @param modulePaths Module path glob patterns.
     */
    fun resideInAModule(vararg modulePaths: String): FilesRuleBuilder = resideInAModule(modulePaths.toList())

    /**
     * Restricts the rules to files satisfying an arbitrary custom predicate logic.
     *
     * @param predicate Predicate checking [FileDeclarationContext].
     */
    infix fun satisfy(predicate: (FileDeclarationContext) -> Boolean): FilesRuleBuilder {
        builder.setThat(predicate)
        return builder
    }

    /**
     * Restricts the rules to files annotated with the specified file-level annotation.
     *
     * @param annotationName Annotation simple name or FQN.
     */
    infix fun haveAnnotationOf(annotationName: String): FilesRuleBuilder {
        builder.setThat { it.declaration.hasAnnotation(annotationName) }
        return builder
    }

    /**
     * Restricts the rules to files annotated with any of the specified file-level annotations.
     *
     * @param annotationNames List of annotation simple names or FQNs.
     */
    infix fun haveAnnotationOf(annotationNames: List<String>): FilesRuleBuilder {
        builder.setThat { file -> annotationNames.any { file.declaration.hasAnnotation(it) } }
        return builder
    }

    /**
     * Restricts the rules to files annotated with any of the specified file-level annotations.
     *
     * @param annotationNames Annotation simple names or FQNs.
     */
    fun haveAnnotationOf(vararg annotationNames: String): FilesRuleBuilder = haveAnnotationOf(annotationNames.asList())

    /**
     * Restricts the rules to files annotated with all of the specified file-level annotations.
     *
     * @param names List of annotation simple names or FQNs.
     */
    infix fun haveAllAnnotationsOf(names: List<String>): FilesRuleBuilder {
        builder.setThat { it.declaration.hasAllAnnotations(names) }
        return builder
    }

    /**
     * Restricts the rules to files annotated with all of the specified file-level annotations.
     *
     * @param names Annotation simple names or FQNs.
     */
    fun haveAllAnnotationsOf(vararg names: String): FilesRuleBuilder = haveAllAnnotationsOf(names.asList())

    /**
     * Restricts the rules to files annotated with any of the specified file-level annotations.
     *
     * @param names List of annotation simple names or FQNs.
     */
    infix fun haveAnyAnnotationOf(names: List<String>): FilesRuleBuilder {
        builder.setThat { it.declaration.hasAnyAnnotation(names) }
        return builder
    }

    /**
     * Restricts the rules to files annotated with any of the specified file-level annotations.
     *
     * @param names Annotation simple names or FQNs.
     */
    fun haveAnyAnnotationOf(vararg names: String): FilesRuleBuilder = haveAnyAnnotationOf(names.asList())

    /**
     * Restricts the rules to files whose filename satisfies [predicate].
     *
     * @param predicate Predicate checking filename.
     */
    infix fun haveName(predicate: (String) -> Boolean): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }

    /**
     * Restricts the rules to files whose filename satisfies [predicate].
     *
     * @param description Descriptive label for violation messages.
     * @param predicate Predicate checking filename.
     */
    @Suppress("UnusedParameter")
    fun haveName(
        description: String,
        predicate: (String) -> Boolean,
    ): FilesRuleBuilder {
        builder.setThat { predicate(it.declaration.name) }
        return builder
    }
}

private fun FileDeclaration.hasAnnotation(annotationName: String): Boolean =
    annotations.any { it.name == annotationName || it.fqName == annotationName }

private fun FileDeclaration.hasAllAnnotations(names: List<String>): Boolean = names.all { name -> hasAnnotation(name) }

private fun FileDeclaration.hasAnyAnnotation(names: List<String>): Boolean = names.any { name -> hasAnnotation(name) }
