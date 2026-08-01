package io.github.sibmaks.jjtemplate.compiler.impl;

import io.github.sibmaks.jjtemplate.compiler.api.*;
import io.github.sibmaks.jjtemplate.compiler.exception.TemplateCompilationException;
import io.github.sibmaks.jjtemplate.compiler.runtime.TemplateEvaluationOptions;
import io.github.sibmaks.jjtemplate.compiler.runtime.exception.TemplateEvalException;
import io.github.sibmaks.jjtemplate.compiler.runtime.reflection.FieldResolver;
import io.github.sibmaks.jjtemplate.parser.exception.TemplateParseException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class TemplateCompilerImplTest {

    @Test
    void compileShortCircuitedLazyFunctionToStaticTemplate() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ default 'selected', (cast:int 'not-a-number') }}")
                .build();

        var compiled = compiler.compile(script);

        assertInstanceOf(StaticCompiledTemplateImpl.class, compiled);
        assertEquals("selected", compiled.render());
    }

    @Test
    void compileTemplateShouldUseLocaleFromEvaluationOptions() {
        var options = TemplateCompileOptions.builder()
                .evaluationOptions(TemplateEvaluationOptions.builder()
                        .locale(Locale.forLanguageTag("tr"))
                        .build())
                .build();
        var compiler = TemplateCompiler.getInstance(options);
        var script = TemplateScript.builder()
                .template("{{ string:upper 'i' }}")
                .build();

        var rendered = compiler.compile(script).render();

        assertEquals("İ", rendered);
    }

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
    void compileTemplateTreatsUnwrappedDefinitionExpressionsAsPlainFields() {
        var compiler = TemplateCompiler.getInstance();

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
    void compileTemplateWithSubstitutionInsideStringLiteral() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .test ? '{{ .ok }}' : 'fail' }}")
                .build();

        var compiled = compiler.compile(script);

        assertEquals("ok", compiled.render(Map.of("test", true, "ok", "ok")));
        assertEquals("fail", compiled.render(Map.of("test", false, "ok", "ok")));
    }

    @Test
    void compileTemplateWithConditionalSubstitutionInsideStringLiteralShouldFail() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ '{{? .ok }}' }}")
                .build();

        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(script));
        assertEquals("Error compiling template", exception.getMessage());

        var cause = assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("Only '{{ ... }}' substitutions are allowed inside string literals", cause.getMessage());
    }

    @Test
    void renderShouldIncludeFailedPipeExpressionInException() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value | gt 1 }}")
                .build();

        var compiled = compiler.compile(script);
        var context = new HashMap<String, Object>();
        context.put("value", null);

        var exception = assertThrows(TemplateEvalException.class, () -> compiled.render(context));
        assertEquals("Failed execute: \".value | gt 1\"", exception.getMessage());
        assertEquals("gt: expected number, actual: null", exception.getCause().getMessage());
    }

    @Test
    void renderShouldIncludeFailedFunctionExpressionInException() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ gt .value, 1 }}")
                .build();

        var compiled = compiler.compile(script);
        var context = new HashMap<String, Object>();
        context.put("value", null);

        var exception = assertThrows(TemplateEvalException.class, () -> compiled.render(context));
        assertEquals("Failed execute: \"gt .value, 1\"", exception.getMessage());
        assertEquals("gt: expected number, actual: null", exception.getCause().getMessage());
    }

    @Test
    void compileShouldIncludeFailedPipeExpressionWhenOptimizerEvaluatesConstantExpression() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ null | gt 1 }}")
                .build();

        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(script));
        assertEquals("Error optimizing template", exception.getMessage());

        var cause = assertInstanceOf(TemplateEvalException.class, exception.getCause());
        assertEquals("Failed execute: \"null | gt 1\"", cause.getMessage());
        assertEquals("gt: expected number, actual: null", cause.getCause().getMessage());
    }

    @Test
    void renderShouldIncludeFailedTernaryExpressionInException() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value ? 'yes' : 'no' }}")
                .build();

        var compiled = compiler.compile(script);

        Map<String, Object> context = Map.of("value", 1);
        var exception = assertThrows(TemplateEvalException.class, () -> compiled.render(context));
        assertEquals("Failed execute: \".value ? 'yes' : 'no'\"", exception.getMessage());
        assertEquals(
                "Cannot evaluate expression: .value ? 'yes' : 'no', condition is not boolean: 1",
                exception.getCause().getMessage()
        );
    }

    @Test
    void renderShouldIncludeFailedVariableExpressionInException() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value.missing() }}")
                .build();

        var compiled = compiler.compile(script);

        Map<String, Object> context = Map.of("value", "text");
        var exception = assertThrows(TemplateEvalException.class, () -> compiled.render(context));
        assertEquals("Failed execute: \".value.missing()\"", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void compileWithTypedContextShouldFailForMissingPropertyOnFinalTypeSoft() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value.missing }}")
                .build();
        var context = new MapTemplateCompileContext(
                Map.of("value", List.of(FinalValue.class)),
                TemplateTypeValidationMode.SOFT
        );

        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(script, context));

        assertEquals(
                "Unknown property 'missing' in expression '.value.missing' for types [io.github.sibmaks.jjtemplate.compiler.impl.TemplateCompilerImplTest$FinalValue]",
                exception.getMessage()
        );
    }

    @Test
    void compileWithTypedContextShouldIgnoreMissingPropertyOnNonFinalTypeSoft() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value.missing }}")
                .build();
        var context = new MapTemplateCompileContext(
                Map.of("value", List.of(SoftValue.class)),
                TemplateTypeValidationMode.SOFT
        );

        var compiled = compiler.compile(script, context);

        assertEquals("ok", compiled.render(Map.of("value", new SoftValueChild("ok"))));
    }

    @Test
    void compileWithTypedContextShouldFailForMissingPropertyOnNonFinalTypeStrict() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value.missing }}")
                .build();
        var context = new MapTemplateCompileContext(
                Map.of("value", List.of(SoftValue.class)),
                TemplateTypeValidationMode.STRICT
        );

        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(script, context));

        assertEquals(
                "Unknown property 'missing' in expression '.value.missing' for types [io.github.sibmaks.jjtemplate.compiler.impl.TemplateCompilerImplTest$SoftValue]",
                exception.getMessage()
        );
    }

    @Test
    void compileWithTypedContextShouldTreatMapAccessAsUnknown() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .payload.user.name }}")
                .build();
        var context = new MapTemplateCompileContext(
                Map.of("payload", List.of(Map.class)),
                TemplateTypeValidationMode.STRICT
        );

        var compiled = compiler.compile(script, context);

        assertEquals(
                "Bob",
                compiled.render(Map.of("payload", Map.of("user", Map.of("name", "Bob"))))
        );
    }

    @Test
    void compileWithTypedContextShouldTreatFieldResolverPropertyAsUnknownInStrictMode() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value.missing }}")
                .build();
        var context = new MapTemplateCompileContext(
                Map.of("value", List.of(FieldResolverValue.class)),
                TemplateTypeValidationMode.STRICT
        );

        var compiled = compiler.compile(script, context);

        assertEquals("resolved:missing", compiled.render(Map.of("value", new FieldResolverValue())));
    }

    @Test
    void compileWithTypedContextShouldBindKnownMethod() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value.upper() }}")
                .build();
        var context = new MapTemplateCompileContext(
                Map.of("value", List.of(FinalMethodValue.class)),
                TemplateTypeValidationMode.SOFT
        );

        var compiled = compiler.compile(script, context);

        assertEquals(
                "OK",
                compiled.render(
                        Map.of("value", new FinalMethodValue("ok"))
                )
        );
    }

    @Test
    void compileWithTypedContextShouldInferTypeFromDefinition() {
        var compiler = TemplateCompiler.getInstance();
        var definition = new Definition();
        definition.put("user", "{{ .source }}");
        var script = TemplateScript.builder()
                .definitions(List.of(definition))
                .template("{{ .user.name }}")
                .build();
        var context = new MapTemplateCompileContext(
                Map.of("source", List.of(FinalValue.class)),
                TemplateTypeValidationMode.SOFT
        );

        var compiled = compiler.compile(script, context);

        assertEquals(
                "Alice",
                compiled.render(
                        Map.of("source", new FinalValue("Alice"))
                )
        );
    }

    @Test
    void compileWithTypedContextShouldValidateArrayRangeItem() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template(Map.of("{{ items range item,index of .values }}", "{{ .item.name }}"))
                .build();

        var context = new MapTemplateCompileContext(
                Map.of("values", List.of(FinalValue[].class)),
                TemplateTypeValidationMode.SOFT
        );

        var compiled = compiler.compile(script, context);

        assertEquals(
                Map.of("items", List.of("Alice", "Bob")),
                compiled.render(
                        Map.of(
                                "values",
                                new FinalValue[]{
                                        new FinalValue("Alice"),
                                        new FinalValue("Bob")
                                }
                        )
                )
        );
    }

    @Test
    void compileShouldWrapUnexpectedBindingErrors() {
        var compiler = TemplateCompiler.getInstance();
        var script = TemplateScript.builder()
                .template("{{ .value.name }}")
                .build();
        TemplateCompileContext context = variableName -> {
            throw new IllegalStateException("boom");
        };

        var exception = assertThrows(TemplateCompilationException.class, () -> compiler.compile(script, context));

        assertEquals("Error binding template types", exception.getMessage());
        assertEquals("boom", exception.getCause().getMessage());
    }

    @Test
    void compileShouldRejectNonFieldDefinitionElements() {
        var compiler = TemplateCompiler.getInstance();
        var definition = new Definition();
        definition.put("{{? true }}", "value");
        var script = TemplateScript.builder()
                .definitions(List.of(definition))
                .template("ok")
                .build();

        var exception = assertThrows(TemplateEvalException.class, () -> compiler.compile(script));

        assertTrue(exception.getMessage().contains("Unknown object field element type"));
    }

    private static final class FinalValue {
        private final String name;

        private FinalValue(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static class SoftValue {
    }

    private static final class SoftValueChild extends SoftValue {
        private final String missing;

        private SoftValueChild(String missing) {
            this.missing = missing;
        }

        public String getMissing() {
            return missing;
        }
    }

    private static final class FinalMethodValue {
        private final String value;

        private FinalMethodValue(String value) {
            this.value = value;
        }

        public String upper() {
            return value.toUpperCase();
        }
    }

    private static final class FieldResolverValue implements FieldResolver {
        @Override
        public Object resolve(String fieldName) {
            return "resolved:" + fieldName;
        }
    }
}
