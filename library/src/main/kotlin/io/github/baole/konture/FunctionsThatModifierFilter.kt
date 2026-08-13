/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

/**
 * Trait interface for modifiers, annotations, visibility and modifier filtering on functions.
 */
@Suppress("ComplexInterface")
public interface FunctionsThatModifierFilter : FunctionsThatScope {
    /** Filters functions that have [Visibility.PUBLIC] visibility. */
    public fun arePublic(): FunctionsRuleBuilder = haveVisibility(Visibility.PUBLIC)

    /** Filters functions that have [Visibility.PUBLIC] visibility. */
    public fun bePublic(): FunctionsRuleBuilder = arePublic()

    /** Filters functions that have [Visibility.INTERNAL] visibility. */
    public fun areInternal(): FunctionsRuleBuilder = haveVisibility(Visibility.INTERNAL)

    /** Filters functions that have [Visibility.INTERNAL] visibility. */
    public fun beInternal(): FunctionsRuleBuilder = areInternal()

    /** Filters functions that have [Visibility.PRIVATE] visibility. */
    public fun arePrivate(): FunctionsRuleBuilder = haveVisibility(Visibility.PRIVATE)

    /** Filters functions that have [Visibility.PRIVATE] visibility. */
    public fun bePrivate(): FunctionsRuleBuilder = arePrivate()

    /** Filters functions that have [Visibility.PROTECTED] visibility. */
    public fun areProtected(): FunctionsRuleBuilder = haveVisibility(Visibility.PROTECTED)

    /** Filters functions that have [Visibility.PROTECTED] visibility. */
    public fun beProtected(): FunctionsRuleBuilder = areProtected()

    /** Filters functions that do not have [Visibility.PUBLIC] visibility. */
    public fun notBePublic(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PUBLIC }
        return builder
    }

    /** Filters functions that do not have [Visibility.INTERNAL] visibility. */
    public fun notBeInternal(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.INTERNAL }
        return builder
    }

    /** Filters functions that do not have [Visibility.PRIVATE] visibility. */
    public fun notBePrivate(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PRIVATE }
        return builder
    }

    /** Filters functions that do not have [Visibility.PROTECTED] visibility. */
    public fun notBeProtected(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility != Visibility.PROTECTED }
        return builder
    }

    /** Filters functions annotated with annotation [annotationName]. */
    public infix fun haveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setThat { it.hasAnnotation(annotationName) }
        return builder
    }

    /** Filters functions annotated with annotation [annotation]. */
    public infix fun haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        haveAnnotationOf(annotation.kontureQualifiedName())

    /** Filters functions annotated with annotation [annotationName]. */
    public infix fun areAnnotatedWith(annotationName: String): FunctionsRuleBuilder = haveAnnotationOf(annotationName)

    /** Filters functions annotated with annotation [annotation]. */
    public infix fun areAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        haveAnnotationOf(annotation)

    /** Filters functions annotated with any of [annotationNames]. */
    public infix fun haveAnnotationOf(annotationNames: List<String>): FunctionsRuleBuilder {
        builder.setThat { func -> annotationNames.any { func.hasAnnotation(it) } }
        return builder
    }

    /** Filters functions annotated with any of [annotationNames]. */
    public fun haveAnnotationOf(vararg annotationNames: String): FunctionsRuleBuilder =
        haveAnnotationOf(annotationNames.asList())

    /** Filters functions not annotated with annotation [annotationName]. */
    public infix fun notHaveAnnotationOf(annotationName: String): FunctionsRuleBuilder {
        builder.setThat { !it.hasAnnotation(annotationName) }
        return builder
    }

    /** Filters functions not annotated with annotation [annotation]. */
    public infix fun notHaveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        notHaveAnnotationOf(annotation.kontureQualifiedName())

    /** Filters functions not annotated with annotation [annotationName]. */
    public infix fun notBeAnnotatedWith(annotationName: String): FunctionsRuleBuilder =
        notHaveAnnotationOf(annotationName)

    /** Filters functions not annotated with annotation [annotation]. */
    public infix fun notBeAnnotatedWith(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
        notHaveAnnotationOf(annotation)

    /** Filters functions annotated with all annotations in [names]. */
    public infix fun haveAllAnnotationsOf(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { it.hasAllAnnotations(names) }
        return builder
    }

    /** Filters functions annotated with all annotations in [names]. */
    public fun haveAllAnnotationsOf(vararg names: String): FunctionsRuleBuilder = haveAllAnnotationsOf(names.asList())

    /** Filters functions annotated with any annotation in [names]. */
    public infix fun haveAnyAnnotationOf(names: List<String>): FunctionsRuleBuilder {
        builder.setThat { it.hasAnyAnnotation(names) }
        return builder
    }

    /** Filters functions annotated with any annotation in [names]. */
    public fun haveAnyAnnotationOf(vararg names: String): FunctionsRuleBuilder = haveAnyAnnotationOf(names.asList())

    /** Filters functions declared with [Modifier.OPEN]. */
    public fun areOpen(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OPEN) }
        return builder
    }

    /** Filters functions declared with [Modifier.ABSTRACT]. */
    public fun areAbstract(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.ABSTRACT) }
        return builder
    }

    /** Filters functions declared with [Modifier.OVERRIDE]. */
    public fun areOverride(): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(Modifier.OVERRIDE) }
        return builder
    }

    /** Filters functions declared with modifier [modifier]. */
    public infix fun haveModifier(modifier: Modifier): FunctionsRuleBuilder {
        builder.setThat { it.declaration.modifiers.contains(modifier) }
        return builder
    }

    /** Filters functions declared with all modifiers in [modifiers]. */
    public infix fun haveAllModifiers(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setThat { func -> modifiers.all { func.declaration.modifiers.contains(it) } }
        return builder
    }

    /** Filters functions declared with all modifiers in [modifiers]. */
    public fun haveAllModifiers(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAllModifiers(modifiers.asList())

    /** Filters functions declared with any modifier in [modifiers]. */
    public infix fun haveAnyModifier(modifiers: List<Modifier>): FunctionsRuleBuilder {
        builder.setThat { func -> modifiers.any { func.declaration.modifiers.contains(it) } }
        return builder
    }

    /** Filters functions declared with any modifier in [modifiers]. */
    public fun haveAnyModifier(vararg modifiers: Modifier): FunctionsRuleBuilder = haveAnyModifier(modifiers.asList())

    /** Filters functions having [visibility]. */
    public infix fun haveVisibility(visibility: Visibility): FunctionsRuleBuilder {
        builder.setThat { it.declaration.visibility == visibility }
        return builder
    }

    /** Filters functions having any visibility in [visibilities]. */
    public infix fun haveAnyVisibility(visibilities: List<Visibility>): FunctionsRuleBuilder {
        builder.setThat { func -> visibilities.contains(func.declaration.visibility) }
        return builder
    }

    /** Filters functions having any visibility in [visibilities]. */
    public fun haveAnyVisibility(vararg visibilities: Visibility): FunctionsRuleBuilder =
        haveAnyVisibility(visibilities.asList())

    /** Filters functions annotated with [annotationName] containing argument [argName] with value [argValue]. */
    public fun haveAnnotationWithArgument(
        annotationName: String,
        argName: String?,
        argValue: String,
    ): FunctionsRuleBuilder {
        builder.setThat { func ->
            func.declaration.annotations.any { ann ->
                (ann.name == annotationName || ann.fqName == annotationName) &&
                    ann.arguments.any { arg ->
                        (argName == null || arg.name == argName) && arg.value == argValue
                    }
            }
        }
        return builder
    }

    /** Filters functions declared with [Modifier.SUSPEND]. */
    public fun beSuspend(): FunctionsRuleBuilder = haveModifier(Modifier.SUSPEND)

    /** Filters functions declared with [Modifier.INLINE]. */
    public fun beInline(): FunctionsRuleBuilder = haveModifier(Modifier.INLINE)

    /** Filters functions declared with [Modifier.INFIX]. */
    public fun beInfix(): FunctionsRuleBuilder = haveModifier(Modifier.INFIX)

    /** Filters functions declared with [Modifier.OPERATOR]. */
    public fun beOperator(): FunctionsRuleBuilder = haveModifier(Modifier.OPERATOR)
}
