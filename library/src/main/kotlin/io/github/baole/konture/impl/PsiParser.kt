/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.ClassDeclaration
import io.github.baole.konture.FileDeclaration
import io.github.baole.konture.FunctionDeclaration
import io.github.baole.konture.Konture
import io.github.baole.konture.PropertyDeclaration
import io.github.baole.konture.impl.cache.IncrementalAstCache
import io.github.baole.konture.impl.cache.SourceHasher
import io.github.baole.konture.impl.psi.DeclarationParser
import io.github.baole.konture.impl.psi.DeclaredClassScanner
import io.github.baole.konture.impl.psi.PsiEnvironment
import io.github.baole.konture.impl.psi.SymbolLookup
import io.github.baole.konture.impl.psi.TypeAliasDefinition
import io.github.baole.konture.impl.psi.TypeAliasScanner
import io.github.baole.konture.impl.psi.TypeResolutionContext
import io.github.baole.konture.impl.psi.UsageExtractor
import org.jetbrains.kotlin.kdoc.psi.impl.KDocImpl
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
    fun getDeclaredClassFqNames(files: List<File>): Set<String> {
        val fqNames = mutableSetOf<String>()
        val isIncremental = Konture.incremental
        files.forEach { file ->
            if (file.exists() && file.name.endsWith(".kt")) {
                scanClassFqNames(file, isIncremental)?.let { fqNames.addAll(it) }
            }
        }
        return fqNames
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private fun scanClassFqNames(
        file: File,
        isIncremental: Boolean,
    ): Set<String>? {
        return try {
            val content = file.readText()
            val hash = if (isIncremental) SourceHasher.hashString(content) else null
            if (isIncremental && hash != null) {
                val cached = IncrementalAstCache.getClassFqNames(hash)
                if (cached != null) return cached
            }
            val ktFile = environment.createKtFile(file.name, content)
            val collected = DeclaredClassScanner.collectFqNames(ktFile)
            if (isIncremental && hash != null) {
                IncrementalAstCache.putClassFqNames(hash, collected)
            }
            collected
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Scans Kotlin files for type aliases and maps each alias FQ name to its declaration
     * context. The source scanner retains enclosing-class scopes because nested aliases are an
     * experimental language feature that may not be represented by all supported Kotlin PSI
     * versions. The target is resolved at use time so alias chains and
     * Kotlin's normal import precedence remain supported.
     */
    fun getDeclaredTypeAliases(files: List<File>): Map<String, TypeAliasDefinition> {
        val aliases = mutableMapOf<String, TypeAliasDefinition>()
        val isIncremental = Konture.incremental
        files.forEach { file ->
            if (file.exists() && file.name.endsWith(".kt")) {
                scanTypeAliases(file, isIncremental)?.let { aliases.putAll(it) }
            }
        }
        return aliases
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private fun scanTypeAliases(
        file: File,
        isIncremental: Boolean,
    ): Map<String, TypeAliasDefinition>? {
        return try {
            val content = file.readText()
            val hash = if (isIncremental) SourceHasher.hashString(content) else null
            if (isIncremental && hash != null) {
                val cached = IncrementalAstCache.getTypeAliases(hash)
                if (cached != null) return cached
            }
            val ktFile = environment.createKtFile(file.name, content)
            val scanned = TypeAliasScanner.scan(ktFile, content)
            if (isIncremental && hash != null) {
                IncrementalAstCache.putTypeAliases(hash, scanned)
            }
            scanned
        } catch (e: Exception) {
            null
        }
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
        val isIncremental = Konture.incremental
        val fileHash = if (isIncremental) SourceHasher.hashString(content) else null
        val cacheKey =
            if (fileHash != null) {
                val lookupKey = symbolLookup?.lookupKey() ?: "none"
                "${file.canonicalPath}:$fileHash:$lookupKey"
            } else {
                null
            }

        if (isIncremental && cacheKey != null) {
            val cached = IncrementalAstCache.getFileDeclaration(cacheKey)
            if (cached != null) {
                return cached
            }
        }

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
                resolveTypeAlias,
            )

        ktFile.declarations.forEach { declaration ->
            when (declaration) {
                is KtClassOrObject -> {
                    classes.addAll(
                        DeclarationParser.parseClassOrObjectWithNested(declaration, file.absolutePath, context),
                    )
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
                .filterIsInstance<KDocImpl>()
                .firstOrNull()
                ?.text

        val fileAnnotations =
            DeclarationParser.parseAnnotations(
                ktFile.fileAnnotationList?.annotationEntries ?: ktFile.annotationEntries,
                context,
            )

        val fileDecl =
            FileDeclaration(
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
                annotations = fileAnnotations,
            )

        if (isIncremental && cacheKey != null) {
            IncrementalAstCache.putFileDeclaration(cacheKey, fileDecl)
        }

        return fileDecl
    }

    /**
     * Disposes of the underlying IntelliJ compiler resources and environments.
     */
    fun dispose() {
        environment.dispose()
        IncrementalAstCache.clear()
    }
}
