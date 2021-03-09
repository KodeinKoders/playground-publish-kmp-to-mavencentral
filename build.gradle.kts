plugins {
    kotlin("multiplatform") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
    id("maven-publish")
    id("signing")
}

repositories {
    jcenter()
    mavenCentral()
}

kotlin {
    jvm()
    macosX64()
    mingwX64()
}

group = "org.kodein.playgrounds"
version = "0.1"

val dokkaOutputDir = "$buildDir/dokka"

tasks.dokkaHtml {
    outputDirectory.set(file(dokkaOutputDir))
}

val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    delete(dokkaOutputDir)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

publishing {
    repositories {
        maven {
            name = "Oss"
            setUrl {
                val repositoryId =
                    System.getenv("SONATYPE_REPOSITORY_ID") ?: error("Missing env variable: SONATYPE_REPOSITORY_ID")
                "https://oss.sonatype.org/service/local/staging/deployByRepositoryId/${repositoryId}/"
            }
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
        maven {
            name = "Snapshot"
            setUrl { "https://oss.sonatype.org/content/repositories/snapshots/" }
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }

    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set("playground-publish-kmp-to-mavencentral")
                description.set("A sample project to see how to publish KMP to Maven Central.")
                url.set("https://github.com/KodeinKoders/playground-publish-kmp-to-mavencentral")
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/KodeinKoders/playground-publish-kmp-to-mavencentral/issues")
                }
                scm {
                    connection.set("https://github.com/KodeinKoders/playground-publish-kmp-to-mavencentral.git")
                    url.set("https://github.com/KodeinKoders/playground-publish-kmp-to-mavencentral")
                }
                developers {
                    developer {
                        name.set("Kodein Koders")
                        email.set("dev@kodein.net")
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PRIVATE_PASSWORD")
    )
    sign(publishing.publications)
}
