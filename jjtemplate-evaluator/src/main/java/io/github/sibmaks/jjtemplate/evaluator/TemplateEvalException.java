package io.github.sibmaks.jjtemplate.evaluator;

public class TemplateEvalException extends RuntimeException {
    public TemplateEvalException(String message) {
        super(message);
    }

    public TemplateEvalException(String message, Throwable cause) {
        super(message, cause);
    }
}