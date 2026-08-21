/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LongMethod")

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ClassesBranchCoverageTest : RuleBuildersTestBase() {
    private fun createClass(
        name: String,
        fqName: String,
        packageName: String,
        isInterface: Boolean = false,
        isAbstract: Boolean = false,
        annotations: List<AnnotationDeclaration> = emptyList(),
        imports: List<String> = emptyList(),
        referencedTypes: Set<String> = emptySet(),
        filePath: String = "/src/$name.kt",
        supertypes: List<String> = emptyList(),
        importAliases: Map<String, String> = emptyMap(),
        properties: List<PropertyDeclaration> = emptyList(),
        functions: List<FunctionDeclaration> = emptyList(),
        primaryConstructor: ConstructorDeclaration? = null,
        secondaryConstructors: List<ConstructorDeclaration> = emptyList(),
    ): ClassDeclaration =
        ClassDeclaration(
            name = name,
            fqName = fqName,
            packageName = packageName,
            isInterface = isInterface,
            isAbstract = isAbstract,
            annotations = annotations,
            imports = imports,
            referencedTypes = referencedTypes,
            filePath = filePath,
            supertypes = supertypes,
            importAliases = importAliases,
            properties = properties,
            functions = functions,
            primaryConstructor = primaryConstructor,
            secondaryConstructors = secondaryConstructors,
        )

    @Test
    fun `test ClassDeclarationExtensions all branch cases`() {
        val baseInterface =
            createClass(
                name = "BaseInterface",
                fqName = "com.example.BaseInterface",
                packageName = "com.example",
                isInterface = true,
                filePath = "/src/BaseInterface.kt",
            )
        val middleClass =
            createClass(
                name = "MiddleClass",
                fqName = "com.example.MiddleClass",
                packageName = "com.example",
                isAbstract = true,
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                imports = listOf("com.example.BaseInterface"),
                supertypes = listOf("BaseInterface"),
                referencedTypes = setOf("BaseInterface"),
                filePath = "/src/MiddleClass.kt",
            )
        val implClass =
            createClass(
                name = "ImplClass",
                fqName = "com.other.ImplClass",
                packageName = "com.other",
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                imports = listOf("com.example.MiddleClass", "com.example.*", "java.util.List"),
                supertypes = listOf("MiddleClass"),
                referencedTypes = setOf("MiddleClass", "com.example.BaseInterface"),
                importAliases = mapOf("AliasMiddle" to "com.example.MiddleClass"),
                filePath = "/src/ImplClass.kt",
                properties =
                    listOf(
                        PropertyDeclaration(
                            name = "items",
                            visibility = Visibility.PUBLIC,
                            modifiers = emptySet(),
                            type = "java.util.List<com.example.BaseInterface>",
                            isVal = true,
                            annotations = emptyList(),
                            kdocText = null,
                        ),
                    ),
                functions =
                    listOf(
                        FunctionDeclaration(
                            name = "process",
                            visibility = Visibility.PUBLIC,
                            modifiers = emptySet(),
                            returnType = "com.example.BaseInterface?",
                            parameters = listOf(ParameterDeclaration("item", "MiddleClass", false, emptyList())),
                            annotations = emptyList(),
                            kdocText = null,
                            isExtension = false,
                        ),
                    ),
                primaryConstructor =
                    ConstructorDeclaration(
                        visibility = Visibility.PUBLIC,
                        parameters = listOf(ParameterDeclaration("init", "String", false, emptyList())),
                        annotations = emptyList(),
                    ),
                secondaryConstructors =
                    listOf(
                        ConstructorDeclaration(
                            visibility = Visibility.PUBLIC,
                            parameters = listOf(ParameterDeclaration("alt", "Int", false, emptyList())),
                            annotations = emptyList(),
                        ),
                    ),
            )

        val allClasses = listOf(baseInterface, middleClass, implClass)

        // isAssignableTo branches
        assertTrue(implClass.isAssignableTo("MiddleClass", allClasses))
        assertTrue(implClass.isAssignableTo("com.example.MiddleClass", allClasses))
        assertTrue(implClass.isAssignableTo("BaseInterface", allClasses))
        assertTrue(implClass.isAssignableTo("com.example.BaseInterface", allClasses))
        assertFalse(implClass.isAssignableTo("NonExistent", allClasses))
        assertFalse(implClass.isAssignableTo("com.other.NonExistent", allClasses))

        // resolveTypeReference branches
        assertEquals(middleClass, implClass.resolveTypeReference("com.example.MiddleClass", allClasses))
        assertEquals(middleClass, implClass.resolveTypeReference("MiddleClass", allClasses))
        assertEquals(middleClass, implClass.resolveTypeReference("AliasMiddle", allClasses))
        assertEquals(baseInterface, middleClass.resolveTypeReference("BaseInterface", allClasses))
        assertNull(implClass.resolveTypeReference("", allClasses))
        assertNull(implClass.resolveTypeReference("UnknownType", allClasses))

        // collectSignatureTypeNames
        val sigNames = implClass.collectSignatureTypeNames()
        assertTrue(sigNames.contains("java.util.List"))
        assertTrue(sigNames.contains("com.example.BaseInterface"))
        assertTrue(sigNames.contains("MiddleClass"))
        assertTrue(sigNames.contains("String"))
        assertTrue(sigNames.contains("Int"))

        // collectDependencyPackages branches
        val depPkgs = implClass.collectDependencyPackages(allClasses)
        assertTrue(depPkgs.contains("com.example"))
        assertTrue(depPkgs.contains("java.util"))

        // dependsOn branches
        assertFalse(implClass.dependsOn(implClass)) // same class
        assertTrue(implClass.dependsOn(middleClass))
        assertTrue(implClass.dependsOn(baseInterface))

        // matchesName on AnnotationDeclaration
        val ann = AnnotationDeclaration("Table", "androidx.room.Table")
        assertTrue(ann.matchesName("Table"))
        assertTrue(ann.matchesName("androidx.room.Table"))
        assertTrue(ann.matchesName("room.Table"))
        assertFalse(ann.matchesName("Entity"))
    }

    @Test
    fun `test ClassesShouldDependencyAssertions cycle detection and package assertions`() {
        val classA =
            createClass(
                name = "ClassA",
                fqName = "com.pkgA.ClassA",
                packageName = "com.pkgA",
                referencedTypes = setOf("com.pkgB.ClassB"),
                imports = listOf("com.pkgB.ClassB"),
                filePath = "/src/ClassA.kt",
            )
        val classB =
            createClass(
                name = "ClassB",
                fqName = "com.pkgB.ClassB",
                packageName = "com.pkgB",
                referencedTypes = setOf("com.pkgA.ClassA"),
                imports = listOf("com.pkgA.ClassA"),
                filePath = "/src/ClassB.kt",
            )
        val fileA = FileDeclaration("ClassA.kt", "com.pkgA", classes = listOf(classA), filePath = "/src/ClassA.kt")
        val fileB = FileDeclaration("ClassB.kt", "com.pkgB", classes = listOf(classB), filePath = "/src/ClassB.kt")
        val modA = Module(":", ":moduleA", "moduleA", emptyList(), emptyList(), emptyList(), listOf(fileA))
        val modB = Module(":", ":moduleB", "moduleB", emptyList(), emptyList(), emptyList(), listOf(fileB))
        val graph = ProjectGraph(mapOf(":" to listOf(modA, modB)))
        val allClasses = listOf(classA, classB)

        // Cycle detection in classes
        val vCycle = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().beFreeOfCycles()
            .getShouldAssertion()!!(classA, allClasses, vCycle)
        assertEquals(1, vCycle.size)

        // notDependOnClassesInAnyPackage branches
        val vPkgFail = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesInAnyPackage("com.pkgB..")
            .getShouldAssertion()!!(classA, allClasses, vPkgFail)
        assertEquals(1, vPkgFail.size)

        val vPkgPass = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notDependOnClassesInAnyPackage("com.pkgC..")
            .getShouldAssertion()!!(classA, allClasses, vPkgPass)
        assertTrue(vPkgPass.isEmpty())

        // inModule / notInModule branches with colon / no-colon / glob / empty / null
        val vInModPass = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().inModule("moduleA")
            .getShouldAssertion()!!(classA, allClasses, vInModPass)
        assertTrue(vInModPass.isEmpty())

        val vInModFail = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().inModule("moduleB")
            .getShouldAssertion()!!(classA, allClasses, vInModFail)
        assertEquals(1, vInModFail.size)

        val vInModListPass = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().inModule(listOf("moduleA", "moduleB"))
            .getShouldAssertion()!!(classA, allClasses, vInModListPass)
        assertTrue(vInModListPass.isEmpty())

        val vInModListFail = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().inModule(listOf("moduleB", "moduleC"))
            .getShouldAssertion()!!(classA, allClasses, vInModListFail)
        assertEquals(1, vInModListFail.size)

        val vNotInModPass = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notInModule("moduleB")
            .getShouldAssertion()!!(classA, allClasses, vNotInModPass)
        assertTrue(vNotInModPass.isEmpty())

        val vNotInModFail = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notInModule("moduleA")
            .getShouldAssertion()!!(classA, allClasses, vNotInModFail)
        assertEquals(1, vNotInModFail.size)

        val vNotInModListPass = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notInModule(listOf("moduleB", "moduleC"))
            .getShouldAssertion()!!(classA, allClasses, vNotInModListPass)
        assertTrue(vNotInModListPass.isEmpty())

        val vNotInModListFail = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().notInModule(listOf("moduleA", "moduleB"))
            .getShouldAssertion()!!(classA, allClasses, vNotInModListFail)
        assertEquals(1, vNotInModListFail.size)

        // Class not in any module
        val orphanClass = createClass("Orphan", "com.orphan.Orphan", "com.orphan", filePath = "/orphan/Orphan.kt")
        val vOrphan = mutableListOf<String>()
        ClassesRuleBuilder(graph).should().inModule("moduleA")
            .getShouldAssertion()!!(orphanClass, allClasses + orphanClass, vOrphan)
        assertEquals(1, vOrphan.size)
    }

    @Test
    fun `test ClassesThatFilterTraits inModule and notInModule filter branches`() {
        val classA = createClass("ClassA", "com.pkgA.ClassA", "com.pkgA", filePath = "/src/ClassA.kt")
        val classB = createClass("ClassB", "com.pkgB.ClassB", "com.pkgB", filePath = "/src/ClassB.kt")
        val orphan = createClass("Orphan", "com.orphan.Orphan", "com.orphan", filePath = "/orphan/Orphan.kt")

        val fileA = FileDeclaration("ClassA.kt", "com.pkgA", classes = listOf(classA), filePath = "/src/ClassA.kt")
        val fileB = FileDeclaration("ClassB.kt", "com.pkgB", classes = listOf(classB), filePath = "/src/ClassB.kt")
        val modA = Module(":", ":moduleA", "moduleA", emptyList(), emptyList(), emptyList(), listOf(fileA))
        val modB = Module(":", ":moduleB", "moduleB", emptyList(), emptyList(), emptyList(), listOf(fileB))
        val graph = ProjectGraph(mapOf(":" to listOf(modA, modB)))

        // inModule filter branches
        val pInModNoColon = ClassesRuleBuilder(graph).that().inModule("moduleA").getThatPredicate()!!
        assertTrue(pInModNoColon(classA))
        assertFalse(pInModNoColon(classB))
        assertFalse(pInModNoColon(orphan))

        val pInModColon = ClassesRuleBuilder(graph).that().inModule(":moduleA").getThatPredicate()!!
        assertTrue(pInModColon(classA))
        assertFalse(pInModColon(classB))

        val pInModList = ClassesRuleBuilder(graph).that().inModule(listOf("moduleA", "moduleB")).getThatPredicate()!!
        assertTrue(pInModList(classA))
        assertTrue(pInModList(classB))
        assertFalse(pInModList(orphan))

        // notInModule filter branches
        val pNotInModNoColon = ClassesRuleBuilder(graph).that().notInModule("moduleA").getThatPredicate()!!
        assertFalse(pNotInModNoColon(classA))
        assertTrue(pNotInModNoColon(classB))
        assertTrue(pNotInModNoColon(orphan))

        val pNotInModColon = ClassesRuleBuilder(graph).that().notInModule(":moduleA").getThatPredicate()!!
        assertFalse(pNotInModColon(classA))
        assertTrue(pNotInModColon(classB))

        val pNotInModList = ClassesRuleBuilder(graph).that().notInModule(listOf("moduleA")).getThatPredicate()!!
        assertFalse(pNotInModList(classA))
        assertTrue(pNotInModList(classB))
        assertTrue(pNotInModList(orphan))
    }
}
