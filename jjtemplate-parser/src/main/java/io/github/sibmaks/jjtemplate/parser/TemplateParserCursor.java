package io.github.sibmaks.jjtemplate.parser;

import io.github.sibmaks.jjtemplate.lexer.api.Keyword;
import io.github.sibmaks.jjtemplate.lexer.api.Token;
import io.github.sibmaks.jjtemplate.lexer.api.TokenType;

import java.util.List;

/**
 * @author sibmaks
 * @since 0.9.0
 */
final class TemplateParserCursor {
    private final List<Token> tokens;
    private int pos;

    TemplateParserCursor(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Token> tokens() {
        return tokens;
    }

    int position() {
        return pos;
    }

    void setPosition(int pos) {
        this.pos = pos;
    }

    boolean hasMore() {
        return pos < tokens.size();
    }

    void expectEnd() {
        if (pos < tokens.size()) {
            var token = peek();
            if (token == null) {
                throw error("Excepted end of expression");
            }
            throw error("Unexpected token: " + token.type);
        }
    }

    /**
     * Checks whether the current token matches the given type and consumes it if so.
     *
     * @param type the token type to match
     * @return {@code true} if the token was matched and consumed, {@code false} otherwise
     */
    boolean check(TokenType type) {
        var token = peek();
        return token != null && token.type == type;
    }

    boolean checkNext(int offset, TokenType type) {
        var token = peekNext(offset);
        return token != null && token.type == type;
    }

    boolean checkNextKeyword(Keyword keyword) {
        var next = peekNext(1);
        return next != null && next.type == TokenType.KEYWORD && keyword.eq(next.lexeme);
    }

    boolean checkKeyword(Keyword keyword) {
        var token = peek();
        return token != null && token.type == TokenType.KEYWORD && keyword.eq(token.lexeme);
    }

    /**
     * Advances the parser to the next token.
     *
     * @return the previously current {@link Token}
     */
    boolean match(TokenType type) {
        if (!check(type)) {
            return false;
        }
        advance();
        return true;
    }

    boolean matchKeyword(Keyword keyword) {
        if (!checkKeyword(keyword)) {
            return false;
        }
        advance();
        return true;
    }

    void expectKeyword(Keyword keyword, String expected) {
        if (checkKeyword(keyword)) {
            advance();
            return;
        }
        throw error("Expected keyword '" + expected + "'");
    }

    /**
     * Ensures that the current token matches the expected type.
     * If not, throws a {@link TemplateParserException}.
     *
     * @param type the expected token type
     * @param msg  the message to include in the exception if the expectation fails
     * @return the consumed {@link Token}
     * @throws TemplateParserException if the expected token is missing or of a different type
     */
    Token expect(TokenType type, String msg) {
        var token = peek();
        if (token == null) {
            throw error("Expected " + msg + ", but reached end");
        }
        if (token.type != type) {
            throw error("Expected " + msg + ", got " + token.type);
        }
        advance();
        return token;
    }

    /**
     * Returns the current token without consuming it.
     *
     * @return the current {@link Token}, or {@code null} if end of stream is reached
     */
    Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }


    /**
     * Advances the parser to the next token.
     *
     * @return the previously current {@link Token}
     */
    Token advance() {
        return tokens.get(pos++);
    }

    /**
     * Creates a new {@link TemplateParserException} with the given message and current position.
     *
     * @param msg the detailed error message
     * @return a new parser exception
     */
    TemplateParserException error(String msg) {
        return new TemplateParserException(msg, pos);
    }

    private Token peekNext(int offset) {
        return pos + offset < tokens.size() ? tokens.get(pos + offset) : null;
    }
}
