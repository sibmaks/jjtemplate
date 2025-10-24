package io.github.sibmaks.jjtemplate.lexer;

import lombok.Getter;

/**
 * Exception thrown when a lexical error occurs while parsing a template.
 * <p>
 * Provides the error message and the position in the input where
 * the lexing process failed.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
public class TemplateLexerException extends RuntimeException {
    /**
     * The position in the template source where the lexing error occurred.
     */
    private final int position;

    /**
     * Constructs a new {@code TemplateLexerException}.
     *
     * @param input    the original template input being processed
     * @param message  the detail error message
     * @param position the position in the input where the error occurred
     */
    public TemplateLexerException(String input, String message, int position) {
        super(message + " at position " + position + ": " + input);
        this.position = position;
    }

}