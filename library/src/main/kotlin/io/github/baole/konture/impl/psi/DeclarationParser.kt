/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

import io.github.baole.konture.AnnotationArgumentDeclaration
import io.github.baole.konture.AnnotationDeclaration
import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.ConstructorDeclaration
import io.github.baole.konture.FunctionDeclaration
import io.github.baole.konture.Modifier
import io.github.baole.konture.ParameterDeclaration
import io.github.baole.konture.PropertyDeclaration
import io.github.baole.konture.Visibility
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtTypeReference

internal object DeclarationParser {
    fun parseClassOrObject(
        classOrObject: KtClassOrObject,
        filePath: String,
        context: TypeResolutionContext,
    ): ClassDeclaration? {
        val simpleName = classOrObject.name ?: return null
        val fqName =
            classOrObject.fqName?.asString() ?: if (context.packageName.isNotEmpty()) "${context.packageName}.$simpleName" else simpleName

        // Create the nested resolution context for class/object lexical scope
        val classContext = context.withClassScope(fqName)

        val isInterface = (classOrObject as? KtClass)?.isInterface() ?: false
        val isAbstract = classOrObject.hasModifier(org.jetbrains.kotlin.lexer.KtTokens.ABSTRACT_KEYWORD)
        val isEnum = (classOrObject as? KtClass)?.isEnum() ?: false

        val annotations =
            parseAnnotations(
                classOrObject.annotationEntries,
                classContext,
            )

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
        val supertypes =
            classOrObject.extractSupertypes().map { supertype ->
                TypeResolver.resolveRawType(
                    supertype,
                    classContext,
                ) ?: supertype
            }

        val primaryConstructor =
            classOrObject.primaryConstructor?.let {
                parseConstructor(
                    it,
                    classContext,
                )
            }
        val secondaryConstructors =
            classOrObject.secondaryConstructors.map {
                parseConstructor(
                    it,
                    classContext,
                )
            }

        val functions =
            classOrObject.declarations.filterIsInstance<KtFunction>().map {
                parseFunction(
                    it,
                    classContext,
                )
            }
        val properties =
            classOrObject.declarations.filterIsInstance<KtProperty>().map {
                parseProperty(
                    it,
                    classContext,
                )
            }

        val companionObject =
            classOrObject.companionObjects.firstOrNull()?.let { companion ->
                parseClassOrObject(
                    companion,
                    filePath,
                    classContext,
                )
            }

        val kdocText = classOrObject.extractKDoc()

        return ClassDeclaration(
            name = simpleName,
            fqName = fqName,
            packageName = context.packageName,
            isInterface = isInterface,
            isAbstract = isAbstract,
            isEnum = isEnum,
            annotations = annotations,
            imports = context.imports,
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
            importAliases = context.importAliases,
        )
    }

    fun parseAnnotations(
        entries: List<KtAnnotationEntry>,
        context: TypeResolutionContext,
    ): List<AnnotationDeclaration> =
        entries.map { entry ->
            val name = entry.typeReference?.text ?: ""
            val resolvedFqName =
                TypeResolver.resolveRawType(name, context) ?: name
            val arguments =
                entry.valueArguments.map { arg ->
                    val argName = arg.getArgumentName()?.asName?.asString()
                    val argValue = arg.getArgumentExpression()?.text ?: ""
                    AnnotationArgumentDeclaration(argName, argValue)
                }
            AnnotationDeclaration(name, resolvedFqName, arguments)
        }

    fun parseParameters(
        psiParams: List<KtParameter>,
        context: TypeResolutionContext,
    ): List<ParameterDeclaration> =
        psiParams.map { param ->
            val name = param.name ?: ""
            val type = param.typeReference?.text ?: ""
            val resolvedType =
                TypeResolver.resolveRawType(type, context)
            val hasDefaultValue = param.hasDefaultValue()
            val annotations =
                parseAnnotations(
                    param.annotationEntries,
                    context,
                )
            ParameterDeclaration(name, type, hasDefaultValue, annotations, resolvedType)
        }

    fun parseConstructor(
        constructor: KtConstructor<*>,
        context: TypeResolutionContext,
    ): ConstructorDeclaration {
        val visibility = constructor.extractVisibility()
        val parameters =
            parseParameters(
                constructor.valueParameters,
                context,
            )
        val annotations =
            parseAnnotations(
                constructor.annotationEntries,
                context,
            )
        return ConstructorDeclaration(visibility, parameters, annotations)
    }

    fun parseFunction(
        function: KtFunction,
        context: TypeResolutionContext,
    ): FunctionDeclaration {
        val name = function.name ?: ""
        val visibility = function.extractVisibility()
        val modifiers = function.extractModifiers()
        val returnType = function.typeReference?.text ?: "Unit"
        val resolvedReturnType =
            TypeResolver.resolveRawType(returnType, context)
        val parameters =
            parseParameters(
                function.valueParameters,
                context,
            )
        val annotations =
            parseAnnotations(
                function.annotationEntries,
                context,
            )
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
            function.textRange.startOffset,
            function.textRange.endOffset,
            resolvedReturnType,
        )
    }

    fun parseProperty(
        property: KtProperty,
        context: TypeResolutionContext,
    ): PropertyDeclaration {
        val name = property.name ?: ""
        val visibility = property.extractVisibility()
        val modifiers = property.extractModifiers()
        val type = property.typeReference?.text ?: ""
        val resolvedType =
            TypeResolver.resolveRawType(type, context)
        val isVal = property.isVar.not()
        val annotations =
            parseAnnotations(
                property.annotationEntries,
                context,
            )
        val kdocText = property.extractKDoc()
        val isExtension = property.receiverTypeReference != null
        return PropertyDeclaration(name, visibility, modifiers, type, isVal, annotations, kdocText, isExtension, resolvedType)
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
}
