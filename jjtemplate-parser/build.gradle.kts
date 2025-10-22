dependencies {
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    implementation(project(":jjtemplate-lexer"))

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}