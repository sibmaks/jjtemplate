plugins {
    id("antlr")
}

val pkg = "io.github.sibmaks.jjtemplate.frontend.antlr"

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
}

tasks.withType<JavaCompile> {
    dependsOn("generateGrammarSource")
}

tasks.generateGrammarSource {
    arguments = arguments + listOf(
        "-visitor",
        "-package", pkg
    )
    outputDirectory = file("src/main/java/${pkg.replace('.', '/')}")
}


tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
    dependsOn("generateGrammarSource")
}