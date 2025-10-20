subprojects {
    apply(plugin = "java")

    group = "io.github.sibmaks.jjtemplate"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}