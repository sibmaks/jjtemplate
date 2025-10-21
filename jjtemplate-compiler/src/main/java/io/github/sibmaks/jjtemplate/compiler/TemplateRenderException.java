package io.github.sibmaks.jjtemplate.compiler;

public final class TemplateRenderException extends RuntimeException {
    public TemplateRenderException(String message) {
        super(message);
    }

    public TemplateRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
