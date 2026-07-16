/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KontureFileScopeTest {
    private lateinit var classA: ClassDeclaration
    private lateinit var classB: ClassDeclaration
    private lateinit var classC: ClassDeclaration
    private lateinit var classWithKdoc: ClassDeclaration

    private lateinit var fileA: FileDeclaration
    private lateinit var fileB: FileDeclaration
    private lateinit var fileC: FileDeclaration
    private lateinit var fileKdoc: FileDeclaration
    private lateinit var fileWildcard: FileDeclaration
    private lateinit var fileMultiClass: FileDeclaration
    private lateinit var fileMismatch: FileDeclaration

    @BeforeEach
    fun setUp() {
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
        fileKdoc =
            FileDeclaration(
                "ClassWithKdoc.kt",
                "com.example",
                classes = listOf(classWithKdoc),
                kdocText = "/** File level KDoc */",
                filePath = "/src/ClassWithKdoc.kt",
            )
        fileWildcard =
            FileDeclaration(
                "Wildcard.kt",
                "com.example",
                imports = listOf("com.other.*"),
                filePath = "/src/Wildcard.kt",
            )
        fileMultiClass =
            FileDeclaration(
                "MultiClass.kt",
                "com.example",
                classes = listOf(classA, classB),
                filePath = "/src/MultiClass.kt",
            )
        fileMismatch =
            FileDeclaration(
                "MismatchName.kt",
                "com.example",
                classes = listOf(classA),
                filePath = "/src/MismatchName.kt",
            )
    }

    @Test
    fun `test FileScopes operators`() {
        val fileScope1 = KontureFileScope(listOf(fileA))
        val fileScope2 = KontureFileScope(listOf(fileB))

        val combined = fileScope1 + fileScope2
        assertEquals(2, combined.files.size)
        assertTrue(combined.files.contains(fileA))
        assertTrue(combined.files.contains(fileB))

        val subtracted = combined - fileScope2
        assertEquals(1, subtracted.files.size)
        assertTrue(subtracted.files.contains(fileA))
        assertFalse(subtracted.files.contains(fileB))
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

        // FileScopes Companion explicit ProjectGraph passing
        val fileScopeFromProj = KontureFileScope.fromProject(mockGraph)
        assertEquals(3, fileScopeFromProj.files.size)

        val fileScopeFromMod = KontureFileScope.fromModule(":core", mockGraph)
        assertEquals(3, fileScopeFromMod.files.size)

        assertThrows<IllegalArgumentException> {
            KontureFileScope.fromModule(":nonexistent", mockGraph)
        }

        val fileScopeFromPkg = KontureFileScope.fromPackage("com.example", mockGraph)
        assertEquals(2, fileScopeFromPkg.files.size)

        // Test default projectGraph lookup via setDefault
        ProjectGraph.setDefault(mockGraph)

        val defaultFileScopeFromProj = KontureFileScope.fromProject()
        assertEquals(3, defaultFileScopeFromProj.files.size)

        val defaultFileScopeFromMod = KontureFileScope.fromModule(":core")
        assertEquals(3, defaultFileScopeFromMod.files.size)

        val defaultFileScopeFromPkg = KontureFileScope.fromPackage("com.example")
        assertEquals(2, defaultFileScopeFromPkg.files.size)
    }

    @Test
    fun `test List of FileDeclaration scoping extensions and KontureFileScope delegation`() {
        val files = listOf(fileA, fileB, fileC)
        val kontureFileScope = KontureFileScope(files)

        assertEquals(1, files.withNameEndingWith("A.kt").size)
        assertEquals(1, kontureFileScope.withNameEndingWith("A.kt").files.size)

        assertEquals(3, files.withNameStartingWith("Class").size)
        assertEquals(3, kontureFileScope.withNameStartingWith("Class").files.size)

        assertEquals(1, files.withNameMatching("*B*").size)
        assertEquals(1, kontureFileScope.withNameMatching("*B*").files.size)

        assertEquals(2, files.withPackage("com.example..").size)
        assertEquals(2, kontureFileScope.withPackage("com.example..").files.size)
    }

    @Test
    fun `test File assertion functions`() {
        val files = listOf(fileKdoc)
        val kontureFileScope = KontureFileScope(files)

        // Success pathways
        files.assertTrue { it.packageName == "com.example" }
        kontureFileScope.assertTrue { it.packageName == "com.example" }
        files.assertHasKDoc()
        kontureFileScope.assertHasKDoc()

        // Wildcard imports assert
        listOf(fileA).assertNoWildcardImports()
        kontureFileScope.assertNoWildcardImports()
        assertThrows<AssertionError> {
            listOf(fileWildcard).assertNoWildcardImports("Wildcard detected")
        }

        // Only one class per file assert
        listOf(fileA).assertOnlyOneClassPerFile()
        kontureFileScope.assertOnlyOneClassPerFile()
        assertThrows<AssertionError> {
            listOf(fileMultiClass).assertOnlyOneClassPerFile("Too many classes")
        }

        // File name matches class name assert
        listOf(fileA).assertFileNameMatchesClassName()
        kontureFileScope.assertFileNameMatchesClassName()
        assertThrows<AssertionError> {
            listOf(fileMismatch).assertFileNameMatchesClassName("Mismatch")
        }

        // Failure pathways general assertTrue
        val errorList =
            assertThrows<AssertionError> {
                listOf(fileA).assertTrue("Custom fail file") { it.name == "Invalid" }
            }
        assertTrue(errorList.message!!.contains("Custom fail file"))
        assertTrue(errorList.message!!.contains("ClassA.kt"))

        val errorScope =
            assertThrows<AssertionError> {
                KontureFileScope(listOf(fileA)).assertTrue { it.name == "Invalid" }
            }
        assertTrue(errorScope.message!!.contains("ClassA.kt"))

        val kdocListErr =
            assertThrows<AssertionError> {
                listOf(fileA).assertHasKDoc("Missing file Kdoc")
            }
        assertTrue(kdocListErr.message!!.contains("Missing file Kdoc"))

        val kdocScopeErr =
            assertThrows<AssertionError> {
                KontureFileScope(listOf(fileA)).assertHasKDoc()
            }
        assertTrue(kdocScopeErr.message!!.contains("ClassA.kt"))
    }

    @Test
    fun `test file high level plural assertions`() {
        val files = listOf(fileA, fileB, fileC)
        val kontureFileScope = KontureFileScope(files)

        // Success pathways (OR matching)
        files.assertResideInAPackage("..example..", "..other..")
        kontureFileScope.assertResideInAPackage("..example..", "..other..")

        files.assertNameEndingWith("A.kt", "B.kt", "C.kt")
        kontureFileScope.assertNameEndingWith("A.kt", "B.kt", "C.kt")

        files.assertNameStartingWith("ClassA", "ClassB", "ClassC")
        kontureFileScope.assertNameStartingWith("ClassA", "ClassB", "ClassC")

        files.assertNameMatching("ClassA*", "ClassB*", "ClassC*")
        kontureFileScope.assertNameMatching("ClassA*", "ClassB*", "ClassC*")

        // Failure pathways
        val packageErr =
            assertThrows<AssertionError> {
                files.assertResideInAPackage("..invalid..")
            }
        assertTrue(packageErr.message!!.contains("Files must reside in any of these packages: ..invalid.."))

        val suffixErr =
            assertThrows<AssertionError> {
                files.assertNameEndingWith("Invalid")
            }
        assertTrue(suffixErr.message!!.contains("Files must have names ending with any of: Invalid"))

        val prefixErr =
            assertThrows<AssertionError> {
                files.assertNameStartingWith("Invalid")
            }
        assertTrue(prefixErr.message!!.contains("Files must have names starting with any of: Invalid"))

        val matchErr =
            assertThrows<AssertionError> {
                files.assertNameMatching("*Invalid*")
            }
        assertTrue(matchErr.message!!.contains("Files must have names matching any of the glob patterns: *Invalid*"))
    }
}
