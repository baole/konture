/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import kotlin.reflect.KClass

infix fun ClassesThat.haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder = haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> ClassesThat.haveAnnotationOf(): ClassesRuleBuilder = haveAnnotationOf(T::class)

fun ClassesThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun ClassesShould.haveAnnotationOf(annotation: KClass<out Annotation>): ClassesRuleBuilder = haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> ClassesShould.haveAnnotationOf(): ClassesRuleBuilder = haveAnnotationOf(T::class)

fun ClassesShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): ClassesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesThat.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): ClassesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

fun ClassesShould.haveAnnotationWithArgument(
    annotation: KClass<out Annotation>,
    argName: String?,
    argValue: String,
): ClassesRuleBuilder = haveAnnotationWithArgument(annotation.kontureQualifiedName(), argName, argValue)

infix fun ClassesThat.areAssignableTo(superType: KClass<*>): ClassesRuleBuilder = areAssignableTo(superType.kontureQualifiedName())

inline fun <reified T : Any> ClassesThat.areAssignableTo(): ClassesRuleBuilder = areAssignableTo(T::class)

fun ClassesThat.areAssignableToAnyOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    areAssignableToAnyOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesThat.areAssignableToAllOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    areAssignableToAllOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

infix fun ClassesShould.beAssignableTo(superType: KClass<*>): ClassesRuleBuilder = beAssignableTo(superType.kontureQualifiedName())

inline fun <reified T : Any> ClassesShould.beAssignableTo(): ClassesRuleBuilder = beAssignableTo(T::class)

fun ClassesShould.beAssignableToAnyOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    beAssignableToAnyOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

fun ClassesShould.beAssignableToAllOf(vararg superTypes: KClass<*>): ClassesRuleBuilder =
    beAssignableToAllOf(*superTypes.map { it.kontureQualifiedName() }.toTypedArray())

infix fun FunctionsThat.haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(
        annotation.kontureQualifiedName(),
    )

inline fun <reified T : Annotation> FunctionsThat.haveAnnotationOfType(): FunctionsRuleBuilder = haveAnnotationOf(T::class)

fun FunctionsThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun FunctionsThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun FunctionsShould.haveAnnotationOf(annotation: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnnotationOf(
        annotation.kontureQualifiedName(),
    )

inline fun <reified T : Annotation> FunctionsShould.haveAnnotationOfType(): FunctionsRuleBuilder = haveAnnotationOf(T::class)

fun FunctionsShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun FunctionsShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): FunctionsRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun PropertiesThat.haveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(
        annotation.kontureQualifiedName(),
    )

inline fun <reified T : Annotation> PropertiesThat.haveAnnotationOfType(): PropertiesRuleBuilder = haveAnnotationOf(T::class)

fun PropertiesThat.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun PropertiesThat.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

infix fun PropertiesShould.haveAnnotationOf(annotation: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnnotationOf(
        annotation.kontureQualifiedName(),
    )

inline fun <reified T : Annotation> PropertiesShould.haveAnnotationOfType(): PropertiesRuleBuilder = haveAnnotationOf(T::class)

fun PropertiesShould.haveAllAnnotationsOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAllAnnotationsOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun PropertiesShould.haveAnyAnnotationOf(vararg annotations: KClass<out Annotation>): PropertiesRuleBuilder =
    haveAnyAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun List<ClassDeclaration>.withAnnotationOf(annotation: KClass<out Annotation>): List<ClassDeclaration> =
    withAnnotationOf(
        annotation.kontureQualifiedName(),
    )

inline fun <reified T : Annotation> List<ClassDeclaration>.withAnnotationOf(): List<ClassDeclaration> = withAnnotationOf(T::class)

fun List<ClassDeclaration>.withoutAnnotationOf(annotation: KClass<out Annotation>): List<ClassDeclaration> =
    withoutAnnotationOf(
        annotation.kontureQualifiedName(),
    )

inline fun <reified T : Annotation> List<ClassDeclaration>.withoutAnnotationOf(): List<ClassDeclaration> = withoutAnnotationOf(T::class)

fun List<ClassDeclaration>.withParentOf(type: KClass<*>): List<ClassDeclaration> = withParentOf(type.kontureQualifiedName())

inline fun <reified T : Any> List<ClassDeclaration>.withParentOf(): List<ClassDeclaration> = withParentOf(T::class)

fun List<ClassDeclaration>.assertHaveAnnotationOf(vararg annotations: KClass<out Annotation>) =
    assertHaveAnnotationOf(*annotations.map { it.kontureQualifiedName() }.toTypedArray())

fun List<ClassDeclaration>.assertAreAssignableTo(
    first: KClass<*>,
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = assertAreAssignableTo(first.kontureQualifiedName(), *additional.map { it.kontureQualifiedName() }.toTypedArray(), allClasses = allClasses)

inline fun <reified T : Annotation> List<ClassDeclaration>.assertHaveAnnotationOfType() = assertHaveAnnotationOf(T::class)

inline fun <reified T : Any> List<ClassDeclaration>.assertAreAssignableToType(
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = assertAreAssignableTo(T::class, *additional, allClasses = allClasses)

fun KontureScope.withAnnotationOf(annotation: KClass<out Annotation>) = KontureScope(classes.withAnnotationOf(annotation))

inline fun <reified T : Annotation> KontureScope.withAnnotationOf() = KontureScope(classes.withAnnotationOf<T>())

fun KontureScope.withoutAnnotationOf(annotation: KClass<out Annotation>) = KontureScope(classes.withoutAnnotationOf(annotation))

inline fun <reified T : Annotation> KontureScope.withoutAnnotationOf() = KontureScope(classes.withoutAnnotationOf<T>())

fun KontureScope.withParentOf(type: KClass<*>) = KontureScope(classes.withParentOf(type))

inline fun <reified T : Any> KontureScope.withParentOf() = KontureScope(classes.withParentOf<T>())

fun KontureScope.assertHaveAnnotationOf(vararg annotations: KClass<out Annotation>) = classes.assertHaveAnnotationOf(*annotations)

fun KontureScope.assertAreAssignableTo(
    first: KClass<*>,
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = classes.assertAreAssignableTo(first, *additional, allClasses = allClasses)

inline fun <reified T : Annotation> KontureScope.assertHaveAnnotationOfType() = assertHaveAnnotationOf(T::class)

inline fun <reified T : Any> KontureScope.assertAreAssignableToType(
    vararg additional: KClass<*>,
    allClasses: List<ClassDeclaration> = Konture.projectGraph.getAllModules().flatMap { it.classes },
) = assertAreAssignableTo(T::class, *additional, allClasses = allClasses)

infix fun ClassesThat.resideInPackageOf(type: KClass<*>): ClassesRuleBuilder = resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> ClassesThat.resideInPackageOf(): ClassesRuleBuilder = resideInPackageOf(T::class)

infix fun ClassesShould.resideInPackageOf(type: KClass<*>): ClassesRuleBuilder = resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> ClassesShould.resideInPackageOf(): ClassesRuleBuilder = resideInPackageOf(T::class)

infix fun FunctionsThat.resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder = resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> FunctionsThat.resideInPackageOf(): FunctionsRuleBuilder = resideInPackageOf(T::class)

infix fun FunctionsShould.resideInPackageOf(type: KClass<*>): FunctionsRuleBuilder = resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> FunctionsShould.resideInPackageOf(): FunctionsRuleBuilder = resideInPackageOf(T::class)

infix fun PropertiesThat.resideInPackageOf(type: KClass<*>): PropertiesRuleBuilder = resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> PropertiesThat.resideInPackageOf(): PropertiesRuleBuilder = resideInPackageOf(T::class)

infix fun PropertiesShould.resideInPackageOf(type: KClass<*>): PropertiesRuleBuilder = resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> PropertiesShould.resideInPackageOf(): PropertiesRuleBuilder = resideInPackageOf(T::class)

infix fun FilesThat.resideInPackageOf(type: KClass<*>): FilesRuleBuilder = resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> FilesThat.resideInPackageOf(): FilesRuleBuilder = resideInPackageOf(T::class)

infix fun FilesShould.resideInPackageOf(type: KClass<*>): FilesRuleBuilder = resideInAPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> FilesShould.resideInPackageOf(): FilesRuleBuilder = resideInPackageOf(T::class)

fun Konture.scopeFromPackageOf(type: KClass<*>) = scopeFromPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> Konture.scopeFromPackageOf() = scopeFromPackageOf(T::class)

fun Konture.fileScopeFromPackageOf(type: KClass<*>) = fileScopeFromPackage(type.toKonturePackageReference().packageName)

inline fun <reified T : Any> Konture.fileScopeFromPackageOf() = fileScopeFromPackageOf(T::class)

fun FunctionAssertionScope.haveReturnType(type: KClass<*>) {
    val expectedType = type.toKontureTypeReference()
    assertions.add { function, violations ->
        if (function.resolvedReturnType?.let { matchesKotlinType(it, expectedType) } != true) {
            violations.add("should have return type '${type.kontureQualifiedName()}' (was '${function.returnType}')")
        }
    }
}

inline fun <reified T : Any> FunctionAssertionScope.haveReturnTypeOf() = haveReturnType(T::class)

fun FunctionAssertionScope.haveAnnotationOf(annotation: KClass<out Annotation>) = haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> FunctionAssertionScope.haveAnnotationOfType() = haveAnnotationOf(T::class)

fun PropertyAssertionScope.haveType(type: KClass<*>) {
    val expectedType = type.toKontureTypeReference()
    assertions.add { property, violations ->
        if (property.resolvedType?.let { matchesKotlinType(it, expectedType) } != true) {
            violations.add("should have type '${type.kontureQualifiedName()}' (was '${property.type}')")
        }
    }
}

inline fun <reified T : Any> PropertyAssertionScope.haveTypeOf() = haveType(T::class)

fun PropertyAssertionScope.haveAnnotationOf(annotation: KClass<out Annotation>) = haveAnnotationOf(annotation.kontureQualifiedName())

inline fun <reified T : Annotation> PropertyAssertionScope.haveAnnotationOfType() = haveAnnotationOf(T::class)
