package io.github.sibmaks.jjtemplate.parser.api;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class FunctionCallExpression implements Expression {
    public final String name;
    public final List<Expression> args;

    public FunctionCallExpression(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }

    public String toString() {
        return name + args;
    }
}