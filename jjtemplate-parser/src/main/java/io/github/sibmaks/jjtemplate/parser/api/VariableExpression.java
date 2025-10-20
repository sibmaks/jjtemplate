package io.github.sibmaks.jjtemplate.parser.api;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class VariableExpression implements Expression {
    public final List<String> path;

    public VariableExpression(List<String> path) {
        this.path = path;
    }

    public String toString() {
        return "." + String.join(".", path);
    }
}
