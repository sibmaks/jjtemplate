import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("maven-publish")
    id("java")
    id("jacoco")
    id("signing")
    id("org.jreleaser") version "1.20.0"
    id("org.sonarqube") version "7.0.1.6134"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    val versionFromProperty = "${project.property("version")}"
    val versionFromEnv: String? = System.getenv("VERSION")

    version = versionFromEnv ?: versionFromProperty
    group = "${project.property("group")}"

    publishing {
        repositories {
            maven {
                name = "Staging"
                url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "maven-publish")
    apply(plugin = "org.sonarqube")

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
        reports {
            xml.required.set(true)
        }
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
                    configureCommonPom(this)
                    packaging = "jar"
                    name = artifactId
                    description = "Part of JJTemplate project"
                }
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

publishing {
    publications {
        create<MavenPublication>("aggregator") {
            pom {
                configureCommonPom(this)
                packaging = "pom"
                name = artifactId
                description = "Template engine for Java projects"

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
}

jreleaser {
    configFile = rootProject.layout.projectDirectory.file("jreleaser.yml")
}