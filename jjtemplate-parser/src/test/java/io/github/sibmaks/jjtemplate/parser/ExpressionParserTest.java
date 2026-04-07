package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import io.github.sibmaks.jjtemplate.parser.api.FunctionCallExpression;
import io.github.sibmaks.jjtemplate.parser.api.VariableExpression;
import io.github.sibmaks.jjtemplate.parser.exception.TemplateParseException;
import io.github.sibmaks.jjtemplate.parser.parser.JJTemplateParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionParserTest {

    @Test
    void parseShouldBuildTemplateContextWithAllInterpolationTypes() {
        var parser = new ExpressionParser();

        var context = parser.parse("prefix {{ .value }} middle {{? .condition }} suffix {{. spread }}");

        assertEquals(6, context.getParts().size());
        assertInstanceOf(JJTemplateParser.TextPart.class, context.getParts().get(0));
        assertInstanceOf(JJTemplateParser.InterpolationPart.class, context.getParts().get(1));
        assertInstanceOf(JJTemplateParser.TextPart.class, context.getParts().get(2));
        assertInstanceOf(JJTemplateParser.InterpolationPart.class, context.getParts().get(3));
        assertInstanceOf(JJTemplateParser.TextPart.class, context.getParts().get(4));
        assertInstanceOf(JJTemplateParser.InterpolationPart.class, context.getParts().get(5));

        var expressionPart = (JJTemplateParser.InterpolationPart) context.getParts().get(1);
        assertEquals(JJTemplateParser.InterpolationType.EXPRESSION, expressionPart.getType());
        assertEquals(
                List.of(new VariableExpression.Segment("value")),
                assertInstanceOf(VariableExpression.class, expressionPart.getExpression()).segments
        );

        var conditionPart = (JJTemplateParser.InterpolationPart) context.getParts().get(3);
        assertEquals(JJTemplateParser.InterpolationType.CONDITION, conditionPart.getType());

        var spreadPart = (JJTemplateParser.InterpolationPart) context.getParts().get(5);
        assertEquals(JJTemplateParser.InterpolationType.SPREAD, spreadPart.getType());
        assertEquals(
                "spread",
                assertInstanceOf(FunctionCallExpression.class, spreadPart.getExpression()).name
        );
    }

    @Test
    void parseShouldSupportNestedInterpolation() {
        var parser = new ExpressionParser();

        var context = parser.parse("{{ {{ .value }} }}");

        assertEquals(1, context.getParts().size());
        var interpolationPart = assertInstanceOf(JJTemplateParser.InterpolationPart.class, context.getParts().get(0));
        var variableExpression = assertInstanceOf(VariableExpression.class, interpolationPart.getExpression());
        assertEquals(List.of(new VariableExpression.Segment("value")), variableExpression.segments);
    }

    @Test
    void parseShouldWrapLexerFailure() {
        var parser = new ExpressionParser();

        var exception = assertThrows(TemplateParseException.class, () -> parser.parse("{{ 'text"));

        assertEquals("Parse failed: '{{ 'text'", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void parseShouldWrapParserFailure() {
        var parser = new ExpressionParser();

        var exception = assertThrows(TemplateParseException.class, () -> parser.parse("{{ . }}"));

        assertEquals("Parse failed: '{{ . }}'", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void privateHelpersShouldHandleUnsupportedCases() throws Exception {
        var parser = new ExpressionParser();

        var mapInterpolationType = ExpressionParser.class.getDeclaredMethod("mapInterpolationType", TokenType.class);
        mapInterpolationType.setAccessible(true);
        var mapException = assertThrows(
                InvocationTargetException.class,
                () -> mapInterpolationType.invoke(parser, TokenType.TEXT)
        );
        assertTrue(mapException.getCause() instanceof TemplateParseException);
        assertEquals("Unknown interpolation type: TEXT", mapException.getCause().getMessage());

        var findClose = ExpressionParser.class.getDeclaredMethod("findClose", List.class, int.class);
        findClose.setAccessible(true);
        var findCloseException = assertThrows(
                InvocationTargetException.class,
                () -> findClose.invoke(parser, List.of(new Token(TokenType.OPEN_EXPR, "{{", 0, 2)), 1)
        );
        assertTrue(findCloseException.getCause() instanceof TemplateParseException);
        assertEquals("Missing closing '}}'", findCloseException.getCause().getMessage());
    }

    @Test
    void jjTemplateParserHelpersShouldBeInstantiable() throws Exception {
        var constructor = JJTemplateParser.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var instance = constructor.newInstance();
        assertNotNull(instance);

        var nullPartsContext = new JJTemplateParser.TemplateContext(null);
        assertTrue(nullPartsContext.getParts().isEmpty());

        var textPart = new JJTemplateParser.TextPart("text");
        assertEquals("text", textPart.getText());

        var interpolationPart = new JJTemplateParser.InterpolationPart(
                JJTemplateParser.InterpolationType.EXPRESSION,
                new VariableExpression(List.of(new VariableExpression.Segment("value")))
        );
        assertEquals(JJTemplateParser.InterpolationType.EXPRESSION, interpolationPart.getType());
        assertInstanceOf(VariableExpression.class, interpolationPart.getExpression());
        assertEquals(3, JJTemplateParser.InterpolationType.values().length);
    }

    @Test
    void templateParseExceptionConstructorsShouldPreserveMessageAndCause() {
        var simple = new TemplateParseException("message");
        assertEquals("message", simple.getMessage());
        assertNull(simple.getCause());

        var cause = new IllegalStateException("cause");
        var wrapped = new TemplateParseException("wrapped", cause);
        assertEquals("wrapped", wrapped.getMessage());
        assertSame(cause, wrapped.getCause());
    }
}
