package io.github.sibmaks.jjtemplate.lexer.api;

import io.github.sibmaks.jjtemplate.lexer.TemplateLexer;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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

}