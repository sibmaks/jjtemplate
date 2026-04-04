import org.gradle.api.tasks.SourceSetContainer

val generatedBuiltInFunctionsDir = layout.buildDirectory.dir("generated/sources/builtInFunctionRegistry/main/java")
val builtInFunctionSources = fileTree("src/main/java/io/github/sibmaks/jjtemplate/compiler/runtime/fun/impl") {
    include("**/*.java")
}
val googleJavaFormat by configurations.creating

dependencies {
    googleJavaFormat("com.google.googlejavaformat:google-java-format:1.22.0")
}

val generateBuiltInFunctionRegistry by tasks.registering(GenerateBuiltInFunctionRegistryTask::class) {
    sourceFiles.from(builtInFunctionSources)
    formatterClasspath.from(googleJavaFormat)
    basePackage.set("io.github.sibmaks.jjtemplate.compiler.runtime.fun.impl")
    outputPackage.set("io.github.sibmaks.jjtemplate.compiler.runtime")
    outputDirectory.set(generatedBuiltInFunctionsDir)
}

extensions.configure<SourceSetContainer>("sourceSets") {
    named("main") {
        java.srcDir(generateBuiltInFunctionRegistry)
    }
}

tasks.named("compileJava") {
    dependsOn(generateBuiltInFunctionRegistry)
}

tasks.named("sourcesJar") {
    dependsOn(generateBuiltInFunctionRegistry)
}

tasks.named("javadoc") {
    dependsOn(generateBuiltInFunctionRegistry)
}

tasks.named("checkstyleMain") {
    dependsOn(generateBuiltInFunctionRegistry)
}
