plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gradle.publish)
}

description = "Gradle configuration-time capture agent that feeds Konture your project's real build graph, enabling fast, build-tool-aware Kotlin architecture tests across Android, KMP, and JVM projects."

group = "io.github.baole.konture"
version = "0.7.1"

gradlePlugin {
    website.set("https://baole.github.io/konture")
    vcsUrl.set("https://github.com/baole/konture.git")

    plugins {
        create("konture") {
            id = "io.github.baole.konture"
            implementationClass = "io.github.baole.konture.plugin.KonturePlugin"
            displayName = "Kotlin Architecture Testing Tool Plugin"
            description = "Gradle configuration-time capture agent that feeds Konture your project's real build graph, enabling fast, build-tool-aware Kotlin architecture tests across Android, KMP, and JVM projects."
            tags.set(listOf("kotlin", "architecture", "testing", "archunit", "konture"))
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin.api)
    implementation(project(":core"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlin.gradle.plugin)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
