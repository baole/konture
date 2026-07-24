/*
 * Copyright 2026 The Konture Contributors
 * Contributors: Bao Le Duc (@baole)
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    `maven-publish`
    signing
}

if (project.name != "plugin-gradle" && project.name != "konture-test") {
    pluginManager.apply("com.gradleup.nmcp")
}

plugins.withType<JavaPlugin> {
    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    configure<PublishingExtension> {
        if (name != "plugin-gradle" && name != "konture-test") {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                }
            }
            plugins.withId("com.gradleup.nmcp") {
                configure<nmcp.NmcpExtension> {
                    publish("mavenJava") {
                        username =
                            providers.gradleProperty("mavenCentralUsername").orNull
                                ?: System.getenv("MAVEN_CENTRAL_USERNAME")
                        password =
                            providers.gradleProperty("mavenCentralPassword").orNull
                                ?: System.getenv("MAVEN_CENTRAL_PASSWORD")
                        publicationType = "AUTOMATIC"
                    }
                }
            }
        }
    }
}

configure<PublishingExtension> {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set(project.name)
            description.set(project.description ?: "Kotlin Architecture Testing Tool - ${project.name}")
            url.set("https://baole.github.io/konture")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("baole")
                    name.set("Bao Le Duc")
                    email.set("leducbao@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/baole/konture.git")
                developerConnection.set("scm:git:ssh://github.com/baole/konture.git")
                url.set("https://baole.github.io/konture")
            }
        }
    }

    repositories {
        if (project.name != "plugin-gradle" && project.name != "konture-test") {
            maven {
                name = "MavenCentral"
                val releasesRepoUrl =
                    uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username =
                        providers.gradleProperty("mavenCentralUsername").orNull
                            ?: System.getenv("MAVEN_CENTRAL_USERNAME")
                    password =
                        providers.gradleProperty("mavenCentralPassword").orNull
                            ?: System.getenv("MAVEN_CENTRAL_PASSWORD")
                }
            }
        }
    }
}

configure<SigningExtension> {
    val signingKey = providers.gradleProperty("signingKey").orNull ?: System.getenv("GPG_SIGNING_KEY")
    val signingPassword =
        providers.gradleProperty("signingPassword").orNull ?: System.getenv("GPG_SIGNING_PASSWORD")
    val isRelease = providers.gradleProperty("releaseBuild").orNull?.toBoolean() ?: false
    isRequired = isRelease
    if (isRelease) {
        if (!signingKey.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    }
    val publishing = extensions.getByType<PublishingExtension>()
    sign(publishing.publications)
}
