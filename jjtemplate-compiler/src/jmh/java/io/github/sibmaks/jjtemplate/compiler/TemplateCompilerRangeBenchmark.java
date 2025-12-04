package io.github.sibmaks.jjtemplate.compiler;

import io.github.sibmaks.jjtemplate.compiler.api.*;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Fork(3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
public class TemplateCompilerRangeBenchmark {

    @Param({"1", "4", "16", "64", "256", "1024"})
    public int length;
    @Param({"true", "false"})
    public boolean inline;

    private TemplateCompiler engine;
    private CompiledTemplate compiledTemplate;
    private Map<String, Object> data;
    private TemplateScript template;

    @Setup(Level.Trial)
    public void setup() {
        engine = TemplateCompiler.getInstance(
                TemplateCompileOptions.builder()
                        .optimize(true)
                        .build()
        );

        var list = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            list.add(Integer.toString(i));
        }

        if (inline) {
            var definition = new Definition();
            definition.put("list", list);
            var rangeDefinition = new Definition();
            rangeDefinition.put("rs range item,index of .list", "{{ .index }}: {{ .item }}");
            data = Map.of();
            template = TemplateScript.builder()
                    .definitions(
                            List.of(
                                    definition,
                                    rangeDefinition
                            )
                    )
                    .template("{{ .list }}")
                    .build();
        } else {
            data = Map.of(
                    "list", list
            );
            var rangeDefinition = new Definition();
            rangeDefinition.put("rs range item,index of .list", "{{ .index }}: {{ .item }}");
            template = TemplateScript.builder()
                    .definitions(
                            List.of(
                                    rangeDefinition
                            )
                    )
                    .template("{{ .list }}")
                    .build();
        }

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
