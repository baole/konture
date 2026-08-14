/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package io.github.baole.konture

import kotlin.reflect.KClass

/** Filters classes having the specified [annotation] class. */
public infix fun ClassesThat.annotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWith(annotation.kontureQualifiedName())

/** Filters classes having annotation type parameter [T]. */
public inline fun <reified T : Annotation> ClassesThat.annotatedWith(): ClassesRuleBuilder = annotatedWith(T::class)

/** Filters classes having all of the specified [annotations]. */
public fun ClassesThat.annotatedWithAllOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWithAllOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Filters classes having any of the specified [annotations]. */
public fun ClassesThat.annotatedWithAnyOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWithAnyOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that classes have the specified [annotation] class. */
public infix fun ClassesShould.annotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWith(annotation.kontureQualifiedName())

/** Asserts that classes have annotation type parameter [T]. */
public inline fun <reified T : Annotation> ClassesShould.annotatedWith(): ClassesRuleBuilder = annotatedWith(T::class)

/** Asserts that classes have all of the specified [annotations]. */
public fun ClassesShould.annotatedWithAllOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWithAllOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that classes have any of the specified [annotations]. */
public fun ClassesShould.annotatedWithAnyOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWithAnyOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

// Legacy deprecations for annotation overloads on ClassesThat and ClassesShould

/** Legacy haveAnnotationOf method. */
@Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotation)"))
public infix fun ClassesThat.haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWith(annotation)

/** Legacy haveAnnotationOf method. */
@Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith<T>()"))
public inline fun <reified T : Annotation> ClassesThat.haveAnnotationOf(): ClassesRuleBuilder = annotatedWith<T>()

/** Legacy haveAllAnnotationsOf method. */
@Deprecated("Use annotatedWithAllOf instead.", ReplaceWith("annotatedWithAllOf(*annotations)"))
public fun ClassesThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWithAllOf(*annotations)

/** Legacy haveAnyAnnotationOf method. */
@Deprecated("Use annotatedWithAnyOf instead.", ReplaceWith("annotatedWithAnyOf(*annotations)"))
public fun ClassesThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWithAnyOf(*annotations)

/** Legacy haveAnnotationOf method. */
@Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotation)"))
public infix fun ClassesShould.haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWith(annotation)

/** Legacy haveAnnotationOf method. */
@Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith<T>()"))
public inline fun <reified T : Annotation> ClassesShould.haveAnnotationOf(): ClassesRuleBuilder = annotatedWith<T>()

/** Legacy haveAllAnnotationsOf method. */
@Deprecated("Use annotatedWithAllOf instead.", ReplaceWith("annotatedWithAllOf(*annotations)"))
public fun ClassesShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWithAllOf(*annotations)

/** Legacy haveAnyAnnotationOf method. */
@Deprecated("Use annotatedWithAnyOf instead.", ReplaceWith("annotatedWithAnyOf(*annotations)"))
public fun ClassesShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWithAnyOf(*annotations)

/** Filters classes having an annotation [annotation] with argument [argName] equal to [argValue]. */
public fun ClassesThat.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): ClassesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

/** Asserts that classes have an annotation [annotation] with argument [argName] equal to [argValue]. */
public fun ClassesShould.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): ClassesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

/** Filters classes that are assignable to the specified [superType]. */
public infix fun ClassesThat.areAssignableTo(superType: KClass<*>): ClassesRuleBuilder =
    areAssignableTo(superType.kontureQualifiedName())

/** Filters classes that are assignable to type parameter [T]. */
public inline fun <reified T : Any> ClassesThat.areAssignableTo(): ClassesRuleBuilder = areAssignableTo(T::class)

/** Filters classes that are assignable to any of the specified [superTypes]. */
public fun ClassesThat.areAssignableToAnyOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    areAssignableToAnyOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

/** Filters classes that are assignable to all of the specified [superTypes]. */
public fun ClassesThat.areAssignableToAllOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    areAssignableToAllOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that classes are assignable to the specified [superType]. */
public infix fun ClassesShould.beAssignableTo(superType: KClass<*>): ClassesRuleBuilder =
    beAssignableTo(superType.kontureQualifiedName())

/** Asserts that classes are assignable to type parameter [T]. */
public inline fun <reified T : Any> ClassesShould.beAssignableTo(): ClassesRuleBuilder = beAssignableTo(T::class)

/** Asserts that classes are assignable to any of the specified [superTypes]. */
public fun ClassesShould.beAssignableToAnyOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    beAssignableToAnyOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that classes are assignable to all of the specified [superTypes]. */
public fun ClassesShould.beAssignableToAllOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    beAssignableToAllOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

/** Filters classes that are assignable from the specified [subType]. */
public infix fun ClassesThat.areAssignableFrom(subType: KClass<*>): ClassesRuleBuilder =
    areAssignableFrom(subType.kontureQualifiedName())

/** Filters classes that are assignable from type parameter [T]. */
public inline fun <reified T : Any> ClassesThat.areAssignableFrom(): ClassesRuleBuilder = areAssignableFrom(T::class)

/** Asserts that classes are assignable from the specified [subType]. */
public infix fun ClassesShould.beAssignableFrom(subType: KClass<*>): ClassesRuleBuilder =
    beAssignableFrom(subType.kontureQualifiedName())

/** Asserts that classes are assignable from type parameter [T]. */
public inline fun <reified T : Any> ClassesShould.beAssignableFrom(): ClassesRuleBuilder = beAssignableFrom(T::class)

/** Filters a list of class declarations returning those with annotation [annotation]. */
public fun List<ClassDeclaration>.withAnnotationOf(annotation: KClass<out Annotation>): List<ClassDeclaration> =
    withAnnotationOf(annotation.kontureQualifiedName())

/** Filters a list of class declarations returning those with annotation type parameter [T]. */
public inline fun <reified T : Annotation> List<ClassDeclaration>.withAnnotationOf(): List<ClassDeclaration> =
    withAnnotationOf(T::class)

/** Filters a list of class declarations returning those without annotation [annotation]. */
public fun List<ClassDeclaration>.withoutAnnotationOf(annotation: KClass<out Annotation>): List<ClassDeclaration> =
    withoutAnnotationOf(annotation.kontureQualifiedName())

/** Filters a list of class declarations returning those without annotation type parameter [T]. */
public inline fun <reified T : Annotation> List<ClassDeclaration>.withoutAnnotationOf(): List<ClassDeclaration> =
    withoutAnnotationOf(T::class)

/** Filters a list of class declarations returning those extending or implementing [type]. */
public fun List<ClassDeclaration>.withParentOf(type: KClass<*>): List<ClassDeclaration> =
    withParentOf(type.kontureQualifiedName())

/** Filters a list of class declarations returning those extending or implementing type parameter [T]. */
public inline fun <reified T : Any> List<ClassDeclaration>.withParentOf(): List<ClassDeclaration> =
    withParentOf(T::class)

/** Asserts that a list of class declarations have the specified [annotations]. */
public fun List<ClassDeclaration>.assertHaveAnnotationOf(vararg annotations: KClass<out Annotation>): Unit =
    assertHaveAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

/** Asserts that a list of class declarations are assignable to [first] and [additional] supertypes. */
public fun List<ClassDeclaration>.assertAreAssignableTo(
    first: KClass<*>,
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) {
    assertAreAssignableTo(
        first.kontureQualifiedName(),
        *additional.map { it.kontureQualifiedName() }.toTypedArray(),
        allClasses = allClasses,
    )
}

/** Asserts that a list of class declarations have annotation type parameter [T]. */
public inline fun <reified T : Annotation> List<ClassDeclaration>.assertHaveAnnotationOfType(): Unit =
    assertHaveAnnotationOf(T::class)

/** Asserts that a list of class declarations are assignable to type parameter [T]. */
public inline fun <reified T : Any> List<ClassDeclaration>.assertAreAssignableToType(
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
): Unit = assertAreAssignableTo(T::class, *additional, allClasses = allClasses)

/** Filters classes in a KontureScope returning those with annotation [annotation]. */
public fun KontureScope.withAnnotationOf(annotation: KClass<out Annotation>): KontureScope =
    KontureScope(classes.withAnnotationOf(annotation))

/** Filters classes in a KontureScope returning those with annotation type parameter [T]. */
public inline fun <reified T : Annotation> KontureScope.withAnnotationOf(): KontureScope =
    KontureScope(classes.withAnnotationOf<T>())

/** Filters classes in a KontureScope returning those without annotation [annotation]. */
public fun KontureScope.withoutAnnotationOf(annotation: KClass<out Annotation>): KontureScope =
    KontureScope(classes.withoutAnnotationOf(annotation))

/** Filters classes in a KontureScope returning those without annotation type parameter [T]. */
public inline fun <reified T : Annotation> KontureScope.withoutAnnotationOf(): KontureScope =
    KontureScope(classes.withoutAnnotationOf<T>())

/** Filters classes in a KontureScope returning those with parent [type]. */
public fun KontureScope.withParentOf(type: KClass<*>): KontureScope = KontureScope(classes.withParentOf(type))

/** Filters classes in a KontureScope returning those with parent type parameter [T]. */
public inline fun <reified T : Any> KontureScope.withParentOf(): KontureScope = KontureScope(classes.withParentOf<T>())

/** Asserts that classes in a KontureScope have the specified [annotations]. */
public fun KontureScope.assertHaveAnnotationOf(vararg annotations: KClass<out Annotation>): Unit =
    classes.assertHaveAnnotationOf(*annotations)

/** Asserts that classes in a KontureScope are assignable to [first] and [additional] supertypes. */
public fun KontureScope.assertAreAssignableTo(
    first: KClass<*>,
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
): Unit = classes.assertAreAssignableTo(first, *additional, allClasses = allClasses)

/** Asserts that classes in a KontureScope have annotation type parameter [T]. */
public inline fun <reified T : Annotation> KontureScope.assertHaveAnnotationOfType(): Unit =
    assertHaveAnnotationOf(T::class)

/** Asserts that classes in a KontureScope are assignable to type parameter [T]. */
public inline fun <reified T : Any> KontureScope.assertAreAssignableToType(
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
): Unit = assertAreAssignableTo(T::class, *additional, allClasses = allClasses)

/** Filters classes residing in the package of [type]. */
public infix fun ClassesThat.inPackageOf(type: KClass<*>): ClassesRuleBuilder =
    inPackage(type.toKonturePackageReference().packageName)

/** Filters classes assignable to [superTypes]. */
@JvmName("areAssignableToKClasses")
public infix fun ClassesThat.areAssignableTo(superTypes: List<KClass<*>>): ClassesRuleBuilder =
    areAssignableTo(superTypes.map { it.qualifiedName ?: it.java.name })

/** Filters classes residing in the package of type parameter [T]. */
public inline fun <reified T : Any> ClassesThat.inPackageOf(): ClassesRuleBuilder = inPackageOf(T::class)

/** Asserts that classes reside in the package of [type]. */
public infix fun ClassesShould.inPackageOf(type: KClass<*>): ClassesRuleBuilder =
    inPackage(type.toKonturePackageReference().packageName)

/** Asserts that classes reside in the package of type parameter [T]. */
public inline fun <reified T : Any> ClassesShould.inPackageOf(): ClassesRuleBuilder = inPackageOf(T::class)

/** Legacy resideInPackageOf method. */
@Deprecated("Use inPackageOf instead.", ReplaceWith("inPackageOf(type)"))
public infix fun ClassesThat.resideInPackageOf(type: KClass<*>): ClassesRuleBuilder = inPackageOf(type)

/** Legacy resideInPackageOf method. */
@Deprecated("Use inPackageOf instead.", ReplaceWith("inPackageOf<T>()"))
public inline fun <reified T : Any> ClassesThat.resideInPackageOf(): ClassesRuleBuilder = inPackageOf<T>()

/** Legacy resideInPackageOf method. */
@Deprecated("Use inPackageOf instead.", ReplaceWith("inPackageOf(type)"))
public infix fun ClassesShould.resideInPackageOf(type: KClass<*>): ClassesRuleBuilder = inPackageOf(type)

/** Legacy resideInPackageOf method. */
@Deprecated("Use inPackageOf instead.", ReplaceWith("inPackageOf<T>()"))
public inline fun <reified T : Any> ClassesShould.resideInPackageOf(): ClassesRuleBuilder = inPackageOf<T>()

/** Filters classes that are not assignable to type parameter [T]. */
public inline fun <reified T : Any> ClassesThat.areNotAssignableTo(): ClassesRuleBuilder = areNotAssignableTo(T::class)

/** Filters classes that are not assignable from type parameter [T]. */
public inline fun <reified T : Any> ClassesThat.areNotAssignableFrom(): ClassesRuleBuilder =
    areNotAssignableFrom(T::class)

/** Legacy beAnnotatedWith method. */
@Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith(annotation)"))
public infix fun ClassesShould.beAnnotatedWith(annotation: KClass<out Annotation>): ClassesRuleBuilder =
    annotatedWith(annotation)

/** Legacy beAnnotatedWith method. */
@Deprecated("Use annotatedWith instead.", ReplaceWith("annotatedWith<T>()"))
public inline fun <reified T : Annotation> ClassesShould.beAnnotatedWith(): ClassesRuleBuilder = annotatedWith<T>()

/** Asserts that classes do not depend on class type parameter [T]. */
public inline fun <reified T : Any> ClassesShould.notDependOnClass(): ClassesRuleBuilder = notReferenceClass(T::class)
