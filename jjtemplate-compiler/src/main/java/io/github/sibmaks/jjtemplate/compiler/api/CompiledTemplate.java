package io.github.sibmaks.jjtemplate.compiler.api;

import java.util.Map;

/**
 * Represents a precompiled template that can be rendered with or without a context.
 * <p>
 * Implementations of this interface are responsible for evaluating the compiled
 * expression tree or template structure and producing a rendered result.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
public interface CompiledTemplate {

    /**
     * Render template with some initial context
     *
     * @param context initial variable set
     * @return rendered template
     */
    Object render(Map<String, Object> context);

    /**
     * Render template without initial context
     *
     * @return rendered template
     */
    default Object render() {
        return render(Map.of());
    }

}
