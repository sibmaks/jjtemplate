package io.github.sibmaks.jjtemplate.evaluator;

import io.github.sibmaks.jjtemplate.evaluator.fun.ExpressionValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sibmaks
 */
public final class Context {
    private final Map<String, Object> vars;
    private final Context parent;

    public Context(Map<String, Object> vars) {
        this(null, vars);
    }

    private Context(Context parent, Map<String, Object> vars) {
        this.parent = parent;
        this.vars = vars == null ? Map.of() : vars;
    }

    public ExpressionValue getRoot(String name) {
        if (!vars.containsKey(name)) {
            if (parent != null) {
                return parent.getRoot(name);
            }
            return ExpressionValue.empty();
        }
        return ExpressionValue.of(vars.get(name));
    }

    public Context kid() {
        return new Context(this, new HashMap<>());
    }
}