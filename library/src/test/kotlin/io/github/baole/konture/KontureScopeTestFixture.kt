/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.BeforeEach

internal abstract class KontureScopeTestFixture {
    protected lateinit var classA: ClassDeclaration
    protected lateinit var classB: ClassDeclaration
    protected lateinit var classC: ClassDeclaration
    protected lateinit var classInterface: ClassDeclaration
    protected lateinit var classAbstract: ClassDeclaration
    protected lateinit var classAnnotated: ClassDeclaration
    protected lateinit var classInternal: ClassDeclaration
    protected lateinit var classPrivate: ClassDeclaration
    protected lateinit var classProtected: ClassDeclaration
    protected lateinit var classData: ClassDeclaration
    protected lateinit var classSealed: ClassDeclaration
    protected lateinit var classInline: ClassDeclaration
    protected lateinit var classWithParent: ClassDeclaration
    protected lateinit var classWithKdoc: ClassDeclaration

    protected lateinit var fileA: FileDeclaration
    protected lateinit var fileB: FileDeclaration
    protected lateinit var fileC: FileDeclaration

    @BeforeEach
    fun setUp() {
        val annotation = AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")

        classA =
            ClassDeclaration(
                name = "ClassA",
                fqName = "com.example.ClassA",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassA.kt",
            )
        classB =
            ClassDeclaration(
                name = "ClassB",
                fqName = "com.example.service.ClassB",
                packageName = "com.example.service",
                isInterface = true,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassB.kt",
            )
        classC =
            ClassDeclaration(
                name = "ClassC",
                fqName = "com.other.ClassC",
                packageName = "com.other",
                isInterface = false,
                isAbstract = true,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassC.kt",
            )
        classInterface =
            ClassDeclaration(
                name = "MyInterface",
                fqName = "com.example.MyInterface",
                packageName = "com.example",
                isInterface = true,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyInterface.kt",
            )
        classAbstract =
            ClassDeclaration(
                name = "MyAbstract",
                fqName = "com.example.MyAbstract",
                packageName = "com.example",
                isInterface = false,
                isAbstract = true,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyAbstract.kt",
            )
        classAnnotated =
            ClassDeclaration(
                name = "ClassAnnotated",
                fqName = "com.example.ClassAnnotated",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(annotation),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassAnnotated.kt",
            )
        classInternal =
            ClassDeclaration(
                name = "ClassInternal",
                fqName = "com.example.ClassInternal",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassInternal.kt",
                visibility = Visibility.INTERNAL,
            )
        classPrivate =
            ClassDeclaration(
                name = "ClassPrivate",
                fqName = "com.example.ClassPrivate",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassPrivate.kt",
                visibility = Visibility.PRIVATE,
            )
        classProtected =
            ClassDeclaration(
                name = "ClassProtected",
                fqName = "com.example.ClassProtected",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassProtected.kt",
                visibility = Visibility.PROTECTED,
            )
        classData =
            ClassDeclaration(
                name = "ClassData",
                fqName = "com.example.ClassData",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassData.kt",
                modifiers = setOf(Modifier.DATA),
            )
        classSealed =
            ClassDeclaration(
                name = "ClassSealed",
                fqName = "com.example.ClassSealed",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassSealed.kt",
                modifiers = setOf(Modifier.SEALED),
            )
        classInline =
            ClassDeclaration(
                name = "ClassInline",
                fqName = "com.example.ClassInline",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassInline.kt",
                modifiers = setOf(Modifier.INLINE),
            )
        classWithParent =
            ClassDeclaration(
                name = "ClassWithParent",
                fqName = "com.example.ClassWithParent",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithParent.kt",
                supertypes = listOf("com.example.ParentType"),
            )
        classWithKdoc =
            ClassDeclaration(
                name = "ClassWithKdoc",
                fqName = "com.example.ClassWithKdoc",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithKdoc.kt",
                kdocText = "/** This is a KDoc */",
            )

        fileA = FileDeclaration("ClassA.kt", "com.example", classes = listOf(classA), filePath = "/src/ClassA.kt")
        fileB =
            FileDeclaration("ClassB.kt", "com.example.service", classes = listOf(classB), filePath = "/src/ClassB.kt")
        fileC = FileDeclaration("ClassC.kt", "com.other", classes = listOf(classC), filePath = "/src/ClassC.kt")
    }
}
