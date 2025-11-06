import org.jreleaser.gradle.plugin.JReleaserPlugin
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.gradle.api.publish.PublishingExtension

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
    apply<JReleaserPlugin>()

    afterEvaluate {
        extensions.configure(org.jreleaser.gradle.plugin.JReleaserExtension::class.java) {
            configFile.set(rootProject.layout.projectDirectory.file("jreleaser.yml"))
        }
    }

    afterProject {
        if (project == rootProject) {
            return@afterProject
        }
        if(!plugins.hasPlugin("maven-publish")) {
            return@afterProject
        }
        extensions.configure<PublishingExtension>("publishing") {
            repositories {
                maven {
                    name = "Staging"
                    url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
                }
            }
        }
    }
}