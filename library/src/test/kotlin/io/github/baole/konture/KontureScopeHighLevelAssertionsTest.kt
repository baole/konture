/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KontureScopeHighLevelAssertionsTest : KontureScopeTestFixture() {
    @Test
    fun `test High-level package, naming, and annotation assertions`() {
        // --- assertResideInAPackage ---
        listOf(classA, classB).assertResideInAPackage("..example..", "..service..")
        KontureScope(listOf(classA, classB)).assertResideInAPackage("..example..")

        assertThrows<AssertionError> {
            listOf(classA, classC).assertResideInAPackage("..example..")
        }
        assertThrows<AssertionError> {
            KontureScope(listOf(classA, classC)).assertResideInAPackage("..example..")
        }

        // --- assertNameEndingWith ---
        listOf(classA, classWithKdoc).assertNameEndingWith("ClassA", "Kdoc")
        KontureScope(listOf(classA, classWithKdoc)).assertNameEndingWith("ClassA", "Kdoc")

        assertThrows<AssertionError> {
            listOf(classA, classB).assertNameEndingWith("ClassA")
        }

        // --- assertNameStartingWith ---
        listOf(classA, classWithKdoc).assertNameStartingWith("Class")
        KontureScope(listOf(classA, classWithKdoc)).assertNameStartingWith("Class")

        assertThrows<AssertionError> {
            listOf(classA, classB).assertNameStartingWith("ClassA")
        }

        // --- assertNameMatching ---
        listOf(classA, classB).assertNameMatching("*A", "*B")
        KontureScope(listOf(classA, classB)).assertNameMatching("*A", "*B")

        assertThrows<AssertionError> {
            listOf(classA, classB).assertNameMatching("*A")
        }

        // --- assertHaveAnnotationOf ---
        listOf(classAnnotated).assertHaveAnnotationOf("com.example.MyAnnotation")
        listOf(classAnnotated).assertHaveAnnotationOf("MyAnnotation")
        KontureScope(listOf(classAnnotated)).assertHaveAnnotationOf("MyAnnotation")

        assertThrows<AssertionError> {
            listOf(classA).assertHaveAnnotationOf("MyAnnotation")
        }
    }

    @Test
    fun `test High-level structural assertions`() {
        // --- assertAreInterfaces ---
        listOf(classInterface).assertAreInterfaces()
        KontureScope(listOf(classInterface)).assertAreInterfaces()

        assertThrows<AssertionError> {
            listOf(classA).assertAreInterfaces()
        }

        // --- assertAreEnums ---
        val enumClass =
            ClassDeclaration(
                name = "MyEnum",
                fqName = "com.example.MyEnum",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                isEnum = true,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyEnum.kt",
            )
        listOf(enumClass).assertAreEnums()
        KontureScope(listOf(enumClass)).assertAreEnums()

        assertThrows<AssertionError> {
            listOf(classA).assertAreEnums()
        }

        // --- assertAreAbstract ---
        listOf(classAbstract).assertAreAbstract()
        KontureScope(listOf(classAbstract)).assertAreAbstract()
        listOf(classInterface).assertAreAbstract()
        KontureScope(listOf(classInterface)).assertAreAbstract()

        assertThrows<AssertionError> {
            listOf(classA).assertAreAbstract()
        }

        // --- assertAreSealed ---
        listOf(classSealed).assertAreSealed()
        KontureScope(listOf(classSealed)).assertAreSealed()

        assertThrows<AssertionError> {
            listOf(classA).assertAreSealed()
        }

        // --- assertAreData ---
        listOf(classData).assertAreData()
        KontureScope(listOf(classData)).assertAreData()

        assertThrows<AssertionError> {
            listOf(classA).assertAreData()
        }

        // --- assertAreInline ---
        val classValue =
            ClassDeclaration(
                name = "ClassValue",
                fqName = "com.example.ClassValue",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassValue.kt",
                modifiers = setOf(Modifier.VALUE),
            )
        listOf(classInline).assertAreInline()
        KontureScope(listOf(classInline)).assertAreInline()
        listOf(classValue).assertAreInline()
        KontureScope(listOf(classValue)).assertAreInline()

        assertThrows<AssertionError> {
            listOf(classA).assertAreInline()
        }

        // --- Visibility Assertions ---
        listOf(classA).assertArePublic()
        listOf(classInternal).assertAreInternal()
        listOf(classPrivate).assertArePrivate()
        listOf(classProtected).assertAreProtected()

        KontureScope(listOf(classA)).assertArePublic()
        KontureScope(listOf(classInternal)).assertAreInternal()
        KontureScope(listOf(classPrivate)).assertArePrivate()
        KontureScope(listOf(classProtected)).assertAreProtected()

        assertThrows<AssertionError> {
            listOf(classA).assertAreInternal()
        }

        // --- assertAreAssignableTo ---
        listOf(classWithParent).assertAreAssignableTo("com.example.ParentType", "SomeOtherType")
        KontureScope(listOf(classWithParent)).assertAreAssignableTo("com.example.ParentType")

        // Transitive assignability
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
        val allHierarchyClasses = listOf(grandParent, parent, child)
        listOf(child).assertAreAssignableTo("GrandParent", allClasses = allHierarchyClasses)
        KontureScope(listOf(child)).assertAreAssignableTo("GrandParent", allClasses = allHierarchyClasses)

        assertThrows<AssertionError> {
            listOf(classWithParent).assertAreAssignableTo("com.example.NonExistentParent")
        }
    }

    @Test
    fun `test High-level dependency and access assertions`() {
        // --- Dependency and Access assertions ---
        val classDep =
            ClassDeclaration(
                name = "ClassDep",
                fqName = "com.example.ClassDep",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.example.Allowed"),
                referencedTypes = emptySet(),
                filePath = "/src/ClassDep.kt",
            )
        val classAllowed =
            ClassDeclaration(
                name = "Allowed",
                fqName = "com.example.Allowed",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Allowed.kt",
            )
        val classForbidden =
            ClassDeclaration(
                name = "Forbidden",
                fqName = "com.forbidden.Forbidden",
                packageName = "com.forbidden",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Forbidden.kt",
            )

        // Success pathways
        listOf(
            classDep,
        ).assertOnlyDependOnClassesInAnyPackage("..example..", allClasses = listOf(classDep, classAllowed))
        KontureScope(
            listOf(classDep),
        ).assertOnlyDependOnClassesInAnyPackage("..example..", allClasses = listOf(classDep, classAllowed))

        listOf(
            classAllowed,
        ).assertOnlyBeAccessedByAnyPackage("..example..", allClasses = listOf(classDep, classAllowed))
        KontureScope(
            listOf(classAllowed),
        ).assertOnlyBeAccessedByAnyPackage("..example..", allClasses = listOf(classDep, classAllowed))

        // Failure pathways
        assertThrows<AssertionError> {
            val classDepBad = classDep.copy(imports = listOf("com.forbidden.Forbidden"))
            listOf(
                classDepBad,
            ).assertOnlyDependOnClassesInAnyPackage("..example..", allClasses = listOf(classDepBad, classForbidden))
        }
        assertThrows<AssertionError> {
            val classDepBad =
                classDep.copy(
                    packageName = "com.unauthorized",
                    fqName = "com.unauthorized.ClassDep",
                    imports = listOf("com.forbidden.Forbidden"),
                )
            listOf(
                classForbidden,
            ).assertOnlyBeAccessedByAnyPackage("..example..", allClasses = listOf(classDepBad, classForbidden))
        }

        // Test standard library exclusions (should be ignored) and external dependencies
        val classWithStdLib =
            ClassDeclaration(
                name = "ClassWithStdLib",
                fqName = "com.example.ClassWithStdLib",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("java.util.UUID", "kotlin.collections.List", "javax.inject.Inject"),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithStdLib.kt",
            )
        // This should pass because java, kotlin, and javax are standard exclusions
        listOf(classWithStdLib).assertOnlyDependOnClassesInAnyPackage("..example..", allClasses = listOf(classWithStdLib))

        // This should fail because "org.json" package is not allowed by pattern "..example.." and is not a standard exclusion
        val classWithExternalLib =
            ClassDeclaration(
                name = "ClassWithExternalLib",
                fqName = "com.example.ClassWithExternalLib",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("org.json.JSONObject"),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithExternalLib.kt",
            )
        assertThrows<AssertionError> {
            listOf(classWithExternalLib).assertOnlyDependOnClassesInAnyPackage("..example..", allClasses = listOf(classWithExternalLib))
        }
    }
}
