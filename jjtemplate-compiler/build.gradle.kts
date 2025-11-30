plugins {
    id("me.champeau.jmh") version "0.7.3"
}

dependencies {
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    implementation(libs.slf4j.api)
    implementation(project(":jjtemplate-lexer"))
    implementation(project(":jjtemplate-parser"))
    implementation(project(":jjtemplate-evaluator"))

    testImplementation(libs.bundles.jackson)

    testImplementation(libs.slf4j.simple)
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}
