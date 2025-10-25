dependencies {
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    implementation(project(":jjtemplate-lexer"))
    implementation(project(":jjtemplate-parser"))

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.bundles.mockito)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}