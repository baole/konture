/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SampleTestAnnotation

class ApiGapFullCompletionTest {
    @Test
    fun `test files that haveName overloads`() {
        val file = FileDeclaration(name = "SampleFile.kt", packageName = "com.example", filePath = "src/SampleFile.kt")
        val context = FileDeclarationContext(file, ":app", null)

        val builder1 = FilesRuleBuilder(ProjectGraph(emptyMap()))
        builder1.that().haveName("SampleFile.kt")
        assertTrue(builder1.getThatPredicate()?.invoke(context) == true)

        val builder2 = FilesRuleBuilder(ProjectGraph(emptyMap()))
        builder2.that().haveName(listOf("SampleFile.kt", "OtherFile.kt"))
        assertTrue(builder2.getThatPredicate()?.invoke(context) == true)

        val builder3 = FilesRuleBuilder(ProjectGraph(emptyMap()))
        builder3.that().haveName("OtherFile.kt")
        assertFalse(builder3.getThatPredicate()?.invoke(context) == true)
    }

    @Test
    fun `test files should notResideInAPackage and notResideInAModule`() {
        val file =
            FileDeclaration(
                name = "SampleFile.kt",
                packageName = "com.example.internal",
                filePath = "src/SampleFile.kt",
            )
        val context = FileDeclarationContext(file, ":app", null)

        val builder = FilesRuleBuilder(ProjectGraph(emptyMap()))
        builder.should().notResideInAPackage("com.example.legacy..")
        builder.should().notResideInAModule(":legacy")

        val violations = mutableListOf<String>()
        builder.getShouldAssertion()?.invoke(context, listOf(context), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `test classesThat haveAnnotationOf overloads and classScope aliases`() {
        val annot = AnnotationDeclaration("SampleTestAnnotation", "io.github.baole.konture.SampleTestAnnotation")
        val cls =
            ClassDeclaration(
                name = "SampleClass",
                fqName = "com.example.SampleClass",
                packageName = "com.example",
                isInterface = false,
                isAbstract = false,
                annotations = listOf(annot),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "src/SampleClass.kt",
            )
        val mod = Module("build", ":app", "/project/app", emptyList(), emptyList(), emptyList())
        val graph = ProjectGraph(mapOf("build" to listOf(mod)))

        val builder = ClassesRuleBuilder(graph)
        builder.that().haveAnnotationOf<SampleTestAnnotation>()
        assertTrue(builder.getThatPredicate()?.invoke(cls) == true)

        val scope1 = KontureScope.fromProject(graph)
        assertNotNull(scope1)
        val scope2 = KontureScope.fromModule(":app", graph)
        assertNotNull(scope2)
        val scope3 = KontureScope.fromPackage("com.example", graph)
        assertNotNull(scope3)
    }

    @Test
    fun `test functionsThat haveExtensionReceiver and reified notReferenceClass`() {
        val funcDecl =
            FunctionDeclaration(
                name = "toUpperCustom",
                visibility = Visibility.PUBLIC,
                modifiers = emptySet(),
                returnType = "String",
                parameters = emptyList(),
                annotations = emptyList(),
                kdocText = null,
                isExtension = true,
                receiverType = "kotlin.String",
            )
        val funcCtx = FunctionDeclarationContext(funcDecl, "com.example", null, ":app", "src/File.kt", null)

        val builder = FunctionsRuleBuilder(ProjectGraph(emptyMap()))
        builder.that().haveExtensionReceiver<String>()
        assertTrue(builder.getThatPredicate()?.invoke(funcCtx) == true)

        val shouldBuilder = FunctionsRuleBuilder(ProjectGraph(emptyMap()))
        shouldBuilder.should().notReferenceClass<SampleTestAnnotation>()
        val violations = mutableListOf<String>()
        shouldBuilder.getShouldAssertion()?.invoke(funcCtx, listOf(funcCtx), violations)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `test KontureModuleScope and Konture moduleScope`() {
        val mod1 = Module("build", ":app", "/project/app", emptyList(), emptyList(), emptyList())
        val mod2 = Module("build", ":core", "/project/core", listOf("kotlin"), emptyList(), emptyList())
        val graph = ProjectGraph(mapOf("build" to listOf(mod1, mod2)))

        val scope = KontureModuleScope.fromProject(graph)
        assertEquals(2, scope.modules.size)

        val filtered = scope.byPath(":app")
        assertEquals(1, filtered.modules.size)
        assertEquals(":app", filtered.modules.first().path)

        val pluginFiltered = scope.withPlugin("kotlin")
        assertEquals(1, pluginFiltered.modules.size)

        scope.assertAny { it.path == ":app" }
        scope.assertNone { it.path == ":unknown" }
    }

    @Test
    fun `test SlicesRuleBuilder with SlicesThat, SlicesFluent, and KontureSliceScope`() {
        val cls1 =
            ClassDeclaration(
                name = "FeatureAClass",
                fqName = "com.acme.featurea.FeatureAClass",
                packageName = "com.acme.featurea",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "src/A.kt",
            )
        val cls2 =
            ClassDeclaration(
                name = "FeatureBClass",
                fqName = "com.acme.featureb.FeatureBClass",
                packageName = "com.acme.featureb",
                isInterface = false,
                isAbstract = false,
                annotations = emptyList(),
                imports = emptyList(),
                referencedTypes = emptySet(),
                filePath = "src/B.kt",
            )
        val file1 =
            FileDeclaration(
                name = "A.kt",
                packageName = "com.acme.featurea",
                filePath = "src/A.kt",
                classes = listOf(cls1),
            )
        val file2 =
            FileDeclaration(
                name = "B.kt",
                packageName = "com.acme.featureb",
                filePath = "src/B.kt",
                classes = listOf(cls2),
            )
        val mod =
            Module(
                buildId = "build",
                path = ":app",
                projectDir = "/project/app",
                appliedPlugins = emptyList(),
                sourceSets = emptyList(),
                dependencies = emptyList(),
                files = listOf(file1, file2),
            )
        val graph = ProjectGraph(mapOf("build" to listOf(mod)))

        val sliceScope = KontureSliceScope.fromProject("com.acme.(*)..", graph)
        assertEquals(2, sliceScope.slices.size)

        val builder = SlicesRuleBuilder(graph)
        builder.matching("com.acme.(*)..")
        builder.that().haveKey("featurea")
        builder.should().beFreeOfCycles()

        builder.that { key == "featurea" }
        builder.should {
            check(slices.isNotEmpty(), "slices should not be empty")
        }
    }
}
