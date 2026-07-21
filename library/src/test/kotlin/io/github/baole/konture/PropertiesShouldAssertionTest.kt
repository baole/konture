/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PropertiesShouldAssertionTest : RuleBuildersTestBase() {
    @Test
    fun `test all PropertiesShould assertion success and failures`() {
        val graph = ProjectGraph(emptyMap())

        // 1. Setup various properties
        val propExt =
            PropertyDeclaration(
                name = "extProp",
                visibility = Visibility.PUBLIC,
                modifiers = setOf(Modifier.CONST),
                type = "kotlin.Int",
                isVal = true,
                annotations = listOf(AnnotationDeclaration("MyAnno", "com.example.MyAnno")),
                kdocText = "Some KDoc",
                isExtension = true,
                resolvedType = "kotlin.Int",
            )
        val propLateinit =
            PropertyDeclaration(
                name = "lateProp",
                visibility = Visibility.PRIVATE,
                modifiers = setOf(Modifier.LATEINIT),
                type = "kotlin.String",
                isVal = false,
                annotations = emptyList(),
                kdocText = null,
                isExtension = false,
                resolvedType = "kotlin.String",
            )

        val ctxExt = PropertyDeclarationContext(propExt, "com.example", "MyClass", ":app", "/src/File.kt")
        val ctxLate = PropertyDeclarationContext(propLateinit, "com.other", null, ":app", "/src/File.kt")
        val allContexts = listOf(ctxExt, ctxLate)

        fun assertSuccess(
            assertion: PropertiesShould.() -> PropertiesRuleBuilder,
            context: PropertyDeclarationContext,
        ) {
            val violations = mutableListOf<String>()
            val builder = PropertiesRuleBuilder(graph)
            val should = PropertiesShould(builder)
            should.assertion()
            builder.getShouldAssertion()!!(context, allContexts, violations)
            assertTrue(violations.isEmpty(), "Expected success but got: $violations")
        }

        fun assertFailure(
            assertion: PropertiesShould.() -> PropertiesRuleBuilder,
            context: PropertyDeclarationContext,
            expectedMessagePart: String,
        ) {
            val violations = mutableListOf<String>()
            val builder = PropertiesRuleBuilder(graph)
            val should = PropertiesShould(builder)
            should.assertion()
            builder.getShouldAssertion()!!(context, allContexts, violations)
            assertEquals(1, violations.size)
            assertTrue(violations[0].contains(expectedMessagePart), "Expected message part '$expectedMessagePart' but got: '${violations[0]}'")
        }

        // 2. Test resideInAPackage variants
        assertSuccess({ resideInAPackage("com..") }, ctxExt)
        assertFailure({ resideInAPackage("com.other") }, ctxExt, "should reside in package 'com.other'")

        assertSuccess({ resideInAPackage(listOf("com.example", "org.example")) }, ctxExt)
        assertFailure({ resideInAPackage(listOf("org.example")) }, ctxExt, "should reside in package in [org.example] but resides in 'com.example'")

        assertSuccess({ resideInAPackage("com.example", "org.example") }, ctxExt)
        assertFailure({ resideInAPackage("org.example") }, ctxExt, "should reside in package 'org.example' but resides in 'com.example'")

        assertSuccess({ resideInAPackage { it.startsWith("com.") } }, ctxExt)
        assertFailure(
            { resideInAPackage { it.startsWith("org.") } },
            ctxExt,
            "should reside in package matching predicate, but resides in 'com.example'",
        )

        // 3. Test haveNameEndingWith variants
        assertSuccess({ haveNameEndingWith("Prop") }, ctxExt)
        assertFailure({ haveNameEndingWith("Late") }, ctxExt, "should have name ending with 'Late'")

        assertSuccess({ haveNameEndingWith(listOf("Prop", "Other")) }, ctxExt)
        assertFailure({ haveNameEndingWith(listOf("Late", "Other")) }, ctxExt, "should have name ending with any of [Late, Other]")

        assertSuccess({ haveNameEndingWith("Prop", "Other") }, ctxExt)
        assertFailure({ haveNameEndingWith("Late", "Other") }, ctxExt, "should have name ending with any of [Late, Other]")

        // 4. Test haveNameStartingWith variants
        assertSuccess({ haveNameStartingWith("ext") }, ctxExt)
        assertFailure({ haveNameStartingWith("late") }, ctxExt, "should have name starting with 'late'")

        assertSuccess({ haveNameStartingWith(listOf("ext", "other")) }, ctxExt)
        assertFailure({ haveNameStartingWith(listOf("late", "other")) }, ctxExt, "should have name starting with any of [late, other]")

        assertSuccess({ haveNameStartingWith("ext", "other") }, ctxExt)
        assertFailure({ haveNameStartingWith("late", "other") }, ctxExt, "should have name starting with any of [late, other]")

        // 5. Test haveNameMatching variants
        assertSuccess({ haveNameMatching("ext*") }, ctxExt)
        assertFailure({ haveNameMatching("late*") }, ctxExt, "should have name matching 'late*'")

        assertSuccess({ haveNameMatching(listOf("ext*", "other")) }, ctxExt)
        assertFailure({ haveNameMatching(listOf("late*", "other")) }, ctxExt, "should have name matching any of [late*, other]")

        assertSuccess({ haveNameMatching("ext*", "other") }, ctxExt)
        assertFailure({ haveNameMatching("late*", "other") }, ctxExt, "should have name matching any of [late*, other]")

        // 6. Test bePublic, beInternal, bePrivate, beProtected, beVar, beVal
        assertSuccess({ bePublic() }, ctxExt)
        assertFailure({ bePublic() }, ctxLate, "should be public")

        assertSuccess({ bePrivate() }, ctxLate)
        assertFailure({ bePrivate() }, ctxExt, "should be private")

        // internal & protected check
        val propInternal = propExt.copy(visibility = Visibility.INTERNAL)
        val ctxInternal = PropertyDeclarationContext(propInternal, "com.example", null, ":app", "/src/File.kt")
        assertSuccess({ beInternal() }, ctxInternal)
        assertFailure({ beInternal() }, ctxExt, "should be internal")

        val propProtected = propExt.copy(visibility = Visibility.PROTECTED)
        val ctxProtected = PropertyDeclarationContext(propProtected, "com.example", null, ":app", "/src/File.kt")
        assertSuccess({ beProtected() }, ctxProtected)
        assertFailure({ beProtected() }, ctxExt, "should be protected")

        assertSuccess({ beVal() }, ctxExt)
        assertFailure({ beVal() }, ctxLate, "should be val (read-only)")

        assertSuccess({ beVar() }, ctxLate)
        assertFailure({ beVar() }, ctxExt, "should be var (mutable)")

        // 7. Test haveType variants
        assertSuccess({ haveType("kotlin.Int") }, ctxExt)
        assertFailure({ haveType("kotlin.String") }, ctxExt, "should have type 'kotlin.String' but was 'kotlin.Int'")

        assertSuccess({ haveType(Int::class) }, ctxExt)
        assertFailure({ haveType(String::class) }, ctxExt, "should have type 'kotlin.String' but was 'kotlin.Int'")

        assertSuccess({ haveTypeOf<Int>() }, ctxExt)
        assertFailure({ haveTypeOf<String>() }, ctxExt, "should have type 'kotlin.String' but was 'kotlin.Int'")

        assertSuccess({ haveType(listOf("kotlin.Int", "kotlin.Double")) }, ctxExt)
        assertFailure({ haveType(listOf("kotlin.String")) }, ctxExt, "should have type in [kotlin.String] but was 'kotlin.Int'")

        assertSuccess({ haveType("kotlin.Int", "kotlin.Double") }, ctxExt)
        assertFailure({ haveType("kotlin.String") }, ctxExt, "should have type 'kotlin.String' but was 'kotlin.Int'")

        // 8. Test haveAnnotationOf variants
        assertSuccess({ haveAnnotationOf("MyAnno") }, ctxExt)
        assertFailure({ haveAnnotationOf("OtherAnno") }, ctxExt, "should be annotated with @OtherAnno")

        assertSuccess({ haveAnnotationOf(listOf("MyAnno", "OtherAnno")) }, ctxExt)
        assertFailure({ haveAnnotationOf(listOf("OtherAnno")) }, ctxExt, "should be annotated with any of [OtherAnno]")

        assertSuccess({ haveAnnotationOf("MyAnno", "OtherAnno") }, ctxExt)
        assertFailure({ haveAnnotationOf("OtherAnno", "SomeOther") }, ctxExt, "should be annotated with any of [OtherAnno, SomeOther]")

        // 9. Test haveAllAnnotationsOf variants
        assertSuccess({ haveAllAnnotationsOf(listOf("MyAnno")) }, ctxExt)
        assertFailure({ haveAllAnnotationsOf(listOf("MyAnno", "OtherAnno")) }, ctxExt, "should have all annotations: MyAnno, OtherAnno")

        assertSuccess({ haveAllAnnotationsOf("MyAnno") }, ctxExt)
        assertFailure({ haveAllAnnotationsOf("MyAnno", "OtherAnno") }, ctxExt, "should have all annotations: MyAnno, OtherAnno")

        // 10. Test haveAnyAnnotationOf variants
        assertSuccess({ haveAnyAnnotationOf(listOf("MyAnno", "OtherAnno")) }, ctxExt)
        assertFailure({ haveAnyAnnotationOf(listOf("OtherAnno")) }, ctxExt, "should have at least one annotation of: OtherAnno")

        assertSuccess({ haveAnyAnnotationOf("MyAnno", "OtherAnno") }, ctxExt)
        assertFailure({ haveAnyAnnotationOf("OtherAnno") }, ctxExt, "should have at least one annotation of: OtherAnno")

        // 11. Test haveModifier, haveAllModifiers, haveAnyModifier
        assertSuccess({ haveModifier(Modifier.CONST) }, ctxExt)
        assertFailure({ haveModifier(Modifier.LATEINIT) }, ctxExt, "should have modifier: LATEINIT")

        assertSuccess({ haveAllModifiers(listOf(Modifier.CONST)) }, ctxExt)
        assertFailure(
            { haveAllModifiers(listOf(Modifier.CONST, Modifier.OPEN)) },
            ctxExt,
            "should have all modifiers: CONST, OPEN, but is missing: OPEN",
        )

        assertSuccess({ haveAllModifiers(Modifier.CONST) }, ctxExt)
        assertFailure({ haveAllModifiers(Modifier.CONST, Modifier.OPEN) }, ctxExt, "should have all modifiers: CONST, OPEN, but is missing: OPEN")

        assertSuccess({ haveAnyModifier(listOf(Modifier.CONST, Modifier.OPEN)) }, ctxExt)
        assertFailure({ haveAnyModifier(listOf(Modifier.OPEN)) }, ctxExt, "should have at least one modifier of: OPEN")

        assertSuccess({ haveAnyModifier(Modifier.CONST, Modifier.OPEN) }, ctxExt)
        assertFailure({ haveAnyModifier(Modifier.OPEN) }, ctxExt, "should have at least one modifier of: OPEN")

        // 12. Test haveVisibility, haveAnyVisibility
        assertSuccess({ haveVisibility(Visibility.PUBLIC) }, ctxExt)
        assertFailure({ haveVisibility(Visibility.PRIVATE) }, ctxExt, "should have visibility: PRIVATE but was: PUBLIC")

        assertSuccess({ haveAnyVisibility(listOf(Visibility.PUBLIC, Visibility.INTERNAL)) }, ctxExt)
        assertFailure({ haveAnyVisibility(listOf(Visibility.PRIVATE)) }, ctxExt, "should have any visibility of: PRIVATE but was: PUBLIC")

        assertSuccess({ haveAnyVisibility(Visibility.PUBLIC, Visibility.INTERNAL) }, ctxExt)
        assertFailure({ haveAnyVisibility(Visibility.PRIVATE) }, ctxExt, "should have any visibility of: PRIVATE but was: PUBLIC")

        // 13. Test beExtension, beConst, beLateinit, beDocumentedWithKDoc
        assertSuccess({ beExtension() }, ctxExt)
        assertFailure({ beExtension() }, ctxLate, "should be an extension property")

        assertSuccess({ beConst() }, ctxExt)
        assertFailure({ beConst() }, ctxLate, "should be const val")

        assertSuccess({ beLateinit() }, ctxLate)
        assertFailure({ beLateinit() }, ctxExt, "should be lateinit var")

        assertSuccess({ beDocumentedWithKDoc() }, ctxExt)
        assertFailure({ beDocumentedWithKDoc() }, ctxLate, "should be documented with KDoc")

        // 14. Test satisfy overloads
        assertSuccess({ satisfy { it.name == "extProp" } }, ctxExt)
        assertFailure({ satisfy { it.name == "other" } }, ctxExt, "should satisfy: custom condition")

        assertSuccess({ satisfy { p, v -> if (p.name != "extProp") v.add("error") } }, ctxExt)
        assertFailure({ satisfy { p, v -> v.add("error") } }, ctxExt, "error")
    }
}
