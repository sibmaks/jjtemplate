import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("me.champeau.jmh") version "0.7.3"
}

jmh {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

    resultFormat.set("json")
    resultsFile.set(layout.buildDirectory.file("reports/jmh/results-$version-${formatter.format(Date())}.json"))
}

dependencies {
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    implementation(libs.slf4j.api)
    implementation(project(":jjtemplate-frontend"))

    testImplementation(libs.bundles.jackson)

    testImplementation(libs.slf4j.simple)
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.bundles.mockito)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}
