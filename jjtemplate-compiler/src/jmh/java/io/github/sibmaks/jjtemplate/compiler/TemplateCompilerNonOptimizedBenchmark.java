package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileOptions;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.compiler.data.DataSamples;
import io.github.sibmaks.jjtemplate.compiler.data.Scenario;
import io.github.sibmaks.jjtemplate.compiler.data.Templates;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class TemplateCompilerNonOptimizedBenchmark {

    @Param
    public Scenario scenario;

    private TemplateCompiler engine;
    private CompiledTemplate compiledTemplate;
    private Map<String, Object> data;
    private TemplateScript template;

    @Setup(Level.Trial)
    public void setup() {
        engine = TemplateCompiler.getInstance(
                TemplateCompileOptions.builder()
                        .optimize(false)
                        .build()
        );

        data = DataSamples.byName(scenario);
        template = Templates.byName(scenario);

        compiledTemplate = engine.compile(template);
    }

    @Benchmark
    public CompiledTemplate benchmarkCompile() {
        return engine.compile(template);
    }

    @Benchmark
    public Object benchmarkRender() {
        return compiledTemplate.render(data);
    }
}
