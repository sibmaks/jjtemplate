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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Measures shared compiled-template rendering under representative concurrency.
 */
@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class ConcurrentRenderBenchmark {
    private CompiledTemplate compiledTemplate;
    private Map<String, Object> context;

    /** Compiles one realistic template shared by all benchmark threads. */
    @Setup(Level.Trial)
    public void setup() {
        var benchmarkCase = BenchmarkFixtures.create(BenchmarkScenario.REALISTIC_DOCUMENT);
        compiledTemplate = TemplateCompiler.getInstance().compile(benchmarkCase.getScript());
        context = benchmarkCase.getContext();
    }

    /**
     * Renders with a single benchmark thread.
     *
     * @return rendered document
     */
    @Benchmark
    @Threads(1)
    public Object renderSingleThread() {
        return compiledTemplate.render(context);
    }

    /**
     * Renders the same compiled template from four threads.
     *
     * @return rendered document
     */
    @Benchmark
    @Threads(4)
    public Object renderFourThreads() {
        return compiledTemplate.render(context);
    }
}
