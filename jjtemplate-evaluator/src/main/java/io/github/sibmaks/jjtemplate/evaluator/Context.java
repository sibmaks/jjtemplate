package io.github.sibmaks.jjtemplate.evaluator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author sibmaks
 */
public final class Context {
    private static final Context EMPTY = new Context(Map.of());

    private final List<Map<String, Object>> innerVars;

    public Context(Map<String, Object> vars) {
        this.innerVars = new LinkedList<>();
        if (vars != null) {
            innerVars.add(vars);
        }
    }

    public static Context empty() {
        return EMPTY;
    }

    public Object getRoot(String name) {
        for (var innerVar : innerVars) {
            if (innerVar.containsKey(name)) {
                return innerVar.get(name);
            }
        }
        return null;
    }

    public void in(Map<String, Object> child) {
        innerVars.add(0, child);
    }

    public void out() {
        innerVars.remove(0);
    }
}