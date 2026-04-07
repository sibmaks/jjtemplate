package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.api.Keyword;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateParserCursorTest {

    @Test
    void cursorShouldNavigateAcrossTokens() {
        var tokens = List.of(
                new Token(TokenType.KEYWORD, "range", 0, 5),
                new Token(TokenType.IDENT, "item", 6, 10),
                new Token(TokenType.CLOSE, "}}", 10, 12)
        );
        var cursor = new TemplateParserCursor(tokens);

        assertSame(tokens, cursor.tokens());
        assertEquals(0, cursor.position());
        assertTrue(cursor.hasMore());
        assertTrue(cursor.check(TokenType.KEYWORD));
        assertTrue(cursor.checkKeyword(Keyword.RANGE));
        assertTrue(cursor.checkNext(1, TokenType.IDENT));
        assertFalse(cursor.checkNext(3, TokenType.IDENT));
        assertFalse(cursor.checkNextKeyword(Keyword.SWITCH));

        assertTrue(cursor.matchKeyword(Keyword.RANGE));
        assertEquals(1, cursor.position());

        var ident = cursor.expect(TokenType.IDENT, "identifier");
        assertEquals("item", ident.lexeme);

        cursor.setPosition(2);
        assertTrue(cursor.check(TokenType.CLOSE));
        assertSame(tokens.get(2), cursor.advance());
        assertFalse(cursor.hasMore());
        cursor.expectEnd();
    }

    @Test
    void cursorShouldReportMeaningfulErrors() {
        var token = new Token(TokenType.IDENT, "value", 0, 5);
        var cursor = new TemplateParserCursor(List.of(token));

        var expectKeywordException = assertThrows(
                TemplateParserException.class,
                () -> cursor.expectKeyword(Keyword.SWITCH, "switch")
        );
        assertEquals("Expected keyword 'switch' at token position 0", expectKeywordException.getMessage());

        var expectTypeException = assertThrows(
                TemplateParserException.class,
                () -> cursor.expect(TokenType.STRING, "string")
        );
        assertEquals("Expected string, got IDENT at token position 0", expectTypeException.getMessage());

        var endCursor = new TemplateParserCursor(List.of());
        var reachedEndException = assertThrows(
                TemplateParserException.class,
                () -> endCursor.expect(TokenType.STRING, "string")
        );
        assertEquals("Expected string, but reached end at token position 0", reachedEndException.getMessage());
    }

    @Test
    void cursorExpectEndShouldHandleNullTokenAndRemainingToken() {
        var cursorWithNull = new TemplateParserCursor(Arrays.asList((Token) null));
        var nullTokenException = assertThrows(TemplateParserException.class, cursorWithNull::expectEnd);
        assertEquals("Excepted end of expression at token position 0", nullTokenException.getMessage());

        var cursorWithToken = new TemplateParserCursor(List.of(new Token(TokenType.IDENT, "value", 0, 5)));
        var tokenException = assertThrows(TemplateParserException.class, cursorWithToken::expectEnd);
        assertEquals("Unexpected token: IDENT at token position 0", tokenException.getMessage());
    }

    @Test
    void cursorShouldReturnCustomError() {
        var cursor = new TemplateParserCursor(List.of());

        var exception = cursor.error("boom");

        assertEquals("boom at token position 0", exception.getMessage());
    }
}
