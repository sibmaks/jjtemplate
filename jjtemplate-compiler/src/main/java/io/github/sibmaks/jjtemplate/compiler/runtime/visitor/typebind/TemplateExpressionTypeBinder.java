package io.github.sibmaks.jjtemplate.compiler.runtime.visitor.typebind;

import io.github.sibmaks.jjtemplate.compiler.api.TemplateCompileContext;
import io.github.sibmaks.jjtemplate.compiler.impl.CompiledTemplateImpl;

/**
 * Performs compile-time validation and partial binding of typed variable access chains.
 *
 * @author sibmaks
 * @since 0.9.0
 */
public final class TemplateExpressionTypeBinder {
    private final TemplateExpressionBindingEngine bindingEngine;

    /**
     * Creates a binder for the provided compile-time context.
     *
     * @param compileContext compile-time type source
     */
    public TemplateExpressionTypeBinder(TemplateCompileContext compileContext) {
        this.bindingEngine = new TemplateExpressionBindingEngine(compileContext);
    }

    /**
     * Applies compile-time binding and validation to a compiled template tree.
     *
     * @param compiledTemplate compiled template
     * @return bound template
     */
    public CompiledTemplateImpl bind(CompiledTemplateImpl compiledTemplate) {
        return bindingEngine.bind(compiledTemplate);
    }
}
