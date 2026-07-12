plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.baole"

description = "Shared data models and JSON structures for Konture"

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = "konture-core"
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
