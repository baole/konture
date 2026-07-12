package io.github.baole.konture

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FunctionAssertionScopeTest : RuleBuildersTestBase() {
    @Test
    fun `test allFunctions assertions`() {
        val func1 =
            FunctionDeclaration(
                name = "fetchData",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.SUSPEND),
                returnType = "kotlin.Unit",
                parameters = emptyList(),
                annotations = listOf(AnnotationDeclaration("MyAnnotation", "com.example.MyAnnotation")),
                kdocText = "/** KDoc */",
                isExtension = false,
            )
        val func2 =
            FunctionDeclaration(
                name = "processPrivate",
                visibility = Visibility.PRIVATE,
                modifiers = setOf(Modifier.INLINE),
                returnType = "kotlin.String",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = true,
            )

        val targetClass =
            ClassDeclaration(
                name = "Service",
                fqName = "com.example.Service",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/Service.kt",
                functions = listOf(func1, func2),
            )

        // Test public requirement (should fail due to func2)
        val rulePublic = ClassesRuleBuilder(projectGraph).should().allFunctions { bePublic() }
        val assertPublic = rulePublic.getShouldAssertion()!!
        val v1 = mutableListOf<String>()
        assertPublic(targetClass, emptyList(), v1)
        assertEquals(1, v1.size)
        assertTrue(v1[0].contains("Function processPrivate in class com.example.Service has violations"))

        // Test suspend requirement on all (should fail due to func2)
        val ruleSuspend = ClassesRuleBuilder(projectGraph).should().allFunctions { beSuspend() }
        val assertSuspend = ruleSuspend.getShouldAssertion()!!
        val v2 = mutableListOf<String>()
        assertSuspend(targetClass, emptyList(), v2)
        assertEquals(1, v2.size)

        // Test inline requirement (should fail due to func1)
        val ruleInline = ClassesRuleBuilder(projectGraph).should().allFunctions { beInline() }
        val assertInline = ruleInline.getShouldAssertion()!!
        val v3 = mutableListOf<String>()
        assertInline(targetClass, emptyList(), v3)
        assertEquals(1, v3.size)

        // Test return type check (should fail due to func2 returning String)
        val ruleReturnType = ClassesRuleBuilder(projectGraph).should().allFunctions { haveReturnType("kotlin.Unit") }
        val assertReturnType = ruleReturnType.getShouldAssertion()!!
        val v4 = mutableListOf<String>()
        assertReturnType(targetClass, emptyList(), v4)
        assertEquals(1, v4.size)

        // Test name matching (both match "f*" or "p*") -> should fail if we require starting with "fetch" (func2 fails)
        val ruleNamePrefix = ClassesRuleBuilder(projectGraph).should().allFunctions { haveNameStartingWith("fetch") }
        val assertNamePrefix = ruleNamePrefix.getShouldAssertion()!!
        val v5 = mutableListOf<String>()
        assertNamePrefix(targetClass, emptyList(), v5)
        assertEquals(1, v5.size)

        // Test annotations & KDoc requirements (func2 fails both)
        val ruleDoc =
            ClassesRuleBuilder(projectGraph).should().allFunctions {
                beDocumentedWithKDoc()
                haveAnnotationOf("MyAnnotation")
            }
        val assertDoc = ruleDoc.getShouldAssertion()!!
        val v6 = mutableListOf<String>()
        assertDoc(targetClass, emptyList(), v6)
        assertEquals(1, v6.size) // func2 violates both, combined under one entry for targetClass function
    }

    @Test
    fun `test function assertion scope consistency`() {
        val func =
            FunctionDeclaration(
                name = "myFunc",
                returnType = "String",
                isExtension = false,
                annotations = listOf(AnnotationDeclaration("A1", "com.example.A1")),
                modifiers = setOf(Modifier.ABSTRACT),
                visibility = Visibility.PROTECTED,
                kdocText = "KDoc",
                parameters = emptyList(),
            )

        val classWithMembers =
            ClassDeclaration(
                name = "ClassWithMembers",
                fqName = "com.example.ClassWithMembers",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/ClassWithMembers.kt",
                functions = listOf(func),
            )

        val ruleFunc =
            ClassesRuleBuilder(projectGraph).should().allFunctions {
                haveAllAnnotationsOf(listOf("A1"))
                haveAllAnnotationsOf("A1")
                haveAnyAnnotationOf(listOf("A1", "A2"))
                haveAnyAnnotationOf("A1", "A2")
                haveAllModifiers(listOf(Modifier.ABSTRACT))
                haveAllModifiers(Modifier.ABSTRACT)
                haveAnyModifier(listOf(Modifier.ABSTRACT, Modifier.OPEN))
                haveAnyModifier(Modifier.ABSTRACT, Modifier.OPEN)
                haveAnyVisibility(listOf(Visibility.PROTECTED))
                haveAnyVisibility(Visibility.PROTECTED)
            }
        val assertFunc = ruleFunc.getShouldAssertion()!!
        val vFunc = mutableListOf<String>()
        assertFunc(classWithMembers, emptyList(), vFunc)
        assertTrue(vFunc.isEmpty(), "Expected no violations: $vFunc")
    }

    @Test
    fun `test function assertion scope failure paths`() {
        val func =
            FunctionDeclaration(
                name = "myFunc",
                returnType = "String",
                isExtension = false,
                annotations = emptyList(),
                modifiers = emptySet(),
                visibility = Visibility.INTERNAL,
                kdocText = null,
                parameters = emptyList(),
            )

        val targetClass =
            ClassDeclaration(
                name = "MyClass",
                fqName = "com.example.MyClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "/src/MyClass.kt",
                functions = listOf(func),
            )

        val scope =
            FunctionAssertionScope().apply {
                haveNameMatching("fetch*")
                haveNameEndingWith("Data")
                haveNameStartingWith("get")
                haveReturnType("Int")
                haveAnnotationOf("AnnotationA")
                beInternal() // should succeed, so let's put it elsewhere or assert separately
                bePrivate()
                beProtected()
                beOpen()
                beAbstract()
                haveModifier(Modifier.SUSPEND)
                beExtension()
                haveAllAnnotationsOf(listOf("A1"))
                haveAnyAnnotationOf("A1", "A2")
                haveAllModifiers(Modifier.SUSPEND)
                haveAnyModifier(Modifier.SUSPEND)
                haveAnyVisibility(Visibility.PUBLIC)
            }

        val violations = mutableListOf<String>()
        for (assertion in scope.assertions) {
            assertion(func, violations)
        }

        assertEquals(16, violations.size)
        assertTrue(violations.any { it.contains("should have name matching any of: 'fetch*'") })
        assertTrue(violations.any { it.contains("should have name ending with any of: 'Data'") })
        assertTrue(violations.any { it.contains("should have name starting with any of: 'get'") })
        assertTrue(violations.any { it.contains("should have return type of any of: 'Int'") })
        assertTrue(violations.any { it.contains("should be annotated with any of: @AnnotationA") })
        assertTrue(violations.any { it.contains("should be private") })
        assertTrue(violations.any { it.contains("should be protected") })
        assertTrue(violations.any { it.contains("should be open") })
        assertTrue(violations.any { it.contains("should be abstract") })
        assertTrue(violations.any { it.contains("should have modifier suspend") })
        assertTrue(violations.any { it.contains("should be an extension function") })
        assertTrue(violations.any { it.contains("should have all annotations: A1") })
        assertTrue(violations.any { it.contains("should have at least one annotation of: A1, A2") })
        assertTrue(violations.any { it.contains("should have all modifiers: suspend") })
        assertTrue(violations.any { it.contains("should have at least one modifier of: suspend") })
        assertTrue(violations.any { it.contains("should have visibility of: public") })
    }

    @Test
    fun `test function assertion scope plural logical OR matching`() {
        val annotation = AnnotationDeclaration(name = "Fetch", fqName = "com.example.Fetch")
        val func =
            FunctionDeclaration(
                name = "fetchUserData",
                returnType = "com.example.User",
                isExtension = false,
                annotations = listOf(annotation),
                modifiers = emptySet(),
                visibility = Visibility.PUBLIC,
                kdocText = null,
                parameters = emptyList(),
            )

        // All of these should PASS since at least one pattern matches (logical OR)
        val passingScope =
            FunctionAssertionScope().apply {
                haveNameMatching("get*", "fetch*")
                haveNameStartingWith("get", "fetch")
                haveNameEndingWith("Data", "Model")
                haveReturnType("Int", "com.example.User")
                haveAnnotationOf("Get", "Fetch")
            }

        val passViolations = mutableListOf<String>()
        for (assertion in passingScope.assertions) {
            assertion(func, passViolations)
        }
        assertTrue(passViolations.isEmpty(), "Expected logical OR to pass but got: $passViolations")

        // All of these should FAIL since none of the patterns match
        val failingScope =
            FunctionAssertionScope().apply {
                haveNameMatching("find*", "load*")
                haveNameStartingWith("find", "load")
                haveNameEndingWith("Entity", "Dto")
                haveReturnType("Int", "Double")
                haveAnnotationOf("Post", "Delete")
            }

        val failViolations = mutableListOf<String>()
        for (assertion in failingScope.assertions) {
            assertion(func, failViolations)
        }
        assertEquals(5, failViolations.size)
        assertTrue(failViolations.any { it.contains("should have name matching any of: 'find*', 'load*'") })
        assertTrue(failViolations.any { it.contains("should have name starting with any of: 'find', 'load'") })
        assertTrue(failViolations.any { it.contains("should have name ending with any of: 'Entity', 'Dto'") })
        assertTrue(failViolations.any { it.contains("should have return type of any of: 'Int', 'Double'") })
        assertTrue(failViolations.any { it.contains("should be annotated with any of: @Post, @Delete") })
    }

    @Test
    fun `test function assertion scope overloads list and singular`() {
        val annotation = AnnotationDeclaration(name = "Fetch", fqName = "com.example.Fetch")
        val func =
            FunctionDeclaration(
                name = "fetchUserData",
                returnType = "com.example.User",
                isExtension = false,
                annotations = listOf(annotation),
                modifiers = emptySet(),
                visibility = Visibility.PUBLIC,
                kdocText = null,
                parameters = emptyList(),
            )

        // Single string overloads (passing)
        val passingSingleScope =
            FunctionAssertionScope().apply {
                haveNameMatching("fetch*")
                haveNameStartingWith("fetch")
                haveNameEndingWith("Data")
                haveReturnType("com.example.User")
                haveAnnotationOf("Fetch")
            }

        val passSingleViolations = mutableListOf<String>()
        for (assertion in passingSingleScope.assertions) {
            assertion(func, passSingleViolations)
        }
        assertTrue(passSingleViolations.isEmpty(), "Expected singular overloads to pass but got: $passSingleViolations")

        // List overloads (passing OR match)
        val passingListScope =
            FunctionAssertionScope().apply {
                haveNameMatching(listOf("get*", "fetch*"))
                haveNameStartingWith(listOf("get", "fetch"))
                haveNameEndingWith(listOf("Data", "Model"))
                haveReturnType(listOf("Int", "com.example.User"))
                haveAnnotationOf(listOf("Get", "Fetch"))
            }

        val passListViolations = mutableListOf<String>()
        for (assertion in passingListScope.assertions) {
            assertion(func, passListViolations)
        }
        assertTrue(passListViolations.isEmpty(), "Expected list overloads to pass but got: $passListViolations")

        // List overloads (failing match)
        val failingListScope =
            FunctionAssertionScope().apply {
                haveNameMatching(listOf("find*", "load*"))
                haveNameStartingWith(listOf("find", "load"))
                haveNameEndingWith(listOf("Entity", "Dto"))
                haveReturnType(listOf("Int", "Double"))
                haveAnnotationOf(listOf("Post", "Delete"))
            }

        val failListViolations = mutableListOf<String>()
        for (assertion in failingListScope.assertions) {
            assertion(func, failListViolations)
        }
        assertEquals(5, failListViolations.size)
    }
}
