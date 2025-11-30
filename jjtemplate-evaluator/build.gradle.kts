dependencies {
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    implementation(libs.slf4j.api)
    implementation(project(":jjtemplate-lexer"))
    implementation(project(":jjtemplate-parser"))

    testImplementation(libs.slf4j.simple)
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.bundles.mockito)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}