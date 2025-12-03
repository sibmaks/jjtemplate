package io.github.sibmaks.jjtemplate.frontend.exception;

import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Thrown when a template parse error occurs.
 *
 * @author sibmaks
 * @since 0.5.0
 */
public class TemplateParseException extends ParseCancellationException {
    /**
     * Creates a new instance with the specified message.
     *
     * @param message error message
     */
    public TemplateParseException(String message) {
        super(message);
    }
}