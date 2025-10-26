import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("maven-publish")
    id("java")
    id("jacoco")
    id("signing")
    id("org.jreleaser") version "1.20.0"
}

allprojects {
    val versionFromProperty = "${project.property("version")}"
    val versionFromEnv: String? = System.getenv("VERSION")

    version = versionFromEnv ?: versionFromProperty
    group = "${project.property("group")}"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "maven-publish")

    val targetJavaVersion = (project.property("jdk_version") as String).toInt()
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)

    repositories {
        mavenCentral()
    }

    configurations {
        create("deployerJars")
    }

    tasks.withType<JavaCompile>().configureEach {
        // ensure that the encoding is set to UTF-8, no matter what the system default is
        // this fixes some edge cases with special characters not displaying correctly
        // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
        // If Javadoc is generated, this must be specified in that task too.
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release = targetJavaVersion
        }
    }

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
        }
        withJavadocJar()
        withSourcesJar()
    }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
    }

    tasks.jar {
        from("LICENSE") {
            rename { "${it}_${project.property("project_name")}" }
        }
        manifest {
            attributes(
                mapOf(
                    "Specification-Title" to project.name,
                    "Specification-Vendor" to project.property("author"),
                    "Specification-Version" to project.version,
                    "Specification-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                    "Timestamp" to System.currentTimeMillis(),
                    "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})"
                )
            )
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    packaging = "jar"
                    name = artifactId
                    description = "Part of JJTemplate project"
                    url = "https://github.com/sibmaks/jjtemplate"

                    licenses {
                        license {
                            name = "Apache License, version 2.0"
                            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }

                    scm {
                        connection = "scm:https://github.com/sibmaks/jjtemplate.git"
                        developerConnection = "scm:git:ssh://github.com/sibmaks"
                        url = "https://github.com/sibmaks/jjtemplate"
                    }

                    developers {
                        developer {
                            id = "sibmaks"
                            name = "Maksim Drobyshev"
                            email = "sibmaks@vk.com"
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "Staging"
                url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }
}

dependencies {
    implementation(project(":jjtemplate-parser"))
    implementation(project(":jjtemplate-lexer"))
    implementation(project(":jjtemplate-evaluator"))
    implementation(project(":jjtemplate-compiler"))
}

tasks.named("jreleaserFullRelease") {
    dependsOn("publishAllPublicationsToStagingRepository")
}

publishing {
    publications {
        create<MavenPublication>("aggregator") {
            pom {
                packaging = "pom"
                url = "https://github.com/sibmaks/jjtemplate"
                name = artifactId
                description = "Template engine for Java projects"

                licenses {
                    license {
                        name = "Apache License, version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                scm {
                    connection = "scm:https://github.com/sibmaks/jjtemplate.git"
                    developerConnection = "scm:git:ssh://github.com/sibmaks"
                    url = "https://github.com/sibmaks/jjtemplate"
                }

                developers {
                    developer {
                        id = "sibmaks"
                        name = "Maksim Drobyshev"
                        email = "sibmaks@vk.com"
                    }
                }

                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    subprojects.forEach { sub ->
                        if (sub.plugins.hasPlugin("java") || sub.plugins.hasPlugin("kotlin")) {
                            dependenciesNode.appendNode("dependency").apply {
                                appendNode("groupId", sub.group.toString())
                                appendNode("artifactId", sub.name)
                                appendNode("version", sub.version.toString())
                                appendNode("scope", "compile")
                            }
                        }
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "Staging"
            url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    configFile = rootProject.layout.projectDirectory.file("jreleaser.yml")
}