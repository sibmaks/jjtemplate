import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import java.io.File
import java.nio.charset.StandardCharsets
import javax.lang.model.element.Modifier
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/**
 * Generates a registry of built-in template functions from source files.
 */
abstract class GenerateBuiltInFunctionRegistryTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @get:Classpath
    abstract val formatterClasspath: ConfigurableFileCollection

    @get:Input
    abstract val basePackage: Property<String>

    @get:Input
    abstract val outputPackage: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generateRegistry() {
        val builtInFunctions = sourceFiles.files
            .sortedBy { it.invariantSeparatorsPath }
            .mapNotNull { file -> buildFunctionMetadata(file, basePackage.get()) }

        val templateFunctionWildcardType = ParameterizedTypeName.get(
            ClassName.get("io.github.sibmaks.jjtemplate.compiler.runtime.fun", "TemplateFunction"),
            WildcardTypeName.subtypeOf(Any::class.java)
        )
        val javaList = ClassName.get("java.util", "List")
        val templateFunctionType = ParameterizedTypeName.get(
            javaList,
            templateFunctionWildcardType
        )
        val functionsCode = CodeBlock.builder()
            .add("return \$T.of(\n", javaList)
            .apply {
                builtInFunctions.forEachIndexed { index, function ->
                    if (index > 0) {
                        add(",\n")
                    }
                    add("new \$T()", ClassName.bestGuess(function.qualifiedName))
                }
            }
            .add("\n);")
            .build()

        val getFunctionsMethod = MethodSpec.methodBuilder("getFunctions")
            .addModifiers(Modifier.STATIC)
            .returns(templateFunctionType)
            .addJavadoc("Returns instantiated built-in template functions.\n\n@return built-in function instances\n")
            .addCode(functionsCode)
            .build()

        val type = TypeSpec.classBuilder("BuiltInFunctionRegistry")
            .addModifiers(Modifier.FINAL)
            .addJavadoc(
                "Generated registry of built-in template functions.\n" +
                        "Regenerated during the build from source files under runtime.fun.impl.\n"
            )
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addJavadoc("Utility class.\n")
                    .build()
            )
            .addMethod(getFunctionsMethod)
            .build()

        val outputDir = outputDirectory.get().asFile
        outputDir.mkdirs()
        val outputFile = File(
            outputDir,
            outputPackage.get().replace('.', '/') + "/BuiltInFunctionRegistry.java"
        )
        JavaFile.builder(outputPackage.get(), type)
            .indent("    ")
            .skipJavaLangImports(true)
            .build()
            .writeTo(outputDir)
        formatSourceFile(outputFile)
    }

    private fun buildFunctionMetadata(file: File, basePackage: String): FunctionMetadata? {
        val simpleName = file.nameWithoutExtension
        val declarationPattern = Regex(
            "^\\s*(?:public\\s+)?(?:final\\s+)?class\\s+${Regex.escape(simpleName)}\\b",
            setOf(RegexOption.MULTILINE)
        )
        if (!declarationPattern.containsMatchIn(file.readText(StandardCharsets.UTF_8))) {
            return null
        }

        val relativePath = file.relativeTo(project.file("src/main/java/${basePackage.replace('.', '/')}"))
            .invariantSeparatorsPath
            .removeSuffix(".java")
            .replace('/', '.')
        return FunctionMetadata(simpleName, "$basePackage.$relativePath")
    }

    private data class FunctionMetadata(
        val simpleName: String,
        val qualifiedName: String
    )

    private fun formatSourceFile(sourceFile: File) {
        execOperations.javaexec {
            classpath(formatterClasspath)
            mainClass.set("com.google.googlejavaformat.java.Main")
            jvmArgs(
                "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
            )
            args("--replace", sourceFile.absolutePath)
        }.rethrowFailure().assertNormalExitValue()
    }
}
