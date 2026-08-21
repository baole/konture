/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TestAnnotationA

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TestAnnotationB

open class TestSuperClass

class TypeSafeOverloadsComprehensiveCoverageTest : RuleBuildersTestBase() {
    @Suppress("DEPRECATION")
    @Test
    fun `test type safe overloads for classes that and should`() {
        val classesThat = Konture.classes().that()
        assertNotNull(classesThat.annotatedWith(TestAnnotationA::class))
        assertNotNull(classesThat.annotatedWith<TestAnnotationA>())
        assertNotNull(classesThat.annotatedWithAllOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(classesThat.annotatedWithAnyOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(classesThat.haveAnnotationOf(TestAnnotationA::class))
        assertNotNull(classesThat.haveAnnotationOf<TestAnnotationA>())
        assertNotNull(classesThat.haveAllAnnotationsOf(TestAnnotationA::class))
        assertNotNull(classesThat.haveAnyAnnotationOf(TestAnnotationA::class))
        assertNotNull(classesThat.haveAnnotationWithArgument(TestAnnotationA::class, "name", "val"))
        assertNotNull(classesThat.areAssignableTo(TestSuperClass::class))
        assertNotNull(classesThat.areAssignableTo<TestSuperClass>())
        assertNotNull(classesThat.areAssignableTo(listOf(TestSuperClass::class)))
        assertNotNull(classesThat.areAssignableToAnyOf(TestSuperClass::class))
        assertNotNull(classesThat.areAssignableToAllOf(TestSuperClass::class))
        assertNotNull(classesThat.areAssignableFrom(TestSuperClass::class))
        assertNotNull(classesThat.areAssignableFrom<TestSuperClass>())
        assertNotNull(classesThat.areNotAssignableTo<TestSuperClass>())
        assertNotNull(classesThat.areNotAssignableFrom<TestSuperClass>())
        assertNotNull(classesThat.inPackageOf(TestSuperClass::class))
        assertNotNull(classesThat.inPackageOf<TestSuperClass>())
        assertNotNull(classesThat.resideInPackageOf(TestSuperClass::class))
        assertNotNull(classesThat.resideInPackageOf<TestSuperClass>())

        val classesShould = Konture.classes().should()
        assertNotNull(classesShould.annotatedWith(TestAnnotationA::class))
        assertNotNull(classesShould.annotatedWith<TestAnnotationA>())
        assertNotNull(classesShould.annotatedWithAllOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(classesShould.annotatedWithAnyOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(classesShould.haveAnnotationOf(TestAnnotationA::class))
        assertNotNull(classesShould.haveAnnotationOf<TestAnnotationA>())
        assertNotNull(classesShould.haveAllAnnotationsOf(TestAnnotationA::class))
        assertNotNull(classesShould.haveAnyAnnotationOf(TestAnnotationA::class))
        assertNotNull(classesShould.beAnnotatedWith(TestAnnotationA::class))
        assertNotNull(classesShould.beAnnotatedWith<TestAnnotationA>())
        assertNotNull(classesShould.haveAnnotationWithArgument(TestAnnotationA::class, "name", "val"))
        assertNotNull(classesShould.beAssignableTo(TestSuperClass::class))
        assertNotNull(classesShould.beAssignableTo<TestSuperClass>())
        assertNotNull(classesShould.beAssignableToAnyOf(TestSuperClass::class))
        assertNotNull(classesShould.beAssignableToAllOf(TestSuperClass::class))
        assertNotNull(classesShould.beAssignableFrom(TestSuperClass::class))
        assertNotNull(classesShould.beAssignableFrom<TestSuperClass>())
        assertNotNull(classesShould.inPackageOf(TestSuperClass::class))
        assertNotNull(classesShould.inPackageOf<TestSuperClass>())
        assertNotNull(classesShould.resideInPackageOf(TestSuperClass::class))
        assertNotNull(classesShould.resideInPackageOf<TestSuperClass>())
        assertNotNull(classesShould.notDependOnClass<TestSuperClass>())

        val classList = listOf(classA, classB)
        assertNotNull(classList.withAnnotationOf(TestAnnotationA::class))
        assertNotNull(classList.withAnnotationOf<TestAnnotationA>())
        assertNotNull(classList.withoutAnnotationOf(TestAnnotationA::class))
        assertNotNull(classList.withoutAnnotationOf<TestAnnotationA>())
        assertNotNull(classList.withParentOf(TestSuperClass::class))
        assertNotNull(classList.withParentOf<TestSuperClass>())

        val scope = KontureScope(classList)
        assertNotNull(scope.withAnnotationOf(TestAnnotationA::class))
        assertNotNull(scope.withAnnotationOf<TestAnnotationA>())
        assertNotNull(scope.withoutAnnotationOf(TestAnnotationA::class))
        assertNotNull(scope.withoutAnnotationOf<TestAnnotationA>())
        assertNotNull(scope.withParentOf(TestSuperClass::class))
        assertNotNull(scope.withParentOf<TestSuperClass>())
    }

    @Test
    fun `test type safe overloads for properties that and should`() {
        val propertiesThat = Konture.properties().that()
        assertNotNull(propertiesThat.haveAnnotationWithArgument(TestAnnotationA::class, "arg", "val"))
        assertNotNull(propertiesThat.haveAnnotationOf(TestAnnotationA::class))
        assertNotNull(propertiesThat.haveAnnotationOfType<TestAnnotationA>())
        assertNotNull(propertiesThat.haveAnnotationOf<TestAnnotationA>())
        assertNotNull(propertiesThat.haveAllAnnotationsOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(propertiesThat.haveAnyAnnotationOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(propertiesThat.resideInPackageOf(TestSuperClass::class))
        assertNotNull(propertiesThat.resideInPackageOf<TestSuperClass>())
        assertNotNull(propertiesThat.haveImportOf<TestSuperClass>())
        assertNotNull(propertiesThat.notHaveImportOf<TestSuperClass>())
        assertNotNull(propertiesThat.areAnnotatedWith(TestAnnotationA::class))
        assertNotNull(propertiesThat.areAnnotatedWith<TestAnnotationA>())
        assertNotNull(propertiesThat.dependOnPackageOf<TestSuperClass>())
        assertNotNull(propertiesThat.notDependOnPackageOf(TestSuperClass::class))
        assertNotNull(propertiesThat.notDependOnPackageOf<TestSuperClass>())

        val propertiesShould = Konture.properties().should()
        assertNotNull(propertiesShould.haveAnnotationWithArgument(TestAnnotationA::class, "arg", "val"))
        assertNotNull(propertiesShould.haveAnnotationOf(TestAnnotationA::class))
        assertNotNull(propertiesShould.haveAnnotationOfType<TestAnnotationA>())
        assertNotNull(propertiesShould.haveAllAnnotationsOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(propertiesShould.haveAnyAnnotationOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(propertiesShould.resideInPackageOf(TestSuperClass::class))
        assertNotNull(propertiesShould.resideInPackageOf<TestSuperClass>())
        assertNotNull(propertiesShould.notCall<TestSuperClass>())
        assertNotNull(propertiesShould.notReferenceClass<TestSuperClass>())
        assertNotNull(propertiesShould.haveImportOf<TestSuperClass>())
        assertNotNull(propertiesShould.notHaveImportOf<TestSuperClass>())
        assertNotNull(propertiesShould.beAnnotatedWith(TestAnnotationA::class))
        assertNotNull(propertiesShould.beAnnotatedWith<TestAnnotationA>())
        assertNotNull(propertiesShould.dependOnPackageOf<TestSuperClass>())
        assertNotNull(propertiesShould.onlyDependOnPackageOf<TestSuperClass>())
        assertNotNull(propertiesShould.notDependOnPackageOf<TestSuperClass>())

        val scope = PropertyAssertionScope()
        scope.haveType(String::class)
        scope.haveTypeOf<String>()
        scope.haveAnnotationOf(TestAnnotationA::class)
        scope.haveAnnotationOfType<TestAnnotationA>()
    }

    @Test
    fun `test type safe overloads for functions that and should`() {
        val functionsThat = Konture.functions().that()
        assertNotNull(functionsThat.haveAnnotationWithArgument(TestAnnotationA::class, "arg", "val"))
        assertNotNull(functionsThat.haveAnnotationOf(TestAnnotationA::class))
        assertNotNull(functionsThat.haveAnnotationOfType<TestAnnotationA>())
        assertNotNull(functionsThat.haveAnnotationOf<TestAnnotationA>())
        assertNotNull(functionsThat.haveAllAnnotationsOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(functionsThat.haveAnyAnnotationOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(functionsThat.resideInPackageOf(TestSuperClass::class))
        assertNotNull(functionsThat.resideInPackageOf<TestSuperClass>())
        assertNotNull(functionsThat.haveReturnType<String>())
        assertNotNull(functionsThat.notHaveReturnType<String>())
        assertNotNull(functionsThat.notHaveParameterOf<String>())
        assertNotNull(functionsThat.haveParameterOf(listOf(String::class, Int::class)))
        assertNotNull(functionsThat.areAnnotatedWith(TestAnnotationA::class))
        assertNotNull(functionsThat.dependOnPackageOf<TestSuperClass>())
        assertNotNull(functionsThat.notDependOnPackageOf(TestSuperClass::class))
        assertNotNull(functionsThat.notDependOnPackageOf<TestSuperClass>())

        val functionsShould = Konture.functions().should()
        assertNotNull(functionsShould.haveAnnotationWithArgument(TestAnnotationA::class, "arg", "val"))
        assertNotNull(functionsShould.haveAnnotationOf(TestAnnotationA::class))
        assertNotNull(functionsShould.haveAnnotationOfType<TestAnnotationA>())
        assertNotNull(functionsShould.haveAllAnnotationsOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(functionsShould.haveAnyAnnotationOf(TestAnnotationA::class, TestAnnotationB::class))
        assertNotNull(functionsShould.resideInPackageOf(TestSuperClass::class))
        assertNotNull(functionsShould.resideInPackageOf<TestSuperClass>())
        assertNotNull(functionsShould.beAnnotatedWith(TestAnnotationA::class))
        assertNotNull(functionsShould.beAnnotatedWith<TestAnnotationA>())
        assertNotNull(functionsShould.dependOnPackageOf<TestSuperClass>())
        assertNotNull(functionsShould.onlyDependOnPackageOf<TestSuperClass>())
        assertNotNull(functionsShould.notDependOnPackageOf<TestSuperClass>())

        val scope = FunctionAssertionScope()
        scope.haveReturnType(String::class)
        scope.haveReturnTypeOf<String>()
        scope.haveAnnotationOf(TestAnnotationA::class)
        scope.haveAnnotationOfType<TestAnnotationA>()
    }
}
