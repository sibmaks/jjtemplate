dependencies {
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    implementation(project(":jjtemplate-lexer"))
    implementation(project(":jjtemplate-parser"))
    implementation(project(":jjtemplate-evaluator"))

    testImplementation(libs.bundles.jackson)

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
