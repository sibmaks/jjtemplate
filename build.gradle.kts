import java.text.SimpleDateFormat
import java.util.*
import me.champeau.gradle.japicmp.JapicmpTask
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

data class MavenArtifactMetadata(
    val displayName: String,
    val description: String,
)

val mavenArtifactMetadata = mapOf(
    "jjtemplate" to MavenArtifactMetadata(
        "JJTemplate",
        "Lightweight Java JSON template engine optimized for fast rendering and JSON-compatible output.",
    ),
    "jjtemplate-lexer" to MavenArtifactMetadata(
        "JJTemplate Lexer",
        "Tokenizes JJTemplate templates and their embedded expressions.",
    ),
    "jjtemplate-parser" to MavenArtifactMetadata(
        "JJTemplate Parser",
        "Parses JJTemplate tokens into abstract syntax trees for template expressions.",
    ),
    "jjtemplate-compiler" to MavenArtifactMetadata(
        "JJTemplate Compiler",
        "Compiles JJTemplate templates into optimized executable trees and provides the rendering runtime.",
    ),
)

buildscript {
    configurations.classpath {
        resolutionStrategy.activateDependencyLocking()
    }
}

plugins {
    id("maven-publish")
    id("java")
    id("jacoco")
    id("me.champeau.gradle.japicmp") version "0.4.6" apply false
    id("org.sonarqube") version "7.0.1.6134"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    dependencyLocking {
        lockAllConfigurations()
    }

    repositories {
        mavenCentral()
    }

    val versionFromProperty = "${project.property("version")}"
    val versionFromEnv: String? = System.getenv("VERSION")

    version = versionFromEnv ?: versionFromProperty
    group = "${project.property("group")}"
}

subprojects {
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")

    val targetJavaVersion = (project.property("jdk_version") as String).toInt()
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)

    configurations {
        create("deployerJars")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = targetJavaVersion
    }

    extensions.configure<CheckstyleExtension> {
        toolVersion = "10.18.2"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        isIgnoreFailures = false
    }

    tasks.withType<Checkstyle>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
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
        jvmArgs("-Xshare:off")
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
                    val metadata = mavenArtifactMetadata.getValue(project.name)
                    configureCommonPom(this)
                    packaging = "jar"
                    name = metadata.displayName
                    description = metadata.description
                }
            }
        }
    }
}

val releaseCheck = tasks.register("releaseCheck") {
    group = "verification"
    description = "Runs all quality gates required before publishing release artifacts."
    dependsOn(subprojects.map { it.tasks.named("check") })
}

allprojects {
    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(releaseCheck)
    }
}

dependencies {
    implementation(project(":jjtemplate-compiler"))
}

publishing {
    publications {
        create<MavenPublication>("aggregator") {
            pom {
                val metadata = mavenArtifactMetadata.getValue(project.name)
                configureCommonPom(this)
                packaging = "pom"
                name = metadata.displayName
                description = metadata.description

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

sonarqube {
    properties {
        property("sonar.organization", project.properties["sonar.organization"] ?: "sibmaks")
        property("sonar.projectKey", project.properties["sonar.projectKey"] ?: "sibmaks_jjtemplate")
        property("sonar.host.url", project.properties["sonar.host.url"] ?: "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.scm.disabled", "true")

        property("sonar.modules", subprojects.joinToString(",") { it.name })

        subprojects.forEach { sub ->
            val subPath = sub.projectDir.toString().substring(projectDir.toString().length + 1)
            property("${sub.name}.sonar.projectBaseDir", subPath)
            property("${sub.name}.sonar.sources", "src/main/java")
            property("${sub.name}.sonar.tests", "src/test/java")
            property("${sub.name}.sonar.java.binaries", "build/classes")
            property("${sub.name}.sonar.junit.reportPaths", "build/test-results/test")
            property(
                "${sub.name}.sonar.coverage.jacoco.xmlReportPaths",
                "build/reports/jacoco/test/jacocoTestReport.xml"
            )
        }
    }
}

tasks.register("printVersion") {
    group = "build"
    description = "Prints the current project version."

    doLast {
        println(project.version)
    }
}
