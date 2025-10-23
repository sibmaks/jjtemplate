package io.github.sibmaks.jjtemplate.parser.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * Represents a function call expression within a template.
 * <p>
 * Contains the function name and a list of argument expressions.
 * </p>
 *
 * @author sibmaks
 * @since 0.0.1
 */
@ToString
@EqualsAndHashCode
public class FunctionCallExpression implements Expression {
    /**
     * The name of the function being called.
     */
    public final String name;
    /**
     * The list of argument expressions passed to the function.
     */
    public final List<Expression> args;

    /**
     * Creates a new {@code FunctionCallExpression} instance.
     *
     * @param name the name of the function
     * @param args the list of argument expressions
     */
    public FunctionCallExpression(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }
}