/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ClassesThatTest : RuleBuildersTestBase() {
    @Test
    fun `test classes rule builder satisfy filtering`() {
        val rule =
            ClassesRuleBuilder(projectGraph)
                .that()
                .satisfy { it.filePath.endsWith("B.kt") }
        val pred = rule.getThatPredicate()!!
        assertFalse(pred(classA))
        assertTrue(pred(classB))
        assertFalse(pred(classC))
    }

    @Test
    fun `test classes rule builder anyOf - allOf - noneOf filtering`() {
        val ruleAny =
            ClassesRuleBuilder(projectGraph)
                .that()
                .anyOf(
                    { haveNameStartingWith("ClassA") },
                    { haveNameStartingWith("ClassC") },
                )
        val predAny = ruleAny.getThatPredicate()!!
        assertTrue(predAny(classA))
        assertFalse(predAny(classB))
        assertTrue(predAny(classC))

        val ruleAll =
            ClassesRuleBuilder(projectGraph)
                .that()
                .allOf(
                    { resideInAPackage("com.example") },
                    { areInterfaces() },
                )
        val predAll = ruleAll.getThatPredicate()!!
        assertFalse(predAll(classA))
        assertTrue(predAll(classB))
        assertFalse(predAll(classC))

        val ruleNone =
            ClassesRuleBuilder(projectGraph)
                .that()
                .noneOf(
                    { haveNameStartingWith("ClassA") },
                    { areInterfaces() },
                )
        val predNone = ruleNone.getThatPredicate()!!
        assertFalse(predNone(classA))
        assertFalse(predNone(classB))
        assertTrue(predNone(classC))
    }

    @Test
    fun `test ClassesThat additions`() {
        // test haveNamePredicate
        val ruleNamePred = ClassesRuleBuilder(projectGraph).that().haveName { it.endsWith("A") }
        val predNamePred = ruleNamePred.getThatPredicate()!!
        assertTrue(predNamePred(classA))
        assertFalse(predNamePred(classB))

        // test haveNameMatching glob
        val ruleNameGlob = ClassesRuleBuilder(projectGraph).that().haveNameMatching("*C")
        val predNameGlob = ruleNameGlob.getThatPredicate()!!
        assertTrue(predNameGlob(classC))
        assertFalse(predNameGlob(classA))

        // test beDocumentedWithKDoc
        val classWithKDoc =
            ClassDeclaration(
                name = "DocClass",
                fqName = "com.example.DocClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/DocClass.kt",
                kdocText = "/** This is a KDoc */",
            )
        val ruleKDoc = ClassesRuleBuilder(projectGraph).that().beDocumentedWithKDoc()
        val predKDoc = ruleKDoc.getThatPredicate()!!
        assertTrue(predKDoc(classWithKDoc))
        assertFalse(predKDoc(classA))
    }

    @Test
    fun `test additional classes modifiers that`() {
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

        val ruleSealed = ClassesRuleBuilder(projectGraph).that().beSealed()
        assertTrue(ruleSealed.getThatPredicate()!!(classSealed))
        assertFalse(ruleSealed.getThatPredicate()!!(classData))

        val ruleData = ClassesRuleBuilder(projectGraph).that().beData()
        assertTrue(ruleData.getThatPredicate()!!(classData))
        assertFalse(ruleData.getThatPredicate()!!(classSealed))

        val ruleAbstract = ClassesRuleBuilder(projectGraph).that().areAbstract()
        assertTrue(ruleAbstract.getThatPredicate()!!(classC))
        assertTrue(ruleAbstract.getThatPredicate()!!(classB)) // Interfaces qualify as abstract
        assertFalse(ruleAbstract.getThatPredicate()!!(classA))

        val classInlineMod =
            ClassDeclaration(
                name = "MyInline",
                fqName = "com.example.MyInline",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyInline.kt",
                modifiers = setOf(Modifier.INLINE),
            )
        val classValueMod =
            ClassDeclaration(
                name = "MyValue",
                fqName = "com.example.MyValue",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyValue.kt",
                modifiers = setOf(Modifier.VALUE),
            )

        val ruleInline = ClassesRuleBuilder(projectGraph).that().beInline()
        assertTrue(ruleInline.getThatPredicate()!!(classInlineMod))
        assertTrue(ruleInline.getThatPredicate()!!(classValueMod))
        assertFalse(ruleInline.getThatPredicate()!!(classA))

        val rulePrivate = ClassesRuleBuilder(projectGraph).that().bePrivate()
        assertTrue(rulePrivate.getThatPredicate()!!(classPrivate))
        assertFalse(rulePrivate.getThatPredicate()!!(classA))
    }

    @Test
    fun `test classes rule builder multi-parameter rules that`() {
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

        val ruleAllAnno = ClassesRuleBuilder(projectGraph).that().haveAllAnnotationsOf("A1", "A2")
        assertTrue(ruleAllAnno.getThatPredicate()!!(classMulti))

        val ruleAnyAnno = ClassesRuleBuilder(projectGraph).that().haveAnyAnnotationOf("A2", "A3")
        assertTrue(ruleAnyAnno.getThatPredicate()!!(classMulti))

        val ruleNotAnno = ClassesRuleBuilder(projectGraph).that().haveAllAnnotationsOf("A1", "A3")
        assertFalse(ruleNotAnno.getThatPredicate()!!(classMulti))

        val ruleAllMod = ClassesRuleBuilder(projectGraph).that().haveAllModifiers(Modifier.SEALED, Modifier.DATA)
        assertTrue(ruleAllMod.getThatPredicate()!!(classMulti))

        val ruleAnyMod = ClassesRuleBuilder(projectGraph).that().haveAnyModifier(Modifier.DATA, Modifier.INLINE)
        assertTrue(ruleAnyMod.getThatPredicate()!!(classMulti))

        val ruleNotMod = ClassesRuleBuilder(projectGraph).that().haveAllModifiers(Modifier.SEALED, Modifier.INLINE)
        assertFalse(ruleNotMod.getThatPredicate()!!(classMulti))

        val ruleAnyVis =
            ClassesRuleBuilder(
                projectGraph,
            ).that().haveAnyVisibility(Visibility.INTERNAL, Visibility.PRIVATE)
        assertTrue(ruleAnyVis.getThatPredicate()!!(classMulti))

        val ruleNotVis =
            ClassesRuleBuilder(
                projectGraph,
            ).that().haveAnyVisibility(Visibility.PUBLIC, Visibility.PRIVATE)
        assertFalse(ruleNotVis.getThatPredicate()!!(classMulti))

        // List-based overloads
        val ruleAllAnnoList = ClassesRuleBuilder(projectGraph).that().haveAllAnnotationsOf(listOf("A1", "A2"))
        assertTrue(ruleAllAnnoList.getThatPredicate()!!(classMulti))

        val ruleAnyAnnoList = ClassesRuleBuilder(projectGraph).that().haveAnyAnnotationOf(listOf("A2", "A3"))
        assertTrue(ruleAnyAnnoList.getThatPredicate()!!(classMulti))

        val ruleAllModList =
            ClassesRuleBuilder(
                projectGraph,
            ).that().haveAllModifiers(listOf(Modifier.SEALED, Modifier.DATA))
        assertTrue(ruleAllModList.getThatPredicate()!!(classMulti))

        val ruleAnyModList =
            ClassesRuleBuilder(
                projectGraph,
            ).that().haveAnyModifier(listOf(Modifier.DATA, Modifier.INLINE))
        assertTrue(ruleAnyModList.getThatPredicate()!!(classMulti))

        val ruleAnyVisList =
            ClassesRuleBuilder(
                projectGraph,
            ).that().haveAnyVisibility(listOf(Visibility.INTERNAL, Visibility.PRIVATE))
        assertTrue(ruleAnyVisList.getThatPredicate()!!(classMulti))

        // areInterfaces, areEnums, haveAnnotationWithArgument
        val classInterface =
            ClassDeclaration(
                name = "MyInterface",
                fqName = "com.example.MyInterface",
                packageName = "com.example",
                isInterface = true,
                isAbstract = true,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyInterface.kt",
            )
        val classEnum =
            ClassDeclaration(
                name = "MyEnum",
                fqName = "com.example.MyEnum",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyEnum.kt",
                isEnum = true,
            )
        val classWithAnnoArg =
            ClassDeclaration(
                name = "WithAnno",
                fqName = "com.example.WithAnno",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations =
                    listOf(
                        AnnotationDeclaration(
                            name = "MyAnno",
                            fqName = "com.example.MyAnno",
                            arguments =
                                listOf(
                                    AnnotationArgumentDeclaration("value", "foo"),
                                    AnnotationArgumentDeclaration("num", "100"),
                                ),
                        ),
                    ),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/WithAnno.kt",
            )

        assertTrue(ClassesRuleBuilder(projectGraph).that().areInterfaces().getThatPredicate()!!(classInterface))
        assertFalse(ClassesRuleBuilder(projectGraph).that().areInterfaces().getThatPredicate()!!(classEnum))

        assertTrue(ClassesRuleBuilder(projectGraph).that().areEnums().getThatPredicate()!!(classEnum))
        assertFalse(ClassesRuleBuilder(projectGraph).that().areEnums().getThatPredicate()!!(classInterface))

        // haveAnnotationWithArgument checks
        val ruleAnnoArgOk = ClassesRuleBuilder(projectGraph).that().haveAnnotationWithArgument("MyAnno", "value", "foo")
        assertTrue(ruleAnnoArgOk.getThatPredicate()!!(classWithAnnoArg))

        val ruleAnnoArgNullNameOk =
            ClassesRuleBuilder(
                projectGraph,
            ).that().haveAnnotationWithArgument("MyAnno", null, "100")
        assertTrue(ruleAnnoArgNullNameOk.getThatPredicate()!!(classWithAnnoArg))

        val ruleAnnoArgWrongValue =
            ClassesRuleBuilder(
                projectGraph,
            ).that().haveAnnotationWithArgument("MyAnno", "value", "bar")
        assertFalse(ruleAnnoArgWrongValue.getThatPredicate()!!(classWithAnnoArg))

        val ruleAnnoArgWrongName =
            ClassesRuleBuilder(
                projectGraph,
            ).that().haveAnnotationWithArgument("MyAnno", "wrong", "foo")
        assertFalse(ruleAnnoArgWrongName.getThatPredicate()!!(classWithAnnoArg))

        // areAssignableTo checks
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

        val fileGrandParent = FileDeclaration("GrandParent.kt", "com.example", classes = listOf(grandParent))
        val fileParent = FileDeclaration("Parent.kt", "com.example", classes = listOf(parent))
        val fileChild = FileDeclaration("Child.kt", "com.example", classes = listOf(child))

        val hierarchyModule =
            Module(
                buildId = ":",
                path = ":hierarchy",
                projectDir = "hierarchy",
                appliedPlugins = listOf("kotlin"),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(fileGrandParent, fileParent, fileChild),
            )
        val hierarchyGraph =
            ProjectGraph(
                builds = mapOf(":" to listOf(hierarchyModule)),
            )

        val ruleAssign = ClassesRuleBuilder(hierarchyGraph).that().areAssignableTo("GrandParent")
        val predAssign = ruleAssign.getThatPredicate()!!
        assertTrue(predAssign(child))
        assertTrue(predAssign(parent))
        assertFalse(predAssign(grandParent))

        val ruleAssignAny = ClassesRuleBuilder(hierarchyGraph).that().areAssignableToAnyOf("GrandParent", "NonExistent")
        assertTrue(ruleAssignAny.getThatPredicate()!!(child))

        val ruleAssignAll = ClassesRuleBuilder(hierarchyGraph).that().areAssignableToAllOf("Parent", "GrandParent")
        assertTrue(ruleAssignAll.getThatPredicate()!!(child))
        assertFalse(ruleAssignAll.getThatPredicate()!!(parent))
    }
}
