package io.github.sibmaks.jjtemplate.compiler.benchmark;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileOptions;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Measures rendering latency and allocations after compilation has completed.
 */
@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class RenderBenchmark {
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

    /** Whether the prepared template is optimized. */
    @Param({"false", "true"})
    public boolean optimize;

    private CompiledTemplate compiledTemplate;
    private Map<String, Object> context;

    /** Compiles the template once and prepares its runtime context. */
    @Setup(Level.Trial)
    public void setup() {
        var benchmarkCase = BenchmarkFixtures.create(scenario);
        var compiler = TemplateCompiler.getInstance(
                TemplateCompileOptions.builder().optimize(optimize).build()
        );
        compiledTemplate = compiler.compile(benchmarkCase.getScript());
        context = benchmarkCase.getContext();
    }

    /**
     * Renders the prepared template.
     *
     * @return rendered value
     */
    @Benchmark
    public Object render() {
        return compiledTemplate.render(context);
    }
}
