package io.github.sibmaks.jjtemplate.compiler.runtime.exception;

import io.github.sibmaks.jjtemplate.compiler.runtime.fun.TemplateFunction;

/**
 * Thrown when a template function evaluation error occurs.
 *
 * @author sibmaks
 * @since 0.4.0
 */
public final class TemplateFunctionEvalException extends TemplateEvalException {
    /**
     * Creates a new instance with the specified function and message.
     *
     * @param caused  the function that caused the error
     * @param message error message
     */
    public TemplateFunctionEvalException(TemplateFunction<?> caused, String message) {
        super(buildName(caused, message));
    }

    /**
     * Creates a new instance with the specified function, message and cause.
     *
     * @param caused  the function that caused the error
     * @param message error message
     * @param cause   underlying cause
     */
    public TemplateFunctionEvalException(TemplateFunction<?> caused, String message, Throwable cause) {
        super(buildName(caused, message), cause);
    }

    /**
     * Builds a readable error message prefix using the function namespace and name.
     *
     * @param caused  the function that caused the error
     * @param message error message
     * @return formatted message
     */
    private static String buildName(TemplateFunction<?> caused, String message) {
        var namespace = caused.getNamespace();
        var name = caused.getName();
        if (namespace == null || namespace.isEmpty()) {
            return String.format("%s: %s", name, message);
        }
        return String.format("%s:%s: %s", namespace, name, message);
    }
}