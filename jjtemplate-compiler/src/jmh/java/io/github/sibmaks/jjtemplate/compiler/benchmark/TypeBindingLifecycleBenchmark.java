package io.github.sibmaks.jjtemplate.compiler.benchmark;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
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
 * Measures when explicit type binding repays its additional compilation cost.
 */
@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class TypeBindingLifecycleBenchmark {
    /** Type resolution path under test. */
    @Param({"DTO_PROPERTY", "DTO_METHOD", "POLYMORPHIC_PROPERTY", "MAP_FALLBACK"})
    public TypeBindingScenario scenario;

    /** How the compiler obtains the root variable type. */
    @Param({"DYNAMIC", "EXPLICIT_CONTEXT"})
    public TypeBindingMode binding;

    /** Number of renders performed after each compilation. */
    @Param({"1", "10", "100", "1000"})
    public int renders;

    private TypeBindingCase benchmarkCase;
    private TemplateCompiler compiler;

    /** Prepares the compiler and DTO or Map fixture. */
    @Setup(Level.Trial)
    public void setup() {
        benchmarkCase = TypeBindingFixtures.create(scenario);
        compiler = TemplateCompiler.getInstance();
    }

    /**
     * Compiles once and renders the configured number of times.
     *
     * @param blackhole consumes every render result
     */
    @Benchmark
    public void compileAndRender(Blackhole blackhole) {
        var compiled = compileTemplate();
        for (int i = 0; i < renders; i++) {
            blackhole.consume(compiled.render(benchmarkCase.getContext()));
        }
    }

    private CompiledTemplate compileTemplate() {
        return benchmarkCase.compile(compiler, binding);
    }
}
