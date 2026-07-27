package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import io.github.sibmaks.jjtemplate.compiler.api.MapTemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileOptions;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.compiler.data.DataSamples;
import io.github.sibmaks.jjtemplate.compiler.data.Scenario;
import io.github.sibmaks.jjtemplate.compiler.data.Templates;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class TemplateCompilerContextTypeBenchmark {

    @Param({
            "VARS__STRING_CONCAT",
            "VARS__SWITCH",
            "VARS__TERNARY",
            "VARS__SUB_FIELD"
    })
    public Scenario scenario;

    private TemplateCompiler engine;
    private CompiledTemplate untypedTemplate;
    private CompiledTemplate typedTemplate;
    private TemplateCompileContext compileContext;
    private Map<String, Object> data;
    private TemplateScript template;

    @Setup(Level.Trial)
    public void setup() {
        engine = TemplateCompiler.getInstance(
                TemplateCompileOptions.builder()
                        .optimize(true)
                        .build()
        );
        data = DataSamples.byName(scenario);
        template = Templates.byName(scenario);
        compileContext = new MapTemplateCompileContext(buildTypes(data));
        untypedTemplate = engine.compile(template);
        typedTemplate = engine.compile(template, compileContext);
    }

    @Benchmark
    public CompiledTemplate compileWithoutContextType() {
        return engine.compile(template);
    }

    @Benchmark
    public CompiledTemplate compileWithContextType() {
        return engine.compile(template, compileContext);
    }

    @Benchmark
    public Object renderWithoutContextType() {
        return untypedTemplate.render(data);
    }

    @Benchmark
    public Object renderWithContextType() {
        return typedTemplate.render(data);
    }

    private static Map<String, List<Class<?>>> buildTypes(Map<String, Object> context) {
        var types = new LinkedHashMap<String, List<Class<?>>>(context.size());
        for (var entry : context.entrySet()) {
            var value = entry.getValue();
            if (value != null) {
                types.put(entry.getKey(), List.of(value.getClass()));
            }
        }
        return types;
    }
}
