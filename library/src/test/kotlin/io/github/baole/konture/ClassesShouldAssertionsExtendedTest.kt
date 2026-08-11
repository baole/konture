/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

open class ClassesShouldAssertionsExtendedTest : RuleBuildersTestBase() {
    private fun builder() = ClassesRuleBuilder(projectGraph)

    @Test
    fun `test assignable and access assertions`() {
        val testClass =
            ClassDeclaration(
                name = "TestClass",
                fqName = "com.test.TestClass",
                packageName = "com.test",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/TestClass.kt",
                supertypes = listOf("SuperInterface", "SuperClass"),
                kdocText = "Some documentation",
            )

        val assertAss = builder().should().beAssignableTo("com.test.SuperInterface").getShouldAssertion()!!
        val vAss1 = mutableListOf<String>()
        assertAss(testClass, emptyList(), vAss1)
        assertTrue(vAss1.isEmpty())

        val assertAssFail = builder().should().beAssignableTo("com.test.WrongSuper").getShouldAssertion()!!
        val vAss2 = mutableListOf<String>()
        assertAssFail(testClass, emptyList(), vAss2)
        assertEquals(1, vAss2.size)

        val assertAssAnyList =
            builder()
                .should()
                .beAssignableToAnyOf(
                    listOf("com.test.SuperInterface", "com.test.Wrong"),
                ).getShouldAssertion()!!
        val vAss3 = mutableListOf<String>()
        assertAssAnyList(testClass, emptyList(), vAss3)
        assertTrue(vAss3.isEmpty())

        val assertAssAnyVararg =
            builder()
                .should()
                .beAssignableToAnyOf(
                    "com.test.Wrong",
                    "com.test.Wrong2",
                ).getShouldAssertion()!!
        val vAss4 = mutableListOf<String>()
        assertAssAnyVararg(testClass, emptyList(), vAss4)
        assertEquals(1, vAss4.size)

        val assertAssAllList =
            builder()
                .should()
                .beAssignableToAllOf(
                    listOf("com.test.SuperInterface", "com.test.SuperClass"),
                ).getShouldAssertion()!!
        val vAss5 = mutableListOf<String>()
        assertAssAllList(testClass, emptyList(), vAss5)
        assertTrue(vAss5.isEmpty())

        val assertAssAllVarargFail =
            builder()
                .should()
                .beAssignableToAllOf(
                    "com.test.SuperInterface",
                    "com.test.Wrong",
                ).getShouldAssertion()!!
        val vAss6 = mutableListOf<String>()
        assertAssAllVarargFail(testClass, emptyList(), vAss6)
        assertEquals(1, vAss6.size)

        val grandParent =
            ClassDeclaration(
                name = "GrandParent",
                fqName = "com.example.GrandParent",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/GrandParent.kt",
                supertypes = emptyList(),
            )
        val parent =
            ClassDeclaration(
                name = "Parent",
                fqName = "com.example.Parent",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Parent.kt",
                supertypes = listOf("GrandParent"),
            )
        val child =
            ClassDeclaration(
                name = "Child",
                fqName = "com.example.Child",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Child.kt",
                supertypes = listOf("Parent"),
            )
        val allHierarchy = listOf(grandParent, parent, child)
        val assertAssTransitive = builder().should().beAssignableTo("GrandParent").getShouldAssertion()!!
        val vAssTransitive = mutableListOf<String>()
        assertAssTransitive(child, allHierarchy, vAssTransitive)
        assertTrue(vAssTransitive.isEmpty())

        val assertAssAnyTransitive =
            builder().should().beAssignableToAnyOf(
                "GrandParent",
                "WrongType",
            ).getShouldAssertion()!!
        val vAssAnyTransitive = mutableListOf<String>()
        assertAssAnyTransitive(child, allHierarchy, vAssAnyTransitive)
        assertTrue(vAssAnyTransitive.isEmpty())

        val assertAssAllTransitive =
            builder().should().beAssignableToAllOf(
                "GrandParent",
                "Parent",
            ).getShouldAssertion()!!
        val vAssAllTransitive = mutableListOf<String>()
        assertAssAllTransitive(child, allHierarchy, vAssAllTransitive)
        assertTrue(vAssAllTransitive.isEmpty())

        val assertKDoc = builder().should().beDocumentedWithKDoc().getShouldAssertion()!!
        val vKd1 = mutableListOf<String>()
        assertKDoc(testClass, emptyList(), vKd1)
        assertTrue(vKd1.isEmpty())

        val testClassNoKDoc = testClass.copy(kdocText = null)
        val vKd2 = mutableListOf<String>()
        assertKDoc(testClassNoKDoc, emptyList(), vKd2)
        assertEquals(1, vKd2.size)

        val testClassBlankKDoc = testClass.copy(kdocText = "   ")
        val vKd3 = mutableListOf<String>()
        assertKDoc(testClassBlankKDoc, emptyList(), vKd3)
        assertEquals(1, vKd3.size)

        val accessorClass =
            ClassDeclaration(
                name = "Accessor",
                fqName = "com.other.Accessor",
                packageName = "com.other",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.test.TestClass"),
                referencedTypes = setOf("com.test.TestClass"),
                filePath = "/src/Accessor.kt",
            )
        val assertAccess = builder().should().onlyBeAccessedByAnyPackage("com.other").getShouldAssertion()!!
        val vAcc1 = mutableListOf<String>()
        assertAccess(testClass, listOf(testClass, accessorClass), vAcc1)
        assertTrue(vAcc1.isEmpty())

        val assertAccessFail = builder().should().onlyBeAccessedByAnyPackage("com.allowed.*").getShouldAssertion()!!
        val vAcc2 = mutableListOf<String>()
        assertAccessFail(testClass, listOf(testClass, accessorClass), vAcc2)
        assertEquals(1, vAcc2.size)

        val assertDep = builder().should().onlyDependOnClassesInAnyPackage("com.other").getShouldAssertion()!!
        val vDep1 = mutableListOf<String>()
        assertDep(testClass, listOf(testClass, accessorClass), vDep1)
        assertTrue(vDep1.isEmpty())

        val dependentClass =
            ClassDeclaration(
                name = "Dependent",
                fqName = "com.test.TestClass",
                packageName = "com.test",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.other.Accessor"),
                referencedTypes = setOf("com.other.Accessor"),
                filePath = "/src/TestClass.kt",
            )
        val assertDepFail = builder().should().onlyDependOnClassesInAnyPackage("com.allowed").getShouldAssertion()!!
        val vDep2 = mutableListOf<String>()
        assertDepFail(dependentClass, listOf(dependentClass, accessorClass), vDep2)
        assertEquals(1, vDep2.size)

        val classWithStd =
            ClassDeclaration(
                name = "ClassWithStd",
                fqName = "com.test.ClassWithStd",
                packageName = "com.test",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("java.util.UUID", "kotlin.collections.List", "javax.inject.Inject"),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithStd.kt",
            )
        val assertDepStd = builder().should().onlyDependOnClassesInAnyPackage("com.test").getShouldAssertion()!!
        val vDepStd = mutableListOf<String>()
        assertDepStd(classWithStd, listOf(classWithStd), vDepStd)
        assertTrue(vDepStd.isEmpty())

        val classWithExt =
            ClassDeclaration(
                name = "ClassWithExt",
                fqName = "com.test.ClassWithExt",
                packageName = "com.test",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("org.json.JSONObject"),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithExt.kt",
            )
        val vDepExt = mutableListOf<String>()
        assertDepStd(classWithExt, listOf(classWithExt), vDepExt)
        assertEquals(1, vDepExt.size)

        val assertNotDepExt = builder().should().notDependOnClassesInAnyPackage("org.json..").getShouldAssertion()!!
        val vNotDepExt = mutableListOf<String>()
        assertNotDepExt(classWithExt, listOf(classWithExt), vNotDepExt)
        assertEquals(1, vNotDepExt.size)

        val func1 =
            FunctionDeclaration(
                name = "myFunc",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "Unit",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val classWithFunc = testClass.copy(functions = listOf(func1))
        val assertFunc =
            builder()
                .should()
                .allFunctions {
                    bePublic()
                    beInline()
                }.getShouldAssertion()!!
        val vFn = mutableListOf<String>()
        assertFunc(classWithFunc, emptyList(), vFn)
        assertEquals(1, vFn.size)
        assertTrue(vFn[0].contains("Function myFunc in class com.test.TestClass has violations"))

        val prop1 =
            PropertyDeclaration(
                name = "myProp",
                visibility = Visibility.PRIVATE,
                modifiers = emptySet(),
                type = "String",
                isVal = true,
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
            )
        val classWithProp = testClass.copy(properties = listOf(prop1))
        val assertProp =
            builder()
                .should()
                .allProperties {
                    bePublic()
                }.getShouldAssertion()!!
        val vPr = mutableListOf<String>()
        assertProp(classWithProp, emptyList(), vPr)
        assertEquals(1, vPr.size)
        assertTrue(vPr[0].contains("Property myProp in class com.test.TestClass has violations"))
    }

    @Test
    fun `test classes notBeAccessedByAnyPackage flags accessors in forbidden packages`() {
        val target =
            ClassDeclaration(
                name = "DomainModel",
                fqName = "com.acme.domain.DomainModel",
                packageName = "com.acme.domain",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/DomainModel.kt",
            )
        val forbiddenAccessor =
            ClassDeclaration(
                name = "WebController",
                fqName = "com.acme.web.WebController",
                packageName = "com.acme.web",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.acme.domain.DomainModel"),
                referencedTypes = setOf("com.acme.domain.DomainModel"),
                filePath = "/src/WebController.kt",
            )
        val allowedAccessor =
            ClassDeclaration(
                name = "DomainService",
                fqName = "com.acme.domain.DomainService",
                packageName = "com.acme.domain",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.acme.domain.DomainModel"),
                referencedTypes = setOf("com.acme.domain.DomainModel"),
                filePath = "/src/DomainService.kt",
            )

        val assertForbidden = builder().should().notBeAccessedByAnyPackage("..web..").getShouldAssertion()!!
        val vForbidden = mutableListOf<String>()
        assertForbidden(target, listOf(target, forbiddenAccessor, allowedAccessor), vForbidden)
        assertEquals(1, vForbidden.size)

        val assertClean = builder().should().notBeAccessedByAnyPackage("..web..").getShouldAssertion()!!
        val vClean = mutableListOf<String>()
        assertClean(target, listOf(target, allowedAccessor), vClean)
        assertTrue(vClean.isEmpty())

        val assertVacuous = builder().should().notBeAccessedByAnyPackage("..web..").getShouldAssertion()!!
        val vVacuous = mutableListOf<String>()
        assertVacuous(target, listOf(target), vVacuous)
        assertTrue(vVacuous.isEmpty())
    }

    @Test
    fun `test signature leak in constructor parameters`() {
        val entityAnnotation = AnnotationDeclaration("Entity", "jakarta.persistence.Entity")
        val entityClass =
            ClassDeclaration(
                name = "UserEntity",
                fqName = "com.example.data.UserEntity",
                packageName = "com.example.data",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(entityAnnotation),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/UserEntity.kt",
            )
        val classWithLeakingConstructor =
            ClassDeclaration(
                name = "UserService",
                fqName = "com.example.UserService",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.example.data.UserEntity"),
                referencedTypes = emptySet(),
                filePath = "/src/UserService.kt",
                primaryConstructor =
                    ConstructorDeclaration(
                        visibility = Visibility.PUBLIC,
                        parameters =
                            listOf(
                                ParameterDeclaration(
                                    name = "userEntity",
                                    type = "UserEntity",
                                    hasDefaultValue = false,
                                    annotations = emptyList(),
                                ),
                            ),
                        annotations = emptyList(),
                    ),
            )

        val rule =
            ClassesRuleBuilder(projectGraph)
                .should()
                .notHaveSignaturesWithTypesAnnotatedWith("jakarta.persistence.Entity")
        val assertion = rule.getShouldAssertion()!!
        val violations = mutableListOf<String>()
        assertion(classWithLeakingConstructor, listOf(entityClass, classWithLeakingConstructor), violations)
        assertEquals(1, violations.size)
        assertTrue(violations[0].contains("UserEntity"))
        assertTrue(violations[0].contains("Entity"))
    }
}
