package io.github.sibmaks.jjtemplate.evaulator;

import io.github.sibmaks.jjtemplate.evaulator.fun.ExpressionValue;

import java.util.Map;

/**
 * @author sibmaks
 */
public final class Context {
    private final Map<String, Object> vars;

    public Context(Map<String, Object> vars) {
        this.vars = vars == null ? Map.of() : vars;
    }

    public ExpressionValue getRoot(String name) {
        if (!vars.containsKey(name)) {
            return ExpressionValue.empty();
        }
        return ExpressionValue.of(vars.get(name));
    }
}