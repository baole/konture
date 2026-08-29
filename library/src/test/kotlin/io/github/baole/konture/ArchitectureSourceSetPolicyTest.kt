/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole), Octavio Calleya Garcia (@octaviospain)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

class ArchitectureSourceSetPolicyTest : RuleBuildersTestBase() {
    private fun sourceSetId(
        modulePath: String,
        name: String,
    ) = SourceSetId(modulePath, name, SourceSetKind.KMP, SourceSetRole.PRODUCTION)

    private fun mockClass(
        name: String,
        packageName: String,
        imports: List<String> = emptyList(),
        filePath: String = "/src/$name.kt",
    ) = ClassDeclaration(
        name = name,
        fqName = "$packageName.$name",
        packageName = packageName,
        isInterface = false,
        isAbstract = false,
        annotations = emptyList(),
        imports = imports,
        referencedTypes = emptySet(),
        filePath = filePath,
    )

    private fun mockModule(
        path: String,
        files: List<FileDeclaration>,
        sourceSets: List<SourceSet> = emptyList(),
    ) = Module(
        buildId = ":",
        path = path,
        projectDir = path.removePrefix(":"),
        appliedPlugins = listOf("kotlin-multiplatform"),
        sourceSets = sourceSets,
        dependencies = emptyList(),
        files = files,
    )

    private fun graphWith(modules: List<Module>): ProjectGraph =
        ProjectGraph(builds = mapOf(":" to modules)).also { ProjectGraph.setDefault(it) }

    @Test
    fun `mustBePlatformIndependent passes for pure common code`() {
        val commonFile =
            FileDeclaration(
                name = "CommonModel.kt",
                packageName = "com.example.common",
                filePath = "core/src/commonMain/kotlin/com/example/common/CommonModel.kt",
                classes =
                    listOf(
                        mockClass(
                            name = "CommonModel",
                            packageName = "com.example.common",
                            imports = listOf("kotlin.collections.List", "kotlinx.coroutines.flow.Flow"),
                            filePath = "core/src/commonMain/kotlin/com/example/common/CommonModel.kt",
                        ),
                    ),
                sourceSets = listOf(sourceSetId(":core", "commonMain")),
            )

        val module =
            mockModule(
                path = ":core",
                files = listOf(commonFile),
                sourceSets = listOf(SourceSet("commonMain", "KMP", true, emptyList())),
            )
        graphWith(listOf(module))

        assertDoesNotThrow {
            architecture {
                sourceSet("commonMain") {
                    mustBePlatformIndependent()
                }
            }
        }
    }

    @Test
    fun `mustBePlatformIndependent catches android import in commonMain`() {
        val commonFile =
            FileDeclaration(
                name = "AndroidInCommon.kt",
                packageName = "com.example.common",
                filePath = "core/src/commonMain/kotlin/com/example/common/AndroidInCommon.kt",
                classes =
                    listOf(
                        mockClass(
                            name = "AndroidInCommon",
                            packageName = "com.example.common",
                            imports = listOf("android.os.Bundle"),
                            filePath = "core/src/commonMain/kotlin/com/example/common/AndroidInCommon.kt",
                        ),
                    ),
                sourceSets = listOf(sourceSetId(":core", "commonMain")),
            )

        val module =
            mockModule(
                path = ":core",
                files = listOf(commonFile),
            )
        graphWith(listOf(module))

        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    sourceSet("commonMain") {
                        mustBePlatformIndependent()
                    }
                }
            }

        assertTrue(
            error.message!!.contains(
                "must be platform-independent, but class com.example.common.AndroidInCommon references platform-specific symbol: android.os.Bundle",
            ),
        )
    }

    @Test
    fun `mustBePlatformIndependent respects excluding filter`() {
        val commonFile =
            FileDeclaration(
                name = "JavaUtilInCommon.kt",
                packageName = "com.example.common",
                filePath = "core/src/commonMain/kotlin/com/example/common/JavaUtilInCommon.kt",
                classes =
                    listOf(
                        mockClass(
                            name = "JavaUtilInCommon",
                            packageName = "com.example.common",
                            imports = listOf("java.util.UUID"),
                            filePath = "core/src/commonMain/kotlin/com/example/common/JavaUtilInCommon.kt",
                        ),
                    ),
                sourceSets = listOf(sourceSetId(":core", "commonMain")),
            )

        val module =
            mockModule(
                path = ":core",
                files = listOf(commonFile),
            )
        graphWith(listOf(module))

        assertDoesNotThrow {
            architecture {
                sourceSet("commonMain") {
                    mustBePlatformIndependent(excluding = listOf("java.util.**"))
                }
            }
        }
    }

    @Test
    fun `mustBePlatformIndependent catches additional banned packages`() {
        val commonFile =
            FileDeclaration(
                name = "JsonInCommon.kt",
                packageName = "com.example.common",
                filePath = "core/src/commonMain/kotlin/com/example/common/JsonInCommon.kt",
                classes =
                    listOf(
                        mockClass(
                            name = "JsonInCommon",
                            packageName = "com.example.common",
                            imports = listOf("org.json.JSONObject"),
                            filePath = "core/src/commonMain/kotlin/com/example/common/JsonInCommon.kt",
                        ),
                    ),
                sourceSets = listOf(sourceSetId(":core", "commonMain")),
            )

        val module =
            mockModule(
                path = ":core",
                files = listOf(commonFile),
            )
        graphWith(listOf(module))

        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    sourceSet("commonMain") {
                        mustBePlatformIndependent(additionalBanned = listOf("org.json.**"))
                    }
                }
            }

        assertTrue(error.message!!.contains("org.json.JSONObject"))
    }

    @Test
    fun `mustNotDependOn catches prohibited packages`() {
        val commonFile =
            FileDeclaration(
                name = "ForbiddenRepo.kt",
                packageName = "com.example.common",
                filePath = "core/src/commonMain/kotlin/com/example/common/ForbiddenRepo.kt",
                classes =
                    listOf(
                        mockClass(
                            name = "ForbiddenRepo",
                            packageName = "com.example.common",
                            imports = listOf("com.example.legacy.LegacyDatabase"),
                            filePath = "core/src/commonMain/kotlin/com/example/common/ForbiddenRepo.kt",
                        ),
                    ),
                sourceSets = listOf(sourceSetId(":core", "commonMain")),
            )

        val module =
            mockModule(
                path = ":core",
                files = listOf(commonFile),
            )
        graphWith(listOf(module))

        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    sourceSet("commonMain") {
                        mustNotDependOn("com.example.legacy.**")
                    }
                }
            }

        assertTrue(error.message!!.contains("must not depend on packages [com.example.legacy.**]"))
    }

    @Test
    fun `mayDependOn allows permitted platform packages and bans others`() {
        val androidFile =
            FileDeclaration(
                name = "AndroidBridge.kt",
                packageName = "com.example.bridge",
                filePath = "core/src/androidMain/kotlin/com/example/bridge/AndroidBridge.kt",
                classes =
                    listOf(
                        mockClass(
                            name = "AndroidBridge",
                            packageName = "com.example.bridge",
                            imports = listOf("android.content.Context", "platform.UIKit.UIView"),
                            filePath = "core/src/androidMain/kotlin/com/example/bridge/AndroidBridge.kt",
                        ),
                    ),
                sourceSets = listOf(sourceSetId(":core", "androidMain")),
            )

        val module =
            mockModule(
                path = ":core",
                files = listOf(androidFile),
            )
        graphWith(listOf(module))

        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    sourceSet("androidMain") {
                        mayDependOn("android.**", "androidx.**")
                    }
                }
            }

        assertTrue(error.message!!.contains("may only depend on packages [android.**, androidx.**]"))
        assertTrue(error.message!!.contains("platform.UIKit.UIView"))
    }

    @Test
    fun `mustNotDependOnSourceSets catches forbidden hierarchy edge`() {
        val module =
            mockModule(
                path = ":core",
                files =
                    listOf(
                        FileDeclaration(
                            name = "CommonFile.kt",
                            packageName = "com.example.common",
                            filePath = "core/src/commonMain/kotlin/com/example/common/CommonFile.kt",
                            classes = listOf(mockClass("CommonClass", "com.example.common")),
                            sourceSets = listOf(sourceSetId(":core", "commonMain")),
                        ),
                    ),
                sourceSets =
                    listOf(
                        SourceSet(
                            name = "commonMain",
                            kind = "KMP",
                            production = true,
                            srcDirs = emptyList(),
                            dependsOnSourceSets = listOf("jvmMain", "androidMain"),
                        ),
                    ),
            )
        graphWith(listOf(module))

        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    sourceSet("commonMain") {
                        mustNotDependOnSourceSets("jvmMain", "androidMain", "iosMain")
                    }
                }
            }

        assertTrue(
            error.message!!.contains(
                "must not depend on source sets [jvmMain, androidMain, iosMain], but depends on: jvmMain",
            ),
        )
    }

    @Test
    fun `sourceSet vararg applies policy across multiple source sets`() {
        val file1 =
            FileDeclaration(
                name = "A.kt",
                packageName = "com.example",
                classes = listOf(mockClass("A", "com.example", imports = listOf("android.os.Bundle"))),
                sourceSets = listOf(sourceSetId(":core", "commonMain")),
            )
        val file2 =
            FileDeclaration(
                name = "B.kt",
                packageName = "com.example",
                classes = listOf(mockClass("B", "com.example", imports = listOf("java.awt.Color"))),
                sourceSets = listOf(sourceSetId(":core", "jvmMain")),
            )
        val module =
            mockModule(
                path = ":core",
                files = listOf(file1, file2),
            )
        graphWith(listOf(module))

        val error =
            assertThrows(AssertionError::class.java) {
                architecture {
                    sourceSet("commonMain", "jvmMain") {
                        mustNotDependOn("android.**", "java.awt.**")
                    }
                }
            }

        assertTrue(error.message!!.contains("android.os.Bundle"))
        assertTrue(error.message!!.contains("java.awt.Color"))
    }

    @Test
    fun `sourceSet localized error messages work across locales`() {
        val originalLocale = Konture.locale
        try {
            Konture.locale = Locale.FRENCH
            val file =
                FileDeclaration(
                    name = "Bad.kt",
                    packageName = "com.example",
                    classes = listOf(mockClass("Bad", "com.example", imports = listOf("android.os.Bundle"))),
                    sourceSets = listOf(sourceSetId(":core", "commonMain")),
                )
            graphWith(listOf(mockModule(path = ":core", files = listOf(file))))

            val error =
                assertThrows(AssertionError::class.java) {
                    architecture {
                        sourceSet("commonMain") {
                            mustBePlatformIndependent()
                        }
                    }
                }
            assertTrue(error.message!!.contains("doit être indépendant de la plateforme"))
        } finally {
            Konture.locale = originalLocale
        }
    }
}
