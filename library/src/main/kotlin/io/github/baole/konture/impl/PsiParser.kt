/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.AnnotationArgumentDeclaration
import io.github.baole.konture.AnnotationDeclaration
import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.ConstructorDeclaration
import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.FunctionDeclaration
import io.github.baole.konture.Modifier
import io.github.baole.konture.ParameterDeclaration
import io.github.baole.konture.PropertyDeclaration
import io.github.baole.konture.Visibility
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtTypeReference
import java.io.File

/**
 * A parser that uses the JetBrains Kotlin compiler AST/PSI (Program Structure Interface) infrastructure
 * to parse and extract structural metadata from Kotlin source files.
 *
 * This parser avoids compiling source code to bytecode, enabling lightweight and fast analysis of class structures,
 * annotations, imports, and type references directly from source files on disk.
 */
@OptIn(
    org.jetbrains.kotlin.config.CompilerConfiguration.Internals::class,
    org.jetbrains.kotlin.K1Deprecation::class,
)
internal object PsiParser {
    private val disposable = Disposer.newDisposable()
    private val project: Project by lazy {
        val configuration = CompilerConfiguration()
        val environment =
            KotlinCoreEnvironment.createForProduction(
                disposable,
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES,
            )
        environment.project
    }

    /**
     * Parses a Kotlin source file (`.kt`) and returns a [FileDeclaration] representing the complete file.
     *
     * @param file The Kotlin source file on disk to parse.
     * @return A [FileDeclaration] extracted from the source file, or null if the file does not exist.
     */
    fun parseFile(file: File): FileDeclaration? {
        if (!file.exists()) return null
        val content = file.readText()
        val ktFile =
            PsiFileFactory.getInstance(project).createFileFromText(
                file.name,
                KotlinFileType.INSTANCE,
                content,
            ) as KtFile

        val packageName = ktFile.packageFqName.asString()
        val imports: List<String> = ktFile.importDirectives.mapNotNull { it.importPath?.toString() }
        val importAliases =
            ktFile.importDirectives
                .mapNotNull { directive ->
                    val aliasName = directive.aliasName
                    val fqName = directive.importedFqName?.asString()
                    if (aliasName != null && fqName != null) {
                        aliasName to fqName
                    } else {
                        null
                    }
                }.toMap()

        val classes = mutableListOf<ClassDeclaration>()
        val topLevelFunctions = mutableListOf<FunctionDeclaration>()
        val topLevelProperties = mutableListOf<PropertyDeclaration>()

        ktFile.declarations.forEach { declaration ->
            when (declaration) {
                is KtClassOrObject -> {
                    parseClassOrObject(declaration, packageName, imports, file.absolutePath, importAliases)?.let {
                        classes.add(it)
                    }
                }

                is KtFunction -> {
                    topLevelFunctions.add(parseFunction(declaration, imports, packageName, importAliases))
                }

                is KtProperty -> {
                    topLevelProperties.add(parseProperty(declaration, imports, packageName, importAliases))
                }
            }
        }

        // Search for file-level KDoc
        val fileKDoc =
            ktFile.children
                .filterIsInstance<org.jetbrains.kotlin.kdoc.psi.impl.KDocImpl>()
                .firstOrNull()
                ?.text

        return FileDeclaration(
            name = file.name,
            packageName = packageName,
            imports = imports,
            classes = classes,
            topLevelFunctions = topLevelFunctions,
            topLevelProperties = topLevelProperties,
            kdocText = fileKDoc,
            filePath = file.absolutePath,
            importAliases = importAliases,
        )
    }

    private fun parseClassOrObject(
        classOrObject: KtClassOrObject,
        packageName: String,
        imports: List<String>,
        filePath: String,
        importAliases: Map<String, String> = emptyMap(),
    ): ClassDeclaration? {
        val simpleName = classOrObject.name ?: return null
        val fqName =
            classOrObject.fqName?.asString() ?: if (packageName.isNotEmpty()) "$packageName.$simpleName" else simpleName

        val isInterface = (classOrObject as? KtClass)?.isInterface() ?: false
        val isAbstract = classOrObject.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.ABSTRACT_KEYWORD)
        val isEnum = (classOrObject as? KtClass)?.isEnum() ?: false

        val annotations = parseAnnotations(classOrObject.annotationEntries, imports, packageName, importAliases)

        // Gather all referenced types in this class
        val referencedTypes = mutableSetOf<String>()
        classOrObject.accept(
            object : KtTreeVisitorVoid() {
                override fun visitTypeReference(typeReference: KtTypeReference) {
                    super.visitTypeReference(typeReference)
                    val typeText = typeReference.text
                    val tokens = typeText.split("<", ">", ",", "?", " ", "(", ")", "->").filter { it.isNotEmpty() }
                    tokens.forEach { token ->
                        val parts = token.split(".")
                        val simpleNamePart = parts.lastOrNull()
                        if (simpleNamePart != null && simpleNamePart.isNotEmpty() && simpleNamePart[0].isUpperCase()) {
                            referencedTypes.add(token)
                        }
                    }
                }

                override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
                    super.visitSimpleNameExpression(expression)
                    val text = expression.text
                    if (text.isNotEmpty() && text[0].isUpperCase()) {
                        referencedTypes.add(text)
                    }
                }
            },
        )

        val visibility = classOrObject.extractVisibility()
        val modifiers = classOrObject.extractModifiers()
        val supertypes = classOrObject.extractSupertypes()

        val primaryConstructor =
            classOrObject.primaryConstructor?.let {
                parseConstructor(it, imports, packageName, importAliases)
            }
        val secondaryConstructors =
            classOrObject.secondaryConstructors.map {
                parseConstructor(
                    it,
                    imports,
                    packageName,
                    importAliases,
                )
            }

        val functions =
            classOrObject.declarations.filterIsInstance<KtFunction>().map {
                parseFunction(it, imports, packageName, importAliases)
            }
        val properties =
            classOrObject.declarations.filterIsInstance<KtProperty>().map {
                parseProperty(it, imports, packageName, importAliases)
            }

        val companionObject =
            classOrObject.companionObjects.firstOrNull()?.let { companion ->
                parseClassOrObject(companion, packageName, imports, filePath, importAliases)
            }

        val kdocText = classOrObject.extractKDoc()

        return ClassDeclaration(
            name = simpleName,
            fqName = fqName,
            packageName = packageName,
            isInterface = isInterface,
            isAbstract = isAbstract,
            isEnum = isEnum,
            annotations = annotations,
            imports = imports,
            referencedTypes = referencedTypes,
            filePath = filePath,
            visibility = visibility,
            modifiers = modifiers,
            supertypes = supertypes,
            primaryConstructor = primaryConstructor,
            secondaryConstructors = secondaryConstructors,
            functions = functions,
            properties = properties,
            companionObject = companionObject,
            kdocText = kdocText,
            importAliases = importAliases,
        )
    }

    private fun parseAnnotations(
        entries: List<KtAnnotationEntry>,
        imports: List<String>,
        packageName: String,
        importAliases: Map<String, String> = emptyMap(),
    ): List<AnnotationDeclaration> =
        entries.map { entry ->
            val name = entry.typeReference?.text ?: ""
            var resolvedFqName = importAliases[name] ?: name
            if (resolvedFqName == name && !name.contains(".")) {
                val imported = imports.firstOrNull { it.endsWith(".$name") || it == name }
                if (imported != null) {
                    resolvedFqName = imported
                } else if (packageName.isNotEmpty()) {
                    resolvedFqName = "$packageName.$name"
                }
            }
            val arguments =
                entry.valueArguments.map { arg ->
                    val argName = arg.getArgumentName()?.asName?.asString()
                    val argValue = arg.getArgumentExpression()?.text ?: ""
                    AnnotationArgumentDeclaration(argName, argValue)
                }
            AnnotationDeclaration(name, resolvedFqName, arguments)
        }

    private fun parseParameters(
        psiParams: List<KtParameter>,
        imports: List<String>,
        packageName: String,
        importAliases: Map<String, String> = emptyMap(),
    ): List<ParameterDeclaration> =
        psiParams.map { param ->
            val name = param.name ?: ""
            val type = param.typeReference?.text ?: ""
            val hasDefaultValue = param.hasDefaultValue()
            val annotations = parseAnnotations(param.annotationEntries, imports, packageName, importAliases)
            ParameterDeclaration(name, type, hasDefaultValue, annotations)
        }

    private fun parseConstructor(
        constructor: KtConstructor<*>,
        imports: List<String>,
        packageName: String,
        importAliases: Map<String, String> = emptyMap(),
    ): ConstructorDeclaration {
        val visibility = constructor.extractVisibility()
        val parameters = parseParameters(constructor.valueParameters, imports, packageName, importAliases)
        val annotations = parseAnnotations(constructor.annotationEntries, imports, packageName, importAliases)
        return ConstructorDeclaration(visibility, parameters, annotations)
    }

    private fun parseFunction(
        function: KtFunction,
        imports: List<String>,
        packageName: String,
        importAliases: Map<String, String> = emptyMap(),
    ): FunctionDeclaration {
        val name = function.name ?: ""
        val visibility = function.extractVisibility()
        val modifiers = function.extractModifiers()
        val returnType = function.typeReference?.text ?: "Unit"
        val parameters = parseParameters(function.valueParameters, imports, packageName, importAliases)
        val annotations = parseAnnotations(function.annotationEntries, imports, packageName, importAliases)
        val kdocText = function.extractKDoc()
        val isExtension = function.receiverTypeReference != null
        return FunctionDeclaration(
            name,
            visibility,
            modifiers,
            returnType,
            parameters,
            annotations,
            kdocText,
            isExtension,
        )
    }

    private fun parseProperty(
        property: KtProperty,
        imports: List<String>,
        packageName: String,
        importAliases: Map<String, String> = emptyMap(),
    ): PropertyDeclaration {
        val name = property.name ?: ""
        val visibility = property.extractVisibility()
        val modifiers = property.extractModifiers()
        val type = property.typeReference?.text ?: ""
        val isVal = property.isVar.not()
        val annotations = parseAnnotations(property.annotationEntries, imports, packageName, importAliases)
        val kdocText = property.extractKDoc()
        val isExtension = property.receiverTypeReference != null
        return PropertyDeclaration(name, visibility, modifiers, type, isVal, annotations, kdocText, isExtension)
    }

    private fun KtModifierListOwner.extractVisibility(): Visibility {
        val modifierList = this.modifierList ?: return Visibility.PUBLIC
        return when {
            modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.PRIVATE_KEYWORD) -> Visibility.PRIVATE
            modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.PROTECTED_KEYWORD) -> Visibility.PROTECTED
            modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.INTERNAL_KEYWORD) -> Visibility.INTERNAL
            else -> Visibility.PUBLIC
        }
    }

    private fun KtModifierListOwner.extractModifiers(): Set<Modifier> {
        val modifierList = this.modifierList ?: return emptySet()
        val modifiers = mutableSetOf<Modifier>()
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.SEALED_KEYWORD)) modifiers.add(Modifier.SEALED)
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.OPEN_KEYWORD)) modifiers.add(Modifier.OPEN)
        if (modifierList.hasModifier(
                org.jetbrains.kotlin.lexer.KtTokens.ABSTRACT_KEYWORD,
            )
        ) {
            modifiers.add(Modifier.ABSTRACT)
        }
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.DATA_KEYWORD)) modifiers.add(Modifier.DATA)
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.VALUE_KEYWORD)) modifiers.add(Modifier.VALUE)
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.INNER_KEYWORD)) modifiers.add(Modifier.INNER)
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.INLINE_KEYWORD)) modifiers.add(Modifier.INLINE)
        if (modifierList.hasModifier(
                org.jetbrains.kotlin.lexer.KtTokens.SUSPEND_KEYWORD,
            )
        ) {
            modifiers.add(Modifier.SUSPEND)
        }
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.EXPECT_KEYWORD)) modifiers.add(Modifier.EXPECT)
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.ACTUAL_KEYWORD)) modifiers.add(Modifier.ACTUAL)
        if (modifierList.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.CONST_KEYWORD)) modifiers.add(Modifier.CONST)
        if (modifierList.hasModifier(
                org.jetbrains.kotlin.lexer.KtTokens.LATEINIT_KEYWORD,
            )
        ) {
            modifiers.add(Modifier.LATEINIT)
        }
        if (this is KtObjectDeclaration) {
            if (this.isCompanion()) {
                modifiers.add(Modifier.COMPANION)
            }
            modifiers.add(Modifier.OBJECT)
        }
        return modifiers
    }

    private fun KtClassOrObject.extractSupertypes(): List<String> =
        this.superTypeListEntries.mapNotNull {
            it.typeReference?.text
        }

    private fun KtElement.extractKDoc(): String? = (this as? KtDeclaration)?.docComment?.text

    /**
     * Disposes of the underlying IntelliJ compiler resources and environments.
     */
    fun dispose() {
        Disposer.dispose(disposable)
    }
}
