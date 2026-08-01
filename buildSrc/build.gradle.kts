buildscript {
    configurations.classpath {
        resolutionStrategy.activateDependencyLocking()
    }
}

plugins {
    `kotlin-dsl`
}

dependencyLocking {
    lockAllConfigurations()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
}
