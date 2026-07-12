package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FilesRuleBuilderTest : RuleBuildersTestBase() {
    @Test
    fun `test files rule builder filtering and assertions`() {
        val files =
            projectGraph.getAllModules().flatMap { module ->
                module.files.map { file ->
                    FileDeclarationContext(file, module.path)
                }
            }

        // 1. resideInAPackage and resideInAModule filtering
        val rule1 =
            FilesRuleBuilder(projectGraph)
                .that()
                .resideInAPackage("com.example")
                .and()
                .resideInAModule(":moduleA")
        val pred1 = rule1.getThatPredicate()!!

        val fileAContext = files.find { it.declaration.name == "ClassA.kt" }!!
        val fileCContext = files.find { it.declaration.name == "ClassC.kt" }!!

        assertTrue(pred1(fileAContext))
        assertFalse(pred1(fileCContext)) // in :lib

        // 2. resideInAPackage with lambda
        val ruleLambdaPkg =
            FilesRuleBuilder(projectGraph)
                .that()
                .resideInAPackage { it.contains("other") }
        val predLambdaPkg = ruleLambdaPkg.getThatPredicate()!!
        assertFalse(predLambdaPkg(fileAContext))
        assertTrue(predLambdaPkg(fileCContext))

        // 3. haveNameStartingWith and haveNameEndingWith
        val ruleName =
            FilesRuleBuilder(projectGraph)
                .that()
                .haveNameStartingWith("Class")
                .and()
                .haveNameEndingWith(".kt")
        val predName = ruleName.getThatPredicate()!!
        assertTrue(predName(fileAContext))

        // 4. satisfy and logical operator combinations on filtering
        val ruleXor =
            FilesRuleBuilder(projectGraph)
                .that()
                .haveNameStartingWith("ClassA")
                .xor()
                .resideInAModule(":moduleC")
        val predXor = ruleXor.getThatPredicate()!!
        // ClassA: name starts with ClassA (T) xor in :moduleC (F) -> T
        // ClassC: name starts with ClassA (F) xor in :moduleC (T) -> T
        // ClassB: name starts with ClassA (F) xor in :moduleC (F) -> F
        val fileBContext = files.find { it.declaration.name == "ClassB.kt" }!!
        assertTrue(predXor(fileAContext))
        assertTrue(predXor(fileCContext))
        assertFalse(predXor(fileBContext))

        // 5. check negated filter
        val ruleNot =
            FilesRuleBuilder(projectGraph)
                .not()
                .resideInAModule(":moduleC")
        val predNot = ruleNot.getThatPredicate()!!
        assertTrue(predNot(fileAContext))
        assertFalse(predNot(fileCContext))
    }

    @Test
    fun `test files rule builder assertions`() {
        val files =
            projectGraph.getAllModules().flatMap { module ->
                module.files.map { file ->
                    FileDeclarationContext(file, module.path)
                }
            }
        val fileAContext = files.find { it.declaration.name == "ClassA.kt" }!!
        val fileCContext = files.find { it.declaration.name == "ClassC.kt" }!!

        // Test haveNameMatching
        val ruleMatch = FilesRuleBuilder(projectGraph).should().haveNameMatching("Class*")
        val assertMatch = ruleMatch.getShouldAssertion()!!
        val vMatch = mutableListOf<String>()
        assertMatch(fileAContext, files, vMatch)
        assertTrue(vMatch.isEmpty())

        val classDFile =
            FileDeclaration(
                name = "WrongName.kt",
                packageName = "com.example",
                imports = emptyList(),
                classes = emptyList(),
                filePath = "/src/WrongName.kt",
            )
        val fileDContext = FileDeclarationContext(classDFile, ":app")
        assertMatch(fileDContext, files, vMatch)
        assertEquals(1, vMatch.size)

        // Test resideInAPackage (lambda and string pattern)
        val rulePkgString = FilesRuleBuilder(projectGraph).should().resideInAPackage("com.other")
        val assertPkgString = rulePkgString.getShouldAssertion()!!
        val vPkgString = mutableListOf<String>()
        assertPkgString(fileCContext, files, vPkgString)
        assertTrue(vPkgString.isEmpty())

        assertPkgString(fileAContext, files, vPkgString)
        assertEquals(1, vPkgString.size)

        val rulePkgLambda = FilesRuleBuilder(projectGraph).should().resideInAPackage { it.startsWith("com") }
        val assertPkgLambda = rulePkgLambda.getShouldAssertion()!!
        val vPkgLambda = mutableListOf<String>()
        assertPkgLambda(fileAContext, files, vPkgLambda)
        assertTrue(vPkgLambda.isEmpty())

        // Test haveNameStartingWith and haveNameEndingWith
        val ruleEnds = FilesRuleBuilder(projectGraph).should().haveNameEndingWith("C.kt")
        val assertEnds = ruleEnds.getShouldAssertion()!!
        val vEnds = mutableListOf<String>()
        assertEnds(fileCContext, files, vEnds)
        assertTrue(vEnds.isEmpty())

        assertEnds(fileAContext, files, vEnds)
        assertEquals(1, vEnds.size)

        // Test satisfy (lambda predicate)
        val ruleSatisfy = FilesRuleBuilder(projectGraph).should().satisfy { it.modulePath == ":moduleC" }
        val assertSatisfy = ruleSatisfy.getShouldAssertion()!!
        val vSatisfy = mutableListOf<String>()
        assertSatisfy(fileCContext, files, vSatisfy)
        assertTrue(vSatisfy.isEmpty())

        assertSatisfy(fileAContext, files, vSatisfy)
        assertEquals(1, vSatisfy.size)

        // Test satisfy with custom violation reporting
        val ruleSatisfyCustom =
            FilesRuleBuilder(projectGraph).should().satisfy { context, violations ->
                if (context.declaration.name != "ClassA.kt") {
                    violations.add("Incorrect name")
                }
            }
        val assertSatisfyCustom = ruleSatisfyCustom.getShouldAssertion()!!
        val vSatisfyCustom = mutableListOf<String>()
        assertSatisfyCustom(fileAContext, files, vSatisfyCustom)
        assertTrue(vSatisfyCustom.isEmpty())

        assertSatisfyCustom(fileCContext, files, vSatisfyCustom)
        assertEquals(1, vSatisfyCustom.size)
    }

    @Test
    fun `test file rule builder structural rules`() {
        val files =
            projectGraph.getAllModules().flatMap { module ->
                module.files.map { file ->
                    FileDeclarationContext(file, module.path)
                }
            }
        val fileAContext = files.find { it.declaration.name == "ClassA.kt" }!!

        // Test notHaveWildcardImports
        val fileWithWildcard =
            FileDeclaration(
                name = "Wildcard.kt",
                packageName = "com.example",
                imports = listOf("kotlin.collections.*", "com.example.ClassA"),
                classes = emptyList(),
                filePath = "/src/Wildcard.kt",
            )
        val fileWithWildcardContext = FileDeclarationContext(fileWithWildcard, ":app")

        val ruleWildcard = FilesRuleBuilder(projectGraph).should().notHaveWildcardImports()
        val assertWildcard = ruleWildcard.getShouldAssertion()!!

        val v1 = mutableListOf<String>()
        assertWildcard(fileAContext, files, v1)
        assertTrue(v1.isEmpty())

        assertWildcard(fileWithWildcardContext, files, v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("should not contain wildcard imports but contains: kotlin.collections.*"))

        // Test haveOnlyOneClassPerFile
        val multiClassFile =
            FileDeclaration(
                name = "Multi.kt",
                packageName = "com.example",
                imports = emptyList(),
                classes =
                    listOf(
                        ClassDeclaration(
                            "Foo",
                            "com.example.Foo",
                            "com.example",
                            false,
                            false,
                            emptyList(),
                            emptyList(),
                            emptySet(),
                            "/src/Multi.kt",
                        ),
                        ClassDeclaration(
                            "Bar",
                            "com.example.Bar",
                            "com.example",
                            false,
                            false,
                            emptyList(),
                            emptyList(),
                            emptySet(),
                            "/src/Multi.kt",
                        ),
                    ),
                filePath = "/src/Multi.kt",
            )
        val multiClassFileContext = FileDeclarationContext(multiClassFile, ":app")

        val ruleOneClass = FilesRuleBuilder(projectGraph).should().haveOnlyOneClassPerFile()
        val assertOneClass = ruleOneClass.getShouldAssertion()!!

        val v2 = mutableListOf<String>()
        assertOneClass(fileAContext, files, v2)
        assertTrue(v2.isEmpty())

        assertOneClass(multiClassFileContext, files, v2)
        assertEquals(1, v2.size)

        // Test haveNameMatchingClassName
        val mismatchClassFile =
            FileDeclaration(
                name = "WrongName.kt",
                packageName = "com.example",
                imports = emptyList(),
                classes =
                    listOf(
                        ClassDeclaration(
                            "RightName",
                            "com.example.RightName",
                            "com.example",
                            false,
                            false,
                            emptyList(),
                            emptyList(),
                            emptySet(),
                            "/src/WrongName.kt",
                        ),
                    ),
                filePath = "/src/WrongName.kt",
            )
        val mismatchClassFileContext = FileDeclarationContext(mismatchClassFile, ":app")

        val ruleNameClass = FilesRuleBuilder(projectGraph).should().haveNameMatchingClassName()
        val assertNameClass = ruleNameClass.getShouldAssertion()!!

        val v3 = mutableListOf<String>()
        assertNameClass(fileAContext, files, v3)
        assertTrue(v3.isEmpty())

        assertNameClass(mismatchClassFileContext, files, v3)
        assertEquals(1, v3.size)

        // Test beDocumentedWithKDoc
        val ruleKDoc = FilesRuleBuilder(projectGraph).should().beDocumentedWithKDoc()
        val assertKDoc = ruleKDoc.getShouldAssertion()!!

        val fileNoKDocContext = fileAContext
        val fileWithKDoc =
            FileDeclaration(
                name = "Doc.kt",
                packageName = "com.example",
                imports = emptyList(),
                classes = emptyList(),
                filePath = "/src/Doc.kt",
                kdocText = "/** File documentation. */",
            )
        val fileWithKDocContext = FileDeclarationContext(fileWithKDoc, ":app")

        val v4 = mutableListOf<String>()
        assertKDoc(fileWithKDocContext, files, v4)
        assertTrue(v4.isEmpty())

        assertKDoc(fileNoKDocContext, files, v4)
        assertEquals(1, v4.size)
    }

    @Test
    fun `test files rule builder logical operators on assertions`() {
        val files =
            projectGraph.getAllModules().flatMap { module ->
                module.files.map { file ->
                    FileDeclarationContext(file, module.path)
                }
            }
        val fileAContext = files.find { it.declaration.name == "ClassA.kt" }!!
        val fileCContext = files.find { it.declaration.name == "ClassC.kt" }!!

        // AND should assertion
        val ruleAnd =
            FilesRuleBuilder(projectGraph)
                .should()
                .resideInAPackage("com.example")
                .andShould()
                .haveNameStartingWith("Class")
        val assertAnd = ruleAnd.getShouldAssertion()!!
        val vAnd = mutableListOf<String>()
        assertAnd(fileAContext, files, vAnd)
        assertTrue(vAnd.isEmpty())

        assertAnd(fileCContext, files, vAnd)
        assertEquals(1, vAnd.size) // fails package check

        // OR should assertion
        val ruleOr =
            FilesRuleBuilder(projectGraph)
                .should()
                .resideInAPackage("com.other")
                .orShould()
                .haveNameStartingWith("ClassA")
        val assertOr = ruleOr.getShouldAssertion()!!
        val vOr = mutableListOf<String>()
        assertOr(fileAContext, files, vOr) // package is com.example (F) but name starts with ClassA (T) -> passes OR
        assertTrue(vOr.isEmpty())

        assertOr(fileCContext, files, vOr) // package is com.other (T) but name is ClassC (F) -> passes OR
        assertTrue(vOr.isEmpty())

        val fileBContext = files.find { it.declaration.name == "ClassB.kt" }!!
        assertOr(fileBContext, files, vOr) // package is com.example (F) and name starts with ClassB (F) -> fails
        assertEquals(1, vOr.size)

        // XOR should assertion
        val ruleXor =
            FilesRuleBuilder(projectGraph)
                .should()
                .resideInAPackage("com.example")
                .xorShould()
                .haveNameStartingWith("ClassA")
        val assertXor = ruleXor.getShouldAssertion()!!
        val vXor = mutableListOf<String>()
        assertXor(fileBContext, files, vXor) // package com.example (T) xor starts with ClassA (F) -> passes
        assertTrue(vXor.isEmpty())

        assertXor(fileAContext, files, vXor) // package com.example (T) xor starts with ClassA (T) -> fails XOR
        assertEquals(1, vXor.size)

        // NOT should assertion
        val ruleNot =
            FilesRuleBuilder(projectGraph)
                .notShould()
                .resideInAPackage("com.other")
        val assertNot = ruleNot.getShouldAssertion()!!
        val vNot = mutableListOf<String>()
        assertNot(fileAContext, files, vNot) // package is com.example -> passes NOT(com.other)
        assertTrue(vNot.isEmpty())

        assertNot(fileCContext, files, vNot) // package is com.other -> fails NOT(com.other)
        assertEquals(1, vNot.size)
    }
}
