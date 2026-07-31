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

import java.util.concurrent.TimeUnit;

/**
 * Measures compile and render scaling as range input cardinality grows.
 */
@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class ScalingBenchmark {
    /** Range input cardinality. */
    @Param({"1", "10", "100", "1000"})
    public int size;

    /** Whether range data is external or embedded in the compiled template. */
    @Param({"EXTERNAL", "INLINE"})
    public CollectionDataLocation dataLocation;

    /** Whether optimizer passes are enabled. */
    @Param({"false", "true"})
    public boolean optimize;

    private BenchmarkCase benchmarkCase;
    private TemplateCompiler compiler;
    private CompiledTemplate compiledTemplate;

    /** Builds the requested collection size and compiles it once for rendering. */
    @Setup(Level.Trial)
    public void setup() {
        benchmarkCase = BenchmarkFixtures.collection(size, dataLocation);
        compiler = TemplateCompiler.getInstance(
                TemplateCompileOptions.builder().optimize(optimize).build()
        );
        compiledTemplate = compiler.compile(benchmarkCase.getScript());
    }

    /**
     * Compiles a template whose runtime data size is recorded as a parameter.
     *
     * @return compiled template
     */
    @Benchmark
    public CompiledTemplate compile() {
        return compiler.compile(benchmarkCase.getScript());
    }

    /**
     * Renders a range over the requested number of values.
     *
     * @return rendered collection
     */
    @Benchmark
    public Object render() {
        return compiledTemplate.render(benchmarkCase.getContext());
    }
}
