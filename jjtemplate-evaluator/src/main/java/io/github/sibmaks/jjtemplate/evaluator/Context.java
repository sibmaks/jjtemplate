package io.github.sibmaks.jjtemplate.evaluator;

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

    public Object getRoot(String name) {
        return vars.get(name);
    }

    public static Context empty() {
        return EMPTY;
    }

}