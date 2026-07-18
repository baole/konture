/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Suppress("LongMethod", "LargeClass")
class ClassesShouldTest : RuleBuildersTestBase() {
    @Test
    fun `test classes rule builder assertions anyOf - allOf - noneOf`() {
        val ruleAny =
            ClassesRuleBuilder(projectGraph)
                .should()
                .anyOf(
                    {
                        satisfy { cls, violations ->
                            if (!cls.name.endsWith("A")) {
                                violations.add("error")
                            }
                        }
                    },
                    {
                        satisfy { cls, violations ->
                            if (!cls.name.endsWith("B")) {
                                violations.add("error")
                            }
                        }
                    },
                )
        val assertionAny = ruleAny.getShouldAssertion()!!

        val vA = mutableListOf<String>()
        assertionAny(classA, emptyList(), vA)
        assertTrue(vA.isEmpty())

        val vC = mutableListOf<String>()
        assertionAny(classC, emptyList(), vC)
        assertFalse(vC.isEmpty())

        val ruleAll =
            ClassesRuleBuilder(projectGraph)
                .should()
                .allOf(
                    {
                        satisfy { cls, violations ->
                            if (!cls.name.startsWith("Class")) {
                                violations.add("error")
                            }
                        }
                    },
                    {
                        satisfy { cls, violations ->
                            if (cls.packageName != "com.example") {
                                violations.add("error")
                            }
                        }
                    },
                )
        val assertionAll = ruleAll.getShouldAssertion()!!

        val vAAll = mutableListOf<String>()
        assertionAll(classA, emptyList(), vAAll)
        assertTrue(vAAll.isEmpty())

        val vCAll = mutableListOf<String>()
        assertionAll(classC, emptyList(), vCAll)
        assertEquals(1, vCAll.size) // fails package check

        val ruleNone =
            ClassesRuleBuilder(projectGraph)
                .should()
                .noneOf(
                    {
                        satisfy { cls, violations ->
                            if (cls.name.endsWith("X")) {
                                violations.add("error")
                            }
                        }
                    },
                )
        val assertionNone = ruleNone.getShouldAssertion()!!

        val vANone = mutableListOf<String>()
        assertionNone(classA, emptyList(), vANone)
        assertEquals(1, vANone.size) // satisfy did NOT fail, so noneOf fails (violates rule)
    }

    @Test
    fun `test ClassesShould additions`() {
        // test haveNameMatching glob assertions
        val ruleNameGlob = ClassesRuleBuilder(projectGraph).should().haveNameMatching("Class*")
        val assertNameGlob = ruleNameGlob.getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertNameGlob(classA, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val v2 = mutableListOf<String>()
        val classX = classA.copy(name = "NotMatching")
        assertNameGlob(classX, emptyList(), v2)
        assertEquals(1, v2.size)

        // test haveAnnotationOf assertion
        val ruleAnno = ClassesRuleBuilder(projectGraph).should().haveAnnotationOf("MyAnnotation")
        val assertAnno = ruleAnno.getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertAnno(classB, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val v4 = mutableListOf<String>()
        assertAnno(classA, emptyList(), v4)
        assertEquals(1, v4.size)

        // test beInline and haveModifier assertions
        val inlineClass =
            ClassDeclaration(
                name = "InlineClass",
                fqName = "com.example.InlineClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/InlineClass.kt",
                modifiers = setOf(Modifier.INLINE),
            )
        val valueClass =
            ClassDeclaration(
                name = "ValueClass",
                fqName = "com.example.ValueClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ValueClass.kt",
                modifiers = setOf(Modifier.VALUE),
            )
        val ruleInline = ClassesRuleBuilder(projectGraph).should().beInline()
        val assertInline = ruleInline.getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertInline(inlineClass, emptyList(), v5)
        assertTrue(v5.isEmpty())

        val v5b = mutableListOf<String>()
        assertInline(valueClass, emptyList(), v5b)
        assertTrue(v5b.isEmpty())

        val v6 = mutableListOf<String>()
        assertInline(classA, emptyList(), v6)
        assertEquals(1, v6.size)

        val ruleModifier = ClassesRuleBuilder(projectGraph).should().haveModifier(Modifier.ABSTRACT)
        val assertModifier = ruleModifier.getShouldAssertion()!!
        val v7 = mutableListOf<String>()
        assertModifier(classC, emptyList(), v7)
        assertTrue(v7.isEmpty())

        val v8 = mutableListOf<String>()
        assertModifier(classA, emptyList(), v8)
        assertEquals(1, v8.size)
    }

    @Test
    fun `test additional classes modifiers should`() {
        val classSealed =
            ClassDeclaration(
                name = "MySealed",
                fqName = "com.example.MySealed",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MySealed.kt",
                modifiers = setOf(Modifier.SEALED),
            )
        val classData =
            ClassDeclaration(
                name = "MyData",
                fqName = "com.example.MyData",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyData.kt",
                modifiers = setOf(Modifier.DATA),
            )
        val classPrivate =
            ClassDeclaration(
                name = "MyPrivate",
                fqName = "com.example.MyPrivate",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyPrivate.kt",
                visibility = Visibility.PRIVATE,
            )

        val ruleShouldSealed = ClassesRuleBuilder(projectGraph).should().beSealed()
        val assertSealed = ruleShouldSealed.getShouldAssertion()!!
        val vSealed = mutableListOf<String>()
        assertSealed(classSealed, emptyList(), vSealed)
        assertTrue(vSealed.isEmpty())

        assertSealed(classData, emptyList(), vSealed)
        assertEquals(1, vSealed.size)

        val ruleShouldData = ClassesRuleBuilder(projectGraph).should().beData()
        val assertData = ruleShouldData.getShouldAssertion()!!
        val vData = mutableListOf<String>()
        assertData(classData, emptyList(), vData)
        assertTrue(vData.isEmpty())

        assertData(classSealed, emptyList(), vData)
        assertEquals(1, vData.size)

        val ruleShouldAbstract = ClassesRuleBuilder(projectGraph).should().beAbstract()
        val assertAbstract = ruleShouldAbstract.getShouldAssertion()!!
        val vAbstract = mutableListOf<String>()
        assertAbstract(classC, emptyList(), vAbstract)
        assertTrue(vAbstract.isEmpty())

        val vAbstractB = mutableListOf<String>()
        assertAbstract(classB, emptyList(), vAbstractB)
        assertTrue(vAbstractB.isEmpty()) // Interfaces are abstract too

        val vAbstractA = mutableListOf<String>()
        assertAbstract(classA, emptyList(), vAbstractA)
        assertEquals(1, vAbstractA.size)

        val ruleShouldPrivate = ClassesRuleBuilder(projectGraph).should().bePrivate()
        val assertPrivate = ruleShouldPrivate.getShouldAssertion()!!
        val vPrivate = mutableListOf<String>()
        assertPrivate(classPrivate, emptyList(), vPrivate)
        assertTrue(vPrivate.isEmpty())

        assertPrivate(classA, emptyList(), vPrivate)
        assertEquals(1, vPrivate.size)
    }

    @Test
    fun `test class dependencies and access constraints`() {
        val classUser =
            ClassDeclaration(
                name = "User",
                fqName = "com.example.User",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = setOf("com.example.ClassA", "com.other.ClassC"),
                filePath = "/src/User.kt",
            )

        // 1. onlyDependOnClassesInAnyPackage
        val ruleDeps = ClassesRuleBuilder(projectGraph).should().onlyDependOnClassesInAnyPackage("com.example..")
        val assertDeps = ruleDeps.getShouldAssertion()!!
        val vDeps = mutableListOf<String>()
        assertDeps(classUser, listOf(classA, classC), vDeps)
        assertEquals(1, vDeps.size) // referenced com.other.ClassC which is outside "com.example.."
        assertTrue(vDeps[0].contains("depends on com.other.ClassC"))

        // 2. onlyBeAccessedByAnyPackage
        val ruleAccess = ClassesRuleBuilder(projectGraph).should().onlyBeAccessedByAnyPackage("com.internal..")
        val assertAccess = ruleAccess.getShouldAssertion()!!
        val vAccess = mutableListOf<String>()
        assertAccess(classA, listOf(classUser), vAccess) // classUser (com.example) accesses classA
        assertEquals(1, vAccess.size)
        assertTrue(vAccess[0].contains("is accessed by com.example.User"))

        // 3. notDependOnClassesInAnyPackage
        val ruleNoDep = ClassesRuleBuilder(projectGraph).should().notDependOnClassesInAnyPackage("com.other..")
        val assertNoDep = ruleNoDep.getShouldAssertion()!!
        val vNoDep = mutableListOf<String>()
        assertNoDep(classUser, listOf(classA, classC), vNoDep)
        assertEquals(1, vNoDep.size)
        assertTrue(vNoDep[0].contains("depends on com.other.ClassC"))

        val vNoDepPass = mutableListOf<String>()
        val classClean = classUser.copy(referencedTypes = setOf("com.example.ClassA"))
        assertNoDep(classClean, listOf(classA, classC), vNoDepPass)
        assertTrue(vNoDepPass.isEmpty())
    }

    @Test
    fun `test notHaveSignaturesWithTypesAnnotatedWith`() {
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
        val serviceWithEntitySignature =
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
                properties =
                    listOf(
                        PropertyDeclaration(
                            name = "entity",
                            visibility = Visibility.PUBLIC,
                            modifiers = emptySet(),
                            type = "UserEntity",
                            isVal = true,
                            annotations = emptyList(),
                            kdocText = null,
                        ),
                    ),
            )

        val rule =
            ClassesRuleBuilder(projectGraph)
                .should()
                .notHaveSignaturesWithTypesAnnotatedWith("jakarta.persistence.Entity")
        val assertion = rule.getShouldAssertion()!!
        val violations = mutableListOf<String>()
        assertion(serviceWithEntitySignature, listOf(entityClass, serviceWithEntitySignature), violations)
        assertEquals(1, violations.size)
        assertTrue(violations[0].contains("UserEntity"))
        assertTrue(violations[0].contains("Entity"))
    }

    @Test
    fun `test classes rule builder multi-parameter rules should`() {
        val classMulti =
            ClassDeclaration(
                name = "MultiClass",
                fqName = "com.example.MultiClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration("A1", "com.example.A1"),
                        AnnotationDeclaration("A2", "com.example.A2"),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MultiClass.kt",
                modifiers = setOf(Modifier.SEALED, Modifier.DATA),
                visibility = Visibility.INTERNAL,
            )

        val assertAllAnno =
            ClassesRuleBuilder(
                projectGraph,
            ).should().haveAllAnnotationsOf("A1", "A2").getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertAllAnno(classMulti, emptyList(), v1)
        assertTrue(v1.isEmpty())

        val assertNotAnno =
            ClassesRuleBuilder(
                projectGraph,
            ).should().haveAllAnnotationsOf("A1", "A3").getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertNotAnno(classMulti, emptyList(), v2)
        assertEquals(1, v2.size)

        val assertAnyVis =
            ClassesRuleBuilder(
                projectGraph,
            ).should().haveAnyVisibility(Visibility.INTERNAL, Visibility.PRIVATE).getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertAnyVis(classMulti, emptyList(), v3)
        assertTrue(v3.isEmpty())

        val assertNotVis =
            ClassesRuleBuilder(
                projectGraph,
            ).should().haveAnyVisibility(Visibility.PUBLIC).getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertNotVis(classMulti, emptyList(), v4)
        assertEquals(1, v4.size)
    }

    @Test
    fun `test classes should remaining assertions`() {
        val testClass =
            ClassDeclaration(
                name = "TestClass",
                fqName = "com.test.TestClass",
                packageName = "com.test",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            "MyAnno",
                            "com.test.MyAnno",
                            arguments =
                                listOf(
                                    AnnotationArgumentDeclaration("value", "foo"),
                                    AnnotationArgumentDeclaration("num", "42"),
                                ),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/TestClass.kt",
                modifiers = setOf(Modifier.DATA, Modifier.INLINE),
                visibility = Visibility.PROTECTED,
                supertypes = listOf("com.test.SuperInterface", "com.test.SuperClass"),
                kdocText = "Hello KDoc",
            )

        fun builder() = ClassesRuleBuilder(projectGraph)

        // satisfy
        val assertSatisfyPred = builder().should().satisfy { it.name == "TestClass" }.getShouldAssertion()!!
        val vSat1 = mutableListOf<String>()
        assertSatisfyPred(testClass, emptyList(), vSat1)
        assertTrue(vSat1.isEmpty())

        val assertSatisfyPredFail = builder().should().satisfy { it.name == "Wrong" }.getShouldAssertion()!!
        val vSat2 = mutableListOf<String>()
        assertSatisfyPredFail(testClass, emptyList(), vSat2)
        assertEquals(1, vSat2.size)
        assertTrue(vSat2[0].contains("should satisfy: custom condition"))

        val assertSatisfyDesc = builder().should().satisfy("be awesome").getShouldAssertion()!!
        val vSat3 = mutableListOf<String>()
        assertSatisfyDesc(testClass, emptyList(), vSat3)
        assertEquals(1, vSat3.size)
        assertTrue(vSat3[0].contains("should satisfy: be awesome"))

        // satisfy custom violations builder
        val assertCustomViolations =
            builder()
                .should()
                .satisfy { cls, violations ->
                    if (cls.name != "Different") {
                        violations.add("custom failure")
                    }
                }.getShouldAssertion()!!
        val vSat4 = mutableListOf<String>()
        assertCustomViolations(testClass, emptyList(), vSat4)
        assertEquals(1, vSat4.size)
        assertEquals("custom failure", vSat4[0])

        // name starts / ends / residesInAPackage list and predicate
        val assertStart = builder().should().haveNameStartingWith("Test").getShouldAssertion()!!
        val vSt1 = mutableListOf<String>()
        assertStart(testClass, emptyList(), vSt1)
        assertTrue(vSt1.isEmpty())

        val assertStartFail = builder().should().haveNameStartingWith("Wrong").getShouldAssertion()!!
        val vSt2 = mutableListOf<String>()
        assertStartFail(testClass, emptyList(), vSt2)
        assertEquals(1, vSt2.size)

        val assertEnd = builder().should().haveNameEndingWith("Class").getShouldAssertion()!!
        val vEd1 = mutableListOf<String>()
        assertEnd(testClass, emptyList(), vEd1)
        assertTrue(vEd1.isEmpty())

        val assertEndFail = builder().should().haveNameEndingWith("Wrong").getShouldAssertion()!!
        val vEd2 = mutableListOf<String>()
        assertEndFail(testClass, emptyList(), vEd2)
        assertEquals(1, vEd2.size)

        val assertPkgPred = builder().should().resideInAPackage { it.startsWith("com.t") }.getShouldAssertion()!!
        val vPk1 = mutableListOf<String>()
        assertPkgPred(testClass, emptyList(), vPk1)
        assertTrue(vPk1.isEmpty())

        val assertPkgPredFail =
            builder()
                .should()
                .resideInAPackage {
                    it.startsWith(
                        "com.other",
                    )
                }.getShouldAssertion()!!
        val vPk2 = mutableListOf<String>()
        assertPkgPredFail(testClass, emptyList(), vPk2)
        assertEquals(1, vPk2.size)

        // haveName with predicate
        val assertNamePred = builder().should().haveName { it.length == 9 }.getShouldAssertion()!!
        val vNm1 = mutableListOf<String>()
        assertNamePred(testClass, emptyList(), vNm1)
        assertTrue(vNm1.isEmpty())

        val assertNamePredFail = builder().should().haveName("be 5 chars") { it.length == 5 }.getShouldAssertion()!!
        val vNm2 = mutableListOf<String>()
        assertNamePredFail(testClass, emptyList(), vNm2)
        assertEquals(1, vNm2.size)
        assertTrue(vNm2[0].contains("should have name matching: be 5 chars"))

        // haveAllAnnotationsOf
        val assertAllAnnosList = builder().should().haveAllAnnotationsOf(listOf("MyAnno")).getShouldAssertion()!!
        val vAn1 = mutableListOf<String>()
        assertAllAnnosList(testClass, emptyList(), vAn1)
        assertTrue(vAn1.isEmpty())

        val assertAllAnnosListFail =
            builder()
                .should()
                .haveAllAnnotationsOf(
                    listOf("MyAnno", "OtherAnno"),
                ).getShouldAssertion()!!
        val vAn2 = mutableListOf<String>()
        assertAllAnnosListFail(testClass, emptyList(), vAn2)
        assertEquals(1, vAn2.size)

        // haveAnyAnnotationOf
        val assertAnyAnnosList =
            builder()
                .should()
                .haveAnyAnnotationOf(
                    listOf("MyAnno", "OtherAnno"),
                ).getShouldAssertion()!!
        val vAn3 = mutableListOf<String>()
        assertAnyAnnosList(testClass, emptyList(), vAn3)
        assertTrue(vAn3.isEmpty())

        val assertAnyAnnosVararg = builder().should().haveAnyAnnotationOf("Other1", "Other2").getShouldAssertion()!!
        val vAn4 = mutableListOf<String>()
        assertAnyAnnosVararg(testClass, emptyList(), vAn4)
        assertEquals(1, vAn4.size)

        val assertAnyAnnosListFail = builder().should().haveAnyAnnotationOf(listOf("OtherAnno")).getShouldAssertion()!!
        val vAn5 = mutableListOf<String>()
        assertAnyAnnosListFail(testClass, emptyList(), vAn5)
        assertEquals(1, vAn5.size)

        // haveAnnotationWithArgument
        val assertArg1 = builder().should().haveAnnotationWithArgument("MyAnno", "value", "foo").getShouldAssertion()!!
        val vArg1 = mutableListOf<String>()
        assertArg1(testClass, emptyList(), vArg1)
        assertTrue(vArg1.isEmpty())

        val assertArg2 = builder().should().haveAnnotationWithArgument("MyAnno", "num", "42").getShouldAssertion()!!
        val vArg2 = mutableListOf<String>()
        assertArg2(testClass, emptyList(), vArg2)
        assertTrue(vArg2.isEmpty())

        val assertArgFailVal =
            builder()
                .should()
                .haveAnnotationWithArgument(
                    "MyAnno",
                    "value",
                    "bar",
                ).getShouldAssertion()!!
        val vArg3 = mutableListOf<String>()
        assertArgFailVal(testClass, emptyList(), vArg3)
        assertEquals(1, vArg3.size)

        val assertArgFailName =
            builder()
                .should()
                .haveAnnotationWithArgument(
                    "MyAnno",
                    "wrongArg",
                    "foo",
                ).getShouldAssertion()!!
        val vArg4 = mutableListOf<String>()
        assertArgFailName(testClass, emptyList(), vArg4)
        assertEquals(1, vArg4.size)

        val assertArgFailAnno =
            builder()
                .should()
                .haveAnnotationWithArgument(
                    "WrongAnno",
                    "value",
                    "foo",
                ).getShouldAssertion()!!
        val vArg5 = mutableListOf<String>()
        assertArgFailAnno(testClass, emptyList(), vArg5)
        assertEquals(1, vArg5.size)

        // beInterfaces, beEnums, beAbstract, beSealed, beData, beInline
        val assertInterfaces = builder().should().beInterfaces().getShouldAssertion()!!
        val vBi = mutableListOf<String>()
        assertInterfaces(testClass, emptyList(), vBi)
        assertEquals(1, vBi.size)

        val assertEnums = builder().should().beEnums().getShouldAssertion()!!
        val vBe = mutableListOf<String>()
        assertEnums(testClass, emptyList(), vBe)
        assertEquals(1, vBe.size)

        val assertAbstract = builder().should().beAbstract().getShouldAssertion()!!
        val vBab = mutableListOf<String>()
        assertAbstract(testClass, emptyList(), vBab)
        assertEquals(1, vBab.size)

        val assertSealed = builder().should().beSealed().getShouldAssertion()!!
        val vBse = mutableListOf<String>()
        assertSealed(testClass, emptyList(), vBse)
        assertEquals(1, vBse.size)

        val assertData = builder().should().beData().getShouldAssertion()!!
        val vBd = mutableListOf<String>()
        assertData(testClass, emptyList(), vBd)
        assertTrue(vBd.isEmpty())

        val assertInline = builder().should().beInline().getShouldAssertion()!!
        val vBin = mutableListOf<String>()
        assertInline(testClass, emptyList(), vBin)
        assertTrue(vBin.isEmpty())

        // haveModifier, haveAllModifiers, haveAnyModifier
        val assertMod1 = builder().should().haveModifier(Modifier.DATA).getShouldAssertion()!!
        val vMod1 = mutableListOf<String>()
        assertMod1(testClass, emptyList(), vMod1)
        assertTrue(vMod1.isEmpty())

        val assertMod1Fail = builder().should().haveModifier(Modifier.ABSTRACT).getShouldAssertion()!!
        val vMod2 = mutableListOf<String>()
        assertMod1Fail(testClass, emptyList(), vMod2)
        assertEquals(1, vMod2.size)

        val assertAllModsList =
            builder()
                .should()
                .haveAllModifiers(
                    listOf(Modifier.DATA, Modifier.INLINE),
                ).getShouldAssertion()!!
        val vMod3 = mutableListOf<String>()
        assertAllModsList(testClass, emptyList(), vMod3)
        assertTrue(vMod3.isEmpty())

        val assertAllModsListFail =
            builder()
                .should()
                .haveAllModifiers(
                    listOf(Modifier.DATA, Modifier.ABSTRACT),
                ).getShouldAssertion()!!
        val vMod4 = mutableListOf<String>()
        assertAllModsListFail(testClass, emptyList(), vMod4)
        assertEquals(1, vMod4.size)

        val assertAnyModsList =
            builder()
                .should()
                .haveAnyModifier(
                    listOf(Modifier.ABSTRACT, Modifier.INLINE),
                ).getShouldAssertion()!!
        val vMod5 = mutableListOf<String>()
        assertAnyModsList(testClass, emptyList(), vMod5)
        assertTrue(vMod5.isEmpty())

        val assertAnyModsListFail =
            builder()
                .should()
                .haveAnyModifier(
                    listOf(Modifier.ABSTRACT, Modifier.SEALED),
                ).getShouldAssertion()!!
        val vMod6 = mutableListOf<String>()
        assertAnyModsListFail(testClass, emptyList(), vMod6)
        assertEquals(1, vMod6.size)

        // haveVisibility, haveAnyVisibility
        val assertVis = builder().should().haveVisibility(Visibility.PROTECTED).getShouldAssertion()!!
        val vVis1 = mutableListOf<String>()
        assertVis(testClass, emptyList(), vVis1)
        assertTrue(vVis1.isEmpty())

        val assertVisFail = builder().should().haveVisibility(Visibility.PUBLIC).getShouldAssertion()!!
        val vVis2 = mutableListOf<String>()
        assertVisFail(testClass, emptyList(), vVis2)
        assertEquals(1, vVis2.size)

        val assertAnyVisList =
            builder()
                .should()
                .haveAnyVisibility(
                    listOf(Visibility.PROTECTED, Visibility.PUBLIC),
                ).getShouldAssertion()!!
        val vVis3 = mutableListOf<String>()
        assertAnyVisList(testClass, emptyList(), vVis3)
        assertTrue(vVis3.isEmpty())

        val assertAnyVisListFail =
            builder()
                .should()
                .haveAnyVisibility(
                    listOf(Visibility.PRIVATE, Visibility.PUBLIC),
                ).getShouldAssertion()!!
        val vVis4 = mutableListOf<String>()
        assertAnyVisListFail(testClass, emptyList(), vVis4)
        assertEquals(1, vVis4.size)

        // bePublic, beInternal, bePrivate, beProtected
        assertTrue(builder().should().beProtected().getShouldAssertion() != null)
        assertTrue(builder().should().bePublic().getShouldAssertion() != null)
        assertTrue(builder().should().beInternal().getShouldAssertion() != null)
        assertTrue(builder().should().bePrivate().getShouldAssertion() != null)

        // beAssignableTo, beAssignableToAnyOf, beAssignableToAllOf
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

        // Transitive assignability checks
        val grandParent = ClassDeclaration(
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
        val parent = ClassDeclaration(
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
        val child = ClassDeclaration(
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

        val assertAssAnyTransitive = builder().should().beAssignableToAnyOf("GrandParent", "WrongType").getShouldAssertion()!!
        val vAssAnyTransitive = mutableListOf<String>()
        assertAssAnyTransitive(child, allHierarchy, vAssAnyTransitive)
        assertTrue(vAssAnyTransitive.isEmpty())

        val assertAssAllTransitive = builder().should().beAssignableToAllOf("GrandParent", "Parent").getShouldAssertion()!!
        val vAssAllTransitive = mutableListOf<String>()
        assertAssAllTransitive(child, allHierarchy, vAssAllTransitive)
        assertTrue(vAssAllTransitive.isEmpty())

        // beDocumentedWithKDoc
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

        // onlyBeAccessedByAnyPackage
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

        // onlyDependOnClassesInAnyPackage
        val assertDep = builder().should().onlyDependOnClassesInAnyPackage("com.other").getShouldAssertion()!!
        val vDep1 = mutableListOf<String>()
        assertDep(testClass, listOf(testClass, accessorClass), vDep1)
        assertTrue(vDep1.isEmpty()) // testClass does not depend on anything in allClasses

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

        // allFunctions
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
        assertEquals(1, vFn.size) // fails beInline()
        assertTrue(vFn[0].contains("Function myFunc in class com.test.TestClass has violations"))

        // allProperties
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
