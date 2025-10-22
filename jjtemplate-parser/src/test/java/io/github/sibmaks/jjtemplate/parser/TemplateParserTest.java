package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.Token;
import io.github.sibmaks.jjtemplate.lexer.TokenType;
import io.github.sibmaks.jjtemplate.parser.api.FunctionCallExpression;
import io.github.sibmaks.jjtemplate.parser.api.LiteralExpression;
import io.github.sibmaks.jjtemplate.parser.api.PipeExpression;
import io.github.sibmaks.jjtemplate.parser.api.VariableExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
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
        assertEquals(value, literalExpression.value);
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
        var literalExpression = assertInstanceOf(VariableExpression.class, expression);
        assertEquals(List.of(varName), literalExpression.path);
    }

    @Test
    void justAVariableChain() {
        var varName = "varName";
        var subVarName = "varName";
        var tokens = List.of(
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, varName, 1, varName.length() + 1),
                new Token(TokenType.DOT, ".", 0, 1),
                new Token(TokenType.IDENT, subVarName, 1, varName.length() + 1)
        );
        var parser = new TemplateParser(tokens);
        var expression = parser.parseExpression();
        assertNotNull(expression);
        var literalExpression = assertInstanceOf(VariableExpression.class, expression);
        assertEquals(List.of(varName, subVarName), literalExpression.path);
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
        assertEquals(List.of(arg0), argExpression.path);
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
        assertEquals(List.of(arg0), arg0Expression.path);
        var arg1Expression = assertInstanceOf(VariableExpression.class, functionCallExpression.args.get(1));
        assertEquals(List.of(arg1), arg1Expression.path);
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

        var argExpression = assertInstanceOf(VariableExpression.class, rightExpression.args.get(0));
        assertEquals(List.of(arg0), argExpression.path);
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
        var literalExpression = assertInstanceOf(VariableExpression.class, expression);
        assertEquals(List.of(varName), literalExpression.path);
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
}