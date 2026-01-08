package io.github.sibmaks.jjtemplate.compiler.exception;

/**
 * Thrown when a template compilation error occurs.
 *
 * @author sibmaks
 * @since 0.5.0
 */
public final class TemplateCompilationException extends RuntimeException {
    /**
     * Creates a new instance with the specified message.
     *
     * @param message error message
     */
    public TemplateCompilationException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the specified message and cause.
     *
     * @param message error message
     * @param cause   the underlying cause
     */
    public TemplateCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}