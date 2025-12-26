package io.github.sibmaks.jjtemplate.parser.exception;

/**
 * Thrown when a template parse error occurs.
 *
 * @author sibmaks
 * @since 0.5.0
 */
public class TemplateParseException extends RuntimeException {
    /**
     * Creates a new instance with the specified message.
     *
     * @param message error message
     */
    public TemplateParseException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the specified message and cause.
     *
     * @param message error message
     * @param cause   error cause
     */
    public TemplateParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
