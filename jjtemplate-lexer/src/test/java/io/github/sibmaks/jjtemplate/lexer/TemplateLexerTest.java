package io.github.sibmaks.jjtemplate.lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author sibmaks
 */
class TemplateLexerTest {

    @Test
    void staticText() {
        var template = String.format("'%s'", UUID.randomUUID());
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
        var token = tokens.get(0);
        assertEquals(TemplateLexer.TokenType.TEXT, token.type);
        assertEquals(0, token.start);
        assertEquals(template.length(), token.end);
        assertEquals(template, token.lexeme);
    }

    @ParameterizedTest
    @MethodSource("unfinishedTemplateIsTextCases")
    void unfinishedTemplateIsText(String prefix, TemplateLexer.TokenType exceptedToken) {
        var string = UUID.randomUUID().toString();
        var template = String.format("%s '%s'", prefix, string);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(2, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(exceptedToken, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(prefix.length(), beginToken.end);
        assertEquals(prefix, beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.STRING, token.type);
        assertEquals(prefix.length() + 1, token.start);
        assertEquals(prefix.length() + 1 + 2 + string.length(), token.end);
        assertEquals(string, token.lexeme);
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
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.STRING, token.type);
        assertEquals(3, token.start);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length(), token.end);
        assertEquals(string, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
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
        assertEquals(TemplateLexer.TokenType.TEXT, prefixToken.type);
        assertEquals(0, prefixToken.start);
        assertEquals(prefix.length(), prefixToken.end);
        assertEquals(prefix, prefixToken.lexeme);

        var beginToken = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(prefix.length(), beginToken.start);
        assertEquals(prefix.length() + 2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.STRING, token.type);
        assertEquals(prefix.length() + 2 + 1, token.start);
        assertEquals(prefix.length() + 2 + 1 + 2 + string.length(), token.end);
        assertEquals(string, token.lexeme);

        var endToken = tokens.get(3);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
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
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.STRING, token.type);
        assertEquals(3, token.start);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length(), token.end);
        assertEquals(excepted, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length() + 1, endToken.start);
        assertEquals(3 /* '{{ ' */ + 2 /* ''' */ + string.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "case", "then", "else", "range", "of"
    })
    void templateKeywords(String string) {
        var template = String.format("{{ %s }}", string);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.KEYWORD, token.type);
        assertEquals(3, token.start);
        assertEquals(3 /* '{{ ' */ + string.length(), token.end);
        assertEquals(string, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
        assertEquals(3 /* '{{ ' */ + string.length() + 1, endToken.start);
        assertEquals(3 /* '{{ ' */ + string.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
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
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.NUMBER, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + Integer.toString(integer).length(), token.end);
        assertEquals(Integer.toString(integer), token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
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
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.NUMBER, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + numberString.length(), token.end);
        assertEquals(numberString, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
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
                    "7.51E-9"
            }
    )
    void templateScientificNumber(String number) {
        var template = String.format("{{ %s }}", number);
        var lexer = new TemplateLexer(template);
        var tokens = lexer.tokens();
        assertNotNull(tokens);
        assertEquals(3, tokens.size());

        var beginToken = tokens.get(0);
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.NUMBER, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + number.length(), token.end);
        assertEquals(number, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
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
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.BOOLEAN, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + Boolean.toString(value).length(), token.end);
        assertEquals(Boolean.toString(value), token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
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
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var dotToken = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.DOT, dotToken.type);
        assertEquals(3, dotToken.start);
        assertEquals(3 + 1, dotToken.end);
        assertEquals(".", dotToken.lexeme);

        var token = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.IDENT, token.type);
        assertEquals(3 + 1, token.start);
        assertEquals(3 + 1 + varName.length(), token.end);
        assertEquals(varName, token.lexeme);

        var endToken = tokens.get(3);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
        assertEquals(3 + 1 + varName.length() + 1, endToken.start);
        assertEquals(3 + 1 + varName.length() + 1 + 2, endToken.end);
        assertEquals("}}", endToken.lexeme);
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
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var token = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.NULL, token.type);
        assertEquals(3, token.start);
        assertEquals(3 + lexeme.length(), token.end);
        assertEquals(lexeme, token.lexeme);

        var endToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
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
        assertEquals(TemplateLexer.TokenType.OPEN_EXPR, beginToken.type);
        assertEquals(0, beginToken.start);
        assertEquals(2, beginToken.end);
        assertEquals("{{", beginToken.lexeme);

        var leftToken = tokens.get(1);
        assertEquals(TemplateLexer.TokenType.IDENT, leftToken.type);
        assertEquals(3, leftToken.start);
        assertEquals(3 + leftLexem.length(), leftToken.end);
        assertEquals(leftLexem, leftToken.lexeme);

        var pipeToken = tokens.get(2);
        assertEquals(TemplateLexer.TokenType.PIPE, pipeToken.type);
        assertEquals(3 + leftLexem.length() + 1, pipeToken.start);
        assertEquals(3 + leftLexem.length() + 2, pipeToken.end);
        assertEquals("|", pipeToken.lexeme);

        var rightToken = tokens.get(3);
        assertEquals(TemplateLexer.TokenType.IDENT, rightToken.type);
        assertEquals(3 + leftLexem.length() + 2 + 1, rightToken.start);
        assertEquals(3 + leftLexem.length() + 2 + 1 + rightLexem.length(), rightToken.end);
        assertEquals(rightLexem, rightToken.lexeme);

        var endToken = tokens.get(4);
        assertEquals(TemplateLexer.TokenType.CLOSE, endToken.type);
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

    public static Stream<Arguments> unfinishedTemplateIsTextCases() {
        return Stream.of(
                Arguments.of("{{", TemplateLexer.TokenType.OPEN_EXPR),
                Arguments.of("{{?", TemplateLexer.TokenType.OPEN_COND),
                Arguments.of("{{.", TemplateLexer.TokenType.OPEN_SPREAD)
        );
    }
}