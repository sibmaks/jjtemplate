plugins {
    id("java")
    id("jacoco")
}

group = "io.github.sibmaks.jjtemplate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":jjtemplate-lexer"))
    implementation(project(":jjtemplate-parser"))
    implementation(project(":jjtemplate-evaluator"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}