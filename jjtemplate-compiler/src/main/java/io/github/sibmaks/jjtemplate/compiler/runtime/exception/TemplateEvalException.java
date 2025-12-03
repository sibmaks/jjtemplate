package io.github.sibmaks.jjtemplate.compiler.runtime.exception;

/**
 * Thrown when a template evaluation error occurs.
 *
 * @author sibmaks
 * @since 0.0.1
 */
public class TemplateEvalException extends RuntimeException {
    /**
     * Creates a new instance with the specified message.
     *
     * @param message error message
     */
    public TemplateEvalException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the specified message and cause.
     *
     * @param message error message
     * @param cause   the underlying cause
     */
    public TemplateEvalException(String message, Throwable cause) {
        super(message, cause);
    }
}