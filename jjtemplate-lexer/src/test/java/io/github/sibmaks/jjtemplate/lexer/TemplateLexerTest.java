package io.github.sibmaks.jjtemplate.lexer;

import io.github.sibmaks.jjtemplate.lexer.api.Keyword;
import io.github.sibmaks.jjtemplate.lexer.api.TemplateLexerException;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author sibmaks
 */
class TemplateLexerTest {

    @Test
    void emptyInput() {
        var lexer = new TemplateLexer("");
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(0, tokens.size());
    }

    @Test
    void oneBracketIsJustAText() {
        var lexer = new TemplateLexer("{");
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(1, tokens.size());

        var token = tokens.get(0);
        assertEquals(TokenType.TEXT, token.type);
        assertEquals(0, token.start);
        assertEquals(1, token.end);
        assertEquals("{", token.lexeme);
    }

    @Test
    void unexpectedEnd() {
        var lexer = new TemplateLexer("{{ . ");
        var exception = assertThrows(TemplateLexerException.class, lexer::tokens);
        assertEquals("Unexpected end inside expression at position 5: {{ . ", exception.getMessage());
    }

    @Test
    void notClosedTemplate() {
        var lexer = new TemplateLexer("{{ true }");
        var exception = assertThrows(TemplateLexerException.class, lexer::tokens);
        assertEquals("Unexpected character '}' at position 8: {{ true }",  exception.getMessage());
    }

    @Test
    void staticText() {
        var template = String.format("'%s'", UUID.randomUUID());
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        var token = tokens.get(0);
        assertEquals(TokenType.TEXT, token.type);
        assertEquals(0, token.start);
        assertEquals(template.length(), token.end);
        assertEquals(template, token.lexeme);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{{",
            "{{?",
            "{{."
    })
    void unfinishedTemplateIsText(String prefix) {
        var string = UUID.randomUUID().toString();
        var template = String.format("%s '%s'", prefix, string);
        var lexer = new TemplateLexer(template);

        var exception = assertThrows(TemplateLexerException.class, lexer::tokens);
        assertEquals(String.format("Unterminated template: missing closing '}}' at position %d: %s", template.length(), template), exception.getMessage());
        assertEquals(template.length(), exception.getPosition());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "hello world!",
            "a",
            ""
    })
    void templateString(String string) {
        var template = String.format("{{ '%s' }}", string);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TokenType.STRING, token.type);
        assertEquals(3, token.start);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length(), token.end);
        assertEquals(string, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length() + 1, endToken.start);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "hello world!",
            "a",
            ""
    })
    void templateConcatString(String string) {
        var prefix = UUID.randomUUID().toString();
        var template = String.format("%s{{ '%s' }}", prefix, string);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(4, tokens.size());

        var prefixToken = tokens.get(0);
        assertEquals(TokenType.TEXT, prefixToken.type);
        assertEquals(0, prefixToken.start);
        assertEquals(prefix.length(), prefixToken.end);
        assertEquals(prefix, prefixToken.lexeme);

        var beginToken = tokens.get(1);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(prefix.length(), beginToken.start);
        assertEquals(prefix.length() + 2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(2);
        assertEquals(TokenType.STRING, token.type);
        assertEquals(prefix.length() + 2 + 1, token.start);
        assertEquals(prefix.length() + 2 + 1 + 2 + string.length(), token.end);
        assertEquals(string, token.lexeme);

        var endToken = tokens.get(3);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(prefix.length() + 2 + 1 + 2 + string.length() + 1, endToken.start);
        assertEquals(prefix.length() + 2 + 1 + 2 + string.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @ParameterizedTest
    @MethodSource("templateEscapingStringCases")
    void templateEscapingString(String string, String excepted) {
        var template = String.format("{{ '%s' }}", string);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TokenType.STRING, token.type);
        assertEquals(3, token.start);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length(), token.end);
        assertEquals(excepted, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length() + 1, endToken.start);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @ParameterizedTest
    @EnumSource(Keyword.class)
    void templateKeywords(Keyword keyword) {
        var lexem = keyword.getLexem();
        var template = String.format("{{ %s }}", lexem);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TokenType.KEYWORD, token.type);
        assertEquals(3, token.start);
        assertEquals(3 /* '{{ ' */ + lexem.length(), token.end);
        assertEquals(lexem, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 /* '{{ ' */ + lexem.length() + 1, endToken.start);
        assertEquals(3 /* '{{ ' */ + lexem.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "а",
            "п",
            "я",
            "%",
            "$"
    })
    void unexpectedCharacterInTemplate(String value) {
        var template = String.format("{{ %s }}", value);
        var lexer = new TemplateLexer(template);
        var exception = assertThrows(TemplateLexerException.class, lexer::tokens);
        assertEquals(String.format("Unexpected character '%s' at position 3: %s", value, template), exception.getMessage());
        assertEquals(3, exception.getPosition());
    }

    @Test
    void unterminatedStringLiteral() {
        var template = "{{\t 'text";
        var lexer = new TemplateLexer(template);
        var exception = assertThrows(TemplateLexerException.class, lexer::tokens);
        assertEquals(String.format("Unterminated string literal at position 9: %s", template), exception.getMessage());
        assertEquals(9, exception.getPosition());
    }

    @ParameterizedTest
    @ValueSource(
            ints = {
                    42,
                    0,
                    -42
            }
    )
    void templateInteger(int integer) {
        var template = String.format("{{ %d }}", integer);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TokenType.NUMBER, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + Integer.toString(integer).length(), token.end);
        assertEquals(Integer.toString(integer), token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 + Integer.toString(integer).length() + 1, endToken.start);
        assertEquals(3 + Integer.toString(integer).length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @ParameterizedTest
    @ValueSource(
            doubles = {
                    42.12,
                    0.0,
                    -42.12
            }
    )
    void templateDouble(double number) {
        var numberString = Double.toString(number);
        var template = String.format("{{ %s }}", numberString);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TokenType.NUMBER, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + numberString.length(), token.end);
        assertEquals(numberString, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 + numberString.length() + 1, endToken.start);
        assertEquals(3 + numberString.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "2E0",
                    "3E2",
                    "4.321768E3",
                    "-5.3E4",
                    "5.3E4",
                    "+5.3E4",
                    "6.72E9",
                    "2E-1",
                    "9.87E2",
                    "7.51E-9",

                    "2e0",
                    "3e2",
                    "4.321768e3",
                    "-5.3e4",
                    "5.3e4",
                    "+5.3e4",
                    "6.72e9",
                    "2e-1",
                    "9.87e2",
                    "7.51e-9"
            }
    )
    void templateScientificNumber(String number) {
        var template = String.format("{{ %s }}", number);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TokenType.NUMBER, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + number.length(), token.end);
        assertEquals(number, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 + number.length() + 1, endToken.start);
        assertEquals(3 + number.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void templateBoolean(boolean value) {
        var template = String.format("{{ %b }}", value);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TokenType.BOOLEAN, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + Boolean.toString(value).length(), token.end);
        assertEquals(Boolean.toString(value), token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 + Boolean.toString(value).length() + 1, endToken.start);
        assertEquals(3 + Boolean.toString(value).length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @Test
    void templateVariable() {
        var varName = "varName";
        var template = String.format("{{ .%s }}", varName);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(4, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var dotToken = tokens.get(1);
        assertEquals(TokenType.DOT, dotToken.type);
        assertEquals(3, dotToken.start);
        assertEquals(3 + 1, dotToken.end);
        assertEquals(".", dotToken.lexeme);

        var token = tokens.get(2);
        assertEquals(TokenType.IDENT, token.type);
        assertEquals(3 + 1, token.start);
        assertEquals(3 + 1 + varName.length(), token.end);
        assertEquals(varName, token.lexeme);

        var endToken = tokens.get(3);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 + 1 + varName.length() + 1, endToken.start);
        assertEquals(3 + 1 + varName.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @Test
    void templateExpressionInParenthesis() {
        var template = "{{ (true ) }}";
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(5, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var lParentToken = tokens.get(1);
        assertEquals(TokenType.LPAREN, lParentToken.type);
        assertEquals(3, lParentToken.start);
        assertEquals(3 + 1, lParentToken.end);
        assertEquals("(", lParentToken.lexeme);

        var boolToken = tokens.get(2);
        assertEquals(TokenType.BOOLEAN, boolToken.type);
        assertEquals(3 + 1, boolToken.start);
        assertEquals(3 + 1 + "true".length(), boolToken.end);
        assertEquals("true", boolToken.lexeme);

        var rParentToken = tokens.get(3);
        assertEquals(TokenType.RPAREN, rParentToken.type);
        assertEquals(3 + 1 + "true".length() + 1, rParentToken.start);
        assertEquals(3 + 1 + "true".length() + 1 + 1, rParentToken.end);
        assertEquals(")", rParentToken.lexeme);

        var endToken = tokens.get(4);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 + 1 + "true".length() + 1 + 1 + 1, endToken.start);
        assertEquals(3 + 1 + "true".length() + 1 + 1 + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @Test
    void templateExpressionWithSeveralArgs() {
        var template = "{{ func(arg0,arg1) }}";
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(8, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var funcToken = tokens.get(1);
        assertEquals(TokenType.IDENT, funcToken.type);
        assertEquals("func", funcToken.lexeme);

        var lParentToken = tokens.get(2);
        assertEquals(TokenType.LPAREN, lParentToken.type);
        assertEquals("(", lParentToken.lexeme);

        var arg0Token = tokens.get(3);
        assertEquals(TokenType.IDENT, arg0Token.type);
        assertEquals("arg0", arg0Token.lexeme);

        var commaToken = tokens.get(4);
        assertEquals(TokenType.COMMA, commaToken.type);
        assertEquals(",", commaToken.lexeme);

        var arg1Token = tokens.get(5);
        assertEquals(TokenType.IDENT, arg1Token.type);
        assertEquals("arg1", arg1Token.lexeme);

        var rParentToken = tokens.get(6);
        assertEquals(TokenType.RPAREN, rParentToken.type);
        assertEquals(")", rParentToken.lexeme);

        var closeToken = tokens.get(7);
        assertEquals(TokenType.CLOSE, closeToken.type);
        assertEquals("}}", closeToken.lexeme);
    }

    @Test
    void templateExpressionWithTernaryOperator() {
        var template = "{{ true ? 'true-value' : 'false-value'  }}";
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(7, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var conditionToken = tokens.get(1);
        assertEquals(TokenType.BOOLEAN, conditionToken.type);
        assertEquals("true", conditionToken.lexeme);

        var questionToken = tokens.get(2);
        assertEquals(TokenType.QUESTION, questionToken.type);
        assertEquals("?", questionToken.lexeme);

        var trueStatementToken = tokens.get(3);
        assertEquals(TokenType.STRING, trueStatementToken.type);
        assertEquals("true-value", trueStatementToken.lexeme);

        var statementDividerToken = tokens.get(4);
        assertEquals(TokenType.COLON, statementDividerToken.type);
        assertEquals(":", statementDividerToken.lexeme);

        var falseStatementToken = tokens.get(5);
        assertEquals(TokenType.STRING, falseStatementToken.type);
        assertEquals("false-value", falseStatementToken.lexeme);

        var closeToken = tokens.get(6);
        assertEquals(TokenType.CLOSE, closeToken.type);
        assertEquals("}}", closeToken.lexeme);
    }

    @Test
    void templateNull() {
        var lexeme = "null";
        var template = String.format("{{ %s }}", lexeme);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TokenType.NULL, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + lexeme.length(), token.end);
        assertEquals(lexeme, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 + lexeme.length() + 1, endToken.start);
        assertEquals(3 + lexeme.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @Test
    void templatePipeOperator() {
        var leftLexem = "leftLexem";
        var rightLexem = "rightLexem";
        var template = String.format("{{ %s | %s }}", leftLexem, rightLexem);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(5, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var leftToken = tokens.get(1);
        assertEquals(TokenType.IDENT, leftToken.type);
        assertEquals(3, leftToken.start);
        assertEquals(3 + leftLexem.length(), leftToken.end);
        assertEquals(leftLexem, leftToken.lexeme);

        var pipeToken = tokens.get(2);
        assertEquals(TokenType.PIPE, pipeToken.type);
        assertEquals(3 + leftLexem.length() + 1, pipeToken.start);
        assertEquals(3 + leftLexem.length() + 2, pipeToken.end);
        assertEquals("|", pipeToken.lexeme);

        var rightToken = tokens.get(3);
        assertEquals(TokenType.IDENT, rightToken.type);
        assertEquals(3 + leftLexem.length() + 2 + 1, rightToken.start);
        assertEquals(3 + leftLexem.length() + 2 + 1 + rightLexem.length(), rightToken.end);
        assertEquals(rightLexem, rightToken.lexeme);

        var endToken = tokens.get(4);
        assertEquals(TokenType.CLOSE, endToken.type);
        assertEquals(3 + leftLexem.length() + 2 + 1 + rightLexem.length() + 1, endToken.start);
        assertEquals(3 + leftLexem.length() + 2 + 1 + rightLexem.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    public static Stream<Arguments> templateEscapingStringCases() {
        return Stream.of(
                Arguments.of("first\\n second", "first\n second"),
                Arguments.of("first\\r second", "first\r second"),
                Arguments.of("first\\t second", "first\t second"),
                Arguments.of("first\\b second", "first\b second"),
                Arguments.of("first\\f second", "first\f second"),
                Arguments.of("first\\\\second", "first\\second"),
                Arguments.of("first\\'second", "first'second"),
                Arguments.of("first\\\"second", "first\"second"),
                Arguments.of("first\\o second", "firsto second")
        );
    }

}