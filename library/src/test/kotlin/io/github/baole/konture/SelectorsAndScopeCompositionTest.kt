/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SelectorsAndScopeCompositionTest {
    private fun mockClass(
        fqName: String,
        isInterface: Boolean = false,
        isAbstract: Boolean = false,
        isEnum: Boolean = false,
        isPublic: Boolean = true,
        visibility: Visibility = if (isPublic) Visibility.PUBLIC else Visibility.INTERNAL,
        modifiers: Set<Modifier> = emptySet(),
        annotations: List<AnnotationDeclaration> = emptyList(),
        dependencies: List<String> = emptyList(),
    ): ClassDeclaration {
        val simpleName = fqName.substringAfterLast('.')
        val pkg = if (fqName.contains('.')) fqName.substringBeforeLast('.') else ""
        return ClassDeclaration(
            name = simpleName,
            fqName = fqName,
            packageName = pkg,
            isInterface = isInterface,
            isAbstract = isAbstract,
            isEnum = isEnum,
            annotations = annotations,
            imports = dependencies.map { "import $it.*" },
            referencedTypes = dependencies.toSet(),
            filePath = "/src/$simpleName.kt",
            visibility = visibility,
            modifiers = modifiers,
        )
    }

    private fun mockModule(
        path: String,
        dependencies: List<String> = emptyList(),
        plugins: List<String> = emptyList(),
        files: List<FileDeclaration> = emptyList(),
    ): Module {
        return Module(
            buildId = "root",
            path = path,
            projectDir = path.replace(':', '/'),
            appliedPlugins = plugins,
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
        imports: List<String> = emptyList(),
        classes: List<ClassDeclaration> = emptyList(),
    ): FileDeclaration {
        return FileDeclaration(
            name = name,
            packageName = packageName,
            imports = imports,
            classes = classes,
            filePath = "/src/$packageName/$name",
        )
    }

    private fun mockFunction(
        name: String,
        packageName: String = "com.example",
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = emptySet(),
        annotations: List<AnnotationDeclaration> = emptyList(),
    ): FunctionDeclarationContext {
        val decl =
            FunctionDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                returnType = "Unit",
                parameters = emptyList(),
                annotations = annotations,
                kdocText = null,
                isExtension = false,
            )
        return FunctionDeclarationContext(
            declaration = decl,
            packageName = packageName,
            className = null,
            modulePath = ":app",
            filePath = "/src/$name.kt",
        )
    }

    private fun mockProperty(
        name: String,
        packageName: String = "com.example",
        visibility: Visibility = Visibility.PUBLIC,
        modifiers: Set<Modifier> = emptySet(),
        isVal: Boolean = true,
        annotations: List<AnnotationDeclaration> = emptyList(),
    ): PropertyDeclarationContext {
        val decl =
            PropertyDeclaration(
                name = name,
                visibility = visibility,
                modifiers = modifiers,
                type = "String",
                isVal = isVal,
                annotations = annotations,
                kdocText = null,
            )
        return PropertyDeclarationContext(
            declaration = decl,
            packageName = packageName,
            className = null,
            modulePath = ":app",
            filePath = "/src/$name.kt",
        )
    }

    @Test
    fun `class selector filtering is immutable and returns new instances`() {
        val class1 = mockClass("com.example.domain.UserRepository", isInterface = true)
        val class2 = mockClass("com.example.data.UserRepositoryImpl", isInterface = false)
        val initialScope = KontureScope(listOf(class1, class2))

        val domainClasses = initialScope.inPackage("..domain..")
        val interfaceClasses = domainClasses.interfaces()

        assertEquals(2, initialScope.classes.size)
        assertEquals(1, domainClasses.classes.size)
        assertEquals("UserRepository", domainClasses.classes.first().name)
        assertEquals(1, interfaceClasses.classes.size)
        assertNotSame(initialScope, domainClasses)
    }

    @Test
    fun `sequential should assertions execute independently on same selector instance`() {
        val repo1 = mockClass("com.example.domain.UserRepository", isInterface = true, isPublic = true)
        val repo2 = mockClass("com.example.domain.OrderRepository", isInterface = true, isPublic = true)
        val selector: ClassSelector = KontureScope(listOf(repo1, repo2))

        selector.should().beInterfaces()
        selector.should().bePublic()
        selector.should().haveNameEndingWith("Repository")
        selector.should().haveNameStartingWith("User", "Order")
        selector.should().haveNameMatching("*Repository")
        selector.should().notDependOnPackages("..data..")
        selector.should().notDependOnPackages(listOf("..data.."))
        selector.should().onlyDependOnPackages("..domain..", "..common..")
        selector.should().onlyDependOnPackages(listOf("..domain..", "..common.."))
        selector.should().beAccessedBy("..")
        selector.should().onlyBeAccessedByAnyPackage("..")
        selector.should().notBeAccessedByAnyPackage("..forbidden..")
        selector.should().resideInAPackage("..domain..")
        selector.should().haveVisibility(Visibility.PUBLIC)
    }

    @Test
    fun `class selector should assertions for modifier types`() {
        val enumClass = mockClass("com.example.Type", isEnum = true)
        val abstractClass = mockClass("com.example.Base", isAbstract = true)
        val sealedClass = mockClass("com.example.State", modifiers = setOf(Modifier.SEALED))
        val dataClass = mockClass("com.example.UserDto", modifiers = setOf(Modifier.DATA))
        val inlineClass = mockClass("com.example.UserId", modifiers = setOf(Modifier.VALUE))

        KontureScope(listOf(enumClass)).should().beEnums()
        KontureScope(listOf(abstractClass)).should().beAbstract()
        KontureScope(listOf(sealedClass)).should().beSealed()
        KontureScope(listOf(dataClass)).should().beData()
        KontureScope(listOf(inlineClass)).should().beInline()

        val internalClass = mockClass("com.example.InternalClass", isPublic = false, visibility = Visibility.INTERNAL)
        val privateClass = mockClass("com.example.PrivateClass", isPublic = false, visibility = Visibility.PRIVATE)
        val protectedClass =
            mockClass("com.example.ProtectedClass", isPublic = false, visibility = Visibility.PROTECTED)

        KontureScope(listOf(internalClass)).should().beInternal()
        KontureScope(listOf(privateClass)).should().bePrivate()
        KontureScope(listOf(protectedClass)).should().beProtected()
    }

    @Test
    fun `sequential should assertion fails when criteria is violated`() {
        val class1 = mockClass("com.example.domain.UserRepository", isInterface = true)
        val class2 = mockClass("com.example.domain.UserService", isInterface = false)
        val selector: ClassSelector = KontureScope(listOf(class1, class2))

        val error =
            assertThrows<AssertionError> {
                selector.should().beInterfaces()
            }
        assertTrue(error.message?.contains("UserService") == true)
    }

    @Test
    fun `set composition plus and minus operators combine and subtract selectors`() {
        val class1 = mockClass("com.example.domain.User")
        val class2 = mockClass("com.example.domain.Order")
        val class3 = mockClass("com.example.data.UserEntity")

        val selectorA: ClassSelector = KontureScope(listOf(class1, class2))
        val selectorB: ClassSelector = KontureScope(listOf(class2, class3))

        val combined = selectorA + selectorB
        assertEquals(3, combined.classes.size)

        val subtracted = combined - selectorB
        assertEquals(1, subtracted.classes.size)
        assertEquals("User", subtracted.classes.first().name)
    }

    @Test
    fun `module selector fluent extensions and should assertions`() {
        val fileInDomain = mockFile("User.kt", "com.example.domain")
        val modA =
            mockModule(
                ":core:domain",
                dependencies = listOf(":core:common"),
                plugins = listOf("org.jetbrains.kotlin.jvm"),
                files = listOf(fileInDomain),
            )
        val modB =
            mockModule(":feature:user", dependencies = listOf(":core:domain"), plugins = listOf("com.android.library"))

        val selector: ModuleSelector = KontureModuleScope(listOf(modA, modB))

        val domainModule = selector.inPackage("..domain..")
        assertEquals(1, domainModule.modules.size)

        val featureModule = selector.withName(":feature:*")
        assertEquals(1, featureModule.modules.size)

        selector.should().notDependOnModules(":data:database")
        selector.should().notDependOnModule(":data:database")
        selector.should().onlyDependOnModules(":core:common", ":core:domain")

        KontureModuleScope(listOf(modA)).should().havePlugin("org.jetbrains.kotlin.jvm")
        KontureModuleScope(listOf(modA)).should().notHavePlugin("com.android.application")

        assertThrows<AssertionError> {
            selector.should().notDependOnModules(":core:domain")
        }
        assertThrows<AssertionError> {
            KontureModuleScope(listOf(modA)).should().onlyDependOnModules(":none")
        }
        assertThrows<AssertionError> {
            KontureModuleScope(listOf(modA)).should().havePlugin("com.android.application")
        }
        assertThrows<AssertionError> {
            KontureModuleScope(listOf(modA)).should().notHavePlugin("org.jetbrains.kotlin.jvm")
        }
    }

    @Test
    fun `file selector fluent extensions and should assertions`() {
        val file1 = mockFile("UserRepository.kt", "com.example.domain", imports = listOf("com.example.model.User"))
        val file2 = mockFile("OrderRepository.kt", "com.example.domain", imports = listOf("com.example.model.Order"))
        val selector: FileSelector = KontureFileScope(listOf(file1, file2))

        val domainFiles = selector.inPackage("..domain..")
        assertEquals(2, domainFiles.files.size)

        val userFiles = selector.withName("User*")
        assertEquals(1, userFiles.files.size)

        selector.should().resideInAPackage("..domain..")
        selector.should().haveNameMatching("*Repository.kt")
        selector.should().haveNameEndingWith("Repository.kt")
        selector.should().haveNameStartingWith("User", "Order")
        selector.should().haveNoWildcardImports()
        selector.should().haveOnlyOneClassPerFile()
    }

    @Test
    fun `function selector fluent extensions and should assertions`() {
        val fn1 =
            mockFunction(
                "save",
                "com.example.domain",
                visibility = Visibility.PUBLIC,
                modifiers =
                    setOf(
                        Modifier.SUSPEND,
                        Modifier.INLINE,
                        Modifier.OPERATOR,
                        Modifier.INFIX,
                        Modifier.OVERRIDE,
                    ),
            )
        val fn2 = mockFunction("internalFn", "com.example.domain", visibility = Visibility.INTERNAL)
        val fn3 = mockFunction("privateFn", "com.example.domain", visibility = Visibility.PRIVATE)
        val fn4 = mockFunction("protectedFn", "com.example.domain", visibility = Visibility.PROTECTED)

        val scope = KontureFunctionScope(listOf(fn1, fn2, fn3, fn4))

        val domainFns = scope.inPackage("..domain..")
        assertEquals(4, domainFns.functions.size)

        val saveFns = scope.withName("save")
        assertEquals(1, saveFns.functions.size)

        val publicScope = KontureFunctionScope(listOf(fn1))
        publicScope.should().bePublic()
        publicScope.should().beSuspend()
        publicScope.should().beInline()
        publicScope.should().beOperator()
        publicScope.should().beInfix()
        publicScope.should().beOverride()
        publicScope.should().haveNameMatching("save")

        KontureFunctionScope(listOf(fn2)).should().beInternal()
        KontureFunctionScope(listOf(fn3)).should().bePrivate()
        KontureFunctionScope(listOf(fn4)).should().beProtected()
    }

    @Test
    fun `property selector fluent extensions and should assertions`() {
        val prop1 = mockProperty("id", "com.example.domain", visibility = Visibility.PUBLIC, isVal = true)
        val prop2 = mockProperty("name", "com.example.domain", visibility = Visibility.INTERNAL, isVal = false)
        val prop3 =
            mockProperty(
                "secret",
                "com.example.domain",
                visibility = Visibility.PRIVATE,
                modifiers = setOf(Modifier.CONST),
            )
        val prop4 =
            mockProperty(
                "cached",
                "com.example.domain",
                visibility = Visibility.PROTECTED,
                modifiers = setOf(Modifier.LATEINIT),
            )

        val scope = KonturePropertyScope(listOf(prop1, prop2, prop3, prop4))

        val domainProps = scope.inPackage("..domain..")
        assertEquals(4, domainProps.properties.size)

        val nameProps = scope.withName("name")
        assertEquals(1, nameProps.properties.size)

        KonturePropertyScope(listOf(prop1)).should().bePublic()
        KonturePropertyScope(listOf(prop1)).should().beVal()
        KonturePropertyScope(listOf(prop1)).should().haveNameMatching("id")

        KonturePropertyScope(listOf(prop2)).should().beInternal()
        KonturePropertyScope(listOf(prop2)).should().beVar()

        KonturePropertyScope(listOf(prop3)).should().bePrivate()
        KonturePropertyScope(listOf(prop3)).should().beConst()

        KonturePropertyScope(listOf(prop4)).should().beProtected()
        KonturePropertyScope(listOf(prop4)).should().beLateinit()
    }

    @Test
    fun `slice selector fluent extensions and should assertions`() {
        val classA = mockClass("com.example.featureA.ClassA")
        val classB = mockClass("com.example.featureB.ClassB")

        val sliceA = Slice("featureA", setOf("com.example.featureA"), listOf(classA))
        val sliceB = Slice("featureB", setOf("com.example.featureB"), listOf(classB))

        val selector: SliceSelector = KontureSliceScope(listOf(sliceA, sliceB))

        val featureASlice = selector.withName("featureA")
        assertEquals(1, featureASlice.slices.size)

        selector.should().beFreeOfCycles()
        selector.should().notHaveCycles()
    }

    @Test
    fun `end to end selector workflow matching issue 62 example`() {
        val repoInterface = mockClass("com.example.domain.UserRepository", isInterface = true, isPublic = true)
        val domainEntity = mockClass("com.example.domain.User", isInterface = false, isPublic = true)
        val dataImpl =
            mockClass(
                "com.example.data.UserRepositoryImpl",
                isInterface = false,
                isPublic = true,
                dependencies = listOf("com.example.domain"),
            )

        val allScope = KontureScope(listOf(repoInterface, domainEntity, dataImpl))

        val domainClasses = allScope.inPackage("..domain..")
        val publicDomainClasses = domainClasses.public()

        publicDomainClasses.should().notDependOnPackages("..data..")

        val repositories = allScope.withName("*Repository").inPackage("..domain..")

        repositories.should().beInterfaces()
        repositories.should().bePublic()
        repositories.should().notDependOnPackages("..data..")
    }
}
