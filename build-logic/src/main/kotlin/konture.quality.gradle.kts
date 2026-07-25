/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

import com.diffplug.gradle.spotless.SpotlessExtension
import dev.detekt.gradle.extensions.DetektExtension
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("dev.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.diffplug.spotless")
    jacoco
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.12"
}

tasks.withType<JacocoReport> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<Test> {
    finalizedBy(tasks.withType<JacocoReport>())
}

tasks.withType<JacocoCoverageVerification> {
    dependsOn(tasks.withType<JacocoReport>())
    val reportFile = layout.buildDirectory.file("reports/jacoco/test/html/index.html")
    doFirst {
        logger.lifecycle("Coverage Report: file://${reportFile.get().asFile.absolutePath}")
    }
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.matching { it.name == "check" }.configureEach {
    dependsOn(tasks.withType<JacocoCoverageVerification>())
}

configure<DetektExtension> {
    config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig.set(true)
    parallel.set(true)
    ignoreFailures.set(false)
}

configure<KtlintExtension> {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(ReporterType.PLAIN)
    }
}

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt", "**/*.kts")
        custom("validate contributor header") { source ->
            require(
                Regex(
                    """\A/\*\R \* Copyright \d{4}(?:-\d{4})? .+\R(?: \* Contributors: .+\R)? \* SPDX-License-Identifier: Apache-2\.0\R \*/\R\R""",
                ).containsMatchIn(source),
            ) { "Kotlin files must start with a copyright and SPDX header." }
            source
        }
    }
}

tasks.matching { it.name == "spotlessApply" }.configureEach {
    val updateTask = rootProject.tasks.findByName("updateKotlinContributors")
    if (updateTask != null) {
        dependsOn(updateTask)
    }
}

plugins.withId("org.jetbrains.dokka") {
    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            perPackageOption {
                matchingRegex.set("io\\.github\\.baole\\.konture\\.impl(\\..*)?")
                suppress.set(true)
            }
        }
    }
}
