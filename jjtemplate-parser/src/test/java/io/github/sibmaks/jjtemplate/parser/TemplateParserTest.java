package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import io.github.sibmaks.jjtemplate.parser.api.FunctionCallExpression;
import io.github.sibmaks.jjtemplate.parser.api.LiteralExpression;
import io.github.sibmaks.jjtemplate.parser.api.PipeExpression;
import io.github.sibmaks.jjtemplate.parser.api.RangeExpression;
import io.github.sibmaks.jjtemplate.parser.api.SpreadExpression;
import io.github.sibmaks.jjtemplate.parser.api.SwitchExpression;
import io.github.sibmaks.jjtemplate.parser.api.ThenSwitchCaseExpression;
import io.github.sibmaks.jjtemplate.parser.api.ElseSwitchCaseExpression;
import io.github.sibmaks.jjtemplate.parser.api.TernaryExpression;
import io.github.sibmaks.jjtemplate.parser.api.VariableExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class TemplateParserTest {

    @Test
    void justAString() {
        var value = UUID.randomUUID().toString();
        var tokens = List.of(
                new Token(TokenType.STRING, value, 0, value.length())
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var literalExpression = assertInstanceOf(LiteralExpression.class, expression);
        assertEquals(value, literalExpression.value);
    }

    @Test
    void justAInteger() {
        var value = UUID.randomUUID().hashCode();
        var stringValue = value + "";
        var tokens = List.of(
                new Token(TokenType.NUMBER, stringValue, 0, stringValue.length())
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var literalExpression = assertInstanceOf(LiteralExpression.class, expression);
        assertEquals(BigInteger.valueOf(value), literalExpression.value);
    }

    @Test
    void justADouble() {
        var value = UUID.randomUUID().hashCode() / 100.;
        var stringValue = value + "";
        var tokens = List.of(
                new Token(TokenType.NUMBER, stringValue, 0, stringValue.length())
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var literalExpression = assertInstanceOf(LiteralExpression.class, expression);
        var excepted = BigDecimal.valueOf(value);
        assertEquals(excepted, literalExpression.value);
    }

    @Test
    void justANull() {
        var value = "null";
        var tokens = List.of(
                new Token(TokenType.NULL, value, 0, value.length())
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var literalExpression = assertInstanceOf(LiteralExpression.class, expression);
        assertNull(literalExpression.value);
    }

    @ParameterizedTest
    @ValueSource(booleans = {
            true,
            false
    })
    void justABoolean(boolean value) {
        var stringValue = value + "";
        var tokens = List.of(
                new Token(TokenType.BOOLEAN, stringValue, 0, stringValue.length())
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var literalExpression = assertInstanceOf(LiteralExpression.class, expression);
        assertEquals(value, literalExpression.value);
    }

    @Test
    void justASingleVariable() {
        var varName = "varName";
        var tokens = List.of(
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, varName, 1, varName.length() + 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var variableExpression = assertInstanceOf(VariableExpression.class, expression);
        assertEquals(List.of(new VariableExpression.Segment(varName)), variableExpression.segments);
    }

    @Test
    void justAVariableChain() {
        var varName = "varName";
        var subVarName = "subVarName";
        var tokens = List.of(
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, varName, 1, varName.length() + 1),
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, subVarName, 1, varName.length() + 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var variableExpression = assertInstanceOf(VariableExpression.class, expression);
        assertEquals(
                List.of(
                        new VariableExpression.Segment(varName),
                        new VariableExpression.Segment(subVarName)
                ),
                variableExpression.segments
        );
    }

    @Test
    void callMethodInChain() {
        var varName = "varName";
        var methodName = "methoName";
        var argValue = UUID.randomUUID().toString();
        var tokens = List.of(
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, varName, 1, 1),
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, methodName, 1, 1),
                new Token(TokenType.LPAREN, "(", 1, 1),
                new Token(TokenType.STRING, argValue, 1, 1),
                new Token(TokenType.RPAREN, ")", 1, 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var variableExpression = assertInstanceOf(VariableExpression.class, expression);
        assertEquals(
                List.of(
                        new VariableExpression.Segment(varName),
                        new VariableExpression.Segment(methodName, List.of(
                                new LiteralExpression(argValue)
                        ))
                ),
                variableExpression.segments
        );
    }

    @Test
    void justAFunctionCall() {
        var functionName = "functionName";
        var tokens = List.of(
                new Token(TokenType.IDENT, functionName, 1, functionName.length() + 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var functionCallExpression = assertInstanceOf(FunctionCallExpression.class, expression);
        assertEquals(functionName, functionCallExpression.name);
        assertEquals(List.of(), functionCallExpression.args);
    }

    @Test
    void justAFunctionWithArgCall() {
        var functionName = "functionName";
        var arg0 = "arg0";
        var tokens = List.of(
                new Token(TokenType.IDENT, functionName, 1, functionName.length() + 1),
                new Token(TokenType.DOT, ".", 1, 1),
                new Token(TokenType.IDENT, arg0, 1, arg0.length() + 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var functionCallExpression = assertInstanceOf(FunctionCallExpression.class, expression);
        assertEquals(functionName, functionCallExpression.name);
        var argExpression = assertInstanceOf(VariableExpression.class, functionCallExpression.args.get(0));
        assertEquals(List.of(new VariableExpression.Segment(arg0)), argExpression.segments);
    }

    @Test
    void justAFunctionWithArgsCall() {
        var functionName = "functionName";
        var arg0 = "arg0";
        var arg1 = "arg1";
        var tokens = List.of(
                new Token(TokenType.IDENT, functionName, 1, functionName.length() + 1),
                new Token(TokenType.DOT, ".", 1, 1),
                new Token(TokenType.IDENT, arg0, 1, 1),
                new Token(TokenType.COMMA, ",", 1, 1),
                new Token(TokenType.DOT, ".", 1, 1),
                new Token(TokenType.IDENT, arg1, 1, 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var functionCallExpression = assertInstanceOf(FunctionCallExpression.class, expression);
        assertEquals(functionName, functionCallExpression.name);
        var arg0Expression = assertInstanceOf(VariableExpression.class, functionCallExpression.args.get(0));
        assertEquals(List.of(new VariableExpression.Segment(arg0)), arg0Expression.segments);
        var arg1Expression = assertInstanceOf(VariableExpression.class, functionCallExpression.args.get(1));
        assertEquals(List.of(new VariableExpression.Segment(arg1)), arg1Expression.segments);
    }

    @Test
    void pipeFunctionCall() {
        var leftFunctionName = "leftFunction";
        var rightFunctionName = "rightFunction";
        var tokens = List.of(
                new Token(TokenType.IDENT, leftFunctionName, 1, leftFunctionName.length() + 1),
                new Token(TokenType.PIPE, "|", 1, 1),
                new Token(TokenType.IDENT, rightFunctionName, 1, rightFunctionName.length() + 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var pipeExpression = assertInstanceOf(PipeExpression.class, expression);
        var leftExpression = assertInstanceOf(FunctionCallExpression.class, pipeExpression.left);
        assertEquals(leftFunctionName, leftExpression.name);

        var rightExpression = assertInstanceOf(FunctionCallExpression.class, pipeExpression.chain.get(0));
        assertEquals(rightFunctionName, rightExpression.name);
    }

    @Test
    void pipeFunctionCallWithArgs() {
        var leftFunctionName = "leftFunction";
        var rightFunctionName = "rightFunction";
        var arg0 = "arg0";
        var tokens = List.of(
                new Token(TokenType.IDENT, leftFunctionName, 1, leftFunctionName.length() + 1),
                new Token(TokenType.PIPE, "|", 1, 1),
                new Token(TokenType.IDENT, rightFunctionName, 1, rightFunctionName.length() + 1),
                new Token(TokenType.DOT, ".", 1, 1),
                new Token(TokenType.IDENT, arg0, 1, arg0.length() + 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var pipeExpression = assertInstanceOf(PipeExpression.class, expression);
        var leftExpression = assertInstanceOf(FunctionCallExpression.class, pipeExpression.left);
        assertEquals(leftFunctionName, leftExpression.name);

        var rightExpression = assertInstanceOf(FunctionCallExpression.class, pipeExpression.chain.get(0));
        assertEquals(rightFunctionName, rightExpression.name);

        var arg0Expression = assertInstanceOf(VariableExpression.class, rightExpression.args.get(0));
        assertEquals(List.of(new VariableExpression.Segment(arg0)), arg0Expression.segments);
    }

    @Test
    void parentCall() {
        var varName = "varName";
        var tokens = List.of(
                new Token(TokenType.LPAREN, "(", 0, 1),
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, varName, 0, 1),
                new Token(TokenType.RPAREN, ")", 0, 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var variableExpression = assertInstanceOf(VariableExpression.class, expression);
        assertEquals(List.of(new VariableExpression.Segment(varName)), variableExpression.segments);
    }

    @Test
    void exceptedAnotherType() {
        var tokens = List.of(
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.NUMBER, ".", 0, 1)
        );
        var parser = new TemplateParser(tokens);
        var exception = assertThrows(TemplateParserException.class, parser::parseExpression);
        assertEquals("Expected identifier after '.', got NUMBER at token position 1", exception.getMessage());
    }

    @Test
    void withoutRightParent() {
        var tokens = List.of(
                new Token(TokenType.LPAREN, "(", 0, 1),
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, "varName", 0, 1)
        );
        var parser = new TemplateParser(tokens);
        var exception = assertThrows(TemplateParserException.class, parser::parseExpression);
        assertEquals("Expected ), but reached end at token position 3", exception.getMessage());
    }

    @Test
    void unexpectedEndOfExpression() {
        var tokens = List.<Token>of();
        var parser = new TemplateParser(tokens);
        var exception = assertThrows(TemplateParserException.class, parser::parseExpression);
        assertEquals("Unexpected end of expression at token position 0", exception.getMessage());
    }

    @Test
    void unexpectedToken() {
        var tokens = List.of(
                new Token(TokenType.COMMA, ",", 0, 1)
        );
        var parser = new TemplateParser(tokens);
        var exception = assertThrows(TemplateParserException.class, parser::parseExpression);
        assertEquals("Unexpected token: COMMA at token position 0", exception.getMessage());
    }

    @Test
    void namespacedFunctionCall() {
        var tokens = List.of(
                new Token(TokenType.IDENT, "str", 0, 3),
                new Token(TokenType.COLON, ":", 3, 4),
                new Token(TokenType.IDENT, "join", 4, 8),
                new Token(TokenType.STRING, "x", 9, 12)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(FunctionCallExpression.class, parser.parseExpression());

        assertEquals("str", expression.namespace);
        assertEquals("join", expression.name);
        assertEquals(List.of(new LiteralExpression("x")), expression.args);
    }

    @Test
    void ternaryExpression() {
        var tokens = List.of(
                new Token(TokenType.BOOLEAN, "true", 0, 4),
                new Token(TokenType.QUESTION, "?", 5, 6),
                new Token(TokenType.STRING, "yes", 7, 12),
                new Token(TokenType.COLON, ":", 13, 14),
                new Token(TokenType.STRING, "no", 15, 19)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(TernaryExpression.class, parser.parseExpression());

        assertEquals(Boolean.TRUE, assertInstanceOf(LiteralExpression.class, expression.condition).value);
        assertEquals("yes", assertInstanceOf(LiteralExpression.class, expression.ifTrue).value);
        assertEquals("no", assertInstanceOf(LiteralExpression.class, expression.ifFalse).value);
    }

    @Test
    void postfixSwitchExpression() {
        var tokens = List.of(
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, "value", 1, 6),
                new Token(TokenType.KEYWORD, "switch", 7, 13),
                new Token(TokenType.STRING, "kind", 14, 20)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(SwitchExpression.class, parser.parseExpression());

        assertEquals(
                List.of(new VariableExpression.Segment("value")),
                assertInstanceOf(VariableExpression.class, expression.key).segments
        );
        assertEquals("kind", assertInstanceOf(LiteralExpression.class, expression.condition).value);
    }

    @Test
    void prefixSwitchExpression() {
        var tokens = List.of(
                new Token(TokenType.IDENT, "caseName", 0, 8),
                new Token(TokenType.KEYWORD, "switch", 9, 15),
                new Token(TokenType.STRING, "state", 16, 23)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(SwitchExpression.class, parser.parseExpression());

        assertEquals("caseName", assertInstanceOf(LiteralExpression.class, expression.key).value);
        assertEquals("state", assertInstanceOf(LiteralExpression.class, expression.condition).value);
    }

    @Test
    void postfixRangeExpression() {
        var tokens = List.of(
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, "items", 1, 6),
                new Token(TokenType.KEYWORD, "range", 7, 12),
                new Token(TokenType.IDENT, "item", 13, 17),
                new Token(TokenType.COMMA, ",", 17, 18),
                new Token(TokenType.IDENT, "index", 19, 24),
                new Token(TokenType.KEYWORD, "of", 25, 27),
                new Token(TokenType.DOT, ".", 28, 29),
                new Token(TokenType.IDENT, "values", 29, 35)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(RangeExpression.class, parser.parseExpression());

        assertEquals(
                List.of(new VariableExpression.Segment("items")),
                assertInstanceOf(VariableExpression.class, expression.name).segments
        );
        assertEquals("item", expression.itemVariableName);
        assertEquals("index", expression.indexVariableName);
        assertEquals(
                List.of(new VariableExpression.Segment("values")),
                assertInstanceOf(VariableExpression.class, expression.source).segments
        );
    }

    @Test
    void prefixRangeExpression() {
        var tokens = List.of(
                new Token(TokenType.IDENT, "entries", 0, 7),
                new Token(TokenType.KEYWORD, "range", 8, 13),
                new Token(TokenType.IDENT, "item", 14, 18),
                new Token(TokenType.COMMA, ",", 18, 19),
                new Token(TokenType.IDENT, "idx", 20, 23),
                new Token(TokenType.KEYWORD, "of", 24, 26),
                new Token(TokenType.DOT, ".", 27, 28),
                new Token(TokenType.IDENT, "source", 28, 34)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(RangeExpression.class, parser.parseExpression());

        assertEquals("entries", assertInstanceOf(LiteralExpression.class, expression.name).value);
        assertEquals("item", expression.itemVariableName);
        assertEquals("idx", expression.indexVariableName);
    }

    @Test
    void thenAndElseSwitchCases() {
        var thenTokens = List.of(
                new Token(TokenType.KEYWORD, "then", 0, 4),
                new Token(TokenType.KEYWORD, "switch", 5, 11),
                new Token(TokenType.STRING, "x", 12, 15)
        );
        var thenParser = new TemplateParser(thenTokens);
        var thenExpression = assertInstanceOf(ThenSwitchCaseExpression.class, thenParser.parseExpression());
        assertEquals("x", assertInstanceOf(LiteralExpression.class, thenExpression.condition).value);

        var elseTokens = List.of(
                new Token(TokenType.KEYWORD, "else", 0, 4)
        );
        var elseParser = new TemplateParser(elseTokens);
        var elseExpression = assertInstanceOf(ElseSwitchCaseExpression.class, elseParser.parseExpression());
        assertNull(elseExpression.condition);

        var elseSwitchParser = new TemplateParser(List.of(
                new Token(TokenType.KEYWORD, "else", 0, 4),
                new Token(TokenType.KEYWORD, "switch", 5, 11),
                new Token(TokenType.STRING, "fallback", 12, 22)
        ));
        var elseSwitchExpression = assertInstanceOf(ElseSwitchCaseExpression.class, elseSwitchParser.parseExpression());
        assertEquals("fallback", assertInstanceOf(LiteralExpression.class, elseSwitchExpression.condition).value);

        var plainThenParser = new TemplateParser(List.of(
                new Token(TokenType.KEYWORD, "then", 0, 4)
        ));
        var plainThenExpression = assertInstanceOf(ThenSwitchCaseExpression.class, plainThenParser.parseExpression());
        assertNull(plainThenExpression.condition);
    }

    @Test
    void functionCallShouldSupportSpreadArguments() {
        var tokens = List.of(
                new Token(TokenType.IDENT, "concat", 0, 6),
                new Token(TokenType.DOT, ".", 7, 8),
                new Token(TokenType.DOT, ".", 8, 9),
                new Token(TokenType.DOT, ".", 9, 10),
                new Token(TokenType.DOT, ".", 10, 11),
                new Token(TokenType.IDENT, "items", 11, 16)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(FunctionCallExpression.class, parser.parseExpression());

        var spreadExpression = assertInstanceOf(SpreadExpression.class, expression.args.get(0));
        assertEquals(
                List.of(new VariableExpression.Segment("items")),
                assertInstanceOf(VariableExpression.class, spreadExpression.source).segments
        );
    }

    @Test
    void nestedInterpolationShouldBeParsedAsPrimaryExpression() {
        var tokens = List.of(
                new Token(TokenType.OPEN_EXPR, "{{", 0, 2),
                new Token(TokenType.DOT, ".", 3, 4),
                new Token(TokenType.IDENT, "value", 4, 9),
                new Token(TokenType.CLOSE, "}}", 10, 12)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(VariableExpression.class, parser.parseExpression());

        assertEquals(List.of(new VariableExpression.Segment("value")), expression.segments);
    }

    @Test
    void nestedInterpolationShouldSupportNestedOpeners() {
        var tokens = List.of(
                new Token(TokenType.OPEN_EXPR, "{{", 0, 2),
                new Token(TokenType.OPEN_EXPR, "{{", 3, 5),
                new Token(TokenType.DOT, ".", 6, 7),
                new Token(TokenType.IDENT, "value", 7, 12),
                new Token(TokenType.CLOSE, "}}", 13, 15),
                new Token(TokenType.CLOSE, "}}", 16, 18)
        );
        var parser = new TemplateParser(tokens);

        var expression = assertInstanceOf(VariableExpression.class, parser.parseExpression());

        assertEquals(List.of(new VariableExpression.Segment("value")), expression.segments);
    }

    @Test
    void nestedInterpolationShouldRejectConditionalOpenToken() {
        var tokens = List.of(
                new Token(TokenType.OPEN_COND, "{{?", 0, 3)
        );
        var parser = new TemplateParser(tokens);

        var exception = assertThrows(TemplateParserException.class, parser::parseExpression);

        assertEquals("Only '{{ ... }}' is allowed in nested interpolation at token position 1", exception.getMessage());
    }

    @Test
    void nestedInterpolationShouldRejectMissingClose() {
        var tokens = List.of(
                new Token(TokenType.OPEN_EXPR, "{{", 0, 2),
                new Token(TokenType.DOT, ".", 3, 4),
                new Token(TokenType.IDENT, "value", 4, 9)
        );
        var parser = new TemplateParser(tokens);

        var exception = assertThrows(TemplateParserException.class, parser::parseExpression);

        assertEquals("Missing closing '}}' at token position 3", exception.getMessage());
    }

    @Test
    void nestedInterpolationShouldRejectNullTokenInsideStream() {
        var parser = new TemplateParser(Arrays.asList(
                new Token(TokenType.OPEN_EXPR, "{{", 0, 2),
                null
        ));

        var exception = assertThrows(TemplateParserException.class, parser::parseExpression);

        assertEquals("Missing closing '}}' at token position 1", exception.getMessage());
    }

    @Test
    void parserShouldFailWhenTrailingTokensRemain() {
        var tokens = List.of(
                new Token(TokenType.STRING, "value", 0, 7),
                new Token(TokenType.STRING, "tail", 8, 14)
        );
        var parser = new TemplateParser(tokens);

        parser.parseExpression();
        var exception = assertThrows(TemplateParserException.class, parser::expectEnd);

        assertEquals("Unexpected token: STRING at token position 1", exception.getMessage());
    }
}
