package io.github.sibmaks.jjtemplate.lexer.api;

import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import lombok.ToString;

/**
 * Represents a single lexical token produced by the {@link TemplateLexer}.
 * <p>
 * A token contains its type, the matched text (lexeme), and positional
 * information within the original template source.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */

@ToString
public final class Token {
    /**
     * The type of this token.
     */
    public final TokenType type;

    /**
     * The textual value (lexeme) of the token as it appears in the source.
     */
    public final String lexeme;

    /**
     * The starting position (inclusive) of the token in the source text.
     */
    public final int start;

    /**
     * The ending position (exclusive) of the token in the source text.
     */
    public final int end;

    /**
     * Creates a token with its type, text, and source range.
     *
     * @param type token type
     * @param lexeme matched source text
     * @param start inclusive start offset
     * @param end exclusive end offset
     */
    public Token(TokenType type, String lexeme, int start, int end) {
        this.type = type;
        this.lexeme = lexeme;
        this.start = start;
        this.end = end;
    }

}
