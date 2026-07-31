package io.github.sibmaks.jjtemplate.compiler.benchmark;

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
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Measures practical compile-once and render-many lifecycle costs.
 */
@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class LifecycleBenchmark {
    /** Scenarios representing small, functional and realistic workloads. */
    @Param({"SCALAR_SUBSTITUTION", "FUNCTION_PIPELINE", "REALISTIC_DOCUMENT"})
    public BenchmarkScenario scenario;

    /** Whether optimizer passes are enabled. */
    @Param({"false", "true"})
    public boolean optimize;

    /** Number of renders performed after each compilation. */
    @Param({"1", "10", "1000"})
    public int renders;

    private BenchmarkCase benchmarkCase;
    private TemplateCompiler compiler;

    /** Prepares the compiler and fixture outside measured operations. */
    @Setup(Level.Trial)
    public void setup() {
        benchmarkCase = BenchmarkFixtures.create(scenario);
        compiler = TemplateCompiler.getInstance(
                TemplateCompileOptions.builder().optimize(optimize).build()
        );
    }

    /**
     * Compiles once and renders the configured number of times.
     *
     * @param blackhole consumes every render result
     */
    @Benchmark
    public void compileAndRender(Blackhole blackhole) {
        var compiled = compiler.compile(benchmarkCase.getScript());
        for (int i = 0; i < renders; i++) {
            blackhole.consume(compiled.render(benchmarkCase.getContext()));
        }
    }
}
