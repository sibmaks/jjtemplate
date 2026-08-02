import java.text.SimpleDateFormat
import java.util.*
import me.champeau.gradle.japicmp.JapicmpTask
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

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

val apiBaselineVersion = providers.gradleProperty("api_baseline_version")

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
    apply(plugin = "me.champeau.gradle.japicmp")

    val targetJavaVersion = (project.property("jdk_version") as String).toInt()
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)

    configurations {
        create("deployerJars")
    }

    val apiBaseline = rootProject.configurations.create("${project.name}ApiBaseline") {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = true
        resolutionStrategy.useGlobalDependencySubstitutionRules.set(false)
    }

    rootProject.dependencies.add(
        apiBaseline.name,
        "${project.group}:${project.name}:${apiBaselineVersion.get()}"
    )

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

    val apiPackages = when (project.name) {
        "jjtemplate-lexer" -> listOf(
            "io.github.sibmaks.jjtemplate.lexer",
            "io.github.sibmaks.jjtemplate.lexer.api"
        )
        "jjtemplate-parser" -> listOf(
            "io.github.sibmaks.jjtemplate.parser",
            "io.github.sibmaks.jjtemplate.parser.api",
            "io.github.sibmaks.jjtemplate.parser.exception"
        )
        "jjtemplate-compiler" -> listOf(
            "io.github.sibmaks.jjtemplate.compiler.api",
            "io.github.sibmaks.jjtemplate.compiler.exception",
            "io.github.sibmaks.jjtemplate.compiler.runtime",
            "io.github.sibmaks.jjtemplate.compiler.runtime.exception",
            "io.github.sibmaks.jjtemplate.compiler.runtime.fun"
        )
        else -> emptyList()
    }

    val baselineArchive = apiBaseline.incoming.artifactView {
        componentFilter { component ->
            component is ModuleComponentIdentifier
                    && component.group == project.group.toString()
                    && component.module == project.name
        }
    }.files

    val apiCompatibilityCheck = tasks.register<JapicmpTask>("apiCompatibilityCheck") {
        group = "verification"
        description = "Checks the supported public API against version ${apiBaselineVersion.get()}."
        dependsOn(tasks.named("jar"))
        oldClasspath.from(apiBaseline)
        oldArchives.from(baselineArchive)
        newClasspath.from(configurations.runtimeClasspath)
        newArchives.from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
        packageIncludes = apiPackages
        onlyModified = true
        failOnSourceIncompatibility = true
        txtOutputFile = layout.buildDirectory.file("reports/japicmp/report.txt")
        htmlOutputFile = layout.buildDirectory.file("reports/japicmp/report.html")
    }

    tasks.named("check") {
        dependsOn(apiCompatibilityCheck)
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
