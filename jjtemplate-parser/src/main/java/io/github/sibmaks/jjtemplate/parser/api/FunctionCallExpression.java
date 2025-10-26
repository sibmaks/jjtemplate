package io.github.sibmaks.jjtemplate.parser.api;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class FunctionCallExpression implements Expression {
    /**
     * The name of the function being called.
     */
    public final String name;
    /**
     * The list of argument expressions passed to the function.
     */
    public final List<Expression> args;

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitFunction(this);
    }
}