package io.github.sibmaks.jjtemplate.compiler.api;

import java.util.Map;

/**
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
