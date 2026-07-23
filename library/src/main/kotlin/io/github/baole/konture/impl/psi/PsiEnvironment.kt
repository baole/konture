/*
 * Copyright 2026 Bao Le Duc
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl.psi

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile

/** Owns the Kotlin compiler PSI project used for source parsing. */
@OptIn(
    CompilerConfiguration.Internals::class,
    org.jetbrains.kotlin.K1Deprecation::class,
)
internal class PsiEnvironment {
    private var disposable = Disposer.newDisposable()
    private var isDisposed = false
    private var projectInstance: Project? = null

    fun createKtFile(
        fileName: String,
        content: String,
    ): KtFile = PsiFileFactory.getInstance(project).createFileFromText(fileName, KotlinFileType.INSTANCE, content) as KtFile

    fun dispose() {
        if (!isDisposed) {
            Disposer.dispose(disposable)
            isDisposed = true
            projectInstance = null
        }
    }

    private val project: Project
        get() {
            if (projectInstance == null || isDisposed) {
                disposable = Disposer.newDisposable()
                isDisposed = false
                val configuration = CompilerConfiguration()
                configuration.put(
                    CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS,
                    LanguageVersionSettingsImpl(
                        LanguageVersionSettingsImpl.DEFAULT.languageVersion,
                        LanguageVersionSettingsImpl.DEFAULT.apiVersion,
                        emptyMap(),
                        mapOf(LanguageFeature.NestedTypeAliases to LanguageFeature.State.ENABLED),
                    ),
                )
                projectInstance =
                    KotlinCoreEnvironment.createForProduction(
                        disposable,
                        configuration,
                        EnvironmentConfigFiles.JVM_CONFIG_FILES,
                    ).project
            }
            return requireNotNull(projectInstance)
        }
}
