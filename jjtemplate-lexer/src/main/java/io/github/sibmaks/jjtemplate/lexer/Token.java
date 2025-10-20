package io.github.sibmaks.jjtemplate.lexer;

/**
 * @author sibmaks
 */
public final class Token {
    public final TokenType type;
    public final String lexeme;
    public final int start;
    public final int end;

    public Token(TokenType type, String lexeme, int start, int end) {
        this.type = type;
        this.lexeme = lexeme;
        this.start = start;
        this.end = end;
    }

    public String toString() {
        return type + (lexeme != null ? ("(" + lexeme + ")") : "") + "@" + start + ".." + end;
    }
}