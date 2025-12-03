package io.github.sibmaks.jjtemplate.compiler.impl;

import io.github.sibmaks.jjtemplate.compiler.api.CompiledTemplate;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Implementation of {@link CompiledTemplate} that return a static compiled template.
 *
 * @author sibmaks
 * @since 0.1.2
 */
@AllArgsConstructor
public final class StaticCompiledTemplateImpl implements CompiledTemplate {

    /**
     * The compiled template.
     */
    private final Object compiledTemplate;

    @Override
    public Object render(Map<String, Object> context) {
        return compiledTemplate;
    }

}