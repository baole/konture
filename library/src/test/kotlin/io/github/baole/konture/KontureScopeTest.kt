package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KontureScopeTest {
    private lateinit var classA: ClassDeclaration
    private lateinit var classB: ClassDeclaration
    private lateinit var classC: ClassDeclaration
    private lateinit var classInterface: ClassDeclaration
    private lateinit var classAbstract: ClassDeclaration
    private lateinit var classAnnotated: ClassDeclaration
    private lateinit var classInternal: ClassDeclaration
    private lateinit var classPrivate: ClassDeclaration
    private lateinit var classProtected: ClassDeclaration
    private lateinit var classData: ClassDeclaration
    private lateinit var classSealed: ClassDeclaration
    private lateinit var classInline: ClassDeclaration
    private lateinit var classWithParent: ClassDeclaration
    private lateinit var classWithKdoc: ClassDeclaration

    private lateinit var fileA: FileDeclaration
    private lateinit var fileB: FileDeclaration
    private lateinit var fileC: FileDeclaration

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

    @Test
    fun `test List of ClassDeclaration scoping extensions`() {
        val classes =
            listOf(
                classA,
                classB,
                classC,
                classInterface,
                classAbstract,
                classAnnotated,
                classInternal,
                classPrivate,
                classProtected,
                classData,
                classSealed,
                classInline,
                classWithParent,
                classWithKdoc,
            )

        // package and naming filters
        assertEquals(1, classes.withPackage("com.example.service").size)
        assertEquals(13, classes.withPackage("com.example..").size)
        assertEquals(1, classes.withNameEndingWith("Interface").size)
        assertEquals(2, classes.withNameStartingWith("My").size)
        assertEquals(12, classes.withNameMatching("Class*").size)

        // interfaces & classes
        val onlyInterfaces = classes.interfaces()
        assertEquals(2, onlyInterfaces.size)
        assertTrue(onlyInterfaces.contains(classB))
        assertTrue(onlyInterfaces.contains(classInterface))

        val onlyClasses = classes.classes()
        assertEquals(12, onlyClasses.size)
        assertFalse(onlyClasses.contains(classB))

        // annotation filters
        assertEquals(1, classes.withAnnotationOf("MyAnnotation").size)
        assertEquals(1, classes.withAnnotationOf("com.example.MyAnnotation").size)
        assertEquals(13, classes.withoutAnnotationOf("MyAnnotation").size)

        // parent filters
        assertEquals(1, classes.withParentOf("com.example.ParentType").size)

        // visibility filters
        assertEquals(11, classes.public().size)
        assertEquals(1, classes.internal().size)
        assertEquals(1, classes.private().size)
        assertEquals(1, classes.protected().size)

        // modifier filters
        assertEquals(1, classes.dataClasses().size)
        assertEquals(1, classes.sealedClasses().size)
        assertEquals(1, classes.inlineClasses().size)
    }

    @Test
    fun `test KontureScope delegation and filters`() {
        val kontureScope =
            KontureScope(
                listOf(
                    classA,
                    classB,
                    classC,
                    classInterface,
                    classAbstract,
                    classAnnotated,
                    classInternal,
                    classPrivate,
                    classProtected,
                    classData,
                    classSealed,
                    classInline,
                    classWithParent,
                    classWithKdoc,
                ),
            )

        assertEquals(1, kontureScope.withPackage("com.example.service").classes.size)
        assertEquals(1, kontureScope.withNameEndingWith("Interface").classes.size)
        assertEquals(2, kontureScope.withNameStartingWith("My").classes.size)
        assertEquals(2, kontureScope.interfaces().classes.size)
        assertEquals(12, kontureScope.classes().classes.size)
        assertEquals(1, kontureScope.withAnnotationOf("MyAnnotation").classes.size)
        assertEquals(13, kontureScope.withoutAnnotationOf("MyAnnotation").classes.size)
        assertEquals(1, kontureScope.withParentOf("com.example.ParentType").classes.size)
        assertEquals(11, kontureScope.public().classes.size)
        assertEquals(1, kontureScope.internal().classes.size)
        assertEquals(1, kontureScope.private().classes.size)
        assertEquals(1, kontureScope.protected().classes.size)
        assertEquals(1, kontureScope.dataClasses().classes.size)
        assertEquals(1, kontureScope.sealedClasses().classes.size)
        assertEquals(1, kontureScope.inlineClasses().classes.size)
        assertEquals(12, kontureScope.withNameMatching("Class*").classes.size)
        assertEquals(1, kontureScope.withVisibility(Visibility.INTERNAL).classes.size)
        assertEquals(1, kontureScope.withModifier(Modifier.DATA).classes.size)
    }

    @Test
    fun `test Scopes operators`() {
        val scope1 = KontureScope(listOf(classA))
        val scope2 = KontureScope(listOf(classB))

        val combined = scope1 + scope2
        assertEquals(2, combined.classes.size)
        assertTrue(combined.classes.contains(classA))
        assertTrue(combined.classes.contains(classB))

        val subtracted = combined - scope2
        assertEquals(1, subtracted.classes.size)
        assertTrue(subtracted.classes.contains(classA))
        assertFalse(subtracted.classes.contains(classB))
    }

    @Test
    fun `test Companion builders`() {
        val fileList = listOf(fileA, fileB, fileC)
        val mockModule =
            Module(
                buildId = ":",
                path = ":core",
                projectDir = "/core",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = fileList,
            )
        val mockGraph = ProjectGraph(mapOf(":" to listOf(mockModule)))

        // Test explicit ProjectGraph passing
        val scopeFromProj = KontureScope.fromProject(mockGraph)
        assertEquals(3, scopeFromProj.classes.size)

        val scopeFromMod = KontureScope.fromModule(":core", mockGraph)
        assertEquals(3, scopeFromMod.classes.size)

        assertThrows<IllegalArgumentException> {
            KontureScope.fromModule(":nonexistent", mockGraph)
        }

        val scopeFromPkg = KontureScope.fromPackage("com.example", mockGraph)
        assertEquals(2, scopeFromPkg.classes.size) // com.example and com.example.service

        // Test default projectGraph lookup via setDefault
        ProjectGraph.setDefault(mockGraph)

        val defaultScopeFromProj = KontureScope.fromProject()
        assertEquals(3, defaultScopeFromProj.classes.size)

        val defaultScopeFromMod = KontureScope.fromModule(":core")
        assertEquals(3, defaultScopeFromMod.classes.size)

        val defaultScopeFromPkg = KontureScope.fromPackage("com.example")
        assertEquals(2, defaultScopeFromPkg.classes.size)
    }

    @Test
    fun `test Class assertion functions`() {
        val classes = listOf(classWithKdoc)
        val kontureScope = KontureScope(classes)

        // Success pathway
        classes.assertTrue { it.name.startsWith("Class") }
        kontureScope.assertTrue { it.name.startsWith("Class") }
        classes.assertHasKDoc()
        kontureScope.assertHasKDoc()

        // Failure pathway
        val errorList =
            assertThrows<AssertionError> {
                listOf(classA).assertTrue("Custom fail msg") { it.name == "Invalid" }
            }
        assertTrue(errorList.message!!.contains("Custom fail msg"))
        assertTrue(errorList.message!!.contains("ClassA"))

        val errorScope =
            assertThrows<AssertionError> {
                KontureScope(listOf(classA)).assertTrue { it.name == "Invalid" }
            }
        assertTrue(errorScope.message!!.contains("ClassA"))

        val kdocListErr =
            assertThrows<AssertionError> {
                listOf(classA).assertHasKDoc("Missing Kdoc")
            }
        assertTrue(kdocListErr.message!!.contains("Missing Kdoc"))

        val kdocScopeErr =
            assertThrows<AssertionError> {
                KontureScope(listOf(classA)).assertHasKDoc()
            }
        assertTrue(kdocScopeErr.message!!.contains("ClassA"))
    }

    @Test
    fun `test High-level functional assertions`() {
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
        listOf(classInline).assertAreInline()
        KontureScope(listOf(classInline)).assertAreInline()

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

        assertThrows<AssertionError> {
            listOf(classWithParent).assertAreAssignableTo("com.example.NonExistentParent")
        }

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
    }
}
