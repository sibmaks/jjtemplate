package io.github.sibmaks.jjtemplate.compiler.impl;

import io.github.sibmaks.jjtemplate.compiler.api.Definition;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileOptions;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompiler;
import io.github.sibmaks.jjtemplate.compiler.api.TemplateScript;
import io.github.sibmaks.jjtemplate.compiler.exception.TemplateCompilationException;
import io.github.sibmaks.jjtemplate.parser.exception.TemplateParseException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class TemplateCompilerImplTest {

    @Test
    void compileTemplateIsRequired() {
        var compiler = TemplateCompiler.getInstance();
        var templateScript = new TemplateScript();
        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(templateScript));
        assertEquals("'template' field required", exception.getMessage());
    }

    @Test
    void compileTemplateWhenTemplateIsInvalid() {
        var compiler = TemplateCompiler.getInstance();
        var templateScript = new TemplateScript();
        templateScript.setTemplate("{{");
        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(templateScript));
        assertEquals("Error compiling template", exception.getMessage());
        var cause = assertInstanceOf(TemplateParseException.class, exception.getCause());
        assertEquals("Parse string: '{{' failed", cause.getMessage());
    }

    @Test
    void compileTemplateWhenDefinitionIsInvalid() {
        var compiler = TemplateCompiler.getInstance();
        var templateScript = new TemplateScript();
        var definition = new Definition();
        definition.put("key", "{{");
        templateScript.setDefinitions(List.of(definition));
        templateScript.setTemplate("ok");
        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(templateScript));
        assertEquals("Error compiling definition", exception.getMessage());
        var cause = assertInstanceOf(TemplateParseException.class, exception.getCause());
        assertEquals("Parse string: '{{' failed", cause.getMessage());
    }

    @Test
    void compileTemplateWithDefinitionFallbackEnabled() {
        var options = TemplateCompileOptions.builder()
                .definitionExpressionFallback(true)
                .build();
        var compiler = TemplateCompiler.getInstance(options);

        var source = new Definition();
        source.put("value", "text");
        source.put("items", List.of(1, 2));

        var switchDefinition = new Definition();
        switchDefinition.put("result switch .value", Map.of(
                "'text'", "matched",
                "else", "fallback"
        ));

        var rangeDefinition = new Definition();
        rangeDefinition.put("list range item,index of .items", "{{ .item }}");

        var script = TemplateScript.builder()
                .definitions(List.of(source, switchDefinition, rangeDefinition))
                .template(Map.of(
                        "switch", "{{ .result }}",
                        "range", "{{ .list }}"
                ))
                .build();

        var compiled = compiler.compile(script);
        var rendered = compiled.render(Map.of());

        assertEquals(
                Map.of("switch", "matched", "range", List.of(1, 2)),
                rendered
        );
    }

    @Test
    void compileTemplateWithDefinitionFallbackDisabled() {
        var options = TemplateCompileOptions.builder()
                .definitionExpressionFallback(false)
                .build();
        var compiler = TemplateCompiler.getInstance(options);

        var source = new Definition();
        source.put("value", "text");
        source.put("items", List.of(1, 2));

        var switchDefinition = new Definition();
        switchDefinition.put("result switch .value", Map.of(
                "'text'", "matched",
                "else", "fallback"
        ));

        var rangeDefinition = new Definition();
        rangeDefinition.put("list range item,index of .items", "{{ .item }}");

        var script = TemplateScript.builder()
                .definitions(List.of(source, switchDefinition, rangeDefinition))
                .template(Map.of(
                        "switch", "{{ .result }}",
                        "range", "{{ .list }}"
                ))
                .build();

        var compiled = compiler.compile(script);
        var rendered = compiled.render(Map.of());

        var expected = new HashMap<String, Object>();
        expected.put("switch", null);
        expected.put("range", null);
        assertEquals(
                expected,
                rendered
        );
    }

    @Test
    void compileTemplateWithDefinitionFallbackEnabledForSwitchInsideRange() {
        var options = TemplateCompileOptions.builder()
                .definitionExpressionFallback(true)
                .build();
        var compiler = TemplateCompiler.getInstance(options);

        var source = new Definition();
        source.put("products", List.of(
                Map.of("category", "MILK", "price", 0),
                Map.of("category", "WATER", "price", 42)
        ));

        var rangeWithSwitch = new Definition();
        rangeWithSwitch.put("objects range product,index of .products", Map.of(
                "category", "{{ .product.category }}",
                "caption switch .product.price | le 0", Map.of(
                        "then", "Free",
                        "false", "Price: {{ .product.price }}"
                )
        ));

        var script = TemplateScript.builder()
                .definitions(List.of(source, rangeWithSwitch))
                .template("{{ .objects }}")
                .build();

        var compiled = compiler.compile(script);
        var rendered = compiled.render(Map.of());

        assertEquals(
                List.of(
                        Map.of("category", "MILK", "caption", "Free"),
                        Map.of("category", "WATER", "caption", "Price: 42")
                ),
                rendered
        );
    }

    @Test
    void compileTemplateWithDefinitionFallbackEnabledForRangeInsideRange() {
        var options = TemplateCompileOptions.builder()
                .definitionExpressionFallback(true)
                .build();
        var compiler = TemplateCompiler.getInstance(options);

        var source = new Definition();
        source.put("products", List.of(
                Map.of("category", "MILK", "tags", List.of("ECO", "HEALTHY")),
                Map.of("category", "WATER", "tags", List.of("PURE"))
        ));

        var nestedRange = new Definition();
        nestedRange.put("objects range product,index of .products", Map.of(
                "category", "{{ .product.category }}",
                "tags range tag,tagIndex of .product.tags", Map.of(
                        "name", "{{ .tag }}"
                )
        ));

        var script = TemplateScript.builder()
                .definitions(List.of(source, nestedRange))
                .template("{{ .objects }}")
                .build();

        var compiled = compiler.compile(script);
        var rendered = compiled.render(Map.of());

        assertEquals(
                List.of(
                        Map.of(
                                "category", "MILK",
                                "tags", List.of(
                                        Map.of("name", "ECO"),
                                        Map.of("name", "HEALTHY")
                                )
                        ),
                        Map.of(
                                "category", "WATER",
                                "tags", List.of(
                                        Map.of("name", "PURE")
                                )
                        )
                ),
                rendered
        );
    }

    @Test
    void compileTemplateWithDefinitionFallbackEnabledElseCaseShouldBeProcessedLast() {
        var options = TemplateCompileOptions.builder()
                .definitionExpressionFallback(true)
                .build();
        var compiler = TemplateCompiler.getInstance(options);

        var source = new Definition();
        source.put("value", "text");

        var switchCases = new java.util.LinkedHashMap<String, Object>();
        switchCases.put("else", "fallback");
        switchCases.put("'text'", "matched");

        var switchDefinition = new Definition();
        switchDefinition.put("result switch .value", switchCases);

        var script = TemplateScript.builder()
                .definitions(List.of(source, switchDefinition))
                .template("{{ .result }}")
                .build();

        var compiled = compiler.compile(script);
        var rendered = compiled.render(Map.of());

        assertEquals("matched", rendered);
    }
}
