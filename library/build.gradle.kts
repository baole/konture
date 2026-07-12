plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.github.baole"

description = "Konture primary public API library"

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = "konture"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
