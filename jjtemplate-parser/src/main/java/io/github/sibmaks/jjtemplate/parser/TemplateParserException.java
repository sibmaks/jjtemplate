package io.github.sibmaks.jjtemplate.parser;


/**
 * Exception thrown when a parsing error occurs in a template.
 * <p>
 * Includes the error message and the position of the token where the error was detected.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class TemplateParserException extends RuntimeException {
    /**
     * Constructs a new {@code TemplateParserException} with the specified error message
     * and the token position where the error occurred.
     *
     * @param message the detail error message
     * @param pos     the token position at which the error was detected
     */
    public TemplateParserException(String message, int pos) {
        super(message + " at token position " + pos);
    }
}