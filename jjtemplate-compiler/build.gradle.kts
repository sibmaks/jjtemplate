import java.text.SimpleDateFormat
import java.util.Date
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar

plugins {
    id("me.champeau.jmh") version "0.7.3"
    id("jjtemplate-built-in-function-registry")
}

val benchmarkFixtures = extensions.getByType<SourceSetContainer>().create("benchmarkFixtures")
benchmarkFixtures.compileClasspath += sourceSets.main.get().output
benchmarkFixtures.runtimeClasspath += benchmarkFixtures.output + benchmarkFixtures.compileClasspath

sourceSets.test {
    compileClasspath += benchmarkFixtures.output
    runtimeClasspath += benchmarkFixtures.output
}

sourceSets.named("jmh") {
    compileClasspath += benchmarkFixtures.output
    runtimeClasspath += benchmarkFixtures.output
}

configurations.named(benchmarkFixtures.implementationConfigurationName) {
    extendsFrom(configurations.implementation.get())
}

configurations.named(benchmarkFixtures.compileOnlyConfigurationName) {
    extendsFrom(configurations.compileOnly.get())
}

jmh {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss")
    val date = formatter.format(Date())

    resultFormat.set("json")
    resultsFile.set(layout.buildDirectory.file("reports/jmh/results-$version-$date.json"))
    humanOutputFile.set(layout.buildDirectory.file("reports/jmh/human-$version-$date.txt"))
}

tasks.named<Jar>("jmhJar") {
    from(benchmarkFixtures.output)
}

fun registerJmhRun(
    taskName: String,
    benchmarkPattern: String,
    reportName: String,
    extraArguments: List<String>
) {
    tasks.register<JavaExec>(taskName) {
        group = "benchmark"
        description = "Runs the $reportName JMH benchmark profile."
        dependsOn(tasks.named("jmhJar"))
        classpath(tasks.named<Jar>("jmhJar").flatMap { it.archiveFile })
        mainClass.set("org.openjdk.jmh.Main")
        doFirst {
            layout.buildDirectory.dir("reports/jmh").get().asFile.mkdirs()
        }
        args(
            benchmarkPattern,
            "-rf", "json",
            "-rff", layout.buildDirectory.file("reports/jmh/results-$reportName.json").get().asFile.absolutePath,
            "-o", layout.buildDirectory.file("reports/jmh/human-$reportName.txt").get().asFile.absolutePath
        )
        args(extraArguments)
    }
}

registerJmhRun(
    "jmhQuick",
    ".*benchmark\\.(CompileBenchmark|RenderBenchmark)\\..*",
    "quick",
    listOf(
        "-p", "scenario=SCALAR_SUBSTITUTION,REALISTIC_DOCUMENT",
        "-p", "optimize=true",
        "-f", "1",
        "-wi", "1",
        "-i", "2",
        "-w", "300ms",
        "-r", "300ms"
    )
)

registerJmhRun(
    "jmhFull",
    ".*Benchmark.*",
    "full",
    listOf("-prof", "gc")
)

tasks.register<Exec>("jmhReport") {
    group = "benchmark"
    description = "Renders a JMH JSON result as a Markdown report."
    val results = providers.gradleProperty("jmhResults")
        .orElse(layout.buildDirectory.file("reports/jmh/results-full.json").map { it.asFile.absolutePath })
    val output = providers.gradleProperty("jmhReport")
        .orElse(layout.buildDirectory.file("reports/jmh/report.md").map { it.asFile.absolutePath })
    commandLine(
        "python3",
        file("src/jmh/scripts/render_report.py").absolutePath,
        results.get(),
        "--output",
        output.get()
    )
}

tasks.register<Exec>("jmhCompare") {
    group = "benchmark"
    description = "Compares current JMH JSON results with a baseline and writes Markdown."
    doFirst {
        val baseline = providers.gradleProperty("jmhBaseline").orNull
            ?: throw GradleException("Pass -PjmhBaseline=/path/to/baseline.json")
        val results = providers.gradleProperty("jmhResults").orNull
            ?: throw GradleException("Pass -PjmhResults=/path/to/current.json")
        val output = providers.gradleProperty("jmhReport")
            .orElse(layout.buildDirectory.file("reports/jmh/comparison.md").map { it.asFile.absolutePath })
            .get()
        commandLine(
            "python3",
            file("src/jmh/scripts/render_report.py").absolutePath,
            results,
            "--baseline",
            baseline,
            "--output",
            output
        )
    }
}

dependencies {
    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    implementation(libs.slf4j.api)
    implementation(project(":jjtemplate-parser"))

    testImplementation(libs.bundles.jackson)

    testImplementation(libs.slf4j.simple)
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.bundles.mockito)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    jmhRuntimeOnly(libs.slf4j.simple)
}
