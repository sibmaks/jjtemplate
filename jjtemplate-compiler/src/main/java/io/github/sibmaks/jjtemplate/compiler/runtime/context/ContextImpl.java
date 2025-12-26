package io.github.sibmaks.jjtemplate.compiler.runtime.context;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@code ContextImpl} represents an evaluation-time variable scope.
 * <p>
 * It provides access to variables used during template expression evaluation
 * and acts as a mutable container for intermediate and computed values.
 * <p>
 * A context is typically created per template rendering and may be populated
 * both from the initial input data and from internally evaluated template
 * definitions.
 *
 * @author sibmaks
 * @see io.github.sibmaks.jjtemplate.compiler.runtime.expression.TemplateExpression
 * @since 0.0.1
 */
final class ContextImpl implements Context {
    private final List<Map<String, Object>> layers;

    /**
     * Creates a new evaluation context backed by the given variable map.
     *
     * @param variables map used to store and resolve variables during evaluation
     */
    public ContextImpl(Map<String, Object> variables) {
        this.layers = new LinkedList<>();
        this.layers.add(variables);
    }

    @Override
    public Object getRoot(String name) {
        for (var layer : layers) {
            if (layer.containsKey(name)) {
                return layer.get(name);
            }
        }
        return null;
    }

    @Override
    public void in(Map<String, Object> child) {
        layers.add(0, child);
    }

    @Override
    public void out() {
        layers.remove(0);
    }

}