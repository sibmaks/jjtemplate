import org.gradle.api.publish.maven.MavenPom

fun configureCommonPom(pom: MavenPom) {
    pom.url.set("https://github.com/sibmaks/jjtemplate")
    pom.licenses {
        license {
            name.set("Apache License, version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    pom.scm {
        connection.set("scm:https://github.com/sibmaks/jjtemplate.git")
        developerConnection.set("scm:git:ssh://github.com/sibmaks")
        url.set("https://github.com/sibmaks/jjtemplate")
    }
    pom.developers {
        developer {
            id.set("sibmaks")
            name.set("Maksim Drobyshev")
            email.set("sibmaks@vk.com")
        }
    }
}
