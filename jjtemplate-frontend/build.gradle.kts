plugins {
    id("java")
    id("antlr")
}

val pkg = "io.github.sibmaks.jjtemplate.frontend.antlr"
val antlrDirectory = layout.buildDirectory
    .dir("generated-sources/antlr/main")
    .get()
    .asFile

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
}

tasks.withType<JavaCompile> {
    dependsOn(tasks.generateGrammarSource)
}

tasks.generateGrammarSource {
    arguments = listOf(
        "-visitor",
        "-package", pkg
    )

    outputDirectory = antlrDirectory.resolve(pkg.replace('.', '/'))
}


tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
    dependsOn(tasks.generateGrammarSource)
}

sourceSets {
    main {
        java {
            srcDir(antlrDirectory)
        }
    }
}