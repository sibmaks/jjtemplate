package io.github.sibmaks.jjtemplate.compiler.runtime;

import io.github.sibmaks.jjtemplate.compiler.runtime.context.Context;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.ConstantTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.RangeTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.object.ObjectFieldElement;
import io.github.sibmaks.jjtemplate.compiler.runtime.expression.switch_case.SwitchDefinitionTemplateExpression;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.TemplateType;
import io.github.sibmaks.jjtemplate.compiler.runtime.visitor.TemplateTypeInferenceVisitor;
import io.github.sibmaks.jjtemplate.parser.ExpressionParser;
import io.github.sibmaks.jjtemplate.parser.exception.TemplateParseException;
import io.github.sibmaks.jjtemplate.parser.parser.JJTemplateParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sibmaks
 */
class RootTemplateExpressionFactoryTest {

    @Test
    void compileListShouldWrapCollectionItemParseFailure() {
        var factory = new RootTemplateExpressionFactory(
                new TemplateTypeInferenceVisitor(),
                new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build()),
                new ExpressionParser(),
                false
        );

        var exception = assertThrows(TemplateParseException.class, () -> factory.compile(List.of("{{")));

        assertEquals("Parse collection item: '{{' failed", exception.getMessage());
    }

    @Test
    void compileObjectShouldWrapObjectFieldParseFailure() {
        var factory = new RootTemplateExpressionFactory(
                new TemplateTypeInferenceVisitor(),
                new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build()),
                new ExpressionParser(),
                false
        );

        var exception = assertThrows(TemplateParseException.class, () -> factory.compileObject(Map.of("{{", "value")));

        assertEquals("Parse object field: '{{' failed", exception.getMessage());
    }

    @Test
    void compileObjectShouldCompilePlainObjectField() {
        var factory = new RootTemplateExpressionFactory(
                new TemplateTypeInferenceVisitor(),
                new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build()),
                new ExpressionParser(),
                false
        );

        var object = factory.compileObject(Map.of("name", "Bob"));

        assertEquals(Map.of("name", "Bob"), object.apply(Context.empty()));
    }

    @Test
    void compileDefinitionObjectShouldRequireMapForNestedSwitchCase() {
        var factory = new RootTemplateExpressionFactory(
                new TemplateTypeInferenceVisitor(),
                new TemplateExpressionFactory(TemplateEvaluationOptions.builder().build()),
                new ExpressionParser(),
                true
        );

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.compileDefinitionObject(Map.of(
                        "result switch .value", Map.of("then switch .flag", "not-a-map")
                ))
        );

        assertEquals("Expected a map entry for 'then switch .flag'", exception.getMessage());
    }

    @Test
    void compileDefinitionObjectShouldRejectNonSwitchCompiledKey() {
        TemplateTypeInferenceVisitor inferenceVisitor = mock();
        TemplateExpressionFactory expressionFactory = mock();
        ExpressionParser parser = new ExpressionParser();

        when(inferenceVisitor.infer(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(TemplateType.SWITCH)
                .thenReturn(TemplateType.SWITCH_ELSE);
        when(expressionFactory.compile(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(new ConstantTemplateExpression("not-a-switch"));

        var factory = new RootTemplateExpressionFactory(
                inferenceVisitor,
                expressionFactory,
                parser,
                true
        );

        var exception = assertThrows(
                IllegalStateException.class,
                () -> factory.compileDefinitionObject(Map.of(
                        "result switch .value", Map.of("else", "fallback")
                ))
        );

        assertEquals("Switch definition expression expected", exception.getMessage());
    }

    @Test
    void compileListShouldRejectUnsupportedCollectionKeyType() {
        TemplateTypeInferenceVisitor inferenceVisitor = mock();
        TemplateExpressionFactory expressionFactory = mock();
        ExpressionParser parser = new ExpressionParser();

        when(inferenceVisitor.infer(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(TemplateType.SWITCH);
        when(expressionFactory.compile(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(new ConstantTemplateExpression("value"));

        var factory = new RootTemplateExpressionFactory(inferenceVisitor, expressionFactory, parser, false);

        var exception = assertThrows(IllegalArgumentException.class, () -> factory.compile(List.of("{{ .value }}")));

        assertEquals("Unknown key type: SWITCH", exception.getMessage());
    }

    @Test
    void compileDefinitionObjectShouldRejectUnsupportedObjectKeyType() {
        TemplateTypeInferenceVisitor inferenceVisitor = mock();
        TemplateExpressionFactory expressionFactory = mock();
        ExpressionParser parser = new ExpressionParser();

        when(inferenceVisitor.infer(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(TemplateType.SWITCH_ELSE);

        var factory = new RootTemplateExpressionFactory(inferenceVisitor, expressionFactory, parser, true);

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.compileDefinitionObject(Map.of("field", "value"))
        );

        assertEquals("Unknown key type: SWITCH_ELSE", exception.getMessage());
    }

    @Test
    void compileDefinitionObjectShouldRejectNonRangeCompiledKey() {
        TemplateTypeInferenceVisitor inferenceVisitor = mock();
        TemplateExpressionFactory expressionFactory = mock();
        ExpressionParser parser = new ExpressionParser();

        when(inferenceVisitor.infer(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(TemplateType.RANGE);
        when(expressionFactory.compile(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(new ConstantTemplateExpression("not-a-range"));

        var factory = new RootTemplateExpressionFactory(
                inferenceVisitor,
                expressionFactory,
                parser,
                true
        );

        var exception = assertThrows(
                ClassCastException.class,
                () -> factory.compileDefinitionObject(Map.of(
                        "items range item,index of .values", "{{ .item }}"
                ))
        );

        assertTrue(exception.getMessage().contains("ConstantTemplateExpression"));
        assertTrue(exception.getMessage().contains("RangeTemplateExpression"));
    }

    @Test
    void compileDefinitionObjectShouldBuildRangeFromCompiledKey() {
        var key = new ConstantTemplateExpression("items");
        var source = new ConstantTemplateExpression(List.of("a", "b"));
        RangeTemplateExpression range = RangeTemplateExpression.builder()
                .name(key)
                .itemVariableName("item")
                .indexVariableName("index")
                .source(source)
                .body(new ConstantTemplateExpression(null))
                .sourceExpression("items range item,index of .values")
                .build();

        SwitchDefinitionTemplateExpression unused = SwitchDefinitionTemplateExpression.builder()
                .key(key)
                .condition(new ConstantTemplateExpression(true))
                .sourceExpression("unused")
                .build();
        assertNotNull(unused);

        TemplateTypeInferenceVisitor inferenceVisitor = mock();
        TemplateExpressionFactory expressionFactory = mock();
        ExpressionParser parser = new ExpressionParser();

        when(inferenceVisitor.infer(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(TemplateType.RANGE)
                .thenReturn(TemplateType.EXPRESSION);
        when(expressionFactory.compile(any(JJTemplateParser.TemplateContext.class)))
                .thenReturn(range)
                .thenReturn(new ConstantTemplateExpression("body"));

        var factory = new RootTemplateExpressionFactory(
                inferenceVisitor,
                expressionFactory,
                parser,
                true
        );

        var object = factory.compileDefinitionObject(Map.of(
                "items range item,index of .values", "{{ .item }}"
        ));

        assertEquals(1, object.getElements().size());
        var field = assertInstanceOf(ObjectFieldElement.class, object.getElements().get(0));
        assertEquals("items", ((ConstantTemplateExpression) field.getKey()).getValue());
    }
}
