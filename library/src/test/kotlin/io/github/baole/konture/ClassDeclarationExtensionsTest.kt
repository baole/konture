/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ClassDeclarationExtensionsTest {
    @Test
    fun `test ClassDeclaration dependsOn`() {
        val classTarget =
            ClassDeclaration(
                name = "Target",
                fqName = "com.example.Target",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Target.kt",
            )

        // 1. Direct import
        val classImporter =
            ClassDeclaration(
                name = "Importer",
                fqName = "com.example.Importer",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.example.Target"),
                referencedTypes = emptySet(),
                filePath = "/src/Importer.kt",
            )
        assertTrue(classImporter.dependsOn(classTarget))

        // 2. Explicit FQ name reference
        val classReferencer =
            ClassDeclaration(
                name = "Referencer",
                fqName = "com.example.Referencer",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = setOf("com.example.Target"),
                filePath = "/src/Referencer.kt",
            )
        assertTrue(classReferencer.dependsOn(classTarget))

        // 3. Same package reference with simple name reference
        val classSamePackage =
            ClassDeclaration(
                name = "SamePackage",
                fqName = "com.example.SamePackage",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = setOf("Target"),
                filePath = "/src/SamePackage.kt",
            )
        assertTrue(classSamePackage.dependsOn(classTarget))

        // 4. Star import reference
        val classStarImport =
            ClassDeclaration(
                name = "StarImport",
                fqName = "com.other.StarImport",
                packageName = "com.other",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.example.*"),
                referencedTypes = setOf("Target"),
                filePath = "/src/StarImport.kt",
            )
        assertTrue(classStarImport.dependsOn(classTarget))

        // 5. exact import ending with .Target
        val classExactImport =
            ClassDeclaration(
                name = "ExactImport",
                fqName = "com.other.ExactImport",
                packageName = "com.other",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = listOf("com.example.Target"),
                referencedTypes = setOf("Target"),
                filePath = "/src/ExactImport.kt",
            )
        assertTrue(classExactImport.dependsOn(classTarget))

        // 6. Inherits from target
        val classInherits =
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
                supertypes = listOf("Target"),
            )
        assertTrue(classInherits.dependsOn(classTarget))

        // 7. Annotated with target
        val classAnnotated =
            ClassDeclaration(
                name = "Annotated",
                fqName = "com.example.Annotated",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(AnnotationDeclaration("Target", "com.example.Target")),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Annotated.kt",
            )
        assertTrue(classAnnotated.dependsOn(classTarget))

        // 8. No dependency
        val classNoDep =
            ClassDeclaration(
                name = "NoDep",
                fqName = "com.example.NoDep",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/NoDep.kt",
            )
        assertFalse(classNoDep.dependsOn(classTarget))

        // 9. Dependency via import alias
        val classAliasDep =
            ClassDeclaration(
                name = "AliasDep",
                fqName = "com.example.AliasDep",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                isEnum = false,
                annotations = emptyList(),
                imports = listOf("com.example.Target as TargetAlias"),
                referencedTypes = setOf("TargetAlias"),
                filePath = "/src/AliasDep.kt",
                importAliases = mapOf("TargetAlias" to "com.example.Target"),
            )
        assertTrue(classAliasDep.dependsOn(classTarget))

        // 10. Dependency via annotation alias
        val classAnnotationAliasDep =
            ClassDeclaration(
                name = "AnnAliasDep",
                fqName = "com.example.AnnAliasDep",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                isEnum = false,
                annotations = listOf(AnnotationDeclaration("TargetAlias", "com.example.Target")),
                imports = listOf("com.example.Target as TargetAlias"),
                referencedTypes = emptySet(),
                filePath = "/src/AnnAliasDep.kt",
                importAliases = mapOf("TargetAlias" to "com.example.Target"),
            )
        assertTrue(classAnnotationAliasDep.dependsOn(classTarget))
    }

    @Test
    fun `test ClassDeclaration isAssignableTo transitively`() {
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
        val allClasses = listOf(grandParent, parent, child)

        assertTrue(child.isAssignableTo("Parent", allClasses))
        assertTrue(child.isAssignableTo("GrandParent", allClasses))
        assertTrue(parent.isAssignableTo("GrandParent", allClasses))
        assertFalse(grandParent.isAssignableTo("Parent", allClasses))
    }

    @Test
    fun `test ClassDeclaration collectDependencyPackages`() {
        val target =
            ClassDeclaration(
                name = "Target",
                fqName = "com.example.Target",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Target.kt",
            )
        val clazz =
            ClassDeclaration(
                name = "MyClass",
                fqName = "org.test.MyClass",
                packageName = "org.test",
                isInterface = false,
                isAbstract = false,
                imports = listOf("org.springframework.stereotype.Service", "com.jackson.*"),
                supertypes = listOf("com.example.Target"),
                annotations = listOf(AnnotationDeclaration("Service", "org.springframework.stereotype.Service")),
                referencedTypes = setOf("com.other.Helper"),
                filePath = "/src/MyClass.kt",
            )

        val dependencyPackages = clazz.collectDependencyPackages(listOf(target))

        assertTrue(dependencyPackages.contains("org.springframework.stereotype"))
        assertTrue(dependencyPackages.contains("com.jackson"))
        assertTrue(dependencyPackages.contains("com.example"))
        assertTrue(dependencyPackages.contains("com.other"))
    }
}
