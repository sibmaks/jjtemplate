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
 * Measures the first compiler construction and compilation in a fresh JVM fork.
 */
@Fork(10)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class ColdCompileBenchmark {
    /** Representative small and realistic templates. */
    @Param({"SCALAR_SUBSTITUTION", "REALISTIC_DOCUMENT"})
    public BenchmarkScenario scenario;

    /** Whether optimizer passes are enabled. */
    @Param({"false", "true"})
    public boolean optimize;

    private TemplateScript script;

    /** Prepares the script before the one measured invocation in each fork. */
    @Setup(Level.Trial)
    public void setup() {
        script = BenchmarkFixtures.create(scenario).getScript();
    }

    /**
     * Creates a compiler and performs its first compilation.
     *
     * @return compiled template
     */
    @Benchmark
    public CompiledTemplate coldCompile() {
        var compiler = TemplateCompiler.getInstance(
                TemplateCompileOptions.builder().optimize(optimize).build()
        );
        return compiler.compile(script);
    }
}
