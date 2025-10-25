package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;

import java.util.Map;

/**
 * @author sibmaks
 */
public final class Context {
    private static final Context EMPTY = new Context();

    private final Map<String, Object> vars;

    private Context() {
        this.vars = Map.of();
    }

    public Context(Map<String, Object> vars) {
        this.vars = vars == null ? Map.of() : vars;
    }

    public ExpressionValue getRoot(String name) {
        if (!vars.containsKey(name)) {
            return ExpressionValue.empty();
        }
        return ExpressionValue.of(vars.get(name));
    }

    public static Context empty() {
        return EMPTY;
    }

}