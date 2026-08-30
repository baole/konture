/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnifiedPatternMatchersTest {
    private fun mockClass(
        fqName: String,
        isInterface: Boolean = false,
        dependencies: List<String> = emptyList(),
    ): ClassDeclaration {
        val simpleName = fqName.substringAfterLast('.')
        val pkg = if (fqName.contains('.')) fqName.substringBeforeLast('.') else ""
        return ClassDeclaration(
            name = simpleName,
            fqName = fqName,
            packageName = pkg,
            isInterface = isInterface,
            isAbstract = false,
            isEnum = false,
            annotations = emptyList(),
            imports = dependencies.map { "import $it.*" },
            referencedTypes = dependencies.toSet(),
            filePath = "/src/$simpleName.kt",
            visibility = Visibility.PUBLIC,
            modifiers = emptySet(),
        )
    }

    private fun mockModule(
        path: String,
        dependencies: List<String> = emptyList(),
        files: List<FileDeclaration> = emptyList(),
    ): Module {
        return Module(
            buildId = "root",
            path = path,
            projectDir = path.replace(':', '/'),
            appliedPlugins = listOf("org.jetbrains.kotlin.jvm"),
            sourceSets = emptyList(),
            dependencies =
                dependencies.map {
                    Dependency(configuration = "implementation", targetBuildId = "root", targetPath = it)
                },
            files = files,
        )
    }

    private fun mockFile(
        name: String,
        packageName: String,
        classes: List<ClassDeclaration> = emptyList(),
    ): FileDeclaration {
        return FileDeclaration(
            name = name,
            packageName = packageName,
            imports = emptyList(),
            classes = classes,
            filePath = "/src/$packageName/$name",
        )
    }

    @Test
    fun `modules under filters module selectors correctly`() {
        val modFeatureCheckout = mockModule(":feature:checkout")
        val modFeatureProfile = mockModule(":feature:profile")
        val modCore = mockModule(":core:database")

        val selector: ModuleSelector = KontureModuleScope(listOf(modFeatureCheckout, modFeatureProfile, modCore))

        val featureModules = selector.withName(modules.under(":feature"))
        assertEquals(2, featureModules.modules.size)
        assertTrue(featureModules.modules.any { it.path == ":feature:checkout" })
        assertTrue(featureModules.modules.any { it.path == ":feature:profile" })

        val coreModules = selector.withName(Modules.under(":core"))
        assertEquals(1, coreModules.modules.size)
        assertEquals(":core:database", coreModules.modules.first().path)
    }

    @Test
    fun `packages under filters class selectors correctly`() {
        val classDomainModel = mockClass("com.acme.domain.model.User")
        val classDomainRepo = mockClass("com.acme.domain.repository.UserRepository", isInterface = true)
        val classFeatureUI = mockClass("com.acme.feature.checkout.CheckoutScreen")

        val selector: ClassSelector = KontureScope(listOf(classDomainModel, classDomainRepo, classFeatureUI))

        val domainClasses = selector.inPackage(packages.under("com.acme.domain"))
        assertEquals(2, domainClasses.classes.size)
        assertTrue(domainClasses.classes.any { it.fqName == "com.acme.domain.model.User" })
        assertTrue(domainClasses.classes.any { it.fqName == "com.acme.domain.repository.UserRepository" })

        val featureClasses = selector.inPackage(Packages.under("com.acme.feature"))
        assertEquals(1, featureClasses.classes.size)
        assertEquals("com.acme.feature.checkout.CheckoutScreen", featureClasses.classes.first().fqName)
    }

    @Test
    fun `modules under and packages under integrate with LayerSelectorDsl`() {
        val policy = ArchitectureLayerPolicy("feature")
        policy.selector {
            modules(modules.under(":feature"))
            packages(packages.under("com.acme.feature"))
        }

        val snapshot = policy.selectorSnapshot()
        assertEquals(listOf(":feature:**"), snapshot.modulePatterns)
        assertEquals(listOf("com.acme.feature.."), snapshot.packagePatterns)
    }

    @Test
    fun `modules and packages under multi-argument overloads with LayerSelectorDsl`() {
        val policy = ArchitectureLayerPolicy("core_and_feature")
        policy.selector {
            modules(modules.under(":core", ":feature"))
            packages(packages.under("com.acme.core", "com.acme.feature"))
        }

        val snapshot = policy.selectorSnapshot()
        assertEquals(listOf(":core:**", ":feature:**"), snapshot.modulePatterns)
        assertEquals(listOf("com.acme.core..", "com.acme.feature.."), snapshot.packagePatterns)
    }

    @Test
    fun `file selector inPackage supports packages under`() {
        val fileDomain = mockFile("User.kt", "com.acme.domain.model")
        val fileFeature = mockFile("Checkout.kt", "com.acme.feature.checkout")

        val selector: FileSelector = KontureFileScope(listOf(fileDomain, fileFeature))
        val domainFiles = selector.inPackage(packages.under("com.acme.domain"))

        assertEquals(1, domainFiles.files.size)
        assertEquals("User.kt", domainFiles.files.first().name)
    }
}
