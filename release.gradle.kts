import org.gradle.api.publish.PublishingExtension
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.JReleaserPlugin

initscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.jreleaser:jreleaser-gradle-plugin:1.20.0")
    }
}

allprojects {
    if (this != rootProject) {
        return@allprojects
    }
    apply<JReleaserPlugin>()

    afterEvaluate {
        extensions.configure(org.jreleaser.gradle.plugin.JReleaserExtension::class.java) {
            configFile.set(rootProject.layout.projectDirectory.file("jreleaser.yml"))
        }
    }
}

allprojects {
    afterProject {
        if (project == rootProject) {
            return@afterProject
        }
        if (!plugins.hasPlugin("maven-publish")) {
            return@afterProject
        }
        extensions.configure<PublishingExtension>("publishing") {
            repositories {
                if (none { it.name == "Staging" }) {
                    maven {
                        name = "Staging"
                        url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
                    }
                }
            }
        }
    }
}