/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.baole.konture.impl

import io.github.baole.konture.Dependency
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DependencyExtensionsTest {
    @Test
    fun `identifies standard test configurations`() {
        assertTrue(isTestConfiguration("testImplementation"))
        assertTrue(isTestConfiguration("testRuntimeOnly"))
        assertTrue(isTestConfiguration("testCompileClasspath"))
        assertTrue(isTestConfiguration("test"))
        assertTrue(isTestConfiguration("androidTestImplementation"))
        assertTrue(isTestConfiguration("commonTestApi"))
        assertTrue(isTestConfiguration("jvmTestImplementation"))
        assertTrue(isTestConfiguration("debugTestImplementation"))
    }

    @Test
    fun `identifies benchmark and non-production configurations`() {
        assertTrue(isTestConfiguration("benchmarkImplementation"))
        assertTrue(isTestConfiguration("benchmark"))
        assertTrue(isTestConfiguration("releaseBenchmark"))
        assertTrue(isTestConfiguration("androidTestBenchmark"))
        assertTrue(isTestConfiguration("baselineProfile"))
        assertTrue(isTestConfiguration("releaseBaselineProfileImplementation"))
        assertTrue(isTestConfiguration("testedApks"))
        assertTrue(isTestConfiguration("swiftPMDependenciesForLockFilesMetadataClasspathDependencies"))
        assertTrue(isTestConfiguration("metadataClasspath"))
        assertTrue(isTestConfiguration("metadataCompileClasspath"))
    }

    @Test
    fun `does not misclassify production configurations containing profile or metadata`() {
        assertFalse(isTestConfiguration("userProfileImplementation"))
        assertFalse(isTestConfiguration("companyProfileApi"))
        assertFalse(isTestConfiguration("profileFeatureImplementation"))
        assertFalse(isTestConfiguration("editProfileReleaseImplementation"))
        assertFalse(isTestConfiguration("documentMetadataApi"))
        assertFalse(isTestConfiguration("productMetadataImplementation"))
    }

    @Test
    fun `does not misclassify standard production configurations`() {
        assertFalse(isTestConfiguration("implementation"))
        assertFalse(isTestConfiguration("api"))
        assertFalse(isTestConfiguration("runtimeOnly"))
        assertFalse(isTestConfiguration("compileOnly"))
        assertFalse(isTestConfiguration("releaseImplementation"))
        assertFalse(isTestConfiguration("debugImplementation"))
        assertFalse(isTestConfiguration("jvmMainImplementation"))
        assertFalse(isTestConfiguration("commonMainApi"))
    }

    @Test
    fun `dependency extension delegates correctly`() {
        val prodDep =
            Dependency(configuration = "userProfileImplementation", targetBuildId = "root", targetPath = ":core")
        val testDep = Dependency(configuration = "testImplementation", targetBuildId = "root", targetPath = ":core")
        assertFalse(prodDep.isTestConfiguration())
        assertTrue(testDep.isTestConfiguration())
    }
}
