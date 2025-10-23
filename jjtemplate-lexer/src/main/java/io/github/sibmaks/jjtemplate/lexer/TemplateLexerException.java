package io.github.sibmaks.jjtemplate.lexer;

import lombok.Getter;

/**
 * Lexing mistake in template source
 *
 * @author sibmaks
 */
@Getter
public class TemplateLexerException extends RuntimeException {
    /**
     * Mistake position in template
     */
    private final int position;

    public TemplateLexerException(String input, String message, int position) {
        super(message + " at position " + position + ": " + input);
        this.position = position;
    }

}