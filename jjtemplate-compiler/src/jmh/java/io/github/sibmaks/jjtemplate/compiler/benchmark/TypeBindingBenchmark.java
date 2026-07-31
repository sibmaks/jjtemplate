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

import java.util.concurrent.TimeUnit;

/**
 * Compares dynamic lookup with genuinely bound DTO property and method paths.
 */
@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class TypeBindingBenchmark {
    /** Type resolution path under test. */
    @Param({"DTO_PROPERTY", "DTO_METHOD", "POLYMORPHIC_PROPERTY", "MAP_FALLBACK"})
    public TypeBindingScenario scenario;

    /** Whether compile-time type information is supplied. */
    @Param({"false", "true"})
    public boolean typed;

    private TypeBindingCase benchmarkCase;
    private TemplateCompiler compiler;
    private CompiledTemplate compiledTemplate;

    /** Builds the DTO or Map fixture and compiles the render target. */
    @Setup(Level.Trial)
    public void setup() {
        benchmarkCase = TypeBindingFixtures.create(scenario);
        compiler = TemplateCompiler.getInstance();
        compiledTemplate = compileTemplate();
    }

    /**
     * Compiles with or without type information.
     *
     * @return compiled template
     */
    @Benchmark
    public CompiledTemplate compile() {
        return compileTemplate();
    }

    /**
     * Renders the typed or dynamic compiled template.
     *
     * @return rendered value
     */
    @Benchmark
    public Object render() {
        return compiledTemplate.render(benchmarkCase.getContext());
    }

    private CompiledTemplate compileTemplate() {
        if (typed) {
            return compiler.compile(
                    benchmarkCase.getScript(),
                    benchmarkCase.getCompileContext()
            );
        }
        return compiler.compile(benchmarkCase.getScript());
    }
}
