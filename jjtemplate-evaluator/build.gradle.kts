dependencies {
    implementation(project(":jjtemplate-lexer"))
    implementation(project(":jjtemplate-parser"))

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.mockito)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}