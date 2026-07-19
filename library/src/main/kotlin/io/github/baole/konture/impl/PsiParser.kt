/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.FunctionDeclaration
import io.github.baole.konture.PropertyDeclaration
import io.github.baole.konture.impl.psi.DeclarationParser
import io.github.baole.konture.impl.psi.DeclaredClassScanner
import io.github.baole.konture.impl.psi.PsiEnvironment
import io.github.baole.konture.impl.psi.SymbolLookup
import io.github.baole.konture.impl.psi.TypeAliasDefinition
import io.github.baole.konture.impl.psi.TypeAliasScanner
import io.github.baole.konture.impl.psi.TypeResolutionContext
import io.github.baole.konture.impl.psi.UsageExtractor
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import java.io.File

/**
 * A parser that uses the JetBrains Kotlin compiler AST/PSI (Program Structure Interface) infrastructure
 * to parse and extract structural metadata from Kotlin source files.
 *
 * This parser avoids compiling source code to bytecode, enabling lightweight and fast analysis of class structures,
 * annotations, imports, and type references directly from source files on disk.
 */
internal object PsiParser {
    private val environment = PsiEnvironment()

    /**
     * Scans a list of files to quickly extract all fully-qualified class names declared in them.
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun getDeclaredClassFqNames(files: List<File>): Set<String> {
        val fqNames = mutableSetOf<String>()
        files.forEach { file ->
            if (file.exists() && file.name.endsWith(".kt")) {
                try {
                    val content = file.readText()
                    val ktFile = environment.createKtFile(file.name, content)
                    fqNames.addAll(DeclaredClassScanner.collectFqNames(ktFile))
                } catch (e: Exception) {
                    // Ignore parsing issues for individual files in global scan
                }
            }
        }
        return fqNames
    }

    /**
     * Scans Kotlin files for type aliases and maps each alias FQ name to its declaration
     * context. The source scanner retains enclosing-class scopes because nested aliases are an
     * experimental language feature that may not be represented by all supported Kotlin PSI
     * versions. The target is resolved at use time so alias chains and
     * Kotlin's normal import precedence remain supported.
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    fun getDeclaredTypeAliases(files: List<File>): Map<String, TypeAliasDefinition> {
        val aliases = mutableMapOf<String, TypeAliasDefinition>()
        files.forEach { file ->
            if (!file.exists() || !file.name.endsWith(".kt")) return@forEach
            try {
                val content = file.readText()
                val ktFile = environment.createKtFile(file.name, content)
                aliases.putAll(TypeAliasScanner.scan(ktFile, content))
            } catch (e: Exception) {
                // Ignore parsing issues for individual files in global scan.
            }
        }
        return aliases
    }

    /**
     * Parses a Kotlin source file (`.kt`) and returns a [FileDeclaration] representing the complete file.
     *
     * @param file The Kotlin source file on disk to parse.
     * @param symbolLookup Global project-wide symbol lookup to assist with type resolution.
     * @return A [FileDeclaration] extracted from the source file, or null if the file does not exist.
     */
    fun parseFile(
        file: File,
        symbolLookup: SymbolLookup? = null,
    ): FileDeclaration? {
        if (!file.exists()) return null
        val content = file.readText()
        val ktFile = environment.createKtFile(file.name, content)

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
        val fileDeclaredFqNames = DeclaredClassScanner.collectFqNames(ktFile)
        val fileTypeAliases = TypeAliasScanner.scan(ktFile, content)

        val isClassDeclared = { fqName: String ->
            fileDeclaredFqNames.contains(fqName) || (symbolLookup?.isClassDeclared(fqName) ?: false)
        }
        val resolveTypeAlias = { fqName: String -> fileTypeAliases[fqName] ?: symbolLookup?.resolveTypeAlias(fqName) }

        val context =
            TypeResolutionContext(
                packageName = packageName,
                imports = imports,
                importAliases = importAliases,
                isClassDeclared = isClassDeclared,
                resolveTypeAlias = resolveTypeAlias,
            )

        val classes = mutableListOf<ClassDeclaration>()
        val topLevelFunctions = mutableListOf<FunctionDeclaration>()
        val topLevelProperties = mutableListOf<PropertyDeclaration>()
        val usages =
            UsageExtractor.extract(
                ktFile,
                content,
                packageName,
                imports,
                importAliases,
                file.absolutePath,
                isClassDeclared,
            )

        ktFile.declarations.forEach { declaration ->
            when (declaration) {
                is KtClassOrObject -> {
                    DeclarationParser.parseClassOrObject(declaration, file.absolutePath, context)?.let {
                        classes.add(it)
                    }
                }

                is KtFunction -> {
                    topLevelFunctions.add(DeclarationParser.parseFunction(declaration, context))
                }

                is KtProperty -> {
                    topLevelProperties.add(DeclarationParser.parseProperty(declaration, context))
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
            usages = usages,
        )
    }

    /**
     * Disposes of the underlying IntelliJ compiler resources and environments.
     */
    fun dispose() {
        environment.dispose()
    }
}
