package io.github.sibmaks.jjtemplate.parser.api;

import java.util.List;

/**
 *
 * @author sibmaks
 */
public class PipeExpression implements Expression {
    public final Expression left;
    public final List<FunctionCallExpression> chain;

    public PipeExpression(Expression left, List<FunctionCallExpression> chain) {
        this.left = left;
        this.chain = chain;
    }

    public String toString() {
        return left + " | " + chain;
    }
}
