package io.github.sibmaks.jjtemplate.compiler.benchmark;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileOptions;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * Measures steady-state compilation latency for representative template shapes.
 */
@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class CompileBenchmark {
    /** Scenario selected explicitly to keep suite growth controlled. */
    @Param({
            "STATIC_LITERAL",
            "SCALAR_SUBSTITUTION",
            "NESTED_MAP",
            "FUNCTION_PIPELINE",
            "CONDITIONALS",
            "COLLECTION",
            "REALISTIC_DOCUMENT"
    })
    public BenchmarkScenario scenario;

    /** Whether optimizer passes are enabled. */
    @Param({"false", "true"})
    public boolean optimize;

    private TemplateCompiler compiler;
    private TemplateScript script;

    /** Prepares immutable inputs outside measured operations. */
    @Setup(Level.Trial)
    public void setup() {
        compiler = TemplateCompiler.getInstance(
                TemplateCompileOptions.builder().optimize(optimize).build()
        );
        script = BenchmarkFixtures.create(scenario).getScript();
    }

    /**
     * Compiles the prepared script.
     *
     * @return compiled template, consumed by JMH
     */
    @Benchmark
    public CompiledTemplate compile() {
        return compiler.compile(script);
    }
}
